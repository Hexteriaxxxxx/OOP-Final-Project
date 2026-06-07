package main.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import dao.ActivityLogDAO;
import dao.PassSlipDAO;
import main.utils.OverdueCheckerService;
import models.ActivityLog;
import models.PassSlip;
import models.User;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML private Label  lblSidebarUser, lblSidebarRole, lblWelcome;
    @FXML private Label  lblPendingCount, lblApprovedCount, lblRejectedCount, lblActiveCount;
    @FXML private Label  lblTotalRequests, lblApprovalRate, lblActiveNow;
    @FXML private Button btnDashboard, btnPassSlip, btnReports, btnUserMgmt;
    @FXML private Button btnNotification;
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
        // Init filteredList ONCE — never recreate it
        filteredList = new FilteredList<>(masterList, p -> true);
        tblPassSlips.setItems(filteredList);
        setupTableColumns();
        setupFilterComboBox();
        loadDashboardData();
        loadRecentActivity();
        startOverdueChecker();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            lblSidebarUser.setText(user.getUsername());
            lblSidebarRole.setText(user.getRole());
            lblWelcome.setText("Welcome back, " + user.getUsername());
        }
    }

    // ── Overdue Checker ──────────────────────────────────────────
    private void startOverdueChecker() {
        overdueChecker = new OverdueCheckerService(this::onOverdueDetected);
        overdueChecker.start();
    }

    private void onOverdueDetected(List<PassSlip> overdueSlips) {
        // Refresh data first so Overdue status is in masterList
        loadDashboardData();
        loadRecentActivity();

        StringBuilder msg = new StringBuilder();
        msg.append("The following employee(s) have NOT returned on time:\n\n");
        for (PassSlip slip : overdueSlips) {
            String expected = slip.getTimeIn() != null ? slip.getTimeIn().format(TIME_FMT) : "—";
            long minsLate   = slip.getTimeIn() != null
                ? Duration.between(slip.getTimeIn(), LocalDateTime.now()).toMinutes() : 0;
            msg.append("• ").append(slip.getEmpName())
               .append("  (PS-").append(String.format("%04d", slip.getSlipId())).append(")")
               .append("\n  Expected: ").append(expected)
               .append("  |  ").append(minsLate).append(" min(s) late\n\n");
        }
        msg.append("Click 'View Overdue' to see them in the dashboard.");

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("⚠  Overdue Pass Slips");
        alert.setHeaderText("⚠  " + overdueSlips.size() + " employee(s) have not returned on time!");
        alert.setContentText(msg.toString());

        ButtonType btnViewOverdue = new ButtonType("View Overdue", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnDismiss     = new ButtonType("Dismiss",      ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(btnViewOverdue, btnDismiss);
        alert.show();

        alert.resultProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == btnViewOverdue && cmbFilter != null) {
                // Set filter to Overdue THEN reload — data already has Overdue records
                cmbFilter.setValue("Overdue");
                loadDashboardData(); // reloads masterList and calls applyFilter with Overdue set
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
                Label badge = new Label(status);
                badge.setPadding(new Insets(3, 10, 3, 10));
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
            final Button btnView    = new Button("👁");
            final Button btnApprove = new Button("✓");
            final Button btnReject  = new Button("✗");
            final Button btnReturn  = new Button("↩ Return");
            final HBox   box        = new HBox(4, btnView, btnApprove, btnReject, btnReturn);
            {
                btnView   .setStyle("-fx-background-color: transparent; -fx-font-size: 13px; -fx-cursor: hand;");
                btnApprove.setStyle("-fx-background-color: transparent; -fx-text-fill: #28a745; -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;");
                btnReject .setStyle("-fx-background-color: transparent; -fx-text-fill: #dc3545; -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;");
                btnReturn .setStyle("-fx-background-color: #FF6B35; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 3 8; -fx-background-radius: 10; -fx-cursor: hand; -fx-border-width: 0;");
                btnReturn.setTooltip(new Tooltip("Record employee return"));
                btnView   .setOnAction(e -> handleViewPassSlip  (getTableView().getItems().get(getIndex())));
                btnApprove.setOnAction(e -> handleApprovePassSlip(getTableView().getItems().get(getIndex())));
                btnReject .setOnAction(e -> handleRejectPassSlip (getTableView().getItems().get(getIndex())));
                btnReturn .setOnAction(e -> handleRecordReturn   (getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                PassSlip ps   = getTableView().getItems().get(getIndex());
                String status = ps.getStatus() != null ? ps.getStatus().toLowerCase() : "";
                btnApprove.setVisible(status.equals("pending"));
                btnReject .setVisible(status.equals("pending"));
                btnReturn .setVisible(status.equals("approved") || status.equals("overdue"));
                setGraphic(box);
            }
        });
    }

    private void setupFilterComboBox() {
        cmbFilter.setItems(FXCollections.observableArrayList("All","Pending","Approved","Overdue","Rejected","Returned"));
        cmbFilter.setValue("All");
    }

    private void loadDashboardData() {
        try {
            List<PassSlip> all = passSlipDAO.getAllPassSlips();
            masterList.setAll(all);
            applyFilter(); // Uses current cmbFilter value — preserves active filter

            long pending  = all.stream().filter(p -> "Pending" .equalsIgnoreCase(p.getStatus())).count();
            long approved = all.stream().filter(p -> "Approved".equalsIgnoreCase(p.getStatus())).count();
            long rejected = all.stream().filter(p -> "Rejected".equalsIgnoreCase(p.getStatus())).count();
            long overdue  = all.stream().filter(p -> "Overdue" .equalsIgnoreCase(p.getStatus())).count();
            lblPendingCount .setText(String.valueOf(pending));
            lblApprovedCount.setText(String.valueOf(approved));
            lblRejectedCount.setText(overdue > 0 ? "⚠ " + overdue : String.valueOf(rejected));
            lblActiveCount  .setText(String.valueOf(approved + overdue));
            List<PassSlip> today = passSlipDAO.getTodayPassSlips();
            long todayApproved   = today.stream().filter(p -> "Approved".equalsIgnoreCase(p.getStatus())).count();
            int  approvalRate    = today.isEmpty() ? 0 : (int)((todayApproved*100)/today.size());
            lblTotalRequests.setText(String.valueOf(today.size()));
            lblApprovalRate .setText(approvalRate + "%");
            lblActiveNow    .setText(String.valueOf(approved + overdue));
        } catch (Exception e) { System.out.println("Dashboard Error: " + e.getMessage()); }
    }

    private void loadRecentActivity() {
        try {
            vboxRecentActivity.getChildren().clear();
            List<ActivityLog> logs = activityLogDAO.getRecentLogs(10);
            if (logs.isEmpty()) {
                Label empty = new Label("No recent activity.");
                empty.setStyle("-fx-font-size: 11px; -fx-text-fill: #999;");
                vboxRecentActivity.getChildren().add(empty); return;
            }
            for (ActivityLog log : logs) {
                HBox row = new HBox(8); row.setAlignment(Pos.CENTER_LEFT);
                Circle dot = new Circle(4, Color.web("#8B0000"));
                VBox textBox = new VBox(1);
                Label lblAction = new Label(log.getAction());
                Label lblTime   = new Label(log.getFormattedTimestamp());
                lblAction.setStyle("-fx-font-size: 11px; -fx-text-fill: #333;");
                lblTime  .setStyle("-fx-font-size: 10px; -fx-text-fill: #999;");
                textBox.getChildren().addAll(lblAction, lblTime);
                row.getChildren().addAll(dot, textBox);
                vboxRecentActivity.getChildren().add(row);
            }
        } catch (Exception e) { System.out.println("Activity Error: " + e.getMessage()); }
    }

    @FXML private void handleSearch() { applyFilter(); }
    @FXML private void handleFilter() { applyFilter(); }

    private void applyFilter() {
        if (filteredList == null) return;
        String kw = txtSearch != null ? txtSearch.getText().toLowerCase().trim() : "";
        String sf = cmbFilter != null ? cmbFilter.getValue() : "All";
        filteredList.setPredicate(ps -> {
            boolean matchKw = kw.isEmpty()
                || String.valueOf(ps.getSlipId()).contains(kw)
                || (ps.getEmpName()    != null && ps.getEmpName()   .toLowerCase().contains(kw))
                || (ps.getDepartment() != null && ps.getDepartment().toLowerCase().contains(kw))
                || (ps.getReason()     != null && ps.getReason()    .toLowerCase().contains(kw));
            boolean matchSt = sf == null || "All".equals(sf)
                || (ps.getStatus() != null && ps.getStatus().equalsIgnoreCase(sf));
            return matchKw && matchSt;
        });
    }

    @FXML private void handleCreatePassSlip() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/fxml/CreatePassSlip.fxml"));
            Parent root = loader.load();
            CreatePassSlipController ctrl = loader.getController();
            if (currentUser != null) ctrl.setCurrentUserId(currentUser.getUserId());
            Stage stage = new Stage();
            stage.setTitle("Create Pass Slip"); stage.setScene(new Scene(root)); stage.showAndWait();
            loadDashboardData(); loadRecentActivity();
        } catch (IOException e) { showAlert(Alert.AlertType.ERROR, "Error", "Could not open Pass Slip form."); }
    }

    private void handleViewPassSlip(PassSlip ps) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Pass Slip Details"); a.setHeaderText("Request ID: PS-" + ps.getSlipId());
        a.setContentText(
            "Employee   : " + ps.getEmpName() +
            "\nDepartment : " + ps.getDepartment() +
            "\nCategory   : " + ps.getCategory() +
            "\nPurpose    : " + ps.getReason() +
            "\nTime Out   : " + ps.getFormattedTimeOut() +
            "\nExpected In: " + ps.getFormattedTimeIn() +
            "\nStatus     : " + ps.getStatus());
        a.showAndWait();
    }

    private void handleApprovePassSlip(PassSlip ps) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Approve Pass Slip");
        confirm.setHeaderText("Approve request #" + ps.getSlipId() + "?");
        confirm.setContentText("Employee: " + ps.getEmpName());
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                if (passSlipDAO.updatePassSlipStatus(ps.getSlipId(), "Approved")) {
                    activityLogDAO.logActivity(ps.getEmpId(),
                        "Pass slip #" + ps.getSlipId() + " approved",
                        currentUser != null ? currentUser.getUsername() : "Admin");
                    Alert printPrompt = new Alert(Alert.AlertType.CONFIRMATION);
                    printPrompt.setTitle("Print Pass Slip");
                    printPrompt.setHeaderText("✅  Pass slip approved!");
                    printPrompt.setContentText("Generate PDF pass slip for " + ps.getEmpName() + " now?");
                    ButtonType btnPrint = new ButtonType("🖨  Print Now");
                    ButtonType btnSkip  = new ButtonType("Skip");
                    printPrompt.getButtonTypes().setAll(btnPrint, btnSkip);
                    printPrompt.showAndWait().ifPresent(choice -> {
                        if (choice == btnPrint) {
                            String pdfPath = main.utils.PassSlipPdfGenerator.generateAndOpen(ps);
                            if (pdfPath != null) {
                                showAlert(Alert.AlertType.INFORMATION, "PDF Ready",
                                    "Pass slip saved to Desktop:\n" + new File(pdfPath).getName() +
                                    "\n\nThe PDF has been opened automatically — ready to print!");
                            } else {
                                showAlert(Alert.AlertType.WARNING, "PDF Failed",
                                    "Could not generate PDF.\nMake sure Python + reportlab are installed:\n\n  pip install reportlab");
                            }
                        }
                    });
                    loadDashboardData(); loadRecentActivity();
                } else showAlert(Alert.AlertType.ERROR, "Error", "Failed to approve pass slip.");
            }
        });
    }

    private void handleRejectPassSlip(PassSlip ps) {
        Alert c = new Alert(Alert.AlertType.CONFIRMATION);
        c.setTitle("Reject"); c.setHeaderText("Reject request #" + ps.getSlipId() + "?");
        c.setContentText("Employee: " + ps.getEmpName());
        c.showAndWait().ifPresent(r -> { if (r == ButtonType.OK) {
            if (passSlipDAO.updatePassSlipStatus(ps.getSlipId(), "Rejected")) {
                activityLogDAO.logActivity(ps.getEmpId(), "Pass slip #" + ps.getSlipId() + " rejected",
                    currentUser != null ? currentUser.getUsername() : "Admin");
                showAlert(Alert.AlertType.INFORMATION, "Done", "Pass slip rejected.");
                loadDashboardData(); loadRecentActivity();
            } else showAlert(Alert.AlertType.ERROR, "Error", "Failed to reject.");
        }});
    }

    // ── Record Return ─────────────────────────────────────────────
    private void handleRecordReturn(PassSlip ps) {
        final LocalDateTime now = LocalDateTime.now();
        final String durationOut = (ps.getTimeOut() != null)
            ? (Duration.between(ps.getTimeOut(), now).toMinutes() / 60) + "h "
              + (Duration.between(ps.getTimeOut(), now).toMinutes() % 60) + "m"
            : "—";
        final String lateInfo = ("Overdue".equalsIgnoreCase(ps.getStatus()) && ps.getTimeIn() != null)
            ? "\n⚠  Late by      : "
              + (Duration.between(ps.getTimeIn(), now).toMinutes() / 60) + "h "
              + (Duration.between(ps.getTimeIn(), now).toMinutes() % 60) + "m"
            : "";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Employee Return");
        confirm.setHeaderText("Confirm return of " + ps.getEmpName() + "?");
        confirm.setContentText(
            "Pass Slip     :  PS-" + String.format("%04d", ps.getSlipId()) +
            "\nDepartment    :  " + ps.getDepartment() +
            "\nTime Out      :  " + ps.getFormattedTimeOut() +
            "\nExpected In   :  " + ps.getFormattedTimeIn() +
            "\nActual Return :  " + now.format(DATETIME_FMT) +
            "\nTotal Duration:  " + durationOut +
            lateInfo +
            "\n\nHave you personally verified that\n" +
            ps.getEmpName() + " has returned to the campus?");

        ButtonType btnConfirm = new ButtonType("✅  Yes, Confirm Return", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel  = new ButtonType("Cancel",                  ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(btnConfirm, btnCancel);
        confirm.showAndWait().ifPresent(r -> {
            if (r == btnConfirm) {
                if (passSlipDAO.recordTimeIn(ps.getSlipId(), now, durationOut)) {
                    activityLogDAO.logActivity(ps.getEmpId(),
                        "Return confirmed: " + ps.getEmpName() +
                        " (PS-" + ps.getSlipId() + ") at " + now.format(TIME_FMT),
                        currentUser != null ? currentUser.getUsername() : "Admin");
                    showAlert(Alert.AlertType.INFORMATION, "Return Confirmed",
                        "✅  " + ps.getEmpName() + " has been marked as RETURNED." +
                        "\nActual return  : " + now.format(TIME_FMT) +
                        "\nTotal duration : " + durationOut);
                    // Reset filter to All after confirming return
                    cmbFilter.setValue("All");
                    loadDashboardData(); loadRecentActivity();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to record return. Please try again.");
                }
            }
        });
    }

    @FXML private void handleNavDashboard()      { setActiveNav(btnDashboard); }
    @FXML private void handleNavPassSlip()       { setActiveNav(btnPassSlip);  stopOverdueChecker(); navigateTo("/main/resources/fxml/PassSlipIssuance.fxml","Pass Slip Issuance"); }
    @FXML private void handleNavReports()        { setActiveNav(btnReports);   stopOverdueChecker(); navigateTo("/main/resources/fxml/Reports.fxml","Reports"); }
    @FXML private void handleNavUserManagement() { setActiveNav(btnUserMgmt);  stopOverdueChecker(); navigateTo("/main/resources/fxml/UserManagement.fxml","User Management"); }

    private void setActiveNav(Button active) {
        String on  = "-fx-background-color: rgba(255,255,255,0.22); -fx-text-fill: white; -fx-font-size: 12.5px; -fx-font-weight: bold; -fx-alignment: CENTER_LEFT; -fx-padding: 10 12; -fx-background-radius: 8; -fx-cursor: hand; -fx-border-width: 0;";
        String off = "-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.75); -fx-font-size: 12.5px; -fx-alignment: CENTER_LEFT; -fx-padding: 10 12; -fx-border-width: 0; -fx-cursor: hand;";
        for (Button btn : new Button[]{btnDashboard, btnPassSlip, btnReports, btnUserMgmt})
            if (btn != null) btn.setStyle(off);
        if (active != null) active.setStyle(on);
    }

    private void stopOverdueChecker() { if (overdueChecker != null) overdueChecker.stop(); }

    @FXML private void handleLogout() {
        Alert c = new Alert(Alert.AlertType.CONFIRMATION);
        c.setTitle("Logout"); c.setHeaderText("Are you sure you want to logout?");
        c.showAndWait().ifPresent(r -> { if (r == ButtonType.OK) {
            stopOverdueChecker();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/fxml/Login.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) lblSidebarUser.getScene().getWindow();
                stage.setScene(new Scene(root)); stage.setTitle("Pass Slip Issuance System"); stage.show();
            } catch (IOException e) { System.out.println("Logout Error: " + e.getMessage()); }
        }});
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) lblSidebarUser.getScene().getWindow();
            double w=stage.getWidth(), h=stage.getHeight();
            stage.setScene(new Scene(root)); stage.setTitle(title); stage.setWidth(w); stage.setHeight(h);
        } catch (IOException e) { showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not open " + title + "."); }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert a = new Alert(type); a.setTitle(title); a.setHeaderText(null); a.setContentText(message); a.showAndWait();
    }
}
