# Unbound Platform - Detailed API Documentation

## 📋 **Complete API Endpoints Reference**

This document provides detailed information about all API endpoints, including headers, request bodies, and response formats.

---

## 🔐 **Authentication Endpoints**

### **1. Register User**
```http
POST /api/auth/register
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "role": "Student|College|Admin",
  "sname": "Student Name",           // Required for Student role
  "collegeId": 1,                   // Required for Student role
  "cname": "College Name",          // Required for College role
  "cdescription": "Description",     // Required for College role
  "address": "Address",             // Required for College role
  "contactEmail": "contact@college.com" // Required for College role
}
```

**Response:**
```json
{
  "token": "jwt_token_here",
  "role": "Student|College|Admin",
  "email": "user@example.com",
  "sname": "Student Name",          // For students
  "cname": "College Name"           // For colleges
}
```

### **2. Login User**
```http
POST /api/auth/login
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "jwt_token_here",
  "role": "Student|College|Admin",
  "email": "user@example.com",
  "sname": "Student Name",          // For students
  "cname": "College Name"           // For colleges
}
```

### **3. Forgot Password**
```http
POST /api/auth/forgot-password
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "user@example.com"
}
```

**Response:**
```json
{
  "message": "Password reset link sent to your email"
}
```

### **4. Reset Password**
```http
POST /api/auth/reset-password
Content-Type: application/json
```

**Request Body:**
```json
{
  "token": "reset_token_from_email",
  "newPassword": "newpassword123"
}
```

**Response:**
```json
{
  "message": "Password reset successfully"
}
```

---

## 🏫 **College Management Endpoints**

### **5. Configure College Payment Settings**
```http
POST /api/college/payment-config
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "razorpayAccountId": "acc_1234567890",
  "bankAccountNumber": "1234567890",
  "bankIfscCode": "SBIN0001234",
  "bankAccountHolderName": "College Name",
  "contactEmail": "college@example.com"
}
```

**Response:**
```json
{
  "message": "Payment settings configured successfully"
}
```

### **6. Get College Payment Settings**
```http
GET /api/college/payment-config
Authorization: Bearer {token}
```

**Response:**
```json
{
  "razorpayAccountId": "acc_1234567890",
  "bankAccountNumber": "1234567890",
  "bankIfscCode": "SBIN0001234",
  "bankAccountHolderName": "College Name",
  "contactEmail": "college@example.com"
}
```

---

## 🎪 **Fest Management Endpoints**

### **7. Create Fest**
```http
POST /api/fests
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "fname": "TechFest 2024",
  "fdescription": "Annual technical festival",
  "startDate": "2024-03-15",
  "endDate": "2024-03-17",
  "city": "Mumbai",
  "state": "Maharashtra",
  "country": "India",
  "mode": "Offline",
  "website": "https://techfest2024.com",
  "contactPhone": "+91-9876543210"
}
```

**Response:**
```json
{
  "fid": 1,
  "fname": "TechFest 2024",
  "fdescription": "Annual technical festival",
  "startDate": "2024-03-15",
  "endDate": "2024-03-17",
  "city": "Mumbai",
  "state": "Maharashtra",
  "country": "India",
  "mode": "Offline",
  "website": "https://techfest2024.com",
  "contactPhone": "+91-9876543210",
  "approved": false,
  "active": true,
  "festImageUrl": null,
  "festThumbnailUrl": null,
  "collegeName": "College One"
}
```

### **8. Upload Fest Image**
```http
POST /api/fests/{fid}/image
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

**Form Data:**
- `image`: File (image file)

**Response:**
```json
{
  "message": "Fest image uploaded successfully",
  "imageUrl": "/uploads/fests/fest_image.jpg",
  "thumbnailUrl": "/uploads/fests/fest_thumbnail.jpg"
}
```

### **9. List Fests**
```http
GET /api/fests
Authorization: Bearer {token}
```

**Response:**
```json
[
  {
    "fid": 1,
    "fname": "TechFest 2024",
    "fdescription": "Annual technical festival",
    "startDate": "2024-03-15",
    "endDate": "2024-03-17",
    "city": "Mumbai",
    "state": "Maharashtra",
    "country": "India",
    "mode": "Offline",
    "website": "https://techfest2024.com",
    "contactPhone": "+91-9876543210",
    "approved": true,
    "active": true,
    "festImageUrl": "/uploads/fests/fest_image.jpg",
    "festThumbnailUrl": "/uploads/fests/fest_thumbnail.jpg",
    "collegeName": "College One"
  }
]
```

### **10. Update Fest**
```http
PUT /api/fests/{fid}
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "fname": "Tech Fest Updated",
  "fdescription": "Updated description",
  "startDate": "2024-08-01",
  "endDate": "2024-08-04"
}
```

**Response:**
```json
{
  "message": "Fest updated successfully"
}
```

### **11. Delete Fest**
```http
DELETE /api/fests/{fid}
Authorization: Bearer {token}
```

**Response:**
```json
{
  "message": "Fest deleted successfully"
}
```

### **12. Get Fest Events**
```http
GET /api/fests/{fid}/events
Authorization: Bearer {token}
```

**Response:**
```json
{
  "festId": 1,
  "festName": "TechFest 2024",
  "totalFestEvents": 2,
  "totalStandaloneEvents": 1,
  "festEvents": [
    {
      "eid": 1,
      "ename": "Hackathon 2024",
      "edescription": "24-hour coding competition",
      "eventDate": "2024-03-16",
      "fees": 500,
      "location": "Main Auditorium",
      "capacity": 100,
      "teamIsAllowed": true,
      "category": "Technical",
      "mode": "Offline",
      "posterUrl": "/uploads/events/poster.jpg",
      "posterThumbnailUrl": "/uploads/events/thumbnail.jpg",
      "approved": true,
      "active": true,
      "cashPrize": "₹50,000",
      "firstPrize": "₹25,000 + Trophy",
      "secondPrize": "₹15,000 + Medal",
      "thirdPrize": "₹10,000 + Certificate",
      "city": "Mumbai",
      "state": "Maharashtra",
      "country": "India",
      "eventWebsite": "https://hackathon2024.com",
      "contactPhone": "+91-9876543210",
      "organizerName": "Prof. John Doe",
      "organizerEmail": "john.doe@college.edu",
      "organizerPhone": "+91-9876543211",
      "rules": "1. Teams of 2-4 members\n2. 24-hour time limit",
      "requirements": "Laptop, College ID, Basic programming knowledge",
      "registrationDeadline": "2024-03-10",
      "registrationOpen": true,
      "collegeName": "College One",
      "collegeEmail": "college1@example.com",
      "festName": "TechFest 2024",
      "registrationCount": 25,
      "daysLeft": 5
    }
  ],
  "standaloneEvents": [
    {
      "eid": 3,
      "ename": "Solo Coding Challenge",
      "edescription": "Individual coding contest",
      "eventDate": "2024-12-10",
      "fees": 100,
      "location": "Main Hall",
      "capacity": 50,
      "teamIsAllowed": false,
      "category": "Technical",
      "mode": "Offline",
      "posterUrl": null,
      "posterThumbnailUrl": null,
      "approved": true,
      "active": true,
      "cashPrize": "₹10,000",
      "firstPrize": "₹5,000",
      "secondPrize": "₹3,000",
      "thirdPrize": "₹2,000",
      "city": "Mumbai",
      "state": "Maharashtra",
      "country": "India",
      "eventWebsite": "https://codingchallenge.com",
      "contactPhone": "+91-9876543210",
      "organizerName": "Prof. Jane Doe",
      "organizerEmail": "jane.doe@college.edu",
      "organizerPhone": "+91-9876543211",
      "rules": "1. Individual participation only\n2. No plagiarism",
      "requirements": "Laptop, College ID",
      "registrationDeadline": "2024-12-05",
      "registrationOpen": true,
      "collegeName": "College One",
      "collegeEmail": "college1@example.com",
      "festName": null,
      "registrationCount": 10,
      "daysLeft": 15
    }
  ]
}
```

---

## 🎯 **Event Management Endpoints**

### **13. Create Event**
```http
POST /api/events
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "ename": "Hackathon 2024",
  "edescription": "24-hour coding competition with exciting prizes",
  "eventDate": "2024-12-01",
  "fees": 500,
  "location": "Main Auditorium",
  "capacity": 100,
  "teamIsAllowed": true,
  "category": "Technical",
  "mode": "Offline",
  "fid": 1,                         // Optional: Link to fest
  "cashPrize": "₹50,000",
  "firstPrize": "₹25,000 + Trophy",
  "secondPrize": "₹15,000 + Medal",
  "thirdPrize": "₹10,000 + Certificate",
  "city": "Mumbai",
  "state": "Maharashtra",
  "country": "India",
  "eventWebsite": "https://hackathon2024.com",
  "contactPhone": "+91-9876543210",
  "organizerName": "Prof. John Doe",
  "organizerEmail": "john.doe@college.edu",
  "organizerPhone": "+91-9876543211",
  "rules": "1. Teams of 2-4 members\n2. 24-hour time limit\n3. No external help allowed\n4. Original code only",
  "requirements": "Laptop, College ID, Basic programming knowledge",
  "registrationDeadline": "2024-11-25"
}
```

**Response:**
```json
{
  "eid": 1,
  "ename": "Hackathon 2024",
  "edescription": "24-hour coding competition with exciting prizes",
  "eventDate": "2024-12-01",
  "fees": 500,
  "location": "Main Auditorium",
  "capacity": 100,
  "teamIsAllowed": true,
  "category": "Technical",
  "mode": "Offline",
  "posterUrl": null,
  "posterThumbnailUrl": null,
  "approved": false,
  "active": true,
  "cashPrize": "₹50,000",
  "firstPrize": "₹25,000 + Trophy",
  "secondPrize": "₹15,000 + Medal",
  "thirdPrize": "₹10,000 + Certificate",
  "city": "Mumbai",
  "state": "Maharashtra",
  "country": "India",
  "eventWebsite": "https://hackathon2024.com",
  "contactPhone": "+91-9876543210",
  "organizerName": "Prof. John Doe",
  "organizerEmail": "john.doe@college.edu",
  "organizerPhone": "+91-9876543211",
  "rules": "1. Teams of 2-4 members\n2. 24-hour time limit\n3. No external help allowed\n4. Original code only",
  "requirements": "Laptop, College ID, Basic programming knowledge",
  "registrationDeadline": "2024-11-25",
  "registrationOpen": true,
  "collegeName": "College One",
  "collegeEmail": "college1@example.com",
  "festName": "TechFest 2024",
  "registrationCount": 0,
  "daysLeft": 30
}
```

### **14. Upload Event Poster**
```http
POST /api/events/{eid}/poster
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

**Form Data:**
- `file`: File (image file)

**Response:**
```json
{
  "message": "Event poster uploaded successfully",
  "posterUrl": "/uploads/events/poster.jpg",
  "posterThumbnailUrl": "/uploads/events/thumbnail.jpg"
}
```

### **15. List Events**
```http
GET /api/events
Authorization: Bearer {token}
```

**Response:**
```json
[
  {
    "eid": 1,
    "ename": "Hackathon 2024",
    "edescription": "24-hour coding competition with exciting prizes",
    "eventDate": "2024-12-01",
    "fees": 500,
    "location": "Main Auditorium",
    "capacity": 100,
    "teamIsAllowed": true,
    "category": "Technical",
    "mode": "Offline",
    "posterUrl": "/uploads/events/poster.jpg",
    "posterThumbnailUrl": "/uploads/events/thumbnail.jpg",
    "approved": true,
    "active": true,
    "cashPrize": "₹50,000",
    "firstPrize": "₹25,000 + Trophy",
    "secondPrize": "₹15,000 + Medal",
    "thirdPrize": "₹10,000 + Certificate",
    "city": "Mumbai",
    "state": "Maharashtra",
    "country": "India",
    "eventWebsite": "https://hackathon2024.com",
    "contactPhone": "+91-9876543210",
    "organizerName": "Prof. John Doe",
    "organizerEmail": "john.doe@college.edu",
    "organizerPhone": "+91-9876543211",
    "rules": "1. Teams of 2-4 members\n2. 24-hour time limit\n3. No external help allowed\n4. Original code only",
    "requirements": "Laptop, College ID, Basic programming knowledge",
    "registrationDeadline": "2024-11-25",
    "registrationOpen": true,
    "collegeName": "College One",
    "collegeEmail": "college1@example.com",
    "festName": "TechFest 2024",
    "registrationCount": 25,
    "daysLeft": 30
  }
]
```

### **16. Update Event**
```http
PUT /api/events/{eid}
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "ename": "Coding Challenge Updated",
  "edescription": "Updated description",
  "eventDate": "2024-12-02",
  "fees": 150,
  "location": "Main Hall",
  "capacity": 60,
  "teamIsAllowed": false,
  "fid": 1,
  "registrationDeadline": "2024-11-28"
}
```

**Response:**
```json
{
  "message": "Event updated successfully"
}
```

### **17. Delete Event**
```http
DELETE /api/events/{eid}
Authorization: Bearer {token}
```

**Response:**
```json
{
  "message": "Event deleted successfully"
}
```

### **18. Delete Event Poster**
```http
DELETE /api/events/{eid}/poster
Authorization: Bearer {token}
```

**Response:**
```json
{
  "message": "Event poster deleted successfully"
}
```

### **19. Get Event Stats (Public)**
```http
GET /api/events/{eventId}/stats
```

**Response:**
```json
{
  "registrationCount": 25,
  "daysLeft": 5,
  "registrationDeadline": "2024-11-25",
  "eventDate": "2024-12-01"
}
```

---

## 👨‍🎓 **Student Event Operations**

### **20. Register for Event**
```http
POST /api/student/events/register
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body (Solo Registration):**
```json
{
  "eventId": 1,
  "registrationType": "solo",
  "teamName": null,
  "teamId": null
}
```

**Request Body (Team Registration):**
```json
{
  "eventId": 1,
  "registrationType": "team",
  "teamName": "Code Warriors",
  "teamId": null
}
```

**Response:**
```json
{
  "registrationId": 1,
  "eventName": "Hackathon 2024",
  "eventDate": "2024-12-01",
  "eventLocation": "Main Auditorium",
  "fees": 500,
  "registrationType": "solo",
  "teamName": null,
  "registrationStatus": "registered",
  "paymentStatus": "pending",
  "registrationDateTime": "2024-11-20T10:30:00",
  "studentName": "John Doe",
  "studentEmail": "john@example.com",
  "collegeName": "College One",
  "festName": "TechFest 2024",
  "cashPrize": "₹50,000",
  "firstPrize": "₹25,000 + Trophy",
  "secondPrize": "₹15,000 + Medal",
  "thirdPrize": "₹10,000 + Certificate",
  "registrationDeadline": "2024-11-25",
  "daysLeft": 5,
  "receiptNumber": "RCP1703123456789",
  "message": "Registration successful! Check your email for receipt.",
  "success": true
}
```

### **21. My Registrations**
```http
GET /api/student/events/my
Authorization: Bearer {token}
```

**Response:**
```json
[
  {
    "registrationId": 1,
    "eventId": 1,
    "eventName": "Hackathon 2024",
    "festName": "TechFest 2024",
    "eventDate": "2024-12-01",
    "location": "Main Auditorium",
    "registrationStatus": "registered",
    "paymentStatus": "pending",
    "fees": 500,
    "teamName": null,
    "registrationDate": "2024-11-20T10:30:00",
    "daysLeft": 5,
    "isRegistered": true,
    "registrationStatus": "registered",
    "paymentStatus": "pending"
  }
]
```

### **22. Student Dashboard Stats**
```http
GET /api/student/events/dashboard/stats
Authorization: Bearer {token}
```

**Response:**
```json
{
  "totalRegistrations": 5,
  "paidRegistrations": 3,
  "pendingPayments": 2,
  "totalSpent": 1500,
  "certificatesDownloaded": 2,
  "reviewsSubmitted": 3,
  "recentRegistrations": [
    {
      "registrationId": 1,
      "eventName": "Hackathon 2024",
      "eventDate": "2024-12-01",
      "paymentStatus": "paid",
      "registrationDate": "2024-11-20T10:30:00"
    }
  ]
}
```

### **23. Download Event Certificate**
```http
GET /api/student/events/{eventId}/certificate
Authorization: Bearer {token}
```

**Response:** PDF file download

---

## 💰 **Payment Endpoints**

### **24. Get All Registrations**
```http
GET /api/payments/registrations
Authorization: Bearer {token}
```

**Response:**
```json
{
  "totalRegistrations": 3,
  "registrations": [
    {
      "registrationId": 1,
      "eventId": 1,
      "eventName": "Hackathon 2024",
      "studentId": 1,
      "studentName": "John Doe",
      "studentEmail": "john@example.com",
      "registrationDate": "2024-11-20T10:30:00",
      "registrationStatus": "registered",
      "paymentStatus": "pending",
      "fees": 500,
      "teamName": null,
      "teamId": null
    }
  ]
}
```

### **25. Create Payment Order**
```http
POST /api/payments/create-order
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "registrationId": 1,
  "amount": 500,
  "currency": "INR",
  "receiptEmail": "student@example.com"
}
```

**Response:**
```json
{
  "order": {
    "id": "order_1234567890",
    "entity": "order",
    "amount": 50000,
    "amount_paid": 0,
    "amount_due": 50000,
    "currency": "INR",
    "receipt": "receipt_1234567890",
    "status": "created",
    "attempts": 0,
    "notes": [],
    "created_at": 1703123456
  }
}
```

### **26. Verify Payment**
```http
POST /api/payments/verify
Content-Type: application/json
```

**Request Body:**
```json
{
  "razorpayOrderId": "order_1234567890",
  "status": "paid",
  "paymentId": "pay_1234567890"
}
```

**Response:**
```json
{
  "message": "Payment verified successfully",
  "paymentStatus": "paid"
}
```

---

## 🏫 **College Dashboard Endpoints**

### **27. College Dashboard Stats**
```http
GET /api/college/dashboard/stats
Authorization: Bearer {token}
```

**Response:**
```json
{
  "totalEvents": 10,
  "totalFests": 3,
  "totalRegistrations": 150,
  "totalEarnings": 75000,
  "pendingApprovals": 2,
  "recentRegistrations": [
    {
      "registrationId": 1,
      "eventName": "Hackathon 2024",
      "studentName": "John Doe",
      "registrationDate": "2024-11-20T10:30:00",
      "paymentStatus": "paid",
      "amount": 500
    }
  ]
}
```

### **28. College Event List with Stats**
```http
GET /api/college/dashboard/events
Authorization: Bearer {token}
```

**Response:**
```json
[
  {
    "eid": 1,
    "ename": "Hackathon 2024",
    "eventDate": "2024-12-01",
    "capacity": 100,
    "registrationCount": 25,
    "totalEarnings": 12500,
    "approved": true,
    "active": true,
    "festName": "TechFest 2024"
  }
]
```

### **29. College Dashboard Earnings**
```http
GET /api/college/dashboard/earnings
Authorization: Bearer {token}
```

**Response:**
```json
{
  "totalEarnings": 75000,
  "monthlyEarnings": [
    {
      "month": "November 2024",
      "earnings": 25000,
      "registrations": 50
    }
  ],
  "topEarningEvents": [
    {
      "eventName": "Hackathon 2024",
      "earnings": 12500,
      "registrations": 25
    }
  ]
}
```

### **30. Event Registrations (with Payment Status)**
```http
GET /api/college/dashboard/events/{eventId}/registrations
Authorization: Bearer {token}
```

**Response:**
```json
{
  "eventId": 1,
  "eventName": "Hackathon 2024",
  "totalRegistrations": 25,
  "eventCapacity": 100,
  "availableSlots": 75,
  "registrations": [
    {
      "registrationId": 1,
      "studentId": 1,
      "studentName": "John Doe",
      "studentEmail": "john@example.com",
      "registrationDate": "2024-11-20T10:30:00",
      "registrationStatus": "registered",
      "paymentStatus": "paid",
      "certificateApproved": false,
      "teamName": null,
      "teamId": null,
      "isTeamCreator": false
    }
  ]
}
```

### **31. Approve Certificate**
```http
POST /api/college/dashboard/events/{eventId}/registrations/{registrationId}/approve-certificate
Authorization: Bearer {token}
```

**Response:**
```json
{
  "message": "Certificate approved successfully"
}
```

### **32. Approve All Certificates**
```http
POST /api/college/dashboard/events/{eventId}/registrations/approve-all-certificates
Authorization: Bearer {token}
```

**Response:**
```json
{
  "message": "All certificates approved successfully",
  "approvedCount": 25
}
```

### **33. Approve Certificates for List**
```http
POST /api/college/dashboard/events/{eventId}/registrations/approve-certificates
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "registrationIds": [1, 2, 3]
}
```

**Response:**
```json
{
  "message": "Certificates approved successfully",
  "approvedCount": 3
}
```

---

## 👨‍💼 **Admin Management Endpoints**

### **34. Get Pending Fests**
```http
GET /api/admin/fests/pending
Authorization: Bearer {token}
```

**Response:**
```json
[
  {
    "fid": 1,
    "fname": "TechFest 2024",
    "fdescription": "Annual technical festival",
    "startDate": "2024-03-15",
    "endDate": "2024-03-17",
    "collegeName": "College One",
    "createdAt": "2024-11-20T10:30:00"
  }
]
```

### **35. Get Pending Events**
```http
GET /api/admin/events/pending
Authorization: Bearer {token}
```

**Response:**
```json
[
  {
    "eid": 1,
    "ename": "Hackathon 2024",
    "edescription": "24-hour coding competition",
    "eventDate": "2024-12-01",
    "collegeName": "College One",
    "festName": "TechFest 2024",
    "createdAt": "2024-11-20T10:30:00"
  }
]
```

### **36. Approve Fest**
```http
POST /api/admin/fests/{festId}/approve
Authorization: Bearer {token}
```

**Response:**
```json
{
  "message": "Fest approved successfully"
}
```

### **37. Reject Fest**
```http
POST /api/admin/fests/{festId}/reject
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "reason": "Fest content violates platform guidelines"
}
```

**Response:**
```json
{
  "message": "Fest rejected successfully"
}
```

### **38. Approve Event**
```http
POST /api/admin/events/{eventId}/approve
Authorization: Bearer {token}
```

**Response:**
```json
{
  "message": "Event approved successfully"
}
```

### **39. Reject Event**
```http
POST /api/admin/events/{eventId}/reject
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "reason": "Event content violates platform guidelines"
}
```

**Response:**
```json
{
  "message": "Event rejected successfully"
}
```

### **40. Admin Dashboard Stats**
```http
GET /api/admin/dashboard/stats
Authorization: Bearer {token}
```

**Response:**
```json
{
  "totalColleges": 15,
  "totalStudents": 500,
  "totalFests": 25,
  "totalEvents": 100,
  "totalRegistrations": 1500,
  "totalEarnings": 750000,
  "pendingFests": 3,
  "pendingEvents": 8
}
```

### **41. Get All Colleges**
```http
GET /api/admin/colleges
Authorization: Bearer {token}
```

**Response:**
```json
[
  {
    "cid": 1,
    "cname": "College One",
    "cdescription": "A great college",
    "address": "123 Main St",
    "contactEmail": "contact@college1.com",
    "totalEvents": 10,
    "totalFests": 3,
    "totalEarnings": 75000
  }
]
```

---

## 🔍 **Public Exploration Endpoints**

### **42. Explore Fests (Public)**
```http
GET /api/explore/fests?name=tech&city=Mumbai&mode=Online
```

**Query Parameters:**
- `name`: Filter by fest name
- `city`: Filter by city
- `mode`: Filter by mode (Online/Offline/Hybrid)

**Response:**
```json
[
  {
    "fid": 1,
    "fname": "TechFest 2024",
    "fdescription": "Annual technical festival",
    "startDate": "2024-03-15",
    "endDate": "2024-03-17",
    "city": "Mumbai",
    "state": "Maharashtra",
    "country": "India",
    "mode": "Offline",
    "website": "https://techfest2024.com",
    "contactPhone": "+91-9876543210",
    "approved": true,
    "active": true,
    "festImageUrl": "/uploads/fests/fest_image.jpg",
    "festThumbnailUrl": "/uploads/fests/fest_thumbnail.jpg",
    "collegeName": "College One",
    "eventCount": 5
  }
]
```

### **43. Explore Events (Public)**
```http
GET /api/explore/events?category=Technical&minFee=0&maxFee=500&teamAllowed=true
```

**Query Parameters:**
- `category`: Filter by event category
- `minFee`: Minimum entry fee
- `maxFee`: Maximum entry fee
- `teamAllowed`: Filter by team participation (true/false)

**Response:**
```json
[
  {
    "eid": 1,
    "ename": "Hackathon 2024",
    "edescription": "24-hour coding competition with exciting prizes",
    "eventDate": "2024-12-01",
    "fees": 500,
    "location": "Main Auditorium",
    "capacity": 100,
    "teamIsAllowed": true,
    "category": "Technical",
    "mode": "Offline",
    "posterUrl": "/uploads/events/poster.jpg",
    "posterThumbnailUrl": "/uploads/events/thumbnail.jpg",
    "approved": true,
    "active": true,
    "cashPrize": "₹50,000",
    "firstPrize": "₹25,000 + Trophy",
    "secondPrize": "₹15,000 + Medal",
    "thirdPrize": "₹10,000 + Certificate",
    "city": "Mumbai",
    "state": "Maharashtra",
    "country": "India",
    "eventWebsite": "https://hackathon2024.com",
    "contactPhone": "+91-9876543210",
    "organizerName": "Prof. John Doe",
    "organizerEmail": "john.doe@college.edu",
    "organizerPhone": "+91-9876543211",
    "rules": "1. Teams of 2-4 members\n2. 24-hour time limit\n3. No external help allowed\n4. Original code only",
    "requirements": "Laptop, College ID, Basic programming knowledge",
    "registrationDeadline": "2024-11-25",
    "registrationOpen": true,
    "collegeName": "College One",
    "collegeEmail": "college1@example.com",
    "festName": "TechFest 2024",
    "registrationCount": 25,
    "daysLeft": 5
  }
]
```

### **44. Get Explore Stats (Public)**
```http
GET /api/explore/stats
```

**Response:**
```json
{
  "totalFests": 25,
  "totalEvents": 100,
  "totalColleges": 15,
  "totalRegistrations": 1500,
  "totalEarnings": 750000,
  "categories": [
    {
      "name": "Technical",
      "count": 50
    },
    {
      "name": "Cultural",
      "count": 30
    },
    {
      "name": "Sports",
      "count": 20
    }
  ]
}
```

---

## 🏥 **Health Check Endpoints**

### **45. Health Check**
```http
GET /api/health
```

**Response:**
```json
{
  "status": "UP",
  "timestamp": "2024-11-20T10:30:00",
  "database": "UP",
  "memory": {
    "used": "512MB",
    "total": "2GB",
    "free": "1.5GB"
  },
  "uptime": "2 hours 30 minutes"
}
```

### **46. Ping**
```http
GET /api/health/ping
```

**Response:**
```json
{
  "message": "pong",
  "timestamp": "2024-11-20T10:30:00"
}
```

### **47. Test**
```http
GET /api/health/test
```

**Response:**
```json
{
  "message": "Application is working!",
  "timestamp": "2024-11-20T10:30:00"
}
```

### **48. Swagger Test**
```http
GET /api/health/swagger-test
```

**Response:**
```json
{
  "message": "Swagger is working!",
  "timestamp": "2024-11-20T10:30:00"
}
```

### **49. Debug**
```http
GET /api/health/debug
```

**Response:**
```json
{
  "request": {
    "method": "GET",
    "url": "/api/health/debug",
    "headers": {
      "User-Agent": "PostmanRuntime/7.32.3",
      "Accept": "*/*"
    }
  },
  "timestamp": "2024-11-20T10:30:00"
}
```

---

## 🧪 **Test & Debug Endpoints**

### **50. Protected Endpoint Test**
```http
GET /api/protected
Authorization: Bearer {token}
```

**Response:**
```json
{
  "message": "Hello, user@example.com! You are authenticated as Student"
}
```

### **51. Get All Users**
```http
GET /api/users
```

**Response:**
```json
[
  {
    "uid": 1,
    "email": "student@example.com",
    "role": "Student",
    "createdAt": "2024-11-20T10:30:00"
  },
  {
    "uid": 2,
    "email": "college@example.com",
    "role": "College",
    "createdAt": "2024-11-20T10:30:00"
  },
  {
    "uid": 3,
    "email": "admin@unbound.com",
    "role": "Admin",
    "createdAt": "2024-11-20T10:30:00"
  }
]
```

---

## 📝 **Error Response Format**

All endpoints return consistent error responses:

```json
{
  "error": "Error Type",
  "message": "Detailed error message",
  "timestamp": 1703123456789
}
```

**Common Error Types:**
- `Email not found` - 404
- `Incorrect password` - 401
- `Email already registered` - 409
- `College not found` - 404
- `Event not found` - 404
- `Fest not found` - 404
- `Registration closed` - 403
- `Payment failed` - 402
- `Invalid JSON request` - 400
- `Missing file upload` - 400

---

## 🔐 **Authentication Headers**

**Protected Endpoints:**
```http
Authorization: Bearer {jwt_token}
```

**Content-Type Headers:**
- `application/json` - For JSON requests
- `multipart/form-data` - For file uploads

---

## 📊 **Response Status Codes**

- `200` - Success
- `201` - Created
- `400` - Bad Request
- `401` - Unauthorized
- `403` - Forbidden
- `404` - Not Found
- `409` - Conflict
- `500` - Internal Server Error

---

## 🚀 **Usage Examples**

### **Complete Registration Flow:**
1. Register user: `POST /api/auth/register`
2. Login: `POST /api/auth/login`
3. Create fest: `POST /api/fests`
4. Create event: `POST /api/events`
5. Register for event: `POST /api/student/events/register`
6. Create payment: `POST /api/payments/create-order`
7. Verify payment: `POST /api/payments/verify`

### **College Management Flow:**
1. Register college: `POST /api/auth/register`
2. Login: `POST /api/auth/login`
3. Configure payments: `POST /api/college/payment-config`
4. Create fest: `POST /api/fests`
5. Create events: `POST /api/events`
6. View analytics: `GET /api/college/dashboard/stats`

### **Admin Management Flow:**
1. Register admin: `POST /api/auth/register`
2. Login: `POST /api/auth/login`
3. View pending content: `GET /api/admin/fests/pending`
4. Approve/reject content: `POST /api/admin/fests/{festId}/approve`

---

This comprehensive documentation covers all 51 endpoints with complete request/response examples, headers, and usage patterns. 