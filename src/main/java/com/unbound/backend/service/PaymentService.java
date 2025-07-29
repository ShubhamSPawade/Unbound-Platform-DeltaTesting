package com.unbound.backend.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.unbound.backend.entity.EventRegistration;
import com.unbound.backend.entity.Payment;
import com.unbound.backend.entity.Student;
import com.unbound.backend.entity.College;
import com.unbound.backend.repository.EventRegistrationRepository;
import com.unbound.backend.repository.PaymentRepository;
import com.unbound.backend.service.EmailService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private EventRegistrationRepository eventRegistrationRepository;

    @Autowired
    private EmailService emailService;

    @Value("${razorpay.keyId}")
    private String razorpayKeyId;

    @Value("${razorpay.keySecret}")
    private String razorpayKeySecret;

    public Order createOrder(EventRegistration registration, int amount, String currency, String receiptEmail) throws RazorpayException {
        RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
        
        // Get the college that will receive the payment
        College college = registration.getEvent().getCollege();
        
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount * 100); // amount in paise
        orderRequest.put("currency", currency);
        orderRequest.put("receipt", "reg-" + registration.getRid());
        orderRequest.put("payment_capture", 1);
        
        // Add college payment routing if configured
        if (college.getRazorpayAccountId() != null && !college.getRazorpayAccountId().isEmpty()) {
            JSONObject transferRequest = new JSONObject();
            transferRequest.put("account", college.getRazorpayAccountId());
            transferRequest.put("amount", amount * 100);
            transferRequest.put("currency", currency);
            orderRequest.put("transfers", new JSONObject[]{transferRequest});
        }
        
        Order order = client.orders.create(orderRequest);

        Payment payment = Payment.builder()
                .eventRegistration(registration)
                .razorpayOrderId(order.get("id"))
                .status("pending")
                .amount(amount)
                .currency(currency)
                .createdAt(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .receiptEmail(receiptEmail)
                .college(college) // Track which college receives the payment
                .build();
        paymentRepository.save(payment);
        return order;
    }

    public void updatePaymentStatus(String razorpayOrderId, String status, String paymentId) {
        Payment payment = paymentRepository.findAll().stream()
                .filter(p -> p.getRazorpayOrderId().equals(razorpayOrderId))
                .findFirst().orElse(null);
        if (payment != null) {
            payment.setStatus(status);
            payment.setPaymentId(paymentId);
            paymentRepository.save(payment);
            
            // Update EventRegistration paymentStatus
            EventRegistration reg = payment.getEventRegistration();
            if (reg != null) {
                reg.setPaymentStatus(status);
                eventRegistrationRepository.save(reg);
                
                // Send email receipt if payment is successful
                if ("paid".equalsIgnoreCase(status) && payment.getReceiptEmail() != null) {
                    Student student = reg.getStudent();
                    College college = payment.getCollege();
                    
                    String subject = "Payment Receipt - Unbound Event Registration";
                    String text = String.format(
                        "Dear %s,\n\n" +
                        "Your payment for event '%s' was successful!\n\n" +
                        "Payment Details:\n" +
                        "- Amount: %d %s\n" +
                        "- Payment ID: %s\n" +
                        "- Order ID: %s\n" +
                        "- College: %s\n" +
                        "- Event: %s\n\n" +
                        "The payment has been received by %s.\n\n" +
                        "Thank you for registering!\n\n" +
                        "- Unbound Platform Team",
                        student.getSname(),
                        reg.getEvent().getEname(),
                        payment.getAmount(),
                        payment.getCurrency(),
                        paymentId,
                        razorpayOrderId,
                        college.getCname(),
                        reg.getEvent().getEname(),
                        college.getCname()
                    );
                    emailService.sendEmail(payment.getReceiptEmail(), subject, text);
                    
                    // Send notification to college about the payment
                    if (college.getContactEmail() != null) {
                        String collegeSubject = "New Payment Received - Event Registration";
                        String collegeText = String.format(
                            "Dear %s,\n\n" +
                            "A new payment has been received for your event!\n\n" +
                            "Payment Details:\n" +
                            "- Student: %s (%s)\n" +
                            "- Event: %s\n" +
                            "- Amount: %d %s\n" +
                            "- Payment ID: %s\n" +
                            "- Order ID: %s\n\n" +
                            "The payment has been credited to your account.\n\n" +
                            "- Unbound Platform Team",
                            college.getCname(),
                            student.getSname(),
                            student.getUser().getEmail(),
                            reg.getEvent().getEname(),
                            payment.getAmount(),
                            payment.getCurrency(),
                            paymentId,
                            razorpayOrderId
                        );
                        emailService.sendEmail(college.getContactEmail(), collegeSubject, collegeText);
                    }
                }
            }
        }
    }
} 