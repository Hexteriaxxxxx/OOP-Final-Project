package main.controllers;

import dao.DepartmentDAO;
import dao.EmployeeDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Employee;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class EditEmployeeController implements Initializable {

    @FXML private TextField        tfEmployeeId;
    @FXML private TextField        tfFullName;
    @FXML private ComboBox<String> cbDepartment;
    @FXML private TextField        tfCustomDept;
    @FXML private ComboBox<String> cbPosition;
    @FXML private TextField        tfCustomPosition;

    private Stage             dialogStage;
    private Employee          employee;
    private Runnable          onSaveCallback;
    private final EmployeeDAO   employeeDAO   = new EmployeeDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();

    private static final List<String> POSITIONS = List.of(
        "Campus Director",
        "Administrative Officer",
        "Faculty Member",
        "Department Head",
        "Registrar",
        "Admission Officer",
        "Librarian",
        "Security Guard",
        "Utility Staff",
        "Canteen Staff",
        "Medical Staff / Nurse",
        "Research Staff",
        "Student Services Officer",
        "IT Staff",
        "HR Officer",
        "Finance Officer",
        "Accounting Staff",
        "Office Staff",
        "── Add new... ──"
    );

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadDepartments();
        cbPosition.setItems(FXCollections.observableArrayList(POSITIONS));
    }

    private void loadDepartments() {
        List<String> depts = departmentDAO.getAllDepartmentNames();
        depts.add("── Add new... ──");
        cbDepartment.setItems(FXCollections.observableArrayList(depts));
    }

    public void setDialogStage(Stage stage)    { this.dialogStage = stage; }
    public void setOnSaveCallback(Runnable cb) { this.onSaveCallback = cb; }

    public void setEmployee(Employee emp) {
        this.employee = emp;
        if (tfEmployeeId != null) {
            tfEmployeeId.setText(String.valueOf(emp.getEmpId()));
            tfEmployeeId.setEditable(false);
        }
        tfFullName.setText(emp.getName());

        // Set department — select existing or fall back to custom
        String dept = emp.getDepartment();
        if (dept != null && cbDepartment.getItems().contains(dept)) {
            cbDepartment.setValue(dept);
        } else if (dept != null && !dept.isEmpty()) {
            cbDepartment.getItems().add(0, dept);
            cbDepartment.setValue(dept);
        }

        // Set position — select existing or fall back to custom
        String pos = emp.getPosition();
        if (pos != null && cbPosition.getItems().contains(pos)) {
            cbPosition.setValue(pos);
        } else if (pos != null && !pos.isEmpty()) {
            cbPosition.getItems().add(0, pos);
            cbPosition.setValue(pos);
        }
    }

    @FXML private void handleDepartmentChange() {
        boolean isCustom = "── Add new... ──".equals(cbDepartment.getValue());
        tfCustomDept.setVisible(isCustom);
        tfCustomDept.setManaged(isCustom);
        if (isCustom) tfCustomDept.requestFocus();
    }

    @FXML private void handlePositionChange() {
        boolean isCustom = "── Add new... ──".equals(cbPosition.getValue());
        tfCustomPosition.setVisible(isCustom);
        tfCustomPosition.setManaged(isCustom);
        if (isCustom) tfCustomPosition.requestFocus();
    }

    @FXML
    private void handleSave() {
        String name     = tfFullName.getText().trim();
        String dept     = getDepartment();
        String position = getPosition();

        if (name.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Full Name is required."); return;
        }
        if (name.matches(".*\\d.*")) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Full Name must not contain numbers."); return;
        }
        if (dept == null || dept.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Department is required."); return;
        }
        if (position == null || position.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Position is required."); return;
        }

        employee.setName      (name);
        employee.setDepartment(dept);
        employee.setPosition  (position);

        boolean saved = employeeDAO.updateEmployee(employee);
        if (!saved) {
            showAlert(Alert.AlertType.ERROR, "Save Failed", "Could not save changes. Please try again.");
            return;
        }

        // Save custom dept to DB
        if ("── Add new... ──".equals(cbDepartment.getValue()) && !tfCustomDept.getText().trim().isEmpty()) {
            departmentDAO.addDepartment(tfCustomDept.getText().trim());
        }

        if (onSaveCallback != null) onSaveCallback.run();
        showAlert(Alert.AlertType.INFORMATION, "Success", employee.getName() + " updated successfully!");
        dialogStage.close();
    }

    private String getDepartment() {
        if ("── Add new... ──".equals(cbDepartment.getValue())) return tfCustomDept.getText().trim();
        return cbDepartment.getValue();
    }

    private String getPosition() {
        if ("── Add new... ──".equals(cbPosition.getValue())) return tfCustomPosition.getText().trim();
        return cbPosition.getValue();
    }

    @FXML private void handleClose() { dialogStage.close(); }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(content); a.showAndWait();
    }
}
