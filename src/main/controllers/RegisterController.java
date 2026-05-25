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

    @FXML private Button staffTab;
    @FXML private Button adminTab;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private CheckBox termsCheckBox;

    private String selectedRole = "Admin";

    private final UserDAO userDAO = new UserDAO();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setActiveTab("Admin");
    }

    // ─── Tab Toggle ───────────────────────────────────────────────

    @FXML
    public void handleStaffTab(ActionEvent event) {
        selectedRole = "Staff";
        setActiveTab("Staff");
    }

    @FXML
    public void handleAdminTab(ActionEvent event) {
        selectedRole = "Admin";
        setActiveTab("Admin");
    }

    private void setActiveTab(String role) {
        String activeStyle = "-fx-background-color: #800000; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 13px; " +
                "-fx-background-radius: 6; -fx-padding: 7 32;";
        String inactiveStyle = "-fx-background-color: transparent; -fx-text-fill: #888; " +
                "-fx-font-size: 13px; -fx-background-radius: 6; -fx-padding: 7 32;";

        if (role.equals("Staff")) {
            staffTab.setStyle(activeStyle);
            adminTab.setStyle(inactiveStyle);
        } else {
            adminTab.setStyle(activeStyle);
            staffTab.setStyle(inactiveStyle);
        }
    }

    // ─── Register ─────────────────────────────────────────────────

    @FXML
    public void handleRegister(ActionEvent event) {
        String fullName = fullNameField.getText().trim();
        String email    = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirm  = confirmPasswordField.getText().trim();

        // 1. Empty fields check
        if (fullName.isEmpty() || email.isEmpty() || username.isEmpty()
                || password.isEmpty() || confirm.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Fields",
                    "Please fill in all fields before registering.");
            return;
        }

        // 2. Email format check
        if (!email.contains("@") || !email.contains(".")) {
            showAlert(Alert.AlertType.WARNING, "Invalid Email",
                    "Please enter a valid email address.");
            return;
        }

        // 3. Password match check
        if (!password.equals(confirm)) {
            showAlert(Alert.AlertType.WARNING, "Password Mismatch",
                    "Passwords do not match. Please try again.");
            confirmPasswordField.clear();
            return;
        }

        // 4. Password length check
        if (password.length() < 6) {
            showAlert(Alert.AlertType.WARNING, "Weak Password",
                    "Password must be at least 6 characters long.");
            return;
        }

        // 5. Terms and Conditions check
        if (!termsCheckBox.isSelected()) {
            showAlert(Alert.AlertType.WARNING, "Terms Not Accepted",
                    "Please agree to the Terms and Conditions to proceed.");
            return;
        }

        // 6. Username exists check
        if (userDAO.usernameExists(username)) {
            showAlert(Alert.AlertType.WARNING, "Username Taken",
                    "The username \"" + username + "\" is already taken.\nPlease choose a different username.");
            usernameField.clear();
            return;
        }

        // 7. Register in database
        boolean success = userDAO.register(fullName, email, username, password, selectedRole);

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Registration Successful",
                    "Account created successfully!\nYou can now log in with your credentials.");
            navigateToLogin(event);
        } else {
            showAlert(Alert.AlertType.ERROR, "Registration Failed",
                    "Something went wrong. Please try again later.");
        }
    }

    // ─── Back to Login ────────────────────────────────────────────

    @FXML
    public void handleBackToLogin(ActionEvent event) {
        navigateToLogin(event);
    }

    private void navigateToLogin(ActionEvent event) {
        try {
            URL loginUrl = getClass().getClassLoader().getResource("fxml/Login.fxml");
            Parent root = FXMLLoader.load(loginUrl);
            Stage stage = (Stage) fullNameField.getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 720));
            stage.setTitle("Pass Slip Issuance System");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Could not load Login page.");
        }
    }

    // ─── Alert Helper ─────────────────────────────────────────────

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
