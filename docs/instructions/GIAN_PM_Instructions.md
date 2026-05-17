# 👤 GIAN — Project Manager
## Role: Monitor, Coordinate, Review, and Assist

---

## 🛠️ SETUP (11:00 AM)

### Before the meeting:
- [ ] I-verify na naka-accept na lahat ng GitHub invites
- [ ] I-share ang instructions per role sa bawat member
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
- Mag-coordinate sa pagitan ng UI (JM/Ryken) at Devs
- I-update ang Trello board habang may natapos

### 2:00 PM — Integration Phase
- I-assist sa pag-merge ng Login + Dashboard
- I-test ang database connection
- I-coordinate ang bug fixes

### 3:30 PM — Git Push Phase
- I-verify na naka-push lahat ng members
- I-check ang GitHub repo

### 4:00 PM — QA Phase
- I-review ang bug reports ni Josiah/Evasco
- I-assign ang bug fixes sa tamang developer
- I-update ang Trello (move to Done)

### 5:00 PM — Wrap Up
- I-summarize ang nagawa
- I-set ang next tasks para Week 3
- I-send sa GC ang recap message

---

## 💻 YOUR TASK (Code)

Gumawa ng `Main.java` — entry point ng application.

I-save sa: `src/main/Main.java`

### 🤖 CLAUDE PROMPT (I-paste mo ito sa Claude):

```
Ikaw ay isang expert Java developer na tumutulong sa akin.

Ang project namin ay: Employee Pass Slip Request, Issuance and Monitoring System

Kailangan ko ng Main.java na may:
- Package: main
- Extends Application (JavaFX)
- start() method na naglo-load ng Login.fxml
- Window title: "Pass Slip Issuance System"
- Window size: 1280x720
- setResizable(false)

FXML path: /fxml/Login.fxml
```

---

## 📤 GIT PUSH
1. **Git → Pull** muna (Ctrl+T)
2. **Git → Commit** (Ctrl+K)
3. I-check: ✅ src/main/Main.java
4. Commit message: `"Add Main entry point - Gian"`
5. **Commit and Push → Push**

---

## GC RECAP MESSAGE (I-send sa 5PM):

```
✅ Day 1 Coding Session — DONE!

Nagawa natin ngayon:
- [i-list mo dito]

In progress:
- [i-list mo dito]

Bugs found:
- [i-list mo dito]

Next session:
- [kailan]
- [target]

Salamat sa lahat! 💪 June 17 kaya natin! 🎯
```
