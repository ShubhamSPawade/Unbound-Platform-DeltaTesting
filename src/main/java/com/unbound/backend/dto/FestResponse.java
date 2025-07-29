package com.unbound.backend.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FestResponse {
    private Integer fid;
    private String fname;
    private String fdescription;
    private String startDate;
    private String endDate;
    private String festImageUrl;
    private String festThumbnailUrl;
    private boolean approved;
    private boolean active;
    private String city;
    private String state;
    private String country;
    private String mode;
    private String website;
    private String contactPhone;
    private String collegeName;
    private String collegeEmail;
    private Integer eventCount; // Number of events in this fest
    private Integer registrationCount; // Total registrations across all events
} 