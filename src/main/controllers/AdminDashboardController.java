package main.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import dao.ActivityLogDAO;
import dao.PassSlipDAO;

import models.ActivityLog;
import models.PassSlip;
import models.User;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    // ===== SIDEBAR =====
    @FXML
    private Label lblSidebarUser;

    @FXML
    private Label lblSidebarRole;

    // ===== TOP BAR =====
    @FXML
    private Label lblWelcome;

    // ===== STAT CARDS =====
    @FXML
    private Label lblPendingCount;

    @FXML
    private Label lblApprovedCount;

    @FXML
    private Label lblRejectedCount;

    @FXML
    private Label lblActiveCount;

    // ===== TABLE =====
    @FXML
    private TableView<PassSlip> tblPassSlips;

    @FXML
    private TableColumn<PassSlip, Integer> colRequestId;

    @FXML
    private TableColumn<PassSlip, String> colName;

    @FXML
    private TableColumn<PassSlip, String> colDepartment;

    @FXML
    private TableColumn<PassSlip, String> colPurpose;

    @FXML
    private TableColumn<PassSlip, String> colTimeOut;

    @FXML
    private TableColumn<PassSlip, String> colTimeIn;

    @FXML
    private TableColumn<PassSlip, String> colStatus;

    @FXML
    private TableColumn<PassSlip, Void> colActions;

    // ===== SEARCH & FILTER =====
    @FXML
    private TextField txtSearch;

    @FXML
    private ComboBox<String> cmbFilter;

    // ===== RIGHT PANEL =====
    @FXML
    private VBox vboxRecentActivity;

    // ===== SUMMARY =====
    @FXML
    private Label lblTotalRequests;

    @FXML
    private Label lblApprovalRate;

    @FXML
    private Label lblActiveNow;

    // ===== NAV BUTTONS =====
    @FXML
    private Button btnDashboard;

    @FXML
    private Button btnPassSlip;

    @FXML
    private Button btnVisitor;

    @FXML
    private Button btnReports;

    @FXML
    private Button btnUserMgmt;

    // ===== DATA =====
    private final PassSlipDAO passSlipDAO = new PassSlipDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();

    private final ObservableList<PassSlip> masterList =
            FXCollections.observableArrayList();

    private FilteredList<PassSlip> filteredList;

    private User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTableColumns();
        setupFilterComboBox();
        loadDashboardData();
        loadRecentActivity();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            lblSidebarUser.setText(user.getUsername());
            lblSidebarRole.setText(user.getRole());
            lblWelcome.setText("Welcome back, " + user.getUsername());
        }
    }

    private void setupTableColumns() {

        colRequestId.setCellValueFactory(new PropertyValueFactory<>("slipId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("empName"));
        colDepartment.setCellValueFactory(new PropertyValueFactory<>("department"));
        colPurpose.setCellValueFactory(new PropertyValueFactory<>("reason"));
        colTimeOut.setCellValueFactory(new PropertyValueFactory<>("formattedTimeOut"));
        colTimeIn.setCellValueFactory(new PropertyValueFactory<>("formattedTimeIn"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // ===== STATUS BADGE =====
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(status);
                badge.setPadding(new Insets(3, 10, 3, 10));
                badge.setStyle("-fx-background-radius: 12;-fx-font-size: 10px;-fx-font-weight: bold;");
                switch (status.toLowerCase()) {
                    case "pending":
                        badge.setStyle(badge.getStyle() + "-fx-background-color: #FFF3CD;-fx-text-fill: #856404;");
                        break;
                    case "approved":
                        badge.setStyle(badge.getStyle() + "-fx-background-color: #D4EDDA;-fx-text-fill: #155724;");
                        break;
                    case "rejected":
                        badge.setStyle(badge.getStyle() + "-fx-background-color: #F8D7DA;-fx-text-fill: #721c24;");
                        break;
                    case "returned":
                        badge.setStyle(badge.getStyle() + "-fx-background-color: #CCE5FF;-fx-text-fill: #004085;");
                        break;
                    default:
                        badge.setStyle(badge.getStyle() + "-fx-background-color: #E2E3E5;-fx-text-fill: #383d41;");
                }
                setGraphic(badge);
                setText(null);
            }
        });

        // ===== ACTION BUTTONS =====
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnView = new Button("👁");
            private final Button btnApprove = new Button("✓");
            private final Button btnReject = new Button("✗");
            private final HBox box = new HBox(4, btnView, btnApprove, btnReject);
            {
                btnView.setStyle("-fx-background-color: transparent;-fx-font-size: 13px;-fx-cursor: hand;");
                btnApprove.setStyle("-fx-background-color: transparent;-fx-text-fill: #28a745;-fx-font-size: 13px;-fx-font-weight: bold;-fx-cursor: hand;");
                btnReject.setStyle("-fx-background-color: transparent;-fx-text-fill: #dc3545;-fx-font-size: 13px;-fx-font-weight: bold;-fx-cursor: hand;");
                btnView.setOnAction(e -> handleViewPassSlip(getTableView().getItems().get(getIndex())));
                btnApprove.setOnAction(e -> handleApprovePassSlip(getTableView().getItems().get(getIndex())));
                btnReject.setOnAction(e -> handleRejectPassSlip(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                PassSlip ps = getTableView().getItems().get(getIndex());
                boolean isPending = "Pending".equalsIgnoreCase(ps.getStatus());
                btnApprove.setVisible(isPending);
                btnReject.setVisible(isPending);
                setGraphic(box);
            }
        });
    }

    private void setupFilterComboBox() {
        cmbFilter.setItems(FXCollections.observableArrayList("All", "Pending", "Approved", "Rejected", "Returned"));
        cmbFilter.setValue("All");
    }

    private void loadDashboardData() {
        try {
            List<PassSlip> all = passSlipDAO.getAllPassSlips();
            masterList.setAll(all);
            filteredList = new FilteredList<>(masterList, p -> true);
            tblPassSlips.setItems(filteredList);

            long pending = all.stream().filter(p -> "Pending".equalsIgnoreCase(p.getStatus())).count();
            long approved = all.stream().filter(p -> "Approved".equalsIgnoreCase(p.getStatus())).count();
            long rejected = all.stream().filter(p -> "Rejected".equalsIgnoreCase(p.getStatus())).count();

            lblPendingCount.setText(String.valueOf(pending));
            lblApprovedCount.setText(String.valueOf(approved));
            lblRejectedCount.setText(String.valueOf(rejected));
            lblActiveCount.setText(String.valueOf(approved));

            List<PassSlip> today = passSlipDAO.getTodayPassSlips();
            lblTotalRequests.setText(String.valueOf(today.size()));

            long todayApproved = today.stream()
                    .filter(p -> "Approved".equalsIgnoreCase(p.getStatus()) || "Returned".equalsIgnoreCase(p.getStatus()))
                    .count();

            int approvalRate = today.isEmpty() ? 0 : (int) ((todayApproved * 100) / today.size());
            lblApprovalRate.setText(approvalRate + "%");
            lblActiveNow.setText(String.valueOf(approved));

        } catch (Exception e) {
            System.out.println("Dashboard Error: " + e.getMessage());
        }
    }

    private void loadRecentActivity() {
        try {
            vboxRecentActivity.getChildren().clear();
            List<ActivityLog> logs = activityLogDAO.getRecentLogs(10);
            if (logs.isEmpty()) {
                Label empty = new Label("No recent activity.");
                empty.setStyle("-fx-font-size: 11px;-fx-text-fill: #999;");
                vboxRecentActivity.getChildren().add(empty);
                return;
            }
            for (ActivityLog log : logs) {
                HBox row = new HBox(8);
                row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                Circle dot = new Circle(4, Color.web("#8B0000"));
                VBox textBox = new VBox(1);
                Label lblAction = new Label(log.getAction());
                lblAction.setStyle("-fx-font-size: 11px;-fx-text-fill: #333;");
                Label lblTime = new Label(log.getFormattedTimestamp());
                lblTime.setStyle("-fx-font-size: 10px;-fx-text-fill: #999;");
                textBox.getChildren().addAll(lblAction, lblTime);
                row.getChildren().addAll(dot, textBox);
                vboxRecentActivity.getChildren().add(row);
            }
        } catch (Exception e) {
            System.out.println("Activity Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() { applyFilter(); }

    @FXML
    private void handleFilter() { applyFilter(); }

    private void applyFilter() {
        if (filteredList == null) return;
        String keyword = txtSearch.getText().toLowerCase().trim();
        String statusFilter = cmbFilter.getValue();
        filteredList.setPredicate(ps -> {
            boolean matchesSearch = keyword.isEmpty()
                    || String.valueOf(ps.getSlipId()).contains(keyword)
                    || (ps.getEmpName() != null && ps.getEmpName().toLowerCase().contains(keyword))
                    || (ps.getDepartment() != null && ps.getDepartment().toLowerCase().contains(keyword))
                    || (ps.getReason() != null && ps.getReason().toLowerCase().contains(keyword));
            boolean matchesStatus = "All".equals(statusFilter)
                    || (ps.getStatus() != null && ps.getStatus().equalsIgnoreCase(statusFilter));
            return matchesSearch && matchesStatus;
        });
    }

    @FXML
    private void handleCreatePassSlip() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/main/resources/fxml/PassSlipIssuance.fxml") // ✅ FIXED
            );
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Issue Pass Slip");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadDashboardData();
        } catch (IOException e) {
            System.out.println("Open Form Error: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open Pass Slip form.");
        }
    }

    private void handleViewPassSlip(PassSlip ps) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Pass Slip Details");
        alert.setHeaderText("Request ID: " + ps.getSlipId());
        alert.setContentText(
                "Employee: " + ps.getEmpName() + "\n" +
                        "Department: " + ps.getDepartment() + "\n" +
                        "Purpose: " + ps.getReason() + "\n" +
                        "Time Out: " + ps.getFormattedTimeOut() + "\n" +
                        "Time In: " + ps.getFormattedTimeIn() + "\n" +
                        "Status: " + ps.getStatus()
        );
        alert.showAndWait();
    }

    private void handleApprovePassSlip(PassSlip ps) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Approve Pass Slip");
        confirm.setHeaderText("Approve request #" + ps.getSlipId() + "?");
        confirm.setContentText("Employee: " + ps.getEmpName());
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = passSlipDAO.updatePassSlipStatus(ps.getSlipId(), "Approved");
                if (success) {
                    activityLogDAO.logActivity(ps.getEmpId(),
                            "Pass slip #" + ps.getSlipId() + " approved",
                            currentUser != null ? currentUser.getUsername() : "Admin");
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Pass slip approved.");
                    loadDashboardData();
                    loadRecentActivity();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to approve pass slip.");
                }
            }
        });
    }

    private void handleRejectPassSlip(PassSlip ps) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Reject Pass Slip");
        confirm.setHeaderText("Reject request #" + ps.getSlipId() + "?");
        confirm.setContentText("Employee: " + ps.getEmpName());
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = passSlipDAO.updatePassSlipStatus(ps.getSlipId(), "Rejected");
                if (success) {
                    activityLogDAO.logActivity(ps.getEmpId(),
                            "Pass slip #" + ps.getSlipId() + " rejected",
                            currentUser != null ? currentUser.getUsername() : "Admin");
                    showAlert(Alert.AlertType.INFORMATION, "Done", "Pass slip rejected.");
                    loadDashboardData();
                    loadRecentActivity();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to reject pass slip.");
                }
            }
        });
    }

    @FXML
    private void handleNavDashboard() {
        setActiveNav(btnDashboard);
    }

    @FXML
    private void handleNavPassSlip() {
        setActiveNav(btnPassSlip);
        navigateTo("/main/resources/fxml/PassSlipIssuance.fxml", "Pass Slip Issuance"); // ✅ FIXED
    }

    @FXML
    private void handleNavVisitor() {
        setActiveNav(btnVisitor);
        navigateTo("/main/resources/fxml/Visitor.fxml", "Visitor Module"); // ✅ FIXED
    }

    @FXML
    private void handleNavReports() {
        setActiveNav(btnReports);
        navigateTo("/main/resources/fxml/Reports.fxml", "Reports"); // ✅ FIXED
    }

    @FXML
    private void handleNavUserManagement() {
        setActiveNav(btnUserMgmt);
        navigateTo("/main/resources/fxml/UserManagement.fxml", "User Management"); // ✅ FIXED
    }

    private void setActiveNav(Button active) {
        String inactive = "-fx-background-color: transparent;" +
                "-fx-text-fill: rgba(255,255,255,0.80);" +
                "-fx-font-size: 13px;" +
                "-fx-alignment: CENTER_LEFT;" +
                "-fx-padding: 10 14 10 14;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;" +
                "-fx-border-color: transparent;";
        String activeStyle = "-fx-background-color: rgba(255,255,255,0.22);" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-alignment: CENTER_LEFT;" +
                "-fx-padding: 10 14 10 14;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;" +
                "-fx-border-color: transparent;";
        for (Button btn : new Button[]{btnDashboard, btnPassSlip, btnVisitor, btnReports, btnUserMgmt}) {
            if (btn != null) btn.setStyle(inactive);
        }
        if (active != null) active.setStyle(activeStyle);
    }

    @FXML
    private void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Logout");
        confirm.setHeaderText("Are you sure you want to logout?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource("/main/resources/fxml/Login.fxml") // ✅ FIXED
                    );
                    Parent root = loader.load();
                    Stage stage = (Stage) lblSidebarUser.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Employee Pass Slip System - Login");
                    stage.show();
                } catch (IOException e) {
                    System.out.println("Logout Error: " + e.getMessage());
                }
            }
        });
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) lblSidebarUser.getScene().getWindow();
            double w = stage.getWidth();
            double h = stage.getHeight();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.setWidth(w);
            stage.setHeight(h);
        } catch (IOException e) {
            System.out.println("Navigation Error: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not open " + title + ".");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}