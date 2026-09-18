@echo off
title Bank Account Management System - BAMS
color 0F

:: Ensure javac is available
where javac >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    if exist "%JAVA_HOME%\bin\javac.exe" (
        set "PATH=%JAVA_HOME%\bin;%PATH%"
    ) else if exist "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin\javac.exe" (
        set "PATH=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin;%PATH%"
    )
)

:: Step 1: Compile using javac
echo  [*] Compiling project...
if not exist bin mkdir bin
javac -cp "lib\*" -d bin src\com\bank\model\*.java src\com\bank\exception\*.java src\com\bank\dao\*.java src\com\bank\service\*.java src\com\bank\util\*.java src\com\bank\main\*.java > bin\compile.log 2>&1

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo  [ERROR] Compilation failed! Error details:
    echo --------------------------------------------------------------------------------
    type bin\compile.log
    echo --------------------------------------------------------------------------------
    echo  Make sure JDK is installed and javac is accessible.
    echo.
    pause
    exit /b 1
)

echo  [OK] Compiled successfully!
echo  [*] Starting application...
echo.

:: Step 2: Run the app
java -cp "bin;lib\*" com.bank.main.BankApp

echo.
echo  Session ended. Press any key to close this window.
pause > nul
