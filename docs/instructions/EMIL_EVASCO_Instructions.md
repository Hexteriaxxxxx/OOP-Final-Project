# 👤 EMIL & EVASCO — JavaFX Developer 1
## Role: Login Screen + Register Screen Implementation

---

## 🛠️ SETUP (Do this first — 11:00 AM)

### Step 1 — Accept GitHub Invite
- Buksan yung email na ginamit mo sa GitHub
- Hanapin yung invite mula kay Hexteriaxxxxx
- Click **Accept Invitation**

### Step 2 — Install IntelliJ (kung wala pa)
- Pumunta sa: https://www.jetbrains.com/idea/download
- I-download ang **Community Edition** (libre)
- I-install

### Step 3 — I-clone ang Repo
1. Buksan ang IntelliJ
2. Click **Get from VCS**
3. I-paste: `https://github.com/Hexteriaxxxxx/OOP-Final-Project`
4. Piliin kung saan i-save (Documents o Desktop)
5. Click **Clone**

### Step 4 — I-download ang JavaFX SDK
- Pumunta sa: https://gluonhq.com/products/javafx
- Download: **JavaFX 21 LTS, Windows, x64, SDK**
- I-extract sa loob ng project folder → `lib/javafx-sdk-21/`

### Step 5 — I-download ang MySQL Connector
- Pumunta sa: https://dev.mysql.com/downloads/connector/j
- Piliin: **Platform Independent**
- I-download ang **.zip**
- I-extract, kunin ang `.jar` file
- I-paste sa `lib/mysql-connector-j-9.7.0/`

### Step 6 — I-setup ang Project sa IntelliJ
1. **File → Project Structure**
2. **Project → SDK** → piliin **jbr-21** o **Java 21**
3. **Language Level** → **21**
4. **Libraries → +** → i-add ang `lib/javafx-sdk-21/lib`
5. **Libraries → +** → i-add ang `lib/mysql-connector-j-9.7.0/mysql-connector-j-9.7.0.jar`
6. Click **Apply → OK**

---

## 🎨 KUHANIN ANG UI DESIGN (Bago mag-code)

1. Lapitan si **JM o Ryken**
2. Hinging i-screenshot o i-export ang:
   - ✅ Login Screen (Staff view)
   - ✅ Login Screen (Admin view)
   - ✅ Register Screen (Staff view)
   - ✅ Register Screen (Admin view)
3. I-save ang mga screenshots sa laptop mo

---

## 💻 YOUR TASK

### Gagawin mo (gamit ang Claude):
1. `Login.fxml` — Login Screen
2. `Register.fxml` — Register Screen
3. `LoginController.java` — logic ng Login
4. `RegisterController.java` — logic ng Register

---

## 🤖 CLAUDE PROMPT

**Step 1 — Buksan ang Claude (claude.ai)**

**Step 2 — I-paste mo ito sa simula ng chat, KASAMA ang screenshot ng Login Screen:**

```
Ikaw ay isang expert Java developer na tumutulong sa akin na gumawa ng JavaFX application.

Ang project namin ay: Employee Pass Slip Request, Issuance and Monitoring System

[I-attach mo ang screenshot ng Login Screen dito]

Base sa screenshot na ito, gumawa ka ng:

1. Login.fxml - JavaFX FXML file na kapareho ng design sa screenshot
   - Gamitin ang dark red (#8B0000) color theme
   - May Staff/Admin toggle buttons
   - May Username field
   - May Password field
   - May Remember me checkbox
   - May Forgot Password link
   - May Sign In button
   - May Sign Up link sa baba
   - May copyright footer: "© 2026 Pass Slip Issuance System"

2. LoginController.java na may:
   - Package: controllers
   - handleLogin() method na nag-check ng username/password sa database
   - handleStaffTab() at handleAdminTab() para sa toggle
   - handleSignUp() method na nagbubukas ng Register.fxml
   - Role-based redirect: admin → Dashboard.fxml, staff → Dashboard.fxml
   - Alert dialog kung mali ang credentials

Existing files sa project:
- models/User.java (userId, username, password, role)
- dao/UserDAO.java (login(username, password, role) → User)
- utils/DBConnection.java (getConnection() → Connection)
- Database: pass_slip_db, host: localhost, port: 3306, user: root, password: Projectgian27

I-save ang FXML sa: resources/fxml/Login.fxml
I-save ang Java sa: src/controllers/LoginController.java

Gawin mo ang bawat file nang kumpleto at handa nang i-run.
```

**Step 3 — Pagkatapos makuha ang Login files, bagong prompt para sa Register:**

```
Ikaw ay isang expert Java developer na tumutulong sa akin na gumawa ng JavaFX application.

Ang project namin ay: Employee Pass Slip Request, Issuance and Monitoring System

[I-attach mo ang screenshot ng Register Screen dito]

Base sa screenshot, gumawa ka ng:

1. Register.fxml - JavaFX FXML file na kapareho ng design sa screenshot
   - Same dark red (#8B0000) theme
   - May Staff/Admin toggle buttons
   - May Full Name field
   - May Email Address field
   - May Username field
   - May Password field
   - May Confirm Password field
   - May Terms and Conditions checkbox
   - May Create Account button
   - May Back to Login link

2. RegisterController.java na may:
   - Package: controllers
   - handleRegister() — nag-save ng bagong user sa database
   - Validation: empty fields, password match, username exists
   - handleBack() — bumalik sa Login.fxml
   - Success alert pagkatapos mag-register

Existing files:
- dao/UserDAO.java (register(fullName, email, username, password, role) → boolean)
- dao/UserDAO.java (usernameExists(username) → boolean)

I-save ang FXML sa: resources/fxml/Register.fxml
I-save ang Java sa: src/controllers/RegisterController.java
```

---

## 📋 KUNG SAAN ILALAGAY ANG MGA FILES

Pagkatapos makuha ang code mula kay Claude:

- `Login.fxml` → i-save sa `resources/fxml/Login.fxml`
- `Register.fxml` → i-save sa `resources/fxml/Register.fxml`
- `LoginController.java` → i-save sa `src/controllers/LoginController.java`
- `RegisterController.java` → i-save sa `src/controllers/RegisterController.java`

---

## 📤 GIT PUSH (Gawin pagkatapos mag-code)

1. **Git → Pull** muna (Ctrl+T) — para updated
2. **Git → Commit** (Ctrl+K)
3. I-check lang:
   - ✅ src/controllers/LoginController.java
   - ✅ src/controllers/RegisterController.java
   - ✅ resources/fxml/Login.fxml
   - ✅ resources/fxml/Register.fxml
4. Commit message: `"Add Login and Register screens - Emil/Evasco"`
5. Click **Commit and Push → Push**

## ⚠️ REMINDERS
- Huwag i-push ang `lib/` folder
- Palaging `Git → Pull` muna bago mag-code
- Kung may error — screenshot at ipakita kay Gian
