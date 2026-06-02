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
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class VisitorController implements Initializable {

    // ── Sidebar ──
    @FXML private Label lblAdminName, lblAdminRole;

    // ── Stat Cards ──
    @FXML private Label lblPending, lblApproved, lblRejected, lblActive;

    // ── Table ──
    @FXML private TextField           txtSearch;
    @FXML private ComboBox<String>    cmbFilter;
    @FXML private TableView<Visitor>  tblVisitors;

    @FXML private TableColumn<Visitor, String> colId;
    @FXML private TableColumn<Visitor, String> colName;
    @FXML private TableColumn<Visitor, String> colCompany;
    @FXML private TableColumn<Visitor, String> colPurpose;
    @FXML private TableColumn<Visitor, String> colTimeOut;
    @FXML private TableColumn<Visitor, String> colTimeIn;
    @FXML private TableColumn<Visitor, String> colHost;
    @FXML private TableColumn<Visitor, String> colStatus;
    @FXML private TableColumn<Visitor, String> colActions;

    // ── DAO ──
    private final VisitorDAO dao = new VisitorDAO();

    // ── Data ──
    private final ObservableList<Visitor> masterList  = FXCollections.observableArrayList();
    private FilteredList<Visitor>         filteredList;

    // ── Session ──
    private String sessionUser = "Admin";
    private String sessionRole = "Admin";

    // ─────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupFilter();
        setupColumns();
        setupActionColumn();
        loadData();
    }

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
                "All", "Pending", "Approved", "Rejected"));
        cmbFilter.setValue("All");
        filteredList = new FilteredList<>(masterList, p -> true);
        tblVisitors.setItems(filteredList);
    }

    private void setupColumns() {
        colId     .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRequestId()));
        colName   .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getVisitorName()));
        colCompany.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCompany()));
        colPurpose.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPurpose()));
        colTimeOut.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFormattedTimeOut()));
        colTimeIn .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFormattedTimeIn()));
        colHost   .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getHostEmployee()));

        // Status — colored label
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        colStatus.setCellFactory(col -> new TableCell<Visitor, String>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setGraphic(null); return; }
                Label lbl = new Label(val);
                if (val.equalsIgnoreCase("Approved")) {
                    lbl.setStyle("-fx-text-fill:#2EAA5A; -fx-font-weight:bold;");
                } else if (val.equalsIgnoreCase("Rejected")) {
                    lbl.setStyle("-fx-text-fill:#E53935; -fx-font-weight:bold;");
                } else {
                    lbl.setStyle("-fx-text-fill:#E6A817; -fx-font-weight:bold;");
                }
                setGraphic(lbl);
                setText(null);
            }
        });
    }

    private void setupActionColumn() {
        colActions.setCellFactory(col -> new TableCell<Visitor, String>() {
            final Button btnView    = new Button("👁");
            final Button btnApprove = new Button("✔");
            final Button btnReject  = new Button("✖");
            final HBox   box        = new HBox(4, btnView, btnApprove, btnReject);

            {
                box.setAlignment(Pos.CENTER);
                btnView   .setStyle("-fx-background-color:transparent; -fx-text-fill:#1565C0; -fx-font-size:14px; -fx-cursor:hand; -fx-padding:2 5;");
                btnApprove.setStyle("-fx-background-color:transparent; -fx-text-fill:#2EAA5A; -fx-font-size:14px; -fx-cursor:hand; -fx-padding:2 5;");
                btnReject .setStyle("-fx-background-color:transparent; -fx-text-fill:#E53935; -fx-font-size:14px; -fx-cursor:hand; -fx-padding:2 5;");

                btnView.setOnAction(e -> {
                    Visitor v = getTableView().getItems().get(getIndex());
                    showDetails(v);
                });
                btnApprove.setOnAction(e -> {
                    Visitor v = getTableView().getItems().get(getIndex());
                    approveVisitor(v);
                });
                btnReject.setOnAction(e -> {
                    Visitor v = getTableView().getItems().get(getIndex());
                    rejectVisitor(v);
                });
            }

            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty) { setGraphic(null); return; }
                Visitor v      = getTableView().getItems().get(getIndex());
                boolean pending = "Pending".equalsIgnoreCase(v.getStatus());
                btnApprove.setVisible(pending);
                btnReject .setVisible(pending);
                setGraphic(box);
            }
        });
    }

    // ─────────────────────────────────────────────────────────────
    //  DATA LOADING
    // ─────────────────────────────────────────────────────────────
    private void loadData() {
        masterList.clear();
        List<Visitor> rows = dao.getAllVisitors();
        if (rows != null) masterList.addAll(rows);
        refreshStats();
        applyFilters();
    }

    private void refreshStats() {
        lblPending .setText(String.valueOf(dao.countPending()));
        lblApproved.setText(String.valueOf(dao.countApproved()));
        lblRejected.setText(String.valueOf(dao.countRejected()));
        lblActive  .setText(String.valueOf(dao.countActiveToday()));
    }

    // ─────────────────────────────────────────────────────────────
    //  SEARCH & FILTER
    // ─────────────────────────────────────────────────────────────
    @FXML private void handleSearch() { applyFilters(); }
    @FXML private void handleFilter() { applyFilters(); }

    private void applyFilters() {
        String kw     = txtSearch.getText().toLowerCase().trim();
        String status = cmbFilter.getValue();

        filteredList.setPredicate(v -> {
            boolean matchSt = "All".equals(status)
                    || v.getStatus().equalsIgnoreCase(status);
            boolean matchKw = kw.isEmpty()
                    || v.getVisitorName().toLowerCase().contains(kw)
                    || v.getCompany()    .toLowerCase().contains(kw)
                    || v.getPurpose()    .toLowerCase().contains(kw)
                    || v.getRequestId()  .toLowerCase().contains(kw);
            return matchSt && matchKw;
        });
    }

    // ─────────────────────────────────────────────────────────────
    //  NEW VISITOR DIALOG
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void handleNewVisitor() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setTitle("New Visitor Request");

        // ── Root ──
        VBox root = new VBox();
        root.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-radius: 12;");
        root.setPrefWidth(620);

        // ── Title Bar ──
        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(18, 20, 14, 20));
        titleBar.setStyle("-fx-border-color: #f0f0f0; -fx-border-width: 0 0 1 0;");

        Label lblTitle = new Label("New Visitor Request");
        lblTitle.setFont(Font.font("System", FontWeight.BOLD, 15));
        lblTitle.setTextFill(Color.web("#1a1a1a"));
        HBox.setHgrow(lblTitle, Priority.ALWAYS);

        Button btnClose = new Button("✕");
        btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: #888; " +
                "-fx-font-size: 14px; -fx-cursor: hand; -fx-border-width: 0;");
        btnClose.setOnAction(e -> dialog.close());

        titleBar.getChildren().addAll(lblTitle, btnClose);

        // ── Form ──
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(10);
        grid.setPadding(new Insets(18, 20, 10, 20));

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);

        // Fields
        TextField txtVisitorName  = styledField("Visitor's full name");
        TextField txtCompany      = styledField("Company name");
        TextField txtContact      = styledField("Phone number");
        TextField txtEmail        = styledField("Email");
        TextField txtHost         = styledField("Employee to visit");
        TextField txtVisitDate    = styledField("YYYY-MM-DD");
        TextField txtTimeIn       = styledField("e.g. 09:00 AM");
        TextField txtTimeOut      = styledField("e.g. 05:00 PM");
        TextArea  txtPurpose      = new TextArea();
        txtPurpose.setPromptText("Enter purpose of visit");
        txtPurpose.setPrefRowCount(3);
        txtPurpose.setStyle(fieldStyle());
        txtPurpose.setWrapText(true);

        // Row 0
        grid.add(labelFor("Visitor Name"),         0, 0);
        grid.add(labelFor("Company/Organization"), 1, 0);
        grid.add(txtVisitorName,                   0, 1);
        grid.add(txtCompany,                       1, 1);

        // Row 2
        grid.add(labelFor("Contact Number"), 0, 2);
        grid.add(labelFor("Email Address"),  1, 2);
        grid.add(txtContact,                 0, 3);
        grid.add(txtEmail,                   1, 3);

        // Row 4
        grid.add(labelFor("Host Employee"), 0, 4);
        grid.add(labelFor("Visit Date"),    1, 4);
        grid.add(txtHost,                   0, 5);
        grid.add(txtVisitDate,              1, 5);

        // Row 6
        grid.add(labelFor("Expected Time In"),  0, 6);
        grid.add(labelFor("Expected Time Out"), 1, 6);
        grid.add(txtTimeIn,                     0, 7);
        grid.add(txtTimeOut,                    1, 7);

        // Row 8 — Purpose (full width)
        grid.add(labelFor("Purpose of Visit"), 0, 8, 2, 1);
        grid.add(txtPurpose,                   0, 9, 2, 1);

        // ── Note ──
        HBox noteBox = new HBox();
        noteBox.setStyle("-fx-background-color: #fff5f5; -fx-background-radius: 8; " +
                "-fx-border-color: #ffd6d6; -fx-border-radius: 8; -fx-border-width: 1;");
        noteBox.setPadding(new Insets(10, 14, 10, 14));
        noteBox.setMargin(noteBox, new Insets(0, 20, 0, 20));
        Label noteLabel = new Label("Note: ");
        noteLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        noteLabel.setTextFill(Color.web("#8B0000"));
        Label noteText = new Label("Request will be sent for approval.");
        noteText.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
        noteBox.getChildren().addAll(noteLabel, noteText);

        VBox noteWrapper = new VBox(noteBox);
        noteWrapper.setPadding(new Insets(6, 20, 10, 20));

        // ── Footer Buttons ──
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(12, 20, 16, 20));
        footer.setStyle("-fx-border-color: #f0f0f0; -fx-border-width: 1 0 0 0;");

        Button btnCancel = new Button("Cancel");
        btnCancel.setStyle("-fx-background-color: transparent; -fx-text-fill: #555; " +
                "-fx-font-size: 12px; -fx-padding: 7 18 7 18; " +
                "-fx-border-color: #ccc; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> dialog.close());

        Button btnSubmit = new Button("🖫  Submit Request");
        btnSubmit.setStyle("-fx-background-color: #8B0000; -fx-text-fill: white; " +
                "-fx-font-size: 12px; -fx-padding: 7 18 7 18; " +
                "-fx-background-radius: 6; -fx-border-width: 0; -fx-cursor: hand;");
        btnSubmit.setOnAction(e -> {
            // Validate required fields
            if (txtVisitorName.getText().trim().isEmpty()
                    || txtCompany.getText().trim().isEmpty()
                    || txtContact.getText().trim().isEmpty()
                    || txtEmail.getText().trim().isEmpty()
                    || txtHost.getText().trim().isEmpty()
                    || txtVisitDate.getText().trim().isEmpty()
                    || txtTimeIn.getText().trim().isEmpty()
                    || txtTimeOut.getText().trim().isEmpty()
                    || txtPurpose.getText().trim().isEmpty()) {
                showError("Please fill in all fields.");
                return;
            }

            Visitor v = new Visitor(
                    txtVisitorName.getText().trim(),
                    txtCompany.getText().trim(),
                    txtPurpose.getText().trim(),
                    java.time.LocalDateTime.now(),
                    txtHost.getText().trim()
            );

            boolean ok = dao.addVisitor(v);
            if (ok) {
                dialog.close();
                showInfo("Visitor request submitted successfully!");
                loadData();
            } else {
                showError("Failed to submit visitor request.");
            }
        });

        footer.getChildren().addAll(btnCancel, btnSubmit);

        // ── Assemble ──
        root.getChildren().addAll(titleBar, grid, noteWrapper, footer);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    // ── Field helpers ──
    private TextField styledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle(fieldStyle());
        tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }

    private String fieldStyle() {
        return "-fx-background-color: white; " +
                "-fx-border-color: #e0c0c0; " +
                "-fx-border-radius: 6; " +
                "-fx-background-radius: 6; " +
                "-fx-padding: 7 10 7 10; " +
                "-fx-font-size: 12.5px;";
    }

    private Label labelFor(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #333; -fx-font-weight: bold;");
        return lbl;
    }

    // ─────────────────────────────────────────────────────────────
    //  ACTIONS
    // ─────────────────────────────────────────────────────────────
    private void showDetails(Visitor v) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Visitor Details");
        a.setHeaderText(v.getRequestId());
        a.setContentText(
                "Name          : " + v.getVisitorName()      + "\n" +
                        "Company       : " + v.getCompany()          + "\n" +
                        "Purpose       : " + v.getPurpose()          + "\n" +
                        "Time Out      : " + v.getFormattedTimeOut() + "\n" +
                        "Time In       : " + v.getFormattedTimeIn()  + "\n" +
                        "Host Employee : " + v.getHostEmployee()     + "\n" +
                        "Status        : " + v.getStatus()
        );
        a.showAndWait();
    }

    private void approveVisitor(Visitor v) {
        Optional<ButtonType> res = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Approve " + v.getVisitorName() + "?",
                ButtonType.OK, ButtonType.CANCEL).showAndWait();

        if (res.isPresent() && res.get() == ButtonType.OK) {
            if (dao.updateStatus(v.getVisitorId(), "Approved")) {
                v.setStatus("Approved");
                tblVisitors.refresh();
                refreshStats();
                showInfo(v.getVisitorName() + " approved!");
            } else {
                showError("Failed to approve visitor.");
            }
        }
    }

    private void rejectVisitor(Visitor v) {
        Optional<ButtonType> res = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Reject " + v.getVisitorName() + "?",
                ButtonType.OK, ButtonType.CANCEL).showAndWait();

        if (res.isPresent() && res.get() == ButtonType.OK) {
            if (dao.updateStatus(v.getVisitorId(), "Rejected")) {
                v.setStatus("Rejected");
                tblVisitors.refresh();
                refreshStats();
                showInfo(v.getVisitorName() + " rejected.");
            } else {
                showError("Failed to reject visitor.");
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  NAVIGATION
    // ─────────────────────────────────────────────────────────────
    @FXML private void handleNavDashboard() { goTo("/main/resources/fxml/AdminDashboard.fxml",  "Dashboard");          }
    @FXML private void handleNavPassSlip()  { goTo("/main/resources/fxml/PassSlipIssuance.fxml","Pass Slip Issuance"); }
    @FXML private void handleNavVisitor()   { /* already here */ }
    @FXML private void handleNavReports()   { goTo("/main/resources/fxml/Reports.fxml",         "Reports");            }
    @FXML private void handleNavUserMgmt()  { goTo("/main/resources/fxml/UserManagement.fxml",  "User Management");    }

    @FXML
    private void handleLogout() {
        Optional<ButtonType> res = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Are you sure you want to logout?",
                ButtonType.OK, ButtonType.CANCEL).showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            goTo("/main/resources/fxml/Login.fxml", "Pass Slip System — Login");
        }
    }

    private void goTo(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root  = loader.load();
            Stage  stage = (Stage) tblVisitors.getScene().getWindow();
            double w = stage.getWidth();
            double h = stage.getHeight();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.setWidth(w);
            stage.setHeight(h);
        } catch (IOException e) {
            showError("Screen not yet available:\n" + fxml);
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────
    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Success"); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error"); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }
}