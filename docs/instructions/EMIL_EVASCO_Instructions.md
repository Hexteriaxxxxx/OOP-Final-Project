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

## 💻 YOUR TASK
1. Gumawa ng `Login.fxml` — Login Screen
2. Gumawa ng `Register.fxml` — Register Screen
3. Gumawa ng `LoginController.java` — logic ng Login
4. Gumawa ng `RegisterController.java` — logic ng Register
5. Gumawa ng `Main.java` — entry point ng application

---

## 🤖 CLAUDE PROMPT (I-paste mo ito sa Claude)

```
Ikaw ay isang expert Java developer na tumutulong sa akin na gumawa ng JavaFX application.

Ang project namin ay: Employee Pass Slip Request, Issuance and Monitoring System

Ang kailangan ko:
1. Login.fxml - JavaFX login screen na may:
   - Staff/Admin toggle buttons
   - Username field
   - Password field
   - Remember me checkbox
   - Forgot Password link
   - Sign In button
   - Sign Up link
   - Copyright footer "© 2026 Pass Slip Issuance System"
   - Dark red (#8B0000) color theme
   - White card in center

2. LoginController.java na may:
   - Package: controllers
   - Import: models.User, dao.UserDAO
   - handleLogin() method na nag-check ng username/password sa database
   - handleRegister() method na nagbubukas ng Register screen
   - Role-based redirect: admin → DashboardAdmin.fxml, staff → DashboardStaff.fxml
   - Error handling kung mali ang credentials

3. Register.fxml - JavaFX register screen na may:
   - Staff/Admin toggle buttons
   - Full Name field
   - Email field
   - Username field
   - Password field
   - Confirm Password field
   - Terms and Conditions checkbox
   - Create Account button
   - Back to Login link
   - Same dark red theme

4. RegisterController.java na may:
   - Package: controllers
   - handleRegister() method na nag-save ng bagong user sa database
   - Validation: password match, empty fields, username exists
   - handleBack() method para bumalik sa Login

5. Main.java na may:
   - Naglo-load ng Login.fxml as primary stage
   - Window title: "Pass Slip Issuance System"
   - Window size: 1280x720

Existing files sa project:
- models/User.java (may userId, username, password, role)
- dao/UserDAO.java (may login() at register() methods)
- utils/DBConnection.java (may getConnection() method)
- Database: pass_slip_db, MySQL port: 3306, user: root, password: Projectgian27

I-save ang fxml files sa: resources/fxml/
I-save ang java files sa: src/controllers/ at src/main/

Gawin mo ang bawat file nang kumpleto at ready to run.
```

---

## 📤 GIT PUSH (Gawin pagkatapos mag-code)

1. Click **Git → Commit** (Ctrl+K)
2. I-check lang:
   - ✅ src/controllers/LoginController.java
   - ✅ src/controllers/RegisterController.java
   - ✅ src/main/Main.java
   - ✅ resources/fxml/Login.fxml
   - ✅ resources/fxml/Register.fxml
3. Commit message: `"Add Login and Register screens - Emil/Evasco"`
4. Click **Commit and Push → Push**

## ⚠️ REMINDERS
- Huwag i-push ang `lib/` folder
- Palaging `Git → Pull` muna bago mag-code
- Kung may error — screenshot at ipakita kay Gian
