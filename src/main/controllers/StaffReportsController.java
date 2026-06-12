package main.controllers;

import dao.MonthlyReportDAO;
import dao.PassSlipDAO;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import main.utils.SkeletonLoader;
import models.PassSlip;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class StaffReportsController implements Initializable {

    @FXML private Label lblTotalMonth, lblApprovedRate, lblAvgDuration, lblTotalVisitors;
    @FXML private Label lblStaffName, lblStaffRole;
    @FXML private Button btnNotification;
    @FXML private ToggleButton btnDailyLogs, btnMonthlyLogs;
    @FXML private VBox dailySection, monthlySection;
    @FXML private Button btnFilterToday, btnFilterWeek, btnFilterMonth, btnFilterAll, btnFilterDate;
    @FXML private StackPane skeletonContainer;
    @FXML private TableView<PassSlip> dailyTable;
    @FXML private TableColumn<PassSlip, String> colRequestId, colDate, colEmpName, colDepartment, colPurpose, colTimeOut, colTimeIn, colDuration, colStatus;
    @FXML private TableView<ReportsController.MonthlyReport> monthlyTable;
    @FXML private TableColumn<ReportsController.MonthlyReport, String> colMonth, colTotalRequests, colApproved, colRejected, colPending, colMonthVisitors, colAvgDuration;
    @FXML private TextField        searchField;
    @FXML private ComboBox<String> filterStatus;

    private final ObservableList<PassSlip> allData = FXCollections.observableArrayList();
    private FilteredList<PassSlip> filteredDailyData;
    private final PassSlipDAO      passSlipDAO      = new PassSlipDAO();
    private final MonthlyReportDAO monthlyReportDAO = new MonthlyReportDAO();
    private LocalDate specificDate = null;
    private String    dateRange    = "All";
    private boolean   isMonthlyTab = false;
    private String sessionUser = "Staff", sessionRole = "Staff";
    private NotificationHelper notifHelper;

    private static final String TOGGLE_ACTIVE   = "-fx-background-color:#8B0000;-fx-text-fill:white;-fx-background-radius:20;-fx-border-radius:20;-fx-padding:6 18;-fx-cursor:hand;-fx-font-weight:bold;-fx-border-width:0;";
    private static final String TOGGLE_INACTIVE = "-fx-background-color:white;-fx-text-fill:#333;-fx-border-color:#ddd;-fx-border-width:1;-fx-background-radius:20;-fx-border-radius:20;-fx-padding:6 18;-fx-cursor:hand;";
    private static final String DATE_ACTIVE     = "-fx-background-color:#8B0000;-fx-text-fill:white;-fx-border-width:0;-fx-background-radius:20;-fx-border-radius:20;-fx-font-size:12px;-fx-padding:5 14;-fx-cursor:hand;-fx-font-weight:bold;";
    private static final String DATE_INACTIVE   = "-fx-background-color:white;-fx-text-fill:#555;-fx-border-color:#ddd;-fx-border-width:1;-fx-background-radius:20;-fx-border-radius:20;-fx-font-size:12px;-fx-padding:5 14;-fx-cursor:hand;";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupDailyColumns(); setupMonthlyColumns(); setupFilterCombo();
        filteredDailyData = new FilteredList<>(allData, p -> true);
        dailyTable.setItems(filteredDailyData);
        btnDailyLogs.setStyle(TOGGLE_ACTIVE);  btnMonthlyLogs.setStyle(TOGGLE_INACTIVE);
        btnFilterAll.setStyle(DATE_ACTIVE);    btnFilterToday.setStyle(DATE_INACTIVE);
        btnFilterWeek.setStyle(DATE_INACTIVE); btnFilterMonth.setStyle(DATE_INACTIVE);
        btnFilterDate.setStyle(DATE_INACTIVE);
        SkeletonLoader.show(skeletonContainer);
        loadAllDataAsync();
        loadMonthlyData();
    }

    private void loadAllDataAsync() {
        Task<List<PassSlip>> task = new Task<>() {
            @Override protected List<PassSlip> call() { return passSlipDAO.getAllPassSlips(); }
        };
        task.setOnSucceeded(e -> {
            SkeletonLoader.hide(skeletonContainer);
            List<PassSlip> slips = task.getValue();
            allData.setAll(slips != null ? slips : List.of());
            loadStatCards();
        });
        task.setOnFailed(e -> SkeletonLoader.hide(skeletonContainer));
        new Thread(task, "StaffReportsLoader").start();
    }

    public void initSession(String username, String role) {
        this.sessionUser = username != null ? username : "Staff";
        this.sessionRole = role     != null ? role     : "Staff";
        if (lblStaffName != null) lblStaffName.setText(this.sessionUser);
        if (lblStaffRole != null) lblStaffRole.setText(capitalize(this.sessionRole));
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase();
    }

    @FXML private void handleNotification() {
        if (notifHelper == null) notifHelper = new NotificationHelper(btnNotification, NotificationHelper.Role.STAFF);
        notifHelper.toggle();
    }

    @FXML private void handleFilterToday() { dateRange="Today"; specificDate=null; setActiveDateBtn(btnFilterToday); applyFilter(); }
    @FXML private void handleFilterWeek()  { dateRange="Week";  specificDate=null; setActiveDateBtn(btnFilterWeek);  applyFilter(); }
    @FXML private void handleFilterMonth() { dateRange="Month"; specificDate=null; setActiveDateBtn(btnFilterMonth); applyFilter(); }
    @FXML private void handleFilterAll()   { dateRange="All";   specificDate=null; setActiveDateBtn(btnFilterAll);   applyFilter(); }

    @FXML private void handleFilterDate() {
        Stage owner = (Stage) dailyTable.getScene().getWindow();
        LocalDate picked = DatePickerDialog.show(owner, specificDate != null ? specificDate : LocalDate.now());
        if (picked != null) { specificDate=picked; dateRange="Specific"; btnFilterDate.setText("📅  "+picked.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))); setActiveDateBtn(btnFilterDate); applyFilter(); }
    }

    private void setActiveDateBtn(Button active) {
        for (Button btn : new Button[]{btnFilterToday,btnFilterWeek,btnFilterMonth,btnFilterAll,btnFilterDate}) btn.setStyle(btn==active ? DATE_ACTIVE : DATE_INACTIVE);
        if (active != btnFilterDate && specificDate == null) btnFilterDate.setText("Pick Date");
    }

    @FXML private void switchToDaily()  { isMonthlyTab=false; dailySection.setVisible(true);dailySection.setManaged(true);monthlySection.setVisible(false);monthlySection.setManaged(false);btnDailyLogs.setStyle(TOGGLE_ACTIVE);btnMonthlyLogs.setStyle(TOGGLE_INACTIVE); }
    @FXML private void switchToMonthly(){ isMonthlyTab=true; monthlySection.setVisible(true);monthlySection.setManaged(true);dailySection.setVisible(false);dailySection.setManaged(false);btnMonthlyLogs.setStyle(TOGGLE_ACTIVE);btnDailyLogs.setStyle(TOGGLE_INACTIVE);loadMonthlyData(); }

    private void setupDailyColumns() {
        colRequestId .setCellValueFactory(d->new SimpleStringProperty("PS-"+d.getValue().getSlipId()));
        colDate      .setCellValueFactory(d->new SimpleStringProperty(d.getValue().getTimeOut()!=null?d.getValue().getTimeOut().toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")):"-"));
        colEmpName   .setCellValueFactory(d->new SimpleStringProperty(d.getValue().getEmpName()));
        colDepartment.setCellValueFactory(d->new SimpleStringProperty(d.getValue().getDepartment()));
        colPurpose   .setCellValueFactory(d->new SimpleStringProperty(d.getValue().getReason()));
        colTimeOut   .setCellValueFactory(d->new SimpleStringProperty(d.getValue().getFormattedTimeOut()));
        colTimeIn    .setCellValueFactory(d->new SimpleStringProperty(d.getValue().getTimeIn()!=null?d.getValue().getFormattedTimeIn():"—"));
        colDuration  .setCellValueFactory(d->new SimpleStringProperty(d.getValue().getDuration()!=null?d.getValue().getDuration():"—"));
        colStatus    .setCellValueFactory(d->new SimpleStringProperty(d.getValue().getStatus()));
        colStatus.setCellFactory(col->new TableCell<>(){@Override protected void updateItem(String item,boolean empty){super.updateItem(item,empty);if(empty||item==null){setText(null);setStyle("");return;}setText(item);switch(item.toLowerCase()){case"approved"->setStyle("-fx-text-fill:#1D9E75;-fx-font-weight:bold;");case"pending"->setStyle("-fx-text-fill:#BA7517;-fx-font-weight:bold;");case"rejected"->setStyle("-fx-text-fill:#E24B4A;-fx-font-weight:bold;");default->setStyle("");}}});
    }

    private void setupMonthlyColumns() {
        colMonth        .setCellValueFactory(d->new SimpleStringProperty(d.getValue().getMonth()));
        colTotalRequests.setCellValueFactory(d->new SimpleStringProperty(String.valueOf(d.getValue().getTotalRequests())));
        colApproved     .setCellValueFactory(d->new SimpleStringProperty(String.valueOf(d.getValue().getApproved())));
        colRejected     .setCellValueFactory(d->new SimpleStringProperty(String.valueOf(d.getValue().getRejected())));
        colPending      .setCellValueFactory(d->new SimpleStringProperty(String.valueOf(d.getValue().getPending())));
        colMonthVisitors.setCellValueFactory(d->new SimpleStringProperty(String.valueOf(d.getValue().getTotalVisitors())));
        colAvgDuration  .setCellValueFactory(d->new SimpleStringProperty(d.getValue().getAvgDuration()));
    }

    private void loadMonthlyData() {
        List<MonthlyReportDAO.MonthlyRow> rows = monthlyReportDAO.getMonthlyReports();
        ObservableList<ReportsController.MonthlyReport> data = FXCollections.observableArrayList();
        if (!rows.isEmpty()) for (MonthlyReportDAO.MonthlyRow row : rows) data.add(new ReportsController.MonthlyReport(row.month, row.totalRequests, row.approved, row.rejected, row.pending, row.totalVisitors, row.avgDuration));
        monthlyTable.setItems(data);
    }

    private void loadStatCards() {
        long total   = filteredDailyData!=null?filteredDailyData.size():allData.size();
        long approved= (filteredDailyData!=null?filteredDailyData:allData).stream().filter(s->"approved".equalsIgnoreCase(s.getStatus())).count();
        double rate  = total==0?0.0:(double)approved/total*100;
        lblTotalMonth.setText(String.valueOf(total)); lblApprovedRate.setText(String.format("%.1f%%",rate)); lblAvgDuration.setText("—"); lblTotalVisitors.setText("0");
    }

    private void setupFilterCombo() { filterStatus.setItems(FXCollections.observableArrayList("All","Approved","Pending","Rejected")); filterStatus.getSelectionModel().selectFirst(); }
    @FXML private void handleSearch() { applyFilter(); }
    @FXML private void handleFilter() { applyFilter(); }

    private void applyFilter() {
        if (filteredDailyData == null) return;
        String kw = searchField.getText()==null?"":searchField.getText().toLowerCase().trim();
        String status = filterStatus.getValue(); LocalDate today = LocalDate.now();
        filteredDailyData.setPredicate(slip -> {
            boolean matchDate=true; if(slip.getTimeOut()!=null){LocalDate sd=slip.getTimeOut().toLocalDate();matchDate=switch(dateRange){case"Today"->sd.equals(today);case"Week"->!sd.isBefore(today.minusDays(6))&&!sd.isAfter(today);case"Month"->sd.getMonth()==today.getMonth()&&sd.getYear()==today.getYear();case"Specific"->specificDate!=null&&sd.equals(specificDate);default->true;};}
            boolean matchKw = kw.isEmpty()||slip.getEmpName().toLowerCase().contains(kw)||slip.getDepartment().toLowerCase().contains(kw)||slip.getReason().toLowerCase().contains(kw)||String.valueOf(slip.getSlipId()).contains(kw);
            boolean matchSt = status==null||"All".equals(status)||slip.getStatus().equalsIgnoreCase(status);
            return matchDate&&matchKw&&matchSt;
        });
        loadStatCards();
    }

    @FXML private void handleExport() {
        FileChooser chooser = new FileChooser(); chooser.setTitle("Export Report");
        String timestamp = LocalDateTime.now().format(DATE_FMT);
        if (isMonthlyTab) {
            chooser.setInitialFileName("MonthlyReport_"+timestamp+".csv"); chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files","*.csv"));
            File file = chooser.showSaveDialog(dailyTable.getScene().getWindow()); if (file==null) return;
            try (FileWriter fw = new FileWriter(file)) { fw.write("Month,Total Requests,Approved,Rejected,Pending,Pass Slips,Avg Duration\n"); for (ReportsController.MonthlyReport row : monthlyTable.getItems()) fw.write(String.format("%s,%d,%d,%d,%d,%d,%s\n",csvEscape(row.getMonth()),row.getTotalRequests(),row.getApproved(),row.getRejected(),row.getPending(),row.getTotalVisitors(),csvEscape(row.getAvgDuration()))); showSuccess("Exported!\n"+file.getAbsolutePath()); } catch (IOException e) { showError("Export failed: "+e.getMessage()); }
        } else {
            if (filteredDailyData==null||filteredDailyData.isEmpty()){showError("No data.");return;}
            chooser.setInitialFileName("DailyReport_"+timestamp+".csv"); chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files","*.csv"));
            File file = chooser.showSaveDialog(dailyTable.getScene().getWindow()); if (file==null) return;
            try (FileWriter fw = new FileWriter(file)) { fw.write("Request ID,Date,Employee Name,Department,Purpose,Time Out,Time In,Duration,Status\n"); DateTimeFormatter dateFmt=DateTimeFormatter.ofPattern("yyyy-MM-dd"); for (PassSlip slip : filteredDailyData) fw.write(String.format("PS-%04d,%s,%s,%s,%s,%s,%s,%s,%s\n",slip.getSlipId(),slip.getTimeOut()!=null?slip.getTimeOut().toLocalDate().format(dateFmt):"",csvEscape(slip.getEmpName()),csvEscape(slip.getDepartment()),csvEscape(slip.getReason()),csvEscape(slip.getFormattedTimeOut()),slip.getTimeIn()!=null?csvEscape(slip.getFormattedTimeIn()):"",slip.getDuration()!=null?csvEscape(slip.getDuration()):"",csvEscape(slip.getStatus()))); showSuccess("Exported "+filteredDailyData.size()+" record(s).\n"+file.getAbsolutePath()); } catch (IOException e) { showError("Export failed: "+e.getMessage()); }
        }
    }

    private String csvEscape(String val){if(val==null)return"";if(val.contains(",")||val.contains("\"")||val.contains("\n"))return"\""+val.replace("\"","\"\"")+"\"";return val;}
    private void showSuccess(String msg){Alert a=new Alert(Alert.AlertType.INFORMATION);a.setTitle("Export Successful");a.setHeaderText(null);a.setContentText(msg);a.showAndWait();}
    private void showError(String msg)  {Alert a=new Alert(Alert.AlertType.ERROR);a.setTitle("Export Failed");a.setHeaderText(null);a.setContentText(msg);a.showAndWait();}

    @FXML private void handleDashboard(){goTo("/main/resources/fxml/StaffDashboard.fxml","Dashboard");}
    @FXML private void handlePassSlip() {goTo("/main/resources/fxml/StaffPassSlipIssuance.fxml","Pass Slip");}
    @FXML private void handleLogout(){Optional<ButtonType> res=new Alert(Alert.AlertType.CONFIRMATION,"Logout?",ButtonType.OK,ButtonType.CANCEL).showAndWait();if(res.isPresent()&&res.get()==ButtonType.OK)goTo("/main/resources/fxml/Login.fxml","Login");}

    private void goTo(String fxml, String title) {
        try { FXMLLoader loader=new FXMLLoader(getClass().getResource(fxml));Parent root=loader.load();Object ctrl=loader.getController();
              if(ctrl instanceof StaffPassSlipController)((StaffPassSlipController)ctrl).initSession(sessionUser,sessionRole);
              else if(ctrl instanceof StaffDashboardController)((StaffDashboardController)ctrl).initSession(sessionUser,sessionRole);
              Stage stage=(Stage)dailyTable.getScene().getWindow();double w=stage.getWidth(),h=stage.getHeight();stage.setTitle(title);stage.setScene(new Scene(root));stage.setWidth(w);stage.setHeight(h); } catch(IOException e){new Alert(Alert.AlertType.ERROR,"Screen not available:\n"+fxml).showAndWait();}
    }
}
