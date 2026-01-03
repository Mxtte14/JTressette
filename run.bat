@echo off
REM Run script for JTressette (Windows)
REM This script runs the compiled application

echo Running JTressette...

REM Navigate to the JTressette directory
cd JTressette

REM Check if bin directory exists
if not exist bin (
    echo Error: bin directory not found. Please run build.bat first.
    exit /b 1
)

REM Run the application
java -cp bin;src main.JTressette

REM Check if run was successful
if %ERRORLEVEL% NEQ 0 (
    echo Failed to run JTressette!
    exit /b 1
)
