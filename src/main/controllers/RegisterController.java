package main.controllers;

import dao.UserDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class RegisterController implements Initializable {

    @FXML private Button        staffTab, adminTab;
    @FXML private TextField     fullNameField, emailField, usernameField;
    @FXML private PasswordField passwordField, confirmPasswordField;
    @FXML private TextField     passwordVisible, confirmPasswordVisible;
    @FXML private Button        btnTogglePassword, btnToggleConfirm;
    @FXML private CheckBox      termsCheckBox;

    private boolean showPassword = false;
    private boolean showConfirm  = false;
    private String  selectedRole = "admin";
    private final UserDAO userDAO = new UserDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setActiveTab("Admin");

        // Sync visible <-> hidden fields
        passwordVisible.textProperty().bindBidirectional(passwordField.textProperty());
        confirmPasswordVisible.textProperty().bindBidirectional(confirmPasswordField.textProperty());
    }

    // ── Tab Toggle ───────────────────────────────────────────────
    @FXML public void handleStaffTab(ActionEvent e) { selectedRole = "staff"; setActiveTab("Staff"); }
    @FXML public void handleAdminTab(ActionEvent e) { selectedRole = "admin"; setActiveTab("Admin"); }

    private void setActiveTab(String role) {
        String on  = "-fx-background-color: #800000; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 6; -fx-padding: 7 32;";
        String off = "-fx-background-color: transparent; -fx-text-fill: #888; -fx-font-size: 13px; -fx-background-radius: 6; -fx-padding: 7 32;";
        staffTab.setStyle(role.equals("Staff") ? on : off);
        adminTab.setStyle(role.equals("Admin") ? on : off);
    }

    // ── Eye Button — Password ────────────────────────────────────
    @FXML
    public void handleTogglePassword(ActionEvent e) {
        showPassword = !showPassword;
        passwordField  .setVisible(!showPassword); passwordField  .setManaged(!showPassword);
        passwordVisible.setVisible( showPassword); passwordVisible.setManaged( showPassword);
        btnTogglePassword.setText(showPassword ? "🙈" : "👁");
    }

    // ── Eye Button — Confirm Password ────────────────────────────
    @FXML
    public void handleToggleConfirm(ActionEvent e) {
        showConfirm = !showConfirm;
        confirmPasswordField  .setVisible(!showConfirm); confirmPasswordField  .setManaged(!showConfirm);
        confirmPasswordVisible.setVisible( showConfirm); confirmPasswordVisible.setManaged( showConfirm);
        btnToggleConfirm.setText(showConfirm ? "🙈" : "👁");
    }

    // ── Register ─────────────────────────────────────────────────
    @FXML
    public void handleRegister(ActionEvent e) {
        String fullName = fullNameField.getText().trim();
        String email    = emailField   .getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirm  = confirmPasswordField.getText().trim();

        if (fullName.isEmpty()||email.isEmpty()||username.isEmpty()||password.isEmpty()||confirm.isEmpty()) {
            showAlert(Alert.AlertType.WARNING,"Missing Fields","Please fill in all fields."); return;
        }
        if (!email.contains("@")||!email.contains(".")) {
            showAlert(Alert.AlertType.WARNING,"Invalid Email","Please enter a valid email address."); return;
        }
        if (!password.equals(confirm)) {
            showAlert(Alert.AlertType.WARNING,"Password Mismatch","Passwords do not match.");
            confirmPasswordField.clear(); confirmPasswordVisible.clear(); return;
        }
        if (password.length() < 6) {
            showAlert(Alert.AlertType.WARNING,"Weak Password","Password must be at least 6 characters."); return;
        }
        if (!termsCheckBox.isSelected()) {
            showAlert(Alert.AlertType.WARNING,"Terms Not Accepted","Please agree to the Terms and Conditions."); return;
        }
        if (userDAO.usernameExists(username)) {
            showAlert(Alert.AlertType.WARNING,"Username Taken","Username \""+username+"\" is already taken.");
            usernameField.clear(); return;
        }

        if (userDAO.register(fullName, email, username, password, selectedRole)) {
            showAlert(Alert.AlertType.INFORMATION,"Registration Successful","Account created! You can now log in.");
            navigateToLogin(e);
        } else {
            showAlert(Alert.AlertType.ERROR,"Registration Failed","Something went wrong. Please try again.");
        }
    }

    // ── Navigation ───────────────────────────────────────────────
    @FXML public void handleBackToLogin(ActionEvent e) { navigateToLogin(e); }

    private void navigateToLogin(ActionEvent e) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/main/resources/fxml/Login.fxml"));
            Stage stage = (Stage) fullNameField.getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 720));
            stage.setTitle("Pass Slip Issuance System");
            stage.show();
        } catch (IOException ex) {
            showAlert(Alert.AlertType.ERROR,"Navigation Error","Could not load Login page.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
