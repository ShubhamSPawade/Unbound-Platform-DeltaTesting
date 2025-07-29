package com.unbound.backend.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
public class CollegePaymentConfigRequest {
    
    @NotBlank(message = "Razorpay account ID is required")
    private String razorpayAccountId;
    
    @NotBlank(message = "Bank account number is required")
    private String bankAccountNumber;
    
    @NotBlank(message = "Bank IFSC code is required")
    private String bankIfscCode;
    
    @NotBlank(message = "Bank account holder name is required")
    private String bankAccountHolderName;
    
    @Email(message = "Contact email must be valid")
    @NotBlank(message = "Contact email is required")
    private String contactEmail;
} 