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

import java.io.*;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private Button        staffTab;
    @FXML private Button        adminTab;
    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField     passwordVisible;
    @FXML private Button        btnTogglePassword;
    @FXML private CheckBox      rememberMe;
    @FXML private Label         lblPortalType;   // ← dynamic portal label

    private String  selectedRole  = "staff";
    private boolean passwordShown = false;

    private static final String PREFS_FILE = "preferences.properties";
    private final UserDAO userDAO = new UserDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setActiveTab("Staff");

        passwordField  .textProperty().addListener((obs,o,n)->{ if(!passwordVisible.getText().equals(n)) passwordVisible.setText(n); });
        passwordVisible.textProperty().addListener((obs,o,n)->{ if(!passwordField  .getText().equals(n)) passwordField  .setText(n); });

        passwordVisible.setVisible(false);
        passwordVisible.setManaged(false);
        btnTogglePassword.setText("👁");

        loadSavedCredentials();
    }

    @FXML
    public void handleTogglePassword(ActionEvent event) {
        passwordShown = !passwordShown;
        if (passwordShown) {
            passwordVisible.setText(passwordField.getText());
            passwordField  .setVisible(false); passwordField  .setManaged(false);
            passwordVisible.setVisible(true);  passwordVisible.setManaged(true);
            btnTogglePassword.setText("🙈");
            passwordVisible.requestFocus();
            passwordVisible.positionCaret(passwordVisible.getText().length());
        } else {
            passwordField.setText(passwordVisible.getText());
            passwordVisible.setVisible(false); passwordVisible.setManaged(false);
            passwordField  .setVisible(true);  passwordField  .setManaged(true);
            btnTogglePassword.setText("👁");
            passwordField.requestFocus();
            passwordField.positionCaret(passwordField.getText().length());
        }
    }

    @FXML public void handleStaffTab(ActionEvent event) { selectedRole = "staff"; setActiveTab("Staff"); }
    @FXML public void handleAdminTab(ActionEvent event) { selectedRole = "admin"; setActiveTab("Admin"); }

    private void setActiveTab(String role) {
        String on  = "-fx-background-color:#8B0000;-fx-text-fill:white;-fx-font-weight:bold;-fx-font-size:13px;-fx-background-radius:6;-fx-padding:8 36;";
        String off = "-fx-background-color:transparent;-fx-text-fill:#888;-fx-font-size:13px;-fx-background-radius:6;-fx-padding:8 36;";
        staffTab.setStyle(role.equals("Staff") ? on : off);
        adminTab.setStyle(role.equals("Admin") ? on : off);
        // ← Update the portal label dynamically
        if (lblPortalType != null)
            lblPortalType.setText(role.equals("Admin") ? "Admin Portal" : "Staff Portal");
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordShown ? passwordVisible.getText().trim() : passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING,"Missing Fields","Please enter your username and password.");
            return;
        }

        User user = userDAO.login(username, password, selectedRole);
        if (user != null) {
            if (rememberMe.isSelected()) saveCredentials(username, selectedRole);
            else                         clearCredentials();
            redirectToDashboard(event, user);
        } else {
            showAlert(Alert.AlertType.ERROR,"Login Failed",
                    "Invalid username or password for " + selectedRole + " account.\nPlease try again.");
            passwordField.clear(); passwordVisible.clear();
        }
    }

    private void saveCredentials(String username, String role) {
        Properties props = new Properties();
        props.setProperty("username",   username);
        props.setProperty("role",       role);
        props.setProperty("rememberMe", "true");
        try (FileOutputStream fos = new FileOutputStream(PREFS_FILE)) {
            props.store(fos, "Login Preferences");
        } catch (IOException e) { System.out.println("Could not save preferences: " + e.getMessage()); }
    }

    private void clearCredentials() {
        File f = new File(PREFS_FILE); if (f.exists()) f.delete();
    }

    private void loadSavedCredentials() {
        File f = new File(PREFS_FILE); if (!f.exists()) return;
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(f)) {
            props.load(fis);
            if ("true".equals(props.getProperty("rememberMe"))) {
                String savedUser = props.getProperty("username", "");
                String savedRole = props.getProperty("role",     "staff");
                usernameField.setText(savedUser);
                rememberMe.setSelected(true);
                selectedRole = savedRole;
                setActiveTab(savedRole.equals("admin") ? "Admin" : "Staff");
            }
        } catch (IOException e) { System.out.println("Could not load preferences: " + e.getMessage()); }
    }

    private void redirectToDashboard(ActionEvent event, User user) {
        try {
            String fxmlPath = selectedRole.equals("admin")
                    ? "/main/resources/fxml/AdminDashboard.fxml"
                    : "/main/resources/fxml/StaffDashboard.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            if (selectedRole.equals("admin")) {
                AdminDashboardController ctrl = loader.getController();
                ctrl.setCurrentUser(user);
            } else {
                StaffDashboardController ctrl = loader.getController();
                ctrl.setCurrentUser(user);
            }

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 720));
            stage.setTitle(selectedRole.equals("admin")
                    ? "Pass Slip System - Admin Dashboard"
                    : "Pass Slip System - Staff Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR,"Navigation Error","Could not load Dashboard.");
        }
    }

    @FXML public void handleSignUp(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/main/resources/fxml/Register.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 720));
            stage.setTitle("Register");
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML public void handleForgotPassword(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION,"Forgot Password","Please contact your system administrator to reset your password.");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message);
        alert.showAndWait();
    }
}
