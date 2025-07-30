package com.unbound.backend.controller;

import com.razorpay.Order;
import com.razorpay.RazorpayException;
import com.unbound.backend.entity.EventRegistration;
import com.unbound.backend.repository.EventRegistrationRepository;
import com.unbound.backend.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.unbound.backend.entity.User;

import java.util.Map;
import com.unbound.backend.exception.PaymentFailedException;
import com.unbound.backend.exception.RegistrationClosedException;
import java.util.List;
import java.util.HashMap;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private EventRegistrationRepository eventRegistrationRepository;

    @GetMapping("/registrations")
    public ResponseEntity<?> getAllRegistrations() {
        List<EventRegistration> allRegistrations = eventRegistrationRepository.findAll();
        List<Map<String, Object>> registrations = allRegistrations.stream()
            .map(reg -> {
                Map<String, Object> regInfo = new HashMap<>();
                regInfo.put("registrationId", reg.getRid());
                regInfo.put("eventId", reg.getEvent().getEid());
                regInfo.put("eventName", reg.getEvent().getEname());
                regInfo.put("studentId", reg.getStudent().getSid());
                regInfo.put("studentName", reg.getStudent().getSname());
                regInfo.put("studentEmail", reg.getStudent().getUser().getEmail());
                regInfo.put("registrationDate", reg.getErdateTime());
                regInfo.put("registrationStatus", reg.getStatus());
                regInfo.put("paymentStatus", reg.getPaymentStatus());
                regInfo.put("fees", reg.getEvent().getFees());
                if (reg.getTeam() != null) {
                    regInfo.put("teamName", reg.getTeam().getTname());
                    regInfo.put("teamId", reg.getTeam().getTid());
                } else {
                    regInfo.put("teamName", null);
                    regInfo.put("teamId", null);
                }
                return regInfo;
            })
            .collect(java.util.stream.Collectors.toList());
        
        return ResponseEntity.ok(Map.of(
            "totalRegistrations", allRegistrations.size(),
            "registrations", registrations
        ));
    }

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@AuthenticationPrincipal User user, @RequestBody Map<String, Object> req) {
        try {
            Long registrationId = Long.valueOf((Integer) req.get("registrationId"));
            Integer amount = (Integer) req.get("amount");
            String currency = (String) req.getOrDefault("currency", "INR");
            String receiptEmail = (String) req.get("receiptEmail");
            
            EventRegistration registration = eventRegistrationRepository.findById(registrationId).orElse(null);
            if (registration == null) {
                // Get available registrations for better error message
                List<EventRegistration> allRegistrations = eventRegistrationRepository.findAll();
                List<Map<String, Object>> availableRegistrations = allRegistrations.stream()
                    .map(reg -> {
                        Map<String, Object> regInfo = new HashMap<>();
                        regInfo.put("registrationId", reg.getRid());
                        regInfo.put("eventName", reg.getEvent().getEname());
                        regInfo.put("studentName", reg.getStudent().getSname());
                        regInfo.put("paymentStatus", reg.getPaymentStatus());
                        return regInfo;
                    })
                    .collect(java.util.stream.Collectors.toList());
                
                return ResponseEntity.status(404).body(Map.of(
                    "error", "Registration not found",
                    "message", "Registration with ID " + registrationId + " does not exist.",
                    "availableRegistrations", availableRegistrations,
                    "totalRegistrations", allRegistrations.size()
                ));
            }
            
            Order order = paymentService.createOrder(registration, amount, currency, receiptEmail);
            return ResponseEntity.ok(Map.of("order", order.toJson()));
        } catch (RazorpayException e) {
            throw new PaymentFailedException("Payment gateway error for registration ID " + req.get("registrationId") + ": " + e.getMessage());
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, Object> req) {
        String razorpayOrderId = (String) req.get("razorpayOrderId");
        String status = (String) req.get("status");
        String paymentId = (String) req.get("paymentId");
        paymentService.updatePaymentStatus(razorpayOrderId, status, paymentId);
        return ResponseEntity.ok(Map.of("message", "Payment status updated"));
    }
} 