# 🛍️ eCommerce Application - Complete Setup & Deployment Guide

## 📚 Quick Navigation

- **[Deployment Summary](#-quick-deployment-checklist)** - Essential steps only
- **[Full Deployment Guide](#-complete-deployment-guide)** - Detailed instructions
- **[Security Details](#-security-architecture)** - Authentication & authorization
- **[API Documentation](#-api-endpoints-reference)** - Complete endpoint list
- **[Troubleshooting](#-troubleshooting-common-issues)** - Common problems & solutions

---

## ✅ Quick Deployment Checklist

### Prerequisites
- [ ] GitHub account with repository
- [ ] Render.com account
- [ ] Node.js 16+ installed locally
- [ ] Java 17+ and Maven installed locally

### Backend Setup (5 minutes)
```bash
cd eCommersApp
mvn clean package -DskipTests
```

### Frontend Setup (5 minutes)
```bash
cd frontend
npm install
npm run build
```

### Deploy Backend to Render
1. Push code to GitHub
2. Create Render Web Service → Docker
3. Set environment variables (see below)
4. Deploy

### Deploy Frontend to Render
1. Create Render Static Site
2. Connect repository
3. Build: `npm run build`
4. Publish: `build`

---

## 🔐 Security Architecture

### Three-Layer Security

#### Layer 1: Database Security
```
PostgreSQL (Render)
├─ Username/Password authentication
├─ Connection pooling (HikariCP)
├─ SSL/TLS encrypted connection
└─ Automatic backups
```

#### Layer 2: API Security
```
Spring Security + JWT
├─ BCrypt password hashing
├─ JWT Bearer tokens (15 min access, 7 day refresh)
├─ Role-based access control (ADMIN/USER)
├─ CORS origin whitelisting
└─ Request rate limiting ready
```

#### Layer 3: Application Security
```
Permission Management
├─ Table-level access control
├─ User role assignment
├─ Permission audit trail
└─ Third-party middleware tokens
```

### Token Architecture

```
LOGIN REQUEST
    ↓
Validate email & password (BCrypt)
    ↓
Generate tokens:
  ┌──────────────────────┐
  │ ACCESS TOKEN         │
  │ ├─ Valid: 15 min     │
  │ ├─ Contains: roles   │
  │ └─ Use: API calls    │
  └──────────────────────┘
  
  ┌──────────────────────┐
  │ REFRESH TOKEN        │
  │ ├─ Valid: 7 days     │
  │ ├─ Contains: id only  │
  │ └─ Use: Get new token│
  └──────────────────────┘
    ↓
Return both tokens to client
    ↓
Client uses in requests:
Authorization: Bearer <access_token>
```

---

## 📦 Environment Variables

### Backend (.env on Render)

```properties
# DATABASE
DB_HOST=xxx.render.internal
DB_PORT=5432
DB_NAME=ecommerce
DB_USER=ecommerce_user
DB_PASSWORD=<strong-password>

# JWT SECURITY
JWT_SECRET=<generate-32-char-min-random-string>
JWT_ACCESS_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=604800000

# APPLICATION
PORT=8080
SPRING_PROFILES_ACTIVE=prod

# CORS
CORS_ORIGINS=http://localhost:3000,https://frontend-url.onrender.com

# ADMIN
ADMIN_PASSWORD=<strong-admin-password>
```

### Frontend (.env.production on Render)

```properties
REACT_APP_API_URL=https://your-backend.onrender.com
REACT_APP_ENV=production
```

---

## 🚀 Complete Deployment Guide

### Step 1: Prepare Backend Code

#### 1.1 Verify Dependencies

Check `pom.xml` has all required dependencies:
```xml
<!-- JWT -->
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-api</artifactId>
  <version>0.11.5</version>
</dependency>

<!-- Spring Security -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- PostgreSQL -->
<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>postgresql</artifactId>
  <scope>runtime</scope>
</dependency>
```

#### 1.2 Build Locally

```bash
cd eCommersApp
mvn clean package -DskipTests -P prod
```

#### 1.3 Test JAR

```bash
# Set environment variables
$env:DB_HOST = "localhost"
$env:DB_PORT = "5432"
$env:DB_NAME = "ecommerce"
$env:DB_USER = "postgres"
$env:DB_PASSWORD = "password"

# Run JAR
java -jar target/Ecom-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### Step 2: Prepare Frontend Code

#### 2.1 Install Dependencies

```bash
cd frontend
npm install
```

#### 2.2 Build Locally

```bash
npm run build
```

#### 2.3 Test Build

```bash
npm install -g serve
serve -s build
# Visit http://localhost:3000
```

### Step 3: Set Up Database on Render

1. **Create PostgreSQL**
   - Render Dashboard → New → PostgreSQL
   - Name: `ecommerce-db`
   - Region: Same as API service
   - Create database

2. **Note credentials**
   ```
   Host: xxx.render.internal
   Port: 5432
   Database: ecommerce
   User: ecommerce_user
   Password: <copy>
   ```

3. **Save connection string**
   ```
   postgresql://ecommerce_user:password@host:5432/ecommerce
   ```

### Step 4: Deploy Backend to Render

#### 4.1 Create GitHub Repository

```bash
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/username/ecommerce-app.git
git push -u origin main
```

#### 4.2 Create Render Web Service

1. Render Dashboard → New → Web Service
2. Connect GitHub repository
3. Configure:
   - **Name**: `ecommerce-api`
   - **Environment**: Docker
   - **Region**: Same as database
   - **Plan**: Standard (for production)

#### 4.3 Set Environment Variables

In Render Service Settings → Environment Variables:

```
DB_HOST=xxx.render.internal
DB_PORT=5432
DB_NAME=ecommerce
DB_USER=ecommerce_user
DB_PASSWORD=<paste>
JWT_SECRET=your-32-character-minimum-secret-key
JWT_ACCESS_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=604800000
ADMIN_PASSWORD=StrongAdminPass123!
CORS_ORIGINS=http://localhost:3000,https://frontend-url.onrender.com
PORT=8080
SPRING_PROFILES_ACTIVE=prod
```

#### 4.4 Deploy

- Click "Deploy"
- Wait 10-15 minutes for build and startup
- Check logs for errors
- Get URL from Render dashboard (e.g., `https://ecommerce-api.onrender.com`)

#### 4.5 Verify Deployment

```bash
# Check health
curl https://ecommerce-api.onrender.com/actuator/health

# Should return: {"status":"UP"}
```

### Step 5: Deploy Frontend to Render

#### 5.1 Create Render Static Site

1. Render Dashboard → New → Static Site
2. Connect GitHub repository (frontend folder)
3. Configure:
   - **Name**: `ecommerce-frontend`
   - **Build Command**: `npm run build`
   - **Publish Directory**: `build`

#### 5.2 Set Environment Variables

```
REACT_APP_API_URL=https://ecommerce-api.onrender.com
REACT_APP_ENV=production
```

#### 5.3 Deploy

- Click "Deploy"
- Wait for build to complete
- Get URL from Render (e.g., `https://ecommerce-frontend.onrender.com`)

---

## 👥 User & Permission Management

### Create Users

#### Via Registration Endpoint

```bash
POST https://your-api.onrender.com/ecom/customers
Content-Type: application/json

{
  "email": "newuser@example.com",
  "password": "SecurePassword123!",
  "name": "John Doe",
  "phone": "9876543210"
}
```

#### Via Admin Dashboard

1. Login as admin
2. Navigate to User Management
3. Click "Add User"
4. Fill details and save

### User Roles

| Role | Permissions |
|------|------------|
| `ROLE_ADMIN` | ✅ All system access, manage users, view reports |
| `ROLE_USER` | ✅ Browse products, place orders, manage profile |

### Grant Permissions

```bash
POST https://your-api.onrender.com/ecom/auth/permissions
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "userId": 2,
  "resourceName": "PRODUCTS",
  "permissionType": "READ",
  "notes": "Can view products"
}
```

**Available Permissions:**
- `CREATE` - Create new records
- `READ` - View records
- `UPDATE` - Modify records
- `DELETE` - Remove records
- `EXPORT` - Export data

---

## 🔌 Third-Party Middleware Integration

### Generate Middleware Token

```bash
POST https://your-api.onrender.com/ecom/auth/middleware-token
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "username": "service@example.com",
  "scopes": ["READ_PRODUCTS", "READ_ORDERS"]
}

Response:
{
  "token": "eyJhbGc...",
  "tokenType": "Bearer",
  "expiresIn": 900000,
  "scopes": ["READ_PRODUCTS", "READ_ORDERS"]
}
```

### Use in External Service

```javascript
const token = "eyJhbGc...";

fetch('https://api.onrender.com/ecom/products', {
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});
```

---

## 📖 API Endpoints Reference

### Authentication

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/ecom/auth/login` | ❌ | Login and get tokens |
| POST | `/ecom/auth/refresh` | ❌ | Refresh access token |
| GET | `/ecom/auth/me` | ✅ | Get current user |
| POST | `/ecom/auth/logout` | ✅ | Logout |

### Permissions

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/ecom/auth/permissions` | ✅ ADMIN | Grant permission |
| GET | `/ecom/auth/permissions/{userId}` | ✅ | View user permissions |
| DELETE | `/ecom/auth/permissions/{id}` | ✅ ADMIN | Revoke permission |

### Middleware

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/ecom/auth/middleware-token` | ✅ ADMIN | Generate token for third-party |

### Products

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/ecom/products` | ❌ | List all products |
| GET | `/ecom/products/{id}` | ❌ | Get product details |
| POST | `/ecom/products` | ✅ ADMIN | Create product |
| PUT | `/ecom/products/{id}` | ✅ ADMIN | Update product |
| DELETE | `/ecom/products/{id}` | ✅ ADMIN | Delete product |

### Orders

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/ecom/orders` | ✅ USER | List user's orders |
| POST | `/ecom/orders` | ✅ USER | Create order |
| GET | `/ecom/orders/{id}` | ✅ USER | Get order details |
| PUT | `/ecom/orders/{id}` | ✅ ADMIN | Update order |
| DELETE | `/ecom/orders/{id}` | ✅ ADMIN | Delete order |

---

## 🐛 Troubleshooting Common Issues

### "Failed to Connect to Database"

**Symptoms**: Application crashes on startup

**Solution**:
1. Verify DB credentials in Render environment
2. Check PostgreSQL service is running
3. Verify network connectivity (same region)
4. Check logs: `Render Dashboard → Logs`

```bash
# Test connection string
postgresql://user:password@host:5432/database
```

### "Invalid JWT Token" Error

**Symptoms**: 401 Unauthorized errors

**Solution**:
1. Verify JWT_SECRET is set and same on all instances
2. Check token hasn't expired (15 min access token)
3. Use refresh endpoint to get new token
4. Verify Authorization header format: `Bearer <token>`

### "CORS Error"

**Symptoms**: Frontend requests blocked

**Solution**:
1. Add frontend URL to CORS_ORIGINS environment variable
2. Format: `https://yourdomain.com,https://other-domain.com`
3. Restart service after changing

### "Token Refresh Fails"

**Symptoms**: Refresh endpoint returns 401

**Solution**:
1. Refresh token expired? Must login again (7 day limit)
2. Refresh token database issue
3. Check database connection
4. Verify refresh token matches stored format

### "Port Already in Use"

**Symptoms**: Service won't start

**Solution**:
1. Render automatically assigns port from `PORT` environment variable
2. Clear any local port conflicts
3. Verify `PORT=8080` is set

### "Build Fails on Render"

**Symptoms**: Docker build error

**Solution**:
1. Check Dockerfile syntax
2. Verify all source files are committed to Git
3. Check logs in Render dashboard
4. Verify build command: `mvn clean package -DskipTests`

---

## 🔒 Security Best Practices

✅ **Do:**
- Use HTTPS for all connections (Render provides free SSL)
- Store tokens in secure storage
- Rotate JWT secret periodically
- Monitor suspicious login attempts
- Use strong passwords (12+ characters, mixed case, numbers, symbols)
- Implement rate limiting for login endpoint
- Enable database backups
- Use environment variables for all secrets
- Audit permission changes
- Log security events

❌ **Don't:**
- Commit `.env` files to Git
- Use weak JWT secrets
- Store passwords in plain text
- Log sensitive data
- Allow long token expiration times
- Share credentials via chat/email
- Use HTTP in production
- Trust client-side validation only
- Ignore security warnings

---

## 📊 Monitoring & Maintenance

### Health Check

```bash
curl https://your-api.onrender.com/actuator/health
```

Expected response:
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "livenessState": {"status": "UP"},
    "readinessState": {"status": "UP"}
  }
}
```

### View Logs

**Backend Logs:**
1. Render Dashboard → ecommerce-api → Logs
2. Filter by ERROR for issues

**Frontend Logs:**
1. Browser DevTools (F12) → Console
2. Check for network errors

### Performance Optimization

1. **Database**
   - Render auto-scales based on load
   - Connection pooling enabled (HikariCP)
   - Slow queries logged

2. **API**
   - Gzip compression enabled
   - Cache headers set
   - Pagination on list endpoints

3. **Frontend**
   - Minified production build
   - Code splitting enabled
   - Static asset caching

---

## 📚 Testing with Postman

1. Import `Postman_Collection.json`
2. Set variables:
   - `BASE_URL`: Your API URL
   - `accessToken`: From login response
   - `refreshToken`: From login response
3. Run test sequences in order
4. Check test results

---

## 🆘 Getting Help

1. **Check Documentation**: [RENDER_DEPLOYMENT_GUIDE.md](RENDER_DEPLOYMENT_GUIDE.md)
2. **Review Error Messages**: Usually indicate the issue
3. **Check Logs**: Render dashboard shows detailed logs
4. **Database Issues**: Check Render PostgreSQL dashboard
5. **API Issues**: Use Postman to test endpoints

---

## 📝 Common Customizations

### Change Token Expiration

1. Update `application-prod.properties`:
   ```properties
   jwt.access-token-expiration=1800000  # 30 minutes instead of 15
   jwt.refresh-token-expiration=2592000000  # 30 days instead of 7
   ```
2. Redeploy to Render

### Add Custom Role

1. Update `UserRole.java`:
   ```java
   public enum UserRole {
     ROLE_ADMIN,
     ROLE_USER,
     ROLE_VENDOR  // New role
   }
   ```
2. Rebuild and redeploy

### Customize Permissions

1. Add new resources to permission system
2. Update permission grant/check logic
3. Add UI for new permission types

---

## 🎯 Next Steps

1. ✅ Deploy backend to Render
2. ✅ Deploy frontend to Render
3. ✅ Create admin user
4. ✅ Grant permissions to test users
5. ✅ Test authentication flow
6. ✅ Test permission system
7. ✅ Set up monitoring/alerts
8. ✅ Configure backups
9. ✅ Plan load testing
10. ✅ Go live!

---

**Version**: 1.0 | **Updated**: February 2026 | **Status**: Production Ready
