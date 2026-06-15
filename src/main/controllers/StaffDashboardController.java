package main.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import dao.ActivityLogDAO;
import dao.PassSlipDAO;
import main.utils.SkeletonLoader;
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
    @FXML private Button btnDashboard, btnPassSlip, btnReports;
    @FXML private Button btnNotification;
    @FXML private StackPane           skeletonContainer;
    @FXML private TextField           txtSearch;
    @FXML private ComboBox<String>    cmbFilter;
    @FXML private TableView<PassSlip> tblPassSlips;
    @FXML private TableColumn<PassSlip, String> colRequestId, colName, colDepartment;
    @FXML private TableColumn<PassSlip, String> colPurpose, colDate, colTimeOut, colTimeIn, colStatus;
    @FXML private TableColumn<PassSlip, Void>   colActions;
    @FXML private VBox notifContainer, activityContainer;

    private final PassSlipDAO    passSlipDAO    = new PassSlipDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();
    private final ObservableList<PassSlip> masterList = FXCollections.observableArrayList();
    private User currentUser;
    private Timer autoRefreshTimer;
    private NotificationHelper notifHelper;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        main.utils.SessionManager.setCurrentUser(user);
        applySession();
    }

    public void initSession(String username, String role) {
        String name = (username != null && !username.isBlank()) ? username : (currentUser != null ? currentUser.getUsername() : "Staff");
        String r    = (role     != null && !role.isBlank())     ? role     : (currentUser != null ? currentUser.getRole()     : "Staff");
        if (lblUserName != null) lblUserName.setText(name);
        if (lblUserRole != null) lblUserRole.setText(capitalize(r));
        if (lblWelcome  != null) lblWelcome .setText("Welcome back, " + name);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupFilterCombo();
        setupTableColumns();
        tblPassSlips.setItems(masterList);
        applySession();
        SkeletonLoader.show(skeletonContainer);
        loadDataAsync();
        loadNotifications();
        loadRecentActivity();
        startAutoRefresh();
    }

    private void applySession() {
        String username = main.utils.SessionManager.getUsername();
        String role     = main.utils.SessionManager.getRole();
        if (lblUserName != null) lblUserName.setText(username);
        if (lblUserRole != null) lblUserRole.setText(role);
        if (lblWelcome  != null) lblWelcome.setText("Welcome back, " + username);
    }

    private void loadDataAsync() {
        Task<List<PassSlip>> task = new Task<>() {
            @Override protected List<PassSlip> call() { return passSlipDAO.getAllPassSlips(); }
        };
        task.setOnSucceeded(e -> {
            SkeletonLoader.hide(skeletonContainer);
            List<PassSlip> all = task.getValue();
            masterList.setAll(all != null ? all : List.of());
            updateStatCards(all != null ? all : List.of());
        });
        task.setOnFailed(e -> SkeletonLoader.hide(skeletonContainer));
        new Thread(task, "StaffDashLoader").start();
    }

    private void updateStatCards(List<PassSlip> all) {
        java.time.LocalDate today = java.time.LocalDate.now();

        long pending  = all.stream().filter(s -> "Pending" .equalsIgnoreCase(s.getStatus())).count();
        long approved = all.stream().filter(s -> "Approved".equalsIgnoreCase(s.getStatus())).count();
        long rejected = all.stream().filter(s -> "Rejected".equalsIgnoreCase(s.getStatus())).count();

        long totalToday    = all.stream()
                .filter(s -> s.getTimeOut() != null && s.getTimeOut().toLocalDate().equals(today))
                .count();
        long approvedToday = all.stream()
                .filter(s -> s.getTimeOut() != null && s.getTimeOut().toLocalDate().equals(today)
                        && "Approved".equalsIgnoreCase(s.getStatus()))
                .count();
        int rate = totalToday == 0 ? 0 : (int)((approvedToday * 100.0) / totalToday);

        long active = 0;
        try { active = passSlipDAO.countActiveSlips(); } catch (Exception ignored) {}

        lblPending .setText(String.valueOf(pending));
        lblApproved.setText(String.valueOf(approved));
        lblRejected.setText(String.valueOf(rejected));
        lblActive  .setText(String.valueOf(active));

        lblTotalRequests.setText(String.valueOf(totalToday));
        lblApprovalRate .setText(rate + "%");
        if (lblActiveNow != null) lblActiveNow.setText(String.valueOf(active));
    }

    @FXML private void handleNotification() {
        if (notifHelper == null) notifHelper = new NotificationHelper(btnNotification, NotificationHelper.Role.STAFF);
        notifHelper.toggle();
    }

    private void setupFilterCombo() {
        cmbFilter.setItems(FXCollections.observableArrayList("All","Pending","Approved","Rejected"));
        cmbFilter.setValue("All");
    }

    private void setupTableColumns() {
        colRequestId .setCellValueFactory(new PropertyValueFactory<>("slipId"));
        colName      .setCellValueFactory(new PropertyValueFactory<>("empName"));
        colDepartment.setCellValueFactory(new PropertyValueFactory<>("department"));
        colPurpose   .setCellValueFactory(new PropertyValueFactory<>("reason"));
        colDate      .setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getTimeOut() != null ? d.getValue().getTimeOut().toLocalDate().format(java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy")) : "-"));
        colTimeOut   .setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getTimeOut() != null ? d.getValue().getTimeOut().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a")) : ""));
        colTimeIn    .setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getTimeIn() != null ? d.getValue().getTimeIn().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a")) : "Not yet returned"));
        colStatus    .setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setText(null); setGraphic(null); return; }
                Label badge = new Label(status); badge.setPadding(new Insets(3, 10, 3, 10));
                badge.setStyle("-fx-background-radius: 12; -fx-font-size: 10px; -fx-font-weight: bold;");
                switch (status.toLowerCase()) {
                    case "pending"  -> badge.setStyle(badge.getStyle() + "-fx-background-color: #FFF3CD; -fx-text-fill: #856404;");
                    case "approved" -> badge.setStyle(badge.getStyle() + "-fx-background-color: #D4EDDA; -fx-text-fill: #155724;");
                    case "rejected" -> badge.setStyle(badge.getStyle() + "-fx-background-color: #F8D7DA; -fx-text-fill: #721c24;");
                    case "overdue"  -> badge.setStyle(badge.getStyle() + "-fx-background-color: #FF6B35; -fx-text-fill: white;");
                    case "returned" -> badge.setStyle(badge.getStyle() + "-fx-background-color: #E2E3E5; -fx-text-fill: #383d41;");
                    default         -> badge.setStyle(badge.getStyle() + "-fx-background-color: #E2E3E5; -fx-text-fill: #383d41;");
                }
                setGraphic(badge); setText(null);
            }
        });
        colActions.setCellFactory(col -> new TableCell<>() {
            final Button viewBtn = new Button("👁");
            { viewBtn.setStyle("-fx-background-color:transparent;-fx-cursor:hand;-fx-font-size:16px;");
                viewBtn.setOnAction(e -> handleViewPassSlip(getTableView().getItems().get(getIndex()))); }
            @Override protected void updateItem(Void item, boolean empty) { super.updateItem(item,empty); setGraphic(empty?null:viewBtn); }
        });
    }

    private void loadNotifications() {
        if (notifContainer == null) return;
        notifContainer.getChildren().clear();
        try {
            List<PassSlip> all = passSlipDAO.getAllPassSlips();
            long pending = all.stream().filter(s -> "PENDING".equalsIgnoreCase(s.getStatus())).count();
            int  active  = passSlipDAO.countActiveSlips();
            if (pending > 0) addNotifItem(notifContainer, pending+" pending approval"+(pending>1?"s":""), "#FFF3E0", "#E67E00");
            if (active  > 0) addNotifItem(notifContainer, active+" employee"+(active>1?"s":"")+" currently out", "#FFF8E1", "#F39C12");
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
                VBox item=new VBox(2);
                Label action=new Label("• "+log.getAction()); action.setStyle("-fx-font-size:12px;-fx-text-fill:#333;");
                Label ts=new Label(log.getFormattedTimestamp()); ts.setStyle("-fx-font-size:10px;-fx-text-fill:#999;");
                item.getChildren().addAll(action,ts); activityContainer.getChildren().add(item);
            }
        } catch (Exception e) { activityContainer.getChildren().add(styledLabel("Could not load activity.", "#E74C3C")); }
    }

    private void addNotifItem(VBox container, String text, String bg, String border) {
        HBox box=new HBox(); box.setPadding(new Insets(8,10,8,10));
        box.setStyle("-fx-background-color:"+bg+";-fx-border-color:"+border+";-fx-border-width:0 0 0 3;-fx-background-radius:4;-fx-border-radius:4;");
        Label lbl=new Label(text); lbl.setStyle("-fx-font-size:12px;-fx-text-fill:#333;"); lbl.setWrapText(true);
        box.getChildren().add(lbl); container.getChildren().add(box);
    }

    private Label styledLabel(String text, String color) {
        Label lbl=new Label(text); lbl.setStyle("-fx-font-size:12px;-fx-text-fill:"+color+";"); return lbl;
    }

    private void startAutoRefresh() {
        autoRefreshTimer = new Timer(true);
        autoRefreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() { Platform.runLater(() -> refreshDashboard()); }
        }, 30_000, 30_000);
    }

    public void refreshDashboard() {
        SkeletonLoader.show(skeletonContainer);
        loadDataAsync();
        loadNotifications();
        loadRecentActivity();
    }

    @FXML public void handleSearch() {
        String kw = txtSearch.getText().trim().toLowerCase();
        String filter = cmbFilter.getValue();
        tblPassSlips.setItems(masterList.filtered(slip -> {
            boolean matchKw = kw.isEmpty() ||
                    (slip.getEmpName() != null && slip.getEmpName().toLowerCase().contains(kw)) ||
                    String.valueOf(slip.getSlipId()).contains(kw) ||
                    (slip.getDepartment() != null && slip.getDepartment().toLowerCase().contains(kw));
            boolean matchF = "All".equals(filter) || slip.getStatus().equalsIgnoreCase(filter);
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

            Rectangle2D screen = Screen.getPrimary().getVisualBounds();
            double winW = 480;
            double winH = Math.min(screen.getHeight() * 0.88, 620);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(tblPassSlips.getScene().getWindow());
            stage.setTitle("Create Pass Slip");
            stage.setScene(new Scene(root, winW, winH));
            stage.setResizable(false);
            stage.setWidth(winW);
            stage.setHeight(winH);
            stage.setX(screen.getMinX() + (screen.getWidth()  - winW) / 2);
            stage.setY(screen.getMinY() + (screen.getHeight() - winH) / 2);
            stage.showAndWait();
            refreshDashboard();
        } catch (IOException e) { System.out.println("Open form error: " + e.getMessage()); }
    }

    @FXML public void handleLogout() {
        Alert c=new Alert(Alert.AlertType.CONFIRMATION,"Are you sure you want to logout?",ButtonType.OK,ButtonType.CANCEL);
        Optional<ButtonType> res=c.showAndWait();
        if (res.isPresent()&&res.get()==ButtonType.OK) { stopAutoRefresh(); navigateTo("/main/resources/fxml/Login.fxml","Login"); }
    }

    @FXML public void handleDashboard()        { setActiveButton(btnDashboard); refreshDashboard(); }
    @FXML public void handlePassSlipIssuance() { setActiveButton(btnPassSlip);  navigateToStaff("/main/resources/fxml/StaffPassSlipIssuance.fxml","Pass Slip Issuance"); }
    @FXML public void handleReports()          { setActiveButton(btnReports);   navigateToStaff("/main/resources/fxml/StaffReports.fxml","Reports"); }

    private void handleViewPassSlip(PassSlip slip) {
        Alert a=new Alert(Alert.AlertType.INFORMATION); a.setTitle("Pass Slip Details"); a.setHeaderText("Slip ID: PS-"+slip.getSlipId());
        a.setContentText("Employee  : "+slip.getEmpName()+"\nDepartment: "+slip.getDepartment()+"\nCategory  : "+slip.getCategory()+"\nPurpose   : "+slip.getReason()+"\nTime Out  : "+slip.getFormattedTimeOut()+"\nTime In   : "+slip.getFormattedTimeIn()+"\nStatus    : "+slip.getStatus());
        a.showAndWait();
    }

    private void navigateToStaff(String fxmlPath, String title) {
        stopAutoRefresh();
        Stage stage = (Stage) tblPassSlips.getScene().getWindow();
        double w = stage.getWidth(), h = stage.getHeight();
        boolean wasFullscreen = stage.isFullScreen();
        boolean wasMaximized  = stage.isMaximized();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object ctrl = loader.getController();
            String username = main.utils.SessionManager.getUsername();
            String role     = main.utils.SessionManager.getRole();
            if (ctrl instanceof StaffPassSlipController)
                ((StaffPassSlipController) ctrl).initSession(username, role);
            else if (ctrl instanceof StaffReportsController)
                ((StaffReportsController) ctrl).initSession(username, role);

            Scene navScene = new Scene(root);
            navScene.setFill(Color.WHITE);
            stage.setScene(navScene);
            stage.setTitle(title + " – Pass Slip System");
            stage.setWidth(w);
            stage.setHeight(h);
            if (wasFullscreen) Platform.runLater(() -> stage.setFullScreen(true));
            else if (wasMaximized) Platform.runLater(() -> stage.setMaximized(true));
            stage.show();
        } catch (IOException e) { System.out.println("Nav error: " + e.getMessage()); }
    }

    private void navigateTo(String fxmlPath, String title) {
        stopAutoRefresh();
        Stage stage = (Stage) tblPassSlips.getScene().getWindow();
        double w = stage.getWidth(), h = stage.getHeight();
        boolean wasFullscreen = stage.isFullScreen();
        boolean wasMaximized  = stage.isMaximized();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Scene navScene = new Scene(root);
            navScene.setFill(Color.WHITE);
            stage.setScene(navScene);
            stage.setTitle(title + " – Pass Slip System");
            stage.setWidth(w);
            stage.setHeight(h);
            if (wasFullscreen) Platform.runLater(() -> stage.setFullScreen(true));
            else if (wasMaximized) Platform.runLater(() -> stage.setMaximized(true));
            stage.show();
        } catch (IOException e) { System.out.println("Nav error: " + e.getMessage()); }
    }

    private void setActiveButton(Button active) {
        for (Button btn : new Button[]{btnDashboard,btnPassSlip,btnReports})
            if (btn!=null) btn.getStyleClass().remove("nav-btn-active");
        if (active!=null&&!active.getStyleClass().contains("nav-btn-active")) active.getStyleClass().add("nav-btn-active");
    }

    private void stopAutoRefresh() { if(autoRefreshTimer!=null){autoRefreshTimer.cancel();autoRefreshTimer=null;} }
}