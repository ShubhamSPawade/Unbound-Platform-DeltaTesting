# Unbound Platform - Backend API

A comprehensive fest and event management system for colleges and students, built with Spring Boot.

## 🚀 **Features**

### **Core Functionality**
- **User Management**: Student, College, and Admin roles with JWT authentication
- **Fest Management**: Create, manage, and approve fests with detailed information
- **Event Management**: Events can be standalone (not under any fest) or linked to a fest. The API returns both types distinctly for clarity.
- **Payment Integration**: Razorpay payment gateway for secure transactions with direct college routing
- **Email Notifications**: Automated email receipts and reminders
- **Certificate Generation**: PDF certificates for event participation
- **File Management**: Secure image upload and storage system

### **Advanced Features**
- **Admin Approval System**: Content moderation for fests and events
- **College Dashboard**: Analytics, earnings, and student management
- **Student Dashboard**: Event exploration, registration, and payment tracking
- **Team Management**: Support for team-based event registrations
- **Review System**: Student ratings and reviews for events
- **Search & Filtering**: Advanced event discovery with multiple filters
- **Audit Logging**: Comprehensive action tracking for security
- **Health Monitoring**: System health checks and performance metrics
- **Public Access**: Non-authenticated users can explore fests and events
- **College Payment Configuration**: Colleges can set up their payment receiving details

### **Payment System**
- **Direct College Routing**: Payments go directly to college's bank account
- **College Payment Setup**: Colleges configure Razorpay account and bank details
- **Automatic Payment Routing**: System automatically routes payments to correct college
- **Payment Notifications**: Both student and college receive email confirmations
- **Payment Analytics**: Detailed payment tracking and analytics for colleges

### **Public Access Features**
- **Fest Exploration**: Browse approved fests with filtering by name, college, location, mode
- **Event Discovery**: Search events with comprehensive filters (category, fee range, team participation)
- **Statistics**: View platform statistics without authentication
- **Image Access**: View uploaded fest and event images publicly
- **Enhanced Experience**: Login for additional features like registration and personalization

### **Technical Features**
- **RESTful API**: Well-structured REST endpoints
- **JWT Authentication**: Secure stateless authentication
- **Data Validation**: Comprehensive input validation
- **Error Handling**: Detailed error responses and logging
- **File Upload**: Secure image upload with validation
- **Database Optimization**: Efficient queries and indexing
- **API Documentation**: Swagger/OpenAPI documentation
- **CORS Support**: Cross-origin resource sharing
- **Health Checks**: System monitoring and diagnostics

## 🏗️ **Architecture**

### **Layered Architecture**
```
┌─────────────────┐
│   Controllers   │ ← REST API endpoints
├─────────────────┤
│    Services     │ ← Business logic
├─────────────────┤
│  Repositories   │ ← Data access
├─────────────────┤
│    Entities     │ ← Data models
└─────────────────┘
```

### **Key Components**
- **Controllers**: Handle HTTP requests and responses
- **Services**: Business logic and external integrations
- **Repositories**: Data access layer with Spring Data JPA
- **Entities**: JPA entities for database mapping
- **DTOs**: Data transfer objects for API requests/responses
- **Config**: Security, CORS, and application configuration

## 🔐 **Authentication System**

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

## 📋 **API Endpoints & Flows**

### **Public Access (No Authentication Required)**
- `GET /api/explore/fests` - Explore fests with filtering options
- `GET /api/explore/events` - Explore events with comprehensive filtering
- `GET /api/explore/stats` - Get public statistics about platform usage
- `GET /api/health` - System health check
- `GET /api/health/ping` - Simple connectivity check
- `GET /api/users` - Get all users (for debugging)

### **Authentication**
- `POST /api/auth/register` - User registration (Student/College/Admin)
- `POST /api/auth/login` - User login (Student/College/Admin)
- `POST /api/auth/forgot-password` - Password reset request
- `POST /api/auth/reset-password` - Password reset

### **College Management**
- `POST /api/college/payment-config` - Configure college payment settings
- `GET /api/college/payment-config` - Get college payment settings

### **Fest Management**
- `GET /api/fests` - List fests (college view)
- `POST /api/fests` - Create fest
- `PUT /api/fests/{fid}` - Update fest
- `DELETE /api/fests/{fid}` - Delete fest
- `POST /api/fests/{fid}/image` - Upload fest image
- `GET /api/fests/{fid}/events` - Get fest events (returns both fest-linked and standalone events for the college in separate lists)

### **Event Management**
- `GET /api/events` - List events (college view)
- `POST /api/events` - Create event
- `PUT /api/events/{eid}` - Update event
- `DELETE /api/events/{eid}` - Delete event
- `POST /api/events/{eid}/poster` - Upload event poster
- `POST /api/events/{eid}/poster/approve` - Approve poster
- `POST /api/events/{eid}/poster/reject` - Reject poster
- `DELETE /api/events/{eid}/poster` - Delete poster
- `GET /api/events/{eid}/poster/audit-logs` - Poster audit logs

### **Student Operations**
- `GET /api/explore/fests` - Explore fests
- `GET /api/explore/events` - Explore events
- `POST /api/student/events/{eid}/register` - Register for event
- `GET /api/student/dashboard` - Student dashboard
- `GET /api/student/certificates/{eid}` - Download certificate

### **College Dashboard**
- `GET /api/college/dashboard/earnings` - Earnings analytics
- `GET /api/college/dashboard/registrations` - Registration analytics
- `GET /api/college/dashboard/analytics` - Detailed analytics
- `GET /api/college/dashboard/top-events` - Top performing events
- `GET /api/college/dashboard/events/{eventId}/registrations` - Event registrations
- `POST /api/college/dashboard/certificates/{registrationId}/approve` - Approve certificate
- `POST /api/college/dashboard/certificates/approve-all` - Approve all certificates
- `POST /api/college/dashboard/certificates/approve-list` - Approve certificate list

### **Admin Management**
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

## 💰 **Payment Flow**

### **How Payments Work**
1. **College Setup**: College configures their Razorpay account and bank details
2. **Student Registration**: Student registers for an event and initiates payment
3. **Payment Processing**: Razorpay processes the payment
4. **Direct Transfer**: Money goes directly to college's bank account
5. **Notifications**: Both student and college receive email confirmations

### **Payment Debugging & Error Handling**
- **Registration Validation**: Use `GET /api/payments/registrations` to see all available registrations
- **Error Messages**: Clear, user-friendly error messages for invalid registration IDs
- **Available Registrations**: When a registration is not found, the API returns a list of all available registrations
- **JSON Validation**: Proper error handling for invalid JSON requests (e.g., leading zeros in numbers)

### **College Payment Configuration**
Colleges must set up their payment details to receive payments:
- **Razorpay Account ID**: For direct payment routing
- **Bank Account Number**: For verification
- **Bank IFSC Code**: For bank transfers
- **Bank Account Holder Name**: For verification
- **Contact Email**: For payment notifications

### **Payment Routing**
```
Student → Razorpay → College's Bank Account
```
- Payments are automatically routed to the college that organized the event
- No platform fees or delays
- Secure processing through Razorpay
- Complete audit trail maintained

## 📊 **DTOs (Data Transfer Objects)**

### **Authentication DTOs**
- `RegisterRequest` - User registration data
- `LoginRequest` - Login credentials
- `AuthResponse` - Authentication response
- `ForgotPasswordRequest` - Password reset request
- `ResetPasswordRequest` - Password reset data

### **Event & Fest DTOs**
- `EventRequest` - Event creation/update data
- `EventResponse` - Detailed event information
- `FestRequest` - Fest creation/update data
- `FestResponse` - Detailed fest information
- `EventRegistrationRequest` - Event registration data
- `RegistrationResponse` - Registration receipt

### **Payment DTOs**
- `CollegePaymentConfigRequest` - College payment configuration

## 📚 **API Documentation**

### **Swagger/OpenAPI Documentation**
- **Swagger UI**: Available at `http://localhost:8081/swagger-ui/index.html`
- **OpenAPI Specification**: Available at `http://localhost:8081/v3/api-docs`
- **Interactive Testing**: Test all endpoints directly from the Swagger UI
- **Authentication**: JWT Bearer token authentication supported
- **Request/Response Examples**: Detailed examples for all endpoints

### **API Categories**
- **Public Exploration APIs**: No authentication required
- **Authentication APIs**: User registration and login
- **College Management APIs**: College payment configuration
- **Fest Management APIs**: College access required
- **Event Management APIs**: College access required
- **Student Event APIs**: Student access required
- **College Dashboard APIs**: College access required
- **Admin Management APIs**: Admin access required
- **Health Check APIs**: System monitoring
- **Test & Debug APIs**: For development and debugging

### **Authentication**
- **JWT Bearer Token**: Required for protected endpoints
- **Role-based Access**: Student, College, Admin roles
- **Token Format**: `Authorization: Bearer {token}`
- **Token Expiration**: 24 hours

### **Testing with Swagger**
1. **Access Swagger UI**: Navigate to `http://localhost:8081/swagger-ui/index.html`
2. **Authenticate**: Click "Authorize" and enter your JWT token
3. **Test Endpoints**: Use the interactive interface to test all APIs
4. **View Documentation**: Detailed descriptions and examples for each endpoint

## 🔧 **Setup & Configuration**

### **Prerequisites**
- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+

### **Environment Setup**
1. **Clone Repository**
   ```bash
   git clone <repository-url>
   cd unbound-backend
   ```

2. **Configure Database**
   ```sql
   CREATE DATABASE unbound_db;
   ```

3. **Update application.properties**
   ```properties
   # Database
   spring.datasource.url=jdbc:mysql://localhost:3306/unbound_db
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   
   # Razorpay (for payments)
   razorpay.keyId=your_razorpay_key_id
   razorpay.keySecret=your_razorpay_secret
   
   # Email (for notifications)
   spring.mail.host=smtp.gmail.com
   spring.mail.username=your_email@gmail.com
   spring.mail.password=your_app_password
   ```

4. **Build and Run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

### **Access Points**
- **Application**: http://localhost:8081
- **Swagger UI**: http://localhost:8081/swagger-ui/index.html
- **Health Check**: http://localhost:8081/api/health

## 💡 **Usage Examples**

### **College Payment Setup**
```bash
POST /api/college/payment-config
{
  "razorpayAccountId": "acc_1234567890",
  "bankAccountNumber": "1234567890",
  "bankIfscCode": "SBIN0001234",
  "bankAccountHolderName": "ABC College",
  "contactEmail": "college@example.com"
}
```

### **Student Payment Flow**
1. Student registers for event
2. System creates Razorpay order with college's account
3. Student pays through Razorpay
4. Money goes directly to college's account
5. Both receive email confirmations

### **Payment Analytics**
Colleges can view their payment analytics:
- Total earnings and revenue
- Payment statistics by fest and date
- Student enrollment details
- Payment status tracking

## 🔒 **Security Features**

- **JWT Authentication**: Secure token-based authentication
- **Role-based Access**: Different permissions for different user types
- **Input Validation**: Comprehensive validation on all inputs
- **Payment Security**: Secure payment processing through Razorpay
- **File Upload Security**: Validated and secure file uploads
- **CORS Configuration**: Secure cross-origin requests

## 📞 **Support**

For technical support or questions about the payment system, please contact the development team.

---

**The Unbound Platform provides a complete solution for fest and event management with secure payment processing, comprehensive analytics, and user-friendly interfaces.**