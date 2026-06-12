package main.controllers;

import dao.DepartmentDAO;
import dao.EmployeeDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Employee;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AddEmployeeController implements Initializable {

    @FXML private TextField        tfFullName;
    @FXML private ComboBox<String> cbDepartment;
    @FXML private TextField        tfCustomDept;
    @FXML private ComboBox<String> cbPosition;
    @FXML private TextField        tfCustomPosition;

    private Stage                    dialogStage;
    private ObservableList<Employee> masterList;
    private Runnable                 onSaveCallback;
    private final EmployeeDAO        employeeDAO    = new EmployeeDAO();
    private final DepartmentDAO      departmentDAO  = new DepartmentDAO();

    // Predefined positions — admin can always type custom via "Add new..."
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
        loadPositions();
    }

    private void loadDepartments() {
        List<String> depts = departmentDAO.getAllDepartmentNames();
        depts.add("── Add new... ──");
        cbDepartment.setItems(FXCollections.observableArrayList(depts));
    }

    private void loadPositions() {
        cbPosition.setItems(FXCollections.observableArrayList(POSITIONS));
    }

    @FXML private void handleDepartmentChange() {
        String selected = cbDepartment.getValue();
        boolean isCustom = "── Add new... ──".equals(selected);
        tfCustomDept.setVisible(isCustom);
        tfCustomDept.setManaged(isCustom);
        if (isCustom) tfCustomDept.requestFocus();
    }

    @FXML private void handlePositionChange() {
        String selected = cbPosition.getValue();
        boolean isCustom = "── Add new... ──".equals(selected);
        tfCustomPosition.setVisible(isCustom);
        tfCustomPosition.setManaged(isCustom);
        if (isCustom) tfCustomPosition.requestFocus();
    }

    public void setDialogStage(Stage stage)                  { this.dialogStage = stage; }
    public void setMasterList(ObservableList<Employee> list) { this.masterList  = list; }
    public void setOnSaveCallback(Runnable cb)               { this.onSaveCallback = cb; }

    @FXML
    private void handleAddEmployee() {
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

        Employee emp = new Employee();
        emp.setName      (name);
        emp.setDepartment(dept);
        emp.setPosition  (position);

        boolean saved = employeeDAO.addEmployee(emp);
        if (!saved) {
            showAlert(Alert.AlertType.ERROR, "Save Failed", "Could not save employee. Please try again.");
            return;
        }

        // If custom dept was entered — save to Department table too
        if ("── Add new... ──".equals(cbDepartment.getValue()) && !tfCustomDept.getText().trim().isEmpty()) {
            departmentDAO.addDepartment(tfCustomDept.getText().trim());
        }

        masterList.setAll(employeeDAO.getAllEmployees());
        if (onSaveCallback != null) onSaveCallback.run();
        showAlert(Alert.AlertType.INFORMATION, "Success", emp.getName() + " added successfully!");
        dialogStage.close();
    }

    private String getDepartment() {
        if ("── Add new... ──".equals(cbDepartment.getValue())) {
            return tfCustomDept.getText().trim();
        }
        return cbDepartment.getValue();
    }

    private String getPosition() {
        if ("── Add new... ──".equals(cbPosition.getValue())) {
            return tfCustomPosition.getText().trim();
        }
        return cbPosition.getValue();
    }

    @FXML private void handleClose() { dialogStage.close(); }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(content); a.showAndWait();
    }
}
