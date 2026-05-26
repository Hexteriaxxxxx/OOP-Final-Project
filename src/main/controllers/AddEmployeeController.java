package main.controllers;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Employee;

import java.net.URL;
import java.util.ResourceBundle;

public class AddEmployeeController implements Initializable {

    @FXML private TextField tfEmployeeId;
    @FXML private TextField tfFullName;
    @FXML private TextField tfDepartment;
    @FXML private TextField tfPosition;

    private Stage                    dialogStage;
    private ObservableList<Employee> masterList;
    private Runnable                 onSaveCallback;

    @Override
    public void initialize(URL url, ResourceBundle rb) {}

    public void setDialogStage(Stage stage)                  { this.dialogStage = stage; }
    public void setMasterList(ObservableList<Employee> list) { this.masterList  = list; }
    public void setOnSaveCallback(Runnable cb)               { this.onSaveCallback = cb; }

    @FXML
    private void handleAddEmployee() {
        if (!isValid()) return;

        int id;
        try {
            id = Integer.parseInt(tfEmployeeId.getText().trim());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Invalid ID",
                    "Ang Employee ID ay dapat numero lamang.");
            return;
        }

        boolean duplicate = masterList.stream().anyMatch(e -> e.getEmpId() == id);
        if (duplicate) {
            showAlert(Alert.AlertType.WARNING, "Duplicate ID",
                    "Ang Employee ID " + id + " ay mayroon na.");
            return;
        }

        masterList.add(new Employee(
                id,
                tfFullName.getText().trim(),
                tfDepartment.getText().trim(),
                tfPosition.getText().trim()
        ));

        if (onSaveCallback != null) onSaveCallback.run();
        dialogStage.close();
    }

    @FXML
    private void handleClose() {
        dialogStage.close();
    }

    private boolean isValid() {
        StringBuilder msg = new StringBuilder();
        if (tfEmployeeId.getText().trim().isEmpty())  msg.append("• Employee ID ay required.\n");
        if (tfFullName.getText().trim().isEmpty())    msg.append("• Full Name ay required.\n");
        if (tfDepartment.getText().trim().isEmpty())  msg.append("• Department ay required.\n");
        if (tfPosition.getText().trim().isEmpty())    msg.append("• Position ay required.\n");

        if (msg.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", msg.toString().trim());
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}