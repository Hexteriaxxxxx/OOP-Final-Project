package main.controllers;
import dao.PassSlipDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import models.PassSlip;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ReportsController implements Initializable {

    // ─── Stat Cards ───────────────────────────────────────────────
    @FXML private Label lblTotalMonth;
    @FXML private Label lblApprovedRate;
    @FXML private Label lblAvgDuration;
    @FXML private Label lblTotalVisitors;
    @FXML private Label lblLoggedInUser;
    @FXML private Label lblLoggedInRole;

    // ─── Tab Buttons ──────────────────────────────────────────────
    @FXML private ToggleButton btnDailyLogs;
    @FXML private ToggleButton btnMonthlyLogs;

    // ─── Sections ─────────────────────────────────────────────────
    @FXML private VBox dailySection;
    @FXML private VBox monthlySection;

    // ─── Daily Table ──────────────────────────────────────────────
    @FXML private TableView<PassSlip> dailyTable;
    @FXML private TableColumn<PassSlip, String> colRequestId;
    @FXML private TableColumn<PassSlip, String> colDate;
    @FXML private TableColumn<PassSlip, String> colEmpName;
    @FXML private TableColumn<PassSlip, String> colDepartment;
    @FXML private TableColumn<PassSlip, String> colPurpose;
    @FXML private TableColumn<PassSlip, String> colTimeOut;
    @FXML private TableColumn<PassSlip, String> colTimeIn;
    @FXML private TableColumn<PassSlip, String> colDuration;
    @FXML private TableColumn<PassSlip, String> colStatus;

    // ─── Monthly Table ────────────────────────────────────────────
    @FXML private TableView<MonthlyReport> monthlyTable;
    @FXML private TableColumn<MonthlyReport, String> colMonth;
    @FXML private TableColumn<MonthlyReport, String> colTotalRequests;
    @FXML private TableColumn<MonthlyReport, String> colApproved;
    @FXML private TableColumn<MonthlyReport, String> colRejected;
    @FXML private TableColumn<MonthlyReport, String> colPending;
    @FXML private TableColumn<MonthlyReport, String> colMonthVisitors;
    @FXML private TableColumn<MonthlyReport, String> colAvgDuration;

    // ─── Search & Filter ──────────────────────────────────────────
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterStatus;

    // ─── Data ─────────────────────────────────────────────────────
    private ObservableList<PassSlip> dailyData = FXCollections.observableArrayList();
    private FilteredList<PassSlip> filteredDailyData;

    private final PassSlipDAO passSlipDAO = new PassSlipDAO();

    // ─────────────────────────────────────────────────────────────
    //  INITIALIZE
    // ─────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupDailyColumns();
        setupMonthlyColumns();
        setupFilterCombo();
        loadDailyData();
        loadMonthlyData();
        loadStatCards();
    }

    // ─────────────────────────────────────────────────────────────
    //  TAB SWITCHING
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void switchToDaily() {
        dailySection.setVisible(true);
        dailySection.setManaged(true);
        monthlySection.setVisible(false);
        monthlySection.setManaged(false);
        btnDailyLogs.setSelected(true);
        btnMonthlyLogs.setSelected(false);
    }

    @FXML
    private void switchToMonthly() {
        monthlySection.setVisible(true);
        monthlySection.setManaged(true);
        dailySection.setVisible(false);
        dailySection.setManaged(false);
        btnMonthlyLogs.setSelected(true);
        btnDailyLogs.setSelected(false);
    }

    // ─────────────────────────────────────────────────────────────
    //  DAILY TABLE SETUP
    // ─────────────────────────────────────────────────────────────
    private void setupDailyColumns() {
        colRequestId.setCellValueFactory(data ->
                new SimpleStringProperty("PS-" + data.getValue().getSlipId()));

        colDate.setCellValueFactory(data -> {
            if (data.getValue().getTimeOut() != null)
                return new SimpleStringProperty(
                        data.getValue().getTimeOut().toLocalDate()
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            return new SimpleStringProperty("-");
        });

        colEmpName.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getEmpName()));

        colDepartment.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDepartment()));

        colPurpose.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getReason()));

        colTimeOut.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getFormattedTimeOut()));

        colTimeIn.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getTimeIn() != null
                                ? data.getValue().getFormattedTimeIn()
                                : "—"));

        colDuration.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getDuration() != null
                                ? data.getValue().getDuration()
                                : "—"));

        colStatus.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getStatus()));

        // Color-code status column
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item.toLowerCase()) {
                        case "completed" -> setStyle("-fx-text-fill: #1D9E75; -fx-font-weight: bold;");
                        case "pending"   -> setStyle("-fx-text-fill: #BA7517; -fx-font-weight: bold;");
                        case "rejected"  -> setStyle("-fx-text-fill: #E24B4A; -fx-font-weight: bold;");
                        default          -> setStyle("");
                    }
                }
            }
        });

        // Color-code approved column in monthly (reuse pattern)
        dailyTable.setItems(filteredDailyData != null ? filteredDailyData : dailyData);
    }

    // ─────────────────────────────────────────────────────────────
    //  MONTHLY TABLE SETUP
    // ─────────────────────────────────────────────────────────────
    private void setupMonthlyColumns() {
        colMonth.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getMonth()));
        colTotalRequests.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getTotalRequests())));
        colApproved.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getApproved())));
        colRejected.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getRejected())));
        colPending.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getPending())));
        colMonthVisitors.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getTotalVisitors())));
        colAvgDuration.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getAvgDuration()));

        // Green for approved
        colApproved.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setStyle(empty || item == null ? "" : "-fx-text-fill: #1D9E75; -fx-font-weight: bold;");
            }
        });

        // Red for rejected
        colRejected.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setStyle(empty || item == null ? "" : "-fx-text-fill: #E24B4A; -fx-font-weight: bold;");
            }
        });

        // Amber for pending
        colPending.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setStyle(empty || item == null ? "" : "-fx-text-fill: #BA7517; -fx-font-weight: bold;");
            }
        });
    }

    // ─────────────────────────────────────────────────────────────
    //  LOAD DATA
    // ─────────────────────────────────────────────────────────────
    private void loadDailyData() {
        List<PassSlip> slips = passSlipDAO.getTodayPassSlips();
        dailyData.setAll(slips);

        filteredDailyData = new FilteredList<>(dailyData, p -> true);
        dailyTable.setItems(filteredDailyData);
    }

    private void loadMonthlyData() {
        // TODO: Replace with real DAO call when MonthlyReportDAO is ready.
        // For now, sample data matching the screenshot.
        ObservableList<MonthlyReport> monthlyData = FXCollections.observableArrayList(
                new MonthlyReport("May 2026",      145, 120, 15, 10, 45, "2h 15m"),
                new MonthlyReport("April 2026",    138, 115, 18,  5, 38, "2h 30m"),
                new MonthlyReport("March 2026",    152, 130, 12, 10, 52, "2h 10m"),
                new MonthlyReport("February 2026", 125, 105, 15,  5, 40, "2h 20m"),
                new MonthlyReport("January 2026",  160, 140, 10, 10, 48, "2h 25m")
        );
        monthlyTable.setItems(monthlyData);
    }

    private void loadStatCards() {
        int total = passSlipDAO.countTodaySlips();
        int active = passSlipDAO.countActiveSlips();

        // Total this month — from daily data size as approximation
        lblTotalMonth.setText(String.valueOf(dailyData.size()));

        // Approved rate
        long approved = dailyData.stream()
                .filter(s -> "completed".equalsIgnoreCase(s.getStatus()))
                .count();
        double rate = dailyData.isEmpty() ? 0.0 : (double) approved / dailyData.size() * 100;
        lblApprovedRate.setText(String.format("%.1f%%", rate));

        // Avg duration — parse "Xh Ym" from duration strings
        long totalMinutes = dailyData.stream()
                .filter(s -> s.getDuration() != null && !s.getDuration().isBlank())
                .mapToLong(s -> parseDurationToMinutes(s.getDuration()))
                .sum();
        long count = dailyData.stream()
                .filter(s -> s.getDuration() != null && !s.getDuration().isBlank())
                .count();
        if (count > 0) {
            long avg = totalMinutes / count;
            lblAvgDuration.setText(String.format("%dh %02dm", avg / 60, avg % 60));
        } else {
            lblAvgDuration.setText("—");
        }

        // Visitors — placeholder; connect VisitorDAO when ready
        lblTotalVisitors.setText("45");
    }

    // ─────────────────────────────────────────────────────────────
    //  SEARCH & FILTER
    // ─────────────────────────────────────────────────────────────
    private void setupFilterCombo() {
        filterStatus.setItems(FXCollections.observableArrayList(
                "All", "Completed", "Pending", "Rejected"
        ));
        filterStatus.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleSearch() {
        applyFilter();
    }

    @FXML
    private void handleFilter() {
        applyFilter();
    }

    private void applyFilter() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String statusFilter = filterStatus.getValue();

        filteredDailyData.setPredicate(slip -> {
            boolean matchesKeyword = keyword.isEmpty()
                    || slip.getEmpName().toLowerCase().contains(keyword)
                    || String.valueOf(slip.getSlipId()).contains(keyword)
                    || slip.getDepartment().toLowerCase().contains(keyword)
                    || slip.getReason().toLowerCase().contains(keyword);

            boolean matchesStatus = statusFilter == null
                    || statusFilter.equals("All")
                    || slip.getStatus().equalsIgnoreCase(statusFilter);

            return matchesKeyword && matchesStatus;
        });
    }

    // ─────────────────────────────────────────────────────────────
    //  EXPORT
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void handleExport() {
        // TODO: Implement CSV/PDF export
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export Report");
        alert.setHeaderText(null);
        alert.setContentText("Export feature coming soon!");
        alert.showAndWait();
    }

    // ─────────────────────────────────────────────────────────────
    //  NAV HANDLERS (replace with SceneManager calls)
    // ─────────────────────────────────────────────────────────────
    @FXML private void handleDashboard()      { /* navigate to Dashboard */ }
    @FXML private void handlePassSlip()       { /* navigate to PassSlip */ }
    @FXML private void handleVisitor()        { /* navigate to Visitor */ }
    @FXML private void handleUserManagement() { /* navigate to UserManagement */ }
    @FXML private void handleLogout()         { /* navigate to Login */ }

    // ─────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────
    private long parseDurationToMinutes(String duration) {
        // Parses formats like "2h 15m", "1h 30m", "45m"
        try {
            long minutes = 0;
            if (duration.contains("h")) {
                String[] parts = duration.split("h");
                minutes += Long.parseLong(parts[0].trim()) * 60;
                if (parts.length > 1 && parts[1].contains("m"))
                    minutes += Long.parseLong(parts[1].replace("m", "").trim());
            } else if (duration.contains("m")) {
                minutes += Long.parseLong(duration.replace("m", "").trim());
            }
            return minutes;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  INNER CLASS — Monthly Report Row Model
    // ─────────────────────────────────────────────────────────────
    public static class MonthlyReport {
        private final String month;
        private final int totalRequests;
        private final int approved;
        private final int rejected;
        private final int pending;
        private final int totalVisitors;
        private final String avgDuration;

        public MonthlyReport(String month, int totalRequests, int approved,
                             int rejected, int pending, int totalVisitors, String avgDuration) {
            this.month = month;
            this.totalRequests = totalRequests;
            this.approved = approved;
            this.rejected = rejected;
            this.pending = pending;
            this.totalVisitors = totalVisitors;
            this.avgDuration = avgDuration;
        }

        public String getMonth()         { return month; }
        public int getTotalRequests()    { return totalRequests; }
        public int getApproved()         { return approved; }
        public int getRejected()         { return rejected; }
        public int getPending()          { return pending; }
        public int getTotalVisitors()    { return totalVisitors; }
        public String getAvgDuration()   { return avgDuration; }
    }
}
