package main.utils;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * SkeletonLoader — Animated skeleton placeholder for table loading states.
 *
 * Usage:
 *   StackPane wrapper = SkeletonLoader.wrap(tblPassSlips);
 *   SkeletonLoader.show(wrapper);       // before DB call
 *   SkeletonLoader.hide(wrapper);       // after DB call
 */
public class SkeletonLoader {

    private static final String BASE_COLOR    = "#e0e0e0";
    private static final String SHIMMER_COLOR = "#f5f5f5";
    private static final int    ROW_COUNT     = 6;
    private static final int    ROW_HEIGHT    = 32;
    private static final int    ROW_SPACING   = 8;

    /**
     * Creates a StackPane that wraps the given content with a skeleton overlay.
     * The skeleton is hidden by default — call show() to display it.
     */
    public static StackPane wrap(Region content) {
        StackPane stack = new StackPane();
        VBox.setVgrow(stack, Priority.ALWAYS);
        stack.getChildren().add(content);
        return stack;
    }

    /**
     * Shows the skeleton overlay on top of the content inside the StackPane.
     * Starts shimmer animation automatically.
     */
    public static Timeline show(StackPane container) {
        // Remove any existing skeleton first
        container.getChildren().removeIf(n -> "skeleton-overlay".equals(n.getId()));

        VBox skeleton = buildSkeleton();
        skeleton.setId("skeleton-overlay");
        skeleton.setStyle(
            "-fx-background-color: white;" +
            "-fx-padding: 14 10 10 10;"
        );
        VBox.setVgrow(skeleton, Priority.ALWAYS);
        container.getChildren().add(skeleton);

        Timeline shimmer = buildShimmerAnimation(skeleton);
        shimmer.play();
        skeleton.setUserData(shimmer); // store reference for cleanup
        return shimmer;
    }

    /**
     * Hides and removes the skeleton overlay from the StackPane.
     * Stops animation automatically.
     */
    public static void hide(StackPane container) {
        container.getChildren().removeIf(n -> {
            if ("skeleton-overlay".equals(n.getId())) {
                // Stop animation if running
                Object data = n.getUserData();
                if (data instanceof Timeline) ((Timeline) data).stop();
                return true;
            }
            return false;
        });
    }

    // ── Build skeleton rows ───────────────────────────────────────
    private static VBox buildSkeleton() {
        VBox box = new VBox(ROW_SPACING);
        box.setPadding(new Insets(8, 10, 8, 10));

        // Header row
        HBox header = buildRow(new double[]{80, 100, 110, 110, 80, 80, 85, 80}, 20);
        header.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 8 6; -fx-background-radius: 4;");
        box.getChildren().add(header);

        // Data rows
        for (int i = 0; i < ROW_COUNT; i++) {
            HBox row = buildRow(new double[]{80, 100, 110, 110, 80, 80, 85, 80}, ROW_HEIGHT);
            row.setStyle("-fx-padding: 4 6; -fx-border-color: transparent transparent #f0f0f0 transparent; -fx-border-width: 1;");
            box.getChildren().add(row);
        }

        return box;
    }

    private static HBox buildRow(double[] widths, int height) {
        HBox row = new HBox(8);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        for (double w : widths) {
            Rectangle rect = new Rectangle(w, height - 8);
            rect.setFill(Color.web(BASE_COLOR));
            rect.setArcWidth(6);
            rect.setArcHeight(6);
            row.getChildren().add(rect);
        }
        return row;
    }

    // ── Shimmer animation — cycles between BASE and SHIMMER color ─
    private static Timeline buildShimmerAnimation(VBox skeleton) {
        Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.setAutoReverse(true);

        // Animate all rectangles in all rows
        for (javafx.scene.Node rowNode : skeleton.getChildren()) {
            if (rowNode instanceof HBox row) {
                for (javafx.scene.Node cell : row.getChildren()) {
                    if (cell instanceof Rectangle rect) {
                        timeline.getKeyFrames().addAll(
                            new KeyFrame(Duration.ZERO,
                                new KeyValue(rect.fillProperty(), Color.web(BASE_COLOR))),
                            new KeyFrame(Duration.millis(800),
                                new KeyValue(rect.fillProperty(), Color.web(SHIMMER_COLOR)))
                        );
                    }
                }
            }
        }

        return timeline;
    }

    /**
     * Convenience: show skeleton for a minimum display time,
     * then run the provided Runnable (on FX thread) to load real data.
     *
     * Usage:
     *   SkeletonLoader.showThen(skeletonContainer, 600, () -> {
     *       loadDashboardData();
     *   });
     */
    public static void showThen(StackPane container, long minDisplayMs, Runnable onDone) {
        Timeline skeleton = show(container);
        javafx.application.Platform.runLater(() -> {
            javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>() {
                @Override protected Void call() throws Exception {
                    Thread.sleep(minDisplayMs); // min skeleton display time
                    return null;
                }
                @Override protected void succeeded() {
                    hide(container);
                    if (onDone != null) onDone.run();
                }
            };
            new Thread(task, "SkeletonTimer").start();
        });
    }
}
