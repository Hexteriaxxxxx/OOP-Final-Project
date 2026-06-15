package main.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import dao.ActivityLogDAO;
import dao.PassSlipDAO;
import main.utils.OverdueCheckerService;
import main.utils.SkeletonLoader;
import models.ActivityLog;
import models.PassSlip;
import models.User;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML private Label  lblSidebarUser, lblSidebarRole, lblWelcome;
    @FXML private Label  lblPendingCount, lblApprovedCount, lblRejectedCount, lblActiveCount;
    @FXML private Label  lblTotalRequests, lblApprovalRate, lblActiveNow;
    @FXML private Button btnDashboard, btnPassSlip, btnReports, btnUserMgmt;
    @FXML private Button btnNotification;
    @FXML private StackPane skeletonContainer;
    @FXML private TableView<PassSlip>            tblPassSlips;
    @FXML private TableColumn<PassSlip, Integer> colRequestId;
    @FXML private TableColumn<PassSlip, String>  colName, colDepartment, colPurpose;
    @FXML private TableColumn<PassSlip, String>  colTimeOut, colTimeIn, colStatus;
    @FXML private TableColumn<PassSlip, Void>    colActions;
    @FXML private TextField        txtSearch;
    @FXML private ComboBox<String> cmbFilter;
    @FXML private VBox vboxRecentActivity;

    private final PassSlipDAO      passSlipDAO    = new PassSlipDAO();
    private final ActivityLogDAO   activityLogDAO = new ActivityLogDAO();
    private final ObservableList<PassSlip> masterList = FXCollections.observableArrayList();
    private FilteredList<PassSlip> filteredList;
    private User currentUser;
    private NotificationHelper notifHelper;
    private OverdueCheckerService overdueChecker;

    private static final DateTimeFormatter TIME_FMT     = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("hh:mm a, MMM dd yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        filteredList = new FilteredList<>(masterList, p -> true);
        tblPassSlips.setItems(filteredList);
        setupTableColumns(); setupFilterComboBox();
        applySession();
        SkeletonLoader.show(skeletonContainer);
        loadDataAsync();
        loadRecentActivity();
        startOverdueChecker();
    }

    private void applySession() {
        String username = main.utils.SessionManager.getUsername();
        String role     = main.utils.SessionManager.getRole();
        if (lblSidebarUser != null) lblSidebarUser.setText(username);
        if (lblSidebarRole != null) lblSidebarRole.setText(role);
        if (lblWelcome     != null) lblWelcome.setText("Welcome back, " + username);
    }

    private void loadDataAsync() {
        Task<List<PassSlip>> task = new Task<>() {
            @Override protected List<PassSlip> call() { return passSlipDAO.getAllPassSlips(); }
        };
        task.setOnSucceeded(e -> {
            SkeletonLoader.hide(skeletonContainer);
            List<PassSlip> all = task.getValue() != null ? task.getValue() : List.of();
            masterList.setAll(all); applyFilter(); updateStatCards(all);
        });
        task.setOnFailed(e -> SkeletonLoader.hide(skeletonContainer));
        new Thread(task, "DashboardLoader").start();
    }

    private void updateStatCards(List<PassSlip> all) {
        long pending  = all.stream().filter(p -> "Pending" .equalsIgnoreCase(p.getStatus())).count();
        long approved = all.stream().filter(p -> "Approved".equalsIgnoreCase(p.getStatus())).count();
        long overdue  = all.stream().filter(p -> "Overdue" .equalsIgnoreCase(p.getStatus())).count();
        lblPendingCount .setText(String.valueOf(pending));
        lblApprovedCount.setText(String.valueOf(approved));
        lblRejectedCount.setText(overdue > 0 ? "⚠ " + overdue : String.valueOf(all.stream().filter(p -> "Rejected".equalsIgnoreCase(p.getStatus())).count()));
        lblActiveCount  .setText(String.valueOf(approved + overdue));
        try {
            List<PassSlip> today = passSlipDAO.getTodayPassSlips();
            long todayApproved = today.stream().filter(p -> "Approved".equalsIgnoreCase(p.getStatus())).count();
            int  rate = today.isEmpty() ? 0 : (int)((todayApproved*100)/today.size());
            lblTotalRequests.setText(String.valueOf(today.size()));
            lblApprovalRate .setText(rate + "%");
            lblActiveNow    .setText(String.valueOf(approved + overdue));
        } catch (Exception ignored) {}
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        main.utils.SessionManager.setCurrentUser(user);
        applySession();
    }

    private void startOverdueChecker() {
        overdueChecker = new OverdueCheckerService(this::onOverdueDetected);
        overdueChecker.start();
    }

    // ── FIXED: Wrapped in Platform.runLater to avoid animation conflict ──
    private void onOverdueDetected(List<PassSlip> overdueSlips) {
        Platform.runLater(() -> {
            loadDashboardData(); loadRecentActivity();
            StringBuilder msg = new StringBuilder("The following employee(s) have NOT returned on time:\n\n");
            for (PassSlip slip : overdueSlips) {
                String expected = slip.getTimeIn() != null ? slip.getTimeIn().format(TIME_FMT) : "—";
                long minsLate = slip.getTimeIn() != null ? java.time.Duration.between(slip.getTimeIn(), LocalDateTime.now()).toMinutes() : 0;
                msg.append("• ").append(slip.getEmpName()).append("  (PS-").append(String.format("%04d", slip.getSlipId())).append(")")
                        .append("\n  Expected: ").append(expected).append("  |  ").append(minsLate).append(" min(s) late\n\n");
            }
            msg.append("Click 'View Overdue' to see them in the dashboard.");
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("⚠  Overdue Pass Slips");
            alert.setHeaderText("⚠  " + overdueSlips.size() + " employee(s) have not returned on time!");
            alert.setContentText(msg.toString());
            ButtonType btnViewOverdue = new ButtonType("View Overdue", ButtonBar.ButtonData.OK_DONE);
            ButtonType btnDismiss     = new ButtonType("Dismiss",      ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(btnViewOverdue, btnDismiss);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == btnViewOverdue) {
                loadDashboardData();
                if (cmbFilter != null) { cmbFilter.getSelectionModel().select("Overdue"); applyFilter(); }
            }
        });
    }

    @FXML private void handleNotification() {
        if (notifHelper == null) notifHelper = new NotificationHelper(btnNotification, NotificationHelper.Role.ADMIN);
        notifHelper.toggle();
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
            final Button btnView=new Button("👁"); final Button btnApprove=new Button("✓"); final Button btnReject=new Button("✗"); final Button btnReturn=new Button("↩");
            final HBox box=new HBox(6, btnView, btnApprove, btnReject, btnReturn);
            { box.setAlignment(Pos.CENTER_LEFT);
                btnView   .setStyle("-fx-background-color:transparent;-fx-font-size:14px;-fx-cursor:hand;-fx-padding:2 4;");
                btnApprove.setStyle("-fx-background-color:transparent;-fx-text-fill:#28a745;-fx-font-size:15px;-fx-font-weight:bold;-fx-cursor:hand;-fx-padding:2 4;");
                btnReject .setStyle("-fx-background-color:transparent;-fx-text-fill:#dc3545;-fx-font-size:15px;-fx-font-weight:bold;-fx-cursor:hand;-fx-padding:2 4;");
                btnReturn .setStyle("-fx-background-color:#FF6B35;-fx-text-fill:white;-fx-font-size:14px;-fx-padding:3 10;-fx-background-radius:12;-fx-cursor:hand;-fx-border-width:0;-fx-font-weight:bold;");
                btnView.setTooltip(new Tooltip("View")); btnApprove.setTooltip(new Tooltip("Approve")); btnReject.setTooltip(new Tooltip("Reject")); btnReturn.setTooltip(new Tooltip("Record return"));
                btnView   .setOnAction(e -> handleViewPassSlip  (getTableView().getItems().get(getIndex())));
                btnApprove.setOnAction(e -> handleApprovePassSlip(getTableView().getItems().get(getIndex())));
                btnReject .setOnAction(e -> handleRejectPassSlip (getTableView().getItems().get(getIndex())));
                btnReturn .setOnAction(e -> handleRecordReturn   (getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                PassSlip ps=getTableView().getItems().get(getIndex()); String status=ps.getStatus()!=null?ps.getStatus().toLowerCase():"";
                boolean isPending=status.equals("pending"); boolean canReturn=status.equals("approved")||status.equals("overdue");
                btnApprove.setVisible(isPending); btnApprove.setManaged(isPending);
                btnReject .setVisible(isPending); btnReject .setManaged(isPending);
                btnReturn .setVisible(canReturn);  btnReturn .setManaged(canReturn);
                setGraphic(box);
            }
        });
    }

    private void setupFilterComboBox() {
        cmbFilter.setItems(FXCollections.observableArrayList("All","Pending","Approved","Overdue","Rejected","Returned"));
        cmbFilter.setValue("All");
    }

    private void loadDashboardData() { SkeletonLoader.show(skeletonContainer); loadDataAsync(); }

    private void loadRecentActivity() {
        try {
            vboxRecentActivity.getChildren().clear();
            List<ActivityLog> logs = activityLogDAO.getRecentLogs(10);
            if (logs.isEmpty()) { Label empty=new Label("No recent activity."); empty.setStyle("-fx-font-size:11px;-fx-text-fill:#999;"); vboxRecentActivity.getChildren().add(empty); return; }
            for (ActivityLog log : logs) {
                HBox row=new HBox(8); row.setAlignment(Pos.CENTER_LEFT);
                Circle dot=new Circle(4,Color.web("#8B0000"));
                VBox textBox=new VBox(1);
                Label lA=new Label(log.getAction()); lA.setStyle("-fx-font-size:11px;-fx-text-fill:#333;");
                Label lT=new Label(log.getFormattedTimestamp()); lT.setStyle("-fx-font-size:10px;-fx-text-fill:#999;");
                textBox.getChildren().addAll(lA,lT); row.getChildren().addAll(dot,textBox); vboxRecentActivity.getChildren().add(row);
            }
        } catch (Exception e) { System.out.println("Activity Error: " + e.getMessage()); }
    }

    @FXML private void handleSearch() { applyFilter(); }
    @FXML private void handleFilter() { applyFilter(); }

    private void applyFilter() {
        if (filteredList == null) return;
        String kw = txtSearch!=null?txtSearch.getText().toLowerCase().trim():"";
        String sf = cmbFilter!=null?cmbFilter.getValue():"All";
        filteredList.setPredicate(ps -> {
            boolean matchKw = kw.isEmpty() || String.valueOf(ps.getSlipId()).contains(kw) || (ps.getEmpName()!=null&&ps.getEmpName().toLowerCase().contains(kw)) || (ps.getDepartment()!=null&&ps.getDepartment().toLowerCase().contains(kw)) || (ps.getReason()!=null&&ps.getReason().toLowerCase().contains(kw));
            boolean matchSt = sf==null||"All".equals(sf)||(ps.getStatus()!=null&&ps.getStatus().equalsIgnoreCase(sf));
            return matchKw && matchSt;
        });
    }

    @FXML private void handleCreatePassSlip() {
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
            stage.initOwner(lblSidebarUser.getScene().getWindow());
            stage.setTitle("Create Pass Slip");
            stage.setScene(new Scene(root, winW, winH));
            stage.setResizable(false);
            stage.setWidth(winW);
            stage.setHeight(winH);
            stage.setX(screen.getMinX() + (screen.getWidth()  - winW) / 2);
            stage.setY(screen.getMinY() + (screen.getHeight() - winH) / 2);
            stage.showAndWait();
            loadDashboardData(); loadRecentActivity();
        } catch (IOException e) { showAlert(Alert.AlertType.ERROR, "Error", "Could not open Pass Slip form."); }
    }

    private void handleViewPassSlip(PassSlip ps) {
        Alert a=new Alert(Alert.AlertType.INFORMATION); a.setTitle("Pass Slip Details"); a.setHeaderText("Request ID: PS-"+ps.getSlipId());
        a.setContentText("Employee   : "+ps.getEmpName()+"\nDepartment : "+ps.getDepartment()+"\nCategory   : "+ps.getCategory()+"\nPurpose    : "+ps.getReason()+"\nTime Out   : "+ps.getFormattedTimeOut()+"\nExpected In: "+ps.getFormattedTimeIn()+"\nStatus     : "+ps.getStatus());
        a.showAndWait();
    }

    private void handleApprovePassSlip(PassSlip ps) {
        Alert confirm=new Alert(Alert.AlertType.CONFIRMATION); confirm.setTitle("Approve"); confirm.setHeaderText("Approve #"+ps.getSlipId()+"?"); confirm.setContentText("Employee: "+ps.getEmpName());
        confirm.showAndWait().ifPresent(r -> { if(r==ButtonType.OK) {
            if(passSlipDAO.updatePassSlipStatus(ps.getSlipId(),"Approved")) {
                activityLogDAO.logActivity(ps.getEmpId(),"Pass slip #"+ps.getSlipId()+" approved",currentUser!=null?currentUser.getUsername():"Admin");
                Alert pp=new Alert(Alert.AlertType.CONFIRMATION); pp.setTitle("Print"); pp.setHeaderText("✅  Approved!"); pp.setContentText("Generate PDF for "+ps.getEmpName()+"?");
                ButtonType btnPrint=new ButtonType("🖨  Print Now"); ButtonType btnSkip=new ButtonType("Skip");
                pp.getButtonTypes().setAll(btnPrint,btnSkip);
                pp.showAndWait().ifPresent(c -> { if(c==btnPrint){String p=main.utils.PassSlipPdfGenerator.generateAndOpen(ps);if(p!=null)showAlert(Alert.AlertType.INFORMATION,"PDF Ready","Saved:\n"+new File(p).getName());else showAlert(Alert.AlertType.WARNING,"PDF Failed","pip install reportlab");} });
                loadDashboardData(); loadRecentActivity();
            } else showAlert(Alert.AlertType.ERROR,"Error","Failed to approve.");
        }});
    }

    private void handleRejectPassSlip(PassSlip ps) {
        Alert c=new Alert(Alert.AlertType.CONFIRMATION); c.setTitle("Reject"); c.setHeaderText("Reject #"+ps.getSlipId()+"?"); c.setContentText("Employee: "+ps.getEmpName());
        c.showAndWait().ifPresent(r -> { if(r==ButtonType.OK) {
            if(passSlipDAO.updatePassSlipStatus(ps.getSlipId(),"Rejected")) { activityLogDAO.logActivity(ps.getEmpId(),"Pass slip #"+ps.getSlipId()+" rejected",currentUser!=null?currentUser.getUsername():"Admin"); showAlert(Alert.AlertType.INFORMATION,"Done","Rejected."); loadDashboardData(); loadRecentActivity(); }
            else showAlert(Alert.AlertType.ERROR,"Error","Failed to reject.");
        }});
    }

    private void handleRecordReturn(PassSlip ps) {
        final LocalDateTime now=LocalDateTime.now();
        final String dur=(ps.getTimeOut()!=null)?(java.time.Duration.between(ps.getTimeOut(),now).toMinutes()/60)+"h "+(java.time.Duration.between(ps.getTimeOut(),now).toMinutes()%60)+"m":"—";
        final String late=("Overdue".equalsIgnoreCase(ps.getStatus())&&ps.getTimeIn()!=null)?"\n⚠  Late: "+(java.time.Duration.between(ps.getTimeIn(),now).toMinutes()/60)+"h "+(java.time.Duration.between(ps.getTimeIn(),now).toMinutes()%60)+"m":"";
        Alert confirm=new Alert(Alert.AlertType.CONFIRMATION); confirm.setTitle("Confirm Return"); confirm.setHeaderText("Confirm return of "+ps.getEmpName()+"?");
        confirm.setContentText("PS-"+String.format("%04d",ps.getSlipId())+" | "+ps.getDepartment()+"\nTime Out: "+ps.getFormattedTimeOut()+" | Expected In: "+ps.getFormattedTimeIn()+"\nActual Return: "+now.format(DATETIME_FMT)+" | Duration: "+dur+late+"\n\nHave you personally verified the return?");
        ButtonType btnConfirm=new ButtonType("✅  Yes, Confirm Return",ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel =new ButtonType("Cancel",ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(btnConfirm,btnCancel);
        confirm.showAndWait().ifPresent(r -> { if(r==btnConfirm) {
            if(passSlipDAO.recordTimeIn(ps.getSlipId(),now,dur)) {
                activityLogDAO.logActivity(ps.getEmpId(),"Return: "+ps.getEmpName()+" at "+now.format(TIME_FMT),currentUser!=null?currentUser.getUsername():"Admin");
                showAlert(Alert.AlertType.INFORMATION,"Return Confirmed","✅  "+ps.getEmpName()+" marked RETURNED.\n"+now.format(TIME_FMT)+" | "+dur);
                cmbFilter.setValue("All"); loadDashboardData(); loadRecentActivity();
            } else showAlert(Alert.AlertType.ERROR,"Error","Failed to record return.");
        }});
    }

    @FXML private void handleNavDashboard()      { setActiveNav(btnDashboard); }
    @FXML private void handleNavPassSlip()       { setActiveNav(btnPassSlip);  stopOverdueChecker(); navigateTo("/main/resources/fxml/PassSlipIssuance.fxml","Pass Slip Issuance"); }
    @FXML private void handleNavReports()        { setActiveNav(btnReports);   stopOverdueChecker(); navigateTo("/main/resources/fxml/Reports.fxml","Reports"); }
    @FXML private void handleNavUserManagement() { setActiveNav(btnUserMgmt);  stopOverdueChecker(); navigateTo("/main/resources/fxml/UserManagement.fxml","User Management"); }

    private void setActiveNav(Button active) {
        String on  = "-fx-background-color: rgba(255,255,255,0.22); -fx-text-fill: white; -fx-font-size: 12.5px; -fx-font-weight: bold; -fx-alignment: CENTER_LEFT; -fx-padding: 10 12; -fx-background-radius: 8; -fx-cursor: hand; -fx-border-width: 0;";
        String off = "-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.75); -fx-font-size: 12.5px; -fx-alignment: CENTER_LEFT; -fx-padding: 10 12; -fx-border-width: 0; -fx-cursor: hand;";
        for (Button btn : new Button[]{btnDashboard, btnPassSlip, btnReports, btnUserMgmt}) if (btn!=null) btn.setStyle(off);
        if (active!=null) active.setStyle(on);
    }

    private void stopOverdueChecker() { if (overdueChecker!=null) overdueChecker.stop(); }

    @FXML private void handleLogout() {
        Alert c=new Alert(Alert.AlertType.CONFIRMATION); c.setTitle("Logout"); c.setHeaderText("Are you sure you want to logout?");
        c.showAndWait().ifPresent(r -> { if(r==ButtonType.OK) { stopOverdueChecker(); navigateTo("/main/resources/fxml/Login.fxml","Pass Slip Issuance System"); }});
    }

    // ── FIXED: navigateTo — no fade/loading screen, direct FXMLLoader swap ──
    private void navigateTo(String fxmlPath, String title) {
        try {
            Stage stage = (Stage) lblSidebarUser.getScene().getWindow();
            double w = stage.getWidth(), h = stage.getHeight();
            boolean wasFullscreen = stage.isFullScreen();
            boolean wasMaximized  = stage.isMaximized();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Object ctrl = loader.getController();

            String username = main.utils.SessionManager.getUsername();
            String role     = main.utils.SessionManager.getRole();
            if (ctrl instanceof ReportsController)               ((ReportsController) ctrl).initSession(username, role);
            else if (ctrl instanceof PassSlipIssuanceController) ((PassSlipIssuanceController) ctrl).initSession(username, role);
            else if (ctrl instanceof UserManagementController)   ((UserManagementController) ctrl).initSession(username, role);

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
            showAlert(Alert.AlertType.ERROR, "Nav Error", "Cannot open " + title);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a=new Alert(type); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}