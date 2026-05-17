# 👤 JM & RYKEN — UI/UX Designer
## Role: UI Design Finalization + Screen References

---

## 🛠️ SETUP (11:00 AM)

### Step 1 — Accept GitHub Invite
- Buksan email/GitHub → Accept Invitation

### Step 2 — I-clone ang Repo
1. IntelliJ → **Get from VCS**
2. I-paste: `https://github.com/Hexteriaxxxxx/OOP-Final-Project`
3. Click **Clone**

---

## 💻 YOUR TASK

### Priority 1 — Export existing screens as PNG:
Yung mga screens na nagawa na — i-export as PNG at i-save sa:
`docs/ui-screenshots/`

Screens na kailangan i-export:
- ✅ Login Screen (Staff)
- ✅ Login Screen (Admin)
- ✅ Register Screen (Staff)
- ✅ Register Screen (Admin)
- ✅ Dashboard

### Priority 2 — Gumawa ng remaining screens:
- Pass Slip Issuance Form
- Monitoring & Logs Screen
- Report Generation Screen
- Employee Management Screen
- Visitor Time-in/Time-out Screen

### Priority 3 — I-export lahat as PNG:
I-save sa `docs/ui-screenshots/` at i-push sa GitHub para reference ng devs

---

## 🤖 CLAUDE PROMPT (para sa screen designs)

```
Ikaw ay isang expert UI/UX designer na tumutulong sa akin na gumawa ng screen designs.

Ang project namin ay: Employee Pass Slip Request, Issuance and Monitoring System

Color theme: Dark red (#8B0000) sidebar, white content area

Existing screens na nagawa na:
- Login Screen (Staff/Admin toggle, Username, Password, Remember me, Sign In button)
- Register Screen (Staff/Admin toggle, Full Name, Email, Username, Password, Confirm Password)
- Dashboard (Sidebar nav, stat cards, pass slip table, notifications panel)

Kailangan ko ng description/layout ng:
1. Pass Slip Issuance Form:
   - Employee dropdown selector
   - Purpose/Reason text field
   - Date (auto-filled)
   - Time Out (auto-filled)
   - Submit button, Cancel button

2. Monitoring & Logs Screen:
   - Table ng lahat ng pass slips (ID, Employee, Department, Purpose, Time Out, Time In, Duration, Status)
   - Search/Filter by date, department
   - Status badges (Active = orange, Returned = green)

3. Report Generation Screen:
   - Daily report tab
   - Monthly report tab
   - Date picker
   - Generate button
   - Table ng results
   - Export/Print button

Ilarawan mo ang layout at color placement para magawa ko sa Figma.
```

---

## 📤 GIT PUSH
1. **Git → Pull** muna
2. I-copy ang PNG screenshots sa `docs/ui-screenshots/` folder ng project
3. **Git → Commit** (Ctrl+K)
4. I-check lang: ✅ docs/ui-screenshots/ (lahat ng PNG)
5. Commit message: `"Add UI screenshots - JM/Ryken"`
6. **Commit and Push → Push**

## ⚠️ REMINDERS
- Hindi kayo mag-co-code — designers kayo, reference lang ang gagawin ninyo
- I-coordinate kay Emil/Evasco at Nico/Josiah/Yuan para alam nila ang exact design
