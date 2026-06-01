package main.controllers;

import dao.VisitorDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
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
    //  ACTIONS
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void handleNewVisitor() {
        // Simple dialog para sa new visitor
        Dialog<Visitor> dialog = new Dialog<>();
        dialog.setTitle("New Visitor Request");
        dialog.setHeaderText("Enter Visitor Details");

        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        // Form fields
        TextField txtName    = new TextField(); txtName.setPromptText("Visitor Name");
        TextField txtCompany = new TextField(); txtCompany.setPromptText("Company");
        TextField txtPurpose = new TextField(); txtPurpose.setPromptText("Purpose");
        TextField txtHost    = new TextField(); txtHost.setPromptText("Host Employee");

        javafx.scene.layout.VBox form = new javafx.scene.layout.VBox(10,
                new Label("Visitor Name:"), txtName,
                new Label("Company:"),      txtCompany,
                new Label("Purpose:"),      txtPurpose,
                new Label("Host Employee:"),txtHost);
        form.setPadding(new javafx.geometry.Insets(10));
        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                if (txtName.getText().isEmpty() || txtCompany.getText().isEmpty()
                        || txtPurpose.getText().isEmpty() || txtHost.getText().isEmpty()) {
                    showError("Please fill in all fields.");
                    return null;
                }
                Visitor v = new Visitor(
                        txtName.getText().trim(),
                        txtCompany.getText().trim(),
                        txtPurpose.getText().trim(),
                        java.time.LocalDateTime.now(),
                        txtHost.getText().trim()
                );
                return v;
            }
            return null;
        });

        Optional<Visitor> result = dialog.showAndWait();
        result.ifPresent(v -> {
            boolean ok = dao.addVisitor(v);
            if (ok) {
                showInfo("Visitor request added successfully!");
                loadData();
            } else {
                showError("Failed to add visitor request.");
            }
        });
    }

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
    @FXML private void handleNavDashboard() { goTo("/main/resources/fxml/AdminDashboard.fxml", "Dashboard");         }
    @FXML private void handleNavPassSlip()  { goTo("/main/resources/fxml/PassSlipIssuance.fxml","Pass Slip Issuance");}
    @FXML private void handleNavVisitor()   { /* already here */ }
    @FXML private void handleNavReports()   { goTo("/main/resources/fxml/Reports.fxml",        "Reports");           }
    @FXML private void handleNavUserMgmt()  { goTo("/main/resources/fxml/UserManagement.fxml", "User Management");   }

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
