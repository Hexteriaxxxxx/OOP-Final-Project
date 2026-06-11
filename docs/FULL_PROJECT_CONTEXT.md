# 🧠 FULL PROJECT CONTEXT — OOP Final Project
## Para i-paste sa bagong Claude chat para ma-transfer lahat ng memory

---

## 📌 BASIC INFO
- **Project:** Employee Pass Slip Request, Issuance and Monitoring System
- **Subject:** Object Oriented Programming (Java)
- **School:** Polytechnic University of the Philippines – Santa Rosa Campus
- **Instructor:** Engr. Emy Lou G. Alinsod
- **Presentation Date:** June 17, 2026
- **Group Size:** 11 members
- **Current Date Context:** May 2026 (Week 2 completed)

---

## 👥 TEAM MEMBERS & ROLES

| Name | Role | GitHub |
|------|------|--------|
| Gian (Santos, R. Justin Gian) | Project Manager | Hexteriaxxxxx |
| Emil (Fernandez, V. Emil John) | System Analyst 1 / JavaFX Dev 1 | emilfernandez0012@gmail.com |
| Ryken (Lapating, B. Ryken Gabriel) | System Analyst 2 / UI/UX Designer 2 | RykenGabriel |
| JM (Delfino, D. Jose Manuel) | UI/UX Designer 1 | delfinojm23@gmail.com |
| Evasco (Evasco, R. Josh Daniel) | JavaFX Dev 1 / QA | joshevasco123@gmail.com |
| Nico (Ancheta, S. Nico Angelo) | JavaFX Dev 2 (Controllers) | anchetanico3@gmail.com |
| Josiah (Olicia, David Josiah) | JavaFX Dev 2 / QA | oliciajosiahdavid@gmail.com |
| Yuan (Sayasaya, T. Yuan Paolo) | JavaFX Dev 2 / JDBC Dev 2 | sayasayayuanpaolo@gmail.com |
| Kevin (Manalo, A. Kevin Brian) | Database Designer | manalokevin27@gmail.com |
| LJ (Catindig, J. Lj Bennedict) | JDBC Developer 1 | ctndg31 |
| Karl (De Guzman, T. Karl Christian) | JDBC Dev 2 / Documentation Lead | kctdg |
| Mico (Panguilinan, M. Mico) | Presenter / Demo Lead | Tamicsx |

---

## 🛠️ TECH STACK
- **Language:** Java 21
- **UI Framework:** JavaFX 21 (FXML)
- **Database:** MySQL 9.7 via JDBC
- **Connector:** mysql-connector-j-9.7.0
- **JavaFX SDK:** javafx-sdk-26.0.1
- **IDE:** IntelliJ IDEA 2025.3.4 Ultimate
- **Version Control:** GitHub

---

## 🗄️ DATABASE CREDENTIALS
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

---

## 📁 PROJECT STRUCTURE
```
C:\Users\ADMIN1\IdeaProjects\OOP Final Project\
├── src/
│   ├── controllers/     # JavaFX Controllers (LoginController, DashboardController, etc.)
│   ├── models/          # User.java, Employee.java, PassSlip.java, ActivityLog.java
│   ├── views/           # UI Logic
│   ├── dao/             # UserDAO.java, EmployeeDAO.java, PassSlipDAO.java
│   ├── utils/           # DBConnection.java
│   └── main/            # Main.java (entry point)
├── resources/
│   ├── fxml/            # Login.fxml, Register.fxml, Dashboard.fxml, etc.
│   └── css/             # Stylesheets
├── database/
│   └── schema.sql       # Final SQL scripts
├── docs/
│   ├── CLAUDE_CONTEXT.md
│   ├── FULL_PROJECT_CONTEXT.md  ← THIS FILE
│   ├── MEETING_FLOW.md
│   └── instructions/    # Per-member instruction files
├── diagrams/
├── lib/                 # JavaFX SDK + MySQL Connector (NOT in GitHub)
├── .gitignore
└── README.md
```

---

## 🐙 GITHUB REPO
- **URL:** https://github.com/Hexteriaxxxxx/OOP-Final-Project
- **Branch:** master
- **Visibility:** Private (all members added as collaborators)

---

## ✅ EXISTING JAVA FILES (Already coded)

### utils/DBConnection.java
- getConnection() — returns MySQL Connection
- closeConnection()
- Connects to: localhost:3306/pass_slip_db, root, Projectgian27

### models/User.java
- Fields: userId, username, password, role
- Getters/setters

### models/Employee.java
- Fields: empId, name, department, position
- Getters/setters

### models/PassSlip.java
- Fields: slipId, empId, empName, department, reason, timeOut, timeIn, duration, issuedBy, status
- Methods: calculateDuration(), getFormattedTimeOut(), getFormattedTimeIn()

### models/ActivityLog.java
- Fields: logId, empId, action, timestamp, performedBy
- Methods: getFormattedTimestamp()

### dao/UserDAO.java
- login(username, password, role) → User or null
- register(fullName, email, username, password, role) → boolean
- usernameExists(username) → boolean

### dao/EmployeeDAO.java
- getAllEmployees() → List<Employee>
- getEmployeeById(empId) → Employee
- addEmployee(employee) → boolean
- updateEmployee(employee) → boolean
- deleteEmployee(empId) → boolean
- searchEmployees(keyword) → List<Employee>

### dao/PassSlipDAO.java
- issuePassSlip(passSlip) → boolean
- recordTimeIn(slipId, timeIn, duration) → boolean
- getAllPassSlips() → List<PassSlip>
- getTodayPassSlips() → List<PassSlip>
- getActivePassSlips() → List<PassSlip>
- countTodaySlips() → int
- countActiveSlips() → int

### src/main/Main.java
- Entry point, loads Login.fxml
- Window: 1280x720, not resizable

---

## 🎨 UI DESIGN (Completed by JM/Ryken)

### Existing Screens:
- ✅ Login Screen (Staff/Admin toggle, Username, Password, Remember me, Sign In)
- ✅ Register Screen (Staff/Admin toggle, Full Name, Email, Username, Password, Confirm Password)
- ✅ Dashboard (Sidebar nav, 4 stat cards, Pass Slip table, Notifications, Recent Activity, Today's Summary)

### Color Theme:
- **Primary:** Dark Red (#8B0000)
- **Sidebar:** Dark Red with white text
- **Content Area:** White/Light gray
- **Buttons:** Dark Red with white text

### Screens Still Needed:
- Pass Slip Issuance Form
- Monitoring & Logs Screen
- Report Generation Screen
- Employee Management Screen
- Visitor Module Screen

---

## 📋 GRADING CRITERIA

### Group Grade (60%):
- System functionality and completeness — 30%
- UI design and usability — 10%
- Database correctness and JDBC usage — 10%
- System integration and presentation — 10%

### Individual Grade (40%):
- Role-specific deliverables — 25%
- Attendance and participation — 10%
- Peer evaluation — 5%

### Role Weights:
| Role | Weight |
|------|--------|
| Project Manager | 13% |
| System Analyst | 11% |
| UI/UX Designer 1 | 10% |
| UI/UX Designer 2 | 10% |
| JavaFX Developer 1 | 12% |
| JavaFX Developer 2 | 11% |
| Database Designer | 11% |
| JDBC Dev 1 | 9% |
| JDBC Dev 2 | 10% |
| QA/Tester | 8% |
| Documentation Lead | 8% |

---

## 📅 TIMELINE & DELIVERABLES

### Week 0-1 (May 9-16) — PROJECT PLANNING ✅ DONE
- ✅ Project Plan — Gian
- ✅ Requirements Specification — Emil, Ryken
- ✅ Proposed Project Details — Karl

### Week 1-2 (May 17-23) — SYSTEM ANALYSIS & DESIGN ✅ DONE
- ✅ Use Case Diagram + DFD — Emil, Ryken
- ✅ Approved Requirements — Gian
- ✅ Chapter 2 Draft — Karl
- ✅ UI Mockups — JM, Ryken
- ✅ ERD Diagram — Kevin
- ✅ System Design Document — Nico, Josiah, Yuan

### Week 2-3 (May 24-30) — BACKEND DEVELOPMENT
- Pass Slip & Registration Module — Nico
- Ingress/Egress Monitoring Logic — Josiah
- Reports & Validation Logic — Yuan

### Week 3-4 (May 31-Jun 6) — DATABASE & FRONTEND
- Create Database & Tables — Kevin, LJ
- JDBC Integration + CRUD — LJ, Karl, Yuan
- Login + Dashboard UI — Evasco, Emil
- Apply Layout & Styling — JM, Ryken

### Week 4-5 (Jun 7-13) — DOCUMENTATION & TESTING
- Final Documentation Ch. 1-5 — Karl
- Appendices — All members
- Functional Testing — Josiah, Evasco
- Bug Reports & Fixes — QA + Devs

### Week 6 (Jun 14-17) — FINALIZATION
- Presentation Slides — Mico, Gian
- Final Demo — All members
- Working System — QA/Testers
- 🎓 FINAL PRESENTATION — June 17, 2026

---

## 🔧 TRELLO BOARD
- **Board Name:** OOP
- **Lists:** GROUP RESOURCES | Week 0-1 | Week 1-2 | Doing | Done | BUG O BOLD
- **GROUP RESOURCES cards:** Documentation, System Description Doc, Roles & Task Matrix, Timeline/Gantt Chart, Project Criteria, Pass Slip Template

---

## 📄 DOCUMENTATION TEMPLATES CREATED
All saved in `/mnt/user-data/outputs/` and can be re-downloaded:
1. Week1_Project_Plan.docx — Gian
2. Week1_Requirements_Specification.docx — Emil, Ryken
3. Week1_Proposed_Project_Details.docx — Karl
4. W12_1_UseCaseDiagram_DFD_Emil_Ryken.docx
5. W12_2_ApprovedRequirements_Gian.docx
6. W12_3_Chapter2Draft_Karl.docx
7. W12_4_UIMockups_JM_Ryken.docx
8. W12_5_ERDDiagram_Kevin.docx
9. W12_6_SystemDesignDoc_Nico_Josiah_Yuan.docx
10. 1_System_Description.docx
11. 2_Roles_Task.docx
12. 3_Timeline.docx
13. Week2_Complete_Instructions.docx
14. gantt_chart_v2.html

---

## 📜 DOCUMENTATION TEMPLATE FORMAT (Prof's requirement)
- **Paper Size:** A4
- **Font:** Arial 12
- **Spacing:** Double-spaced (2.0)
- **Alignment:** Justified
- **Length:** 15-25 pages
- **Must include:** ERD, DFD, Screenshots, Working system

### 5 Chapters:
1. Introduction (Background, Objectives, Scope, Significance)
2. System Analysis & Design (Use Case, DFD, ERD, Architecture)
3. System Development (Tools, Modules, CRUD, Code)
4. Testing & Results (Test Cases, Results, Issues)
5. Conclusion & Recommendations

---

## 🎯 OLD PASS SLIP PROCESS (for context/background)
1. Pupunta sa Director's office
2. Hihingi ng pass slip
3. Fifill-upan ng form
4. I-aapprove ni Dr. Leny V. Salmingo (Campus Director)
5. Magtitime-out sa Director's office
6. Ipapakita sa guard yung pass slip
7. Pag-tapos ng errands, babalik sa Director's office para sa time-in

---

## 💡 ADDITIONAL SYSTEM NOTES
- Employee Time-In is via **fingerprint** — system only records the data, hindi siya nagse-set ng time-in manually para sa employee
- **Visitor module** — separate sa employee: both Time-In and Time-Out recorded manually
- **AI integration** planned but optional (feasibility dependent)
- Pass Slip can be **printed on small paper** (like a receipt)
- Pass Slip template to be provided by Ma'am

---

## 🤖 HOW TO USE THIS CONTEXT FILE
1. I-paste mo ang LAHAT ng laman ng file na ito sa simula ng bagong Claude chat
2. Pagkatapos, isulat mo ang iyong tanong o request
3. Si Claude ay magkakaroon ng buong context ng project

### Example:
```
[I-paste mo ang buong content ng file na ito]

---

Ngayon, kailangan ko ng: [ilagay mo dito ang tanong mo]
```
