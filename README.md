# 🛡 Employee Pass Slip Request, Issuance and Monitoring System
### PUP Santa Rosa — OOP Final Project 2026

---

## 📋 Requirements

| Tool | Version | Download |
|------|---------|----------|
| Java JDK | 21 | https://adoptium.net |
| JavaFX SDK | 21.0.11 | https://gluonhq.com/products/javafx |
| MySQL | 9.7+ | https://dev.mysql.com/downloads/mysql |
| MySQL Connector/J | 9.7.0 | https://dev.mysql.com/downloads/connector/j |
| Python | 3.8+ | https://python.org (for Visitor web form only) |

---

## ⚙️ Setup Steps

### 1. Clone the repository
```
git clone https://github.com/Hexteriaxxxxx/OOP-Final-Project.git
cd OOP-Final-Project
```

### 2. Setup MySQL Database
Open MySQL and run:
```sql
source database/schema.sql
```
Then run the alter script:
```sql
source database/alter_visitor.sql
```

### 3. Configure your database credentials
Copy `.env.example` to `.env` and fill in your MySQL password:
```
DB_HOST=localhost
DB_PORT=3306
DB_NAME=pass_slip_db
DB_USER=root
DB_PASSWORD=your_password_here
```

### 4. Setup JavaFX in IntelliJ
1. Download JavaFX SDK 21.0.11 from https://gluonhq.com/products/javafx
2. Extract to your `Downloads` folder
3. In IntelliJ: **File → Project Structure → Libraries**
4. Remove old JavaFX library (if any)
5. Click `+` → Java → navigate to `javafx-sdk-21.0.11/lib`
6. Click OK → Apply

### 5. Add VM Options
In IntelliJ: **Edit Configurations → Modify Options → Add VM options**
```
--module-path "C:\Users\YOUR_USERNAME\Downloads\javafx-sdk-21.0.11\lib" --add-modules javafx.controls,javafx.fxml
```
> ⚠️ Replace `YOUR_USERNAME` with your actual Windows username

### 6. Add MySQL Connector
1. Download MySQL Connector/J 9.7.0
2. Copy the `.jar` file to the `lib/` folder
3. In IntelliJ: **File → Project Structure → Libraries → +** → add the jar

### 7. Run the app
- Click **Run** ▶️ in IntelliJ
- Default admin login: `admin` / `admin123`

---

## 🌐 Visitor Online Form (Optional)

This allows outside visitors to submit requests via Google Forms.

### Setup:
```bash
pip install flask flask-cors mysql-connector-python
python visitor_server.py
```

The server will display your local IP address. Copy it and put it in the Google Apps Script.

See `visitor_google_script.js` for the full Google Apps Script code.

---

## 👥 Team Members

| Name | Role |
|------|------|
| Gian Santos | Project Manager |
| Emil Fernandez | System Analyst / JavaFX Dev |
| Ryken Lapating | UI/UX Designer |
| JM Delfino | UI/UX Designer |
| Josiah Evasco | JavaFX Dev / QA |
| Nico Ancheta | JavaFX Dev |
| Josiah David | JavaFX Dev |
| Yuan Paolo | JavaFX Dev |
| Kevin Brian | Database Designer |
| LJ Catindig | JavaFX Dev |
| Karl | JavaFX Dev |

---

## 📁 Project Structure
```
OOP Final Project/
├── src/
│   ├── main/
│   │   ├── controllers/     ← JavaFX controllers
│   │   ├── resources/fxml/  ← FXML UI files
│   │   └── utils/           ← DBConnection, etc.
│   ├── dao/                 ← Database access objects
│   └── models/              ← Java model classes
├── database/
│   ├── schema.sql           ← Run this first
│   └── alter_visitor.sql    ← Run this second
├── resources/
│   └── visitor_form.html    ← Public visitor form
├── visitor_server.py        ← Python bridge server
├── visitor_google_script.js ← Google Apps Script
├── .env.example             ← Copy to .env, add your password
└── README.md
```
