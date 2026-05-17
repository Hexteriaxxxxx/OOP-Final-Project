# Employee Pass Slip Request, Issuance and Monitoring System

> OOP Final Group Project | BSIT 2nd Year | Polytechnic University of the Philippines – Santa Rosa Campus

---

## 📋 Project Overview

A desktop-based application designed to assist authorized staff and administrators in issuing official employee pass slips and monitoring employee movement during office hours. The system records employee time-out and time-in transactions, calculates the duration of time spent outside the office, and generates monitoring reports.

---

## 🚨 Problem Statement

Manual logbooks and informal procedures used by offices and campus units result in inaccurate tracking, poor monitoring, lost records, and lack of accountability for employee in-out movements during office hours.

---

## 🎯 Objectives

- Automate the issuance of employee pass slips with time-out recording
- Implement a time-in recording system for returning employees
- Calculate and display the duration of time spent outside the office
- Generate daily and monthly monitoring reports
- Provide secure role-based access for admin and staff

---

## 👥 System Modules

| Module | Description |
|--------|-------------|
| Login | Secure staff/admin authentication |
| Employee Management | Add, edit, delete employee records |
| Pass Slip Issuance | Issue pass slips, auto time-out recording |
| Time-In Recording | Record employee return |
| Visitor Module | Separate time-in/time-out for visitors |
| Duration Calculation | Java Time API-based computation |
| Monitoring & Logs | Real-time and historical records |
| Report Generation | Daily and monthly summaries |

---

## 🛠️ Tech Stack

| Technology | Purpose |
|------------|---------|
| Java API | OOP principles, validation, Date/Time API |
| JavaFX | Forms and role-based UI screens |
| JDBC | Record storage, retrieval, and updates |
| MySQL | Database management |

---

## 🗄️ Database Tables

- `User` — user_id, username, password, role
- `Employee` — emp_id, name, department, position
- `Pass_slip` — slip_id, emp_id, reason, time_out, time_in, duration, issued_by
- `Activity_logs` — log_id, emp_id, action, timestamp

---

## 📁 Project Structure

```
OOP Final Project/
├── src/
│   ├── controllers/     # JavaFX Controllers
│   ├── models/          # Java Model Classes
│   ├── views/           # UI Logic
│   └── utils/           # Helper Classes (DB Connection, etc.)
├── resources/
│   ├── fxml/            # JavaFX FXML files
│   └── css/             # Stylesheets
├── database/
│   └── schema.sql       # SQL CREATE TABLE scripts
├── docs/                # Documentation files
├── diagrams/            # Use Case, DFD, ERD, Class Diagrams
├── lib/                 # External libraries (JavaFX SDK, MySQL Connector)
└── README.md
```

---

## 👨‍💻 Team Members

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
| Mia | Presenter / Demo Lead |

---

## 📅 Timeline

| Week | Dates | Phase |
|------|-------|-------|
| Week 1 | May 9–16 | Project Planning |
| Week 2 | May 17–23 | System Analysis & Design |
| Week 3 | May 24–30 | Backend Development |
| Week 4 | May 31–Jun 6 | Database & Frontend |
| Week 5 | Jun 7–13 | Documentation & Testing |
| Week 6 | Jun 14–17 | Finalization & Presentation |

---

## 🎓 Instructor

**Engr. Emy Lou G. Alinsod**
Object Oriented Programming (Java)
Polytechnic University of the Philippines – Santa Rosa Campus

---

*Presentation Date: June 17, 2026*
