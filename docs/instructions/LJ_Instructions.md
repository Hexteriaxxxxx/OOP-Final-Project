# 👤 LJ — JDBC Developer 1
## Role: Database Connection + Query Testing

---

## 🛠️ SETUP (11:00 AM)

### Step 1 — Accept GitHub Invite
- Buksan email/GitHub → Accept Invitation

### Step 2 — I-clone ang Repo
1. IntelliJ → **Get from VCS**
2. I-paste: `https://github.com/Hexteriaxxxxx/OOP-Final-Project`
3. Click **Clone**

### Step 3 — I-install ang MySQL
- https://dev.mysql.com/downloads/mysql
- Windows MSI Installer (malaki ~450MB)
- Root password: `Projectgian27`

### Step 4 — I-download ang MySQL Connector
- https://dev.mysql.com/downloads/connector/j → Platform Independent → .zip
- Kunin ang `.jar` → i-paste sa `lib/mysql-connector-j-9.7.0/`

### Step 5 — I-setup IntelliJ Libraries
1. **File → Project Structure → Libraries → +**
2. I-add ang `lib/javafx-sdk-21/lib`
3. I-add ang `lib/mysql-connector-j-9.7.0/mysql-connector-j-9.7.0.jar`
4. **Apply → OK**

---

## 💻 YOUR TASK
1. I-test ang `DBConnection.java` — verify na nakaconnect sa database
2. Gumawa ng `DBTest.java` — simple test class
3. I-verify ang lahat ng DAO queries (UserDAO, EmployeeDAO, PassSlipDAO)

### DBTest.java:
```java
package utils;

import java.sql.Connection;

public class DBTest {
    public static void main(String[] args) {
        Connection conn = DBConnection.getConnection();
        if (conn != null) {
            System.out.println("✅ Database connected successfully!");
        } else {
            System.out.println("❌ Connection failed!");
        }
    }
}
```

---

## 🤖 CLAUDE PROMPT

```
Ikaw ay isang expert Java JDBC developer na tumutulong sa akin.

Ang project namin ay: Employee Pass Slip Request, Issuance and Monitoring System

May existing DBConnection.java kami:
- Host: localhost
- Port: 3306
- Database: pass_slip_db
- Username: root
- Password: Projectgian27

May existing DAOs:
- UserDAO.java (login, register, usernameExists)
- EmployeeDAO.java (getAllEmployees, getById, add, update, delete, search)
- PassSlipDAO.java (issuePassSlip, recordTimeIn, getAllPassSlips, getTodayPassSlips, getActivePassSlips)

Kailangan ko:
1. I-verify kung tama ang DBConnection.java
2. Gumawa ng ActivityLogDAO.java na may:
   - logAction(int empId, String action, int performedBy) method
   - getRecentLogs(int limit) method
   - getLogsByEmployee(int empId) method
3. Kung may error sa connection — tulungan mo akong i-fix

[Ilagay mo dito ang specific na tanong o error]
```

---

## 📤 GIT PUSH
1. **Git → Pull** muna
2. **Git → Commit** (Ctrl+K)
3. I-check lang:
   - ✅ src/utils/DBTest.java
   - ✅ src/dao/ActivityLogDAO.java
4. Commit message: `"Add JDBC connection test and ActivityLogDAO - LJ"`
5. **Commit and Push → Push**

## ⚠️ REMINDERS
- Huwag i-push ang `lib/` folder
- Kung hindi makaconnect sa database — koordinate kay Kevin
