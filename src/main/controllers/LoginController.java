package main.controllers;

import dao.UserDAO;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.User;

import java.io.*;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private Button     staffTab;
    @FXML private Button     adminTab;
    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField     passwordVisible;
    @FXML private Button        btnTogglePassword;
    @FXML private CheckBox      rememberMe;
    @FXML private Label         lblPortalType;
    @FXML private StackPane     rootPane;
    @FXML private VBox          loginCard; // the white card — animated on tab switch

    private String  selectedRole  = "staff";
    private boolean passwordShown = false;

    private static final String PREFS_FILE = "preferences.properties";
    private final UserDAO userDAO = new UserDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setActiveTab("Staff");

        passwordField  .textProperty().addListener((obs,o,n)->{ if(!passwordVisible.getText().equals(n)) passwordVisible.setText(n); });
        passwordVisible.textProperty().addListener((obs,o,n)->{ if(!passwordField  .getText().equals(n)) passwordField  .setText(n); });

        passwordVisible.setVisible(false); passwordVisible.setManaged(false);
        btnTogglePassword.setText("👁");
        loadSavedCredentials();

        // ── Fade-in when login screen appears ────────────────────
        if (rootPane != null) {
            rootPane.setOpacity(0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(450), rootPane);
            fadeIn.setFromValue(0.0); fadeIn.setToValue(1.0); fadeIn.play();
        }
    }

    @FXML public void handleTogglePassword(ActionEvent e) {
        passwordShown = !passwordShown;
        if (passwordShown) {
            passwordVisible.setText(passwordField.getText());
            passwordField.setVisible(false);   passwordField.setManaged(false);
            passwordVisible.setVisible(true);  passwordVisible.setManaged(true);
            btnTogglePassword.setText("🙈");
            passwordVisible.requestFocus();
            passwordVisible.positionCaret(passwordVisible.getText().length());
        } else {
            passwordField.setText(passwordVisible.getText());
            passwordVisible.setVisible(false); passwordVisible.setManaged(false);
            passwordField.setVisible(true);    passwordField.setManaged(true);
            btnTogglePassword.setText("👁");
            passwordField.requestFocus();
            passwordField.positionCaret(passwordField.getText().length());
        }
    }

    @FXML public void handleStaffTab(ActionEvent e) { if (selectedRole.equals("staff")) return; selectedRole = "staff"; animateTabSwitch("Staff"); }
    @FXML public void handleAdminTab(ActionEvent e)  { if (selectedRole.equals("admin")) return; selectedRole = "admin"; animateTabSwitch("Admin"); }

    /** Slide the card slightly and fade it while swapping the tab style. */
    private void animateTabSwitch(String role) {
        if (loginCard == null) { setActiveTab(role); return; }

        // Phase 1: fade-out + slide left
        FadeTransition fadeOut = new FadeTransition(Duration.millis(120), loginCard);
        fadeOut.setFromValue(1.0); fadeOut.setToValue(0.0);
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(120), loginCard);
        slideOut.setFromX(0); slideOut.setToX(role.equals("Admin") ? -18 : 18);
        ParallelTransition out = new ParallelTransition(fadeOut, slideOut);

        out.setOnFinished(ev -> {
            setActiveTab(role);
            loginCard.setTranslateX(role.equals("Admin") ? 18 : -18);
            loginCard.setOpacity(0);

            // Phase 2: fade-in + slide back to center
            FadeTransition fadeIn = new FadeTransition(Duration.millis(160), loginCard);
            fadeIn.setFromValue(0.0); fadeIn.setToValue(1.0);
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(160), loginCard);
            slideIn.setFromX(loginCard.getTranslateX()); slideIn.setToX(0);
            new ParallelTransition(fadeIn, slideIn).play();
        });
        out.play();
    }

    private void setActiveTab(String role) {
        String on  = "-fx-background-color:#8B0000;-fx-text-fill:white;-fx-font-weight:bold;-fx-font-size:13px;-fx-background-radius:6;-fx-padding:8 36;";
        String off = "-fx-background-color:transparent;-fx-text-fill:#888;-fx-font-size:13px;-fx-background-radius:6;-fx-padding:8 36;";
        staffTab.setStyle(role.equals("Staff") ? on : off);
        adminTab.setStyle(role.equals("Admin") ? on : off);
        if (lblPortalType != null)
            lblPortalType.setText(role.equals("Admin") ? "Admin Portal" : "Staff Portal");
    }

    @FXML public void handleLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = passwordShown ? passwordVisible.getText().trim() : passwordField.getText().trim();
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING,"Missing Fields","Please enter your username and password."); return;
        }
        User user = userDAO.login(username, password, selectedRole);
        if (user != null) {
            if (rememberMe.isSelected()) saveCredentials(username, selectedRole); else clearCredentials();
            redirectToDashboard(user);
        } else {
            showAlert(Alert.AlertType.ERROR,"Login Failed","Invalid username or password for " + selectedRole + " account.\nPlease try again.");
            passwordField.clear(); passwordVisible.clear();
        }
    }

    private void redirectToDashboard(User user) {
        try {
            String fxmlPath = selectedRole.equals("admin")
                    ? "/main/resources/fxml/AdminDashboard.fxml"
                    : "/main/resources/fxml/StaffDashboard.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            if (selectedRole.equals("admin")) {
                AdminDashboardController ctrl = loader.getController(); ctrl.setCurrentUser(user);
            } else {
                StaffDashboardController ctrl = loader.getController(); ctrl.setCurrentUser(user);
            }

            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root, 1280, 720);
            stage.setScene(scene);
            stage.setTitle(selectedRole.equals("admin")
                    ? "Pass Slip System - Admin Dashboard"
                    : "Pass Slip System - Staff Dashboard");

            // ── Fade-in transition to dashboard ───────────────────
            root.setOpacity(0);
            stage.show();
            FadeTransition fadeIn = new FadeTransition(Duration.millis(450), root);
            fadeIn.setFromValue(0.0); fadeIn.setToValue(1.0); fadeIn.play();

        } catch (IOException ex) { ex.printStackTrace(); showAlert(Alert.AlertType.ERROR,"Navigation Error","Could not load Dashboard."); }
    }

    private void saveCredentials(String username, String role) {
        Properties props = new Properties();
        props.setProperty("username", username); props.setProperty("role", role); props.setProperty("rememberMe", "true");
        try (FileOutputStream fos = new FileOutputStream(PREFS_FILE)) { props.store(fos, "Login Preferences"); }
        catch (IOException e) { System.out.println("Could not save preferences: " + e.getMessage()); }
    }

    private void clearCredentials() { File f = new File(PREFS_FILE); if (f.exists()) f.delete(); }

    private void loadSavedCredentials() {
        File f = new File(PREFS_FILE); if (!f.exists()) return;
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(f)) {
            props.load(fis);
            if ("true".equals(props.getProperty("rememberMe"))) {
                usernameField.setText(props.getProperty("username", ""));
                rememberMe.setSelected(true);
                selectedRole = props.getProperty("role", "staff");
                setActiveTab(selectedRole.equals("admin") ? "Admin" : "Staff");
            }
        } catch (IOException e) { System.out.println("Could not load preferences: " + e.getMessage()); }
    }

    @FXML public void handleSignUp(ActionEvent e) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/main/resources/fxml/Register.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 720)); stage.setTitle("Register");
            root.setOpacity(0); stage.show();
            FadeTransition ft = new FadeTransition(Duration.millis(350), root);
            ft.setFromValue(0.0); ft.setToValue(1.0); ft.play();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    @FXML public void handleForgotPassword(ActionEvent e) {
        showAlert(Alert.AlertType.INFORMATION,"Forgot Password","Please contact your system administrator to reset your password.");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait();
    }
}
