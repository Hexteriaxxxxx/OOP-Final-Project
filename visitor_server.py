"""
visitor_server.py - Visitor Request Bridge Server
- Receives Google Form submissions -> saves to MySQL
- Sends approval emails when admin approves a visitor

HOW TO RUN:
  1. pip install flask flask-cors mysql-connector-python
  2. Add to .env:
       GMAIL_USER=your.email@gmail.com
       GMAIL_APP_PASSWORD=xxxx xxxx xxxx xxxx
       VISITOR_SERVER_URL=https://your-ngrok-url.ngrok-free.app
  3. python visitor_server.py
"""

from flask import Flask, request, jsonify
from flask_cors import CORS
import mysql.connector
from datetime import datetime
import os, socket, re, smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart

app = Flask(__name__)
CORS(app)

def load_env(path=".env"):
    env = {}
    if not os.path.exists(path):
        print("Warning: .env file not found!")
        return env
    with open(path, encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if line and not line.startswith("#") and "=" in line:
                k, v = line.split("=", 1)
                env[k.strip()] = v.strip()
    return env

ENV = load_env()

DB_CONFIG = {
    "host":     ENV.get("DB_HOST",     "localhost"),
    "port":     int(ENV.get("DB_PORT", "3306")),
    "database": ENV.get("DB_NAME",     "pass_slip_db"),
    "user":     ENV.get("DB_USER",     "root"),
    "password": ENV.get("DB_PASSWORD", ""),
}

GMAIL_USER     = ENV.get("GMAIL_USER",         "")
GMAIL_PASSWORD = ENV.get("GMAIL_APP_PASSWORD", "")
SCRIPT_FILE    = "visitor_google_script.js"

def get_local_ip():
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
        s.close()
        return ip
    except:
        return "localhost"

def auto_update_script_ip(ip):
    if not os.path.exists(SCRIPT_FILE):
        return
    with open(SCRIPT_FILE, "r", encoding="utf-8") as f:
        content = f.read()
    new_url  = f"http://{ip}:5055/submit-visitor"
    new_line = f'var SERVER_URL = "{new_url}";'
    updated  = re.sub(r'var SERVER_URL\s*=\s*"[^"]*";', new_line, content)
    if updated != content:
        with open(SCRIPT_FILE, "w", encoding="utf-8") as f:
            f.write(updated)
        print(f"[OK] Auto-updated SERVER_URL -> {new_url}")
        print(f"[!]  Re-paste visitor_google_script.js into Apps Script!")
    else:
        print(f"[OK] SERVER_URL already up to date: {new_url}")

def get_conn():
    return mysql.connector.connect(**DB_CONFIG)


# ── Submit Visitor (from Google Form) ───────────────────────────
@app.route("/submit-visitor", methods=["POST", "OPTIONS"])
def submit_visitor():
    if request.method == "OPTIONS":
        return jsonify({"ok": True}), 200
    try:
        data = request.get_json(force=True)
        print(f"\n[IN] New visitor: {data.get('visitor_name')} | {data.get('email')}")

        visit_date = data.get("visit_date") or datetime.now().strftime("%Y-%m-%d")
        time_out   = data.get("time_out")   or "17:00"

        try:
            dt_out = datetime.strptime(f"{visit_date} {time_out}", "%Y-%m-%d %H:%M")
        except ValueError:
            dt_out = datetime.now()

        conn   = get_conn()
        cursor = conn.cursor()
        cursor.execute("""
            INSERT INTO Visitor
                (visitor_name, company, purpose, time_out,
                 host_employee, email, contact, status)
            VALUES (%s, %s, %s, %s, %s, %s, %s, 'Pending')
        """, (
            data.get("visitor_name",  "").strip(),
            data.get("company",       "").strip(),
            data.get("purpose",       "").strip(),
            dt_out,
            data.get("host_employee", "").strip(),
            data.get("email",         "").strip(),
            data.get("contact",       "").strip(),
        ))
        conn.commit()
        new_id = cursor.lastrowid
        cursor.close(); conn.close()

        request_id = f"VIS-{new_id:04d}"
        print(f"[OK] Saved! {request_id}")
        return jsonify({"success": True, "request_id": request_id, "visitor_id": new_id})

    except Exception as e:
        print(f"[ERR] {e}")
        return jsonify({"success": False, "error": str(e)}), 500


# ── Send Approval Email (called by Java app) ─────────────────────
@app.route("/send-approval-email", methods=["POST", "OPTIONS"])
def send_approval_email():
    if request.method == "OPTIONS":
        return jsonify({"ok": True}), 200
    try:
        data = request.get_json(force=True)
        to_email   = data.get("email",        "")
        name       = data.get("visitor_name", "Visitor")
        request_id = data.get("request_id",   "VIS-0000")
        company    = data.get("company",      "-")
        purpose    = data.get("purpose",      "-")
        visit_date = data.get("visit_date",   "-")
        host       = data.get("host_employee","-")

        print(f"\n[EMAIL] Sending approval to: {to_email}")

        if not to_email:
            return jsonify({"success": False, "error": "No email address"}), 400

        if not GMAIL_USER or not GMAIL_PASSWORD:
            print("[WARN] Gmail credentials not set in .env — skipping email")
            return jsonify({"success": False, "error": "Gmail not configured"}), 500

        # Build email
        msg = MIMEMultipart()
        msg["From"]    = GMAIL_USER
        msg["To"]      = to_email
        msg["Subject"] = f"Visitor Request APPROVED - {request_id}"

        body = f"""Dear {name},

Your visitor request has been APPROVED!

================================
  VISITOR PASS - {request_id}
================================
  Name        : {name}
  Company     : {company}
  Purpose     : {purpose}
  Visit Date  : {visit_date}
  Host        : {host}
  Request ID  : {request_id}
================================

Please present this Request ID at the entrance:

  >> {request_id} <<

Thank you!
PUP Santa Rosa - Pass Slip System
"""
        msg.attach(MIMEText(body, "plain"))

        # Send via Gmail SMTP
        with smtplib.SMTP("smtp.gmail.com", 587) as server:
            server.starttls()
            server.login(GMAIL_USER, GMAIL_PASSWORD)
            server.send_message(msg)

        print(f"[OK] Approval email sent to: {to_email}")
        return jsonify({"success": True})

    except Exception as e:
        print(f"[ERR] Email failed: {e}")
        return jsonify({"success": False, "error": str(e)}), 500


@app.route("/health", methods=["GET"])
def health():
    return jsonify({"status": "ok"})


if __name__ == "__main__":
    local_ip = get_local_ip()
    auto_update_script_ip(local_ip)

    email_status = "configured" if GMAIL_USER else "NOT configured (add GMAIL_USER to .env)"

    print("=" * 55)
    print("  PUP Santa Rosa - Visitor Bridge Server")
    print("=" * 55)
    print(f"  Local   : http://localhost:5055")
    print(f"  Network : http://{local_ip}:5055")
    print(f"  Gmail   : {email_status}")
    print("=" * 55)

    app.run(host="0.0.0.0", port=5055, debug=False)
