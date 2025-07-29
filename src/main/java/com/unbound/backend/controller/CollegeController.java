package com.unbound.backend.controller;

import com.unbound.backend.entity.College;
import com.unbound.backend.entity.User;
import com.unbound.backend.repository.CollegeRepository;
import com.unbound.backend.dto.CollegePaymentConfigRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/college")
@Tag(name = "College Management APIs", description = "APIs for college management and configuration")
@SecurityRequirement(name = "bearerAuth")
public class CollegeController {
    @Autowired
    private CollegeRepository collegeRepository;

    @PostMapping("/payment-config")
    @Operation(summary = "Configure college payment settings", description = "Allows colleges to set up their payment receiving details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment configuration updated successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden: Only colleges can configure payment settings"),
        @ApiResponse(responseCode = "404", description = "College not found")
    })
    public ResponseEntity<?> configurePaymentSettings(@AuthenticationPrincipal User user, 
                                                   @Valid @RequestBody CollegePaymentConfigRequest request) {
        if (user == null || user.getRole() != User.Role.College) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden: Only colleges can configure payment settings"));
        }
        
        College college = collegeRepository.findByUser(user).orElse(null);
        if (college == null) {
            return ResponseEntity.status(404).body(Map.of("error", "College not found"));
        }
        
        // Update college payment configuration
        college.setRazorpayAccountId(request.getRazorpayAccountId());
        college.setBankAccountNumber(request.getBankAccountNumber());
        college.setBankIfscCode(request.getBankIfscCode());
        college.setBankAccountHolderName(request.getBankAccountHolderName());
        college.setContactEmail(request.getContactEmail());
        
        collegeRepository.save(college);
        
        return ResponseEntity.ok(Map.of(
            "message", "Payment configuration updated successfully",
            "collegeName", college.getCname(),
            "razorpayAccountId", college.getRazorpayAccountId(),
            "bankAccountNumber", college.getBankAccountNumber(),
            "bankIfscCode", college.getBankIfscCode(),
            "bankAccountHolderName", college.getBankAccountHolderName(),
            "contactEmail", college.getContactEmail()
        ));
    }

    @GetMapping("/payment-config")
    @Operation(summary = "Get college payment settings", description = "Retrieves the current payment configuration for the college")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment configuration retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden: Only colleges can view payment settings"),
        @ApiResponse(responseCode = "404", description = "College not found")
    })
    public ResponseEntity<?> getPaymentSettings(@AuthenticationPrincipal User user) {
        if (user == null || user.getRole() != User.Role.College) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden: Only colleges can view payment settings"));
        }
        
        College college = collegeRepository.findByUser(user).orElse(null);
        if (college == null) {
            return ResponseEntity.status(404).body(Map.of("error", "College not found"));
        }
        
        return ResponseEntity.ok(Map.of(
            "collegeName", college.getCname(),
            "razorpayAccountId", college.getRazorpayAccountId(),
            "bankAccountNumber", college.getBankAccountNumber(),
            "bankIfscCode", college.getBankIfscCode(),
            "bankAccountHolderName", college.getBankAccountHolderName(),
            "contactEmail", college.getContactEmail(),
            "isConfigured", college.getRazorpayAccountId() != null && !college.getRazorpayAccountId().isEmpty()
        ));
    }
} 