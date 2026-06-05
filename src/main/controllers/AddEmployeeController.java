package main.controllers;

import dao.EmployeeDAO;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Employee;

import java.net.URL;
import java.util.ResourceBundle;

public class AddEmployeeController implements Initializable {

    @FXML private TextField tfFullName;
    @FXML private TextField tfDepartment;
    @FXML private TextField tfPosition;

    private Stage                    dialogStage;
    private ObservableList<Employee> masterList;
    private Runnable                 onSaveCallback;
    private final EmployeeDAO        employeeDAO = new EmployeeDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {}

    public void setDialogStage(Stage stage)                  { this.dialogStage = stage; }
    public void setMasterList(ObservableList<Employee> list) { this.masterList  = list; }
    public void setOnSaveCallback(Runnable cb)               { this.onSaveCallback = cb; }

    @FXML
    private void handleAddEmployee() {
        if (!isValid()) return;

        Employee emp = new Employee();
        emp.setName      (tfFullName  .getText().trim());
        emp.setDepartment(tfDepartment.getText().trim());
        emp.setPosition  (tfPosition  .getText().trim());

        // Save to Supabase database
        boolean saved = employeeDAO.addEmployee(emp);
        if (!saved) {
            showAlert(Alert.AlertType.ERROR, "Save Failed",
                    "Hindi nasave ang employee. Please try again.");
            return;
        }

        // Reload fresh list from DB
        masterList.setAll(employeeDAO.getAllEmployees());

        if (onSaveCallback != null) onSaveCallback.run();
        showAlert(Alert.AlertType.INFORMATION, "Success",
                emp.getName() + " ay naadded na!");
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
