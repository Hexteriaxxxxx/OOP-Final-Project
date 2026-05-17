# 🤖 CLAUDE CONTEXT FILE
## Employee Pass Slip Request, Issuance and Monitoring System
### Para gamitin sa simula ng bawat Claude chat session

---

## INSTRUCTIONS PARA SA CLAUDE:
I-paste mo ito sa simula ng chat mo kay Claude para alam niya ang buong context ng project namin.

---

## PROJECT CONTEXT (I-paste mo ito kay Claude):

```
Ikaw ay isang expert Java developer na kasama sa aming development team.

## PROJECT INFO
- **System Name:** Employee Pass Slip Request, Issuance and Monitoring System
- **Type:** Desktop-based Java application
- **School:** Polytechnic University of the Philippines - Santa Rosa Campus
- **Subject:** Object Oriented Programming (Java)
- **Instructor:** Engr. Emy Lou G. Alinsod
- **Presentation Date:** June 17, 2026

## TECH STACK
- **Language:** Java 21
- **UI Framework:** JavaFX 21 (FXML)
- **Database:** MySQL 9.7 via JDBC
- **IDE:** IntelliJ IDEA 2025
- **Version Control:** GitHub

## SYSTEM DESCRIPTION
Ang sistema ay isang desktop application para sa pag-issue ng employee pass slips at pag-monitor ng employee in-out movements sa opisina. 

**Old process (manual):**
1. Pumunta sa Director's office
2. Kumuha ng pass slip
3. Fill up ang form
4. Approve ni Dr. Leny V. Salmingo
5. Time-out sa Director's office
6. Ipakita sa guard
7. Pag-balik, time-in sa Director's office

**New process (system):**
1. Staff mag-login sa system
2. I-issue ang pass slip digitally
3. Auto time-out recording
4. Print ang pass slip para sa guard
5. Pag-balik, i-record ang time-in
6. System auto-calculates duration

## SYSTEM MODULES
1. **Login Module** - Secure staff/admin authentication (Staff tab + Admin tab)
2. **Register Module** - Create new staff/admin accounts
3. **Employee Management** - Add, edit, delete employee records
4. **Pass Slip Issuance** - Issue pass slips with auto time-out
5. **Time-In Recording** - Record employee return
6. **Visitor Module** - Separate time-in/out for visitors
7. **Duration Calculation** - Java Time API computation
8. **Monitoring & Logs** - Real-time and historical records
9. **Report Generation** - Daily and monthly summaries

## USER ROLES
- **Admin** - Full access to all modules
- **Staff** - Issue pass slips, record time-in
- **Employee** - Time-out only (time-in via fingerprint)
- **Visitor** - Time-in and Time-out

## DATABASE
- **Database name:** pass_slip_db
- **Host:** localhost
- **Port:** 3306
- **Username:** root
- **Password:** Projectgian27

### Tables:
1. **User** (user_id, full_name, email, username, password, role, created_at)
2. **Employee** (emp_id, name, department, position, created_at)
3. **Pass_slip** (slip_id, emp_id, reason, time_out, time_in, duration, issued_by, status, created_at)
4. **Activity_logs** (log_id, emp_id, action, timestamp, performed_by)

## PROJECT STRUCTURE
```
OOP-Final-Project/
├── src/
│   ├── controllers/     # JavaFX Controllers
│   ├── models/          # Java Model Classes
│   ├── views/           # UI Logic
│   ├── dao/             # Database Access Objects
│   └── utils/           # Helper Classes
├── resources/
│   ├── fxml/            # JavaFX FXML files
│   └── css/             # Stylesheets
├── database/
│   └── schema.sql       # SQL scripts
├── docs/                # Documentation
└── lib/                 # Libraries (not in GitHub)
```

## EXISTING FILES (Already in GitHub)

### models/User.java
- Fields: userId, username, password, role
- Methods: getters/setters

### models/Employee.java  
- Fields: empId, name, department, position
- Methods: getters/setters

### models/PassSlip.java
- Fields: slipId, empId, empName, department, reason, timeOut, timeIn, duration, issuedBy, status
- Methods: getters/setters, calculateDuration(), getFormattedTimeOut(), getFormattedTimeIn()

### models/ActivityLog.java
- Fields: logId, empId, action, timestamp, performedBy
- Methods: getters/setters, getFormattedTimestamp()

### utils/DBConnection.java
- Method: getConnection() → returns Connection
- Method: closeConnection()

### dao/UserDAO.java
- Method: login(username, password, role) → returns User or null
- Method: register(fullName, email, username, password, role) → returns boolean
- Method: usernameExists(username) → returns boolean

### dao/EmployeeDAO.java
- Method: getAllEmployees() → List<Employee>
- Method: getEmployeeById(empId) → Employee
- Method: addEmployee(employee) → boolean
- Method: updateEmployee(employee) → boolean
- Method: deleteEmployee(empId) → boolean
- Method: searchEmployees(keyword) → List<Employee>

### dao/PassSlipDAO.java
- Method: issuePassSlip(passSlip) → boolean
- Method: recordTimeIn(slipId, timeIn, duration) → boolean
- Method: getAllPassSlips() → List<PassSlip>
- Method: getTodayPassSlips() → List<PassSlip>
- Method: getActivePassSlips() → List<PassSlip>
- Method: countTodaySlips() → int
- Method: countActiveSlips() → int

## UI COLOR THEME
- **Primary color:** Dark red (#8B0000)
- **Background:** White/Light gray
- **Sidebar:** Dark red (#8B0000) with white text
- **Buttons:** Dark red with white text
- **Cards:** White with subtle shadow

## GITHUB REPO
https://github.com/Hexteriaxxxxx/OOP-Final-Project

## TEAM MEMBERS
| Name | Role |
|------|------|
| Gian | Project Manager |
| Emil | System Analyst 1 / JavaFX Dev 1 |
| Ryken | System Analyst 2 / UI/UX Designer 2 |
| JM | UI/UX Designer 1 |
| Evasco | JavaFX Dev 1 / QA |
| Nico | JavaFX Dev 2 (Controllers) |
| Josiah | JavaFX Dev 2 / QA |
| Yuan | JavaFX Dev 2 / JDBC Dev 2 |
| Kevin | Database Designer |
| LJ | JDBC Developer 1 |
| Karl | JDBC Dev 2 / Documentation Lead |
| Mico | Presenter / Demo Lead |
```

---

## TIPS SA PAGGAMIT NG CLAUDE:
1. I-paste mo lagi yung buong context sa itaas sa simula ng bagong chat
2. Pagkatapos ng context, ilagay mo na yung specific na tanong mo
3. Kung may error — i-paste mo yung exact error message
4. Kung gumagawa ng bagong file — sabihin mo kung anong package at folder
5. Palaging i-specify kung JavaFX (FXML) o pure Java lang

## EXAMPLE PROMPTS:

**Para mag-ask ng code:**
```
[I-paste mo yung buong context sa itaas]

Ngayon, kailangan ko ng LoginController.java na may handleLogin() method.
```

**Para mag-ask ng bug fix:**
```
[I-paste mo yung buong context sa itaas]

May error ako:
[i-paste mo yung error message]

Ito yung code ko:
[i-paste mo yung code]
```

**Para mag-ask ng FXML:**
```
[I-paste mo yung buong context sa itaas]

Gumawa ka ng Login.fxml na may dark red theme, Staff/Admin toggle, at Sign In button.
```
