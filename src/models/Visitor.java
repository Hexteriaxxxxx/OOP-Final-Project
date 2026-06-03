package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Visitor {

    private int           visitorId;
    private String        visitorName;
    private String        company;
    private String        purpose;
    private LocalDateTime timeOut;
    private LocalDateTime timeIn;
    private String        hostEmployee;
    private String        email;
    private String        contact;
    private String        status;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a");
    private static final DateTimeFormatter TIME_ONLY =
            DateTimeFormatter.ofPattern("hh:mm a");

    public Visitor() {}

    public Visitor(String visitorName, String company, String purpose,
                   LocalDateTime timeOut, String hostEmployee) {
        this.visitorName  = visitorName;
        this.company      = company != null ? company : "";
        this.purpose      = purpose;
        this.timeOut      = timeOut;
        this.hostEmployee = hostEmployee;
        this.status       = "Pending";
    }

    public Visitor(String visitorName, String company, String purpose,
                   LocalDateTime timeOut, String hostEmployee,
                   String email, String contact) {
        this(visitorName, company, purpose, timeOut, hostEmployee);
        this.email   = email;
        this.contact = contact;
    }

    // ── Getters & Setters ──────────────────────────────────────────
    public int    getVisitorId()              { return visitorId; }
    public void   setVisitorId(int v)         { this.visitorId = v; }

    public String getVisitorName()            { return visitorName != null ? visitorName : ""; }
    public void   setVisitorName(String v)    { this.visitorName = v; }

    public String getCompany()                { return company != null ? company : ""; }
    public void   setCompany(String v)        { this.company = v; }

    public String getPurpose()                { return purpose != null ? purpose : ""; }
    public void   setPurpose(String v)        { this.purpose = v; }

    public LocalDateTime getTimeOut()         { return timeOut; }
    public void          setTimeOut(LocalDateTime v) { this.timeOut = v; }

    public LocalDateTime getTimeIn()          { return timeIn; }
    public void          setTimeIn(LocalDateTime v)  { this.timeIn = v; }

    public String getHostEmployee()           { return hostEmployee != null ? hostEmployee : ""; }
    public void   setHostEmployee(String v)   { this.hostEmployee = v; }

    public String getEmail()                  { return email != null ? email : ""; }
    public void   setEmail(String v)          { this.email = v; }

    public String getContact()                { return contact != null ? contact : ""; }
    public void   setContact(String v)        { this.contact = v; }

    public String getStatus()                 { return status != null ? status : "Pending"; }
    public void   setStatus(String v)         { this.status = v; }

    // ── Formatted getters ──────────────────────────────────────────
    public String getFormattedTimeOut() {
        return timeOut != null ? timeOut.format(FORMATTER) : "";
    }

    public String getFormattedTimeIn() {
        return timeIn != null ? timeIn.format(TIME_ONLY) : "Not yet";
    }

    public String getRequestId() {
        return String.format("VIS-%04d", visitorId);
    }
}
