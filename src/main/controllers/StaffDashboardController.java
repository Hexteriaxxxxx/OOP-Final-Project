package main.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.stage.Stage;
import dao.ActivityLogDAO;
import dao.PassSlipDAO;
import models.ActivityLog;
import models.PassSlip;
import models.User;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class StaffDashboardController implements Initializable {

    @FXML private Label lblWelcome;
    @FXML private Button btnNotification;
    @FXML private Button btnDashboard;
    @FXML private Button btnPassSlip;
    @FXML private Button btnVisitor;
    @FXML private Button btnReports;
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblPending;
    @FXML private Label lblApproved;
    @FXML private Label lblRejected;
    @FXML private Label lblActive;
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbFilter;
    @FXML private TableView<PassSlip> tblPassSlips;
    @FXML private TableColumn<PassSlip, String> colRequestId;
    @FXML private TableColumn<PassSlip, String> colName;
    @FXML private TableColumn<PassSlip, String> colDepartment;
    @FXML private TableColumn<PassSlip, String> colPurpose;
    @FXML private TableColumn<PassSlip, String> colTimeOut;
    @FXML private TableColumn<PassSlip, String> colTimeIn;
    @FXML private TableColumn<PassSlip, String> colStatus;
    @FXML private TableColumn<PassSlip, Void>   colActions;
    @FXML private VBox notifContainer;
    @FXML private VBox activityContainer;
    @FXML private Label lblTotalRequests;
    @FXML private Label lblApprovalRate;
    @FXML private Label lblActiveNow;

    private final PassSlipDAO    passSlipDAO    = new PassSlipDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();
    private ObservableList<PassSlip> masterList = FXCollections.observableArrayList();
    private User currentUser;
    private Timer autoRefreshTimer;

    public void setCurrentUser(User user) { currentUser = user; }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupFilterCombo();
        setupTableColumns();
        applyCurrentUser();
        loadDashboardStats();
        loadPassSlipRequests();
        loadNotifications();
        loadRecentActivity();
        startAutoRefresh();
    }

    private void setupFilterCombo() {
        cmbFilter.setItems(FXCollections.observableArrayList("All", "Pending", "Approved", "Rejected", "Active"));
        cmbFilter.setValue("All");
    }

    private void setupTableColumns() {
        colRequestId .setCellValueFactory(new PropertyValueFactory<>("slipId"));
        colName      .setCellValueFactory(new PropertyValueFactory<>("empName"));
        colDepartment.setCellValueFactory(new PropertyValueFactory<>("department"));
        colPurpose   .setCellValueFactory(new PropertyValueFactory<>("reason"));
        colTimeOut   .setCellValueFactory(new PropertyValueFactory<>("formattedTimeOut"));
        colTimeIn    .setCellValueFactory(new PropertyValueFactory<>("formattedTimeIn"));
        colStatus    .setCellValueFactory(new PropertyValueFactory<>("status"));
        setupStatusColumn();
        setupActionsColumn();
    }

    private void setupStatusColumn() {
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setText(null); setStyle(""); return; }
                setText(status);
                switch (status.toUpperCase()) {
                    case "PENDING":  setStyle("-fx-text-fill: #E67E00; -fx-font-weight: bold;"); break;
                    case "APPROVED": setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold;"); break;
                    case "REJECTED": setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;"); break;
                    default:         setStyle("-fx-text-fill: #333333;");
                }
            }
        });
    }

    private void setupActionsColumn() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("👁");
            {
                viewBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 16px;");
                viewBtn.setOnAction(e -> handleViewPassSlip(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : viewBtn);
            }
        });
    }

    private void applyCurrentUser() {
        if (currentUser != null) {
            lblUserName.setText(currentUser.getUsername());
            lblUserRole.setText(currentUser.getRole());
            lblWelcome.setText("Welcome back, " + currentUser.getUsername());
        }
    }

    public void loadDashboardStats() {
        try {
            List<PassSlip> all = passSlipDAO.getAllPassSlips();
            long pending  = all.stream().filter(s -> "PENDING" .equalsIgnoreCase(s.getStatus())).count();
            long approved = all.stream().filter(s -> "APPROVED".equalsIgnoreCase(s.getStatus())).count();
            long rejected = all.stream().filter(s -> "REJECTED".equalsIgnoreCase(s.getStatus())).count();
            int  active   = passSlipDAO.countActiveSlips();

            lblPending .setText(String.valueOf(pending));
            lblApproved.setText(String.valueOf(approved));
            lblRejected.setText(String.valueOf(rejected));
            lblActive  .setText(String.valueOf(active));

            List<PassSlip> today = passSlipDAO.getTodayPassSlips();
            int totalToday = today.size();
            long approvedToday = today.stream().filter(s -> "APPROVED".equalsIgnoreCase(s.getStatus())).count();
            int rate = totalToday == 0 ? 0 : (int) ((approvedToday * 100.0) / totalToday);
            lblTotalRequests.setText(String.valueOf(totalToday));
            lblApprovalRate .setText(rate + "%");
            lblActiveNow    .setText(String.valueOf(active));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not load stats: " + e.getMessage());
        }
    }

    public void loadPassSlipRequests() {
        try {
            List<PassSlip> slips = passSlipDAO.getAllPassSlips();
            masterList = FXCollections.observableArrayList(slips);
            tblPassSlips.setItems(masterList);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not load pass slips: " + e.getMessage());
        }
    }

    private void loadNotifications() {
        notifContainer.getChildren().clear();
        try {
            List<PassSlip> pending = passSlipDAO.getAllPassSlips().stream()
                    .filter(s -> "PENDING".equalsIgnoreCase(s.getStatus())).toList();
            if (!pending.isEmpty())
                addNotifItem(notifContainer, pending.size() + " pending approval" + (pending.size() > 1 ? "s" : ""), "#FFF3E0", "#E67E00");
            int active = passSlipDAO.countActiveSlips();
            if (active > 0)
                addNotifItem(notifContainer, active + " employee" + (active > 1 ? "s" : "") + " currently out", "#FFF8E1", "#F39C12");
            if (notifContainer.getChildren().isEmpty())
                notifContainer.getChildren().add(styledLabel("No new notifications.", "#999"));
        } catch (Exception e) {
            notifContainer.getChildren().add(styledLabel("Could not load notifications.", "#E74C3C"));
        }
    }

    private void loadRecentActivity() {
        activityContainer.getChildren().clear();
        try {
            List<models.ActivityLog> logs = activityLogDAO.getRecentLogs(5);
            if (logs == null || logs.isEmpty()) {
                activityContainer.getChildren().add(styledLabel("No recent activity.", "#999"));
                return;
            }
            for (models.ActivityLog log : logs) {
                VBox item = new VBox(2);
                Label action    = new Label("• " + log.getAction());
                Label timestamp = new Label(log.getFormattedTimestamp());
                action.setStyle("-fx-font-size: 12px; -fx-text-fill: #333;");
                timestamp.setStyle("-fx-font-size: 10px; -fx-text-fill: #999;");
                item.getChildren().addAll(action, timestamp);
                activityContainer.getChildren().add(item);
            }
        } catch (Exception e) {
            activityContainer.getChildren().add(styledLabel("Could not load activity.", "#E74C3C"));
        }
    }

    private void addNotifItem(VBox container, String text, String bg, String border) {
        HBox box = new HBox();
        box.setPadding(new Insets(8, 10, 8, 10));
        box.setStyle("-fx-background-color: " + bg + "; -fx-border-color: " + border + "; -fx-border-width: 0 0 0 3; -fx-background-radius: 4; -fx-border-radius: 4;");
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #333;");
        lbl.setWrapText(true);
        box.getChildren().add(lbl);
        container.getChildren().add(box);
    }

    private Label styledLabel(String text, String color) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: " + color + ";");
        return lbl;
    }

    private void startAutoRefresh() {
        autoRefreshTimer = new Timer(true);
        autoRefreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() { Platform.runLater(() -> refreshDashboard()); }
        }, 30_000, 30_000);
    }

    public void refreshDashboard() {
        loadDashboardStats();
        loadPassSlipRequests();
        loadNotifications();
        loadRecentActivity();
    }

    @FXML public void handleSearch() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        String filter  = cmbFilter.getValue();
        tblPassSlips.setItems(masterList.filtered(slip -> {
            boolean matchKeyword = keyword.isEmpty()
                    || slip.getEmpName().toLowerCase().contains(keyword)
                    || String.valueOf(slip.getSlipId()).contains(keyword)
                    || slip.getDepartment().toLowerCase().contains(keyword);
            boolean matchFilter = "All".equals(filter) || slip.getStatus().equalsIgnoreCase(filter);
            return matchKeyword && matchFilter;
        }));
    }

    @FXML public void handleFilter() { handleSearch(); }

    @FXML public void handleCreatePassSlip() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PassSlipIssuance.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Create Pass Slip");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Cannot open Pass Slip Issuance screen:\n" + e.getMessage());
        }
    }

    @FXML public void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Logout");
        confirm.setHeaderText("Are you sure you want to logout?");
        confirm.setContentText("You will be returned to the login screen.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            stopAutoRefresh();
            navigateTo("/fxml/Login.fxml", "Login");
        }
    }

    @FXML public void handleDashboard()         { setActiveButton(btnDashboard); refreshDashboard(); }
    @FXML public void handlePassSlipIssuance()  { setActiveButton(btnPassSlip); navigateTo("/fxml/PassSlipIssuance.fxml", "Pass Slip Issuance"); }
    @FXML public void handleVisitorModule()     { setActiveButton(btnVisitor); showAlert(Alert.AlertType.INFORMATION, "Visitor Module", "Visitor Module is coming soon."); }
    @FXML public void handleReports()           { setActiveButton(btnReports); showAlert(Alert.AlertType.INFORMATION, "Reports", "Reports Module is coming soon."); }

    private void handleViewPassSlip(PassSlip slip) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Pass Slip Details");
        info.setHeaderText("Slip ID: PS-" + slip.getSlipId());
        info.setContentText(
                "Employee : " + slip.getEmpName()         + "\n" +
                        "Department: "+ slip.getDepartment()       + "\n" +
                        "Purpose   : "+ slip.getReason()           + "\n" +
                        "Time Out  : "+ slip.getFormattedTimeOut() + "\n" +
                        "Time In   : "+ slip.getFormattedTimeIn()  + "\n" +
                        "Status    : "+ slip.getStatus()           + "\n" +
                        "Issued By : "+ slip.getIssuedBy()
        );
        info.showAndWait();
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            stopAutoRefresh();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) tblPassSlips.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title + " – Pass Slip System");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Cannot load " + fxmlPath + ":\n" + e.getMessage());
        }
    }

    private void setActiveButton(Button active) {
        for (Button btn : new Button[]{btnDashboard, btnPassSlip, btnVisitor, btnReports}) {
            btn.getStyleClass().remove("nav-btn-active");
        }
        if (!active.getStyleClass().contains("nav-btn-active"))
            active.getStyleClass().add("nav-btn-active");
    }

    private void stopAutoRefresh() {
        if (autoRefreshTimer != null) { autoRefreshTimer.cancel(); autoRefreshTimer = null; }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}