package main.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class StaffResetPasswordSuccessController {

    @FXML private Button okButton;

    @FXML
    public void handleOk(ActionEvent e) {
        Stage stage = (Stage) okButton.getScene().getWindow();
        stage.close();
    }
}