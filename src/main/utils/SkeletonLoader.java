package main.utils;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * SkeletonLoader — Full-screen animated skeleton overlay.
 *
 * Usage:
 *   SkeletonLoader.show(skeletonContainer);   // table-area skeleton only
 *   SkeletonLoader.showFullScreen(rootPane);  // full screen (sidebar + content)
 *   SkeletonLoader.hide(container);
 *   SkeletonLoader.hideFullScreen(rootPane);
 */
public class SkeletonLoader {

    private static final String BASE_COLOR    = "#e0e0e0";
    private static final String SHIMMER_COLOR = "#f5f5f5";
    private static final int    ROW_COUNT     = 7;
    private static final int    ROW_HEIGHT    = 32;
    private static final int    ROW_SPACING   = 8;

    // ── Full-Screen Skeleton ──────────────────────────────────────────────────

    /**
     * Overlays a full-screen skeleton (sidebar + content area) on top of the
     * given root StackPane. The root must be a StackPane or BorderPane wrapped
     * in a StackPane so the overlay can sit above everything.
     *
     * @param rootStack  The top-level StackPane of the scene's root.
     */
    public static void showFullScreen(StackPane rootStack) {
        rootStack.getChildren().removeIf(n -> "fullscreen-skeleton".equals(n.getId()));

        HBox overlay = new HBox();
        overlay.setId("fullscreen-skeleton");

        // ── Sidebar skeleton (maroon) ─────────────────────────────────────
        VBox sidebar = buildSidebarPanel();
        sidebar.setPrefWidth(220);
        sidebar.setMinWidth(220);
        sidebar.setMaxWidth(220);

        // ── Content skeleton (white) ──────────────────────────────────────
        VBox content = buildContentPanel();
        HBox.setHgrow(content, Priority.ALWAYS);

        overlay.getChildren().addAll(sidebar, content);

        // Place on top of everything
        StackPane.setAlignment(overlay, Pos.TOP_LEFT);
        rootStack.getChildren().add(overlay);

        // Build and play shimmer on both panels
        Timeline shimmer = buildFullScreenShimmer(overlay);
        shimmer.play();
        overlay.setUserData(shimmer);
    }

    public static void hideFullScreen(StackPane rootStack) {
        Node overlay = rootStack.getChildren().stream()
                .filter(n -> "fullscreen-skeleton".equals(n.getId()))
                .findFirst().orElse(null);

        if (overlay == null) return;

        // Stop shimmer
        Object data = overlay.getUserData();
        if (data instanceof Timeline t) t.stop();

        // Fade out then remove
        FadeTransition fade = new FadeTransition(Duration.millis(150), overlay);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(e -> rootStack.getChildren().remove(overlay));
        fade.play();
    }

    // ── Sidebar Panel ─────────────────────────────────────────────────────────

    private static VBox buildSidebarPanel() {
        VBox sidebar = new VBox();
        sidebar.setStyle("-fx-background-color: #8B0000;");
        sidebar.setPadding(new Insets(0));

        // Logo / brand area at top
        HBox brand = new HBox(10);
        brand.setPadding(new Insets(24, 16, 24, 16));
        brand.setAlignment(Pos.CENTER_LEFT);
        Rectangle logoBox = new Rectangle(36, 36);
        logoBox.setFill(Color.web("rgba(255,255,255,0.25)"));
        logoBox.setArcWidth(8); logoBox.setArcHeight(8);
        Rectangle brandText = new Rectangle(100, 14);
        brandText.setFill(Color.web("rgba(255,255,255,0.25)"));
        brandText.setArcWidth(6); brandText.setArcHeight(6);
        brand.getChildren().addAll(logoBox, brandText);
        sidebar.getChildren().add(brand);

        // Divider
        Region div = new Region();
        div.setPrefHeight(1);
        div.setStyle("-fx-background-color: rgba(255,255,255,0.15);");
        sidebar.getChildren().add(div);

        // Nav button skeletons
        VBox nav = new VBox(4);
        nav.setPadding(new Insets(16, 8, 16, 8));
        String[] labels = {"Dashboard", "Pass Slip Issuance", "Reports"};
        double[] labelWidths = {100, 140, 80};
        for (int i = 0; i < labels.length; i++) {
            HBox btn = new HBox(10);
            btn.setAlignment(Pos.CENTER_LEFT);
            btn.setPadding(new Insets(12, 14, 12, 14));
            btn.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 8;");

            Rectangle icon = new Rectangle(18, 18);
            icon.setFill(Color.web("rgba(255,255,255,0.30)"));
            icon.setArcWidth(4); icon.setArcHeight(4);

            Rectangle label = new Rectangle(labelWidths[i], 12);
            label.setFill(Color.web("rgba(255,255,255,0.25)"));
            label.setArcWidth(6); label.setArcHeight(6);

            btn.getChildren().addAll(icon, label);
            nav.getChildren().add(btn);
        }
        sidebar.getChildren().add(nav);

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        // User info at bottom
        HBox userRow = new HBox(10);
        userRow.setAlignment(Pos.CENTER_LEFT);
        userRow.setPadding(new Insets(16, 16, 24, 16));
        Rectangle avatar = new Rectangle(36, 36);
        avatar.setFill(Color.web("rgba(255,255,255,0.25)"));
        avatar.setArcWidth(18); avatar.setArcHeight(18);
        VBox userInfo = new VBox(6);
        Rectangle userName = new Rectangle(90, 11);
        userName.setFill(Color.web("rgba(255,255,255,0.25)"));
        userName.setArcWidth(6); userName.setArcHeight(6);
        Rectangle userRole = new Rectangle(60, 9);
        userRole.setFill(Color.web("rgba(255,255,255,0.18)"));
        userRole.setArcWidth(6); userRole.setArcHeight(6);
        userInfo.getChildren().addAll(userName, userRole);
        userRow.getChildren().addAll(avatar, userInfo);
        sidebar.getChildren().add(userRow);

        return sidebar;
    }

    // ── Content Panel ─────────────────────────────────────────────────────────

    private static VBox buildContentPanel() {
        VBox content = new VBox(0);
        content.setStyle("-fx-background-color: #f4f6f9;");

        // Top bar
        HBox topBar = new HBox(12);
        topBar.setPadding(new Insets(18, 24, 18, 24));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: white; -fx-border-color: transparent transparent #eeeeee transparent; -fx-border-width: 1;");
        Rectangle pageTitle = new Rectangle(160, 18);
        pageTitle.setFill(Color.web(BASE_COLOR));
        pageTitle.setArcWidth(6); pageTitle.setArcHeight(6);
        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);
        Rectangle notifBtn = new Rectangle(32, 32);
        notifBtn.setFill(Color.web(BASE_COLOR));
        notifBtn.setArcWidth(16); notifBtn.setArcHeight(16);
        Rectangle avatarSmall = new Rectangle(32, 32);
        avatarSmall.setFill(Color.web(BASE_COLOR));
        avatarSmall.setArcWidth(16); avatarSmall.setArcHeight(16);
        topBar.getChildren().addAll(pageTitle, topSpacer, notifBtn, avatarSmall);
        content.getChildren().add(topBar);

        // Stat cards row
        HBox cards = new HBox(14);
        cards.setPadding(new Insets(20, 24, 0, 24));
        for (int i = 0; i < 4; i++) {
            VBox card = new VBox(8);
            card.setPadding(new Insets(16));
            card.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
            HBox.setHgrow(card, Priority.ALWAYS);
            Rectangle cardLabel = new Rectangle(70, 10);
            cardLabel.setFill(Color.web(BASE_COLOR));
            cardLabel.setArcWidth(4); cardLabel.setArcHeight(4);
            Rectangle cardValue = new Rectangle(50, 22);
            cardValue.setFill(Color.web(BASE_COLOR));
            cardValue.setArcWidth(4); cardValue.setArcHeight(4);
            card.getChildren().addAll(cardLabel, cardValue);
            cards.getChildren().add(card);
        }
        content.getChildren().add(cards);

        // Search + filter bar
        HBox filterBar = new HBox(10);
        filterBar.setPadding(new Insets(18, 24, 12, 24));
        filterBar.setAlignment(Pos.CENTER_LEFT);
        Rectangle searchBox = new Rectangle(220, 34);
        searchBox.setFill(Color.web(BASE_COLOR));
        searchBox.setArcWidth(8); searchBox.setArcHeight(8);
        Rectangle filterBox = new Rectangle(130, 34);
        filterBox.setFill(Color.web(BASE_COLOR));
        filterBox.setArcWidth(8); filterBox.setArcHeight(8);
        Region fSpacer = new Region();
        HBox.setHgrow(fSpacer, Priority.ALWAYS);
        Rectangle actionBtn = new Rectangle(110, 34);
        actionBtn.setFill(Color.web("#c9a0a0"));
        actionBtn.setArcWidth(8); actionBtn.setArcHeight(8);
        filterBar.getChildren().addAll(searchBox, filterBox, fSpacer, actionBtn);
        content.getChildren().add(filterBar);

        // Table header
        HBox tableHeader = new HBox(8);
        tableHeader.setPadding(new Insets(10, 24, 10, 24));
        tableHeader.setStyle("-fx-background-color: #f0f0f0;");
        double[] headerWidths = {70, 110, 100, 120, 80, 80, 90, 80};
        for (double w : headerWidths) {
            Rectangle h = new Rectangle(w, 14);
            h.setFill(Color.web("#d0d0d0"));
            h.setArcWidth(4); h.setArcHeight(4);
            tableHeader.getChildren().add(h);
        }
        content.getChildren().add(tableHeader);

        // Table rows
        VBox tableBody = new VBox(0);
        VBox.setVgrow(tableBody, Priority.ALWAYS);
        for (int i = 0; i < ROW_COUNT; i++) {
            HBox row = new HBox(8);
            row.setPadding(new Insets(10, 24, 10, 24));
            row.setStyle("-fx-background-color: " + (i % 2 == 0 ? "white" : "#fafafa") + ";"
                    + "-fx-border-color: transparent transparent #f0f0f0 transparent; -fx-border-width: 1;");
            row.setAlignment(Pos.CENTER_LEFT);
            for (double w : headerWidths) {
                Rectangle cell = new Rectangle(w, ROW_HEIGHT - 10);
                cell.setFill(Color.web(BASE_COLOR));
                cell.setArcWidth(4); cell.setArcHeight(4);
                row.getChildren().add(cell);
            }
            tableBody.getChildren().add(row);
        }
        VBox.setVgrow(tableBody, Priority.ALWAYS);
        content.getChildren().add(tableBody);

        return content;
    }

    // ── Shimmer Animation ─────────────────────────────────────────────────────

    private static Timeline buildFullScreenShimmer(HBox overlay) {
        Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.setAutoReverse(true);

        Color base   = Color.web(BASE_COLOR);
        Color shimmer = Color.web(SHIMMER_COLOR);
        Color sBase  = Color.web("rgba(255,255,255,0.22)");
        Color sShine = Color.web("rgba(255,255,255,0.42)");

        collectRects(overlay, timeline, base, shimmer, sBase, sShine);
        return timeline;
    }

    private static void collectRects(Node node, Timeline tl,
                                     Color base, Color shimmer,
                                     Color sBase, Color sShine) {
        if (node instanceof Rectangle rect) {
            // Determine if it's inside the sidebar (maroon) panel by checking fill
            Color fill = (Color) rect.getFill();
            boolean isSidebar = fill.getRed() < 0.5 && fill.getGreen() < 0.5 && fill.getOpacity() < 1.0;
            Color from = isSidebar ? sBase  : base;
            Color to   = isSidebar ? sShine : shimmer;
            tl.getKeyFrames().addAll(
                    new KeyFrame(Duration.ZERO,          new KeyValue(rect.fillProperty(), from)),
                    new KeyFrame(Duration.millis(500),   new KeyValue(rect.fillProperty(), to))
            );
        } else if (node instanceof Pane pane) {
            for (Node child : pane.getChildren()) {
                collectRects(child, tl, base, shimmer, sBase, sShine);
            }
        }
    }

    // ── Table-Area-Only Skeleton (legacy / still used for refresh) ────────────

    public static Timeline show(StackPane container) {
        container.getChildren().removeIf(n -> "skeleton-overlay".equals(n.getId()));

        VBox skeleton = buildSkeleton();
        skeleton.setId("skeleton-overlay");
        skeleton.setStyle("-fx-background-color: white; -fx-padding: 14 10 10 10;");
        VBox.setVgrow(skeleton, Priority.ALWAYS);
        container.getChildren().add(skeleton);

        Timeline shimmer = buildShimmerAnimation(skeleton);
        shimmer.play();
        skeleton.setUserData(shimmer);
        return shimmer;
    }

    public static void hide(StackPane container) {
        Node overlay = container.getChildren().stream()
                .filter(n -> "skeleton-overlay".equals(n.getId()))
                .findFirst().orElse(null);
        if (overlay == null) return;

        Object data = overlay.getUserData();
        if (data instanceof Timeline t) t.stop();

        FadeTransition fade = new FadeTransition(Duration.millis(150), overlay);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(e -> container.getChildren().remove(overlay));
        fade.play();
    }

    private static VBox buildSkeleton() {
        VBox box = new VBox(ROW_SPACING);
        box.setPadding(new Insets(8, 10, 8, 10));
        HBox header = buildRow(new double[]{80, 100, 110, 110, 80, 80, 85, 80}, 20);
        header.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 8 6; -fx-background-radius: 4;");
        box.getChildren().add(header);
        for (int i = 0; i < ROW_COUNT; i++) {
            HBox row = buildRow(new double[]{80, 100, 110, 110, 80, 80, 85, 80}, ROW_HEIGHT);
            row.setStyle("-fx-padding: 4 6; -fx-border-color: transparent transparent #f0f0f0 transparent; -fx-border-width: 1;");
            box.getChildren().add(row);
        }
        return box;
    }

    private static HBox buildRow(double[] widths, int height) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        for (double w : widths) {
            Rectangle rect = new Rectangle(w, height - 8);
            rect.setFill(Color.web(BASE_COLOR));
            rect.setArcWidth(6); rect.setArcHeight(6);
            row.getChildren().add(rect);
        }
        return row;
    }

    private static Timeline buildShimmerAnimation(VBox skeleton) {
        Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.setAutoReverse(true);
        for (Node rowNode : skeleton.getChildren()) {
            if (rowNode instanceof HBox row) {
                for (Node cell : row.getChildren()) {
                    if (cell instanceof Rectangle rect) {
                        timeline.getKeyFrames().addAll(
                                new KeyFrame(Duration.ZERO,        new KeyValue(rect.fillProperty(), Color.web(BASE_COLOR))),
                                new KeyFrame(Duration.millis(500), new KeyValue(rect.fillProperty(), Color.web(SHIMMER_COLOR)))
                        );
                    }
                }
            }
        }
        return timeline;
    }

    // ── Sidebar-only skeleton (kept for backwards compat) ─────────────────────

    public static Timeline showSidebar(StackPane container) {
        container.getChildren().removeIf(n -> "sidebar-skeleton".equals(n.getId()));
        VBox skeleton = buildSidebarSkeleton();
        skeleton.setId("sidebar-skeleton");
        VBox.setVgrow(skeleton, Priority.ALWAYS);
        container.getChildren().add(skeleton);
        Timeline shimmer = buildSidebarShimmer(skeleton);
        shimmer.play();
        skeleton.setUserData(shimmer);
        return shimmer;
    }

    public static void hideSidebar(StackPane container) {
        container.getChildren().removeIf(n -> {
            if ("sidebar-skeleton".equals(n.getId())) {
                Object data = n.getUserData();
                if (data instanceof Timeline t) t.stop();
                return true;
            }
            return false;
        });
    }

    private static VBox buildSidebarSkeleton() {
        VBox box = new VBox(6);
        box.setPadding(new Insets(6, 0, 0, 0));
        double[] navWidths = {120, 140, 90};
        for (double w : navWidths) {
            HBox row = new HBox();
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10, 12, 10, 12));
            Rectangle icon = new Rectangle(16, 16);
            icon.setFill(Color.web("rgba(255,255,255,0.25)"));
            icon.setArcWidth(4); icon.setArcHeight(4);
            Rectangle label = new Rectangle(w, 12);
            label.setFill(Color.web("rgba(255,255,255,0.25)"));
            label.setArcWidth(6); label.setArcHeight(6);
            HBox.setMargin(label, new Insets(0, 0, 0, 10));
            row.getChildren().addAll(icon, label);
            box.getChildren().add(row);
        }
        return box;
    }

    private static Timeline buildSidebarShimmer(VBox skeleton) {
        Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.setAutoReverse(true);
        Color base  = Color.web("rgba(255,255,255,0.20)");
        Color shine = Color.web("rgba(255,255,255,0.38)");
        for (Node rowNode : skeleton.getChildren()) {
            if (rowNode instanceof HBox row) {
                for (Node cell : row.getChildren()) {
                    if (cell instanceof Rectangle rect) {
                        timeline.getKeyFrames().addAll(
                                new KeyFrame(Duration.ZERO,        new KeyValue(rect.fillProperty(), base)),
                                new KeyFrame(Duration.millis(500), new KeyValue(rect.fillProperty(), shine))
                        );
                    }
                }
            }
        }
        return timeline;
    }

    public static StackPane wrap(Region content) {
        StackPane stack = new StackPane();
        VBox.setVgrow(stack, Priority.ALWAYS);
        stack.getChildren().add(content);
        return stack;
    }

    public static void showThen(StackPane container, long minDisplayMs, Runnable onDone) {
        show(container);
        javafx.application.Platform.runLater(() -> {
            javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>() {
                @Override protected Void call() throws Exception { Thread.sleep(minDisplayMs); return null; }
                @Override protected void succeeded() { hide(container); if (onDone != null) onDone.run(); }
            };
            new Thread(task, "SkeletonTimer").start();
        });
    }
}