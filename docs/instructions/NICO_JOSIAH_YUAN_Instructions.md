# 👤 NICO, JOSIAH, YUAN — JavaFX Developer 2
## Role: Dashboard + Controllers + Event Handling

---

## 🛠️ SETUP (Do this first — 11:00 AM)

### Step 1 — Accept GitHub Invite
- Buksan email/GitHub notifications
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
- Kunin ang `.jar` → i-paste sa `lib/mysql-connector-j-9.7.0/`

### Step 5 — I-setup Project sa IntelliJ
1. **File → Project Structure → Libraries → +**
2. I-add ang `lib/javafx-sdk-21/lib`
3. I-add ang `lib/mysql-connector-j-9.7.0/mysql-connector-j-9.7.0.jar`
4. **Apply → OK**

---

## 🎨 KUHANIN ANG UI DESIGN (Bago mag-code)

1. Lapitan si **JM o Ryken**
2. Hinging i-screenshot o i-export ang:
   - ✅ Dashboard Screen (Staff view)
   - ✅ Pass Slip Issuance Form
3. I-save ang mga screenshots sa laptop mo

---

## 💻 YOUR TASK

### Gagawin mo (gamit ang Claude):
1. `Dashboard.fxml` — Dashboard UI
2. `DashboardController.java` — logic ng Dashboard
3. `PassSlipForm.fxml` — Pass Slip Issuance Form
4. `PassSlipController.java` — logic ng Pass Slip issuance

---

## 🤖 CLAUDE PROMPT

**Step 1 — Buksan ang Claude (claude.ai)**

**Step 2 — I-paste mo ito sa simula ng chat, KASAMA ang screenshot ng Dashboard:**

```
Ikaw ay isang expert Java developer na tumutulong sa akin na gumawa ng JavaFX application.

Ang project namin ay: Employee Pass Slip Request, Issuance and Monitoring System

[I-attach mo ang screenshot ng Dashboard dito]

Base sa screenshot na ito, gumawa ka ng:

1. Dashboard.fxml - JavaFX FXML file na kapareho ng design sa screenshot
   - May left sidebar (dark red #8B0000) na may:
     * System logo/name sa taas
     * Navigation items: Dashboard, Pass Slip Requests, Approved Slips, Rejected Slips, Reports, User Management, Settings
     * Logout button sa baba
   - May main content area na may:
     * Header: "Dashboard" + "Welcome back, Staff"
     * 4 stat cards: Pending Requests, Approved Requests, Rejected Requests, Active Pass Slips Today
     * TableView ng Pass Slip Requests (columns: Request ID, Name, Department, Purpose, Time Out, Time In, Status, Actions)
     * Search bar
     * "+ Create Pass Slip" button
     * Notifications panel sa kanan
     * Recent Activity panel
     * Today's Summary panel

2. DashboardController.java na may:
   - Package: controllers
   - initialize() method na naglo-load ng data mula sa database
   - loadPassSlips() method gamit ang PassSlipDAO.getTodayPassSlips()
   - handleCreatePassSlip() method na nagbubukas ng PassSlipForm.fxml
   - handleLogout() method na nagbabalik sa Login.fxml
   - refreshData() method
   - updateStatCards() — i-update ang 4 stat cards

Existing files sa project:
- main.models/PassSlip.java (slipId, empId, empName, department, reason, timeOut, timeIn, duration, status)
- main.dao/PassSlipDAO.java (getTodayPassSlips(), countTodaySlips(), countActiveSlips())
- main.dao/EmployeeDAO.java (getAllEmployees())
- main.utils/DBConnection.java
- Database: pass_slip_db, host: localhost, port: 3306, user: root, password: Projectgian27

I-save ang FXML sa: resources/fxml/Dashboard.fxml
I-save ang Java sa: src/controllers/DashboardController.java

Gawin mo ang bawat file nang kumpleto at handa nang i-run.
```

**Step 3 — Pagkatapos makuha ang Dashboard files, bagong prompt para sa Pass Slip Form:**

```
Ikaw ay isang expert Java developer na tumutulong sa akin na gumawa ng JavaFX application.

Ang project namin ay: Employee Pass Slip Request, Issuance and Monitoring System

Gumawa ka ng:

1. PassSlipForm.fxml - Form para mag-issue ng pass slip na may:
   - Employee dropdown selector (ComboBox)
   - Purpose/Reason text field
   - Date field (auto-filled sa current date, hindi pwedeng baguhin)
   - Time Out field (auto-filled sa current time, hindi pwedeng baguhin)
   - Submit/Issue button (dark red #8B0000)
   - Cancel button
   - Same dark red theme

2. PassSlipController.java na may:
   - Package: controllers
   - initialize() — i-load ang employees sa ComboBox
   - handleIssue() — nag-save ng pass slip sa database at nag-close ng form
   - handleCancel() — nag-close ng form nang walang save
   - Validation ng empty fields
   - Auto-set ng current date at time sa timeOut

3. TimeInController.java na may:
   - Package: controllers
   - handleRecordTimeIn(int slipId) — nag-record ng time-in
   - calculateDuration() — kinukwenta ang oras sa labas
   - i-update ang Pass_slip table sa database

Existing files:
- main.models/PassSlip.java
- main.models/Employee.java
- main.dao/PassSlipDAO.java (issuePassSlip(), recordTimeIn())
- main.dao/EmployeeDAO.java (getAllEmployees())

I-save ang FXML sa: resources/fxml/PassSlipForm.fxml
I-save ang Java sa: src/controllers/PassSlipController.java at src/controllers/TimeInController.java
```

---

## 📋 KUNG SAAN ILALAGAY ANG MGA FILES

- `Dashboard.fxml` → `resources/fxml/Dashboard.fxml`
- `PassSlipForm.fxml` → `resources/fxml/PassSlipForm.fxml`
- `DashboardController.java` → `src/controllers/DashboardController.java`
- `PassSlipController.java` → `src/controllers/PassSlipController.java`
- `TimeInController.java` → `src/controllers/TimeInController.java`

---

## 📤 GIT PUSH (Gawin pagkatapos mag-code)

1. **Git → Pull** muna (Ctrl+T)
2. **Git → Commit** (Ctrl+K)
3. I-check lang:
   - ✅ src/controllers/DashboardController.java
   - ✅ src/controllers/PassSlipController.java
   - ✅ src/controllers/TimeInController.java
   - ✅ resources/fxml/Dashboard.fxml
   - ✅ resources/fxml/PassSlipForm.fxml
4. Commit message: `"Add Dashboard and PassSlip screens - Nico/Josiah/Yuan"`
5. Click **Commit and Push → Push**

## ⚠️ REMINDERS
- Huwag i-push ang `lib/` folder
- Palaging `Git → Pull` muna bago mag-code
- Kung may error — screenshot at ipakita kay Gian
- Koordinate kay Emil/Evasco — kailangan nila matapos ang Login bago kayo mag-integrate
