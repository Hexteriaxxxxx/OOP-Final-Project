@echo off
title PUP Santa Rosa - Pass Slip System
color 4F

echo.
echo  ================================================
echo    PUP Santa Rosa - Pass Slip System
echo    Starting all services...
echo  ================================================
echo.

:: Change to project directory
cd /d "C:\Users\ADMIN1\IdeaProjects\OOP Final Project"

:: Check if Python is available
python --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Python not found! Please install Python first.
    pause
    exit
)

:: Check if ngrok is available
ngrok version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] ngrok not found! Please install ngrok first.
    pause
    exit
)

:: Start visitor server in background
echo [1/3] Starting Visitor Server...
start "PUP Visitor Server" cmd /k "cd /d "C:\Users\ADMIN1\IdeaProjects\OOP Final Project" && python visitor_server.py"
timeout /t 3 /nobreak >nul

:: Start ngrok in background
echo [2/3] Starting ngrok tunnel...
start "PUP ngrok Tunnel" cmd /k "ngrok http 5055"
timeout /t 5 /nobreak >nul

:: Get ngrok public URL and update Apps Script
echo [3/3] Getting ngrok URL and updating Apps Script...
python -c "
import urllib.request, json, re, os

try:
    # Get ngrok URL from API
    response = urllib.request.urlopen('http://localhost:4040/api/tunnels', timeout=10)
    data = json.loads(response.read())
    tunnels = data.get('tunnels', [])
    
    public_url = None
    for t in tunnels:
        if t.get('proto') == 'https':
            public_url = t.get('public_url')
            break
    
    if not public_url:
        print('[WARN] Could not get ngrok URL - update manually')
    else:
        new_server_url = public_url + '/submit-visitor'
        
        # Update visitor_google_script.js
        script_file = 'visitor_google_script.js'
        with open(script_file, 'r', encoding='utf-8') as f:
            content = f.read()
        
        updated = re.sub(
            r'var SERVER_URL\s*=\s*\"[^\"]*\";',
            f'var SERVER_URL = \"{new_server_url}\";',
            content
        )
        
        with open(script_file, 'w', encoding='utf-8') as f:
            f.write(updated)
        
        # Also update .env
        env_file = '.env'
        with open(env_file, 'r', encoding='utf-8') as f:
            env_content = f.read()
        
        env_updated = re.sub(
            r'VISITOR_SERVER_URL=.*',
            f'VISITOR_SERVER_URL={public_url}',
            env_content
        )
        
        with open(env_file, 'w', encoding='utf-8') as f:
            f.write(env_updated)
        
        print(f'[OK] ngrok URL: {public_url}')
        print(f'[OK] Updated visitor_google_script.js')
        print(f'[OK] Updated .env')
        print()
        print('  !! IMPORTANT: Re-paste visitor_google_script.js')
        print('     into Google Apps Script if URL changed !!')
        
except Exception as ex:
    print(f'[WARN] Could not auto-update: {ex}')
    print('       Check ngrok terminal for URL manually')
"

echo.
echo  ================================================
echo    All services started!
echo.
echo    Visitor Server : http://localhost:5055
echo    ngrok Dashboard: http://localhost:4040
echo.
echo    Check ngrok terminal for public URL
echo    Re-paste Apps Script if URL changed!
echo  ================================================
echo.
pause
