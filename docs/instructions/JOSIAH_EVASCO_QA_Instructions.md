# 👤 JOSIAH & EVASCO — QA / Tester
## Role: Testing + Bug Reporting

---

## 🛠️ SETUP (11:00 AM)

### Step 1 — Accept GitHub Invite
- Buksan email/GitHub → Accept Invitation

### Step 2 — I-clone ang Repo
1. IntelliJ → **Get from VCS**
2. I-paste: `https://github.com/Hexteriaxxxxx/OOP-Final-Project`
3. Click **Clone**

### Step 3 — I-install ang MySQL
- https://dev.mysql.com/downloads/mysql → Windows MSI Installer
- Root password: `Projectgian27`

---

## 💻 YOUR TASK

### Testing Schedule:
- **11AM-2PM** — Tulungan ang devs sa setup (ikaw rin dev — Josiah)
- **2PM onwards** — Magsimula ng testing pagkatapos may initial working code

### Test Cases na Gagawin:

**Login Testing:**
- [ ] Valid staff login → dapat mapunta sa staff dashboard
- [ ] Valid admin login → dapat mapunta sa admin dashboard
- [ ] Invalid credentials → dapat may error message
- [ ] Empty fields → dapat may validation message
- [ ] Wrong role (staff credentials sa admin tab) → dapat may error

**Database Connection Testing:**
- [ ] May connection sa database → success message
- [ ] Walang internet/MySQL off → graceful error message

**Pass Slip Testing:**
- [ ] Issue pass slip → dapat ma-save sa database
- [ ] Time-in recording → dapat ma-update ang record
- [ ] Duration calculation → dapat tama ang computation

### Bug Report Format:
```
Bug #: [number]
Screen: [Login/Dashboard/etc]
Steps to reproduce:
1. [step 1]
2. [step 2]
Expected: [ano dapat mangyari]
Actual: [ano nangyari]
Screenshot: [attach]
```

---

## 🤖 CLAUDE PROMPT

```
Ikaw ay isang expert QA tester na tumutulong sa akin.

Ang project namin ay: Employee Pass Slip Request, Issuance and Monitoring System (JavaFX + MySQL)

Ang screens na kailangan i-test:
- Login Screen (Staff/Admin)
- Register Screen
- Dashboard
- Pass Slip Issuance Form

Kailangan ko ng:
1. Comprehensive test cases para sa bawat screen
2. Test case format: Test ID, Description, Steps, Expected Result, Actual Result, Status
3. Edge cases na dapat i-test (empty fields, wrong password, duplicate username, etc.)

[Ilagay mo dito ang specific na tanong o bug na nahanap mo]
```

---

## 📤 GIT PUSH
1. **Git → Pull** muna
2. **Git → Commit** (Ctrl+K)
3. I-check lang: ✅ docs/QA_TestCases.md (o kahit anong doc na nagawa mo)
4. Commit message: `"Add QA test cases and bug reports - Josiah/Evasco"`
5. **Commit and Push → Push**

## ⚠️ REMINDERS
- Huwag baguhin ang source code — ireport lang ang bugs kay Gian
- I-screenshot lahat ng bugs
- I-document lahat ng test results
