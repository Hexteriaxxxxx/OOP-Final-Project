package main.models;

import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

public class PassSlip {
    private int slipId;
    private int empId;
    private String empName;
    private String department;
    private String reason;
    private LocalDateTime timeOut;
    private LocalDateTime timeIn;
    private String duration;
    private int issuedBy;
    private String status; // "Pending", "Approved", "Rejected", "Returned"

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a");

    public PassSlip() {}

    public PassSlip(int empId, String reason, LocalDateTime timeOut, int issuedBy) {
        this.empId = empId;
        this.reason = reason;
        this.timeOut = timeOut;
        this.issuedBy = issuedBy;
        this.status = "Pending";
    }

    // Calculate duration when time-in is recorded
    public void calculateDuration() {
        if (timeOut != null && timeIn != null) {
            Duration dur = Duration.between(timeOut, timeIn);
            long hours = dur.toHours();
            long minutes = dur.toMinutesPart();
            this.duration = hours + "h " + minutes + "m";
        }
    }

    // Getters and Setters
    public int getSlipId() { return slipId; }
    public void setSlipId(int slipId) { this.slipId = slipId; }

    public int getEmpId() { return empId; }
    public void setEmpId(int empId) { this.empId = empId; }

    public String getEmpName() { return empName; }
    public void setEmpName(String empName) { this.empName = empName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getTimeOut() { return timeOut; }
    public void setTimeOut(LocalDateTime timeOut) { this.timeOut = timeOut; }

    public LocalDateTime getTimeIn() { return timeIn; }
    public void setTimeIn(LocalDateTime timeIn) {
        this.timeIn = timeIn;
        calculateDuration();
    }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public int getIssuedBy() { return issuedBy; }
    public void setIssuedBy(int issuedBy) { this.issuedBy = issuedBy; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFormattedTimeOut() {
        return timeOut != null ? timeOut.format(FORMATTER) : "";
    }

    public String getFormattedTimeIn() {
        return timeIn != null ? timeIn.format(FORMATTER) : "Not yet returned";
    }
}