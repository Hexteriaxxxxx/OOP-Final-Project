# 👤 NICO, JOSIAH, YUAN — JavaFX Developer 2
## Role: Dashboard + Controllers + Event Handling

---

## 🛠️ SETUP (Do this first — 11:00 AM)
(Same setup steps as Emil/Evasco — clone repo, install IntelliJ, download JavaFX + MySQL Connector, setup Project SDK)

### Step 1 — Accept GitHub Invite
- Buksan yung email/GitHub notifications
- Click **Accept Invitation** mula kay Hexteriaxxxxx

### Step 2 — I-clone ang Repo
1. Buksan ang IntelliJ → **Get from VCS**
2. I-paste: `https://github.com/Hexteriaxxxxx/OOP-Final-Project`
3. Click **Clone**

### Step 3 — I-download ang JavaFX SDK
- https://gluonhq.com/products/javafx → JavaFX 21 LTS, Windows, x64, SDK
- I-extract sa `lib/javafx-sdk-21/`

### Step 4 — I-download ang MySQL Connector
- https://dev.mysql.com/downloads/connector/j → Platform Independent → .zip
- I-extract, kunin ang `.jar` → i-paste sa `lib/mysql-connector-j-9.7.0/`

### Step 5 — I-setup Project sa IntelliJ
1. **File → Project Structure → Libraries → +**
2. I-add ang `lib/javafx-sdk-21/lib`
3. I-add ang `lib/mysql-connector-j-9.7.0/mysql-connector-j-9.7.0.jar`
4. **Apply → OK**

---

## 💻 YOUR TASK
1. `Dashboard.fxml` — Staff Dashboard UI
2. `DashboardController.java` — logic ng Dashboard
3. `PassSlipForm.fxml` — Pass Slip Issuance Form
4. `PassSlipController.java` — logic ng Pass Slip issuance
5. `TimeInController.java` — logic ng Time-In recording

---

## 🤖 CLAUDE PROMPT (I-paste mo ito sa Claude)

```
Ikaw ay isang expert Java developer na tumutulong sa akin na gumawa ng JavaFX application.

Ang project namin ay: Employee Pass Slip Request, Issuance and Monitoring System

Ang kailangan ko:

1. Dashboard.fxml - JavaFX dashboard na may:
   - Left sidebar (dark red #8B0000) na may:
     * Logo/System name sa taas
     * Navigation: Dashboard, Pass Slip Requests, Approved Slips, Rejected Slips, Reports, User Management, Settings
     * Logout button sa baba
   - Main content area na may:
     * Header: "Dashboard" + "Welcome back, Staff"
     * 4 stat cards: Pending Requests, Approved Requests, Rejected Requests, Active Pass Slips Today
     * Table ng Pass Slip Requests (Request ID, Name, Department, Purpose, Time Out, Time In, Status, Actions)
     * Search bar
     * "+ Create Pass Slip" button
     * Notifications panel (right side)
     * Recent Activity panel
     * Today's Summary panel

2. DashboardController.java na may:
   - Package: controllers
   - initialize() method na naglo-load ng data mula sa database
   - loadPassSlips() method gamit ang PassSlipDAO
   - handleCreatePassSlip() method na nagbubukas ng PassSlipForm
   - handleLogout() method
   - refreshData() method

3. PassSlipForm.fxml - Form para mag-issue ng pass slip na may:
   - Employee selector (dropdown)
   - Purpose/Reason field
   - Date field (auto-filled sa current date)
   - Time Out field (auto-filled sa current time)
   - Submit button
   - Cancel button
   - Same dark red theme

4. PassSlipController.java na may:
   - Package: controllers
   - handleSubmit() method na nag-save ng pass slip sa database
   - handleTimeIn() method na nag-record ng time-in
   - calculateDuration() method
   - Validation ng empty fields

Existing files:
- models/PassSlip.java
- dao/PassSlipDAO.java (may issuePassSlip(), recordTimeIn(), getTodayPassSlips() methods)
- dao/EmployeeDAO.java (may getAllEmployees() method)
- utils/DBConnection.java

Database: pass_slip_db, MySQL port: 3306, user: root, password: Projectgian27

I-save ang fxml sa: resources/fxml/
I-save ang java sa: src/controllers/

Gawin mo ang bawat file nang kumpleto at ready to run.
```

---

## 📤 GIT PUSH

1. **Git → Pull** muna (para updated)
2. **Git → Commit** (Ctrl+K)
3. I-check lang:
   - ✅ src/controllers/DashboardController.java
   - ✅ src/controllers/PassSlipController.java
   - ✅ src/controllers/TimeInController.java
   - ✅ resources/fxml/Dashboard.fxml
   - ✅ resources/fxml/PassSlipForm.fxml
4. Commit message: `"Add Dashboard and PassSlip controllers - Nico/Josiah/Yuan"`
5. **Commit and Push → Push**

## ⚠️ REMINDERS
- Huwag i-push ang `lib/` folder
- `Git → Pull` muna bago mag-code
- Kung may conflict — ipasabi agay kay Gian
