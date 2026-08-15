@echo off
title Quiz CSV Import
cd /d "%~dp0"

echo Compiling Java code...
if not exist bin mkdir bin

javac -cp "lib/postgresql-42.7.2.jar" -d bin src/com/quiz/*.java

if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b %errorlevel%
)

echo Starting server...
start http://localhost:8080/
java -cp "bin;lib/postgresql-42.7.2.jar" com.quiz.Main

pause
