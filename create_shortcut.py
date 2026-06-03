"""
create_shortcut.py - Creates desktop shortcut with PUP icon
Run once: python create_shortcut.py
"""

import os, sys

def create_shortcut():
    try:
        import winshell
        from win32com.client import Dispatch
    except ImportError:
        os.system("pip install winshell pywin32")
        import winshell
        from win32com.client import Dispatch

    project_dir = os.path.dirname(os.path.abspath(__file__))
    bat_file    = os.path.join(project_dir, "START_PUP_SYSTEM.bat")
    ico_file    = os.path.join(project_dir, "pup_passlip.ico")
    desktop     = winshell.desktop()
    shortcut    = os.path.join(desktop, "PUP Pass Slip System.lnk")

    shell = Dispatch('WScript.Shell')
    lnk   = shell.CreateShortCut(shortcut)
    lnk.Targetpath       = bat_file
    lnk.WorkingDirectory = project_dir
    lnk.Description      = "PUP Santa Rosa - Pass Slip System"
    if os.path.exists(ico_file):
        lnk.IconLocation = ico_file
    lnk.save()

    print(f"[OK] Shortcut created on Desktop: PUP Pass Slip System")

if __name__ == "__main__":
    create_shortcut()
