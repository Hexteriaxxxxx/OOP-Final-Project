# 👤 GIAN — Project Manager
## Role: Monitor, Coordinate, Review, and Assist

---

## 🛠️ SETUP (11:00 AM)

### Before the meeting:
- [ ] I-verify na naka-accept na lahat ng GitHub invites
- [ ] I-push ang latest files sa GitHub (current project structure)
- [ ] I-print o i-share ang instructions per role sa bawat member
- [ ] I-update ang Trello — lahat ng W2 cards sa To-do

---

## ⏰ YOUR SCHEDULE BUKAS

### 11:00 AM — Setup Phase
- I-assist ang lahat sa GitHub clone
- I-verify na naka-setup ang IntelliJ ng bawat member
- Si Kevin — tulungan sa MySQL setup at i-run ang schema.sql
- Si LJ — tulungan sa database connection test

### 12:00 PM — Development Phase
- I-monitor ang progress ng bawat member
- Mag-coordinate sa pagitan ng UI (JM/Ryken) at Devs (Emil/Evasco/Nico/Josiah/Yuan)
- I-update ang Trello board habang may natapos

### 2:00 PM — Integration Phase
- I-assist sa pag-merge ng Login + Dashboard
- I-test ang database connection
- I-coordinate ang bug fixes

### 3:30 PM — Git Push Phase
- I-verify na naka-push lahat ng members
- I-check ang GitHub repo kung kumpleto ang files

### 4:00 PM — QA Phase
- I-review ang bug reports ni Josiah/Evasco
- I-assign ang bug fixes sa tamang developer
- I-update ang Trello (move to Done)

### 5:00 PM — Wrap Up
- I-summarize ang nagawa ngayong araw
- I-set ang next tasks para Week 3
- I-send sa GC ang recap message

---

## 💻 YOUR TASK (Code)

### Main.java — Entry point ng application:
```java
package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
        primaryStage.setTitle("Pass Slip Issuance System");
        primaryStage.setScene(new Scene(root, 1280, 720));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

I-save sa: `src/main/Main.java`

---

## 🤖 CLAUDE PROMPT (Para sa PM tasks)

```
Ikaw ay isang expert project manager at Java developer na tumutulong sa akin.

Ang project namin ay: Employee Pass Slip Request, Issuance and Monitoring System

Ako ang Project Manager. Kailangan ko ng tulong sa:
[Ilagay mo dito ang specific na tanong — hal. may bug report, may conflict sa GitHub, etc.]

Project context:
- JavaFX + MySQL + JDBC
- GitHub repo: https://github.com/Hexteriaxxxxx/OOP-Final-Project
- Database: pass_slip_db (localhost:3306, root, Projectgian27)
- 11 members, presentation: June 17, 2026
```

---

## 📤 GIT PUSH
1. **Git → Pull** muna
2. **Git → Commit** (Ctrl+K)
3. I-check: ✅ src/main/Main.java + lahat ng updated files
4. Commit message: `"Add Main entry point and project setup - Gian"`
5. **Commit and Push → Push**

---

## GC RECAP MESSAGE TEMPLATE (I-send sa 5PM):

```
✅ Day 1 Coding Session — DONE!

Nagawa natin ngayon:
- [i-list mo dito ang mga nagawa]

In progress:
- [i-list mo dito ang hindi pa tapos]

Bugs found:
- [i-list mo dito ang bugs]

Next session:
- [kailan ang susunod]
- [ano ang target]

Salamat sa lahat! 💪 June 17 kaya natin! 🎯
```
