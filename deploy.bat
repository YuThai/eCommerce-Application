@echo off
REM 🚀 QUICK DEPLOYMENT SCRIPT - eCommerce to Render.com (Windows)
REM This script helps you deploy the application quickly

setlocal enabledelayedexpansion

echo ==================================
echo eCommerce Application Deployment
echo ==================================
echo.

REM Check prerequisites
echo 📌 Checking prerequisites...

REM Check Git
where git >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Git not found. Please install Git from https://git-scm.com
    exit /b 1
)
echo ✅ Git found

REM Check Node.js
where npm >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Node.js/npm not found. Please install from https://nodejs.org
    exit /b 1
)
echo ✅ Node.js found
node --version
npm --version

REM Check Java
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Java not found. Please install Java 17+ from https://www.oracle.com/java/
    exit /b 1
)
echo ✅ Java found
java -version

REM Check Maven
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Maven not found. Please install from https://maven.apache.org
    exit /b 1
)
echo ✅ Maven found
mvn -version

echo.
echo 📌 Checking application structure...

REM Check if pom.xml exists
if not exist "eCommersApp\pom.xml" (
    echo ❌ pom.xml not found in eCommersApp\. Are you in the right directory?
    exit /b 1
)
echo ✅ Backend found (eCommersApp\)

REM Check if package.json exists
if not exist "frontend\package.json" (
    echo ❌ package.json not found in frontend\. Frontend not found.
    exit /b 1
)
echo ✅ Frontend found (frontend\)

echo.
echo ==================================
echo STEP 1: Build Backend
echo ==================================
cd eCommersApp
echo Building backend...
call mvn clean package -DskipTests -P prod
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Backend build failed
    exit /b 1
)
echo ✅ Backend build successful
cd ..

echo.
echo ==================================
echo STEP 2: Build Frontend
echo ==================================
cd frontend
echo Installing dependencies...
call npm install
if %ERRORLEVEL% NEQ 0 (
    echo ❌ npm install failed
    exit /b 1
)
echo Building frontend...
call npm run build
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Frontend build failed
    exit /b 1
)
echo ✅ Frontend build successful
cd ..

echo.
echo ==================================
echo STEP 3: Git Setup
echo ==================================

REM Initialize git if not already initialized
if not exist ".git" (
    echo Initializing Git repository...
    call git init
    call git branch -M main
    echo ✅ Git repository initialized
) else (
    echo ✅ Git repository already initialized
)

REM Create .gitignore if not exists
if not exist ".gitignore" (
    echo Creating .gitignore...
    (
        echo # IDE
        echo .vscode/
        echo .idea/
        echo *.swp
        echo.
        echo # Dependencies
        echo node_modules/
        echo target/
        echo .mvn/
        echo.
        echo # Environment
        echo .env
        echo .env.local
        echo.
        echo # Logs
        echo logs/
        echo *.log
        echo.
        echo # Build
        echo build/
        echo dist/
    ) > .gitignore
    echo ✅ .gitignore created
)

REM Add and commit files
echo Adding files to Git...
call git add -A
call git commit -m "Initial commit - ready for Render deployment"
echo ✅ Files committed

echo.
echo ==================================
echo ✅ PREPARATION COMPLETE
echo ==================================
echo.
echo Next steps to deploy:
echo.
echo 1️⃣  Push code to GitHub:
echo    git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO.git
echo    git push -u origin main
echo.
echo 2️⃣  Create PostgreSQL database on Render:
echo    - Go to https://render.com
echo    - Click "New" ^> "PostgreSQL"
echo    - Name: ecommerce-db
echo    - Choose your region
echo    - Save all credentials
echo.
echo 3️⃣  Deploy Backend to Render:
echo    - Go to https://render.com
echo    - Click "New" ^> "Web Service"
echo    - Connect your GitHub repository
echo    - Environment: Docker
echo    - Name: ecommerce-api
echo    - Region: Same as database
echo.
echo 4️⃣  Set Environment Variables (in Render Service):
echo    DB_HOST=your-host.render.internal
echo    DB_PORT=5432
echo    DB_NAME=ecommerce
echo    DB_USER=ecommerce_user
echo    DB_PASSWORD=your_password
echo    JWT_SECRET=generate-32-char-random-string
echo    SPRING_PROFILES_ACTIVE=prod
echo    PORT=8080
echo    CORS_ORIGINS=https://yourdomain.com
echo.
echo 5️⃣  Deploy Frontend to Render:
echo    - Go to https://render.com
echo    - Click "New" ^> "Static Site"
echo    - Connect your GitHub repository
echo    - Build Command: npm run build
echo    - Publish Directory: build
echo.
echo 📚 For detailed guide, see: RENDER_DEPLOYMENT_GUIDE.md
echo 📋 For quick reference, see: DEPLOYMENT_README.md
echo.
echo ==================================
pause
