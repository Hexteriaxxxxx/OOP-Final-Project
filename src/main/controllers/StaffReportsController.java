package main.controllers;

import dao.PassSlipDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.PassSlip;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class StaffReportsController implements Initializable {

    @FXML private Label lblTotalMonth, lblApprovedRate, lblAvgDuration, lblTotalVisitors;
    @FXML private Button btnNotification;
    @FXML private ToggleButton btnDailyLogs, btnMonthlyLogs;
    @FXML private VBox dailySection, monthlySection;
    @FXML private Button btnFilterToday, btnFilterWeek, btnFilterMonth, btnFilterAll, btnFilterDate;
    @FXML private TableView<PassSlip> dailyTable;
    @FXML private TableColumn<PassSlip, String> colRequestId, colDate, colEmpName, colDepartment, colPurpose, colTimeOut, colTimeIn, colDuration, colStatus;
    @FXML private TableView<ReportsController.MonthlyReport> monthlyTable;
    @FXML private TableColumn<ReportsController.MonthlyReport, String> colMonth, colTotalRequests, colApproved, colRejected, colPending, colMonthVisitors, colAvgDuration;
    @FXML private TextField        searchField;
    @FXML private ComboBox<String> filterStatus;

    private ObservableList<PassSlip> allData = FXCollections.observableArrayList();
    private FilteredList<PassSlip>   filteredDailyData;
    private final PassSlipDAO passSlipDAO = new PassSlipDAO();
    private LocalDate specificDate = null;
    private String    dateRange    = "All";
    private NotificationHelper notifHelper;

    private static final String ACTIVE_STYLE   = "-fx-background-color:#8B0000;-fx-text-fill:white;-fx-background-radius:20;-fx-border-radius:20;-fx-padding:6 18;-fx-cursor:hand;-fx-font-weight:bold;";
    private static final String INACTIVE_STYLE = "-fx-background-color:white;-fx-text-fill:#333;-fx-border-color:#ddd;-fx-border-width:1;-fx-background-radius:20;-fx-border-radius:20;-fx-padding:6 18;-fx-cursor:hand;";
    private static final String DATE_BTN_ACTIVE   = "-fx-background-color:#8B0000;-fx-text-fill:white;-fx-border-width:0;-fx-background-radius:20;-fx-font-size:12px;-fx-padding:5 14;-fx-cursor:hand;";
    private static final String DATE_BTN_INACTIVE = "-fx-background-color:white;-fx-text-fill:#555;-fx-border-color:#ddd;-fx-border-width:1;-fx-background-radius:20;-fx-font-size:12px;-fx-padding:5 14;-fx-cursor:hand;";
    private static final String DATE_BTN_PICK     = "-fx-background-color:#F0F0F0;-fx-text-fill:#333;-fx-border-color:#ccc;-fx-border-width:1;-fx-background-radius:20;-fx-font-size:12px;-fx-padding:5 14;-fx-cursor:hand;";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupDailyColumns(); setupMonthlyColumns(); setupFilterCombo();
        loadAllData(); loadMonthlyData();
        btnDailyLogs.setStyle(ACTIVE_STYLE); btnMonthlyLogs.setStyle(INACTIVE_STYLE);
        setActiveDateBtn(btnFilterAll);
    }

    @FXML
    private void handleNotification() {
        if (notifHelper == null)
            notifHelper = new NotificationHelper(btnNotification, NotificationHelper.Role.STAFF);
        notifHelper.toggle();
    }

    @FXML private void handleFilterToday(){dateRange="Today";specificDate=null;setActiveDateBtn(btnFilterToday);applyFilter();}
    @FXML private void handleFilterWeek() {dateRange="Week"; specificDate=null;setActiveDateBtn(btnFilterWeek); applyFilter();}
    @FXML private void handleFilterMonth(){dateRange="Month";specificDate=null;setActiveDateBtn(btnFilterMonth);applyFilter();}
    @FXML private void handleFilterAll()  {dateRange="All";  specificDate=null;setActiveDateBtn(btnFilterAll);  applyFilter();}

    @FXML private void handleFilterDate() {
        Stage owner=(Stage)dailyTable.getScene().getWindow();
        LocalDate picked=DatePickerDialog.show(owner,specificDate!=null?specificDate:LocalDate.now());
        if(picked!=null){specificDate=picked;dateRange="Specific";btnFilterDate.setText("📅  "+picked.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));setActiveDateBtn(btnFilterDate);applyFilter();}
    }

    private void setActiveDateBtn(Button active){
        for(Button btn:new Button[]{btnFilterToday,btnFilterWeek,btnFilterMonth,btnFilterAll})btn.setStyle(btn==active?DATE_BTN_ACTIVE:DATE_BTN_INACTIVE);
        if(active==btnFilterDate)btnFilterDate.setStyle("-fx-background-color:#8B0000;-fx-text-fill:white;-fx-border-width:0;-fx-background-radius:20;-fx-font-size:12px;-fx-padding:5 14;-fx-cursor:hand;");
        else{if(specificDate==null)btnFilterDate.setText("📅  Pick Date");btnFilterDate.setStyle(DATE_BTN_PICK);}
    }

    @FXML private void switchToDaily(){dailySection.setVisible(true);dailySection.setManaged(true);monthlySection.setVisible(false);monthlySection.setManaged(false);btnDailyLogs.setStyle(ACTIVE_STYLE);btnMonthlyLogs.setStyle(INACTIVE_STYLE);}
    @FXML private void switchToMonthly(){monthlySection.setVisible(true);monthlySection.setManaged(true);dailySection.setVisible(false);dailySection.setManaged(false);btnMonthlyLogs.setStyle(ACTIVE_STYLE);btnDailyLogs.setStyle(INACTIVE_STYLE);}

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
        dailyTable.setItems(filteredDailyData!=null?filteredDailyData:allData);
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

    private void loadAllData() {
        List<PassSlip> slips=passSlipDAO.getAllPassSlips();allData.setAll(slips!=null?slips:List.of());
        filteredDailyData=new FilteredList<>(allData,p->true);dailyTable.setItems(filteredDailyData);loadStatCards();
    }

    private void loadMonthlyData() {
        monthlyTable.setItems(FXCollections.observableArrayList(
                new ReportsController.MonthlyReport("May 2026",145,120,15,10,45,"2h 15m"),new ReportsController.MonthlyReport("April 2026",138,115,18,5,38,"2h 30m"),
                new ReportsController.MonthlyReport("March 2026",152,130,12,10,52,"2h 10m"),new ReportsController.MonthlyReport("February 2026",125,105,15,5,40,"2h 20m"),
                new ReportsController.MonthlyReport("January 2026",160,140,10,10,48,"2h 25m")));
    }

    private void loadStatCards() {
        long total=filteredDailyData!=null?filteredDailyData.size():allData.size();
        long approved=(filteredDailyData!=null?filteredDailyData:allData).stream().filter(s->"approved".equalsIgnoreCase(s.getStatus())).count();
        double rate=total==0?0.0:(double)approved/total*100;
        lblTotalMonth.setText(String.valueOf(total));lblApprovedRate.setText(String.format("%.1f%%",rate));lblAvgDuration.setText("—");lblTotalVisitors.setText("0");
    }

    private void setupFilterCombo(){filterStatus.setItems(FXCollections.observableArrayList("All","Approved","Pending","Rejected"));filterStatus.getSelectionModel().selectFirst();}
    @FXML private void handleSearch(){applyFilter();}
    @FXML private void handleFilter(){applyFilter();}

    private void applyFilter() {
        String kw=searchField.getText()==null?"":searchField.getText().toLowerCase().trim();
        String status=filterStatus.getValue(); LocalDate today=LocalDate.now();
        filteredDailyData.setPredicate(slip->{
            boolean matchDate=true;if(slip.getTimeOut()!=null){LocalDate sd=slip.getTimeOut().toLocalDate();matchDate=switch(dateRange){case"Today"->sd.equals(today);case"Week"->!sd.isBefore(today.minusDays(6))&&!sd.isAfter(today);case"Month"->sd.getMonth()==today.getMonth()&&sd.getYear()==today.getYear();case"Specific"->specificDate!=null&&sd.equals(specificDate);default->true;};}
            boolean matchKw=kw.isEmpty()||slip.getEmpName().toLowerCase().contains(kw)||slip.getDepartment().toLowerCase().contains(kw)||slip.getReason().toLowerCase().contains(kw)||String.valueOf(slip.getSlipId()).contains(kw);
            boolean matchSt=status==null||"All".equals(status)||slip.getStatus().equalsIgnoreCase(status);return matchDate&&matchKw&&matchSt;});
        loadStatCards();
    }

    @FXML private void handleExport(){new Alert(Alert.AlertType.INFORMATION,"Export coming soon!").showAndWait();}

    @FXML private void handleDashboard(){goTo("/main/resources/fxml/StaffDashboard.fxml","Dashboard");}
    @FXML private void handlePassSlip() {goTo("/main/resources/fxml/StaffPassSlipIssuance.fxml","Pass Slip");}
    @FXML private void handleVisitor()  {goTo("/main/resources/fxml/StaffVisitorModule.fxml","Visitor Module");}
    @FXML private void handleLogout(){Optional<ButtonType> res=new Alert(Alert.AlertType.CONFIRMATION,"Logout?",ButtonType.OK,ButtonType.CANCEL).showAndWait();if(res.isPresent()&&res.get()==ButtonType.OK)goTo("/main/resources/fxml/Login.fxml","Login");}
    private void goTo(String fxml,String title){try{FXMLLoader loader=new FXMLLoader(getClass().getResource(fxml));Parent root=loader.load();Stage stage=(Stage)dailyTable.getScene().getWindow();double w=stage.getWidth(),h=stage.getHeight();stage.setTitle(title);stage.setScene(new Scene(root));stage.setWidth(w);stage.setHeight(h);}catch(IOException e){new Alert(Alert.AlertType.ERROR,"Screen not available:\n"+fxml).showAndWait();}}
}
