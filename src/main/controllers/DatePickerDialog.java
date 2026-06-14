package main.controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class DatePickerDialog {

    private static final String   BRAND  = "#8B0000";
    private static final Color    BRAND_CLR = Color.web(BRAND);
    private static final String[] DAYS   = {"Su","Mo","Tu","We","Th","Fr","Sa"};
    private static final String[] MONTHS = {
        "January","February","March","April","May","June",
        "July","August","September","October","November","December"
    };
    // Fixed dialog dimensions — stage never resizes, so OS never repositions
    private static final double DIALOG_W = 320;
    private static final double DIALOG_H = 500;

    private LocalDate result   = null;
    private LocalDate selected;
    private YearMonth viewMonth;
    private GridPane  calGrid;
    private Label     lblMonthYear;
    private Stage     dialog;

    public static LocalDate show(Stage owner, LocalDate initial) {
        return new DatePickerDialog().showDialog(owner, initial);
    }

    private LocalDate showDialog(Stage owner, LocalDate initial) {
        selected  = initial != null ? initial : LocalDate.now();
        viewMonth = YearMonth.from(selected);

        dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color:white; -fx-background-radius:16; -fx-border-radius:16; " +
                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.25),20,0,0,6);");
        root.setAlignment(Pos.CENTER);

        // ── Header ──
        HBox header = new HBox();
        header.setPadding(new Insets(16, 24, 10, 24));
        header.setStyle("-fx-background-color:" + BRAND + "; -fx-background-radius:16 16 0 0;");
        Label lblSelect = new Label("SELECT DATE");
        lblSelect.setStyle("-fx-text-fill:rgba(255,255,255,0.75); -fx-font-size:11px; -fx-font-weight:bold;");
        header.getChildren().add(lblSelect);

        // ── Date Display ──
        HBox dateDisplay = new HBox();
        dateDisplay.setAlignment(Pos.CENTER_LEFT);
        dateDisplay.setPadding(new Insets(8, 24, 14, 24));
        dateDisplay.setStyle("-fx-background-color:" + BRAND + ";");
        Label lblDate = new Label(selected.format(DateTimeFormatter.ofPattern("EEE, MMM d yyyy")));
        lblDate.setFont(Font.font("System", FontWeight.BOLD, 22));
        lblDate.setTextFill(Color.WHITE);
        dateDisplay.getChildren().add(lblDate);

        // ── Month Navigation ──
        HBox nav = new HBox(10);
        nav.setAlignment(Pos.CENTER);
        nav.setPadding(new Insets(12, 20, 6, 20));

        Button btnPrev = navBtn("❮");
        lblMonthYear = new Label();
        lblMonthYear.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblMonthYear.setStyle("-fx-text-fill:#1a1a1a;");
        HBox.setHgrow(lblMonthYear, Priority.ALWAYS);
        lblMonthYear.setMaxWidth(Double.MAX_VALUE);
        lblMonthYear.setAlignment(Pos.CENTER);
        Button btnNext = navBtn("❯");

        btnPrev.setOnAction(e -> {
            viewMonth = viewMonth.minusMonths(1);
            buildCalendar(lblDate);
            updateNavButtons(btnPrev, btnNext);
        });
        btnNext.setOnAction(e -> {
            YearMonth next = viewMonth.plusMonths(1);
            if (!next.isAfter(YearMonth.now())) {
                viewMonth = next;
                buildCalendar(lblDate);
                updateNavButtons(btnPrev, btnNext);
            }
        });
        updateNavButtons(btnPrev, btnNext);
        nav.getChildren().addAll(btnPrev, lblMonthYear, btnNext);

        // ── Day Headers ──
        GridPane dayHeaders = new GridPane();
        dayHeaders.setPadding(new Insets(0, 16, 4, 16));
        dayHeaders.setHgap(4);
        for (int i = 0; i < 7; i++) {
            Label d = new Label(DAYS[i]);
            d.setStyle("-fx-font-size:11px; -fx-text-fill:#888; -fx-font-weight:bold;");
            d.setPrefWidth(36); d.setAlignment(Pos.CENTER);
            dayHeaders.add(d, i, 0);
        }

        // ── Calendar Grid ──
        calGrid = new GridPane();
        calGrid.setPadding(new Insets(0, 16, 8, 16));
        calGrid.setHgap(4); calGrid.setVgap(4);
        buildCalendar(lblDate);

        // ── Spacer: pushes footer to bottom so it looks good for 4/5/6-row months ──
        Region vSpacer = new Region();
        VBox.setVgrow(vSpacer, Priority.ALWAYS);

        // ── Footer ──
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(8, 20, 14, 20));
        footer.setStyle("-fx-border-color:#f0f0f0; -fx-border-width:1 0 0 0;");

        Button btnCancel = new Button("CANCEL");
        btnCancel.setStyle("-fx-background-color:transparent; -fx-text-fill:" + BRAND +
                "; -fx-font-weight:bold; -fx-font-size:13px; -fx-cursor:hand; -fx-border-width:0;");
        btnCancel.setOnAction(e -> { result = null; dialog.close(); });

        Button btnOK = new Button("OK");
        btnOK.setStyle("-fx-background-color:transparent; -fx-text-fill:" + BRAND +
                "; -fx-font-weight:bold; -fx-font-size:13px; -fx-cursor:hand; -fx-border-width:0;");
        btnOK.setOnAction(e -> { result = selected; dialog.close(); });

        footer.getChildren().addAll(btnCancel, btnOK);
        root.getChildren().addAll(header, dateDisplay, nav, dayHeaders, calGrid, vSpacer, footer);

        // Fixed scene size — stage never resizes, OS never repositions on month change
        Scene scene = new Scene(root, DIALOG_W, DIALOG_H);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.setWidth(DIALOG_W);
        dialog.setHeight(DIALOG_H);

        // Center on owner window, clamped to screen
        dialog.setOnShown(e -> {
            Rectangle2D screen = Screen.getPrimary().getVisualBounds();
            double dx = owner.getX() + (owner.getWidth()  - DIALOG_W) / 2;
            double dy = owner.getY() + (owner.getHeight() - DIALOG_H) / 2;
            dialog.setX(Math.max(screen.getMinX(), Math.min(dx, screen.getMaxX() - DIALOG_W)));
            dialog.setY(Math.max(screen.getMinY() + 30, Math.min(dy, screen.getMaxY() - DIALOG_H)));
        });

        dialog.showAndWait();
        return result;
    }

    private void buildCalendar(Label lblDate) {
        calGrid.getChildren().clear();
        lblMonthYear.setText(MONTHS[viewMonth.getMonthValue() - 1] + " " + viewMonth.getYear());

        LocalDate first     = viewMonth.atDay(1);
        int       startDay  = first.getDayOfWeek().getValue() % 7; // 0 = Sun
        int       daysInMonth = viewMonth.lengthOfMonth();
        LocalDate today     = LocalDate.now();

        int row = 0, col = startDay;
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = viewMonth.atDay(day);
            Button btn = new Button(String.valueOf(day));
            btn.setPrefSize(36, 36);
            btn.setMinSize(36, 36);

            boolean isSelected = date.equals(selected);
            boolean isToday    = date.equals(today);
            boolean isFuture   = date.isAfter(today);

            if (isSelected) {
                btn.setStyle("-fx-background-color:" + BRAND + "; -fx-text-fill:white; " +
                        "-fx-background-radius:18; -fx-font-weight:bold; -fx-font-size:12px; -fx-cursor:hand; -fx-border-width:0;");
            } else if (isToday) {
                btn.setStyle("-fx-background-color:transparent; -fx-text-fill:" + BRAND + "; " +
                        "-fx-border-color:" + BRAND + "; -fx-border-width:1.5; " +
                        "-fx-background-radius:18; -fx-border-radius:18; -fx-font-weight:bold; -fx-font-size:12px; -fx-cursor:hand;");
            } else if (isFuture) {
                btn.setStyle("-fx-background-color:transparent; -fx-text-fill:#ccc; " +
                        "-fx-background-radius:18; -fx-font-size:12px; -fx-cursor:default; -fx-border-width:0;");
                btn.setDisable(true);
            } else {
                btn.setStyle("-fx-background-color:transparent; -fx-text-fill:#333; " +
                        "-fx-background-radius:18; -fx-font-size:12px; -fx-cursor:hand; -fx-border-width:0;");
            }

            LocalDate finalDate = date;
            btn.setOnAction(e -> {
                selected = finalDate;
                lblDate.setText(selected.format(DateTimeFormatter.ofPattern("EEE, MMM d yyyy")));
                buildCalendar(lblDate);
            });

            calGrid.add(btn, col, row);
            col++;
            if (col == 7) { col = 0; row++; }
        }
    }

    private void updateNavButtons(Button btnPrev, Button btnNext) {
        boolean atCurrentMonth = viewMonth.equals(YearMonth.now());
        btnNext.setDisable(atCurrentMonth);
        btnNext.setStyle(navBtnStyle(!atCurrentMonth));
    }

    private Button navBtn(String text) {
        Button btn = new Button(text);
        btn.setStyle(navBtnStyle(true));
        return btn;
    }

    private String navBtnStyle(boolean enabled) {
        String color = enabled ? BRAND : "#ccc";
        return "-fx-background-color:transparent; -fx-text-fill:" + color + "; " +
                "-fx-font-size:14px; -fx-font-weight:bold; -fx-cursor:" + (enabled ? "hand" : "default") +
                "; -fx-border-width:0; -fx-padding:4 10;";
    }
}
