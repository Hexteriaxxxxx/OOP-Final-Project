package main.utils;

import dao.PassSlipDAO;
import models.PassSlip;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

/**
 * Background service that checks every 60 seconds for overdue pass slips.
 *
 * On FIRST run after start():
 *   - Detects newly overdue (status='Approved' + time_in passed)
 *   - ALSO includes existing 'Overdue' slips so they show in the alert
 *
 * On subsequent runs:
 *   - Only newly overdue (to avoid spamming alerts for already-known slips)
 */
public class OverdueCheckerService {

    private static final long CHECK_INTERVAL_MS = 60_000;

    private Timer timer;
    private final PassSlipDAO passSlipDAO = new PassSlipDAO();
    private final Consumer<List<PassSlip>> onOverdueFound;
    private boolean isFirstRun = true;

    public OverdueCheckerService(Consumer<List<PassSlip>> onOverdueFound) {
        this.onOverdueFound = onOverdueFound;
    }

    public void start() {
        if (timer != null) timer.cancel();
        isFirstRun = true; // reset on every start
        timer = new Timer("OverdueChecker", true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() { checkOverdue(); }
        }, 5_000, CHECK_INTERVAL_MS);
        System.out.println("[OverdueChecker] Started — checking every 60 seconds");
    }

    public void stop() {
        if (timer != null) { timer.cancel(); timer = null; }
        System.out.println("[OverdueChecker] Stopped");
    }

    private void checkOverdue() {
        try {
            // 1. Detect new ones (status='Approved' with passed time_in)
            List<PassSlip> newlyOverdue = passSlipDAO.getNewlyOverdueSlips();
            if (newlyOverdue == null) newlyOverdue = new ArrayList<>();

            // Mark new ones in DB
            for (PassSlip slip : newlyOverdue) {
                if (passSlipDAO.updatePassSlipStatus(slip.getSlipId(), "Overdue")) {
                    System.out.println("[OverdueChecker] Marked OVERDUE: PS-" + slip.getSlipId()
                        + " — " + slip.getEmpName());
                }
            }

            // 2. On first run, also include EXISTING Overdue slips so admin sees them
            List<PassSlip> toNotify = new ArrayList<>(newlyOverdue);
            if (isFirstRun) {
                List<PassSlip> existing = passSlipDAO.getOverdueSlips();
                if (existing != null) {
                    // Add existing that aren't already in newlyOverdue (avoid duplicates)
                    for (PassSlip ex : existing) {
                        boolean alreadyIn = false;
                        for (PassSlip ne : newlyOverdue) {
                            if (ne.getSlipId() == ex.getSlipId()) { alreadyIn = true; break; }
                        }
                        if (!alreadyIn) toNotify.add(ex);
                    }
                }
                isFirstRun = false;
            }

            if (toNotify.isEmpty()) return;

            System.out.println("[OverdueChecker] Notifying " + toNotify.size() + " overdue slip(s)");

            // Notify on JavaFX thread
            if (onOverdueFound != null) {
                final List<PassSlip> finalList = toNotify;
                Platform.runLater(() -> onOverdueFound.accept(finalList));
            }

        } catch (Exception e) {
            System.out.println("[OverdueChecker] Error: " + e.getMessage());
        }
    }
}
