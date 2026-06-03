package main.controllers;

import dao.PassSlipDAO;
import dao.VisitorDAO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Popup;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared notification dropdown helper.
 * Used by Admin and Staff screens.
 *
 * ADMIN notifications:
 *   - Pending pass slips
 *   - Active employees outside
 *   - Pending visitor requests
 *
 * STAFF notifications:
 *   - Approved pass slips today
 *   - Active employees outside
 */
public class NotificationHelper {

    private static final String BELL_NORMAL = "-fx-background-color: transparent; -fx-font-size: 18px; -fx-text-fill: #888; -fx-cursor: hand; -fx-border-width: 0; -fx-padding: 4;";
    private static final String BELL_ACTIVE = "-fx-background-color: transparent; -fx-font-size: 18px; -fx-text-fill: #8B0000; -fx-cursor: hand; -fx-border-width: 0; -fx-padding: 4;";

    public enum Role { ADMIN, STAFF }

    private final Button bellBtn;
    private final Role   role;
    private Popup        popup;

    private static class NotifItem {
        String icon, text, time; boolean read;
        NotifItem(String icon, String text, String time) { this.icon=icon; this.text=text; this.time=time; }
    }

    private final List<NotifItem> notifications = new ArrayList<>();

    public NotificationHelper(Button bellBtn, Role role) {
        this.bellBtn = bellBtn;
        this.role    = role;
    }

    // ── Call this from @FXML handleNotification() ──────────────
    public void toggle() {
        refreshNotifications();

        if (popup != null && popup.isShowing()) {
            popup.hide();
            bellBtn.setStyle(BELL_NORMAL);
            return;
        }

        bellBtn.setStyle(BELL_ACTIVE);

        popup = new Popup();
        popup.setAutoHide(true);
        popup.setOnHidden(e -> bellBtn.setStyle(BELL_NORMAL));

        VBox container = new VBox(0);
        container.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-border-radius: 12; -fx-border-color: #e8e8e8; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 16, 0, 0, 4);");
        container.setPrefWidth(300);

        // ── Header ──
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 14, 10, 14));
        header.setStyle("-fx-border-color: #f0f0f0; -fx-border-width: 0 0 1 0;");

        Label title = new Label("🔔  Notifications");
        title.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
        HBox.setHgrow(title, Priority.ALWAYS);

        MenuButton menuBtn = new MenuButton("⋮");
        menuBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #888; -fx-font-size: 16px; -fx-cursor: hand; -fx-border-width: 0; -fx-padding: 0 4;");
        MenuItem markAll  = new MenuItem("✅  Mark all as read");
        MenuItem clearAll = new MenuItem("🗑  Clear all");
        markAll .setOnAction(e -> { notifications.forEach(n -> n.read = true); popup.hide(); bellBtn.setStyle(BELL_NORMAL); });
        clearAll.setOnAction(e -> { notifications.clear(); popup.hide(); bellBtn.setStyle(BELL_NORMAL); });
        menuBtn.getItems().addAll(markAll, clearAll);
        header.getChildren().addAll(title, menuBtn);

        // ── Items ──
        VBox itemsBox = new VBox(0);
        if (notifications.isEmpty()) {
            Label empty = new Label("✅  No new notifications");
            empty.setStyle("-fx-font-size: 12px; -fx-text-fill: #999; -fx-padding: 14 14;");
            itemsBox.getChildren().add(empty);
        } else {
            notifications.forEach(n -> itemsBox.getChildren().add(buildRow(n)));
        }

        ScrollPane scroll = new ScrollPane(itemsBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: white; -fx-background-color: white; -fx-border-width: 0;");
        scroll.setMaxHeight(320);

        container.getChildren().addAll(header, scroll);
        popup.getContent().add(container);

        javafx.geometry.Bounds bounds = bellBtn.localToScreen(bellBtn.getBoundsInLocal());
        popup.show(bellBtn.getScene().getWindow(), bounds.getMaxX() - 300, bounds.getMaxY() + 4);
    }

    private HBox buildRow(NotifItem notif) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        row.setStyle(notif.read
                ? "-fx-background-color: white; -fx-border-color: #f5f5f5; -fx-border-width: 0 0 1 0;"
                : "-fx-background-color: #FFF8F8; -fx-border-color: #f0f0f0; -fx-border-width: 0 0 1 0;");

        Circle dot = new Circle(4, notif.read ? Color.TRANSPARENT : Color.web("#8B0000"));

        VBox textBox = new VBox(2);
        HBox.setHgrow(textBox, Priority.ALWAYS);
        Label lblText = new Label(notif.icon + "  " + notif.text);
        lblText.setStyle("-fx-font-size: 12px; -fx-text-fill: " + (notif.read ? "#666" : "#1a1a1a") + ";");
        lblText.setWrapText(true);
        lblText.setMaxWidth(200);
        Label lblTime = new Label(notif.time);
        lblTime.setStyle("-fx-font-size: 10px; -fx-text-fill: #aaa;");
        textBox.getChildren().addAll(lblText, lblTime);

        MenuButton itemMenu = new MenuButton("⋮");
        itemMenu.setStyle("-fx-background-color: transparent; -fx-text-fill: #bbb; -fx-font-size: 14px; -fx-cursor: hand; -fx-border-width: 0; -fx-padding: 0 2;");
        MenuItem markRead = new MenuItem(notif.read ? "Mark as unread" : "Mark as read");
        MenuItem delete   = new MenuItem("🗑  Delete");
        markRead.setOnAction(e -> { notif.read = !notif.read; popup.hide(); bellBtn.setStyle(BELL_NORMAL); });
        delete  .setOnAction(e -> { notifications.remove(notif); popup.hide(); bellBtn.setStyle(BELL_NORMAL); });
        itemMenu.getItems().addAll(markRead, delete);

        row.setOnMouseClicked(e -> { notif.read = true; popup.hide(); bellBtn.setStyle(BELL_NORMAL); });
        row.getChildren().addAll(dot, textBox, itemMenu);
        return row;
    }

    private void refreshNotifications() {
        notifications.clear();
        try {
            PassSlipDAO passSlipDAO = new PassSlipDAO();
            List<?> all = passSlipDAO.getAllPassSlips();

            if (role == Role.ADMIN) {
                // ── Admin notifications ──
                long pending = ((List<models.PassSlip>) all).stream()
                        .filter(p -> "Pending".equalsIgnoreCase(p.getStatus())).count();
                int active = passSlipDAO.countActiveSlips();

                if (pending > 0)
                    notifications.add(new NotifItem("⏳",
                            pending + " pending pass slip" + (pending > 1 ? "s" : "") + " awaiting approval",
                            "Just now"));
                if (active > 0)
                    notifications.add(new NotifItem("📋",
                            active + " employee" + (active > 1 ? "s are" : " is") + " currently outside",
                            "Just now"));

                // Visitor notifications
                try {
                    VisitorDAO visitorDAO = new VisitorDAO();
                    int pendingVisitors = visitorDAO.countPending();
                    if (pendingVisitors > 0)
                        notifications.add(new NotifItem("👤",
                                pendingVisitors + " visitor request" + (pendingVisitors > 1 ? "s" : "") + " pending approval",
                                "Just now"));
                } catch (Exception ignored) {}

            } else {
                // ── Staff notifications ──
                List<models.PassSlip> today = passSlipDAO.getTodayPassSlips();
                long approved = today.stream()
                        .filter(p -> "Approved".equalsIgnoreCase(p.getStatus())).count();
                int active = passSlipDAO.countActiveSlips();

                if (approved > 0)
                    notifications.add(new NotifItem("✅",
                            approved + " pass slip" + (approved > 1 ? "s" : "") + " approved today",
                            "Today"));
                if (active > 0)
                    notifications.add(new NotifItem("📋",
                            active + " employee" + (active > 1 ? "s are" : " is") + " currently outside",
                            "Just now"));
            }

        } catch (Exception e) {
            System.out.println("NotificationHelper error: " + e.getMessage());
        }
    }
}
