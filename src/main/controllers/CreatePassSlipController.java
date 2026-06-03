package main.controllers;

import dao.ActivityLogDAO;
import dao.EmployeeDAO;
import dao.PassSlipDAO;
import models.Employee;
import models.PassSlip;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

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
    @FXML private TextField        txtDate;
    @FXML private TextArea         txtPurpose;
    @FXML private Button           btnTimeOut;
    @FXML private Button           btnTimeIn;
    @FXML private Label            lblError;
    @FXML private Button           btnCancel;
    @FXML private Button           btnSubmit;

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("hh:mm a");

    private final EmployeeDAO    employeeDAO    = new EmployeeDAO();
    private final PassSlipDAO    passSlipDAO    = new PassSlipDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();

    private List<Employee> employees;
    private Employee       selectedEmployee = null;
    private int            currentUserId    = 1;

    private LocalTime selectedTimeOut = LocalTime.of(8, 0);
    private LocalTime selectedTimeIn  = LocalTime.of(17, 0);

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        txtDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        employees = employeeDAO.getAllEmployees();
        lstSuggestions.setVisible(false);
        lstSuggestions.setManaged(false);

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

        if (txtEmployee.getText().trim().isEmpty()) { lblError.setText("Please enter an employee name."); return; }
        if (selectedEmployee == null) {
            selectedEmployee = employees.stream()
                    .filter(e -> e.getName().equalsIgnoreCase(txtEmployee.getText().trim()))
                    .findFirst().orElse(null);
            if (selectedEmployee == null) { lblError.setText("Employee not found. Please select from suggestions."); return; }
        }
        if (txtPurpose.getText().trim().isEmpty()) { lblError.setText("Please enter a purpose."); return; }
        if (!selectedTimeIn.isAfter(selectedTimeOut)) { lblError.setText("Time In must be after Time Out."); return; }

        try {
            PassSlip ps = new PassSlip();
            ps.setEmpId(selectedEmployee.getEmpId());
            ps.setReason(txtPurpose.getText().trim());
            ps.setTimeOut(LocalDateTime.of(LocalDate.now(), selectedTimeOut));
            ps.setTimeIn (LocalDateTime.of(LocalDate.now(), selectedTimeIn));
            ps.setIssuedBy(currentUserId);
            ps.setStatus("Pending");

            if (passSlipDAO.createPassSlip(ps)) {
                activityLogDAO.logActivity(selectedEmployee.getEmpId(),
                        "Pass slip created for " + selectedEmployee.getName(), "Admin");
                showInfo("Pass slip submitted successfully!");
                closeWindow();
            } else { lblError.setText("Failed to save. Please try again."); }
        } catch (Exception e) { lblError.setText("Error: " + e.getMessage()); }
    }

    @FXML private void handleCancel() { closeWindow(); }
    private void closeWindow() { ((Stage) btnCancel.getScene().getWindow()).close(); }
    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Success"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
