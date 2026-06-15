package main.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class StaffResetPasswordController implements Initializable {

    @FXML private PasswordField newPasswordField;
    @FXML private TextField newPasswordVisible;
    @FXML private Button toggleNewPassword;

    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField confirmPasswordVisible;
    @FXML private Button toggleConfirmPassword;

    @FXML private Label errorLabel;
    @FXML private Label strengthLabel;
    @FXML private HBox strengthBarBox;
    @FXML private VBox checklistBox;
    @FXML private Label matchLabel;

    @FXML private Rectangle seg1;
    @FXML private Rectangle seg2;
    @FXML private Rectangle seg3;
    @FXML private Rectangle seg4;

    @FXML private Label checkLength;
    @FXML private Label checkUpper;
    @FXML private Label checkLower;
    @FXML private Label checkNumber;
    @FXML private Label checkSpecial;

    private boolean newPassVisible = false;
    private boolean confirmPassVisible = false;
    private String email;

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        newPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newPasswordVisible.getText().equals(newVal))
                newPasswordVisible.setText(newVal);
            updateStrengthAndChecklist(newVal);
            updateMatchLabel();
        });
        newPasswordVisible.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newPasswordField.getText().equals(newVal))
                newPasswordField.setText(newVal);
        });

        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!confirmPasswordVisible.getText().equals(newVal))
                confirmPasswordVisible.setText(newVal);
            updateMatchLabel();
        });
        confirmPasswordVisible.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!confirmPasswordField.getText().equals(newVal))
                confirmPasswordField.setText(newVal);
        });
    }

    @FXML
    public void handleToggleNewPassword(ActionEvent e) {
        newPassVisible = !newPassVisible;
        newPasswordField.setVisible(!newPassVisible);
        newPasswordField.setManaged(!newPassVisible);
        newPasswordVisible.setVisible(newPassVisible);
        newPasswordVisible.setManaged(newPassVisible);
        toggleNewPassword.setText(newPassVisible ? "🙈" : "👁");
        if (newPassVisible) newPasswordVisible.requestFocus();
        else newPasswordField.requestFocus();
    }

    @FXML
    public void handleToggleConfirmPassword(ActionEvent e) {
        confirmPassVisible = !confirmPassVisible;
        confirmPasswordField.setVisible(!confirmPassVisible);
        confirmPasswordField.setManaged(!confirmPassVisible);
        confirmPasswordVisible.setVisible(confirmPassVisible);
        confirmPasswordVisible.setManaged(confirmPassVisible);
        toggleConfirmPassword.setText(confirmPassVisible ? "🙈" : "👁");
        if (confirmPassVisible) confirmPasswordVisible.requestFocus();
        else confirmPasswordField.requestFocus();
    }

    private void updateStrengthAndChecklist(String password) {
        boolean hasContent = password != null && !password.isEmpty();

        checklistBox.setVisible(hasContent);
        checklistBox.setManaged(hasContent);
        strengthBarBox.setVisible(hasContent);
        strengthBarBox.setManaged(hasContent);
        strengthLabel.setVisible(hasContent);
        strengthLabel.setManaged(hasContent);

        if (!hasContent) return;

        boolean lenOk     = password.length() >= 8;
        boolean upperOk   = password.matches(".*[A-Z].*");
        boolean lowerOk   = password.matches(".*[a-z].*");
        boolean numberOk  = password.matches(".*[0-9].*");
        boolean specialOk = password.matches(".*[^A-Za-z0-9].*");

        setCheck(checkLength,  lenOk);
        setCheck(checkUpper,   upperOk);
        setCheck(checkLower,   lowerOk);
        setCheck(checkNumber,  numberOk);
        setCheck(checkSpecial, specialOk);

        int score = (lenOk ? 1 : 0) + (upperOk ? 1 : 0) + (lowerOk ? 1 : 0)
                + (numberOk ? 1 : 0) + (specialOk ? 1 : 0);

        String level, color;
        int filledSegs;

        if (password.length() < 8) {
            level = "Too Short";  color = "#e53935"; filledSegs = 1;
        } else if (score <= 2) {
            level = "Weak";       color = "#fb8c00"; filledSegs = 1;
        } else if (score == 3) {
            level = "Fair";       color = "#fdd835"; filledSegs = 2;
        } else if (score == 4) {
            level = "Good";       color = "#43a047"; filledSegs = 3;
        } else {
            level = "Strong 💪";  color = "#1b5e20"; filledSegs = 4;
        }

        Rectangle[] segs = {seg1, seg2, seg3, seg4};
        for (int i = 0; i < segs.length; i++)
            segs[i].setFill(Color.web(i < filledSegs ? color : "#dddddd"));

        strengthLabel.setText(level);
        strengthLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
    }

    private void setCheck(Label checkLabel, boolean passed) {
        if (passed) {
            checkLabel.setText("✓");
            checkLabel.setStyle("-fx-text-fill: #43a047; -fx-font-size: 11px; -fx-font-weight: bold;");
        } else {
            checkLabel.setText("✗");
            checkLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 11px; -fx-font-weight: bold;");
        }
    }

    private void updateMatchLabel() {
        String newPass     = newPasswordField.getText();
        String confirmPass = confirmPasswordField.getText();

        if (confirmPass.isEmpty()) {
            matchLabel.setVisible(false);
            matchLabel.setManaged(false);
            return;
        }

        matchLabel.setVisible(true);
        matchLabel.setManaged(true);

        if (newPass.equals(confirmPass)) {
            matchLabel.setText("✓ Passwords match");
            matchLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #43a047; -fx-font-weight: bold;");
        } else {
            matchLabel.setText("✗ Passwords do not match");
            matchLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #e53935; -fx-font-weight: bold;");
        }
    }

    @FXML
    public void handleEnter(ActionEvent e) {
        String newPassword     = newPasswordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        if (newPassword.isEmpty()) {
            errorLabel.setText("Please enter a new password.");
            return;
        }
        if (newPassword.length() < 8) {
            errorLabel.setText("Password must be at least 8 characters.");
            return;
        }

        boolean upperOk   = newPassword.matches(".*[A-Z].*");
        boolean lowerOk   = newPassword.matches(".*[a-z].*");
        boolean numberOk  = newPassword.matches(".*[0-9].*");
        boolean specialOk = newPassword.matches(".*[^A-Za-z0-9].*");
        int score = (upperOk ? 1 : 0) + (lowerOk ? 1 : 0)
                + (numberOk ? 1 : 0) + (specialOk ? 1 : 0);

        if (score <= 1) {
            errorLabel.setText("Password is too weak. Please add uppercase, numbers, or special characters.");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            errorLabel.setText("Passwords do not match.");
            return;
        }

        errorLabel.setText("");

        // Save new password to database
        boolean updated = new dao.UserDAO().updatePasswordByEmail(email, newPassword);
        if (!updated) {
            errorLabel.setText("Failed to update password. Please try again.");
            return;
        }

        Stage stage = (Stage) newPasswordField.getScene().getWindow();
        showSkeletonThenSuccess(stage);
    }

    private void showSkeletonThenSuccess(Stage stage) {
        VBox skeleton = buildSkeleton();
        Scene skeletonScene = new Scene(skeleton, stage.getScene().getWidth(), stage.getScene().getHeight());
        stage.setScene(skeletonScene);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), skeleton);
        fadeIn.setFromValue(0); fadeIn.setToValue(1); fadeIn.play();

        PauseTransition pause = new PauseTransition(Duration.millis(900));
        pause.setOnFinished(ev -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/main/resources/fxml/StaffResetPasswordSuccess.fxml"));
                Parent root = loader.load();

                root.setOpacity(0);
                Scene successScene = new Scene(root, 420, 320);
                stage.setScene(successScene);
                stage.setTitle("Password Reset");
                stage.sizeToScene();

                FadeTransition fadeInSuccess = new FadeTransition(Duration.millis(300), root);
                fadeInSuccess.setFromValue(0); fadeInSuccess.setToValue(1); fadeInSuccess.play();

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        pause.play();
    }

    private VBox buildSkeleton() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: #ffffff;");
        box.setPrefWidth(420); box.setPrefHeight(620);
        box.setPadding(new Insets(25, 30, 25, 30));

        StackPane iconPill = new StackPane();
        iconPill.setPrefHeight(55); iconPill.setMaxWidth(Double.MAX_VALUE);
        iconPill.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 30;");
        addShimmer(iconPill);

        StackPane titleBar = new StackPane();
        titleBar.setPrefHeight(20); titleBar.setPrefWidth(160); titleBar.setMaxWidth(160);
        titleBar.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 6;");
        addShimmer(titleBar);
        HBox titleBox = new HBox(titleBar);
        titleBox.setAlignment(Pos.CENTER);

        StackPane subBar = new StackPane();
        subBar.setPrefHeight(12); subBar.setPrefWidth(220); subBar.setMaxWidth(220);
        subBar.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 6;");
        addShimmer(subBar);
        HBox subBox = new HBox(subBar);
        subBox.setAlignment(Pos.CENTER);

        StackPane input1 = makeSkeletonInput();
        StackPane input2 = makeSkeletonInput();

        StackPane checklist = new StackPane();
        checklist.setPrefHeight(95); checklist.setMaxWidth(Double.MAX_VALUE);
        checklist.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 10;");
        addShimmer(checklist);

        StackPane btn1 = new StackPane();
        btn1.setPrefHeight(36); btn1.setMaxWidth(Double.MAX_VALUE);
        btn1.setStyle("-fx-background-color: #e8c0c0; -fx-background-radius: 25;");
        addShimmer(btn1);

        StackPane btn2 = new StackPane();
        btn2.setPrefHeight(36); btn2.setMaxWidth(Double.MAX_VALUE);
        btn2.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 25;");
        addShimmer(btn2);

        VBox btnBox = new VBox(7, btn1, btn2);
        box.getChildren().addAll(iconPill, titleBox, subBox, input1, input2, checklist, btnBox);
        return box;
    }

    private StackPane makeSkeletonInput() {
        StackPane pane = new StackPane();
        pane.setPrefHeight(34); pane.setMaxWidth(Double.MAX_VALUE);
        pane.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 8;");
        addShimmer(pane);
        return pane;
    }

    private void addShimmer(StackPane pane) {
        FadeTransition shimmer = new FadeTransition(Duration.millis(700), pane);
        shimmer.setFromValue(1.0); shimmer.setToValue(0.4);
        shimmer.setAutoReverse(true); shimmer.setCycleCount(FadeTransition.INDEFINITE);
        shimmer.play();
    }

    @FXML
    public void handleCancel(ActionEvent e) {
        Stage stage = (Stage) newPasswordField.getScene().getWindow();
        stage.close();
    }
}