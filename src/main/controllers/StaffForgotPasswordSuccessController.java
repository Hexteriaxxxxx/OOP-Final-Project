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
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;

public class StaffForgotPasswordSuccessController {

    @FXML private Label messageLabel;

    private String email;

    public void setEmail(String email) {
        this.email = email;
        if (messageLabel != null) {
            messageLabel.setText("A password reset code has been sent to "
                    + email + ". Please check your inbox.");
        }
    }

    @FXML
    public void handleReceivedCode(ActionEvent e) {
        Stage currentStage = (Stage) messageLabel.getScene().getWindow();
        showSkeletonThenLoad(currentStage);
    }

    private void showSkeletonThenLoad(Stage stage) {
        VBox skeleton = buildSkeleton();
        Scene skeletonScene = new Scene(skeleton, stage.getScene().getWidth(), stage.getScene().getHeight());
        stage.setScene(skeletonScene);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), skeleton);
        fadeIn.setFromValue(0); fadeIn.setToValue(1); fadeIn.play();

        PauseTransition pause = new PauseTransition(Duration.millis(900));
        pause.setOnFinished(ev -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/main/resources/fxml/StaffForgotPasswordOTP.fxml"));
                Parent root = loader.load();

                StaffForgotPasswordOTPController controller = loader.getController();
                controller.setEmail(email);

                root.setOpacity(0);
                Scene nextScene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
                stage.setScene(nextScene);
                stage.setTitle("Enter Verification Code");

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

        // Icon pill skeleton
        StackPane iconPill = new StackPane();
        iconPill.setPrefHeight(65); iconPill.setMaxWidth(Double.MAX_VALUE);
        iconPill.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 35;");
        addShimmer(iconPill);

        // Checkmark pill skeleton
        StackPane checkPill = new StackPane();
        checkPill.setPrefHeight(42); checkPill.setMaxWidth(Double.MAX_VALUE);
        checkPill.setStyle("-fx-background-color: #e8c0c0; -fx-background-radius: 35;");
        addShimmer(checkPill);

        // Title skeleton
        StackPane titleBar = new StackPane();
        titleBar.setPrefHeight(22); titleBar.setPrefWidth(180); titleBar.setMaxWidth(180);
        titleBar.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 6;");
        addShimmer(titleBar);
        HBox titleBox = new HBox(titleBar);
        titleBox.setAlignment(Pos.CENTER);

        // Message skeleton
        StackPane msgBar = new StackPane();
        msgBar.setPrefHeight(14); msgBar.setMaxWidth(Double.MAX_VALUE);
        msgBar.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 6;");
        addShimmer(msgBar);

        StackPane msgBar2 = new StackPane();
        msgBar2.setPrefHeight(14); msgBar2.setPrefWidth(260); msgBar2.setMaxWidth(260);
        msgBar2.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 6;");
        addShimmer(msgBar2);
        HBox msgBox2 = new HBox(msgBar2);
        msgBox2.setAlignment(Pos.CENTER);

        // Button skeletons
        StackPane btn1 = new StackPane();
        btn1.setPrefHeight(38); btn1.setMaxWidth(Double.MAX_VALUE);
        btn1.setStyle("-fx-background-color: #e8c0c0; -fx-background-radius: 25;");
        addShimmer(btn1);

        StackPane btn2 = new StackPane();
        btn2.setPrefHeight(38); btn2.setMaxWidth(Double.MAX_VALUE);
        btn2.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 25;");
        addShimmer(btn2);

        VBox btnBox = new VBox(8, btn1, btn2);

        box.getChildren().addAll(iconPill, checkPill, titleBox, msgBar, msgBox2, btnBox);
        return box;
    }

    private void addShimmer(StackPane pane) {
        FadeTransition shimmer = new FadeTransition(Duration.millis(700), pane);
        shimmer.setFromValue(1.0); shimmer.setToValue(0.4);
        shimmer.setAutoReverse(true); shimmer.setCycleCount(FadeTransition.INDEFINITE);
        shimmer.play();
    }

    @FXML
    public void handleBackToLogin(ActionEvent e) {
        Stage stage = (Stage) messageLabel.getScene().getWindow();
        stage.close();
    }
}