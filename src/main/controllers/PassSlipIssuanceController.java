package main.controllers;

import dao.PassSlipDAO;
import dao.ActivityLogDAO;
import models.PassSlip;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class PassSlipIssuanceController implements Initializable {

    // ── Sidebar ──
    @FXML private Button btnDashboard, btnPassSlip, btnVisitor, btnReports, btnUserMgmt;
    @FXML private Label  lblAdminName, lblAdminRole;

    // ── Stat Cards ──
    @FXML private Label lblTotalApproved;
    @FXML private Label lblTodayApproved;
    @FXML private Label lblDownloaded;
    @FXML private Label lblPrinted;

    // ── Table ──
    @FXML private TextField           txtSearch;
    @FXML private ComboBox<String>    cmbFilter;
    @FXML private Label               lblApprovedCount;
    @FXML private TableView<PassSlip> tblSlips;

    @FXML private TableColumn<PassSlip, String> colId;
    @FXML private TableColumn<PassSlip, String> colName;
    @FXML private TableColumn<PassSlip, String> colDept;
    @FXML private TableColumn<PassSlip, String> colPurpose;
    @FXML private TableColumn<PassSlip, String> colTimeOut;
    @FXML private TableColumn<PassSlip, String> colTimeIn;
    @FXML private TableColumn<PassSlip, String> colDate;
    @FXML private TableColumn<PassSlip, String> colApprovedBy;
    @FXML private TableColumn<PassSlip, String> colActions;

    // ── DAOs ──
    private final PassSlipDAO    passSlipDAO    = new PassSlipDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();

    // ── Data ──
    private final ObservableList<PassSlip> masterList   = FXCollections.observableArrayList();
    private FilteredList<PassSlip>         filteredList;

    // ── Session counters ──
    private int downloadCount = 0;
    private int printCount    = 0;

    // ── Session info ──
    private String sessionUser = "Admin";
    private String sessionRole = "Admin";

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ─────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupFilter();
        setupColumns();
        setupActionColumn();
        loadData();
    }

    /** Called by previous controller after scene load */
    public void initSession(String username, String role) {
        this.sessionUser = username;
        this.sessionRole = role;
        if (lblAdminName != null) lblAdminName.setText(username);
        if (lblAdminRole != null) lblAdminRole.setText(role);
    }

    // ─────────────────────────────────────────────────────────────
    //  SETUP
    // ─────────────────────────────────────────────────────────────
    private void setupFilter() {
        cmbFilter.setItems(FXCollections.observableArrayList(
                "All Departments", "IT Department", "HR Department",
                "Finance", "Marketing", "Operations"));
        cmbFilter.setValue("All Departments");
        filteredList = new FilteredList<>(masterList, p -> true);
        tblSlips.setItems(filteredList);
    }

    private void setupColumns() {
        colId.setCellValueFactory(c ->
                new SimpleStringProperty(
                        "PS-" + String.format("%04d", c.getValue().getSlipId())));

        colName.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getEmpName()));

        colDept.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getDepartment()));

        colPurpose.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getReason()));

        colTimeOut.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getFormattedTimeOut()));

        colTimeIn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getFormattedTimeIn()));

        colDate.setCellValueFactory(c -> {
            String date = c.getValue().getTimeOut() != null
                    ? c.getValue().getTimeOut().format(DATE_FMT) : "";
            return new SimpleStringProperty(date);
        });

        // "Approved By" — issuedBy is int (user ID); show as "User #ID"
        colApprovedBy.setCellValueFactory(c ->
                new SimpleStringProperty("User #" + c.getValue().getIssuedBy()));
    }

    private void setupActionColumn() {
        colActions.setCellFactory(col -> new TableCell<PassSlip, String>() {
            final Button btnDownload = new Button("⬇ Download");
            final Button btnPrint    = new Button("🖨");
            final Button btnView     = new Button("👁");
            final HBox   box         = new HBox(4, btnDownload, btnPrint, btnView);

            {
                box.setAlignment(Pos.CENTER);

                btnDownload.setStyle(
                        "-fx-background-color:#8B0000; -fx-text-fill:white; " +
                        "-fx-font-size:11px; -fx-font-weight:bold; " +
                        "-fx-padding:5 10; -fx-cursor:hand; -fx-background-radius:5;");
                btnPrint.setStyle(
                        "-fx-background-color:transparent; -fx-text-fill:#E67E22; " +
                        "-fx-font-size:15px; -fx-cursor:hand; -fx-padding:2 5; " +
                        "-fx-background-radius:4;");
                btnView.setStyle(
                        "-fx-background-color:transparent; -fx-text-fill:#1565C0; " +
                        "-fx-font-size:15px; -fx-cursor:hand; -fx-padding:2 5; " +
                        "-fx-background-radius:4;");

                btnDownload.setOnAction(e -> {
                    PassSlip ps = getTableView().getItems().get(getIndex());
                    handleDownload(ps);
                });
                btnPrint.setOnAction(e -> {
                    PassSlip ps = getTableView().getItems().get(getIndex());
                    handlePrint(ps);
                });
                btnView.setOnAction(e -> {
                    PassSlip ps = getTableView().getItems().get(getIndex());
                    showDetails(ps);
                });
            }

            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty) { setGraphic(null); return; }
                setGraphic(box);
            }
        });
    }

    // ─────────────────────────────────────────────────────────────
    //  DATA LOADING
    //  — "Approved" status  = active/out (approved but not yet returned)
    //  — "Returned" status  = completed (shown in Issuance for download)
    // ─────────────────────────────────────────────────────────────
    private void loadData() {
        masterList.clear();

        // getAllPassSlips() — existing DAO method
        List<PassSlip> all = passSlipDAO.getAllPassSlips();
        if (all != null) {
            for (PassSlip ps : all) {
                // Show only "Approved" slips (ready for issuance/download)
                if ("Approved".equalsIgnoreCase(ps.getStatus())) {
                    masterList.add(ps);
                }
            }
        }

        refreshStats();
        applyFilters();
    }

    private void refreshStats() {
        int total = masterList.size();

        // Count today's approved from masterList
        String today = java.time.LocalDate.now().format(DATE_FMT);
        long todayCount = masterList.stream()
                .filter(ps -> ps.getTimeOut() != null &&
                        ps.getTimeOut().format(DATE_FMT).equals(today))
                .count();

        lblTotalApproved.setText(String.valueOf(total));
        lblTodayApproved.setText(String.valueOf(todayCount));
        lblDownloaded   .setText(String.valueOf(downloadCount));
        lblPrinted      .setText(String.valueOf(printCount));
        lblApprovedCount.setText(total + " approved requests");
    }

    // ─────────────────────────────────────────────────────────────
    //  SEARCH & FILTER
    // ─────────────────────────────────────────────────────────────
    @FXML private void handleSearch() { applyFilters(); }
    @FXML private void handleFilter() { applyFilters(); }

    private void applyFilters() {
        String kw   = txtSearch.getText().toLowerCase().trim();
        String dept = cmbFilter.getValue();

        filteredList.setPredicate(ps -> {
            boolean matchDept = "All Departments".equals(dept)
                    || ps.getDepartment().equalsIgnoreCase(dept);
            boolean matchKw = kw.isEmpty()
                    || ps.getEmpName()   .toLowerCase().contains(kw)
                    || ps.getDepartment().toLowerCase().contains(kw)
                    || ps.getReason()    .toLowerCase().contains(kw)
                    || String.valueOf(ps.getSlipId()).contains(kw);
            return matchDept && matchKw;
        });
    }

    // ─────────────────────────────────────────────────────────────
    //  DOWNLOAD ALL
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void handleDownloadAll() {
        if (filteredList.isEmpty()) {
            showError("No records to download.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save All Pass Slips");
        chooser.setInitialFileName("PassSlips_All.csv");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = chooser.showSaveDialog(tblSlips.getScene().getWindow());

        if (file != null) {
            try (FileWriter fw = new FileWriter(file)) {
                fw.write("Slip ID,Name,Department,Purpose,Time Out,Time In,Duration,Status\n");
                for (PassSlip ps : filteredList) {
                    fw.write(String.format("PS-%04d,%s,%s,%s,%s,%s,%s,%s\n",
                            ps.getSlipId(),
                            ps.getEmpName(),
                            ps.getDepartment(),
                            ps.getReason(),
                            ps.getFormattedTimeOut(),
                            ps.getFormattedTimeIn(),
                            ps.getDuration() != null ? ps.getDuration() : "",
                            ps.getStatus()));
                }
                downloadCount += filteredList.size();
                refreshStats();

                // Log the activity — activityLogDAO.logActivity()
                activityLogDAO.logActivity(0,
                        "Downloaded " + filteredList.size() + " pass slip(s)",
                        sessionUser);

                showInfo("Downloaded " + filteredList.size() +
                         " record(s) to:\n" + file.getPath());
            } catch (IOException e) {
                showError("Download failed:\n" + e.getMessage());
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  DOWNLOAD SINGLE
    // ─────────────────────────────────────────────────────────────
    private void handleDownload(PassSlip ps) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Pass Slip");
        chooser.setInitialFileName("PassSlip_" + ps.getSlipId() + ".csv");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = chooser.showSaveDialog(tblSlips.getScene().getWindow());

        if (file != null) {
            try (FileWriter fw = new FileWriter(file)) {
                fw.write("Slip ID,Name,Department,Purpose,Time Out,Time In,Duration,Status\n");
                fw.write(String.format("PS-%04d,%s,%s,%s,%s,%s,%s,%s\n",
                        ps.getSlipId(),
                        ps.getEmpName(),
                        ps.getDepartment(),
                        ps.getReason(),
                        ps.getFormattedTimeOut(),
                        ps.getFormattedTimeIn(),
                        ps.getDuration() != null ? ps.getDuration() : "",
                        ps.getStatus()));

                downloadCount++;
                refreshStats();

                // Log activity
                activityLogDAO.logActivity(ps.getEmpId(),
                        "Pass slip PS-" + String.format("%04d", ps.getSlipId()) + " downloaded",
                        sessionUser);

                showInfo("Pass slip downloaded:\n" + file.getPath());
            } catch (IOException e) {
                showError("Download failed:\n" + e.getMessage());
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  PRINT
    // ─────────────────────────────────────────────────────────────
    private void handlePrint(PassSlip ps) {
        Label printLabel = new Label(buildPrintText(ps));
        printLabel.setStyle(
                "-fx-font-family:'Courier New'; -fx-font-size:13px; -fx-padding:20;");

        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            boolean ok = job.showPrintDialog(tblSlips.getScene().getWindow());
            if (ok) {
                boolean printed = job.printPage(printLabel);
                if (printed) {
                    job.endJob();
                    printCount++;
                    refreshStats();

                    // Log activity
                    activityLogDAO.logActivity(ps.getEmpId(),
                            "Pass slip PS-" + String.format("%04d", ps.getSlipId()) + " printed",
                            sessionUser);

                    showInfo("Pass slip sent to printer.");
                } else {
                    showError("Printing failed.");
                }
            }
        } else {
            showError("No printer found.");
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  VIEW DETAILS
    // ─────────────────────────────────────────────────────────────
    private void showDetails(PassSlip ps) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Pass Slip Details");
        a.setHeaderText("Pass Slip ID: PS-" + String.format("%04d", ps.getSlipId()));
        a.setContentText(buildPrintText(ps));
        a.showAndWait();
    }

    private String buildPrintText(PassSlip ps) {
        return  "====================================\n" +
                "       EMPLOYEE PASS SLIP           \n" +
                "====================================\n" +
                "Slip ID    : PS-" + String.format("%04d", ps.getSlipId()) + "\n" +
                "Name       : " + ps.getEmpName()          + "\n" +
                "Department : " + ps.getDepartment()        + "\n" +
                "Purpose    : " + ps.getReason()            + "\n" +
                "Time Out   : " + ps.getFormattedTimeOut()  + "\n" +
                "Time In    : " + ps.getFormattedTimeIn()   + "\n" +
                "Duration   : " + (ps.getDuration() != null
                                    ? ps.getDuration() : "—") + "\n" +
                "Status     : " + ps.getStatus()            + "\n" +
                "====================================";
    }

    // ─────────────────────────────────────────────────────────────
    //  NAVIGATION
    // ─────────────────────────────────────────────────────────────
    @FXML private void handleNavDashboard() {
        goTo("/resources.fxml/AdminDashboard.fxml", "Dashboard");
    }
    @FXML private void handleNavPassSlip()  { /* already here */ }
    @FXML private void handleNavVisitor()   {
        goTo("/resources.fxml/Visitor.fxml", "Visitor Module");
    }
    @FXML private void handleNavReports()   {
        goTo("/resources.fxml/Reports.fxml", "Reports");
    }
    @FXML private void handleNavUserMgmt()  {
        goTo("/resources.fxml/UserManagement.fxml", "User Management");
    }
    @FXML private void handleNotifications() { }

    @FXML
    private void handleLogout() {
        Optional<ButtonType> res = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Are you sure you want to logout?",
                ButtonType.OK, ButtonType.CANCEL)
                .showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            goTo("/resources.fxml/Login.fxml", "Pass Slip System — Login");
        }
    }

    private void goTo(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root  = loader.load();
            Stage  stage = (Stage) tblSlips.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showError("Screen not yet available:\n" + fxml);
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────
    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Success");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
