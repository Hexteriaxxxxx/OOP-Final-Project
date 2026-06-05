package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/main/resources/fxml/Login.fxml")));
        Scene scene = new Scene(root, 1280, 720);

        // Load global CSS to fix button border-radius
        scene.getStylesheets().add(
            Objects.requireNonNull(getClass().getResource("/main/resources/styles/app.css")).toExternalForm()
        );

        primaryStage.setTitle("Pass Slip Issuance System");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
