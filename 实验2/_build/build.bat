@echo off
chcp 65001 >nul
set "JAVA_HOME=C:\dev\jdk-17"
set "MAVEN_HOME=C:\dev\maven"
set "PATH=C:\dev\jdk-17\bin;C:\dev\maven\bin;%PATH%"
cd /d "%~dp0..\ctts"
call mvn -B clean package -DskipTests
exit /b %ERRORLEVEL%
