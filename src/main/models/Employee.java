package main.models;

public class Employee {
    private int empId;
    private String name;
    private String department;
    private String position;

    public Employee() {}

    public Employee(int empId, String name, String department, String position) {
        this.empId = empId;
        this.name = name;
        this.department = department;
        this.position = position;
    }

    public int getEmpId() { return empId; }
    public void setEmpId(int empId) { this.empId = empId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    @Override
    public String toString() {
        return "Employee{empId=" + empId + ", name='" + name +
                "', department='" + department + "', position='" + position + "'}";
    }
}