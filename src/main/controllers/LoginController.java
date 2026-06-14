package main.controllers;

import dao.UserDAO;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import main.utils.SessionManager;
import models.User;

import java.io.*;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    // ── Root & Panels ──────────────────────────────────────────────────────────
    @FXML private HBox      rootPane;
    @FXML private StackPane staffPanel,       adminPanel;
    @FXML private VBox      staffFormWrapper, adminFormWrapper;
    @FXML private VBox      staffInactiveMsg, adminInactiveMsg;
    @FXML private VBox      adminBranding;

    // ── Staff form fields ──────────────────────────────────────────────────────
    @FXML private TextField     staffUsernameField;
    @FXML private PasswordField staffPasswordField;
    @FXML private TextField     staffPasswordVisible;
    @FXML private Button        staffBtnToggle;
    @FXML private CheckBox      staffRememberMe;

    // ── Admin form fields ──────────────────────────────────────────────────────
    @FXML private TextField     adminUsernameField;
    @FXML private PasswordField adminPasswordField;
    @FXML private TextField     adminPasswordVisible;
    @FXML private Button        adminBtnToggle;
    @FXML private CheckBox      adminRememberMe;

    // ── State ──────────────────────────────────────────────────────────────────
    private boolean staffPasswordShown = false;
    private boolean adminPasswordShown = false;
    private boolean isStaffActive      = true;
    private boolean switching          = false;

    private static final String PANEL_ACTIVE   =
            "-fx-background-color: linear-gradient(to bottom right, #7a0000, #8B0000, #960000);";
    private static final String PANEL_INACTIVE =
            "-fx-background-color: linear-gradient(to bottom right, #3d0000, #540000, #4a0000); -fx-cursor: hand;";

    private static final String PREFS_FILE = "preferences.properties";
    private final UserDAO userDAO = new UserDAO();

    // ── Initialise ─────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Sync password fields (both panels)
        staffPasswordField  .textProperty().addListener((o, ov, nv) -> { if (!staffPasswordVisible.getText().equals(nv)) staffPasswordVisible.setText(nv); });
        staffPasswordVisible.textProperty().addListener((o, ov, nv) -> { if (!staffPasswordField  .getText().equals(nv)) staffPasswordField  .setText(nv); });
        adminPasswordField  .textProperty().addListener((o, ov, nv) -> { if (!adminPasswordVisible.getText().equals(nv)) adminPasswordVisible.setText(nv); });
        adminPasswordVisible.textProperty().addListener((o, ov, nv) -> { if (!adminPasswordField  .getText().equals(nv)) adminPasswordField  .setText(nv); });

        // Entrance animation
        if (rootPane != null) {
            rootPane.setOpacity(0);
            rootPane.setTranslateY(22);
            FadeTransition     fade = new FadeTransition(Duration.millis(420), rootPane);  fade.setToValue(1);
            TranslateTransition rise = new TranslateTransition(Duration.millis(420), rootPane); rise.setToY(0); rise.setInterpolator(Interpolator.EASE_OUT);
            new ParallelTransition(fade, rise).play();
        }

        loadSavedCredentials();
    }

    // ── Panel click handlers ───────────────────────────────────────────────────
    @FXML public void handleStaffPanelClick() { if (!isStaffActive && !switching) switchToStaff(); }
    @FXML public void handleAdminPanelClick() { if ( isStaffActive && !switching) switchToAdmin(); }

    // ── Panel switch: Staff → Admin ────────────────────────────────────────────
    private void switchToAdmin() {
        switching = true;

        // 1. Fade out staff form (160ms)
        FadeTransition fadeOutForm = new FadeTransition(Duration.millis(160), staffFormWrapper);
        fadeOutForm.setToValue(0);
        fadeOutForm.setOnFinished(e -> {
            // 2. Swap staff panel state
            staffFormWrapper.setVisible(false); staffFormWrapper.setManaged(false);
            staffInactiveMsg.setVisible(true);  staffInactiveMsg.setManaged(true);
            staffInactiveMsg.setOpacity(0);
            staffPanel.setStyle(PANEL_INACTIVE + " -fx-cursor: hand;");

            // 3. Swap admin panel state
            adminPanel.setStyle(PANEL_ACTIVE + " -fx-cursor: default;");
            adminInactiveMsg.setVisible(false); adminInactiveMsg.setManaged(false);
            adminFormWrapper.setVisible(true);  adminFormWrapper.setManaged(true);
            adminFormWrapper.setOpacity(0);
            adminFormWrapper.setTranslateY(22);

            // Brighten admin branding
            FadeTransition brightenBranding = new FadeTransition(Duration.millis(300), adminBranding);
            brightenBranding.setToValue(1.0);

            // 4. Animate in: admin form slides up + staff hint fades in
            FadeTransition     fadeInForm = new FadeTransition(Duration.millis(300), adminFormWrapper);   fadeInForm.setToValue(1);
            TranslateTransition slideUp   = new TranslateTransition(Duration.millis(300), adminFormWrapper); slideUp.setToY(0); slideUp.setInterpolator(Interpolator.EASE_OUT);
            FadeTransition     fadeInMsg  = new FadeTransition(Duration.millis(280), staffInactiveMsg);    fadeInMsg.setToValue(1);

            ParallelTransition pt = new ParallelTransition(fadeInForm, slideUp, fadeInMsg, brightenBranding);
            pt.setOnFinished(ev -> { switching = false; isStaffActive = false; adminUsernameField.requestFocus(); });
            pt.play();
        });
        fadeOutForm.play();
    }

    // ── Panel switch: Admin → Staff ────────────────────────────────────────────
    private void switchToStaff() {
        switching = true;

        // 1. Fade out admin form (160ms)
        FadeTransition fadeOutForm = new FadeTransition(Duration.millis(160), adminFormWrapper);
        fadeOutForm.setToValue(0);
        fadeOutForm.setOnFinished(e -> {
            // 2. Swap admin panel state
            adminFormWrapper.setVisible(false); adminFormWrapper.setManaged(false);
            adminInactiveMsg.setVisible(true);  adminInactiveMsg.setManaged(true);
            adminInactiveMsg.setOpacity(0);
            adminPanel.setStyle(PANEL_INACTIVE + " -fx-cursor: hand;");

            // Dim admin branding
            FadeTransition dimBranding = new FadeTransition(Duration.millis(300), adminBranding);
            dimBranding.setToValue(0.65);

            // 3. Swap staff panel state
            staffPanel.setStyle(PANEL_ACTIVE + " -fx-cursor: default;");
            staffInactiveMsg.setVisible(false); staffInactiveMsg.setManaged(false);
            staffFormWrapper.setVisible(true);  staffFormWrapper.setManaged(true);
            staffFormWrapper.setOpacity(0);
            staffFormWrapper.setTranslateY(22);

            // 4. Animate in: staff form slides up + admin hint fades in
            FadeTransition      fadeInForm = new FadeTransition(Duration.millis(300), staffFormWrapper);    fadeInForm.setToValue(1);
            TranslateTransition slideUp    = new TranslateTransition(Duration.millis(300), staffFormWrapper); slideUp.setToY(0); slideUp.setInterpolator(Interpolator.EASE_OUT);
            FadeTransition      fadeInMsg  = new FadeTransition(Duration.millis(280), adminInactiveMsg);    fadeInMsg.setToValue(1);

            ParallelTransition pt = new ParallelTransition(fadeInForm, slideUp, fadeInMsg, dimBranding);
            pt.setOnFinished(ev -> { switching = false; isStaffActive = true; staffUsernameField.requestFocus(); });
            pt.play();
        });
        fadeOutForm.play();
    }

    // ── Password toggles ───────────────────────────────────────────────────────
    @FXML public void handleStaffTogglePassword(ActionEvent e) {
        staffPasswordShown = !staffPasswordShown;
        staffPasswordField  .setVisible(!staffPasswordShown); staffPasswordField  .setManaged(!staffPasswordShown);
        staffPasswordVisible.setVisible( staffPasswordShown); staffPasswordVisible.setManaged( staffPasswordShown);
        staffBtnToggle.setText(staffPasswordShown ? "🙈" : "👁");
        if (staffPasswordShown) { staffPasswordVisible.requestFocus(); staffPasswordVisible.positionCaret(staffPasswordVisible.getText().length()); }
        else                    { staffPasswordField  .requestFocus(); staffPasswordField  .positionCaret(staffPasswordField  .getText().length()); }
    }

    @FXML public void handleAdminTogglePassword(ActionEvent e) {
        adminPasswordShown = !adminPasswordShown;
        adminPasswordField  .setVisible(!adminPasswordShown); adminPasswordField  .setManaged(!adminPasswordShown);
        adminPasswordVisible.setVisible( adminPasswordShown); adminPasswordVisible.setManaged( adminPasswordShown);
        adminBtnToggle.setText(adminPasswordShown ? "🙈" : "👁");
        if (adminPasswordShown) { adminPasswordVisible.requestFocus(); adminPasswordVisible.positionCaret(adminPasswordVisible.getText().length()); }
        else                    { adminPasswordField  .requestFocus(); adminPasswordField  .positionCaret(adminPasswordField  .getText().length()); }
    }

    // ── Login handlers ─────────────────────────────────────────────────────────
    @FXML public void handleStaffLogin(ActionEvent e) {
        String username = staffUsernameField.getText().trim();
        String password = staffPasswordShown ? staffPasswordVisible.getText().trim() : staffPasswordField.getText().trim();
        doLogin(username, password, "staff", staffRememberMe.isSelected());
    }

    @FXML public void handleAdminLogin(ActionEvent e) {
        String username = adminUsernameField.getText().trim();
        String password = adminPasswordShown ? adminPasswordVisible.getText().trim() : adminPasswordField.getText().trim();
        doLogin(username, password, "admin", adminRememberMe.isSelected());
    }

    private void doLogin(String username, String password, String role, boolean remember) {
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Fields", "Please enter your username and password."); return;
        }
        User user = userDAO.login(username, password, role);
        if (user != null) {
            if (remember) saveCredentials(username, password, role); else clearCredentials();
            SessionManager.setCurrentUser(user);
            redirectToDashboard(user, role);
        } else {
            showAlert(Alert.AlertType.ERROR, "Login Failed",
                    "Invalid username or password for " + role + " account.\nPlease try again.");
            if (role.equals("staff")) { staffPasswordField.clear(); staffPasswordVisible.clear(); }
            else                      { adminPasswordField.clear(); adminPasswordVisible.clear(); }
        }
    }

    // ── Navigate to dashboard ──────────────────────────────────────────────────
    private void redirectToDashboard(User user, String role) {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        boolean wasFullscreen = stage.isFullScreen();
        boolean wasMaximized  = stage.isMaximized();

        FadeTransition     fadeOut  = new FadeTransition(Duration.millis(200), rootPane); fadeOut.setToValue(0);
        ScaleTransition    scaleOut = new ScaleTransition(Duration.millis(200), rootPane); scaleOut.setToX(0.95); scaleOut.setToY(0.95); scaleOut.setInterpolator(Interpolator.EASE_IN);
        ParallelTransition out      = new ParallelTransition(fadeOut, scaleOut);
        out.setOnFinished(ev -> {
            double w = stage.getWidth(), h = stage.getHeight();
            Scene loadScene = new Scene(createLoadingPane(), w, h);
            loadScene.setFill(Color.WHITE);
            stage.setScene(loadScene); stage.setWidth(w); stage.setHeight(h);
            if (wasFullscreen) Platform.runLater(() -> stage.setFullScreen(true));
            else if (wasMaximized) Platform.runLater(() -> stage.setMaximized(true));

            PauseTransition pause = new PauseTransition(Duration.millis(400));
            pause.setOnFinished(pev -> {
                try {
                    String fxml = role.equals("admin")
                            ? "/main/resources/fxml/AdminDashboard.fxml"
                            : "/main/resources/fxml/StaffDashboard.fxml";
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
                    Parent root = loader.load();
                    if (role.equals("admin")) ((AdminDashboardController) loader.getController()).setCurrentUser(user);
                    else                      ((StaffDashboardController) loader.getController()).setCurrentUser(user);
                    root.setOpacity(0); root.setScaleX(0.97); root.setScaleY(0.97);
                    Scene dash = new Scene(root, w, h); dash.setFill(Color.web("#0f0505"));
                    stage.setScene(dash);
                    stage.setTitle(role.equals("admin") ? "Pass Slip System - Admin Dashboard" : "Pass Slip System - Staff Dashboard");
                    stage.show();
                    if (wasFullscreen) Platform.runLater(() -> stage.setFullScreen(true));
                    else if (wasMaximized) Platform.runLater(() -> stage.setMaximized(true));
                    FadeTransition  fi = new FadeTransition(Duration.millis(420), root);  fi.setToValue(1);
                    ScaleTransition si = new ScaleTransition(Duration.millis(420), root); si.setToX(1); si.setToY(1); si.setInterpolator(Interpolator.EASE_OUT);
                    new ParallelTransition(fi, si).play();
                } catch (IOException ex) { ex.printStackTrace(); showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load Dashboard."); }
            });
            pause.play();
        });
        out.play();
    }

    // ── Sign Up ────────────────────────────────────────────────────────────────
    @FXML public void handleSignUp(ActionEvent e) {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        boolean wasFullscreen = stage.isFullScreen();
        boolean wasMaximized  = stage.isMaximized();
        double  stageW = stage.getWidth(), stageH = stage.getHeight();

        FadeTransition     fadeOut  = new FadeTransition(Duration.millis(160), rootPane); fadeOut.setToValue(0);
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(160), rootPane); slideOut.setToX(-32); slideOut.setInterpolator(Interpolator.EASE_IN);
        ParallelTransition  out      = new ParallelTransition(fadeOut, slideOut);
        out.setOnFinished(ev -> {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/main/resources/fxml/Register.fxml"));
                root.setOpacity(0); root.setTranslateX(40);
                Scene reg = new Scene(root, stageW, stageH); reg.setFill(Color.web("#8B0000"));
                stage.setScene(reg); stage.setTitle("Register"); stage.show();
                Platform.runLater(() -> Platform.runLater(() -> {
                    if (wasFullscreen) stage.setFullScreen(true); else stage.setMaximized(true);
                }));
                FadeTransition      fi = new FadeTransition(Duration.millis(360), root);  fi.setToValue(1);
                TranslateTransition si = new TranslateTransition(Duration.millis(360), root); si.setToX(0); si.setInterpolator(Interpolator.EASE_OUT);
                new ParallelTransition(fi, si).play();
            } catch (Exception ex) { ex.printStackTrace(); }
        });
        out.play();
    }

    @FXML public void handleForgotPassword(ActionEvent e) {
        showAlert(Alert.AlertType.INFORMATION, "Forgot Password",
                "Please contact your system administrator to reset your password.");
    }

    // ── Loading pane ───────────────────────────────────────────────────────────
    private StackPane createLoadingPane() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: white;");
        VBox box = new VBox(16); box.setAlignment(Pos.CENTER);
        ProgressIndicator spinner = new ProgressIndicator(); spinner.setPrefSize(60, 60); spinner.setStyle("-fx-progress-color: #8B0000;");
        Label lbl = new Label("Loading..."); lbl.setStyle("-fx-text-fill: #333333; -fx-font-size: 14px; -fx-font-family: 'Segoe UI';");
        box.getChildren().addAll(spinner, lbl); root.getChildren().add(box);
        return root;
    }

    // ── Credentials persistence ────────────────────────────────────────────────
    private void saveCredentials(String username, String password, String role) {
        Properties p = new Properties();
        p.setProperty("username", username); p.setProperty("password", password);
        p.setProperty("role", role); p.setProperty("rememberMe", "true");
        try (FileOutputStream fos = new FileOutputStream(PREFS_FILE)) { p.store(fos, "Login Preferences"); }
        catch (IOException ex) { System.out.println("Could not save preferences: " + ex.getMessage()); }
    }

    private void clearCredentials() { File f = new File(PREFS_FILE); if (f.exists()) f.delete(); }

    private void loadSavedCredentials() {
        File f = new File(PREFS_FILE); if (!f.exists()) return;
        Properties p = new Properties();
        try (FileInputStream fis = new FileInputStream(f)) {
            p.load(fis);
            if (!"true".equals(p.getProperty("rememberMe"))) return;
            String u    = p.getProperty("username", "");
            String pw   = p.getProperty("password", "");
            String role = p.getProperty("role", "staff");
            if (role.equals("admin")) {
                // Switch directly to admin without animation
                isStaffActive = false;
                staffFormWrapper.setVisible(false); staffFormWrapper.setManaged(false);
                staffInactiveMsg.setVisible(true);  staffInactiveMsg.setManaged(true);
                staffPanel.setStyle(PANEL_INACTIVE + " -fx-cursor: hand;");
                adminPanel.setStyle(PANEL_ACTIVE   + " -fx-cursor: default;");
                adminInactiveMsg.setVisible(false); adminInactiveMsg.setManaged(false);
                adminFormWrapper.setVisible(true);  adminFormWrapper.setManaged(true);
                if (adminBranding != null) adminBranding.setOpacity(1.0);
                adminUsernameField.setText(u); adminPasswordField.setText(pw);
                adminPasswordVisible.setText(pw); adminRememberMe.setSelected(true);
            } else {
                staffUsernameField.setText(u); staffPasswordField.setText(pw);
                staffPasswordVisible.setText(pw); staffRememberMe.setSelected(true);
            }
        } catch (IOException ex) { System.out.println("Could not load preferences: " + ex.getMessage()); }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
