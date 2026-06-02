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
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ResourceBundle;

public class CreatePassSlipController implements Initializable {

    @FXML private ComboBox<String> cmbEmployee;
    @FXML private TextField txtDepartment;
    @FXML private TextField txtDate;
    @FXML private TextArea txtPurpose;
    @FXML private TextField txtTimeOut;
    @FXML private TextField txtTimeIn;
    @FXML private Label lblError;
    @FXML private Button btnCancel;
    @FXML private Button btnSubmit;

    private final EmployeeDAO employeeDAO       = new EmployeeDAO();
    private final PassSlipDAO passSlipDAO       = new PassSlipDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();

    private List<Employee> employees;
    private int currentUserId = 1; // default, overridden by setCurrentUserId()

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Auto-fill today's date
        txtDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));

        loadEmployees();

        // Auto-fill department when employee is selected
        cmbEmployee.setOnAction(e -> {
            int index = cmbEmployee.getSelectionModel().getSelectedIndex();
            if (index >= 0 && index < employees.size()) {
                txtDepartment.setText(employees.get(index).getDepartment());
            }
        });
    }

    // Call this from AdminDashboardController to pass logged-in user's ID
    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }

    private void loadEmployees() {
        employees = employeeDAO.getAllEmployees();
        cmbEmployee.setItems(FXCollections.observableArrayList(
                employees.stream().map(Employee::getName).toList()
        ));
    }

    @FXML
    private void handleSubmit() {
        lblError.setText("");

        // ── Validation ──
        if (cmbEmployee.getValue() == null || cmbEmployee.getValue().isEmpty()) {
            lblError.setText("Please select an employee.");
            return;
        }
        if (txtPurpose.getText().trim().isEmpty()) {
            lblError.setText("Please enter a purpose.");
            return;
        }
        if (txtTimeOut.getText().trim().isEmpty()) {
            lblError.setText("Please enter a time out (HH:MM).");
            return;
        }

        try {
            // ── Get selected employee ──
            int empIndex = cmbEmployee.getSelectionModel().getSelectedIndex();
            Employee emp = employees.get(empIndex);

            // ── Parse Time Out ──
            LocalTime timeOutTime = LocalTime.parse(
                    txtTimeOut.getText().trim(),
                    DateTimeFormatter.ofPattern("HH:mm")
            );
            LocalDateTime timeOut = LocalDateTime.of(LocalDate.now(), timeOutTime);

            // ── Parse Time In (optional) ──
            LocalDateTime timeIn = null;
            if (!txtTimeIn.getText().trim().isEmpty()) {
                LocalTime timeInTime = LocalTime.parse(
                        txtTimeIn.getText().trim(),
                        DateTimeFormatter.ofPattern("HH:mm")
                );
                timeIn = LocalDateTime.of(LocalDate.now(), timeInTime);
            }

            // ── Build PassSlip object ──
            PassSlip ps = new PassSlip();
            ps.setEmpId(emp.getEmpId());
            ps.setReason(txtPurpose.getText().trim());
            ps.setTimeOut(timeOut);
            ps.setTimeIn(timeIn);
            ps.setIssuedBy(currentUserId);
            ps.setStatus("Pending");

            // ── Save to DB ──
            boolean saved = passSlipDAO.createPassSlip(ps);

            if (saved) {
                activityLogDAO.logActivity(
                        emp.getEmpId(),
                        "Pass slip created for " + emp.getName(),
                        "Admin"
                );
                showInfo("Pass slip submitted successfully!");
                closeWindow();
            } else {
                lblError.setText("Failed to save. Please try again.");
            }

        } catch (DateTimeParseException e) {
            lblError.setText("Invalid time format. Use HH:MM (e.g. 09:30)");
        } catch (Exception e) {
            lblError.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Success");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}