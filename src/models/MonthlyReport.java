package models;

public class MonthlyReport {

    private String month;
    private int totalRequests;
    private int approved;
    private int rejected;
    private int pending;
    private String avgDuration;

    public MonthlyReport(String month, int totalRequests, int approved,
                         int rejected, int pending, String avgDuration) {
        this.month = month;
        this.totalRequests = totalRequests;
        this.approved = approved;
        this.rejected = rejected;
        this.pending = pending;
        this.avgDuration = avgDuration;
    }

    public String getMonth()          { return month; }
    public int getTotalRequests()     { return totalRequests; }
    public int getApproved()          { return approved; }
    public int getRejected()          { return rejected; }
    public int getPending()           { return pending; }
    public String getAvgDuration()    { return avgDuration; }

    public void setMonth(String month)                { this.month = month; }
    public void setTotalRequests(int totalRequests)   { this.totalRequests = totalRequests; }
    public void setApproved(int approved)             { this.approved = approved; }
    public void setRejected(int rejected)             { this.rejected = rejected; }
    public void setPending(int pending)               { this.pending = pending; }
    public void setAvgDuration(String avgDuration)    { this.avgDuration = avgDuration; }
}
