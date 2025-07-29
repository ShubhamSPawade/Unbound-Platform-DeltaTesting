# Unbound Platform - Complete Project Documentation

## 🎯 **PROJECT OVERVIEW**

**Unbound Platform** is a comprehensive fest and event management system built with Spring Boot. It serves as a bridge between colleges (who host fests/events) and students (who participate in them), with admin oversight for content moderation.

### **Core Concept**
- **Colleges** can host fests and events, manage registrations, view analytics, approve certificates, and send emails
- **Students** can explore events, register for them, make payments, give reviews, download certificates, and filter content
- **Admins** can approve/reject fests and events, monitor platform activity, and manage colleges
- **Public Users** can explore fests and events without authentication

---

## 🏗️ **SYSTEM ARCHITECTURE**

### **Technology Stack**
- **Backend**: Spring Boot 3.x with Java 17
- **Database**: MySQL 8.0
- **Authentication**: JWT (JSON Web Tokens)
- **Payment**: Razorpay integration
- **Email**: Spring Mail with Gmail SMTP
- **File Storage**: Local file system with organized directories
- **Documentation**: Swagger/OpenAPI 3.0
- **PDF Generation**: OpenPDF for certificates
- **Build Tool**: Maven

### **Layered Architecture**
```
┌─────────────────┐
│   Controllers   │ ← REST API endpoints with Swagger docs
├─────────────────┤
│    Services     │ ← Business logic and external integrations
├─────────────────┤
│  Repositories   │ ← Data access layer (Spring Data JPA)
├─────────────────┤
│    Entities     │ ← JPA entities for database mapping
└─────────────────┘
```

### **Key Components**
- **Controllers**: Handle HTTP requests/responses with proper validation
- **Services**: Business logic, external integrations (email, payments, file storage)
- **Repositories**: Data access with Spring Data JPA
- **Entities**: JPA entities with Lombok for boilerplate reduction
- **DTOs**: Request/response objects with validation
- **Config**: Security, CORS, Swagger, and application configuration

---

## 👥 **USER ROLES & PERMISSIONS**

### **1. Student Role**
**Permissions:**
- Explore fests and events (public access)
- Register for events with payment
- Create/join teams for team events
- Give reviews and ratings
- Download participation certificates
- View personal dashboard with registrations
- Receive email notifications and receipts

**Key Features:**
- Event registration with validation (deadlines, capacity, team requirements)
- Payment integration with Razorpay
- Team management for team-based events
- Certificate generation and download
- Email receipts with detailed information

### **2. College Role**
**Permissions:**
- Create and manage fests
- Create and manage events within fests
- Upload fest and event images
- View registration analytics and earnings
- Approve certificates for participants
- Send emails to participants
- View detailed student enrollment lists

**Key Features:**
- Comprehensive dashboard with earnings, registrations, analytics
- Event management with detailed information (prizes, rules, requirements)
- Image upload for fests and events
- Certificate approval workflow
- Email management for participants

### **3. Admin Role**
**Permissions:**
- Approve/reject fests and events
- View platform statistics
- Manage all colleges
- Monitor system health
- Access admin dashboard

**Key Features:**
- Content moderation for fests and events
- Platform-wide analytics and statistics
- College management and oversight
- System monitoring and health checks

### **4. Public Users (No Authentication)**
**Permissions:**
- Explore approved fests and events
- View platform statistics
- Access uploaded images
- Use comprehensive filtering options

---

## 🔐 **AUTHENTICATION SYSTEM**

### **User Registration & Login**
All user types (Student, College, Admin) use the same authentication endpoints:

#### **Registration Endpoint**
```bash
POST /api/auth/register
```

**Student Registration:**
```json
{
  "email": "student@example.com",
  "password": "password123",
  "role": "Student",
  "sname": "Student Name",
  "collegeId": 1
}
```

**College Registration:**
```json
{
  "email": "college@example.com",
  "password": "password123",
  "role": "College",
  "cname": "College Name",
  "cdescription": "College Description",
  "address": "College Address",
  "contactEmail": "contact@college.com"
}
```

**Admin Registration:**
```json
{
  "email": "admin@unbound.com",
  "password": "admin123",
  "role": "Admin"
}
```

#### **Login Endpoint**
```bash
POST /api/auth/login
```

**Login Request (All Roles):**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Login Response:**
```json
{
  "token": "jwt_token_here",
  "role": "Student|College|Admin",
  "email": "user@example.com",
  "sname": "Student Name", // for students
  "cname": "College Name"  // for colleges
}
```

### **JWT Authentication**
- **Token Format**: `Authorization: Bearer {token}`
- **Token Expiration**: 24 hours
- **Role-based Access**: Different permissions for each role
- **Stateless**: No server-side session storage

### **Debugging Authentication**
Use the test endpoint to check existing users:
```bash
GET /api/users
```
This endpoint returns all registered users to help debug registration issues.

---

## 📋 **COMPLETE FEATURE LIST**

### **🔐 Authentication & Security**
- JWT-based stateless authentication
- Role-based access control (Student, College, Admin)
- Password encryption with BCrypt
- Password reset functionality with email tokens
- Token expiration (24 hours)
- CORS configuration for frontend integration
- Input validation and sanitization
- SQL injection prevention
- XSS protection

### **🎪 Fest Management**
- Create fests with detailed information (name, description, dates, location)
- Upload fest images with validation
- Fest approval workflow (admin approval required)
- Fest statistics and analytics
- Fest-event relationship management: Events can be either linked to a fest or standalone (not under any fest). The API returns both types distinctly.
- Fest filtering and search

### **🎯 Event Management**
- Create events with comprehensive details:
  - Basic info (name, description, date, location)
  - Entry fees and capacity
  - Team participation settings
  - Prize details (cash prizes, 1st/2nd/3rd prizes)
  - Location details (city, state, country)
  - Contact information (organizer details)
  - Rules and requirements
  - Registration deadlines
  - Event website and contact phone
- Event poster upload and management
- Event approval workflow
- Event statistics and analytics
- Event registration tracking

### **💰 Payment Integration**
- Razorpay payment gateway integration with direct college routing
- College payment configuration (Razorpay account, bank details)
- Payment order creation and verification
- Payment status tracking with college routing
- Receipt generation and emailing to both student and college
- Payment analytics for colleges with detailed tracking
- Automatic payment routing to college's bank account
- College payment setup and management
- **Payment Debugging**: Get all registrations to find valid registration IDs
- **Error Handling**: Clear error messages for invalid registration IDs with available options
- **JSON Validation**: Proper handling of invalid JSON requests (e.g., leading zeros in numbers)

### **📧 Email System**
- Registration confirmation emails with detailed receipts
- Payment confirmation emails to both student and college
- Event reminder emails (automated)
- Password reset emails
- College notification emails for new payments
- Email templates with dynamic content

### **💰 Payment System**
- **Direct College Routing**: Payments go directly to college's bank account
- **College Payment Setup**: Colleges configure Razorpay account and bank details
- **Automatic Payment Routing**: System automatically routes payments to correct college
- **Payment Notifications**: Both student and college receive email confirmations
- **Payment Analytics**: Detailed payment tracking and analytics for colleges
- **Secure Processing**: All payments processed through Razorpay's secure gateway
- **Payment Configuration**: Colleges can set up and manage their payment receiving details
- **Payment Tracking**: Complete audit trail for all payment transactions
- **Payment Debugging**: Get all registrations to find valid registration IDs for payments
- **Error Handling**: Clear error messages with available registration options
- **JSON Validation**: Proper handling of invalid JSON requests (e.g., leading zeros in numbers)

### **📄 Certificate System**
- PDF certificate generation for event participation
- Certificate approval workflow (college approval)
- Certificate download for students
- Certificate audit logging

### **👥 Team Management**
- Team creation for team-based events
- Team member management
- Team registration validation
- Team statistics and analytics

### **⭐ Review System**
- Student ratings and reviews for events
- Review moderation and management
- Review analytics for colleges

### **🔍 Search & Filtering**
- Fest filtering by name, college, location, mode, date range
- Event filtering by name, category, fee range, team participation, location
- Advanced search with multiple parameters
- Sorting options (date, popularity, fee)
- Public access for exploration

### **📊 Analytics & Dashboard**
- **College Dashboard:**
  - Total earnings and revenue analytics
  - Registration statistics (total, paid, unpaid)
  - Analytics by fest and date
  - Top performing events
  - Student enrollment details
  - Certificate approval management
- **Student Dashboard:**
  - Personal registration history
  - Payment status tracking
  - Certificate downloads
  - Event participation statistics
- **Admin Dashboard:**
  - Platform-wide statistics
  - Content moderation queue
  - College management
  - System health monitoring

### **📁 File Management**
- Secure image upload for fests and events
- File validation (type, size, format)
- Organized directory structure (fests/, events/)
- Automatic file cleanup
- Public access to uploaded images

### **🏥 Health Monitoring**
- Comprehensive health checks with database connectivity
- Memory usage monitoring
- System performance metrics
- Simple ping endpoint for connectivity
- Health status reporting

### **📝 Audit & Logging**
- Comprehensive action logging
- Security event tracking
- Payment event logging
- Registration event tracking
- Error monitoring and reporting

### **📚 API Documentation**
- Swagger/OpenAPI 3.0 documentation
- Interactive API testing interface
- JWT authentication integration
- Request/response examples
- Professional documentation with proper metadata

---

## 🗄️ **DATABASE SCHEMA**

### **Core Entities**

#### **User Entity**
```java
- uid (Primary Key)
- email (Unique)
- password (Encrypted)
- role (Student/College/Admin)
- createdAt (Timestamp)
```

#### **Student Entity**
```java
- sid (Primary Key)
- sname (Student Name)
- user (OneToOne with User)
- college (ManyToOne with College)
```

#### **College Entity**
```java
- cid (Primary Key)
- cname (College Name)
- cdescription (Description)
- address (Address)
- contactEmail (Contact Email)
- razorpayAccountId (Razorpay Account ID for receiving payments)
- bankAccountNumber (Bank Account Number)
- bankIfscCode (Bank IFSC Code)
- bankAccountHolderName (Bank Account Holder Name)
- user (OneToOne with User)
```

#### **Fest Entity**
```java
- fid (Primary Key)
- fname (Fest Name)
- fdescription (Description)
- startDate, endDate (Date Range)
- college (ManyToOne with College)
- approved (Admin Approval)
- active (Active Status)
- festImageUrl, festThumbnailUrl (Images)
- city, state, country (Location)
- mode (Online/Offline/Hybrid)
- website, contactPhone (Contact)
```

#### **Event Entity**
```java
- eid (Primary Key)
- ename (Event Name)
- edescription (Description)
- eventDate (Event Date)
- location (Location)
- fees (Entry Fee)
- capacity (Max Capacity)
- teamIsAllowed (Team Participation)
- category (Technical/Cultural/etc.)
- mode (Online/Offline)
- posterUrl, posterThumbnailUrl (Images)
- approved (Admin Approval)
- active (Active Status)
- cashPrize, firstPrize, secondPrize, thirdPrize (Prizes)
- city, state, country (Location)
- eventWebsite, contactPhone (Contact)
- organizerName, organizerEmail, organizerPhone (Organizer)
- rules, requirements (Event Details)
- registrationDeadline (Deadline)
- registrationOpen (Registration Status)
- fest (ManyToOne with Fest)
- college (ManyToOne with College)
```

#### **EventRegistration Entity**
```java
- rid (Primary Key)
- event (ManyToOne with Event)
- student (ManyToOne with Student)
- registrationType (Solo/Team)
- team (ManyToOne with Team)
- registrationDate (Timestamp)
- paymentStatus (Paid/Unpaid)
- certificateApproved (Certificate Status)
```

#### **Team Entity**
```java
- tid (Primary Key)
- tname (Team Name)
- event (ManyToOne with Event)
- teamLeader (ManyToOne with Student)
- maxMembers (Max Team Size)
- createdAt (Timestamp)
```

#### **TeamMembers Entity**
```java
- teamMemberId (Composite Primary Key)
- team (ManyToOne with Team)
- student (ManyToOne with Student)
- joinedAt (Timestamp)
```

#### **Payment Entity**
```java
- pid (Primary Key)
- eventRegistration (ManyToOne with EventRegistration)
- college (ManyToOne with College - tracks which college receives payment)
- razorpayOrderId (Razorpay Order ID)
- status (Payment Status - pending, paid, failed)
- amount (Payment Amount)
- currency (Currency - e.g., INR)
- createdAt (Payment Creation Timestamp)
- paymentId (Razorpay Payment ID after success)
- receiptEmail (Email for receipt)
```

#### **EventReview Entity**
```java
- reviewId (Primary Key)
- event (ManyToOne with Event)
- student (ManyToOne with Student)
- rating (Rating 1-5)
- review (Review Text)
- reviewDate (Timestamp)
```

#### **PasswordResetToken Entity**
```java
- id (Primary Key)
- token (Reset Token)
- user (ManyToOne with User)
- expiryDate (Expiration)
```

---

## 🔧 **COMPLETE SETUP INSTRUCTIONS**

### **Prerequisites**
- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- Git

### **1. Project Setup**
```bash
# Clone repository (if starting from scratch)
git clone <repository-url>
cd unbound-backend

# Or create new Spring Boot project with dependencies
```

### **2. Dependencies (pom.xml)**
```xml
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-mail</artifactId>
    </dependency>
    
    <!-- Database -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.33</version>
    </dependency>
    
    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.11.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.11.5</version>
    </dependency>
    
    <!-- Validation -->
    <dependency>
        <groupId>jakarta.validation</groupId>
        <artifactId>jakarta.validation-api</artifactId>
        <version>3.0.2</version>
    </dependency>
    <dependency>
        <groupId>org.hibernate.validator</groupId>
        <artifactId>hibernate-validator</artifactId>
        <version>7.0.5.Final</version>
    </dependency>
    
    <!-- Payment -->
    <dependency>
        <groupId>com.razorpay</groupId>
        <artifactId>razorpay-java</artifactId>
        <version>1.4.4</version>
    </dependency>
    
    <!-- PDF Generation -->
    <dependency>
        <groupId>com.github.librepdf</groupId>
        <artifactId>openpdf</artifactId>
        <version>1.3.30</version>
    </dependency>
    
    <!-- Swagger -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.0.2</version>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    
    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### **3. Application Properties (application.properties)**
```properties
# Application
spring.application.name=Unbound Platform
server.port=8081
server.servlet.context-path=/

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/unbound_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.use_sql_comments=true

# File Upload
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
spring.servlet.multipart.enabled=true

# Email
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.connectiontimeout=5000
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.writetimeout=5000

# Frontend
frontend.reset-password-url=https://localhost:3000/reset-password

# Razorpay
razorpay.keyId=your_razorpay_key_id
razorpay.keySecret=your_razorpay_secret

# Swagger
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui/index.html
springdoc.packages-to-scan=com.unbound.backend.controller
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha

# Logging
logging.level.com.unbound.backend=DEBUG
logging.level.org.springframework.security=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# Security
jwt.secret=your_very_long_secret_key_for_jwt_signing_which_should_be_secure_and_at_least_256_bits_long
jwt.expiration=86400000

# Performance
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.properties.hibernate.jdbc.batch_versioned_data=true
```

### **4. Database Setup**
```sql
-- Create database
CREATE DATABASE unbound_db;
USE unbound_db;

-- Tables will be created automatically by Hibernate
-- Run the application and Hibernate will create all tables
```

### **5. Project Structure**
```
src/main/java/com/unbound/backend/
├── config/
│   ├── SecurityConfig.java
│   ├── SwaggerConfig.java
│   ├── WebConfig.java
│   ├── GlobalExceptionHandler.java
│   ├── JwtAuthenticationFilter.java
│   └── LoggingConfig.java
├── controller/
│   ├── AuthController.java
│   ├── FestController.java
│   ├── EventController.java
│   ├── StudentEventController.java
│   ├── CollegeDashboardController.java
│   ├── AdminController.java
│   ├── ExploreController.java
│   ├── HealthController.java
│   ├── PaymentController.java
│   ├── TeamController.java
│   └── TestController.java
├── dto/
│   ├── RegisterRequest.java
│   ├── LoginRequest.java
│   ├── AuthResponse.java
│   ├── ForgotPasswordRequest.java
│   ├── ResetPasswordRequest.java
│   ├── FestRequest.java
│   ├── FestResponse.java
│   ├── EventRequest.java
│   ├── EventResponse.java
│   ├── EventRegistrationRequest.java
│   └── RegistrationResponse.java
├── entity/
│   ├── User.java
│   ├── Student.java
│   ├── College.java
│   ├── Fest.java
│   ├── Event.java
│   ├── EventRegistration.java
│   ├── Team.java
│   ├── TeamMembers.java
│   ├── Payment.java
│   ├── EventReview.java
│   └── PasswordResetToken.java
├── repository/
│   ├── UserRepository.java
│   ├── StudentRepository.java
│   ├── CollegeRepository.java
│   ├── FestRepository.java
│   ├── EventRepository.java
│   ├── EventRegistrationRepository.java
│   ├── TeamRepository.java
│   ├── TeamMembersRepository.java
│   ├── PaymentRepository.java
│   ├── EventReviewRepository.java
│   └── PasswordResetTokenRepository.java
├── service/
│   ├── AuthService.java
│   ├── JwtService.java
│   ├── EmailService.java
│   ├── PaymentService.java
│   ├── CertificateService.java
│   ├── FileStorageService.java
│   ├── AuditService.java
│   ├── StudentDashboardService.java
│   ├── CollegeDashboardService.java
│   ├── EventReminderService.java
│   └── PasswordService.java
└── UnboundPlatformApplication.java
```

### **6. Build and Run**
```bash
# Clean and build
mvn clean install

# Run application
mvn spring-boot:run

# Or using jar
java -jar target/unbound-platform-1.0.0.jar
```

### **7. Access Points**
- **Application**: http://localhost:8081
- **Swagger UI**: http://localhost:8081/swagger-ui/index.html
- **API Docs**: http://localhost:8081/v3/api-docs
- **Health Check**: http://localhost:8081/api/health

---

## 🔐 **SECURITY IMPLEMENTATION**

### **JWT Authentication**
- Token-based stateless authentication
- 24-hour token expiration
- Role-based access control
- Secure token generation and validation

### **Password Security**
- BCrypt password hashing
- Password reset with email tokens
- Secure token storage and validation

### **Input Validation**
- Comprehensive validation on all DTOs
- SQL injection prevention
- XSS protection
- File upload validation

### **CORS Configuration**
- Configured for frontend integration
- Secure cross-origin requests
- Proper header management

---

## 📊 **API ENDPOINTS REFERENCE**

### **Public Access (No Authentication)**
- `GET /api/explore/fests` - Explore fests with filtering
- `GET /api/explore/events` - Explore events with filtering
- `GET /api/explore/stats` - Platform statistics
- `GET /api/health` - System health check
- `GET /api/health/ping` - Simple connectivity check
- `GET /api/users` - Get all users (for debugging)

### **Authentication**
- `POST /api/auth/register` - User registration (Student/College/Admin)
- `POST /api/auth/login` - User login (Student/College/Admin)
- `POST /api/auth/forgot-password` - Password reset request
- `POST /api/auth/reset-password` - Password reset

### **Fest Management (College Access)**
- `GET /api/fests` - List college fests
- `POST /api/fests` - Create fest
- `PUT /api/fests/{fid}` - Update fest
- `DELETE /api/fests/{fid}` - Delete fest
- `POST /api/fests/{fid}/image` - Upload fest image
- `GET /api/fests/{fid}/events` - Get fest events (returns both fest-linked and standalone events for the college in separate lists)

### **Event Management (College Access)**
- `GET /api/events` - List college events
- `POST /api/events` - Create event
- `PUT /api/events/{eid}` - Update event
- `DELETE /api/events/{eid}` - Delete event
- `POST /api/events/{eid}/poster` - Upload event poster
- `POST /api/events/{eid}/poster/approve` - Approve poster
- `POST /api/events/{eid}/poster/reject` - Reject poster
- `DELETE /api/events/{eid}/poster` - Delete poster
- `GET /api/events/{eid}/poster/audit-logs` - Poster audit logs

### **Student Operations (Student Access)**
- `POST /api/student/events/register` - Register for event
- `GET /api/student/events/my` - My registrations
- `GET /api/student/dashboard/stats` - Student dashboard stats
- `GET /api/student/events/{eventId}/certificate` - Download certificate

### **College Dashboard (College Access)**
- `GET /api/college/dashboard/earnings` - Earnings analytics
- `GET /api/college/dashboard/registrations` - Registration analytics
- `GET /api/college/dashboard/analytics/by-fest` - Analytics by fest
- `GET /api/college/dashboard/analytics/by-date` - Analytics by date
- `GET /api/college/dashboard/analytics/top-events` - Top events
- `GET /api/college/dashboard/events/{eventId}/registrations` - Event registrations
- `POST /api/college/dashboard/events/{eventId}/registrations/{registrationId}/approve-certificate` - Approve certificate
- `POST /api/college/dashboard/events/{eventId}/registrations/approve-all-certificates` - Approve all certificates
- `POST /api/college/dashboard/events/{eventId}/registrations/approve-certificates` - Approve certificate list

### **College Management (College Access)**
- `POST /api/college/payment-config` - Configure college payment settings
- `GET /api/college/payment-config` - Get college payment settings

### **Admin Management (Admin Access)**
- `GET /api/admin/fests/pending` - Get pending fests
- `GET /api/admin/events/pending` - Get pending events
- `POST /api/admin/fests/{festId}/approve` - Approve fest
- `POST /api/admin/fests/{festId}/reject` - Reject fest
- `POST /api/admin/events/{eventId}/approve` - Approve event
- `POST /api/admin/events/{eventId}/reject` - Reject event
- `GET /api/admin/dashboard/stats` - Admin dashboard stats
- `GET /api/admin/colleges` - Get all colleges

### **Payment & Team Management**
- `GET /api/payments/registrations` - Get all registrations with details (for finding valid registration IDs)
- `POST /api/payments/create-order` - Create payment order for a specific registration
- `POST /api/payments/verify` - Verify payment (typically called by Razorpay webhook)
- `POST /api/teams` - Create team
- `GET /api/teams` - List teams
- `PUT /api/teams/{tid}` - Update team
- `DELETE /api/teams/{tid}` - Delete team

### **Test & Debug Endpoints**
- `GET /api/protected` - Test protected endpoint
- `GET /api/users` - Get all users (for debugging registration issues)

---

## 🧪 **TESTING STRATEGY**

### **API Testing**
- Use Postman collection provided
- Test all endpoints with different user roles
- Verify authentication and authorization
- Test error scenarios and edge cases

### **Database Testing**
- Verify all entities and relationships
- Test data integrity constraints
- Validate foreign key relationships
- Test transaction rollbacks

### **Security Testing**
- Test JWT token validation
- Verify role-based access control
- Test input validation and sanitization
- Verify file upload security

### **Integration Testing**
- Test payment gateway integration
- Verify email sending functionality
- Test file upload and storage
- Validate certificate generation

---

## 🚀 **DEPLOYMENT CONSIDERATIONS**

### **Production Environment**
- Use production database (MySQL/PostgreSQL)
- Configure proper email settings
- Set up SSL/TLS certificates
- Configure proper logging levels
- Set up monitoring and health checks

### **Docker Support**
- Create Dockerfile for containerization
- Use environment variables for configuration
- Set up Docker Compose for local development
- Configure proper networking

### **Environment Variables**
- Database credentials
- Email settings
- Payment gateway keys
- JWT secret keys
- File storage paths

### **Monitoring & Logging**
- Application performance monitoring
- Database performance monitoring
- Error tracking and alerting
- User activity logging
- Security event monitoring

---

## 📈 **PERFORMANCE OPTIMIZATION**

### **Database Optimization**
- Proper indexing on frequently queried columns
- Query optimization and caching
- Connection pooling configuration
- Batch processing for bulk operations

### **Application Optimization**
- Response caching where appropriate
- File compression and optimization
- Memory usage monitoring
- Garbage collection tuning

### **Security Optimization**
- Rate limiting for API endpoints
- Request size limits
- Session management
- Audit logging optimization

---

## 🔧 **MAINTENANCE & UPDATES**

### **Regular Maintenance**
- Database backups and maintenance
- Log rotation and cleanup
- File storage cleanup
- Security updates and patches

### **Feature Updates**
- Add new event categories
- Implement new payment methods
- Add advanced analytics
- Enhance notification system

### **Monitoring & Alerts**
- System health monitoring
- Error rate monitoring
- Performance metrics tracking
- Security incident alerting

---

## 📞 **SUPPORT & CONTACT**

### **Technical Support**
- **Email**: support@unboundplatform.com
- **Documentation**: https://docs.unboundplatform.com
- **API Docs**: http://localhost:8081/swagger-ui/index.html

### **Development Team**
- **Lead Developer**: [Your Name]
- **Backend Team**: [Team Members]
- **Frontend Team**: [Team Members]
- **DevOps Team**: [Team Members]

---

## 📄 **LICENSE**

This project is licensed under the MIT License - see the LICENSE file for details.

---

## 🎯 **PROJECT STATUS**

**Current Version**: 1.0.0  
**Last Updated**: July 2025 
**Status**: Production Ready  
**Features**: Complete  
**Documentation**: Comprehensive  
**Testing**: Comprehensive  
**Security**: Implemented  
**Performance**: Optimized  

---

**This README serves as a complete project prompt and can be used to recreate the entire Unbound Platform from scratch if needed. All implementation details, configurations, and features are documented comprehensively.**