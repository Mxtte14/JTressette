@echo off
REM Build script for JTressette (Windows)
REM This script compiles all Java source files

echo Building JTressette...

REM Navigate to the JTressette directory
cd JTressette

REM Create bin directory if it doesn't exist
if not exist bin mkdir bin

REM Compile all Java files
javac -d bin -sourcepath src src\main\JTressette.java

REM Check if compilation was successful
if %ERRORLEVEL% EQU 0 (
    echo Build successful! Compiled files are in JTressette\bin\
    echo To run the application, use: run.bat
) else (
    echo Build failed!
    exit /b 1
)
