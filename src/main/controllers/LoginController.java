package main.controllers;

import dao.UserDAO;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import main.utils.SessionManager;
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
        if (rootPane != null) {
            rootPane.setOpacity(0);
            rootPane.setTranslateY(22);
            FadeTransition fade = new FadeTransition(Duration.millis(420), rootPane);
            fade.setToValue(1);
            TranslateTransition rise = new TranslateTransition(Duration.millis(420), rootPane);
            rise.setToY(0);
            rise.setInterpolator(Interpolator.EASE_OUT);
            new ParallelTransition(fade, rise).play();
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

    @FXML public void handleStaffTab(ActionEvent e) { selectedRole = "staff"; setActiveTab("Staff"); }
    @FXML public void handleAdminTab(ActionEvent e) { selectedRole = "admin"; setActiveTab("Admin"); }

    private void setActiveTab(String role) {
        String on  = "-fx-background-color:#8B0000;-fx-text-fill:white;-fx-font-weight:bold;-fx-font-size:13px;-fx-background-radius:6;-fx-padding:8 36;";
        String off = "-fx-background-color:transparent;-fx-text-fill:#888;-fx-font-size:13px;-fx-background-radius:6;-fx-padding:8 36;";
        staffTab.setStyle(role.equals("Staff") ? on : off);
        adminTab.setStyle(role.equals("Admin") ? on : off);
        if (lblPortalType != null) lblPortalType.setText(role.equals("Admin") ? "Admin Portal" : "Staff Portal");
    }

    @FXML public void handleLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = passwordShown ? passwordVisible.getText().trim() : passwordField.getText().trim();
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING,"Missing Fields","Please enter your username and password."); return;
        }
        User user = userDAO.login(username, password, selectedRole);
        if (user != null) {
            if (rememberMe.isSelected()) saveCredentials(username, password, selectedRole); else clearCredentials();
            // ── SET GLOBAL SESSION — persists across ALL panels ──
            SessionManager.setCurrentUser(user);
            redirectToDashboard(user);
        } else {
            showAlert(Alert.AlertType.ERROR,"Login Failed","Invalid username or password for " + selectedRole + " account.\nPlease try again.");
            passwordField.clear(); passwordVisible.clear();
        }
    }

    private void redirectToDashboard(User user) {
        if (rootPane == null) return;
        Stage stage = (Stage) usernameField.getScene().getWindow();
        boolean wasFullscreen = stage.isFullScreen();
        boolean wasMaximized  = stage.isMaximized();
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), rootPane);
        fadeOut.setToValue(0);
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), rootPane);
        scaleOut.setToX(0.95); scaleOut.setToY(0.95);
        scaleOut.setInterpolator(Interpolator.EASE_IN);
        ParallelTransition out = new ParallelTransition(fadeOut, scaleOut);
        out.setOnFinished(ev -> {
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
                root.setOpacity(0);
                root.setScaleX(0.97); root.setScaleY(0.97);
                Scene dashScene = new Scene(root, 1280, 720);
                dashScene.setFill(Color.web("#0f0505"));
                stage.setScene(dashScene);
                stage.setTitle(selectedRole.equals("admin")
                        ? "Pass Slip System - Admin Dashboard"
                        : "Pass Slip System - Staff Dashboard");
                stage.show();
                if (wasFullscreen) Platform.runLater(() -> stage.setFullScreen(true));
                else if (wasMaximized) Platform.runLater(() -> stage.setMaximized(true));
                FadeTransition fadeIn = new FadeTransition(Duration.millis(420), root);
                fadeIn.setToValue(1);
                ScaleTransition scaleIn = new ScaleTransition(Duration.millis(420), root);
                scaleIn.setToX(1); scaleIn.setToY(1);
                scaleIn.setInterpolator(Interpolator.EASE_OUT);
                new ParallelTransition(fadeIn, scaleIn).play();
            } catch (IOException ex) { ex.printStackTrace(); showAlert(Alert.AlertType.ERROR,"Navigation Error","Could not load Dashboard."); }
        });
        out.play();
    }

    private void saveCredentials(String username, String password, String role) {
        Properties props = new Properties();
        props.setProperty("username", username);
        props.setProperty("password", password);
        props.setProperty("role", role);
        props.setProperty("rememberMe", "true");
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
                String savedUser = props.getProperty("username", "");
                String savedPass = props.getProperty("password", "");
                usernameField.setText(savedUser);
                passwordField.setText(savedPass);
                passwordVisible.setText(savedPass);
                rememberMe.setSelected(true);
                selectedRole = props.getProperty("role", "staff");
                setActiveTab(selectedRole.equals("admin") ? "Admin" : "Staff");
            }
        } catch (IOException e) { System.out.println("Could not load preferences: " + e.getMessage()); }
    }

    @FXML public void handleSignUp(ActionEvent e) {
        if (rootPane == null) return;
        Stage stage = (Stage) usernameField.getScene().getWindow();
        boolean wasFullscreen = stage.isFullScreen();
        boolean wasMaximized  = stage.isMaximized();
        FadeTransition fadeOut = new FadeTransition(Duration.millis(160), rootPane);
        fadeOut.setToValue(0);
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(160), rootPane);
        slideOut.setToX(-32);
        slideOut.setInterpolator(Interpolator.EASE_IN);
        ParallelTransition out = new ParallelTransition(fadeOut, slideOut);
        out.setOnFinished(ev -> {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/main/resources/fxml/Register.fxml"));
                root.setOpacity(0);
                root.setTranslateX(40);
                Scene regScene = new Scene(root, 1280, 720);
                regScene.setFill(Color.web("#0f0505"));
                stage.setScene(regScene);
                stage.setTitle("Register");
                stage.show();
                if (wasFullscreen) Platform.runLater(() -> stage.setFullScreen(true));
                else if (wasMaximized) Platform.runLater(() -> stage.setMaximized(true));
                FadeTransition fadeIn = new FadeTransition(Duration.millis(360), root);
                fadeIn.setToValue(1);
                TranslateTransition slideIn = new TranslateTransition(Duration.millis(360), root);
                slideIn.setToX(0);
                slideIn.setInterpolator(Interpolator.EASE_OUT);
                new ParallelTransition(fadeIn, slideIn).play();
            } catch (Exception ex) { ex.printStackTrace(); }
        });
        out.play();
    }

    @FXML public void handleForgotPassword(ActionEvent e) {
        showAlert(Alert.AlertType.INFORMATION,"Forgot Password","Please contact your system administrator to reset your password.");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait();
    }
}