# eCommerce Application - Complete Deployment Guide for Render.com

## 📋 Table of Contents
1. [Overview](#overview)
2. [Security Architecture](#security-architecture)
3. [Database Setup](#database-setup)
4. [Backend Configuration](#backend-configuration)
5. [Frontend Configuration](#frontend-configuration)
6. [Deployment to Render](#deployment-to-render)
7. [User & Permission Management](#user--permission-management)
8. [Authentication Flow](#authentication-flow)
9. [Third-Party Middleware Integration](#third-party-middleware-integration)
10. [API Endpoints](#api-endpoints)
11. [Troubleshooting](#troubleshooting)

---

## Overview

This is a full-stack eCommerce application with:
- **Backend**: Spring Boot 3.1.1 with JWT authentication
- **Frontend**: React 18 with Axios
- **Database**: PostgreSQL
- **Security**: Role-based access control + Table-level permissions
- **Deployment**: Render.com with Docker

**Key Features**:
✅ User registration and authentication  
✅ JWT token management (Access + Refresh tokens)  
✅ Role-based access control (ADMIN, USER)  
✅ Table-level permission management  
✅ Third-party middleware integration support  
✅ Production-ready security configuration  

---

## Security Architecture

### Authentication Protocol

```
┌─────────────────────────────────────────────────────────────┐
│                    SECURITY LAYERS                           │
├─────────────────────────────────────────────────────────────┤
│ 1. HTTPS/TLS: All traffic encrypted in transit              │
│ 2. JWT Bearer Tokens: Stateless authentication              │
│ 3. Role-Based Access Control: ADMIN & USER roles            │
│ 4. Table-Level Permissions: Granular resource access        │
│ 5. Password Hashing: BCrypt with salt rounds                │
│ 6. CORS Protection: Whitelist specific origins              │
└─────────────────────────────────────────────────────────────┘
```

### Token Management

#### Access Token
- **Type**: JWT Bearer Token
- **Validity**: 15 minutes (900,000 ms)
- **Use**: API request authorization
- **Contents**: Username, Roles, Authorities
- **Header Format**: `Authorization: Bearer <token>`

#### Refresh Token
- **Type**: JWT Token
- **Validity**: 7 days (604,800,000 ms)
- **Use**: Obtain new access token
- **Contents**: Username, Token Type marker
- **Storage**: Client-side (secure storage recommended)

### Token Flow Diagram

```
Client Login
    ↓
POST /ecom/auth/login
    ↓
Server validates credentials → BCrypt password check
    ↓
Generate tokens:
  - Access Token (15 min)
  - Refresh Token (7 days)
    ↓
Return in AuthResponse
    ↓
Client stores tokens
    ↓
Use Access Token for API calls
    ↓
When expired → POST /ecom/auth/refresh
    ↓
Server validates refresh token
    ↓
Generate new Access Token
```

### Database Authentication

**PostgreSQL Connection**:
```properties
# Encrypted connection with credentials
spring.datasource.url=jdbc:postgresql://host:5432/database
spring.datasource.username=db_user
spring.datasource.password=encrypted_password

# Connection pooling with HikariCP
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.connection-timeout=30000
```

**Security Features**:
- Connection pooling (reduces connection attempts)
- SSL/TLS connection option available
- Credential rotation support via environment variables

---

## Database Setup

### Step 1: Create PostgreSQL Database on Render

1. Go to [Render Dashboard](https://dashboard.render.com)
2. Click "New +" → Select "PostgreSQL"
3. Configure:
   - **Name**: `ecommerce-db`
   - **Region**: Choose closest to your users
   - **PostgreSQL Version**: 15 or latest
   - **Database**: `ecommerce`
   - **User**: `ecommerce_user`
   - **Password**: Generate strong password (copy for later)

4. Note the connection details:
   ```
   Host: xxx.render.internal
   Port: 5432
   Database: ecommerce
   User: ecommerce_user
   Password: <generated>
   ```

### Step 2: Initialize Database Schema

The application will auto-create tables on first run (with `spring.jpa.hibernate.ddl-auto=update`).

**Required Tables** (will be created automatically):
- `users` - User accounts with roles
- `products` - Product catalog
- `orders` - Customer orders
- `cart` - Shopping cart items
- `user_permissions` - Role-based access control
- `payments` - Payment records
- `addresses` - Shipping addresses
- And others...

### Step 3: Create Initial Admin User

After deployment, create admin user:

```bash
# Using SQL directly
INSERT INTO users (email, password, role, status, created_at)
VALUES ('admin@example.com', '$2a$10$...', 'ROLE_ADMIN', 'ACTIVE', NOW());
```

Or use the registration endpoint:
```bash
POST http://localhost:8080/ecom/customers
Content-Type: application/json

{
  "email": "admin@example.com",
  "password": "SecurePassword123!",
  "role": "ROLE_ADMIN"
}
```

---

## Backend Configuration

### Step 1: Update application.properties

File: `eCommersApp/src/main/resources/application.properties`

```properties
# Use default development config locally
spring.datasource.url=jdbc:postgresql://localhost:5432/ecommerce
spring.datasource.username=postgres
spring.datasource.password=password
```

### Step 2: Create Production Configuration

File: `eCommersApp/src/main/resources/application-prod.properties`

Already created ✅ - Uses environment variables

### Step 3: Configure JWT Settings

Update JWT secret and expiration (in `application-prod.properties`):

```properties
jwt.secret=${JWT_SECRET}
jwt.access-token-expiration=${JWT_ACCESS_EXPIRATION:900000}
jwt.refresh-token-expiration=${JWT_REFRESH_EXPIRATION:604800000}
```

### Step 4: Security Configuration

Verify `AppConfig.java` has:
- ✅ CORS whitelist configured
- ✅ JWT filter registered
- ✅ Role-based endpoint protection
- ✅ HTTPS enforcement

---

## Frontend Configuration

### Step 1: Update API Configuration

File: `frontend/src/Router/api.jsx`

```javascript
// Detect environment and set API base URL
const getApiBaseUrl = () => {
  if (process.env.NODE_ENV === 'production') {
    return process.env.REACT_APP_API_URL || 'https://your-backend.onrender.com';
  }
  return 'http://localhost:8080';
};

const API_BASE_URL = getApiBaseUrl();

export const loginUser = async (email, password) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/ecom/auth/login`, {
      email,
      password
    });
    
    // Store tokens
    localStorage.setItem('accessToken', response.data.accessToken);
    localStorage.setItem('refreshToken', response.data.refreshToken);
    localStorage.setItem('tokenExpiry', Date.now() + response.data.expiresIn);
    
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

export const refreshAccessToken = async () => {
  const refreshToken = localStorage.getItem('refreshToken');
  if (!refreshToken) throw new Error('No refresh token found');
  
  try {
    const response = await axios.post(`${API_BASE_URL}/ecom/auth/refresh`, {
      refreshToken
    });
    
    localStorage.setItem('accessToken', response.data.accessToken);
    return response.data;
  } catch (error) {
    // Refresh failed - logout user
    logout();
    throw error;
  }
};

export const getAuthHeaders = () => {
  const token = localStorage.getItem('accessToken');
  return {
    Authorization: `Bearer ${token}`,
    'Content-Type': 'application/json'
  };
};
```

### Step 2: Create `.env.local` for Development

```bash
REACT_APP_API_URL=http://localhost:8080
REACT_APP_ENV=development
```

### Step 3: Create `.env.production` for Deployment

```bash
REACT_APP_API_URL=https://your-backend.onrender.com
REACT_APP_ENV=production
```

---

## Deployment to Render

### Phase 1: Prepare Backend

#### 1.1 Update pom.xml with Production Profile

Add to `eCommersApp/pom.xml`:

```xml
<profiles>
  <profile>
    <id>prod</id>
    <activation>
      <activeByDefault>false</activeByDefault>
    </activation>
    <properties>
      <maven.test.skip>true</maven.test.skip>
    </properties>
  </profile>
</profiles>
```

#### 1.2 Create `.env.example` in Backend

(Already created ✅)

#### 1.3 Update Dockerfile

(Already created ✅ - uses multi-stage build)

### Phase 2: Deploy Backend to Render

#### 2.1 Create GitHub Repository

```bash
cd h:\mascot\eCommerce-Application
git init
git add .
git commit -m "Initial commit - ready for deployment"
git branch -M main
git remote add origin https://github.com/yourusername/ecommerce-app.git
git push -u origin main
```

#### 2.2 Create Render Service

1. Go to [Render Dashboard](https://dashboard.render.com)
2. Click "New +" → Select "Web Service"
3. Connect your GitHub repository
4. Configure:
   - **Name**: `ecommerce-api`
   - **Environment**: `Docker`
   - **Plan**: `Standard` (for production)
   - **Region**: Same as database

#### 2.3 Set Environment Variables

In Render Service → Environment:

```
DB_HOST=your-postgres-xxx.render.internal
DB_PORT=5432
DB_NAME=ecommerce
DB_USER=ecommerce_user
DB_PASSWORD=<paste from database creation>
JWT_SECRET=generate-a-very-long-random-string-min-32-chars
JWT_ACCESS_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=604800000
ADMIN_PASSWORD=strong-admin-password
CORS_ORIGINS=http://localhost:3000,https://yourdomain.com,https://frontend-url.onrender.com
PORT=8080
SPRING_PROFILES_ACTIVE=prod
```

#### 2.4 Deploy

- Render auto-deploys when you push to main branch
- Monitor logs in Render dashboard
- Wait for deployment to complete (~10-15 minutes for first build)

### Phase 3: Deploy Frontend to Render

#### 3.1 Create Render Web Service for Frontend

1. Go to Render → "New +" → "Static Site"
2. Connect frontend directory
3. Configure:
   - **Name**: `ecommerce-frontend`
   - **Build Command**: `npm run build`
   - **Publish Directory**: `build`

#### 3.2 Set Environment Variables

```
REACT_APP_API_URL=https://your-ecommerce-api.onrender.com
```

#### 3.3 Deploy Frontend

Push to main → Auto-deploys

---

## User & Permission Management

### Creating New Users

#### Via REST API

```bash
POST /ecom/customers
Content-Type: application/json

{
  "email": "newuser@example.com",
  "password": "SecurePass123!",
  "name": "John Doe",
  "phone": "1234567890"
}
```

#### User Roles

1. **ROLE_ADMIN**: Full system access
   - Can create, read, update, delete all resources
   - Can manage user permissions
   - Can generate middleware tokens

2. **ROLE_USER**: Customer access
   - Can view products
   - Can place orders
   - Can manage own profile
   - Limited table access

### Granting Permissions

#### Example: Grant READ access to PRODUCTS table

```bash
POST /ecom/auth/permissions
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "userId": 2,
  "resourceName": "PRODUCTS",
  "permissionType": "READ",
  "notes": "Can view product catalog"
}
```

#### Available Resources

```
PRODUCTS      - Product information
ORDERS        - Customer orders
USERS         - User accounts
PAYMENTS      - Payment records
CART          - Shopping cart
REVIEWS       - Product reviews
SHIPPING      - Shipping information
REPORTS       - Analytics/reports
```

#### Available Permissions

```
CREATE    - Can create new records
READ      - Can view records
UPDATE    - Can modify records
DELETE    - Can remove records
EXPORT    - Can export data
IMPORT    - Can import data
ADMIN     - Administrative access
```

### View User Permissions

```bash
GET /ecom/auth/permissions/{userId}
Authorization: Bearer <admin-token>

Response:
{
  "userId": 2,
  "permissions": [
    {
      "permissionId": 1,
      "resource": "PRODUCTS",
      "permission": "READ",
      "active": true
    },
    {
      "permissionId": 2,
      "resource": "ORDERS",
      "permission": "READ",
      "active": true
    }
  ]
}
```

### Revoke Permissions

```bash
DELETE /ecom/auth/permissions/{permissionId}
Authorization: Bearer <admin-token>

Response:
{
  "message": "Permission revoked successfully"
}
```

---

## Authentication Flow

### Login Flow

```
1. User submits credentials
   POST /ecom/auth/login
   {
     "email": "user@example.com",
     "password": "password123"
   }

2. Server validates:
   - Email exists
   - Password matches (BCrypt)
   - Account is active

3. Server generates tokens:
   - Access Token (15 min, includes roles)
   - Refresh Token (7 days)

4. Server returns:
   {
     "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
     "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
     "expiresIn": 900000,
     "refreshExpiresIn": 604800000,
     "userId": 1,
     "role": "ROLE_USER",
     "middlewareCompatible": true
   }

5. Client stores tokens:
   - localStorage.setItem('accessToken', token)
   - localStorage.setItem('refreshToken', token)

6. Client uses token in requests:
   GET /ecom/products
   Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

### Token Refresh Flow

```
1. User makes API request with expired token
   GET /ecom/orders
   Authorization: Bearer <expired_token>

2. Server returns 401 Unauthorized

3. Client sends refresh request:
   POST /ecom/auth/refresh
   {
     "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
   }

4. Server validates refresh token:
   - Token is valid (not expired)
   - Token is in database (not revoked)
   - User account is active

5. Server generates new access token:
   {
     "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
     "expiresIn": 900000
   }

6. Client stores new token and retries request

7. If refresh token expired:
   - User must login again
```

---

## Third-Party Middleware Integration

### Generate Middleware Token

For third-party services to access your API:

```bash
POST /ecom/auth/middleware-token
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "username": "middleware-service@example.com",
  "scopes": ["READ_PRODUCTS", "READ_ORDERS", "WRITE_REVIEWS"]
}

Response:
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "username": "middleware-service@example.com",
  "scopes": ["READ_PRODUCTS", "READ_ORDERS", "WRITE_REVIEWS"],
  "expiresIn": 900000,
  "message": "Middleware token generated. Valid for 15 minutes"
}
```

### Middleware Usage

Third-party service uses the token:

```javascript
const middlewareToken = "eyJhbGciOiJIUzUxMiJ9...";

fetch('https://your-ecommerce-api.onrender.com/ecom/products', {
  headers: {
    'Authorization': `Bearer ${middlewareToken}`,
    'Content-Type': 'application/json'
  }
});
```

### Webhook Integration Example

```javascript
// Your middleware service
const integrateWithEcommerce = async () => {
  // 1. Get middleware token from admin
  const tokenResponse = await fetch('/auth/middleware-token', {
    method: 'POST',
    body: JSON.stringify({
      scopes: ['READ_PRODUCTS', 'READ_ORDERS']
    })
  });

  const { token } = await tokenResponse.json();

  // 2. Call eCommerce API with token
  const productsResponse = await fetch('/ecom/products', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });

  const products = await productsResponse.json();
  
  // 3. Process data
  return products;
};
```

---

## API Endpoints

### Authentication Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/ecom/auth/login` | Login user | ❌ |
| POST | `/ecom/auth/refresh` | Refresh token | ❌ |
| GET | `/ecom/auth/me` | Get current user | ✅ |
| POST | `/ecom/auth/logout` | Logout | ✅ |
| POST | `/ecom/auth/permissions` | Grant permission | ✅ ADMIN |
| GET | `/ecom/auth/permissions/{userId}` | View permissions | ✅ |
| DELETE | `/ecom/auth/permissions/{permissionId}` | Revoke permission | ✅ ADMIN |
| POST | `/ecom/auth/middleware-token` | Generate middleware token | ✅ ADMIN |

### Product Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/ecom/products` | List all products | ❌ |
| GET | `/ecom/products/{id}` | Get product details | ❌ |
| POST | `/ecom/products` | Create product | ✅ ADMIN |
| PUT | `/ecom/products/{id}` | Update product | ✅ ADMIN |
| DELETE | `/ecom/products/{id}` | Delete product | ✅ ADMIN |

### Order Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/ecom/orders` | List user orders | ✅ USER |
| POST | `/ecom/orders` | Create order | ✅ USER |
| GET | `/ecom/orders/{id}` | Get order details | ✅ USER |
| PUT | `/ecom/orders/{id}` | Update order | ✅ ADMIN |
| DELETE | `/ecom/orders/{id}` | Delete order | ✅ ADMIN |

---

## Troubleshooting

### Common Issues

#### 1. "Invalid JWT Token" Error

**Cause**: JWT secret mismatch or token expired

**Solution**:
```properties
# Ensure JWT_SECRET is same in all environments
# Check token expiration time
# Generate new token with correct secret
```

#### 2. Database Connection Failed

**Cause**: Incorrect credentials or network isolation

**Solution**:
```bash
# Check credentials in Render dashboard
# Ensure PostgreSQL is in same region
# Add backend service private IP to database whitelist
```

#### 3. CORS Errors

**Cause**: Frontend domain not whitelisted

**Solution**:
```properties
# Add frontend URL to CORS_ORIGINS
CORS_ORIGINS=https://yourdomain.com,https://frontend.onrender.com
```

#### 4. Token Refresh Fails

**Cause**: Refresh token expired or database issue

**Solution**:
```bash
# Refresh tokens valid 7 days - user must login again if expired
# Check database connection for revocation lookup
```

### Debug Mode

Enable verbose logging:

```properties
logging.level.Ecom=DEBUG
logging.level.org.springframework.security=DEBUG
```

### Health Check

```bash
GET http://your-backend.onrender.com/actuator/health

Response:
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "livenessState": { "status": "UP" },
    "readinessState": { "status": "UP" }
  }
}
```

---

## Security Best Practices

✅ **Do**:
- Store tokens in secure storage (httpOnly cookies better than localStorage)
- Use HTTPS for all connections
- Rotate JWT secret periodically
- Implement token blacklist for logout
- Use strong passwords for users
- Enable CORS only for trusted origins
- Monitor suspicious authentication attempts
- Use environment variables for secrets

❌ **Don't**:
- Commit `.env` files to version control
- Log sensitive data (passwords, tokens)
- Use weak JWT secrets
- Trust client-side token validation only
- Store tokens in plain text
- Allow long token validity periods
- Use HTTP in production

---

## Next Steps

1. ✅ Backend security configured with JWT
2. ✅ Frontend configured for token management
3. ✅ Database setup documented
4. ✅ Deployment to Render configured
5. Deploy to Render following Phase 1, 2, 3 above
6. Test all endpoints with provided Postman collection
7. Monitor logs and health metrics
8. Set up backup strategy for database
9. Configure SSL certificates (auto on Render)
10. Set up CI/CD pipeline (optional)

---

## Support Resources

- [Render Documentation](https://render.com/docs)
- [Spring Boot Security](https://spring.io/guides/gs/securing-web/)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8725)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [React Security](https://cheatsheetseries.owasp.org/cheatsheets/React_Security_Cheat_Sheet.html)

---

## Contact & Questions

For issues or questions:
1. Check this documentation
2. Review API response error messages
3. Enable debug logging
4. Check Render service logs
5. Test with provided postman collection

---

**Last Updated**: February 2026  
**Version**: 1.0  
**Security Level**: Production-Ready
