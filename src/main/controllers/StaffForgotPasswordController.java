package main.controllers;

import dao.UserDAO;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import main.utils.EmailService;
import main.utils.OTPManager;

import java.io.IOException;

public class StaffForgotPasswordController {

    @FXML private TextField emailField;
    @FXML private Label     errorLabel;
    @FXML private Button    submitButton;

    @FXML
    public void handleSubmit(ActionEvent e) {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            showError("Please enter your email address.");
            return;
        }
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            showError("Please enter a valid email address.");
            return;
        }

        // Disable UI while working
        submitButton.setDisable(true);
        emailField.setDisable(false);
        showInfo("Checking email address...");

        // Run DB check + email sending on background thread
        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                try {
                    // Step 1: Check if email exists in database
                    UserDAO dao = new UserDAO();
                    if (!dao.emailExists(email)) return "NOT_FOUND";

                    // Step 2: Generate OTP
                    String otp = OTPManager.generateOTP(email);

                    // Step 3: Send email
                    updateMessage("Sending reset code to " + email + "...");
                    boolean sent = EmailService.sendOTPEmail(email, otp);
                    return sent ? "SUCCESS" : "EMAIL_FAILED";

                } catch (Throwable t) {
                    System.out.println("[TASK ERROR] " + t.getClass().getName());
                    System.out.println("[TASK ERROR] " + t.getMessage());
                    t.printStackTrace();
                    throw new Exception(t);
                }
            }
        };

        task.messageProperty().addListener((obs, oldMsg, newMsg) -> {
            javafx.application.Platform.runLater(() -> showInfo(newMsg));
        });

        task.setOnSucceeded(ev -> {
            String result = task.getValue();
            switch (result) {
                case "NOT_FOUND":
                    showError("No account found with that email address.");
                    resetButtons();
                    break;
                case "EMAIL_FAILED":
                    showError("Failed to send email. Please check your connection and try again.");
                    resetButtons();
                    break;
                case "SUCCESS":
                    Stage stage = (Stage) emailField.getScene().getWindow();
                    showSkeletonThenLoad(stage, email);
                    break;
            }
        });

        task.setOnFailed(ev -> {
            Throwable ex = task.getException();
            System.out.println("[TASK FAILED] " + (ex != null ? ex.getMessage() : "null exception"));
            if (ex != null) ex.printStackTrace();
            showError("An unexpected error occurred. Please try again.");
            resetButtons();
        });

        new Thread(task).start();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setStyle("-fx-text-fill: #cc0000; -fx-font-size: 11px;");
    }

    private void showInfo(String msg) {
        errorLabel.setText(msg);
        errorLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 11px;");
    }

    private void resetButtons() {
        submitButton.setDisable(false);
        emailField.setDisable(false);
    }

    private void showSkeletonThenLoad(Stage stage, String email) {
        VBox skeleton = buildSkeleton();
        Scene skeletonScene = new Scene(skeleton, stage.getScene().getWidth(), stage.getScene().getHeight());
        stage.setScene(skeletonScene);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), skeleton);
        fadeIn.setFromValue(0); fadeIn.setToValue(1); fadeIn.play();

        PauseTransition pause = new PauseTransition(Duration.millis(700));
        pause.setOnFinished(ev -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/main/resources/fxml/StaffForgotPasswordSuccess.fxml"));
                Parent root = loader.load();

                StaffForgotPasswordSuccessController controller = loader.getController();
                controller.setEmail(email);

                root.setOpacity(0);
                Scene nextScene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
                stage.setScene(nextScene);
                stage.setTitle("Email Sent");

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
        box.setAlignment(javafx.geometry.Pos.CENTER);
        box.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 6);");
        box.setPrefWidth(420); box.setPrefHeight(380);
        box.setPadding(new javafx.geometry.Insets(30));

        StackPane iconPill = new StackPane();
        iconPill.setPrefHeight(65); iconPill.setMaxWidth(Double.MAX_VALUE);
        iconPill.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 35;");
        addShimmer(iconPill);

        StackPane titleBar = new StackPane();
        titleBar.setPrefHeight(22); titleBar.setPrefWidth(160); titleBar.setMaxWidth(160);
        titleBar.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 6;");
        addShimmer(titleBar);
        javafx.scene.layout.HBox titleBox = new javafx.scene.layout.HBox(titleBar);
        titleBox.setAlignment(javafx.geometry.Pos.CENTER);

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
    public void handleCancel(ActionEvent e) {
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.close();
    }
}