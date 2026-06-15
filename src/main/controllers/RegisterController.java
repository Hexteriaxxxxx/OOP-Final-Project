package main.controllers;

import dao.UserDAO;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

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
    @FXML private Hyperlink     lnkTerms;

    private boolean showPassword = false;
    private boolean showConfirm  = false;
    private String  selectedRole = "admin";
    private final UserDAO userDAO = new UserDAO();

    // ── Password requirements popup ──
    private Popup pwPopup;
    private Label reqLength, reqUpper, reqLower, reqNumber, reqSpecial;

    // ── Confirm password match label ──
    @FXML private Label confirmMatchLabel;

    private static final String TERMS_TEXT =
            "TERMS AND CONDITIONS OF USE\n" +
                    "PUP Santa Rosa Campus — Employee Pass Slip Issuance System\n" +
                    "Effective Date: June 2026\n\n" +
                    "1. ACCEPTANCE OF TERMS\n" +
                    "By creating an account and using this system, you agree to be bound by " +
                    "these Terms and Conditions. If you do not agree, do not proceed with registration.\n\n" +
                    "2. AUTHORIZED USE\n" +
                    "This system is exclusively for authorized personnel of the Polytechnic University " +
                    "of the Philippines – Santa Rosa Campus. Unauthorized access or use is strictly prohibited " +
                    "and may be subject to disciplinary action and/or legal proceedings.\n\n" +
                    "3. PASS SLIP RESPONSIBILITY\n" +
                    "a) Users are responsible for the accuracy of all information submitted through this system.\n" +
                    "b) Pass slips issued for personal reasons are subject to a waiver of liability. " +
                    "The university shall not be held responsible for any incident occurring outside " +
                    "school premises during personal leave.\n" +
                    "c) Misuse of the pass slip system, including falsification of information, " +
                    "shall be subject to disciplinary measures.\n\n" +
                    "4. DATA PRIVACY (Republic Act No. 10173)\n" +
                    "In compliance with the Philippine Data Privacy Act of 2012:\n" +
                    "a) Your personal data (name, email, contact) is collected solely for " +
                    "the purpose of administering the pass slip issuance system.\n" +
                    "b) Your data will be stored securely and will not be shared with " +
                    "unauthorized third parties.\n" +
                    "c) You have the right to access, correct, and request deletion of your personal data " +
                    "by contacting the system administrator.\n" +
                    "d) By registering, you expressly consent to the collection and processing of your " +
                    "personal data for the purposes stated above.\n\n" +
                    "5. SYSTEM AVAILABILITY\n" +
                    "The PUP Santa Rosa Campus administration reserves the right to modify, suspend, " +
                    "or discontinue this system at any time without prior notice.\n\n" +
                    "6. AMENDMENTS\n" +
                    "These Terms and Conditions may be updated from time to time. " +
                    "Continued use of the system after changes constitutes acceptance of the revised terms.\n\n" +
                    "7. GOVERNING LAW\n" +
                    "These Terms and Conditions shall be governed by the laws of the Republic " +
                    "of the Philippines and the policies of the Polytechnic University of the Philippines.\n\n" +
                    "For questions or concerns, please contact the system administrator at " +
                    "PUP Santa Rosa Campus.";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setActiveTab("Admin");
        passwordVisible.textProperty().bindBidirectional(passwordField.textProperty());
        confirmPasswordVisible.textProperty().bindBidirectional(confirmPasswordField.textProperty());

        buildPasswordPopup();
        attachPasswordListeners();
        attachConfirmListeners();
    }

    // ── Password requirements popup ──────────────────────────────────────────

    private void buildPasswordPopup() {
        pwPopup = new Popup();
        pwPopup.setAutoFix(true);
        pwPopup.setAutoHide(false);

        VBox box = new VBox(6);
        box.setPadding(new Insets(12, 16, 12, 16));
        box.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-radius: 8;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 3);"
        );

        Label title = new Label("Password must contain:");
        title.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #333;");

        reqLength  = makeReqLabel("At least 8 characters");
        reqUpper   = makeReqLabel("At least 1 uppercase letter (A\u2013Z)");
        reqLower   = makeReqLabel("At least 1 lowercase letter (a\u2013z)");
        reqNumber  = makeReqLabel("At least 1 number (0\u20139)");
        reqSpecial = makeReqLabel("At least 1 special character (!@#$\u2026)");

        box.getChildren().addAll(title, reqLength, reqUpper, reqLower, reqNumber, reqSpecial);
        pwPopup.getContent().add(box);
    }

    private Label makeReqLabel(String text) {
        Label lbl = new Label("\u2715  " + text);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #cc0000;");
        return lbl;
    }

    private void attachPasswordListeners() {
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> updateRequirements(newVal));
        passwordVisible.textProperty().addListener((obs, oldVal, newVal) -> updateRequirements(newVal));

        passwordField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused) showPopup(passwordField);
            else if (!passwordVisible.isFocused()) pwPopup.hide();
        });
        passwordVisible.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused) showPopup(passwordVisible);
            else if (!passwordField.isFocused()) pwPopup.hide();
        });
    }

    private void attachConfirmListeners() {
        confirmPasswordField.textProperty().addListener((obs, o, n) -> updateConfirmMatch());
        confirmPasswordVisible.textProperty().addListener((obs, o, n) -> updateConfirmMatch());
        passwordField.textProperty().addListener((obs, o, n) -> updateConfirmMatch());
    }

    private void updateConfirmMatch() {
        String pw  = passwordField.getText();
        String cfm = confirmPasswordField.getText();
        if (cfm.isEmpty()) {
            confirmMatchLabel.setText("");
            return;
        }
        if (pw.equals(cfm)) {
            confirmMatchLabel.setText("\u2714  Passwords match");
            confirmMatchLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #1a7a3c;");
        } else {
            confirmMatchLabel.setText("\u2715  Passwords do not match");
            confirmMatchLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #cc0000;");
        }
    }

    private void showPopup(Node anchor) {
        if (!pwPopup.isShowing()) {
            javafx.geometry.Bounds bounds = anchor.localToScreen(anchor.getBoundsInLocal());
            if (bounds != null) {
                pwPopup.show(anchor, bounds.getMinX(), bounds.getMaxY() + 4);
            }
        }
    }

    private void updateRequirements(String pw) {
        setReq(reqLength,  pw.length() >= 8);
        setReq(reqUpper,   pw.matches(".*[A-Z].*"));
        setReq(reqLower,   pw.matches(".*[a-z].*"));
        setReq(reqNumber,  pw.matches(".*[0-9].*"));
        setReq(reqSpecial, pw.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*"));

        Node anchor = showPassword ? passwordVisible : passwordField;
        if (pwPopup.isShowing()) {
            javafx.geometry.Bounds bounds = anchor.localToScreen(anchor.getBoundsInLocal());
            if (bounds != null) {
                pwPopup.setX(bounds.getMinX());
                pwPopup.setY(bounds.getMaxY() + 4);
            }
        }
    }

    private void setReq(Label lbl, boolean met) {
        String base = lbl.getText().substring(3);
        if (met) {
            lbl.setText("\u2714  " + base);
            lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #1a7a3c;");
        } else {
            lbl.setText("\u2715  " + base);
            lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #cc0000;");
        }
    }

    private boolean isPasswordValid(String pw) {
        return pw.length() >= 8
                && pw.matches(".*[A-Z].*")
                && pw.matches(".*[a-z].*")
                && pw.matches(".*[0-9].*")
                && pw.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
    }

    // ── Tab handling ─────────────────────────────────────────────────────────

    @FXML public void handleStaffTab(ActionEvent e) { selectedRole = "staff"; setActiveTab("Staff"); }
    @FXML public void handleAdminTab(ActionEvent e) { selectedRole = "admin"; setActiveTab("Admin"); }

    private void setActiveTab(String role) {
        String on  = "-fx-background-color: #800000; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 6; -fx-padding: 7 32;";
        String off = "-fx-background-color: transparent; -fx-text-fill: #888; -fx-font-size: 13px; -fx-background-radius: 6; -fx-padding: 7 32;";
        staffTab.setStyle(role.equals("Staff") ? on : off);
        adminTab.setStyle(role.equals("Admin") ? on : off);
    }

    // ── Toggle password visibility ───────────────────────────────────────────

    @FXML public void handleTogglePassword(ActionEvent e) {
        showPassword = !showPassword;
        passwordField  .setVisible(!showPassword); passwordField  .setManaged(!showPassword);
        passwordVisible.setVisible( showPassword); passwordVisible.setManaged( showPassword);
        btnTogglePassword.setText(showPassword ? "\uD83D\uDE48" : "\uD83D\uDC41");
    }

    @FXML public void handleToggleConfirm(ActionEvent e) {
        showConfirm = !showConfirm;
        confirmPasswordField  .setVisible(!showConfirm); confirmPasswordField  .setManaged(!showConfirm);
        confirmPasswordVisible.setVisible( showConfirm); confirmPasswordVisible.setManaged( showConfirm);
        btnToggleConfirm.setText(showConfirm ? "\uD83D\uDE48" : "\uD83D\uDC41");
    }

    // ── Terms dialog ─────────────────────────────────────────────────────────

    @FXML public void handleShowTerms(ActionEvent e) { showTermsDialog(); }

    private void showTermsDialog() {
        Stage owner = (Stage) fullNameField.getScene().getWindow();

        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 4);");
        root.setPrefWidth(560);
        root.setOpacity(0);
        root.setTranslateY(30);

        Rectangle clip = new Rectangle();
        clip.setArcWidth(24); clip.setArcHeight(24);
        clip.widthProperty().bind(root.widthProperty());
        clip.heightProperty().bind(root.heightProperty());
        root.setClip(clip);

        VBox header = new VBox(4);
        header.setPadding(new Insets(18, 20, 14, 20));
        header.setStyle("-fx-background-color: #8B0000;");
        Label lblTitle = new Label("\uD83D\uDCCB  Terms and Conditions & Data Privacy Notice");
        lblTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label lblSub = new Label("PUP Santa Rosa Campus — Pass Slip Issuance System");
        lblSub.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.8);");
        header.getChildren().addAll(lblTitle, lblSub);

        TextArea txtContent = new TextArea(TERMS_TEXT);
        txtContent.setEditable(false); txtContent.setWrapText(true); txtContent.setPrefHeight(340);
        txtContent.setStyle("-fx-background-color: white; -fx-border-color: transparent; " +
                "-fx-font-size: 12px; -fx-text-fill: #333; -fx-padding: 14;");

        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(12, 20, 16, 20));
        footer.setStyle("-fx-border-color: #e8e8e8; -fx-border-width: 1 0 0 0;");

        Button btnClose = new Button("I Agree");
        btnClose.setStyle("-fx-background-color: #8B0000; -fx-text-fill: white; " +
                "-fx-font-size: 12px; -fx-padding: 8 24; -fx-background-radius: 20; " +
                "-fx-border-width: 0; -fx-cursor: hand;");

        Button btnDecline = new Button("Decline");
        btnDecline.setStyle("-fx-background-color: white; -fx-text-fill: #555; " +
                "-fx-font-size: 12px; -fx-padding: 8 24; -fx-background-radius: 20; " +
                "-fx-border-color: #ccc; -fx-border-radius: 20; -fx-cursor: hand;");

        footer.getChildren().addAll(btnDecline, btnClose);
        root.getChildren().addAll(header, txtContent, footer);

        TranslateTransition slideDown = new TranslateTransition(Duration.millis(200), root);
        slideDown.setToY(30);
        slideDown.setInterpolator(Interpolator.EASE_IN);
        FadeTransition fadeClose = new FadeTransition(Duration.millis(200), root);
        fadeClose.setToValue(0);
        ParallelTransition closeAnim = new ParallelTransition(slideDown, fadeClose);

        btnClose.setOnAction(ev -> {
            btnClose.setDisable(true); btnDecline.setDisable(true);
            closeAnim.setOnFinished(e -> { termsCheckBox.setSelected(true);  dialog.close(); });
            closeAnim.play();
        });
        btnDecline.setOnAction(ev -> {
            btnClose.setDisable(true); btnDecline.setDisable(true);
            closeAnim.setOnFinished(e -> { termsCheckBox.setSelected(false); dialog.close(); });
            closeAnim.play();
        });

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);

        dialog.setOnShown(e -> {
            dialog.setX(owner.getX() + (owner.getWidth()  - dialog.getWidth())  / 2);
            dialog.setY(owner.getY() + (owner.getHeight() - dialog.getHeight()) / 2);
            TranslateTransition slideUp = new TranslateTransition(Duration.millis(280), root);
            slideUp.setToY(0);
            slideUp.setInterpolator(Interpolator.EASE_OUT);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(280), root);
            fadeIn.setToValue(1);
            new ParallelTransition(slideUp, fadeIn).play();
        });

        dialog.showAndWait();
    }

    // ── Register ─────────────────────────────────────────────────────────────

    @FXML public void handleRegister(ActionEvent e) {
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
        if (!isPasswordValid(password)) {
            showAlert(Alert.AlertType.WARNING,"Weak Password",
                    "Password must be at least 8 characters and contain:\n" +
                            "  \u2022 At least 1 uppercase letter (A\u2013Z)\n" +
                            "  \u2022 At least 1 lowercase letter (a\u2013z)\n" +
                            "  \u2022 At least 1 number (0\u20139)\n" +
                            "  \u2022 At least 1 special character (!@#$\u2026)");
            return;
        }
        if (!password.equals(confirm)) {
            showAlert(Alert.AlertType.WARNING,"Password Mismatch","Passwords do not match.");
            confirmPasswordField.clear(); confirmPasswordVisible.clear(); return;
        }
        if (!termsCheckBox.isSelected()) {
            showTermsDialog();
            if (!termsCheckBox.isSelected()) {
                showAlert(Alert.AlertType.WARNING,"Terms Required",
                        "You must read and agree to the Terms and Conditions and Data Privacy Notice to register.");
                return;
            }
        }
        if (userDAO.usernameExists(username)) {
            showAlert(Alert.AlertType.WARNING,"Username Taken",
                    "Username \""+username+"\" is already taken. Please choose another.");
            usernameField.clear(); return;
        }
        // FIX: Show pending approval message — do NOT navigate to login yet
        if (userDAO.register(fullName, email, username, password, selectedRole)) {
            showAlert(Alert.AlertType.INFORMATION, "Registration Submitted",
                    "Your account has been submitted for admin approval.\n" +
                            "You will be able to log in once your account has been approved.\n\n" +
                            "Please wait for the administrator to review your request.");
            // Clear the form so user knows registration is done
            fullNameField.clear();
            emailField.clear();
            usernameField.clear();
            passwordField.clear();
            confirmPasswordField.clear();
            termsCheckBox.setSelected(false);
            confirmMatchLabel.setText("");
        } else {
            showAlert(Alert.AlertType.ERROR,"Registration Failed","Something went wrong. Please try again.");
        }
    }

    // ── Navigation ───────────────────────────────────────────────────────────

    @FXML public void handleBackToLogin(ActionEvent e) { navigateToLogin(e); }

    private void navigateToLogin(ActionEvent e) {
        Stage stage = (Stage) fullNameField.getScene().getWindow();
        boolean wasFullscreen = stage.isFullScreen();
        double  stageW        = stage.getWidth();
        double  stageH        = stage.getHeight();
        Node currentRoot = fullNameField.getScene().getRoot();
        FadeTransition fadeOut = new FadeTransition(Duration.millis(160), currentRoot);
        fadeOut.setToValue(0);
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(160), currentRoot);
        slideOut.setToX(40);
        slideOut.setInterpolator(Interpolator.EASE_IN);
        ParallelTransition out = new ParallelTransition(fadeOut, slideOut);
        out.setOnFinished(ev -> {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/main/resources/fxml/Login.fxml"));
                root.setOpacity(0);
                root.setTranslateX(-40);
                Scene loginScene = new Scene(root, stageW, stageH);
                loginScene.setFill(Color.web("#8B0000"));
                stage.setScene(loginScene);
                stage.setTitle("Pass Slip Issuance System");
                stage.show();
                Platform.runLater(() -> Platform.runLater(() -> {
                    if (wasFullscreen) stage.setFullScreen(true);
                    else stage.setMaximized(true);
                }));
                FadeTransition fadeIn = new FadeTransition(Duration.millis(360), root);
                fadeIn.setToValue(1);
                TranslateTransition slideIn = new TranslateTransition(Duration.millis(360), root);
                slideIn.setToX(0);
                slideIn.setInterpolator(Interpolator.EASE_OUT);
                new ParallelTransition(fadeIn, slideIn).play();
            } catch (IOException ex) {
                showAlert(Alert.AlertType.ERROR,"Navigation Error","Could not load Login page.");
            }
        });
        out.play();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}