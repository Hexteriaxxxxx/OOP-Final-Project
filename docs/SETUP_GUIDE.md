# 🛠️ SETUP GUIDE
## Employee Pass Slip Request, Issuance and Monitoring System
### Polytechnic University of the Philippines – Santa Rosa Campus

---

## ✅ PREREQUISITES
Before starting, make sure you have the following installed:
- Java JDK 21
- IntelliJ IDEA 2025
- MySQL Server 8.x
- MySQL Workbench
- Git

---

## STEP 1 — Clone the Repository

1. Open **Git Bash** or any terminal
2. Navigate to the folder where you want to save the project:
   ```
   cd Documents
   ```
3. Clone the repository:
   ```
   git clone https://github.com/Hexteriaxxxxx/OOP-Final-Project.git
   ```
4. Open the cloned folder:
   ```
   cd OOP-Final-Project
   ```

---

## STEP 2 — Download JavaFX SDK 21

1. Go to: https://gluonhq.com/products/javafx/
2. Select:
   - Version: **21 (LTS)**
   - Operating System: **Windows**
   - Architecture: **x64**
   - Type: **SDK**
3. Click **Download**
4. Extract the ZIP file to a permanent location, example:
   ```
   C:\javafx-sdk-21\
   ```
   > ⚠️ Do NOT put it inside the project folder.

---

## STEP 3 — Download MySQL Connector/J

1. Go to: https://dev.mysql.com/downloads/connector/j/
2. Select **Platform Independent**
3. Download the **ZIP Archive**
4. Extract and locate the file:
   ```
   mysql-connector-j-x.x.x.jar
   ```
5. Copy that `.jar` file to your project's `lib/` folder:
   ```
   OOP-Final-Project/lib/mysql-connector-j-x.x.x.jar
   ```
   > If `lib/` folder doesn't exist, create it manually.

---

## STEP 4 — Setup IntelliJ Project Structure

### 4A — Open the Project
1. Open **IntelliJ IDEA**
2. Click **Open**
3. Navigate to and select the `OOP-Final-Project` folder
4. Click **OK** / **Trust Project**

### 4B — Set the JDK
1. Go to **File → Project Structure** (`Ctrl+Alt+Shift+S`)
2. Under **Project**, set:
   - SDK: **Java 21**
   - Language Level: **21**
3. Click **Apply**

### 4C — Add JavaFX SDK as a Library
1. Still in **Project Structure**, go to **Libraries**
2. Click **+** → **Java**
3. Navigate to your JavaFX SDK folder:
   ```
   C:\javafx-sdk-21\lib\
   ```
4. Select the `lib` folder and click **OK**
5. Name it `JavaFX 21` and click **OK**
6. Click **Apply**

### 4D — Add MySQL Connector as a Library
1. Still in **Libraries**, click **+** → **Java**
2. Navigate to your project's `lib/` folder
3. Select the `mysql-connector-j-x.x.x.jar` file
4. Click **OK** → **Apply** → **OK**

### 4E — Set VM Options for JavaFX
1. Go to **Run → Edit Configurations**
2. Select your main run configuration (or click **+** → **Application**)
3. Set **Main class** to: `application.HelloApplication` *(or your actual main class)*
4. Under **VM Options**, add:
   ```
   --module-path "C:\javafx-sdk-21\lib" --add-modules javafx.controls,javafx.fxml
   ```
5. Click **Apply** → **OK**

---

## STEP 5 — Setup the Database

### 5A — Open MySQL Workbench
1. Open **MySQL Workbench**
2. Connect to **Local instance 3306**
3. Enter password: `Projectgian27`

### 5B — Run the Schema
1. Click **File → Open SQL Script**
2. Navigate to your project folder → `database/` → `schema.sql`
3. Click **Open**
4. Press **Ctrl + Shift + Enter** to run all
5. On the left panel, right-click **Schemas** → **Refresh All**
6. Verify that `pass_slip_db` now appears with tables:
   - `user`
   - `employee`
   - `pass_slip`
   - `activity_logs`

---

## STEP 6 — Verify DBConnection.java

Make sure the database credentials in `main.utils/DBConnection.java` match:

```java
private static final String URL = "jdbc:mysql://localhost:3306/pass_slip_db";
private static final String USER = "root";
private static final String PASSWORD = "Projectgian27";
```

---

## STEP 7 — Run the Application

1. In IntelliJ, open the **Project** panel on the left
2. Navigate to your main class (e.g., `HelloApplication.java`)
3. Right-click → **Run**
4. OR press **Shift + F10**

> ✅ If everything is set up correctly, the Login screen should appear.

---

## ⚠️ COMMON ERRORS & FIXES

| Error | Fix |
|-------|-----|
| `Cannot run program... java.exe` | Fix SDK path in File → Project Structure |
| `JavaFX runtime components are missing` | Add VM options in Run Configuration |
| `Communications link failure` | Make sure MySQL Server is running |
| `Access denied for user 'root'` | Check password in DBConnection.java |
| `Unknown database 'pass_slip_db'` | Run schema.sql first in MySQL Workbench |

---

## 📁 PROJECT STRUCTURE REFERENCE

```
OOP-Final-Project/
├── src/
│   ├── controllers/     # JavaFX Controllers
│   ├── main.models/          # Java Model Classes
│   ├── views/           # UI Logic
│   ├── main.dao/             # Database Access Objects
│   └── main.utils/           # Helper Classes
├── resources/
│   ├── fxml/            # JavaFX FXML files
│   └── css/             # Stylesheets
├── database/
│   └── schema.sql       # Run this first!
├── docs/                # Documentation
└── lib/                 # Put .jar files here (not in GitHub)
```

---

## 👥 TEAM

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

---

*Polytechnic University of the Philippines – Santa Rosa Campus*
*Object Oriented Programming (OOP) Final Project*
*Instructor: Engr. Emy Lou G. Alinsod*
*Presentation Date: June 17, 2026*
