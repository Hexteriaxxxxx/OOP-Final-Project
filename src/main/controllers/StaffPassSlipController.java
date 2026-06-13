package main.controllers;

import dao.PassSlipDAO;
import models.PassSlip;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import main.utils.SkeletonLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class StaffPassSlipController implements Initializable {

    @FXML private Label  lblStaffName, lblStaffRole;
    @FXML private Label  lblTotalApproved, lblTodayApproved, lblDownloaded, lblPrinted;
    @FXML private Button btnNotification;
    @FXML private TextField           txtSearch;
    @FXML private ComboBox<String>    cmbFilter;
    @FXML private Label               lblApprovedCount;
    @FXML private StackPane           skeletonContainer;
    @FXML private TableView<PassSlip> tblSlips;
    @FXML private TableColumn<PassSlip, String> colId, colName, colDept, colPurpose;
    @FXML private TableColumn<PassSlip, String> colTimeOut, colTimeIn, colDate, colApprovedBy, colActions;

    private final PassSlipDAO passSlipDAO = new PassSlipDAO();
    private final ObservableList<PassSlip> masterList = FXCollections.observableArrayList();
    private FilteredList<PassSlip> filteredList;
    private int downloadCount = 0, printCount = 0;
    private String sessionUser = "Staff", sessionRole = "Staff";
    private NotificationHelper notifHelper;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupFilter(); setupColumns(); setupActionColumn();
        SkeletonLoader.show(skeletonContainer);
        loadDataAsync();
    }

    private void loadDataAsync() {
        Task<List<PassSlip>> task = new Task<>() {
            @Override protected List<PassSlip> call() { return passSlipDAO.getAllPassSlips(); }
        };
        task.setOnSucceeded(e -> {
            SkeletonLoader.hide(skeletonContainer);
            masterList.clear();
            List<PassSlip> all = task.getValue();
            if (all != null) all.stream().filter(ps -> "Approved".equalsIgnoreCase(ps.getStatus())).forEach(masterList::add);
            refreshStats(); applyFilters();
        });
        task.setOnFailed(e -> SkeletonLoader.hide(skeletonContainer));
        new Thread(task, "StaffPassSlipLoader").start();
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

    private void setupFilter() {
        cmbFilter.setItems(FXCollections.observableArrayList("All Departments","IT Department","HR Department","Finance","Marketing","Operations"));
        cmbFilter.setValue("All Departments");
        filteredList = new FilteredList<>(masterList, p -> true);
        tblSlips.setItems(filteredList);
    }

    private void setupColumns() {
        colId        .setCellValueFactory(c->new SimpleStringProperty("PS-"+String.format("%04d",c.getValue().getSlipId())));
        colName      .setCellValueFactory(c->new SimpleStringProperty(c.getValue().getEmpName()));
        colDept      .setCellValueFactory(c->new SimpleStringProperty(c.getValue().getDepartment()));
        colPurpose   .setCellValueFactory(c->new SimpleStringProperty(c.getValue().getReason()));
        colTimeOut   .setCellValueFactory(c->new SimpleStringProperty(c.getValue().getFormattedTimeOut()));
        colTimeIn    .setCellValueFactory(c->new SimpleStringProperty(c.getValue().getFormattedTimeIn()));
        colDate      .setCellValueFactory(c->new SimpleStringProperty(c.getValue().getTimeOut()!=null?c.getValue().getTimeOut().format(DATE_FMT):""));
        colApprovedBy.setCellValueFactory(c->new SimpleStringProperty("User #"+c.getValue().getIssuedBy()));
    }

    private void setupActionColumn() {
        colActions.setCellFactory(col->new TableCell<>(){
            final Button btnDownload=new Button("⬇ Download"); final Button btnPrint=new Button("🖨"); final Button btnView=new Button("👁");
            final HBox box=new HBox(4,btnDownload,btnPrint,btnView);
            {box.setAlignment(Pos.CENTER);
                btnDownload.setStyle("-fx-background-color:#8B0000;-fx-text-fill:white;-fx-font-size:11px;-fx-padding:5 10;-fx-cursor:hand;-fx-background-radius:5;");
                btnPrint.setStyle("-fx-background-color:transparent;-fx-text-fill:#E67E22;-fx-font-size:15px;-fx-cursor:hand;-fx-padding:2 5;");
                btnView.setStyle("-fx-background-color:transparent;-fx-text-fill:#1565C0;-fx-font-size:15px;-fx-cursor:hand;-fx-padding:2 5;");
                btnDownload.setOnAction(e->handleDownload(getTableView().getItems().get(getIndex())));
                btnPrint.setOnAction(e->handlePrint(getTableView().getItems().get(getIndex())));
                btnView.setOnAction(e->showDetails(getTableView().getItems().get(getIndex())));}
            @Override protected void updateItem(String val,boolean empty){super.updateItem(val,empty);setGraphic(empty?null:box);}
        });
    }

    private void refreshStats() {
        int total = masterList.size();
        String today = java.time.LocalDate.now().format(DATE_FMT);
        long todayCount = masterList.stream().filter(ps -> ps.getTimeOut() != null && ps.getTimeOut().format(DATE_FMT).equals(today)).count();
        lblTotalApproved.setText(String.valueOf(total));
        lblTodayApproved.setText(String.valueOf(todayCount));
        lblDownloaded.setText(String.valueOf(downloadCount));
        lblPrinted.setText(String.valueOf(printCount));
        lblApprovedCount.setText(total + " approved requests");
    }

    @FXML private void handleSearch() { applyFilters(); }
    @FXML private void handleFilter() { applyFilters(); }

    private void applyFilters() {
        String kw = txtSearch.getText().toLowerCase().trim();
        String dept = cmbFilter.getValue();
        filteredList.setPredicate(ps -> {
            String name = ps.getEmpName() != null ? ps.getEmpName() : "";
            String department = ps.getDepartment() != null ? ps.getDepartment() : "";
            String reason = ps.getReason() != null ? ps.getReason() : "";
            boolean matchDept = "All Departments".equals(dept) || department.equalsIgnoreCase(dept);
            boolean matchKw = kw.isEmpty() || name.toLowerCase().contains(kw) || department.toLowerCase().contains(kw) || reason.toLowerCase().contains(kw) || String.valueOf(ps.getSlipId()).contains(kw);
            return matchDept && matchKw;
        });
    }

    @FXML private void handleDownloadAll() {
        if (filteredList.isEmpty()) { showError("No records."); return; }
        FileChooser fc = new FileChooser(); fc.setInitialFileName("PassSlips_All.csv"); fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV","*.csv"));
        File f = fc.showSaveDialog(tblSlips.getScene().getWindow());
        if (f != null) { try (FileWriter fw = new FileWriter(f)) {
            fw.write("Slip ID,Name,Department,Purpose,Time Out,Time In,Duration,Status\n");
            for (PassSlip ps : filteredList) fw.write(String.format("PS-%04d,%s,%s,%s,%s,%s,%s,%s\n", ps.getSlipId(), ps.getEmpName(), ps.getDepartment(), ps.getReason(), ps.getFormattedTimeOut(), ps.getFormattedTimeIn(), ps.getDuration()!=null?ps.getDuration():"", ps.getStatus()));
            downloadCount += filteredList.size(); refreshStats(); showInfo("Downloaded " + filteredList.size() + " record(s).");
        } catch (IOException e) { showError("Failed: " + e.getMessage()); } }
    }

    private void handleDownload(PassSlip ps) {
        FileChooser fc = new FileChooser(); fc.setInitialFileName("PassSlip_" + ps.getSlipId() + ".csv"); fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV","*.csv"));
        File f = fc.showSaveDialog(tblSlips.getScene().getWindow());
        if (f != null) { try (FileWriter fw = new FileWriter(f)) {
            fw.write("Slip ID,Name,Department,Purpose,Time Out,Time In,Duration,Status\n");
            fw.write(String.format("PS-%04d,%s,%s,%s,%s,%s,%s,%s\n", ps.getSlipId(), ps.getEmpName(), ps.getDepartment(), ps.getReason(), ps.getFormattedTimeOut(), ps.getFormattedTimeIn(), ps.getDuration()!=null?ps.getDuration():"", ps.getStatus()));
            downloadCount++; refreshStats(); showInfo("Downloaded: " + f.getPath());
        } catch (IOException e) { showError("Failed: " + e.getMessage()); } }
    }

    private void handlePrint(PassSlip ps) {
        Label lbl = new Label(buildText(ps)); lbl.setStyle("-fx-font-family:'Courier New';-fx-font-size:13px;-fx-padding:20;");
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(tblSlips.getScene().getWindow())) { if (job.printPage(lbl)) { job.endJob(); printCount++; refreshStats(); showInfo("Sent to printer."); } else showError("Printing failed."); }
        else if (job == null) showError("No printer found.");
    }

    private void showDetails(PassSlip ps) { Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle("Pass Slip Details"); a.setHeaderText("PS-" + String.format("%04d", ps.getSlipId())); a.setContentText(buildText(ps)); a.showAndWait(); }
    private String buildText(PassSlip ps) { return "====================================\n       EMPLOYEE PASS SLIP\n====================================\nSlip ID    : PS-" + String.format("%04d", ps.getSlipId()) + "\nName       : " + ps.getEmpName() + "\nDepartment : " + ps.getDepartment() + "\nCategory   : " + ps.getCategory() + "\nPurpose    : " + ps.getReason() + "\nTime Out   : " + ps.getFormattedTimeOut() + "\nTime In    : " + ps.getFormattedTimeIn() + "\nDuration   : " + (ps.getDuration()!=null?ps.getDuration():"—") + "\nStatus     : " + ps.getStatus() + "\n===================================="; }

    @FXML private void handleNavDashboard() { goTo("/main/resources/fxml/StaffDashboard.fxml","Dashboard"); }
    @FXML private void handleNavPassSlip()  { /* already here */ }
    @FXML private void handleNavReports()   { goTo("/main/resources/fxml/StaffReports.fxml","Reports"); }
    @FXML private void handleLogout() { Optional<ButtonType> res = new Alert(Alert.AlertType.CONFIRMATION,"Logout?",ButtonType.OK,ButtonType.CANCEL).showAndWait(); if (res.isPresent() && res.get() == ButtonType.OK) goTo("/main/resources/fxml/Login.fxml","Login"); }

    private StackPane createLoadingPane() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: white;");
        VBox box = new VBox(16);
        box.setAlignment(Pos.CENTER);
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setPrefSize(60, 60);
        spinner.setStyle("-fx-progress-color: #8B0000;");
        Label lbl = new Label("Loading...");
        lbl.setStyle("-fx-text-fill: #333333; -fx-font-size: 14px; -fx-font-family: 'Segoe UI';");
        box.getChildren().addAll(spinner, lbl);
        root.getChildren().add(box);
        return root;
    }

    private void goTo(String fxml, String title) {
        Stage stage = (Stage) tblSlips.getScene().getWindow();
        Parent currentRoot = tblSlips.getScene().getRoot();
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), currentRoot);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(ev -> {
            double w = stage.getWidth(), h = stage.getHeight();
            boolean wasFullscreen = stage.isFullScreen();
            boolean wasMaximized  = stage.isMaximized();
            Scene loadScene = new Scene(createLoadingPane(), w, h);
            loadScene.setFill(Color.web("#0f0505"));
            stage.setScene(loadScene);
            stage.setWidth(w); stage.setHeight(h);
            if (wasFullscreen) Platform.runLater(() -> stage.setFullScreen(true));
            else if (wasMaximized) Platform.runLater(() -> stage.setMaximized(true));
            PauseTransition pause = new PauseTransition(Duration.millis(400));
            pause.setOnFinished(pev -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
                    Parent root = loader.load();
                    Object ctrl = loader.getController();
                    if (ctrl instanceof StaffReportsController) ((StaffReportsController) ctrl).initSession(sessionUser, sessionRole);
                    else if (ctrl instanceof StaffDashboardController) ((StaffDashboardController) ctrl).initSession(sessionUser, sessionRole);
                    root.setOpacity(0);
                    stage.setTitle(title);
                    Scene navScene = new Scene(root);
                    navScene.setFill(Color.web("#0f0505"));
                    stage.setScene(navScene);
                    stage.setWidth(w); stage.setHeight(h);
                    if (wasFullscreen) Platform.runLater(() -> stage.setFullScreen(true));
                    else if (wasMaximized) Platform.runLater(() -> stage.setMaximized(true));
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(200), root);
                    fadeIn.setFromValue(0); fadeIn.setToValue(1); fadeIn.play();
                } catch (IOException e) { showError("Screen not available:\n" + fxml); }
            });
            pause.play();
        });
        fadeOut.play();
    }

    private void showInfo(String msg)  { Alert a=new Alert(Alert.AlertType.INFORMATION);a.setTitle("Success");a.setHeaderText(null);a.setContentText(msg);a.showAndWait(); }
    private void showError(String msg) { Alert a=new Alert(Alert.AlertType.ERROR);a.setTitle("Error");a.setHeaderText(null);a.setContentText(msg);a.showAndWait(); }
}
