package main.controllers;

import dao.EmployeeDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import models.Employee;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class UserManagementController implements Initializable {

    @FXML private Label lblTotal, lblActive, lblInactive, lblAdmins;
    @FXML private TextField tfSearch;
    @FXML private ComboBox<String> cbFilter;
    @FXML private TableView<Employee>          tableEmployees;
    @FXML private TableColumn<Employee, Integer> colId;
    @FXML private TableColumn<Employee, String>  colName, colDept, colPosition;
    @FXML private TableColumn<Employee, Void>    colActions;
    @FXML private Button btnNotification;

    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final ObservableList<Employee> masterList = FXCollections.observableArrayList();
    private FilteredList<Employee> filteredList;
    private NotificationHelper notifHelper;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadEmployees();
        setupFilterCombo();
        setupTableColumns();
        setupSearch();
        refreshStats();
    }

    private void loadEmployees() {
        masterList.clear();
        List<Employee> employees = employeeDAO.getAllEmployees();
        if (employees != null) masterList.addAll(employees);
    }

    @FXML
    private void handleNotifications() {
        if (notifHelper == null)
            notifHelper = new NotificationHelper(btnNotification, NotificationHelper.Role.ADMIN);
        notifHelper.toggle();
    }

    private void setupFilterCombo() {
        cbFilter.setItems(FXCollections.observableArrayList(
                "All Departments","IT Department","HR Department","Finance","Marketing","Operations"));
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
            {
                btnEdit  .setStyle("-fx-background-color:#dbeafe;-fx-text-fill:#1d4ed8;-fx-background-radius:5;-fx-cursor:hand;-fx-min-width:28px;-fx-min-height:28px;");
                btnDelete.setStyle("-fx-background-color:#fee2e2;-fx-text-fill:#b91c1c;-fx-background-radius:5;-fx-cursor:hand;-fx-min-width:28px;-fx-min-height:28px;");
                btnEdit  .setOnAction(e -> openEditDialog(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                setGraphic(new HBox(6, btnEdit, btnDelete));
            }
        });

        filteredList = new FilteredList<>(masterList, p -> true);
        tableEmployees.setItems(filteredList);
    }

    private void setupSearch() {
        tfSearch.textProperty().addListener((obs, o, n) -> applyFilter());
    }

    private void applyFilter() {
        String query = tfSearch.getText()==null?"":tfSearch.getText().toLowerCase().trim();
        String dept  = cbFilter.getValue();
        filteredList.setPredicate(emp -> {
            boolean matchSearch = query.isEmpty()
                    || String.valueOf(emp.getEmpId()).contains(query)
                    || emp.getName()      .toLowerCase().contains(query)
                    || emp.getDepartment().toLowerCase().contains(query)
                    || emp.getPosition()  .toLowerCase().contains(query);
            boolean matchDept = dept==null||dept.equals("All Departments")||emp.getDepartment().equals(dept);
            return matchSearch && matchDept;
        });
    }

    private void refreshStats() {
        lblTotal   .setText(String.valueOf(masterList.size()));
        lblActive  .setText(String.valueOf(masterList.stream().filter(e -> e.getDepartment().contains("IT")||e.getDepartment().contains("HR")).count()));
        lblInactive.setText(String.valueOf(masterList.stream().filter(e -> e.getDepartment().contains("Finance")).count()));
        lblAdmins  .setText(String.valueOf(masterList.stream().filter(e -> !e.getDepartment().contains("IT")&&!e.getDepartment().contains("HR")&&!e.getDepartment().contains("Finance")).count()));
    }

    @FXML
    private void handleAddEmployee() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/fxml/AddEmployee.fxml"));
            Parent root = loader.load();
            AddEmployeeController ctrl = loader.getController();
            ctrl.setMasterList(masterList);
            ctrl.setOnSaveCallback(this::refreshStats);
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initStyle(StageStyle.UNDECORATED);
            dialog.setScene(new Scene(root));
            ctrl.setDialogStage(dialog);
            dialog.showAndWait();
        } catch (IOException ex) { showError("Cannot open Add Employee dialog:\n" + ex.getMessage()); }
    }

    private void openEditDialog(Employee emp) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/fxml/EditEmployee.fxml"));
            Parent root = loader.load();
            EditEmployeeController ctrl = loader.getController();
            ctrl.setEmployee(emp);
            ctrl.setOnSaveCallback(() -> { tableEmployees.refresh(); refreshStats(); });
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initStyle(StageStyle.UNDECORATED);
            dialog.setScene(new Scene(root));
            ctrl.setDialogStage(dialog);
            dialog.showAndWait();
        } catch (IOException ex) { showError("Cannot open Edit Employee dialog:\n" + ex.getMessage()); }
    }

    private void handleDelete(Employee emp) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Employee"); confirm.setHeaderText("Delete " + emp.getName() + "?");
        confirm.setContentText("This action cannot be undone.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean deleted = employeeDAO.deleteEmployee(emp.getEmpId());
            if (deleted) { masterList.remove(emp); refreshStats(); }
            else showError("Failed to delete employee.");
        }
    }

    @FXML private void handleNavDashboard() { goTo("/main/resources/fxml/AdminDashboard.fxml",   "Dashboard");          }
    @FXML private void handleNavPassSlip()  { goTo("/main/resources/fxml/PassSlipIssuance.fxml", "Pass Slip Issuance"); }
    @FXML private void handleNavVisitor()   { goTo("/main/resources/fxml/Visitor.fxml",          "Visitor Module");     }
    @FXML private void handleNavReports()   { goTo("/main/resources/fxml/Reports.fxml",          "Reports");            }
    @FXML private void handleNavUserMgmt()  { /* already here */ }

    @FXML private void handleLogout() {
        Optional<ButtonType> res = new Alert(Alert.AlertType.CONFIRMATION,"Are you sure you want to logout?",ButtonType.OK,ButtonType.CANCEL).showAndWait();
        if (res.isPresent()&&res.get()==ButtonType.OK) goTo("/main/resources/fxml/Login.fxml","Login");
    }

    private void goTo(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) tableEmployees.getScene().getWindow();
            double w=stage.getWidth(), h=stage.getHeight();
            stage.setTitle(title); stage.setScene(new Scene(root)); stage.setWidth(w); stage.setHeight(h);
        } catch (IOException e) { showError("Screen not available:\n" + e.getMessage()); }
    }

    private void showError(String msg) { Alert a=new Alert(Alert.AlertType.ERROR);a.setTitle("Error");a.setHeaderText(null);a.setContentText(msg);a.showAndWait(); }
}
