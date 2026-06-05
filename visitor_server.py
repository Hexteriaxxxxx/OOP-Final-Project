"""
visitor_server.py - Visitor Request Bridge Server (Supabase/PostgreSQL)
- Receives Google Form submissions -> saves to Supabase
- Sends approval emails WITH PDF pass slip attachment
"""

from flask import Flask, request, jsonify
from flask_cors import CORS
import psycopg2
from datetime import datetime
import os, socket, re, smtplib, io
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
from email.mime.application import MIMEApplication

app = Flask(__name__)
CORS(app)

def load_env(path=".env"):
    env = {}
    if not os.path.exists(path):
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
    "port":     int(ENV.get("DB_PORT", "5432")),
    "dbname":   ENV.get("DB_NAME",     "postgres"),
    "user":     ENV.get("DB_USER",     "postgres"),
    "password": ENV.get("DB_PASSWORD", ""),
    "sslmode":  "require"
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

def get_conn():
    return psycopg2.connect(**DB_CONFIG)


# ── Generate PDF Pass Slip ────────────────────────────────────────
def generate_pass_slip_pdf(data):
    try:
        from reportlab.lib.pagesizes import A6
        from reportlab.lib import colors
        from reportlab.lib.units import mm
        from reportlab.platypus import SimpleDocTemplate, Table, TableStyle, Paragraph, Spacer
        from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
        from reportlab.lib.enums import TA_CENTER, TA_LEFT

        buffer = io.BytesIO()
        # A6 size (postcard/slip size) — 105mm x 148mm
        doc = SimpleDocTemplate(
            buffer,
            pagesize=A6,
            rightMargin=8*mm, leftMargin=8*mm,
            topMargin=6*mm, bottomMargin=6*mm
        )

        styles = getSampleStyleSheet()
        elements = []

        # ── Title ──
        title_style = ParagraphStyle(
            'Title', fontSize=16, fontName='Helvetica-Bold',
            alignment=TA_CENTER, textColor=colors.HexColor('#8B0000'),
            spaceAfter=4*mm
        )
        elements.append(Paragraph("VISITOR PASS SLIP", title_style))

        # ── School name ──
        school_style = ParagraphStyle(
            'School', fontSize=9, fontName='Helvetica',
            alignment=TA_CENTER, textColor=colors.HexColor('#555555'),
            spaceAfter=4*mm
        )
        elements.append(Paragraph("PUP Santa Rosa Campus", school_style))

        # ── Info table ──
        label_style = ParagraphStyle('Label', fontSize=8, fontName='Helvetica-Bold', textColor=colors.HexColor('#333'))
        value_style = ParagraphStyle('Value', fontSize=9, fontName='Helvetica', textColor=colors.black)

        request_id  = data.get("request_id",   "VIS-0000")
        name        = data.get("visitor_name", "-")
        company     = data.get("company",      "-")
        purpose     = data.get("purpose",      "-")
        visit_date  = data.get("visit_date",   "-")
        host        = data.get("host_employee","-")

        info_data = [
            [Paragraph("Request ID:", label_style),  Paragraph(request_id, value_style)],
            [Paragraph("Visitor Name:", label_style), Paragraph(name, value_style)],
            [Paragraph("Company:", label_style),     Paragraph(company, value_style)],
            [Paragraph("Purpose:", label_style),     Paragraph(purpose, value_style)],
            [Paragraph("Visit Date:", label_style),  Paragraph(visit_date, value_style)],
            [Paragraph("Host Employee:", label_style),Paragraph(host, value_style)],
        ]

        info_table = Table(info_data, colWidths=[35*mm, 55*mm])
        info_table.setStyle(TableStyle([
            ('FONTSIZE',     (0,0), (-1,-1), 8),
            ('ROWBACKGROUNDS',(0,0),(-1,-1),[colors.HexColor('#FFF5F5'), colors.white]),
            ('GRID',         (0,0), (-1,-1), 0.5, colors.HexColor('#DDDDDD')),
            ('VALIGN',       (0,0), (-1,-1), 'MIDDLE'),
            ('TOPPADDING',   (0,0), (-1,-1), 3),
            ('BOTTOMPADDING',(0,0), (-1,-1), 3),
            ('LEFTPADDING',  (0,0), (-1,-1), 4),
        ]))
        elements.append(info_table)
        elements.append(Spacer(1, 4*mm))

        # ── Status badge ──
        approved_style = ParagraphStyle(
            'Approved', fontSize=13, fontName='Helvetica-Bold',
            alignment=TA_CENTER, textColor=colors.white,
            backColor=colors.HexColor('#1D9E75'),
            borderPadding=(4, 10, 4, 10), spaceAfter=4*mm
        )
        elements.append(Paragraph("✓  APPROVED", approved_style))
        elements.append(Spacer(1, 3*mm))

        # ── Instructions ──
        note_style = ParagraphStyle(
            'Note', fontSize=7, fontName='Helvetica',
            alignment=TA_CENTER, textColor=colors.HexColor('#666'),
            spaceAfter=2*mm
        )
        elements.append(Paragraph("Present this slip at the entrance on your visit date.", note_style))
        elements.append(Paragraph("This pass is valid for the visit date indicated only.", note_style))
        elements.append(Spacer(1, 3*mm))

        # ── Signature line ──
        sig_style = ParagraphStyle(
            'Sig', fontSize=7, fontName='Helvetica',
            alignment=TA_CENTER, textColor=colors.HexColor('#333')
        )
        elements.append(Paragraph("_________________________", sig_style))
        elements.append(Paragraph("Signature of Administrative Officer", sig_style))
        elements.append(Spacer(1, 2*mm))

        # ── Footer ──
        footer_style = ParagraphStyle(
            'Footer', fontSize=6, fontName='Helvetica',
            alignment=TA_CENTER, textColor=colors.HexColor('#999')
        )
        elements.append(Paragraph(f"Generated by PUP Pass Slip System • {datetime.now().strftime('%Y-%m-%d %H:%M')}", footer_style))

        doc.build(elements)
        buffer.seek(0)
        return buffer.read()

    except ImportError:
        print("[WARN] reportlab not installed — installing...")
        os.system("pip install reportlab")
        return None
    except Exception as e:
        print(f"[ERR] PDF generation failed: {e}")
        return None


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
            INSERT INTO "Visitor"
                (visitor_name, company, purpose, time_out,
                 host_employee, email, contact, status)
            VALUES (%s, %s, %s, %s, %s, %s, %s, 'Pending')
            RETURNING visitor_id
        """, (
            data.get("visitor_name",  "").strip(),
            data.get("company",       "").strip(),
            data.get("purpose",       "").strip(),
            dt_out,
            data.get("host_employee", "").strip(),
            data.get("email",         "").strip(),
            data.get("contact",       "").strip(),
        ))
        new_id = cursor.fetchone()[0]
        conn.commit()
        cursor.close(); conn.close()

        request_id = f"VIS-{new_id:04d}"
        print(f"[OK] Saved! {request_id}")
        return jsonify({"success": True, "request_id": request_id, "visitor_id": new_id})

    except Exception as e:
        print(f"[ERR] {e}")
        return jsonify({"success": False, "error": str(e)}), 500


# ── Send Approval Email WITH PDF Pass Slip ───────────────────────
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

        print(f"\n[EMAIL] Sending approval + PDF to: {to_email}")

        if not to_email:
            return jsonify({"success": False, "error": "No email address"}), 400
        if not GMAIL_USER or not GMAIL_PASSWORD:
            return jsonify({"success": False, "error": "Gmail not configured"}), 500

        # ── Build email ──
        msg = MIMEMultipart()
        msg["From"]    = GMAIL_USER
        msg["To"]      = to_email
        msg["Subject"] = f"✅ Visitor Request APPROVED - {request_id} | PUP Santa Rosa"

        body = f"""Dear {name},

Your visitor request has been APPROVED! 🎉

Please find your VISITOR PASS SLIP attached to this email (PassSlip_{request_id}.pdf).
Print it and present it at the entrance on your visit date.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  VISITOR DETAILS
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  Request ID   : {request_id}
  Name         : {name}
  Company      : {company}
  Purpose      : {purpose}
  Visit Date   : {visit_date}
  Host Employee: {host}
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

INSTRUCTIONS:
1. Print the attached PDF pass slip
2. Present it at the entrance
3. This pass is valid for the visit date only

Thank you!
PUP Santa Rosa - Pass Slip System
"""
        msg.attach(MIMEText(body, "plain"))

        # ── Generate and attach PDF ──
        pdf_data = generate_pass_slip_pdf({
            "request_id":    request_id,
            "visitor_name":  name,
            "company":       company,
            "purpose":       purpose,
            "visit_date":    visit_date,
            "host_employee": host,
        })

        if pdf_data:
            pdf_attachment = MIMEApplication(pdf_data, _subtype="pdf")
            pdf_attachment.add_header(
                'Content-Disposition', 'attachment',
                filename=f"PassSlip_{request_id}.pdf"
            )
            msg.attach(pdf_attachment)
            print(f"[OK] PDF pass slip generated and attached")
        else:
            print(f"[WARN] PDF generation failed — sending email without attachment")

        # ── Send ──
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

    # Install reportlab if needed
    try:
        import reportlab
    except ImportError:
        print("[SETUP] Installing reportlab...")
        os.system("pip install reportlab")

    print("=" * 55)
    print("  PUP Santa Rosa - Visitor Bridge Server")
    print("=" * 55)
    print(f"  Local   : http://localhost:5055")
    print(f"  Network : http://{local_ip}:5055")
    print(f"  DB      : Supabase (PostgreSQL)")
    print(f"  Gmail   : {'configured' if GMAIL_USER else 'NOT configured'}")
    print(f"  PDF     : Pass slip attachment enabled")
    print("=" * 55)

    app.run(host="0.0.0.0", port=5055, debug=False)
