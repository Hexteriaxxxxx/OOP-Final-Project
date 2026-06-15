package main.controllers;

import dao.DepartmentDAO;
import dao.EmployeeDAO;
import dao.UserDAO;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import main.utils.SessionManager;
import main.utils.SkeletonLoader;
import models.Employee;
import models.User;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class UserManagementController implements Initializable {

    // ── Existing fields ──────────────────────────────────────────
    @FXML private Label lblTotal, lblActive, lblInactive, lblAdmins;
    @FXML private Label lblSidebarUser, lblSidebarRole;
    @FXML private TextField tfSearch;
    @FXML private ComboBox<String> cbFilter;
    @FXML private StackPane skeletonContainer;
    @FXML private TableView<Employee>            tableEmployees;
    @FXML private TableColumn<Employee, Integer> colId;
    @FXML private TableColumn<Employee, String>  colName, colDept, colPosition;
    @FXML private TableColumn<Employee, Void>    colActions;
    @FXML private Button btnNotification;

    // ── Account Approval fields ──────────────────────────────────
    @FXML private TableView<User>            tableApprovals;
    @FXML private TableColumn<User, Integer> colApprovalId;
    @FXML private TableColumn<User, String>  colApprovalName, colApprovalEmail, colApprovalUsername, colApprovalRole, colApprovalStatus;
    @FXML private TableColumn<User, Void>    colApprovalActions;
    @FXML private Label lblPendingCount;

    // ── DAOs & Lists ─────────────────────────────────────────────
    private final EmployeeDAO   employeeDAO   = new EmployeeDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();
    private final UserDAO       userDAO       = new UserDAO();

    private final ObservableList<Employee> masterList    = FXCollections.observableArrayList();
    private final ObservableList<User>     approvalList  = FXCollections.observableArrayList();
    private FilteredList<Employee> filteredList;
    private NotificationHelper notifHelper;

    // ── Initialize ───────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        SessionManager.apply(this::initSession);
        setupFilterCombo();
        setupTableColumns();
        setupSearch();
        setupApprovalTable();
        SkeletonLoader.show(skeletonContainer);
        loadEmployeesAsync();
        loadAllUsersAsync();
    }

    // ── Existing: Load Employees ─────────────────────────────────
    private void loadEmployeesAsync() {
        Task<List<Employee>> task = new Task<>() {
            @Override protected List<Employee> call() { return employeeDAO.getAllEmployees(); }
        };
        task.setOnSucceeded(e -> {
            SkeletonLoader.hide(skeletonContainer);
            masterList.clear();
            if (task.getValue() != null) masterList.addAll(task.getValue());
            refreshStats();
        });
        task.setOnFailed(e -> SkeletonLoader.hide(skeletonContainer));
        new Thread(task, "UserMgmtLoader").start();
    }

    // ── UPDATED: Load ALL users (PENDING, ACTIVE, REJECTED) ─────
    private void loadAllUsersAsync() {
        Task<List<User>> task = new Task<>() {
            @Override protected List<User> call() { return userDAO.getAllUsersForApproval(); }
        };
        task.setOnSucceeded(e -> {
            approvalList.clear();
            if (task.getValue() != null) approvalList.addAll(task.getValue());
            long pendingCount = approvalList.stream()
                    .filter(u -> "PENDING".equalsIgnoreCase(u.getStatus()))
                    .count();
            if (lblPendingCount != null)
                lblPendingCount.setText(pendingCount + " pending");
        });
        new Thread(task, "AllUsersLoader").start();
    }

    // ── UPDATED: Setup Approval Table with Status column ─────────
    private void setupApprovalTable() {
        colApprovalId      .setCellValueFactory(new PropertyValueFactory<>("userId"));
        colApprovalName    .setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colApprovalEmail   .setCellValueFactory(new PropertyValueFactory<>("email"));
        colApprovalUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colApprovalRole    .setCellValueFactory(new PropertyValueFactory<>("role"));
        colApprovalStatus  .setCellValueFactory(new PropertyValueFactory<>("status"));

        // Color-coded Status column
        colApprovalStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setText(null); setStyle(""); return; }
                setText(status);
                switch (status.toUpperCase()) {
                    case "ACTIVE"   -> setStyle("-fx-text-fill:#15803d;-fx-font-weight:bold;");
                    case "REJECTED" -> setStyle("-fx-text-fill:#b91c1c;-fx-font-weight:bold;");
                    default         -> setStyle("-fx-text-fill:#b45309;-fx-font-weight:bold;"); // PENDING = orange
                }
            }
        });

        // Actions column — show buttons only for PENDING
        colApprovalActions.setCellFactory(col -> new TableCell<>() {
            final Button btnApprove = new Button("✔ Approve");
            final Button btnReject  = new Button("✘ Reject");
            final Label  lblDone    = new Label();
            {
                btnApprove.setStyle("-fx-background-color:#dcfce7;-fx-text-fill:#15803d;-fx-background-radius:5;-fx-cursor:hand;-fx-font-size:11px;-fx-padding:4 8;");
                btnReject .setStyle("-fx-background-color:#fee2e2;-fx-text-fill:#b91c1c;-fx-background-radius:5;-fx-cursor:hand;-fx-font-size:11px;-fx-padding:4 8;");
                btnApprove.setOnAction(e -> handleApprove(getTableView().getItems().get(getIndex())));
                btnReject .setOnAction(e -> handleReject (getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                User user = getTableView().getItems().get(getIndex());
                String status = user.getStatus();
                if ("PENDING".equalsIgnoreCase(status)) {
                    setGraphic(new HBox(6, btnApprove, btnReject));
                } else if ("ACTIVE".equalsIgnoreCase(status)) {
                    lblDone.setText("✔ Approved");
                    lblDone.setStyle("-fx-text-fill:#15803d;-fx-font-weight:bold;");
                    setGraphic(lblDone);
                } else {
                    lblDone.setText("✘ Rejected");
                    lblDone.setStyle("-fx-text-fill:#b91c1c;-fx-font-weight:bold;");
                    setGraphic(lblDone);
                }
            }
        });

        tableApprovals.setItems(approvalList);
    }

    // ── UPDATED: Approve Handler — refresh row, don't remove ─────
    private void handleApprove(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Approve Account");
        confirm.setHeaderText("Approve " + user.getFullName() + "?");
        confirm.setContentText("This will activate their account.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (userDAO.approveUser(user.getUserId())) {
                user.setStatus("ACTIVE");
                tableApprovals.refresh();
                updatePendingCount();
                showSuccess("Account of " + user.getFullName() + " has been approved!");
            } else {
                showError("Failed to approve account.");
            }
        }
    }

    // ── UPDATED: Reject Handler — refresh row, don't remove ──────
    private void handleReject(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Reject Account");
        confirm.setHeaderText("Reject " + user.getFullName() + "?");
        confirm.setContentText("Their account status will be set to REJECTED.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (userDAO.rejectUser(user.getUserId())) {
                user.setStatus("REJECTED");
                tableApprovals.refresh();
                updatePendingCount();
                showSuccess("Account of " + user.getFullName() + " has been rejected.");
            } else {
                showError("Failed to reject account.");
            }
        }
    }

    private void updatePendingCount() {
        long pendingCount = approvalList.stream()
                .filter(u -> "PENDING".equalsIgnoreCase(u.getStatus()))
                .count();
        if (lblPendingCount != null)
            lblPendingCount.setText(pendingCount + " pending");
    }

    // ── Existing methods (unchanged) ─────────────────────────────
    @FXML private void handleNotifications() {
        if (notifHelper == null) notifHelper = new NotificationHelper(btnNotification, NotificationHelper.Role.ADMIN);
        notifHelper.toggle();
    }

    public void initSession(String username, String role) {
        if (lblSidebarUser != null) lblSidebarUser.setText(username);
        if (lblSidebarRole != null) lblSidebarRole.setText(role);
    }

    private void setupFilterCombo() {
        List<String> depts = departmentDAO.getAllDepartmentNames();
        ObservableList<String> items = FXCollections.observableArrayList("All Departments");
        items.addAll(depts);
        cbFilter.setItems(items);
        cbFilter.setValue("All Departments");
        cbFilter.setOnAction(e -> applyFilter());
    }

    private void setupTableColumns() {
        colId      .setCellValueFactory(new PropertyValueFactory<>("empId"));
        colName    .setCellValueFactory(new PropertyValueFactory<>("name"));
        colDept    .setCellValueFactory(new PropertyValueFactory<>("department"));
        colPosition.setCellValueFactory(new PropertyValueFactory<>("position"));
        colActions.setCellFactory(col -> new TableCell<>() {
            final Button btnEdit   = new Button("✏");
            final Button btnDelete = new Button("🗑");
            { btnEdit.setStyle("-fx-background-color:#dbeafe;-fx-text-fill:#1d4ed8;-fx-background-radius:5;-fx-cursor:hand;-fx-min-width:28px;-fx-min-height:28px;");
                btnDelete.setStyle("-fx-background-color:#fee2e2;-fx-text-fill:#b91c1c;-fx-background-radius:5;-fx-cursor:hand;-fx-min-width:28px;-fx-min-height:28px;");
                btnEdit.setOnAction(e -> openEditDialog(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex()))); }
            @Override protected void updateItem(Void item, boolean empty) { super.updateItem(item, empty); if (empty) { setGraphic(null); return; } setGraphic(new HBox(6, btnEdit, btnDelete)); }
        });
        filteredList = new FilteredList<>(masterList, p -> true);
        tableEmployees.setItems(filteredList);
    }

    private void setupSearch() { tfSearch.textProperty().addListener((obs, o, n) -> applyFilter()); }

    private void applyFilter() {
        String query = tfSearch.getText()==null?"":tfSearch.getText().toLowerCase().trim();
        String dept  = cbFilter.getValue();
        filteredList.setPredicate(emp -> {
            boolean matchSearch = query.isEmpty() || String.valueOf(emp.getEmpId()).contains(query) || emp.getName().toLowerCase().contains(query) || emp.getDepartment().toLowerCase().contains(query) || emp.getPosition().toLowerCase().contains(query);
            boolean matchDept   = dept==null||dept.equals("All Departments")||emp.getDepartment().equalsIgnoreCase(dept);
            return matchSearch && matchDept;
        });
    }

    private void refreshStats() {
        int total = masterList.size();
        long uniqueDepts     = masterList.stream().map(Employee::getDepartment).filter(d->d!=null&&!d.isBlank()).distinct().count();
        long uniquePositions = masterList.stream().map(Employee::getPosition).filter(p->p!=null&&!p.isBlank()).distinct().count();
        long adminCount      = masterList.stream().filter(e->e.getPosition()!=null&&(e.getPosition().toLowerCase().contains("admin")||e.getPosition().toLowerCase().contains("director")||e.getPosition().toLowerCase().contains("officer"))).count();
        lblTotal.setText(String.valueOf(total)); lblActive.setText(String.valueOf(uniqueDepts));
        lblInactive.setText(String.valueOf(uniquePositions)); lblAdmins.setText(String.valueOf(adminCount));
        String current = cbFilter.getValue();
        List<String> depts = departmentDAO.getAllDepartmentNames();
        ObservableList<String> items = FXCollections.observableArrayList("All Departments"); items.addAll(depts);
        cbFilter.setItems(items);
        cbFilter.setValue(current != null && items.contains(current) ? current : "All Departments");
    }

    @FXML private void handleAddEmployee() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/fxml/AddEmployee.fxml"));
            Parent root = loader.load();
            AddEmployeeController ctrl = loader.getController();
            ctrl.setMasterList(masterList); ctrl.setOnSaveCallback(this::refreshStats);
            Stage dialog = new Stage(); dialog.initModality(Modality.APPLICATION_MODAL); dialog.initStyle(StageStyle.UNDECORATED);
            dialog.setScene(new javafx.scene.Scene(root)); ctrl.setDialogStage(dialog); dialog.showAndWait();
        } catch (IOException ex) { showError("Cannot open Add Employee dialog:\n" + ex.getMessage()); }
    }

    private void openEditDialog(Employee emp) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/fxml/EditEmployee.fxml"));
            Parent root = loader.load();
            EditEmployeeController ctrl = loader.getController();
            ctrl.setEmployee(emp); ctrl.setOnSaveCallback(() -> { tableEmployees.refresh(); refreshStats(); });
            Stage dialog = new Stage(); dialog.initModality(Modality.APPLICATION_MODAL); dialog.initStyle(StageStyle.UNDECORATED);
            dialog.setScene(new javafx.scene.Scene(root)); ctrl.setDialogStage(dialog); dialog.showAndWait();
        } catch (IOException ex) { showError("Cannot open Edit Employee dialog:\n" + ex.getMessage()); }
    }

    private void handleDelete(Employee emp) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION); confirm.setTitle("Delete Employee"); confirm.setHeaderText("Delete " + emp.getName() + "?"); confirm.setContentText("This action cannot be undone.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (employeeDAO.deleteEmployee(emp.getEmpId())) { masterList.remove(emp); refreshStats(); }
            else showError("Failed to delete employee.");
        }
    }

    @FXML private void handleNavDashboard() { goTo("/main/resources/fxml/AdminDashboard.fxml","Dashboard"); }
    @FXML private void handleNavPassSlip()  { goTo("/main/resources/fxml/PassSlipIssuance.fxml","Pass Slip Issuance"); }
    @FXML private void handleNavReports()   { goTo("/main/resources/fxml/Reports.fxml","Reports"); }
    @FXML private void handleNavUserMgmt()  { /* already here */ }
    @FXML private void handleLogout() {
        Optional<ButtonType> res = new Alert(Alert.AlertType.CONFIRMATION,"Are you sure you want to logout?",ButtonType.OK,ButtonType.CANCEL).showAndWait();
        if (res.isPresent()&&res.get()==ButtonType.OK) { SessionManager.clear(); goTo("/main/resources/fxml/Login.fxml","Login"); }
    }

    private void goTo(String fxml, String title) {
        try {
            Stage stage = (Stage) tableEmployees.getScene().getWindow();
            double w = stage.getWidth(), h = stage.getHeight();
            boolean wasFullscreen = stage.isFullScreen();
            boolean wasMaximized  = stage.isMaximized();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Object ctrl = loader.getController();

            String username = SessionManager.getUsername();
            String role     = SessionManager.getRole();
            if (ctrl instanceof ReportsController)               ((ReportsController) ctrl).initSession(username, role);
            else if (ctrl instanceof PassSlipIssuanceController) ((PassSlipIssuanceController) ctrl).initSession(username, role);
            // AdminDashboardController calls applySession() internally via SessionManager — no initSession needed

            Scene scene = new Scene(root, w, h);
            scene.setFill(Color.WHITE);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.setWidth(w);
            stage.setHeight(h);
            if (wasFullscreen) Platform.runLater(() -> stage.setFullScreen(true));
            else if (wasMaximized) Platform.runLater(() -> stage.setMaximized(true));
            stage.show();
        } catch (IOException e) {
            showError("Screen not available:\n" + e.getMessage());
        }
    }

    private void showError(String msg)   { Alert a=new Alert(Alert.AlertType.ERROR);a.setTitle("Error");a.setHeaderText(null);a.setContentText(msg);a.showAndWait(); }
    private void showSuccess(String msg) { Alert a=new Alert(Alert.AlertType.INFORMATION);a.setTitle("Success");a.setHeaderText(null);a.setContentText(msg);a.showAndWait(); }
}