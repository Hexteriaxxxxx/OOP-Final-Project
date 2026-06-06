package main.controllers;

import dao.ActivityLogDAO;
import dao.EmployeeDAO;
import dao.PassSlipDAO;
import models.Employee;
import models.PassSlip;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CreatePassSlipController implements Initializable {

    @FXML private TextField        txtEmployee;
    @FXML private ListView<String> lstSuggestions;
    @FXML private TextField        txtDepartment;
    @FXML private ComboBox<String> cmbCategory;
    @FXML private TextField        txtDate;
    @FXML private TextArea         txtPurpose;
    @FXML private Button           btnTimeOut;
    @FXML private Button           btnTimeIn;
    @FXML private Label            lblError;
    @FXML private Button           btnCancel;
    @FXML private Button           btnSubmit;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");

    private static final String WAIVER_FULL =
        "WAIVER AND ASSUMPTION OF RISK\n\n" +
        "I hereby acknowledge that this pass slip is issued for personal reasons " +
        "and is not in connection with any official school activity or business.\n\n" +
        "The Polytechnic University of the Philippines – Santa Rosa Campus, its " +
        "administration, faculty, and staff shall NOT be held liable for any " +
        "untoward incident, accident, injury, loss, or damage that may occur " +
        "outside the school premises during the duration of this leave.\n\n" +
        "By submitting this request, I voluntarily assume full and sole " +
        "responsibility for my safety, well-being, and actions outside the " +
        "school premises. I further confirm that I have read, understood, and " +
        "agree to this waiver.\n\n" +
        "This waiver is in accordance with PUP Santa Rosa Campus policies and " +
        "applicable laws of the Republic of the Philippines.";

    private final EmployeeDAO    employeeDAO    = new EmployeeDAO();
    private final PassSlipDAO    passSlipDAO    = new PassSlipDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();

    private List<Employee> employees;
    private Employee       selectedEmployee = null;
    private int            currentUserId    = 1;
    private boolean        waiverAgreed     = false;

    private LocalTime selectedTimeOut = LocalTime.of(8, 0);
    private LocalTime selectedTimeIn  = LocalTime.of(17, 0);

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        txtDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        employees = employeeDAO.getAllEmployees();
        lstSuggestions.setVisible(false);
        lstSuggestions.setManaged(false);

        // ── Category dropdown ──
        cmbCategory.setItems(FXCollections.observableArrayList(
                "Official Business", "Personal Reason", "Others"));
        cmbCategory.setValue("Official Business");

        // ── Time buttons initial labels ──
        btnTimeOut.setText("🕐  " + selectedTimeOut.format(TIME_FMT));
        btnTimeIn .setText("🕐  " + selectedTimeIn .format(TIME_FMT));

        // ── Autocomplete ──
        txtEmployee.textProperty().addListener((obs, oldVal, newVal) -> {
            selectedEmployee = null;
            txtDepartment.setText("");
            if (newVal == null || newVal.trim().isEmpty()) { hideSuggestions(); return; }
            List<String> matches = employees.stream()
                    .filter(e -> e.getName().toLowerCase().contains(newVal.toLowerCase()))
                    .map(Employee::getName).collect(Collectors.toList());
            if (matches.isEmpty()) { hideSuggestions(); }
            else {
                lstSuggestions.setItems(FXCollections.observableArrayList(matches));
                lstSuggestions.setVisible(true);
                lstSuggestions.setManaged(true);
                lstSuggestions.setPrefHeight(Math.min(matches.size() * 28, 120));
            }
        });

        lstSuggestions.setOnMouseClicked(e -> {
            String sel = lstSuggestions.getSelectionModel().getSelectedItem();
            if (sel != null) {
                txtEmployee.setText(sel);
                selectedEmployee = employees.stream()
                        .filter(emp -> emp.getName().equals(sel)).findFirst().orElse(null);
                if (selectedEmployee != null) txtDepartment.setText(selectedEmployee.getDepartment());
                hideSuggestions();
            }
        });
    }

    // ── Category change — show waiver if Personal Reason or Others ──
    @FXML
    private void handleCategoryChange() {
        String cat = cmbCategory.getValue();
        waiverAgreed = false; // Reset waiver agreement when category changes
        if ("Personal Reason".equals(cat) || "Others".equals(cat)) {
            showWaiverDialog(cat);
        }
    }

    private void showWaiverDialog(String category) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setTitle("Waiver Agreement");

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 16, 0, 0, 4);");
        root.setPrefWidth(500);

        // Header
        VBox header = new VBox(4);
        header.setPadding(new Insets(18, 20, 14, 20));
        header.setStyle("-fx-background-color: #8B0000; -fx-background-radius: 12 12 0 0;");
        Label lblTitle = new Label("⚠  Waiver and Assumption of Risk");
        lblTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label lblSubtitle = new Label("Category: " + category);
        lblSubtitle.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.8);");
        header.getChildren().addAll(lblTitle, lblSubtitle);

        // Waiver text
        TextArea txtWaiver = new TextArea(WAIVER_FULL);
        txtWaiver.setEditable(false);
        txtWaiver.setWrapText(true);
        txtWaiver.setPrefHeight(220);
        txtWaiver.setStyle("-fx-background-color: #FFF9F9; -fx-border-color: #E8C0C0; " +
                "-fx-border-width: 0; -fx-font-size: 12px; -fx-text-fill: #333; -fx-padding: 14;");

        // Checkbox
        CheckBox chkAgree = new CheckBox(
                "I have read and fully understood the above waiver. " +
                "I voluntarily agree to its terms and conditions.");
        chkAgree.setWrapText(true);
        chkAgree.setStyle("-fx-font-size: 12px; -fx-text-fill: #333; -fx-padding: 2 0 0 0;");
        VBox checkWrapper = new VBox(chkAgree);
        checkWrapper.setPadding(new Insets(12, 20, 10, 20));
        checkWrapper.setStyle("-fx-background-color: #FFF0F0; -fx-border-color: #E8C0C0; " +
                "-fx-border-width: 1 0 0 0;");

        // Buttons
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(12, 20, 16, 20));
        footer.setStyle("-fx-border-color: #e8e8e8; -fx-border-width: 1 0 0 0;");

        Button btnDecline = new Button("Decline");
        btnDecline.setStyle("-fx-background-color: white; -fx-text-fill: #555; " +
                "-fx-font-size: 12px; -fx-padding: 8 20; -fx-background-radius: 20; " +
                "-fx-border-color: #ccc; -fx-border-radius: 20; -fx-cursor: hand;");
        btnDecline.setOnAction(e -> {
            // Revert to Official Business if declined
            cmbCategory.setValue("Official Business");
            waiverAgreed = false;
            dialog.close();
        });

        Button btnAccept = new Button("✓  I Agree");
        btnAccept.setStyle("-fx-background-color: #8B0000; -fx-text-fill: white; " +
                "-fx-font-size: 12px; -fx-padding: 8 20; -fx-background-radius: 20; " +
                "-fx-border-width: 0; -fx-cursor: hand;");
        btnAccept.setDisable(true);
        chkAgree.setOnAction(e -> btnAccept.setDisable(!chkAgree.isSelected()));
        btnAccept.setOnAction(e -> {
            waiverAgreed = true;
            dialog.close();
        });

        footer.getChildren().addAll(btnDecline, btnAccept);
        root.getChildren().addAll(header, txtWaiver, checkWrapper, footer);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    @FXML
    private void handlePickTimeOut() {
        Stage owner = (Stage) btnTimeOut.getScene().getWindow();
        LocalTime picked = TimePickerDialog.show(owner, selectedTimeOut);
        if (picked != null) {
            selectedTimeOut = picked;
            btnTimeOut.setText("🕐  " + picked.format(TIME_FMT));
        }
    }

    @FXML
    private void handlePickTimeIn() {
        Stage owner = (Stage) btnTimeIn.getScene().getWindow();
        LocalTime picked = TimePickerDialog.show(owner, selectedTimeIn);
        if (picked != null) {
            selectedTimeIn = picked;
            btnTimeIn.setText("🕐  " + picked.format(TIME_FMT));
        }
    }

    private void hideSuggestions() {
        lstSuggestions.setVisible(false);
        lstSuggestions.setManaged(false);
    }

    public void setCurrentUserId(int userId) { this.currentUserId = userId; }

    @FXML
    private void handleSubmit() {
        lblError.setText("");

        // Validations
        if (txtEmployee.getText().trim().isEmpty()) {
            lblError.setText("Please enter an employee name."); return;
        }
        if (selectedEmployee == null) {
            selectedEmployee = employees.stream()
                    .filter(e -> e.getName().equalsIgnoreCase(txtEmployee.getText().trim()))
                    .findFirst().orElse(null);
            if (selectedEmployee == null) {
                lblError.setText("Employee not found. Please select from suggestions."); return;
            }
        }
        if (cmbCategory.getValue() == null || cmbCategory.getValue().isBlank()) {
            lblError.setText("Please select a category."); return;
        }
        // Check waiver for Personal Reason or Others
        String category = cmbCategory.getValue();
        if (("Personal Reason".equals(category) || "Others".equals(category)) && !waiverAgreed) {
            lblError.setText("You must agree to the waiver for " + category + " pass slips.");
            showWaiverDialog(category);
            return;
        }
        if (txtPurpose.getText().trim().isEmpty()) {
            lblError.setText("Please enter a purpose."); return;
        }
        if (!selectedTimeIn.isAfter(selectedTimeOut)) {
            lblError.setText("Time In must be after Time Out."); return;
        }

        try {
            PassSlip ps = new PassSlip();
            ps.setEmpId(selectedEmployee.getEmpId());
            ps.setReason(txtPurpose.getText().trim());
            ps.setCategory(category);
            ps.setTimeOut(LocalDateTime.of(LocalDate.now(), selectedTimeOut));
            ps.setTimeIn (LocalDateTime.of(LocalDate.now(), selectedTimeIn));
            ps.setIssuedBy(currentUserId);
            ps.setStatus("Pending");

            if (passSlipDAO.createPassSlip(ps)) {
                activityLogDAO.logActivity(selectedEmployee.getEmpId(),
                        "Pass slip created for " + selectedEmployee.getName() +
                        " [" + category + "]", "Admin");
                showInfo("Pass slip submitted successfully!");
                closeWindow();
            } else {
                lblError.setText("Failed to save. Please try again.");
            }
        } catch (Exception e) {
            lblError.setText("Error: " + e.getMessage());
        }
    }

    @FXML private void handleCancel() { closeWindow(); }
    private void closeWindow() { ((Stage) btnCancel.getScene().getWindow()).close(); }
    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Success"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
