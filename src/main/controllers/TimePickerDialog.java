package main.controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.time.LocalTime;

public class TimePickerDialog {

    // ── Brand color ──
    private static final String BRAND       = "#8B0000";
    private static final Color  BRAND_COLOR = Color.web(BRAND);

    private int     selectedHour;
    private int     selectedMinute;
    private boolean isAM;
    private boolean pickingHour = true;
    private LocalTime result    = null;

    private Canvas clockCanvas;
    private Label  lblHour, lblMinute, lblAM, lblPM;
    private Stage  dialog;

    public static LocalTime show(Stage owner, LocalTime initial) {
        return new TimePickerDialog().showDialog(owner, initial);
    }

    private LocalTime showDialog(Stage owner, LocalTime initial) {
        selectedHour   = initial.getHour() % 12 == 0 ? 12 : initial.getHour() % 12;
        selectedMinute = initial.getMinute();
        isAM           = initial.getHour() < 12;

        dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-border-radius: 16; " +
                "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.25),20,0,0,6);");
        root.setPrefWidth(340);
        root.setAlignment(Pos.CENTER);

        // ── Header ──
        HBox header = new HBox();
        header.setPadding(new Insets(16, 24, 10, 24));
        header.setStyle("-fx-background-color:" + BRAND + "; -fx-background-radius: 16 16 0 0;");
        Label lblSelect = new Label("SELECT TIME");
        lblSelect.setStyle("-fx-text-fill:rgba(255,255,255,0.75); -fx-font-size:11px; -fx-font-weight:bold;");
        header.getChildren().add(lblSelect);

        // ── Time Display ──
        HBox timeDisplay = new HBox(0);
        timeDisplay.setAlignment(Pos.CENTER_LEFT);
        timeDisplay.setPadding(new Insets(8, 24, 14, 24));
        timeDisplay.setStyle("-fx-background-color:" + BRAND + ";");

        lblHour = new Label(String.format("%02d", selectedHour));
        lblHour.setFont(Font.font("System", FontWeight.BOLD, 52));
        lblHour.setStyle("-fx-background-color:rgba(255,255,255,0.2); -fx-background-radius:8; -fx-padding:4 10; -fx-cursor:hand;");
        lblHour.setTextFill(Color.WHITE);

        Label colon = new Label(":");
        colon.setFont(Font.font("System", FontWeight.BOLD, 48));
        colon.setTextFill(Color.WHITE);
        colon.setPadding(new Insets(0, 4, 0, 4));

        lblMinute = new Label(String.format("%02d", selectedMinute));
        lblMinute.setFont(Font.font("System", FontWeight.BOLD, 52));
        lblMinute.setTextFill(Color.web("rgba(255,255,255,0.6)"));
        lblMinute.setStyle("-fx-background-radius:8; -fx-padding:4 10; -fx-cursor:hand;");

        // AM/PM
        VBox ampmBox = new VBox(4);
        ampmBox.setAlignment(Pos.CENTER);
        ampmBox.setPadding(new Insets(0, 0, 0, 12));
        lblAM = new Label("AM"); lblPM = new Label("PM");
        String amOn  = "-fx-background-color:rgba(255,255,255,0.25); -fx-background-radius:6; -fx-text-fill:white; -fx-font-size:13px; -fx-font-weight:bold; -fx-padding:3 8; -fx-cursor:hand;";
        String amOff = "-fx-background-color:transparent; -fx-text-fill:rgba(255,255,255,0.55); -fx-font-size:13px; -fx-padding:3 8; -fx-cursor:hand;";
        lblAM.setStyle(isAM ? amOn : amOff); lblPM.setStyle(isAM ? amOff : amOn);
        lblAM.setOnMouseClicked(e -> { isAM=true;  lblAM.setStyle(amOn);  lblPM.setStyle(amOff); });
        lblPM.setOnMouseClicked(e -> { isAM=false; lblPM.setStyle(amOn);  lblAM.setStyle(amOff); });
        ampmBox.getChildren().addAll(lblAM, lblPM);

        lblHour.setOnMouseClicked(e -> {
            pickingHour = true;
            lblHour  .setStyle("-fx-background-color:rgba(255,255,255,0.2); -fx-background-radius:8; -fx-padding:4 10; -fx-cursor:hand;"); lblHour  .setTextFill(Color.WHITE);
            lblMinute.setStyle("-fx-background-radius:8; -fx-padding:4 10; -fx-cursor:hand;"); lblMinute.setTextFill(Color.web("rgba(255,255,255,0.6)"));
            drawClock();
        });
        lblMinute.setOnMouseClicked(e -> {
            pickingHour = false;
            lblMinute.setStyle("-fx-background-color:rgba(255,255,255,0.2); -fx-background-radius:8; -fx-padding:4 10; -fx-cursor:hand;"); lblMinute.setTextFill(Color.WHITE);
            lblHour  .setStyle("-fx-background-radius:8; -fx-padding:4 10; -fx-cursor:hand;"); lblHour  .setTextFill(Color.web("rgba(255,255,255,0.6)"));
            drawClock();
        });

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        timeDisplay.getChildren().addAll(lblHour, colon, lblMinute, sp, ampmBox);

        // ── Clock ──
        clockCanvas = new Canvas(280, 280);
        VBox cw = new VBox(clockCanvas); cw.setAlignment(Pos.CENTER); cw.setPadding(new Insets(14, 24, 6, 24));
        clockCanvas.setOnMouseClicked(e -> handleClockClick(e.getX(), e.getY()));
        clockCanvas.setOnMouseDragged(e -> handleClockClick(e.getX(), e.getY()));

        // ── Footer ──
        HBox footer = new HBox(10); footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(8, 20, 14, 20));

        Button btnCancel = new Button("CANCEL");
        btnCancel.setStyle("-fx-background-color:transparent; -fx-text-fill:" + BRAND + "; -fx-font-weight:bold; -fx-font-size:13px; -fx-cursor:hand; -fx-border-width:0;");
        btnCancel.setOnAction(e -> { result = null; dialog.close(); });

        Button btnOK = new Button("OK");
        btnOK.setStyle("-fx-background-color:transparent; -fx-text-fill:" + BRAND + "; -fx-font-weight:bold; -fx-font-size:13px; -fx-cursor:hand; -fx-border-width:0;");
        btnOK.setOnAction(e -> {
            int h24 = isAM ? (selectedHour==12?0:selectedHour) : (selectedHour==12?12:selectedHour+12);
            result = LocalTime.of(h24, selectedMinute);
            dialog.close();
        });

        footer.getChildren().addAll(btnCancel, btnOK);
        root.getChildren().addAll(header, timeDisplay, cw, footer);

        Scene scene = new Scene(root); scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
        drawClock();
        dialog.showAndWait();
        return result;
    }

    private void drawClock() {
        GraphicsContext gc = clockCanvas.getGraphicsContext2D();
        double w=clockCanvas.getWidth(), h=clockCanvas.getHeight(), cx=w/2, cy=h/2;
        double r = Math.min(w,h)/2 - 8;
        gc.clearRect(0,0,w,h);

        gc.setFill(Color.web("#F0F0F0")); gc.fillOval(cx-r,cy-r,r*2,r*2);

        for (int i=1; i<=12; i++) {
            double angle = Math.toRadians(i*30.0-90);
            double nx = cx+(r-28)*Math.cos(angle), ny = cy+(r-28)*Math.sin(angle);
            String lbl = pickingHour ? String.valueOf(i) : String.format("%02d", i*5==60?0:i*5);
            boolean sel = pickingHour ? i==selectedHour : (i*5==selectedMinute||(i==12&&selectedMinute==0));
            if (sel) { gc.setFill(BRAND_COLOR); gc.fillOval(nx-18,ny-18,36,36); gc.setFill(Color.WHITE); }
            else     { gc.setFill(Color.web("#333")); }
            gc.setFont(Font.font("System", FontWeight.NORMAL, 14));
            gc.fillText(lbl, nx-lbl.length()*3.8, ny+5);
        }

        double handAngle = pickingHour ? Math.toRadians(selectedHour*30.0-90) : Math.toRadians(selectedMinute*6.0-90);
        double hx=cx+(r-30)*Math.cos(handAngle), hy=cy+(r-30)*Math.sin(handAngle);
        gc.setStroke(BRAND_COLOR); gc.setLineWidth(2); gc.strokeLine(cx,cy,hx,hy);
        gc.setFill(BRAND_COLOR); gc.fillOval(cx-4,cy-4,8,8);
    }

    private void handleClockClick(double mx, double my) {
        double cx=clockCanvas.getWidth()/2, cy=clockCanvas.getHeight()/2;
        double r=Math.min(clockCanvas.getWidth(),clockCanvas.getHeight())/2-8;
        double dx=mx-cx, dy=my-cy;
        if (Math.sqrt(dx*dx+dy*dy)>r) return;

        double angle = Math.toDegrees(Math.atan2(dy,dx))+90;
        if (angle<0) angle+=360;

        if (pickingHour) {
            int hour=(int)Math.round(angle/30.0); if(hour==0)hour=12; if(hour>12)hour=12;
            selectedHour=hour; lblHour.setText(String.format("%02d",selectedHour));
            drawClock();
            // auto-switch to minutes
            pickingHour=false;
            lblMinute.setStyle("-fx-background-color:rgba(255,255,255,0.2); -fx-background-radius:8; -fx-padding:4 10; -fx-cursor:hand;"); lblMinute.setTextFill(Color.WHITE);
            lblHour  .setStyle("-fx-background-radius:8; -fx-padding:4 10; -fx-cursor:hand;");                                            lblHour  .setTextFill(Color.web("rgba(255,255,255,0.6)"));
            drawClock();
        } else {
            int minute=(int)Math.round(angle/6.0); if(minute>=60)minute=0;
            selectedMinute=minute; lblMinute.setText(String.format("%02d",selectedMinute));
            drawClock();
        }
    }
}
