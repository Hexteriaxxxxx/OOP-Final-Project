package main.controllers;

import dao.VisitorDAO;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import models.Visitor;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class VisitorController implements Initializable {

    @FXML private Label  lblAdminName, lblAdminRole;
    @FXML private Label  lblPending, lblApproved, lblRejected, lblActive;
    @FXML private Button btnNotification;
    @FXML private TextField           txtSearch;
    @FXML private ComboBox<String>    cmbFilter;
    @FXML private TableView<Visitor>  tblVisitors;
    @FXML private TableColumn<Visitor, String> colId, colName, colCompany, colPurpose;
    @FXML private TableColumn<Visitor, String> colTimeOut, colTimeIn, colHost, colStatus, colActions;

    private final VisitorDAO dao = new VisitorDAO();
    private final ObservableList<Visitor> masterList = FXCollections.observableArrayList();
    private FilteredList<Visitor> filteredList;
    private String sessionUser = "Admin", sessionRole = "Admin";
    private NotificationHelper notifHelper;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+\\-\\s]{7,15}$");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupFilter(); setupColumns(); setupActionColumn(); loadData();
    }

    public void initSession(String username, String role) {
        this.sessionUser = username; this.sessionRole = role;
        if (lblAdminName != null) lblAdminName.setText(username);
        if (lblAdminRole != null) lblAdminRole.setText(role);
    }

    @FXML private void handleNotification() {
        if (notifHelper == null) notifHelper = new NotificationHelper(btnNotification, NotificationHelper.Role.ADMIN);
        notifHelper.toggle();
    }

    private void setupFilter() {
        cmbFilter.setItems(FXCollections.observableArrayList("All","Pending","Approved","Rejected"));
        cmbFilter.setValue("All");
        filteredList = new FilteredList<>(masterList, p -> true);
        tblVisitors.setItems(filteredList);
    }

    private void setupColumns() {
        colId     .setCellValueFactory(c->new SimpleStringProperty(c.getValue().getRequestId()));
        colName   .setCellValueFactory(c->new SimpleStringProperty(c.getValue().getVisitorName()));
        colCompany.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getCompany()));
        colPurpose.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getPurpose()));
        colTimeOut.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getFormattedTimeOut()));
        colTimeIn .setCellValueFactory(c->new SimpleStringProperty(c.getValue().getFormattedTimeIn()));
        colHost   .setCellValueFactory(c->new SimpleStringProperty(c.getValue().getHostEmployee()));
        colStatus .setCellValueFactory(c->new SimpleStringProperty(c.getValue().getStatus()));
        colStatus.setCellFactory(col->new TableCell<>(){
            @Override protected void updateItem(String val,boolean empty){
                super.updateItem(val,empty);if(empty||val==null){setGraphic(null);return;}
                Label lbl=new Label(val);
                if(val.equalsIgnoreCase("Approved"))lbl.setStyle("-fx-text-fill:#2EAA5A;-fx-font-weight:bold;");
                else if(val.equalsIgnoreCase("Rejected"))lbl.setStyle("-fx-text-fill:#E53935;-fx-font-weight:bold;");
                else lbl.setStyle("-fx-text-fill:#E6A817;-fx-font-weight:bold;");
                setGraphic(lbl);setText(null);}
        });
    }

    private void setupActionColumn() {
        colActions.setCellFactory(col->new TableCell<>(){
            final Button btnView=new Button("👁");final Button btnApprove=new Button("✔");final Button btnReject=new Button("✖");
            final HBox box=new HBox(4,btnView,btnApprove,btnReject);
            {box.setAlignment(Pos.CENTER);btnView.setStyle("-fx-background-color:transparent;-fx-text-fill:#1565C0;-fx-font-size:14px;-fx-cursor:hand;-fx-padding:2 5;");btnApprove.setStyle("-fx-background-color:transparent;-fx-text-fill:#2EAA5A;-fx-font-size:14px;-fx-cursor:hand;-fx-padding:2 5;");btnReject.setStyle("-fx-background-color:transparent;-fx-text-fill:#E53935;-fx-font-size:14px;-fx-cursor:hand;-fx-padding:2 5;");
            btnView.setOnAction(e->showDetails(getTableView().getItems().get(getIndex())));btnApprove.setOnAction(e->approveVisitor(getTableView().getItems().get(getIndex())));btnReject.setOnAction(e->rejectVisitor(getTableView().getItems().get(getIndex())));}
            @Override protected void updateItem(String val,boolean empty){super.updateItem(val,empty);if(empty){setGraphic(null);return;}Visitor v=getTableView().getItems().get(getIndex());boolean pending="Pending".equalsIgnoreCase(v.getStatus());btnApprove.setVisible(pending);btnReject.setVisible(pending);setGraphic(box);}
        });
    }

    private void loadData() {
        masterList.clear();List<Visitor> rows=dao.getAllVisitors();if(rows!=null)masterList.addAll(rows);refreshStats();applyFilters();
    }

    private void refreshStats() {
        lblPending.setText(String.valueOf(dao.countPending()));lblApproved.setText(String.valueOf(dao.countApproved()));
        lblRejected.setText(String.valueOf(dao.countRejected()));lblActive.setText(String.valueOf(dao.countActiveToday()));
    }

    @FXML private void handleSearch(){applyFilters();}
    @FXML private void handleFilter(){applyFilters();}
    private void applyFilters(){
        String kw=txtSearch.getText().toLowerCase().trim();String status=cmbFilter.getValue();
        filteredList.setPredicate(v->{boolean matchSt="All".equals(status)||v.getStatus().equalsIgnoreCase(status);boolean matchKw=kw.isEmpty()||v.getVisitorName().toLowerCase().contains(kw)||v.getCompany().toLowerCase().contains(kw)||v.getPurpose().toLowerCase().contains(kw)||v.getRequestId().toLowerCase().contains(kw);return matchSt&&matchKw;});
    }

    @FXML
    private void handleNewVisitor() {
        Stage dialog=new Stage();dialog.initModality(Modality.APPLICATION_MODAL);dialog.initStyle(StageStyle.UNDECORATED);
        VBox root=new VBox();root.setStyle("-fx-background-color:white;-fx-background-radius:12;-fx-border-radius:12;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.2),16,0,0,4);");root.setPrefWidth(640);
        HBox titleBar=new HBox();titleBar.setAlignment(Pos.CENTER_LEFT);titleBar.setPadding(new Insets(18,20,14,20));titleBar.setStyle("-fx-border-color:#f0f0f0;-fx-border-width:0 0 1 0;");
        Label lblTitle=new Label("New Visitor Request");lblTitle.setFont(Font.font("System",FontWeight.BOLD,15));HBox.setHgrow(lblTitle,Priority.ALWAYS);
        Button btnClose=new Button("✕");btnClose.setStyle("-fx-background-color:transparent;-fx-text-fill:#888;-fx-font-size:14px;-fx-cursor:hand;-fx-border-width:0;");btnClose.setOnAction(e->dialog.close());
        titleBar.getChildren().addAll(lblTitle,btnClose);
        GridPane grid=new GridPane();grid.setHgap(14);grid.setVgap(10);grid.setPadding(new Insets(18,20,10,20));
        ColumnConstraints c1=new ColumnConstraints();c1.setPercentWidth(50);ColumnConstraints c2=new ColumnConstraints();c2.setPercentWidth(50);grid.getColumnConstraints().addAll(c1,c2);
        TextField txtVisitorName=styledField("e.g. Juan Dela Cruz");TextField txtCompany=styledField("e.g. PUP Santa Rosa");
        TextField txtContact=styledField("e.g. 09171234567");TextField txtEmail=styledField("e.g. juan@email.com");TextField txtHost=styledField("e.g. Justin Gian");
        TextArea txtPurpose=new TextArea();txtPurpose.setPromptText("Enter purpose of visit");txtPurpose.setPrefRowCount(3);txtPurpose.setStyle(fieldStyle());txtPurpose.setWrapText(true);
        txtContact.textProperty().addListener((obs,old,nw)->{if(!nw.matches("[0-9+\\-\\s]*"))txtContact.setText(old);});
        DatePicker datePicker=new DatePicker(LocalDate.now());datePicker.setMaxWidth(Double.MAX_VALUE);datePicker.setStyle("-fx-font-size:12.5px;");
        datePicker.setDayCellFactory(pk->new DateCell(){@Override public void updateItem(LocalDate date,boolean empty){super.updateItem(date,empty);setDisable(empty||date.isBefore(LocalDate.now()));}});
        LocalTime[] selTimeIn={LocalTime.of(8,0)};LocalTime[] selTimeOut={LocalTime.of(17,0)};
        Button btnTimeIn=timePickerBtn(selTimeIn[0]);btnTimeIn.setOnAction(e->{LocalTime p=TimePickerDialog.show(dialog,selTimeIn[0]);if(p!=null){selTimeIn[0]=p;btnTimeIn.setText("🕐  "+p.format(TIME_FMT));}});
        Button btnTimeOut=timePickerBtn(selTimeOut[0]);btnTimeOut.setOnAction(e->{LocalTime p=TimePickerDialog.show(dialog,selTimeOut[0]);if(p!=null){selTimeOut[0]=p;btnTimeOut.setText("🕐  "+p.format(TIME_FMT));}});
        Label lblError=new Label("");lblError.setStyle("-fx-text-fill:#dc3545;-fx-font-size:11px;");lblError.setWrapText(true);
        grid.add(labelFor("Visitor Name *"),0,0);grid.add(labelFor("Company/Organization"),1,0);grid.add(txtVisitorName,0,1);grid.add(txtCompany,1,1);
        grid.add(labelFor("Contact Number *"),0,2);grid.add(labelFor("Email Address *"),1,2);grid.add(txtContact,0,3);grid.add(txtEmail,1,3);
        grid.add(labelFor("Host Employee *"),0,4);grid.add(labelFor("Visit Date *"),1,4);grid.add(txtHost,0,5);grid.add(datePicker,1,5);
        grid.add(labelFor("Expected Time In *"),0,6);grid.add(labelFor("Expected Time Out *"),1,6);grid.add(btnTimeIn,0,7);grid.add(btnTimeOut,1,7);
        grid.add(labelFor("Purpose of Visit *"),0,8,2,1);grid.add(txtPurpose,0,9,2,1);grid.add(lblError,0,10,2,1);
        GridPane.setFillWidth(btnTimeIn,true);btnTimeIn.setMaxWidth(Double.MAX_VALUE);GridPane.setFillWidth(btnTimeOut,true);btnTimeOut.setMaxWidth(Double.MAX_VALUE);
        HBox noteBox=new HBox(4);noteBox.setStyle("-fx-background-color:#fff5f5;-fx-background-radius:8;-fx-border-color:#ffd6d6;-fx-border-radius:8;-fx-border-width:1;");noteBox.setPadding(new Insets(10,14,10,14));
        Label noteLbl=new Label("Note: ");noteLbl.setFont(Font.font("System",FontWeight.BOLD,12));noteLbl.setTextFill(Color.web("#8B0000"));
        Label noteTxt=new Label("Request will be sent for approval.");noteTxt.setStyle("-fx-font-size:12px;-fx-text-fill:#555;");
        noteBox.getChildren().addAll(noteLbl,noteTxt);VBox noteWrapper=new VBox(noteBox);noteWrapper.setPadding(new Insets(6,20,10,20));
        HBox footer=new HBox(10);footer.setAlignment(Pos.CENTER_RIGHT);footer.setPadding(new Insets(12,20,16,20));footer.setStyle("-fx-border-color:#f0f0f0;-fx-border-width:1 0 0 0;");
        Button btnCancel=new Button("Cancel");btnCancel.setStyle("-fx-background-color:transparent;-fx-text-fill:#555;-fx-font-size:12px;-fx-padding:7 18;-fx-border-color:#ccc;-fx-border-radius:6;-fx-background-radius:6;-fx-cursor:hand;");btnCancel.setOnAction(e->dialog.close());
        Button btnSubmit=new Button("🖫  Submit Request");btnSubmit.setStyle("-fx-background-color:#8B0000;-fx-text-fill:white;-fx-font-size:12px;-fx-padding:7 18;-fx-background-radius:6;-fx-border-width:0;-fx-cursor:hand;");
        btnSubmit.setOnAction(e->{
            lblError.setText("");
            if(txtVisitorName.getText().trim().isEmpty()){highlight(txtVisitorName);lblError.setText("Visitor name is required.");return;}
            if(txtContact.getText().trim().isEmpty()){highlight(txtContact);lblError.setText("Contact number is required.");return;}
            if(txtEmail.getText().trim().isEmpty()){highlight(txtEmail);lblError.setText("Email is required.");return;}
            if(txtHost.getText().trim().isEmpty()){highlight(txtHost);lblError.setText("Host employee is required.");return;}
            if(txtPurpose.getText().trim().isEmpty()){txtPurpose.setStyle(fieldStyle()+"-fx-border-color:#dc3545;");lblError.setText("Purpose is required.");return;}
            if(!EMAIL_PATTERN.matcher(txtEmail.getText().trim()).matches()){highlight(txtEmail);lblError.setText("Invalid email format.");return;}
            if(!PHONE_PATTERN.matcher(txtContact.getText().trim()).matches()){highlight(txtContact);lblError.setText("Invalid contact number.");return;}
            if(!selTimeOut[0].isAfter(selTimeIn[0])){lblError.setText("Time Out must be after Time In.");return;}
            LocalDateTime dtTimeOut=LocalDateTime.of(datePicker.getValue(),selTimeOut[0]);
            // ✅ 7-parameter constructor — includes email and contact
            Visitor v=new Visitor(
                txtVisitorName.getText().trim(),
                txtCompany.getText().trim(),
                txtPurpose.getText().trim(),
                dtTimeOut,
                txtHost.getText().trim(),
                txtEmail.getText().trim(),
                txtContact.getText().trim()
            );
            if(dao.addVisitor(v)){dialog.close();showInfo("Visitor request submitted!");loadData();}
            else lblError.setText("Failed to submit.");
        });
        footer.getChildren().addAll(btnCancel,btnSubmit);
        root.getChildren().addAll(titleBar,grid,noteWrapper,footer);
        Scene scene=new Scene(root);scene.setFill(Color.TRANSPARENT);dialog.setScene(scene);dialog.showAndWait();
    }

    private Button timePickerBtn(LocalTime t){Button btn=new Button("🕐  "+t.format(TIME_FMT));btn.setStyle("-fx-background-color:white;-fx-border-color:#e0c0c0;-fx-border-radius:6;-fx-background-radius:6;-fx-padding:8 14;-fx-font-size:12.5px;-fx-cursor:hand;-fx-text-fill:#333;-fx-alignment:CENTER_LEFT;");return btn;}
    private void highlight(TextField tf){tf.setStyle(fieldStyle()+"-fx-border-color:#dc3545;");tf.requestFocus();}
    private TextField styledField(String p){TextField tf=new TextField();tf.setPromptText(p);tf.setStyle(fieldStyle());tf.setMaxWidth(Double.MAX_VALUE);tf.focusedProperty().addListener((obs,old,f)->tf.setStyle(f?fieldStyle()+"-fx-border-color:#8B0000;":fieldStyle()));return tf;}
    private String fieldStyle(){return "-fx-background-color:white;-fx-border-color:#e0c0c0;-fx-border-radius:6;-fx-background-radius:6;-fx-padding:7 10;-fx-font-size:12.5px;";}
    private Label labelFor(String t){Label lbl=new Label(t);lbl.setStyle("-fx-font-size:12px;-fx-text-fill:#333;-fx-font-weight:bold;");return lbl;}

    private void showDetails(Visitor v){Alert a=new Alert(Alert.AlertType.INFORMATION);a.setTitle("Visitor Details");a.setHeaderText(v.getRequestId());a.setContentText("Name: "+v.getVisitorName()+"\nCompany: "+v.getCompany()+"\nPurpose: "+v.getPurpose()+"\nTime Out: "+v.getFormattedTimeOut()+"\nHost: "+v.getHostEmployee()+"\nEmail: "+v.getEmail()+"\nStatus: "+v.getStatus());a.showAndWait();}
    private void approveVisitor(Visitor v){Optional<ButtonType> res=new Alert(Alert.AlertType.CONFIRMATION,"Approve "+v.getVisitorName()+"?",ButtonType.OK,ButtonType.CANCEL).showAndWait();if(res.isPresent()&&res.get()==ButtonType.OK){if(dao.updateStatus(v.getVisitorId(),"Approved")){v.setStatus("Approved");tblVisitors.refresh();refreshStats();showInfo(v.getVisitorName()+" approved!");}else showError("Failed.");}}
    private void rejectVisitor(Visitor v){Optional<ButtonType> res=new Alert(Alert.AlertType.CONFIRMATION,"Reject "+v.getVisitorName()+"?",ButtonType.OK,ButtonType.CANCEL).showAndWait();if(res.isPresent()&&res.get()==ButtonType.OK){if(dao.updateStatus(v.getVisitorId(),"Rejected")){v.setStatus("Rejected");tblVisitors.refresh();refreshStats();showInfo(v.getVisitorName()+" rejected.");}else showError("Failed.");}}

    @FXML private void handleNavDashboard(){goTo("/main/resources/fxml/AdminDashboard.fxml","Dashboard");}
    @FXML private void handleNavPassSlip() {goTo("/main/resources/fxml/PassSlipIssuance.fxml","Pass Slip Issuance");}
    @FXML private void handleNavVisitor()  {/* already here */}
    @FXML private void handleNavReports()  {goTo("/main/resources/fxml/Reports.fxml","Reports");}
    @FXML private void handleNavUserMgmt() {goTo("/main/resources/fxml/UserManagement.fxml","User Management");}
    @FXML private void handleLogout(){Optional<ButtonType> res=new Alert(Alert.AlertType.CONFIRMATION,"Logout?",ButtonType.OK,ButtonType.CANCEL).showAndWait();if(res.isPresent()&&res.get()==ButtonType.OK)goTo("/main/resources/fxml/Login.fxml","Login");}
    private void goTo(String fxml,String title){try{FXMLLoader loader=new FXMLLoader(getClass().getResource(fxml));Parent root=loader.load();Stage stage=(Stage)tblVisitors.getScene().getWindow();double w=stage.getWidth(),h=stage.getHeight();stage.setTitle(title);stage.setScene(new Scene(root));stage.setWidth(w);stage.setHeight(h);}catch(IOException e){showError("Screen not available:\n"+fxml);}}
    private void showInfo(String msg){Alert a=new Alert(Alert.AlertType.INFORMATION);a.setTitle("Success");a.setHeaderText(null);a.setContentText(msg);a.showAndWait();}
    private void showError(String msg){Alert a=new Alert(Alert.AlertType.ERROR);a.setTitle("Error");a.setHeaderText(null);a.setContentText(msg);a.showAndWait();}
}
