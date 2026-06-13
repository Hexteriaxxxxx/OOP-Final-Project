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
    }

    @FXML public void handleStaffTab(ActionEvent e) { selectedRole = "staff"; setActiveTab("Staff"); }
    @FXML public void handleAdminTab(ActionEvent e) { selectedRole = "admin"; setActiveTab("Admin"); }

    private void setActiveTab(String role) {
        String on  = "-fx-background-color: #800000; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 6; -fx-padding: 7 32;";
        String off = "-fx-background-color: transparent; -fx-text-fill: #888; -fx-font-size: 13px; -fx-background-radius: 6; -fx-padding: 7 32;";
        staffTab.setStyle(role.equals("Staff") ? on : off);
        adminTab.setStyle(role.equals("Admin") ? on : off);
    }

    @FXML public void handleTogglePassword(ActionEvent e) {
        showPassword = !showPassword;
        passwordField  .setVisible(!showPassword); passwordField  .setManaged(!showPassword);
        passwordVisible.setVisible( showPassword); passwordVisible.setManaged( showPassword);
        btnTogglePassword.setText(showPassword ? "🙈" : "👁");
    }

    @FXML public void handleToggleConfirm(ActionEvent e) {
        showConfirm = !showConfirm;
        confirmPasswordField  .setVisible(!showConfirm); confirmPasswordField  .setManaged(!showConfirm);
        confirmPasswordVisible.setVisible( showConfirm); confirmPasswordVisible.setManaged( showConfirm);
        btnToggleConfirm.setText(showConfirm ? "🙈" : "👁");
    }

    @FXML public void handleShowTerms(ActionEvent e) { showTermsDialog(); }

    private void showTermsDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT); // ← TRANSPARENT removes OS border entirely

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 4);");
        root.setPrefWidth(560);

        // ── Clip to rounded corners — no white residue bleeding out ──
        Rectangle clip = new Rectangle();
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        clip.widthProperty() .bind(root.widthProperty());
        clip.heightProperty().bind(root.heightProperty());
        root.setClip(clip);

        // Header — no background-radius needed since clip handles it
        VBox header = new VBox(4);
        header.setPadding(new Insets(18, 20, 14, 20));
        header.setStyle("-fx-background-color: #8B0000;");
        Label lblTitle = new Label("📋  Terms and Conditions & Data Privacy Notice");
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

        Button btnClose = new Button("Close");
        btnClose.setStyle("-fx-background-color: #8B0000; -fx-text-fill: white; " +
                "-fx-font-size: 12px; -fx-padding: 8 24; -fx-background-radius: 20; " +
                "-fx-border-width: 0; -fx-cursor: hand;");
        btnClose.setOnAction(ev -> { termsCheckBox.setSelected(true); dialog.close(); });

        Button btnDecline = new Button("Decline");
        btnDecline.setStyle("-fx-background-color: white; -fx-text-fill: #555; " +
                "-fx-font-size: 12px; -fx-padding: 8 24; -fx-background-radius: 20; " +
                "-fx-border-color: #ccc; -fx-border-radius: 20; -fx-cursor: hand;");
        btnDecline.setOnAction(ev -> { termsCheckBox.setSelected(false); dialog.close(); });

        footer.getChildren().addAll(btnDecline, btnClose);
        root.getChildren().addAll(header, txtContent, footer);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

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
        if (!password.equals(confirm)) {
            showAlert(Alert.AlertType.WARNING,"Password Mismatch","Passwords do not match.");
            confirmPasswordField.clear(); confirmPasswordVisible.clear(); return;
        }
        if (password.length() < 6) {
            showAlert(Alert.AlertType.WARNING,"Weak Password","Password must be at least 6 characters."); return;
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
        if (userDAO.register(fullName, email, username, password, selectedRole)) {
            showAlert(Alert.AlertType.INFORMATION,"Registration Successful",
                    "Your account has been created successfully!\nYou may now log in.");
            navigateToLogin(e);
        } else {
            showAlert(Alert.AlertType.ERROR,"Registration Failed","Something went wrong. Please try again.");
        }
    }

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
