# 🚀 HƯỚNG DẪN DEPLOY LÊN RENDER - TIẾNG VIỆT

## 🎯 Mục Tiêu
Deploy ứng dụng eCommerce lên Render.com để chạy **24/7 ổn định**, kể cả khi tắt máy tính. Ứng dụng sẽ chạy trên server của Render (cloud), hoàn toàn độc lập.

**Thời gian hoàn thành**: 30-40 phút  
**Chi phí**: MIỄN PHÍ (dùng plan Free của Render)

---

## ✅ BƯỚC 1: CÀI ĐẶT CÔNG CỤ CẦN THIẾT

### 1.1 Kiểm Tra Java
```powershell
java -version
```
**Cần**: Java 17 hoặc cao hơn  
**Nếu chưa có**: Tải tại https://www.oracle.com/java/technologies/downloads/

### 1.2 Kiểm Tra Git
```powershell
git --version
```
**Nếu chưa có**: Tải tại https://git-scm.com/download/win

### 1.3 Kiểm Tra Node.js
```powershell
node --version
npm --version
```
**Nếu chưa có**: Tải tại https://nodejs.org/

---

## ✅ BƯỚC 2: THIẾT LẬP JAVA_HOME (Quan Trọng!)

### Windows:
```powershell
# Tìm thư mục Java
dir "C:\Program Files\Java"

# Thiết lập JAVA_HOME (thay jdk-24 bằng version bạn có)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-24"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# Kiểm tra
echo $env:JAVA_HOME
javac -version
```

**Thiết lập vĩnh viễn**:
1. Nhấn `Windows + R`, gõ `sysdm.cpl`
2. Tab "Advanced" → "Environment Variables"
3. System Variables → New:
   - Variable name: `JAVA_HOME`
   - Variable value: `C:\Program Files\Java\jdk-24` (đường dẫn JDK của bạn)
4. Tìm `Path` → Edit → Add: `%JAVA_HOME%\bin`
5. OK → Mở PowerShell mới → Test

---

## ✅ BƯỚC 3: BUILD PROJECT LOCAL (Kiểm Tra Lỗi)

### 3.1 Build Backend
```powershell
cd h:\mascot\eCommerce-Application\eCommersApp

# Build với Maven wrapper
.\mvnw.cmd clean package -DskipTests

# Hoặc nếu đã cài Maven
mvn clean package -DskipTests
```

**Kết quả mong đợi**: 
```
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

**File JAR tạo ra**: `target/Ecom-0.0.1-SNAPSHOT.jar`

### 3.2 Build Frontend
```powershell
cd h:\mascot\eCommerce-Application\frontend

# Cài dependencies (chỉ lần đầu)
npm install

# Build production
npm run build
```

**Kết quả mong đợi**: Thư mục `build/` được tạo

---

## ✅ BƯỚC 4: TẠO TÀI KHOẢN RENDER.COM

### 4.1 Đăng Ký Render
1. Truy cập: https://render.com
2. Click **"Get Started"**
3. Chọn **"Sign Up with GitHub"** (khuyến nghị) hoặc Email
4. Xác nhận email

### 4.2 Tạo PostgreSQL Database (MIỄN PHÍ)
1. Sau khi đăng nhập Render → Click **"New +"**
2. Chọn **"PostgreSQL"**
3. Điền thông tin:
   ```
   Name: ecommerce-db
   Database: ecommerce
   User: ecommerce_user
   Region: Singapore (gần VN nhất)
   PostgreSQL Version: 15 hoặc 16
   Plan: Free
   ```
4. Click **"Create Database"**
5. **QUAN TRỌNG**: Sau khi tạo xong, vào tab **"Info"**, copy lưu lại:
   ```
   Internal Database URL: postgresql://...
   External Database URL: postgresql://...
   Host: xxx.oregon-postgres.render.com
   Port: 5432
   Database: ecommerce
   Username: ecommerce_user
   Password: [password dài]
   ```

**Lưu ý**: Database Free sẽ bị xóa sau 90 ngày không dùng, nhưng cho demo/test thì OK.

---

## ✅ BƯỚC 5: TẠO GITHUB REPOSITORY

### 5.1 Tạo Repository Mới
1. Vào https://github.com
2. Click dấu **"+"** góc trên → **"New repository"**
3. Điền:
   ```
   Repository name: ecommerce-application
   Description: eCommerce Full Stack App
   Visibility: Public (để Render kết nối miễn phí)
   ✓ Add a README file: KHÔNG chọn
   ```
4. Click **"Create repository"**

### 5.2 Push Code Lên GitHub
```powershell
cd h:\mascot\eCommerce-Application

# Khởi tạo Git (nếu chưa có)
git init
git branch -M main

# Tạo .gitignore
@"
# IDE
.vscode/
.idea/
*.swp

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

# Build
build/
dist/

# OS
.DS_Store
Thumbs.db
"@ | Out-File -FilePath .gitignore -Encoding utf8

# Add files
git add .
git commit -m "Initial commit - Ready for Render deployment"

# Thêm remote (THAY your-username và your-repo)
git remote add origin https://github.com/your-username/ecommerce-application.git

# Push
git push -u origin main
```

**Nếu lỗi authentication**: Dùng Personal Access Token (PAT)
1. GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Generate new token → Chọn quyền `repo`
3. Copy token → Dùng làm password khi push

---

## ✅ BƯỚC 6: DEPLOY BACKEND LÊN RENDER

### 6.1 Tạo Web Service
1. Render Dashboard → **"New +"** → **"Web Service"**
2. Click **"Connect a repository"** 
3. Authorize GitHub (nếu chưa)
4. Chọn repository **"ecommerce-application"**
5. Click **"Connect"**

### 6.2 Cấu Hình Service
```
Name: ecommerce-backend
Region: Singapore
Branch: main
Root Directory: eCommersApp
Runtime: Docker
```

**QUAN TRỌNG**: Render sẽ tự động phát hiện `Dockerfile` trong thư mục `eCommersApp`

### 6.3 Thiết Lập Environment Variables

Click **"Advanced"** → **"Add Environment Variable"**

Thêm **TỪNG CÁI** sau (copy chính xác):

```bash
# Database (lấy từ PostgreSQL bạn tạo ở Bước 4)
DB_HOST=dpg-xxxxx.oregon-postgres.render.com
DB_PORT=5432
DB_NAME=ecommerce
DB_USER=ecommerce_user
DB_PASSWORD=xxxxxxxxxxxxxxxxxxxxx

# JWT Security (TỰ TẠO SECRET MỚI)
JWT_SECRET=your-super-secret-key-min-32-characters-long-12345678
JWT_ACCESS_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=604800000

# Spring Configuration
SPRING_PROFILES_ACTIVE=prod
PORT=10000

# CORS (TẠM THỜI để *, sau sẽ sửa)
CORS_ORIGINS=*

# Admin Password (TỰ ĐẶT PASSWORD ADMIN)
ADMIN_PASSWORD=YourStrongAdminPassword123!
```

**Cách lấy DB credentials**:
1. Vào PostgreSQL service vừa tạo
2. Tab "Info" → Copy từng giá trị

**Cách tạo JWT_SECRET mạnh**:
```powershell
# Tạo random string 32 ký tự
-join ((65..90) + (97..122) + (48..57) | Get-Random -Count 32 | ForEach-Object {[char]$_})
```

### 6.4 Chọn Plan
```
Instance Type: Free
```

**Lưu ý**: Free plan có giới hạn:
- Tự động sleep sau 15 phút không dùng
- Khởi động lại khi có request (mất ~30 giây lần đầu)
- 750 giờ/tháng miễn phí

Nếu muốn **chạy 24/7 không sleep**, nâng lên **Starter plan ($7/tháng)**

### 6.5 Deploy
1. Click **"Create Web Service"**
2. Chờ deploy (10-15 phút lần đầu)
3. Xem logs để theo dõi

**Logs thành công sẽ thấy**:
```
Application started........................
```

### 6.6 Lấy URL Backend
Sau khi deploy xong:
```
URL: https://ecommerce-backend-xxxx.onrender.com
```

**Test URL**:
```powershell
curl https://ecommerce-backend-xxxx.onrender.com/actuator/health
```

Kết quả mong đợi:
```json
{"status":"UP"}
```

---

## ✅ BƯỚC 7: DEPLOY FRONTEND LÊN RENDER

### 7.1 Cập Nhật API URL Trong Frontend

**Chỉnh file**: `frontend/src/Router/api.jsx`

Tìm dòng:
```javascript
const API_BASE_URL = getApiBaseUrl();
```

Đảm bảo function `getApiBaseUrl()` có:
```javascript
const getApiBaseUrl = () => {
  const env = process.env.NODE_ENV;
  const customApiUrl = process.env.REACT_APP_API_URL;
  
  if (customApiUrl) {
    return customApiUrl;
  }
  
  if (env === 'production') {
    return process.env.REACT_APP_API_URL || 'https://ecommerce-backend-xxxx.onrender.com';
  }
  
  return 'http://localhost:8080';
};
```

**Commit & push thay đổi**:
```powershell
cd h:\mascot\eCommerce-Application
git add .
git commit -m "Update API URL for production"
git push
```

### 7.2 Tạo Static Site
1. Render Dashboard → **"New +"** → **"Static Site"**
2. Chọn repository **"ecommerce-application"**
3. Click **"Connect"**

### 7.3 Cấu Hình Static Site
```
Name: ecommerce-frontend
Branch: main
Root Directory: frontend
Build Command: npm run build
Publish Directory: build
```

### 7.4 Environment Variables
Click **"Advanced"** → Thêm:
```
REACT_APP_API_URL=https://ecommerce-backend-xxxx.onrender.com
```
(THAY URL backend thật của bạn)

### 7.5 Deploy Frontend
1. Click **"Create Static Site"**
2. Chờ build (5-10 phút)
3. Sau khi xong, lấy URL:
   ```
   https://ecommerce-frontend-xxxx.onrender.com
   ```

---

## ✅ BƯỚC 8: CẬP NHẬT CORS (Quan Trọng!)

Sau khi có URL frontend, cần cập nhật CORS cho backend:

1. Vào **Backend Web Service** → **Environment**
2. Sửa biến `CORS_ORIGINS`:
   ```
   CORS_ORIGINS=https://ecommerce-frontend-xxxx.onrender.com
   ```
3. Click **"Save Changes"**
4. Backend sẽ tự động redeploy (~2 phút)

---

## ✅ BƯỚC 9: TẠO ADMIN USER

### 9.1 Dùng Postman hoặc cURL

**Import Postman Collection**:
1. Mở file `Postman_Collection.json`
2. Import vào Postman
3. Đổi `BASE_URL` thành `https://ecommerce-backend-xxxx.onrender.com`

**Hoặc dùng cURL**:
```powershell
# Tạo user mới
curl -X POST https://ecommerce-backend-xxxx.onrender.com/ecom/customers `
  -H "Content-Type: application/json" `
  -d '{
    "email": "admin@example.com",
    "password": "Admin123!",
    "name": "Administrator",
    "phone": "0123456789"
  }'
```

### 9.2 Đăng Nhập
```powershell
# Login để lấy token
curl -X POST https://ecommerce-backend-xxxx.onrender.com/ecom/auth/login `
  -H "Content-Type: application/json" `
  -d '{
    "email": "admin@example.com",
    "password": "Admin123!"
  }'
```

Kết quả trả về:
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "userId": 1,
  "role": "ROLE_USER"
}
```

**Lưu lại `accessToken`** để dùng cho requests tiếp theo.

### 9.3 Cấp Quyền ADMIN (Từ Database)

Vì user mới tạo là ROLE_USER, cần nâng lên ADMIN:

**Cách 1: Dùng Render PostgreSQL Console**
1. Vào PostgreSQL service → Tab **"Shell"**
2. Chạy lệnh SQL:
```sql
-- Kiểm tra user
SELECT id, email, role FROM users;

-- Nâng lên ADMIN (thay 1 bằng user ID thật)
UPDATE users SET role = 'ROLE_ADMIN' WHERE id = 1;

-- Kiểm tra lại
SELECT id, email, role FROM users;
```

**Cách 2: Code sẵn trong ứng dụng**
Nếu bạn muốn user đầu tiên tự động là ADMIN, có thể sửa code registration endpoint.

---

## ✅ BƯỚC 10: TEST ỨNG DỤNG

### 10.1 Test Frontend
Mở trình duyệt:
```
https://ecommerce-frontend-xxxx.onrender.com
```

Nên thấy giao diện ứng dụng.

### 10.2 Test API Endpoints

**Health Check**:
```powershell
curl https://ecommerce-backend-xxxx.onrender.com/actuator/health
```

**Lấy danh sách sản phẩm**:
```powershell
curl https://ecommerce-backend-xxxx.onrender.com/ecom/products
```

**Login**:
```powershell
curl -X POST https://ecommerce-backend-xxxx.onrender.com/ecom/auth/login `
  -H "Content-Type: application/json" `
  -d '{"email":"admin@example.com","password":"Admin123!"}'
```

### 10.3 Test Phân Quyền

**Cấp quyền cho user** (dùng admin token):
```powershell
$token = "eyJhbGc..." # Token admin từ login

curl -X POST https://ecommerce-backend-xxxx.onrender.com/ecom/auth/permissions `
  -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" `
  -d '{
    "userId": 2,
    "resourceName": "PRODUCTS",
    "permissionType": "READ",
    "notes": "Cho phép xem sản phẩm"
  }'
```

**Xem quyền của user**:
```powershell
curl https://ecommerce-backend-xxxx.onrender.com/ecom/auth/permissions/2 `
  -H "Authorization: Bearer $token"
```

---

## ✅ BƯỚC 11: ĐẢM BẢO ỔN ĐỊNH 24/7

### 11.1 Nâng Cấp Plan (Khuyến Nghị)

**Free Plan**:
- ✅ Miễn phí
- ❌ Sleep sau 15 phút
- ❌ Khởi động lại mất 30s
- ✅ OK cho demo/test

**Starter Plan ($7/tháng)**:
- ✅ Không sleep
- ✅ Chạy 24/7
- ✅ Nhanh hơn
- ✅ Phù hợp production

**Nâng cấp**:
1. Backend service → Settings → **"Upgrade Instance Type"**
2. Chọn **Starter** → Confirm

### 11.2 Thiết Lập Health Check

Render tự động ping health endpoint mỗi 5 phút. Nếu fail 3 lần, sẽ restart.

**Đảm bảo endpoint hoạt động**:
```
https://ecommerce-backend-xxxx.onrender.com/actuator/health
```

### 11.3 Monitor Logs

**Xem logs**:
1. Backend service → **"Logs"** tab
2. Theo dõi lỗi

**Lọc logs**:
```
ERROR   - Chỉ lỗi
WARN    - Cảnh báo
INFO    - Thông tin
```

### 11.4 Database Backup

**Free PostgreSQL**: Không có auto backup

**Paid PostgreSQL ($7/tháng)**: 
- Auto backup hàng ngày
- Restore bất cứ lúc nào

**Manual backup**:
```powershell
# Tải pgAdmin hoặc dùng pg_dump
pg_dump -h dpg-xxx.oregon-postgres.render.com -U ecommerce_user -d ecommerce > backup.sql
```

### 11.5 Keep Service Active (Trick cho Free Plan)

Nếu dùng Free plan, có thể dùng cron job ping mỗi 10 phút:

**Dùng UptimeRobot** (miễn phí):
1. Vào https://uptimerobot.com
2. Tạo monitor:
   - Type: HTTP(s)
   - URL: https://ecommerce-backend-xxxx.onrender.com/actuator/health
   - Interval: 5 minutes

Service sẽ không bao giờ sleep!

---

## ✅ BƯỚC 12: TÙY CHỈNH & TỐI ƯU

### 12.1 Custom Domain (Tùy Chọn)

**Thêm domain riêng** (ví dụ: myshop.com):

1. Mua domain (Namecheap, GoDaddy, etc.)
2. Backend service → Settings → **"Custom Domain"**
3. Thêm: `api.myshop.com`
4. Config DNS:
   ```
   Type: CNAME
   Name: api
   Value: ecommerce-backend-xxxx.onrender.com
   ```
5. Frontend tương tự: `myshop.com` → CNAME

### 12.2 SSL Certificate

Render tự động cấp SSL miễn phí (Let's Encrypt). HTTPS tự động!

### 12.3 Tăng Performance

**Backend**:
- Database connection pool đã được config trong `application-prod.properties`
- JVM memory optimization trong Dockerfile

**Frontend**:
- Code splitting tự động (React)
- Gzip compression (Render tự động)
- CDN caching

### 12.4 Environment-based Config

**Development** (local):
```
http://localhost:8080
```

**Production** (Render):
```
https://ecommerce-backend-xxxx.onrender.com
```

File `api.jsx` đã tự động switch!

---

## 🎯 TỔNG KẾT

### ✅ Checklist Hoàn Thành

- [ ] Java, Git, Node.js đã cài
- [ ] JAVA_HOME đã thiết lập
- [ ] Backend build thành công
- [ ] Frontend build thành công
- [ ] Tài khoản Render đã tạo
- [ ] PostgreSQL database đã tạo
- [ ] GitHub repository đã tạo
- [ ] Code đã push lên GitHub
- [ ] Backend đã deploy lên Render
- [ ] Frontend đã deploy lên Render
- [ ] CORS đã cập nhật
- [ ] Admin user đã tạo
- [ ] API endpoints test OK
- [ ] Ứng dụng chạy ổn định

### 🔗 URLs Quan Trọng

Lưu lại các URL sau:

```
Backend URL:    https://ecommerce-backend-xxxx.onrender.com
Frontend URL:   https://ecommerce-frontend-xxxx.onrender.com
Database URL:   postgresql://...
GitHub Repo:    https://github.com/your-username/ecommerce-application
```

### 📊 Thông Tin Hệ Thống

```
Database:       PostgreSQL 15/16
Backend:        Java 17 + Spring Boot 3.1.1
Frontend:       React 18
Hosting:        Render.com
SSL:            ✅ Auto (Let's Encrypt)
Uptime:         99.9% (Paid) / 95% (Free)
```

### 🔐 Credentials Quan Trọng

**Lưu vào nơi an toàn**:
```
PostgreSQL Password: xxxxx
JWT Secret: xxxxx
Admin Email: admin@example.com
Admin Password: xxxxx
GitHub Personal Access Token: xxxxx
```

---

## 🆘 XỬ LÝ SỰ CỐ

### Ứng dụng không khởi động

**Kiểm tra**:
1. Logs → Tìm lỗi
2. Environment variables → Đúng chưa?
3. Database → Kết nối OK?

### Database connection failed

**Giải pháp**:
```powershell
# Test kết nối
psql postgresql://user:pass@host:5432/database

# Hoặc dùng pgAdmin
```

### CORS errors

**Giải pháp**:
- Kiểm tra `CORS_ORIGINS` có URL frontend chưa
- Redeploy backend sau khi sửa

### 502 Bad Gateway

**Nguyên nhân**: Service đang khởi động

**Giải pháp**: Chờ 30-60s, refresh lại

### Out of memory

**Giải pháp**:
- Nâng plan lên Starter
- Giảm JVM heap size trong Dockerfile

---

## 📞 HỖ TRỢ

### Tài Liệu Render
- https://render.com/docs
- https://render.com/docs/deploy-spring-boot

### Stack Overflow
- Tag: `render`, `spring-boot`, `postgresql`

### Video Hướng Dẫn
- Tìm "Deploy Spring Boot to Render" trên YouTube

---

## 🎉 CHÚC MỪNG!

Ứng dụng của bạn giờ đã:
- ✅ Chạy trên cloud 24/7
- ✅ Có database riêng
- ✅ SSL/HTTPS tự động
- ✅ Độc lập với máy tính
- ✅ Ổn định tối thiểu 3 ngày (thực tế là vô thời hạn)

**Ứng dụng sẽ chạy ổn định miễn là**:
- Render service không bị xóa
- Database không hết quota
- Domain không expire (nếu dùng custom domain)

**Tắt máy tính → Vẫn chạy bình thường!** 🚀

---

**Version**: 1.0 Vietnamese  
**Last Updated**: February 6, 2026  
**Status**: Production Ready
