# 👤 KARL — JDBC Developer 2 + Documentation Lead
## Role: CRUD Operations + Dashboard Data + Documentation

---

## 🛠️ SETUP (11:00 AM)

### Step 1 — Accept GitHub Invite
- Buksan email/GitHub → Accept Invitation (username: kctdg)

### Step 2 — I-clone ang Repo
1. IntelliJ → **Get from VCS**
2. I-paste: `https://github.com/Hexteriaxxxxx/OOP-Final-Project`
3. Click **Clone**

### Step 3 — I-install ang MySQL
- https://dev.mysql.com/downloads/mysql → Windows MSI Installer
- Root password: `Projectgian27`
- I-run ang `database/schema.sql` sa MySQL Workbench

### Step 4 — Downloads
- JavaFX SDK: https://gluonhq.com/products/javafx → JavaFX 21 LTS
- MySQL Connector: https://dev.mysql.com/downloads/connector/j → Platform Independent

---

## 💻 YOUR TASK

### JDBC Task:
1. Gumawa ng `ReportDAO.java` — para sa report generation
2. I-test ang PassSlipDAO CRUD operations
3. I-verify ang data flow mula DAO hanggang UI

### Documentation Task:
1. I-document ang lahat ng classes na nagagawa ng grupo
2. Gumawa ng `SETUP_GUIDE.md` — para sa ibang members na mag-se-setup

---

## 🤖 CLAUDE PROMPT

```
Ikaw ay isang expert Java developer at technical writer na tumutulong sa akin.

Ang project namin ay: Employee Pass Slip Request, Issuance and Monitoring System

Kailangan ko ng dalawa:

1. ReportDAO.java na may:
   - Package: main.dao
   - getDailyReport(LocalDate date) — lahat ng pass slips sa isang araw
   - getMonthlyReport(int month, int year) — lahat ng pass slips sa isang buwan
   - getTotalByDepartment() — count ng pass slips per department
   - getAverageDuration() — average time outside ng employees
   - Returns List<PassSlip> o Map depende sa method

2. SETUP_GUIDE.md na may:
   - Step by step na paano i-clone ang repo
   - Paano i-install ang JavaFX SDK
   - Paano i-install ang MySQL Connector
   - Paano i-setup ang IntelliJ Project Structure
   - Paano i-run ang schema.sql
   - Paano i-run ang application

Existing files:
- main.models/PassSlip.java
- main.dao/PassSlipDAO.java
- main.utils/DBConnection.java
- Database: pass_slip_db

[Ilagay mo dito ang specific na tanong]
```

---

## 📤 GIT PUSH
1. **Git → Pull** muna
2. **Git → Commit** (Ctrl+K)
3. I-check lang:
   - ✅ src/main.dao/ReportDAO.java
   - ✅ docs/SETUP_GUIDE.md
4. Commit message: `"Add ReportDAO and setup guide - Karl"`
5. **Commit and Push → Push**

## ⚠️ REMINDERS
- Huwag i-push ang `lib/` folder
- Koordinate kay LJ para sa database connection issues
