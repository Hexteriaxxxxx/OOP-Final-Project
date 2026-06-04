@echo off
title PUP Pass Slip System - Setup
color 4F

echo.
echo  ================================================
echo    PUP Santa Rosa - Pass Slip System SETUP
echo  ================================================
echo.

cd /d "%~dp0"

set MYSQL="C:\Program Files\MySQL\MySQL Server 9.7\bin\mysql.exe"

:: ── Step 1: Create database if not exists ──────────────
echo [1/3] Setting up database...
%MYSQL% -u root -pProjectgian27 -e "CREATE DATABASE IF NOT EXISTS pass_slip_db;" 2>nul
if errorlevel 1 (
    echo [ERROR] MySQL not found or wrong password!
    echo         Make sure MySQL is running.
    pause
    exit
)
echo [OK] Database ready.

:: ── Step 2: Import schema + data ──────────────────────
echo [2/3] Importing schema and data...
%MYSQL% -u root -pProjectgian27 pass_slip_db < "database\pass_slip_data.sql"
if errorlevel 1 (
    echo [ERROR] Failed to import database!
    pause
    exit
)
echo [OK] Database imported successfully.

:: ── Step 3: Install Python dependencies ───────────────
echo [3/3] Installing Python dependencies...
pip install flask flask-cors mysql-connector-python >nul 2>&1
echo [OK] Python dependencies ready.

echo.
echo  ================================================
echo    SETUP COMPLETE!
echo    You can now run START_PUP_SYSTEM.bat
echo  ================================================
echo.
pause
