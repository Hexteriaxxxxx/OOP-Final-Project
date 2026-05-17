# 👤 KEVIN — Database Designer
## Role: Database Setup + Schema Verification

---

## 🛠️ SETUP (11:00 AM)

### Step 1 — Accept GitHub Invite
- Buksan email/GitHub → Accept Invitation

### Step 2 — I-clone ang Repo
1. I-install ang IntelliJ kung wala pa: https://www.jetbrains.com/idea/download
2. Buksan IntelliJ → **Get from VCS**
3. I-paste: `https://github.com/Hexteriaxxxxx/OOP-Final-Project`
4. Click **Clone**

### Step 3 — I-install ang MySQL
- Pumunta sa: https://dev.mysql.com/downloads/mysql
- Download: **Windows (x86, 64-bit), MSI Installer** (yung malaki ~450MB)
- I-install, i-set ang root password: `Projectgian27`
- I-install din ang **MySQL Workbench** (kasama sa installer)

---

## 💻 YOUR TASK

### I-run ang Schema:
1. Buksan ang **MySQL Workbench**
2. Connect sa localhost (user: root, password: `Projectgian27`)
3. Click **File → Open SQL Script**
4. Hanapin ang `database/schema.sql` sa project folder
5. Click yung **⚡ lightning bolt** para i-run
6. Verify — dapat makita ang `pass_slip_db` na may 4 tables:
   - ✅ User
   - ✅ Employee
   - ✅ Pass_slip
   - ✅ Activity_logs

### I-verify ang Tables:
```sql
USE pass_slip_db;
SHOW TABLES;
SELECT * FROM User;
SELECT * FROM Employee;
```

---

## 🤖 CLAUDE PROMPT

```
Ikaw ay isang expert sa MySQL database na tumutulong sa akin.

Ang project namin ay: Employee Pass Slip Request, Issuance and Monitoring System

Ang aming database name ay: pass_slip_db

May 4 tables kami:
1. User (user_id, full_name, email, username, password, role, created_at)
2. Employee (emp_id, name, department, position, created_at)
3. Pass_slip (slip_id, emp_id, reason, time_out, time_in, duration, issued_by, status, created_at)
4. Activity_logs (log_id, emp_id, action, timestamp, performed_by)

[Ilagay mo dito ang iyong tanong]
```

---

## 📤 GIT PUSH
1. **Git → Pull** muna
2. **Git → Commit** (Ctrl+K)
3. I-check lang: ✅ database/schema.sql
4. Commit message: `"Update database schema - Kevin"`
5. **Commit and Push → Push**

## ⚠️ REMINDERS
- Huwag i-push ang `lib/` folder
- Ipasabi kay Gian kung may error sa database
