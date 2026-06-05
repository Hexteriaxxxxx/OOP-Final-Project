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

    @FXML private Label  lblWelcome, lblUserName, lblUserRole;
    @FXML private Label  lblPending, lblApproved, lblRejected, lblActive;
    @FXML private Label  lblTotalRequests, lblApprovalRate, lblActiveNow;
    @FXML private Button btnDashboard, btnPassSlip, btnVisitor, btnReports;
    @FXML private Button btnNotification;
    @FXML private TextField        txtSearch;
    @FXML private ComboBox<String> cmbFilter;
    @FXML private TableView<PassSlip>           tblPassSlips;
    @FXML private TableColumn<PassSlip, String> colRequestId, colName, colDepartment;
    @FXML private TableColumn<PassSlip, String> colPurpose, colTimeOut, colTimeIn, colStatus;
    @FXML private TableColumn<PassSlip, Void>   colActions;
    @FXML private VBox notifContainer, activityContainer;

    private final PassSlipDAO    passSlipDAO    = new PassSlipDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();
    private ObservableList<PassSlip> masterList = FXCollections.observableArrayList();
    private User currentUser;
    private Timer autoRefreshTimer;
    private NotificationHelper notifHelper;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            if (lblUserName != null) lblUserName.setText(user.getUsername());
            if (lblUserRole != null) lblUserRole.setText(user.getRole());
            if (lblWelcome  != null) lblWelcome .setText("Welcome back, " + user.getUsername());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupFilterCombo(); setupTableColumns(); loadDashboardStats(); loadPassSlipRequests(); loadNotifications(); loadRecentActivity();
        startAutoRefresh();
    }

    @FXML
    private void handleNotification() {
        if (notifHelper == null)
            notifHelper = new NotificationHelper(btnNotification, NotificationHelper.Role.STAFF);
        notifHelper.toggle();
    }

    private void setupFilterCombo() {
        cmbFilter.setItems(FXCollections.observableArrayList("All","Pending","Approved","Rejected","Active"));
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

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty||status==null){setText(null);setStyle("");return;}
                setText(status);
                switch(status.toUpperCase()){
                    case "PENDING"  -> setStyle("-fx-text-fill:#E67E00;-fx-font-weight:bold;");
                    case "APPROVED" -> setStyle("-fx-text-fill:#27AE60;-fx-font-weight:bold;");
                    case "REJECTED" -> setStyle("-fx-text-fill:#E74C3C;-fx-font-weight:bold;");
                    default         -> setStyle("-fx-text-fill:#333;");
                }
            }
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            final Button viewBtn = new Button("👁");
            { viewBtn.setStyle("-fx-background-color:transparent;-fx-cursor:hand;-fx-font-size:16px;"); viewBtn.setOnAction(e -> handleViewPassSlip(getTableView().getItems().get(getIndex()))); }
            @Override protected void updateItem(Void item, boolean empty) { super.updateItem(item,empty); setGraphic(empty?null:viewBtn); }
        });
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
            long approvedToday = today.stream().filter(s -> "APPROVED".equalsIgnoreCase(s.getStatus())).count();
            int  rate = today.isEmpty() ? 0 : (int)((approvedToday*100.0)/today.size());
            lblTotalRequests.setText(String.valueOf(today.size()));
            lblApprovalRate .setText(rate + "%");
            lblActiveNow    .setText(String.valueOf(active));
        } catch (Exception e) { System.out.println("Stats error: " + e.getMessage()); }
    }

    public void loadPassSlipRequests() {
        try {
            List<PassSlip> slips = passSlipDAO.getAllPassSlips();
            masterList = FXCollections.observableArrayList(slips);
            tblPassSlips.setItems(masterList);
        } catch (Exception e) { System.out.println("Pass slip error: " + e.getMessage()); }
    }

    private void loadNotifications() {
        if (notifContainer == null) return;
        notifContainer.getChildren().clear();
        try {
            List<PassSlip> all = passSlipDAO.getAllPassSlips();
            long pending = all.stream().filter(s -> "PENDING".equalsIgnoreCase(s.getStatus())).count();
            int  active  = passSlipDAO.countActiveSlips();
            if (pending > 0) addNotifItem(notifContainer, pending + " pending approval" + (pending>1?"s":""), "#FFF3E0", "#E67E00");
            if (active  > 0) addNotifItem(notifContainer, active  + " employee" + (active>1?"s":"") + " currently out", "#FFF8E1", "#F39C12");
            if (notifContainer.getChildren().isEmpty()) notifContainer.getChildren().add(styledLabel("No new notifications.", "#999"));
        } catch (Exception e) { notifContainer.getChildren().add(styledLabel("Could not load notifications.", "#E74C3C")); }
    }

    private void loadRecentActivity() {
        if (activityContainer == null) return;
        activityContainer.getChildren().clear();
        try {
            List<ActivityLog> logs = activityLogDAO.getRecentLogs(5);
            if (logs==null||logs.isEmpty()) { activityContainer.getChildren().add(styledLabel("No recent activity.", "#999")); return; }
            for (ActivityLog log : logs) {
                VBox item = new VBox(2);
                Label action    = new Label("• " + log.getAction());
                Label timestamp = new Label(log.getFormattedTimestamp());
                action   .setStyle("-fx-font-size:12px;-fx-text-fill:#333;");
                timestamp.setStyle("-fx-font-size:10px;-fx-text-fill:#999;");
                item.getChildren().addAll(action, timestamp);
                activityContainer.getChildren().add(item);
            }
        } catch (Exception e) { activityContainer.getChildren().add(styledLabel("Could not load activity.", "#E74C3C")); }
    }

    private void addNotifItem(VBox container, String text, String bg, String border) {
        HBox box = new HBox(); box.setPadding(new Insets(8,10,8,10));
        box.setStyle("-fx-background-color:"+bg+";-fx-border-color:"+border+";-fx-border-width:0 0 0 3;-fx-background-radius:4;-fx-border-radius:4;");
        Label lbl = new Label(text); lbl.setStyle("-fx-font-size:12px;-fx-text-fill:#333;"); lbl.setWrapText(true);
        box.getChildren().add(lbl); container.getChildren().add(box);
    }

    private Label styledLabel(String text, String color) {
        Label lbl = new Label(text); lbl.setStyle("-fx-font-size:12px;-fx-text-fill:"+color+";"); return lbl;
    }

    private void startAutoRefresh() {
        autoRefreshTimer = new Timer(true);
        autoRefreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() { Platform.runLater(() -> refreshDashboard()); }
        }, 30_000, 30_000);
    }

    public void refreshDashboard() { loadDashboardStats(); loadPassSlipRequests(); loadNotifications(); loadRecentActivity(); }

    @FXML public void handleSearch() {
        String kw = txtSearch.getText().trim().toLowerCase();
        String filter = cmbFilter.getValue();
        tblPassSlips.setItems(masterList.filtered(slip -> {
            boolean matchKw  = kw.isEmpty()||slip.getEmpName().toLowerCase().contains(kw)||String.valueOf(slip.getSlipId()).contains(kw)||slip.getDepartment().toLowerCase().contains(kw);
            boolean matchF   = "All".equals(filter)||slip.getStatus().equalsIgnoreCase(filter);
            return matchKw && matchF;
        }));
    }

    @FXML public void handleFilter() { handleSearch(); }

    @FXML public void handleCreatePassSlip() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/fxml/CreatePassSlip.fxml"));
            Parent root = loader.load();
            CreatePassSlipController ctrl = loader.getController();
            if (currentUser != null) ctrl.setCurrentUserId(currentUser.getUserId());
            Stage stage = new Stage();
            stage.setTitle("Create Pass Slip"); stage.setScene(new Scene(root)); stage.showAndWait();
            refreshDashboard();
        } catch (IOException e) { System.out.println("Open form error: " + e.getMessage()); }
    }

    @FXML public void handleLogout() {
        Alert c = new Alert(Alert.AlertType.CONFIRMATION,"Are you sure you want to logout?",ButtonType.OK,ButtonType.CANCEL);
        Optional<ButtonType> res = c.showAndWait();
        if (res.isPresent()&&res.get()==ButtonType.OK) { stopAutoRefresh(); navigateTo("/main/resources/fxml/Login.fxml","Login"); }
    }

    @FXML public void handleDashboard()        { setActiveButton(btnDashboard); refreshDashboard(); }
    @FXML public void handlePassSlipIssuance() { setActiveButton(btnPassSlip);  navigateToStaff("/main/resources/fxml/StaffPassSlipIssuance.fxml","Pass Slip Issuance"); }
    @FXML public void handleVisitorModule()    { setActiveButton(btnVisitor);   navigateToStaff("/main/resources/fxml/StaffVisitorModule.fxml","Visitor Module"); }
    @FXML public void handleReports()          { setActiveButton(btnReports);   navigateToStaff("/main/resources/fxml/StaffReports.fxml","Reports"); }

    private void handleViewPassSlip(PassSlip slip) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Pass Slip Details"); a.setHeaderText("Slip ID: PS-" + slip.getSlipId());
        a.setContentText("Employee : " + slip.getEmpName() + "\nDepartment: " + slip.getDepartment() + "\nPurpose   : " + slip.getReason() + "\nTime Out  : " + slip.getFormattedTimeOut() + "\nTime In   : " + slip.getFormattedTimeIn() + "\nStatus    : " + slip.getStatus());
        a.showAndWait();
    }

    // Navigate to staff screens AND pass session info
    private void navigateToStaff(String fxmlPath, String title) {
        try {
            stopAutoRefresh();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Object ctrl = loader.getController();
            String username = currentUser != null ? currentUser.getUsername() : "Staff";
            String role     = currentUser != null ? currentUser.getRole()     : "Staff";
            if (ctrl instanceof StaffPassSlipController) ((StaffPassSlipController)ctrl).initSession(username, role);
            else if (ctrl instanceof StaffVisitorController) ((StaffVisitorController)ctrl).initSession(username, role);
            else if (ctrl instanceof StaffReportsController) ((StaffReportsController)ctrl).initSession(username, role);
            Stage stage = (Stage) tblPassSlips.getScene().getWindow();
            stage.setScene(new Scene(root)); stage.setTitle(title + " – Pass Slip System"); stage.show();
        } catch (IOException e) { System.out.println("Nav error: " + e.getMessage()); }
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            stopAutoRefresh();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) tblPassSlips.getScene().getWindow();
            stage.setScene(new Scene(root)); stage.setTitle(title+" – Pass Slip System"); stage.show();
        } catch (IOException e) { System.out.println("Nav error: " + e.getMessage()); }
    }

    private void setActiveButton(Button active) {
        for (Button btn : new Button[]{btnDashboard,btnPassSlip,btnVisitor,btnReports})
            if (btn != null) btn.getStyleClass().remove("nav-btn-active");
        if (active != null && !active.getStyleClass().contains("nav-btn-active"))
            active.getStyleClass().add("nav-btn-active");
    }

    private void stopAutoRefresh() { if (autoRefreshTimer!=null){autoRefreshTimer.cancel();autoRefreshTimer=null;} }
}
