package main.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;

public class StaffForgotPasswordController {

    @FXML private TextField emailField;
    @FXML private Label errorLabel;

    @FXML
    public void handleSubmit(ActionEvent e) {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            errorLabel.setText("Please enter your email address.");
            return;
        }

        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            errorLabel.setText("Please enter a valid email address.");
            return;
        }

        errorLabel.setText("");

        // Show skeleton loader then transition
        Stage currentStage = (Stage) emailField.getScene().getWindow();
        showSkeletonThenLoad(currentStage, email);
    }

    private void showSkeletonThenLoad(Stage stage, String email) {
        // Build skeleton screen
        VBox skeleton = buildSkeleton();
        Scene skeletonScene = new Scene(skeleton, stage.getScene().getWidth(), stage.getScene().getHeight());
        stage.setScene(skeletonScene);

        // Fade in skeleton
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), skeleton);
        fadeIn.setFromValue(0); fadeIn.setToValue(1); fadeIn.play();

        // Wait then load real screen
        PauseTransition pause = new PauseTransition(Duration.millis(900));
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

        // Icon pill skeleton
        javafx.scene.layout.StackPane iconPill = new javafx.scene.layout.StackPane();
        iconPill.setPrefHeight(65); iconPill.setMaxWidth(Double.MAX_VALUE);
        iconPill.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 35;");
        addShimmer(iconPill);

        // Title skeleton
        javafx.scene.layout.StackPane titleBar = new javafx.scene.layout.StackPane();
        titleBar.setPrefHeight(22); titleBar.setPrefWidth(160); titleBar.setMaxWidth(160);
        titleBar.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 6;");
        addShimmer(titleBar);
        javafx.scene.layout.HBox titleBox = new javafx.scene.layout.HBox(titleBar);
        titleBox.setAlignment(javafx.geometry.Pos.CENTER);

        // Subtitle skeleton
        javafx.scene.layout.StackPane subBar = new javafx.scene.layout.StackPane();
        subBar.setPrefHeight(14); subBar.setMaxWidth(Double.MAX_VALUE);
        subBar.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 6;");
        addShimmer(subBar);

        // Input skeleton
        javafx.scene.layout.StackPane inputBar = new javafx.scene.layout.StackPane();
        inputBar.setPrefHeight(38); inputBar.setMaxWidth(Double.MAX_VALUE);
        inputBar.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 8;");
        addShimmer(inputBar);

        // Button skeletons
        javafx.scene.layout.StackPane btn1 = new javafx.scene.layout.StackPane();
        btn1.setPrefHeight(38); btn1.setMaxWidth(Double.MAX_VALUE);
        btn1.setStyle("-fx-background-color: #e8c0c0; -fx-background-radius: 25;");
        addShimmer(btn1);

        javafx.scene.layout.StackPane btn2 = new javafx.scene.layout.StackPane();
        btn2.setPrefHeight(38); btn2.setMaxWidth(Double.MAX_VALUE);
        btn2.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 25;");
        addShimmer(btn2);

        VBox btnBox = new VBox(8, btn1, btn2);

        box.getChildren().addAll(iconPill, titleBox, subBar, inputBar, btnBox);
        return box;
    }

    private void addShimmer(javafx.scene.layout.StackPane pane) {
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