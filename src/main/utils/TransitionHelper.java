package main.utils;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * TransitionHelper — reusable fade transition for panel changes.
 *
 * Usage:
 *   TransitionHelper.navigateTo(stage, "/main/resources/fxml/Reports.fxml", "Reports");
 */
public class TransitionHelper {

    private static final int FADE_MS = 250; // ms — snappy, not slow

    /**
     * Navigate to a new FXML panel with fade transition.
     * Preserves the current stage dimensions.
     *
     * @return the loaded controller (cast as needed)
     */
    public static Object navigateTo(Stage stage, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(TransitionHelper.class.getResource(fxmlPath));
            Parent root = loader.load();

            double w = stage.getWidth();
            double h = stage.getHeight();

            root.setOpacity(0);
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.setWidth(w);
            stage.setHeight(h);

            FadeTransition ft = new FadeTransition(Duration.millis(FADE_MS), root);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            ft.play();

            return loader.getController();

        } catch (Exception e) {
            System.out.println("[TransitionHelper] Nav error to " + fxmlPath + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Fade-in the root of the current scene (call in initialize()).
     */
    public static void fadeIn(Parent root) {
        if (root == null) return;
        root.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(300), root);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }
}
