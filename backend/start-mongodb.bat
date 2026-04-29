@echo off
REM BFY Backend - MongoDB Installer/Starter Guide
REM Windows users: Use this to set up MongoDB

echo.
echo ============================================
echo BFY Backend - MongoDB Setup
echo ============================================
echo.

REM Check if MongoDB is installed
where mongod >nul 2>nul
if %ERRORLEVEL% == 0 (
    echo [OK] MongoDB found in PATH
    echo.
    echo Starting MongoDB...
    mongod --dbpath C:\data\db
) else (
    echo [ERROR] MongoDB not found in PATH
    echo.
    echo Please choose one option:
    echo.
    echo Option 1: Install MongoDB Community Edition
    echo   Download from: https://www.mongodb.com/try/download/community
    echo   Install and add to PATH
    echo.
    echo Option 2: Use MongoDB Atlas (Cloud)
    echo   - Go to https://www.mongodb.com/cloud/atlas
    echo   - Create free account and cluster
    echo   - Update MONGODB_URI in .env file
    echo   - Connection string: mongodb+srv://user:password@cluster.mongodb.net/bfy
    echo.
    echo Option 3: Use Docker (if installed)
    echo   docker run -d -p 27017:27017 --name mongodb mongo:latest
    echo.
    pause
)
