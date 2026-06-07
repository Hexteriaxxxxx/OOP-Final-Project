"""
generate_passlip.py
Usage: python generate_passlip.py <slip_id> <name> <department> <purpose> <time_out> <time_in> <category> <output_path>
"""
import sys
from reportlab.lib.pagesizes import A5, landscape
from reportlab.pdfgen import canvas
from reportlab.lib import colors
from reportlab.lib.units import mm
import datetime

def generate_pass_slip(output_path, slip_id, name, department, purpose, time_out, time_in, category="Official Business"):
    W, H = landscape(A5)
    c = canvas.Canvas(output_path, pagesize=landscape(A5))

    today    = datetime.date.today()
    date_str = today.strftime("%B %d, %Y")

    # ── OUTER BORDER ─────────────────────────────────────────────
    c.setLineWidth(1.5)
    c.rect(8*mm, 8*mm, W - 16*mm, H - 16*mm)

    # ── HEADER ───────────────────────────────────────────────────
    c.setFont("Helvetica-Bold", 9)
    c.drawRightString(W - 12*mm, H - 14*mm, f"Pass ID: PS-{int(slip_id):04d}")

    c.setFont("Helvetica-Bold", 20)
    c.drawCentredString(W/2, H - 23*mm, f"{int(slip_id)}  PASS SLIP")

    c.setFont("Helvetica-Bold", 7.5)
    c.drawCentredString(W/2, H - 28.5*mm, "POLYTECHNIC UNIVERSITY OF THE PHILIPPINES — Santa Rosa Campus")

    c.setLineWidth(0.75)
    c.line(10*mm, H - 31*mm, W - 10*mm, H - 31*mm)

    # ── DATE & CATEGORY ──────────────────────────────────────────
    c.setFont("Helvetica-Bold", 8)
    c.drawString(12*mm, H - 37*mm, f"DATE:   {date_str}")
    c.drawString(W/2,   H - 37*mm, f"CATEGORY:   {category}")

    # ── CALENDAR STRIP ───────────────────────────────────────────
    cur_month     = today.month
    grid_x        = 10*mm
    grid_y        = H - 40*mm
    grid_w        = W - 20*mm
    grid_h        = 26*mm
    month_strip_w = 14*mm
    day_grid_w    = grid_w - month_strip_w
    day_col_w     = day_grid_w / 16
    day_row_h     = grid_h / 2

    c.setFillColor(colors.HexColor("#f9f9f9"))
    c.rect(grid_x, grid_y - grid_h, grid_w, grid_h, fill=1, stroke=0)
    c.setFillColor(colors.black)
    c.setLineWidth(0.5)
    c.rect(grid_x, grid_y - grid_h, grid_w, grid_h, fill=0, stroke=1)

    months      = ["JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC"]
    month_col_w = month_strip_w / 2
    c.setFont("Helvetica-Bold", 6.5)

    for i, m in enumerate(months[:6]):
        mx = grid_x + month_col_w/2
        my = grid_y - (i + 0.5) * (grid_h/6)
        if (i+1) == cur_month:
            c.setFillColor(colors.black)
            c.rect(grid_x, grid_y - (i+1)*(grid_h/6), month_col_w, grid_h/6, fill=1, stroke=0)
            c.setFillColor(colors.white)
        else:
            c.setFillColor(colors.black)
        c.drawCentredString(mx, my - 2.5, m)

    for i, m in enumerate(months[6:]):
        mx = grid_x + month_col_w + month_col_w/2
        my = grid_y - (i + 0.5) * (grid_h/6)
        if (i+7) == cur_month:
            c.setFillColor(colors.black)
            c.rect(grid_x + month_col_w, grid_y - (i+1)*(grid_h/6), month_col_w, grid_h/6, fill=1, stroke=0)
            c.setFillColor(colors.white)
        else:
            c.setFillColor(colors.black)
        c.drawCentredString(mx, my - 2.5, m)

    c.setStrokeColor(colors.black)
    c.setLineWidth(0.5)
    c.line(grid_x + month_strip_w, grid_y, grid_x + month_strip_w, grid_y - grid_h)

    day_area_x = grid_x + month_strip_w
    c.setFont("Helvetica-Bold", 7.5)
    c.setFillColor(colors.HexColor("#555555"))
    c.drawCentredString(day_area_x + day_grid_w/2, grid_y - grid_h/2 + 2, f"YEAR  {today.year}")
    c.setFillColor(colors.black)

    c.setFont("Helvetica-Bold", 7)
    for idx, day in enumerate(range(1, 17)):
        cx = day_area_x + (idx + 0.5) * day_col_w
        cy = grid_y - day_row_h/2
        if day == today.day:
            c.setFillColor(colors.black)
            c.circle(cx, cy + 1.5, 4.5, fill=1)
            c.setFillColor(colors.white)
            c.drawCentredString(cx, cy - 1.5, str(day))
            c.setFillColor(colors.black)
        else:
            c.drawCentredString(cx, cy - 1.5, str(day))

    for idx, day in enumerate(list(range(17, 32)) + ["-"]):
        cx = day_area_x + (idx + 0.5) * day_col_w
        cy = grid_y - day_row_h - day_row_h/2
        if isinstance(day, int) and day == today.day:
            c.setFillColor(colors.black)
            c.circle(cx, cy + 1.5, 4.5, fill=1)
            c.setFillColor(colors.white)
            c.drawCentredString(cx, cy - 1.5, str(day))
            c.setFillColor(colors.black)
        else:
            c.setFillColor(colors.black)
            c.drawCentredString(cx, cy - 1.5, str(day) if isinstance(day, int) else "-")

    c.setLineWidth(0.3)
    c.setStrokeColor(colors.lightgrey)
    c.line(day_area_x, grid_y - day_row_h, day_area_x + day_grid_w, grid_y - day_row_h)
    for i in range(1, 16):
        x = day_area_x + i * day_col_w
        c.line(x, grid_y, x, grid_y - grid_h)
    c.setStrokeColor(colors.black)

    # ── FIELDS ───────────────────────────────────────────────────
    fields_y  = grid_y - grid_h - 5*mm
    col1_x    = 12*mm
    col2_x    = W/2 + 5*mm
    col_end   = W - 12*mm
    label_w   = 22*mm

    c.setLineWidth(0.5)

    def field_left(label, value, y):
        c.setFont("Helvetica-Bold", 8); c.drawString(col1_x, y, label)
        c.line(col1_x + label_w, y-1, W/2 - 5*mm, y-1)
        c.setFont("Helvetica", 8.5); c.drawString(col1_x + label_w + 1, y, value)

    def field_right(label, value, y):
        c.setFont("Helvetica-Bold", 8); c.drawString(col2_x, y, label)
        c.line(col2_x + label_w, y-1, col_end, y-1)
        c.setFont("Helvetica", 8.5); c.drawString(col2_x + label_w + 1, y, value)

    def field_full(label, value, y):
        c.setFont("Helvetica-Bold", 8); c.drawString(col1_x, y, label)
        c.line(col1_x + label_w, y-1, col_end, y-1)
        c.setFont("Helvetica", 8.5); c.drawString(col1_x + label_w + 1, y, value)

    field_left ("CLIENT #:",   f"PS-{int(slip_id):04d}", fields_y)
    field_right("DEPARTMENT:", department,                fields_y)
    field_full ("NAME:",       name,                      fields_y - 9*mm)
    field_full ("PURPOSE:",    purpose,                   fields_y - 18*mm)
    field_left ("TIME OUT:",   time_out,                  fields_y - 27*mm)
    field_right("TIME IN:",    time_in,                   fields_y - 27*mm)

    if category in ("Personal Reason", "Others"):
        c.setFont("Helvetica-Oblique", 6)
        c.setFillColor(colors.HexColor("#8B0000"))
        c.drawString(col1_x, fields_y - 33*mm,
            "This pass is for personal reasons. PUP Santa Rosa Campus is not liable for any incident outside school premises.")
        c.setFillColor(colors.black)

    # ── SIGNATURE ────────────────────────────────────────────────
    sig_center    = W - 40*mm
    sig_line_half = 22*mm
    sig_y         = 20*mm

    c.setLineWidth(0.5)
    c.line(sig_center - sig_line_half, sig_y, sig_center + sig_line_half, sig_y)
    c.setFont("Helvetica-Bold", 7.5)
    c.drawCentredString(sig_center, sig_y - 4*mm,   "Dr. Leny V. Salmingo")
    c.setFont("Helvetica", 7)
    c.drawCentredString(sig_center, sig_y - 7.5*mm, "Campus Director")
    c.setFont("Helvetica", 6.5)
    c.drawCentredString(sig_center, sig_y - 11*mm,  "SIGNATURE OF ADMINISTRATIVE OFFICER")

    c.save()


if __name__ == "__main__":
    if len(sys.argv) < 9:
        print("Usage: python generate_passlip.py <slip_id> <name> <department> <purpose> <time_out> <time_in> <category> <output_path>")
        sys.exit(1)

    generate_pass_slip(
        output_path = sys.argv[8],
        slip_id     = sys.argv[1],
        name        = sys.argv[2],
        department  = sys.argv[3],
        purpose     = sys.argv[4],
        time_out    = sys.argv[5],
        time_in     = sys.argv[6],
        category    = sys.argv[7],
    )
    print(f"OK:{sys.argv[8]}")
