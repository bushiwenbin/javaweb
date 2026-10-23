@echo off
chcp 65001 >nul
rem 用 IntelliJ IDEA 打开实验二的 CTTS 工程
start "" "C:\dev\idea\bin\idea64.exe" "%~dp0ctts"
