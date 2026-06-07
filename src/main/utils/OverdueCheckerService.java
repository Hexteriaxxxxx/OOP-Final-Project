package main.utils;

import dao.PassSlipDAO;
import models.PassSlip;
import javafx.application.Platform;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

/**
 * Background service that checks every 60 seconds for overdue pass slips.
 * A pass slip is OVERDUE when:
 *   - Status is 'Approved'
 *   - time_in (expected return) is not null AND is in the past
 *   - Employee has not yet returned (no actual time_in recorded)
 *
 * When overdue slips are found, it:
 *   1. Updates their status to 'Overdue' in the DB
 *   2. Calls the onOverdueFound callback on the JavaFX thread
 */
public class OverdueCheckerService {

    private static final long CHECK_INTERVAL_MS = 60_000; // every 60 seconds

    private Timer timer;
    private final PassSlipDAO passSlipDAO = new PassSlipDAO();
    private Consumer<List<PassSlip>> onOverdueFound;

    public OverdueCheckerService(Consumer<List<PassSlip>> onOverdueFound) {
        this.onOverdueFound = onOverdueFound;
    }

    public void start() {
        if (timer != null) timer.cancel();
        timer = new Timer("OverdueChecker", true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() { checkOverdue(); }
        }, 5_000, CHECK_INTERVAL_MS); // first check after 5 seconds
        System.out.println("[OverdueChecker] Started — checking every 60 seconds");
    }

    public void stop() {
        if (timer != null) { timer.cancel(); timer = null; }
        System.out.println("[OverdueChecker] Stopped");
    }

    private void checkOverdue() {
        try {
            List<PassSlip> overdueSlips = passSlipDAO.getNewlyOverdueSlips();
            if (overdueSlips == null || overdueSlips.isEmpty()) return;

            System.out.println("[OverdueChecker] Found " + overdueSlips.size() + " overdue slip(s)");

            // Mark as Overdue in DB
            for (PassSlip slip : overdueSlips) {
                passSlipDAO.updatePassSlipStatus(slip.getSlipId(), "Overdue");
                System.out.println("[OverdueChecker] Marked OVERDUE: PS-" + slip.getSlipId()
                    + " — " + slip.getEmpName());
            }

            // Notify on JavaFX thread
            if (onOverdueFound != null) {
                Platform.runLater(() -> onOverdueFound.accept(overdueSlips));
            }

        } catch (Exception e) {
            System.out.println("[OverdueChecker] Error: " + e.getMessage());
        }
    }
}
