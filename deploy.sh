#!/bin/bash

# 🚀 QUICK DEPLOYMENT SCRIPT - eCommerce to Render.com
# This script helps you deploy the application quickly

set -e

echo "=================================="
echo "eCommerce Application Deployment"
echo "=================================="
echo ""

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_step() {
    echo -e "${BLUE}📌 $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Check prerequisites
print_step "Checking prerequisites..."

# Check Git
if ! command -v git &> /dev/null; then
    print_error "Git not found. Please install Git."
    exit 1
fi
print_success "Git found"

# Check Node.js
if ! command -v npm &> /dev/null; then
    print_error "Node.js/npm not found. Please install Node.js 16+"
    exit 1
fi
print_success "Node.js found ($(node -v))"

# Check Java
if ! command -v java &> /dev/null; then
    print_error "Java not found. Please install Java 17+"
    exit 1
fi
print_success "Java found ($(java -version 2>&1 | head -n 1))"

# Check Maven
if ! command -v mvn &> /dev/null; then
    print_error "Maven not found. Please install Maven"
    exit 1
fi
print_success "Maven found ($(mvn -v 2>&1 | head -n 1))"

echo ""
print_step "Checking application structure..."

# Check if we're in the right directory
if [ ! -f "eCommersApp/pom.xml" ]; then
    print_error "pom.xml not found in eCommersApp/. Are you in the right directory?"
    exit 1
fi
print_success "Backend found (eCommersApp/)"

if [ ! -f "frontend/package.json" ]; then
    print_error "package.json not found in frontend/. Frontend not found."
    exit 1
fi
print_success "Frontend found (frontend/)"

echo ""
read -p "$(echo -e ${BLUE}Enter your GitHub username:${NC} )" github_username
read -p "$(echo -e ${BLUE}Enter your GitHub repository name (e.g., ecommerce-app):${NC} )" repo_name
read -p "$(echo -e ${BLUE}Enter your Render.com PostgreSQL host (e.g., xxx.render.internal):${NC} )" db_host
read -sp "$(echo -e ${BLUE}Enter your Render.com PostgreSQL password:${NC} )" db_password
echo ""

# Initialize git if not already initialized
if [ ! -d ".git" ]; then
    print_step "Initializing Git repository..."
    git init
    git branch -M main
    git config user.email "your-email@example.com" 2>/dev/null || true
    git config user.name "Your Name" 2>/dev/null || true
else
    print_success "Git repository already initialized"
fi

# Build backend
echo ""
print_step "Building backend application..."
cd eCommersApp

if mvn clean package -DskipTests -P prod; then
    print_success "Backend build successful"
else
    print_error "Backend build failed"
    exit 1
fi

cd ..

# Build frontend
echo ""
print_step "Building frontend application..."
cd frontend

if npm install && npm run build; then
    print_success "Frontend build successful"
else
    print_error "Frontend build failed"
    exit 1
fi

cd ..

# Prepare for Git
echo ""
print_step "Preparing for Git deployment..."

# Create .gitignore if not exists
if [ ! -f ".gitignore" ]; then
    cat > .gitignore << EOF
# IDE
.vscode/
.idea/
*.swp
*.swo

# Dependencies
node_modules/
target/
.mvn/

# Environment
.env
.env.local

# Logs
logs/
*.log

# OS
.DS_Store
Thumbs.db

# Build
build/
dist/
EOF
    print_success ".gitignore created"
fi

# Add files to git
echo ""
print_step "Adding files to Git..."
git add -A
git commit -m "Initial commit - ready for Render deployment" || true

print_success "Files committed"

# Generate JWT secret
print_step "Generating JWT secret..."
jwt_secret=$(openssl rand -base64 32)
print_success "JWT secret generated (save this!)"

echo ""
echo "=================================="
echo "✅ PREPARATION COMPLETE"
echo "=================================="
echo ""
echo "Next steps to deploy:"
echo ""
echo "1️⃣  Push code to GitHub:"
echo "   git remote add origin https://github.com/$github_username/$repo_name.git"
echo "   git push -u origin main"
echo ""
echo "2️⃣  Create PostgreSQL database on Render:"
echo "   - Go to https://render.com"
echo "   - New → PostgreSQL"
echo "   - Name: ecommerce-db"
echo "   - Region: Choose your region"
echo "   - Save credentials"
echo ""
echo "3️⃣  Deploy Backend to Render:"
echo "   - New → Web Service"
echo "   - Connect GitHub repository"
echo "   - Environment: Docker"
echo "   - Name: ecommerce-api"
echo ""
echo "4️⃣  Set Environment Variables:"
echo "   DB_HOST=$db_host"
echo "   DB_PORT=5432"
echo "   DB_NAME=ecommerce"
echo "   DB_USER=ecommerce_user"
echo "   DB_PASSWORD=$db_password"
echo "   JWT_SECRET=$jwt_secret"
echo "   JWT_ACCESS_EXPIRATION=900000"
echo "   JWT_REFRESH_EXPIRATION=604800000"
echo "   SPRING_PROFILES_ACTIVE=prod"
echo "   PORT=8080"
echo "   CORS_ORIGINS=https://yourdomain.com"
echo ""
echo "5️⃣  Deploy Frontend to Render:"
echo "   - New → Static Site"
echo "   - Connect GitHub repository"
echo "   - Build Command: npm run build"
echo "   - Publish Directory: build"
echo ""
echo "📚 For detailed guide, see: RENDER_DEPLOYMENT_GUIDE.md"
echo "📋 For quick reference, see: DEPLOYMENT_README.md"
echo ""
echo "=================================="
