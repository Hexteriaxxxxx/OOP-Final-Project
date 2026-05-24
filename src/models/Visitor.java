package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Visitor {

    private int visitorId;
    private String visitorName;
    private String company;
    private String purpose;
    private LocalDateTime timeOut;
    private LocalDateTime timeIn;
    private String hostEmployee;
    private String status; // Pending, Approved, Rejected

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("hh:mm a");

    public Visitor() {}

    public Visitor(String visitorName, String company, String purpose,
                   LocalDateTime timeOut, String hostEmployee) {
        this.visitorName  = visitorName;
        this.company      = company;
        this.purpose      = purpose;
        this.timeOut      = timeOut;
        this.hostEmployee = hostEmployee;
        this.status       = "Pending";
    }

    // ── Getters & Setters ──────────────────────────────────────────
    public int    getVisitorId()               { return visitorId; }
    public void   setVisitorId(int visitorId)  { this.visitorId = visitorId; }

    public String getVisitorName()                     { return visitorName; }
    public void   setVisitorName(String visitorName)   { this.visitorName = visitorName; }

    public String getCompany()               { return company; }
    public void   setCompany(String company) { this.company = company; }

    public String getPurpose()               { return purpose; }
    public void   setPurpose(String purpose) { this.purpose = purpose; }

    public LocalDateTime getTimeOut()                  { return timeOut; }
    public void          setTimeOut(LocalDateTime t)   { this.timeOut = t; }

    public LocalDateTime getTimeIn()                   { return timeIn; }
    public void          setTimeIn(LocalDateTime t)    { this.timeIn = t; }

    public String getHostEmployee()                        { return hostEmployee; }
    public void   setHostEmployee(String hostEmployee)     { this.hostEmployee = hostEmployee; }

    public String getStatus()              { return status; }
    public void   setStatus(String status) { this.status = status; }

    // ── Formatted getters ──────────────────────────────────────────
    public String getFormattedTimeOut() {
        return timeOut != null ? timeOut.format(FORMATTER) : "";
    }

    public String getFormattedTimeIn() {
        return timeIn != null ? timeIn.format(FORMATTER) : "Not yet";
    }

    public String getRequestId() {
        return String.format("VIS-2026-%03d", visitorId);
    }
}
