@echo off
net session >nul 2>&1
if %errorlevel% neq 0 (
  echo Requesting administrator privileges... please click YES on the UAC dialog.
  powershell -NoProfile -Command "Start-Process -FilePath '%~f0' -Verb RunAs"
  exit /b
)
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0fix-github-hosts.ps1"
echo.
echo Done. Press any key to close.
pause >nul