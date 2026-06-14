package main.utils;

import dao.PassSlipDAO;
import models.PassSlip;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public class OverdueCheckerService {

    private static final long CHECK_INTERVAL_MS = 60_000;

    private Timer timer;
    private final PassSlipDAO passSlipDAO = new PassSlipDAO();
    private final Consumer<List<PassSlip>> onOverdueFound;
    private volatile boolean isFirstRun = true; // BUG 9 FIX: volatile para safe sa timer thread

    // BUG 9 FIX: caller na bahala mag-wrap ng Platform.runLater() kung kailangan
    // OverdueCheckerService should not depend on JavaFX — separation of concerns
    public OverdueCheckerService(Consumer<List<PassSlip>> onOverdueFound) {
        this.onOverdueFound = onOverdueFound;
    }

    public void start() {
        if (timer != null) timer.cancel();
        isFirstRun = true;
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
            List<PassSlip> newlyOverdue = passSlipDAO.getNewlyOverdueSlips();
            if (newlyOverdue == null) newlyOverdue = new ArrayList<>();

            for (PassSlip slip : newlyOverdue) {
                if (passSlipDAO.updatePassSlipStatus(slip.getSlipId(), "Overdue")) {
                    System.out.println("[OverdueChecker] Marked OVERDUE: PS-" + slip.getSlipId()
                            + " — " + slip.getEmpName());
                }
            }

            List<PassSlip> toNotify = new ArrayList<>(newlyOverdue);
            if (isFirstRun) {
                List<PassSlip> existing = passSlipDAO.getOverdueSlips();
                if (existing != null) {
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

            if (onOverdueFound != null) {
                final List<PassSlip> finalList = toNotify;
                onOverdueFound.accept(finalList); // caller wraps Platform.runLater() na lang
            }

        } catch (Exception e) {
            System.out.println("[OverdueChecker] Error: " + e.getMessage());
        }
    }
}