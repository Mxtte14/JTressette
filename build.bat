@echo off
REM Build script for JTressette (Windows)
REM This script compiles all Java source files and copies resources

echo Building JTressette...

REM Navigate to the JTressette directory
cd JTressette

REM Create bin directory if it doesn't exist
if not exist bin mkdir bin

REM Compile all Java files
javac -d bin -sourcepath src src\main\JTressette.java

REM Check if compilation was successful
if %ERRORLEVEL% EQU 0 (
    echo Compilation successful!
    
    REM Copy resources to bin directory
    echo Copying resources...
    xcopy /E /I /Y src\res bin\res >nul
    
    echo Build successful! Compiled files are in JTressette\bin\
    echo To run the application, use: run.bat
) else (
    echo Build failed!
    exit /b 1
)
