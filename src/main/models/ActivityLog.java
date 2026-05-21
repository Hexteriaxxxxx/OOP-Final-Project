package main.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ActivityLog {
    private int logId;
    private int empId;
    private String action;
    private LocalDateTime timestamp;
    private String performedBy;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a");

    public ActivityLog() {}

    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }
    public int getEmpId() { return empId; }
    public void setEmpId(int empId) { this.empId = empId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public String getFormattedTimestamp() {
        return timestamp != null ? timestamp.format(FORMATTER) : "";
    }
}