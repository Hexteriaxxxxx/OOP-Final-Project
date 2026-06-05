package main.controllers;

import dao.EmployeeDAO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Employee;

import java.net.URL;
import java.util.ResourceBundle;

public class EditEmployeeController implements Initializable {

    @FXML private TextField tfEmployeeId;
    @FXML private TextField tfFullName;
    @FXML private TextField tfDepartment;
    @FXML private TextField tfPosition;

    private Stage           dialogStage;
    private Employee        employee;
    private Runnable        onSaveCallback;
    private final EmployeeDAO employeeDAO = new EmployeeDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {}

    public void setDialogStage(Stage stage)    { this.dialogStage = stage; }
    public void setOnSaveCallback(Runnable cb) { this.onSaveCallback = cb; }

    public void setEmployee(Employee emp) {
        this.employee = emp;
        tfEmployeeId.setText(String.valueOf(emp.getEmpId()));
        tfEmployeeId.setEditable(false);
        tfFullName  .setText(emp.getName());
        tfDepartment.setText(emp.getDepartment());
        tfPosition  .setText(emp.getPosition());
    }

    @FXML
    private void handleSave() {
        if (!isValid()) return;

        employee.setName      (tfFullName  .getText().trim());
        employee.setDepartment(tfDepartment.getText().trim());
        employee.setPosition  (tfPosition  .getText().trim());

        // Save to Supabase database
        boolean saved = employeeDAO.updateEmployee(employee);
        if (!saved) {
            showAlert(Alert.AlertType.ERROR, "Save Failed",
                    "Hindi nasave ang changes. Please try again.");
            return;
        }

        if (onSaveCallback != null) onSaveCallback.run();
        showAlert(Alert.AlertType.INFORMATION, "Success",
                employee.getName() + " ay na-update na!");
        dialogStage.close();
    }

    @FXML
    private void handleClose() {
        dialogStage.close();
    }

    private boolean isValid() {
        StringBuilder msg = new StringBuilder();
        if (tfFullName  .getText().trim().isEmpty()) msg.append("• Full Name ay required.\n");
        if (tfDepartment.getText().trim().isEmpty()) msg.append("• Department ay required.\n");
        if (tfPosition  .getText().trim().isEmpty()) msg.append("• Position ay required.\n");
        if (msg.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", msg.toString().trim());
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(content); a.showAndWait();
    }
}
