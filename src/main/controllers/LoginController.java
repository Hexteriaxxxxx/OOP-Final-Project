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
import models.User;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private Button staffTab;
    @FXML private Button adminTab;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMe;

    // Currently selected role
    private String selectedRole = "Staff";

    private final UserDAO userDAO = new UserDAO();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Staff tab active by default
        setActiveTab("Staff");
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
        String activeStyle = "-fx-background-color: #8B0000; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 13px; " +
                "-fx-background-radius: 6; -fx-padding: 8 36;";

        String inactiveStyle = "-fx-background-color: transparent; -fx-text-fill: #888; " +
                "-fx-font-size: 13px; -fx-background-radius: 6; -fx-padding: 8 36;";

        if (role.equals("Staff")) {
            staffTab.setStyle(activeStyle);
            adminTab.setStyle(inactiveStyle);
        } else {
            adminTab.setStyle(activeStyle);
            staffTab.setStyle(inactiveStyle);
        }
    }

    // ─── Login ────────────────────────────────────────────────────

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // Validation
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Fields",
                    "Please enter your username and password.");
            return;
        }

        // Attempt login via DAO
        User user = userDAO.login(username, password, selectedRole);

        if (user != null) {
            showAlert(Alert.AlertType.INFORMATION, "Login Successful",
                    "Welcome, " + user.getUsername() + "!");
            redirectToDashboard(event, user);
        } else {
            showAlert(Alert.AlertType.ERROR, "Login Failed",
                    "Invalid username or password for " + selectedRole + " account.\nPlease try again.");
            passwordField.clear();
        }
    }

    // ─── Redirect to Dashboard ────────────────────────────────────

    private void redirectToDashboard(ActionEvent event, User user) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/Dashboard.fxml"));
            Parent root = loader.load();

            // Pass user to DashboardController if needed
            // DashboardController dc = loader.getController();
            // dc.setUser(user);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Employee Pass Slip System - Dashboard");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Could not load Dashboard. Please contact support.");
        }
    }

    // ─── Sign Up ──────────────────────────────────────────────────

    @FXML
    public void handleSignUp(ActionEvent event) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/Register.fxml"
                            )
                    );

            Parent root = loader.load();

            Stage stage =
                    (Stage) usernameField
                            .getScene()
                            .getWindow();

            stage.setScene(new Scene(root));

            stage.setTitle("Register");

            stage.show();

        } catch (Exception e) {

            System.out.println("ERROR LOADING REGISTER:");
            e.printStackTrace();
        }
    }
    // ─── Forgot Password ──────────────────────────────────────────

    @FXML
    public void handleForgotPassword(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Forgot Password",
                "Please contact your system administrator to reset your password.");
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
