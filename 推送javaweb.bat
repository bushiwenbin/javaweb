@echo off
setlocal
cd /d "%~dp0"
set GITEXE=C:\Program Files\Git\cmd\git.exe
echo ============================================
echo   Push local repo to github.com/bushiwenbin/javaweb
echo ============================================
echo.
"%GITEXE%" remote remove origin >nul 2>&1
"%GITEXE%" remote add origin git@github.com:bushiwenbin/javaweb.git
"%GITEXE%" remote -v
echo.
"%GITEXE%" push -u origin main
echo.
if errorlevel 1 (
  echo [FAILED] See the message above.
  echo   "Repository not found"  =^> create the repo on GitHub web first.
  echo   "non-fast-forward"       =^> the remote already has commits:
  echo                              run:  "%GITEXE%" pull --no-rebase --allow-unrelated-histories origin main
) else (
  echo [OK] Pushed. Visit https://github.com/bushiwenbin/javaweb
)
echo.
pause