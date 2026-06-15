package main.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import main.utils.OTPManager;

import java.io.IOException;

public class StaffForgotPasswordOTPController {

    @FXML private TextField otpField;
    @FXML private Label     errorLabel;
    @FXML private Label     subtitleLabel;

    private String email;

    public void setEmail(String email) {
        this.email = email;
        if (subtitleLabel != null) {
            subtitleLabel.setText("Enter the 6-digit code sent to " + email + ".");
        }
    }

    @FXML
    public void handleSubmit(ActionEvent e) {
        String code = otpField.getText().trim();

        if (code.isEmpty()) {
            showError("Please enter the verification code.");
            return;
        }
        if (!code.matches("\\d{6}")) {
            showError("Code must be exactly 6 digits.");
            return;
        }

        // Check expiry first for a better error message
        if (OTPManager.isExpired(email)) {
            showError("Code has expired. Please go back and request a new one.");
            return;
        }

        // Verify OTP
        if (!OTPManager.verifyOTP(email, code)) {
            showError("Incorrect code. Please check your email and try again.");
            return;
        }

        // OTP verified! Navigate to reset password screen
        errorLabel.setText("");
        Stage stage = (Stage) otpField.getScene().getWindow();
        showSkeletonThenLoad(stage);
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setStyle("-fx-text-fill: #cc0000; -fx-font-size: 11px;");
    }

    private void showSkeletonThenLoad(Stage stage) {
        VBox skeleton = buildSkeleton();
        Scene skeletonScene = new Scene(skeleton, 420, 380);
        stage.setScene(skeletonScene);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), skeleton);
        fadeIn.setFromValue(0); fadeIn.setToValue(1); fadeIn.play();

        PauseTransition pause = new PauseTransition(Duration.millis(700));
        pause.setOnFinished(ev -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/main/resources/fxml/StaffResetPassword.fxml"));
                Parent root = loader.load();

                StaffResetPasswordController controller = loader.getController();
                controller.setEmail(email);

                root.setOpacity(0);
                Scene nextScene = new Scene(root, 420, 620);
                stage.setScene(nextScene);
                stage.setTitle("Reset Password");
                stage.sizeToScene();

                FadeTransition fadeInReal = new FadeTransition(Duration.millis(300), root);
                fadeInReal.setFromValue(0); fadeInReal.setToValue(1); fadeInReal.play();

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        pause.play();
    }

    private VBox buildSkeleton() {
        VBox box = new VBox(14);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 6);");
        box.setPrefWidth(420); box.setPrefHeight(380);
        box.setPadding(new Insets(30));

        StackPane iconPill = new StackPane();
        iconPill.setPrefHeight(65); iconPill.setMaxWidth(Double.MAX_VALUE);
        iconPill.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 35;");
        addShimmer(iconPill);

        StackPane titleBar = new StackPane();
        titleBar.setPrefHeight(22); titleBar.setPrefWidth(200); titleBar.setMaxWidth(200);
        titleBar.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 6;");
        addShimmer(titleBar);
        HBox titleBox = new HBox(titleBar);
        titleBox.setAlignment(Pos.CENTER);

        StackPane subBar = new StackPane();
        subBar.setPrefHeight(14); subBar.setMaxWidth(Double.MAX_VALUE);
        subBar.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 6;");
        addShimmer(subBar);

        StackPane inputBar = new StackPane();
        inputBar.setPrefHeight(38); inputBar.setMaxWidth(Double.MAX_VALUE);
        inputBar.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 8;");
        addShimmer(inputBar);

        StackPane btn1 = new StackPane();
        btn1.setPrefHeight(38); btn1.setMaxWidth(Double.MAX_VALUE);
        btn1.setStyle("-fx-background-color: #e8c0c0; -fx-background-radius: 25;");
        addShimmer(btn1);

        StackPane btn2 = new StackPane();
        btn2.setPrefHeight(38); btn2.setMaxWidth(Double.MAX_VALUE);
        btn2.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 25;");
        addShimmer(btn2);

        VBox btnBox = new VBox(8, btn1, btn2);
        box.getChildren().addAll(iconPill, titleBox, subBar, inputBar, btnBox);
        return box;
    }

    private void addShimmer(StackPane pane) {
        FadeTransition shimmer = new FadeTransition(Duration.millis(700), pane);
        shimmer.setFromValue(1.0); shimmer.setToValue(0.4);
        shimmer.setAutoReverse(true); shimmer.setCycleCount(FadeTransition.INDEFINITE);
        shimmer.play();
    }

    @FXML
    public void handleBack(ActionEvent e) {
        Stage stage = (Stage) otpField.getScene().getWindow();
        stage.close();
    }
}
