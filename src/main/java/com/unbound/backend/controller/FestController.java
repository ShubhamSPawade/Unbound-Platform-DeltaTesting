package com.unbound.backend.controller;

import com.unbound.backend.dto.FestRequest;
import com.unbound.backend.dto.FestResponse;
import com.unbound.backend.dto.EventResponse;
import com.unbound.backend.entity.*;
import com.unbound.backend.repository.*;
import com.unbound.backend.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fests")
@Tag(name = "Fest Management APIs", description = "APIs for managing fests (College access required)")
@SecurityRequirement(name = "bearerAuth")
public class FestController {
    @Autowired
    private FestRepository festRepository;
    @Autowired
    private CollegeRepository collegeRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private EventRegistrationRepository eventRegistrationRepository;
    @Autowired
    private FileStorageService fileStorageService;

    private College getCollegeForUser(User user) {
        return collegeRepository.findAll().stream()
                .filter(c -> c.getUser().getUid().equals(user.getUid()))
                .findFirst().orElse(null);
    }

    @GetMapping
    @Operation(summary = "List all fests for a college")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved fests"),
        @ApiResponse(responseCode = "403", description = "Forbidden: Only colleges can access this endpoint"),
        @ApiResponse(responseCode = "404", description = "College not found")
    })
    public ResponseEntity<?> listFests(@AuthenticationPrincipal User user) {
        if (user == null || user.getRole() != User.Role.College) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden: Only colleges can access this endpoint"));
        }
        College college = getCollegeForUser(user);
        if (college == null) return ResponseEntity.notFound().build();
        List<Fest> fests = festRepository.findByCollege(college);
        
        List<FestResponse> responses = fests.stream().map(fest -> {
            int eventCount = eventRepository.findByFest(fest).size();
            int registrationCount = eventRepository.findByFest(fest).stream()
                    .mapToInt(event -> eventRegistrationRepository.findByEvent(event).size())
                    .sum();
            
            return FestResponse.builder()
                    .fid(fest.getFid())
                    .fname(fest.getFname())
                    .fdescription(fest.getFdescription())
                    .startDate(fest.getStartDate())
                    .endDate(fest.getEndDate())
                    .festImageUrl(fest.getFestImageUrl())
                    .festThumbnailUrl(fest.getFestThumbnailUrl())
                    .approved(fest.isApproved())
                    .active(fest.isActive())
                    .city(fest.getCity())
                    .state(fest.getState())
                    .country(fest.getCountry())
                    .mode(fest.getMode())
                    .website(fest.getWebsite())
                    .contactPhone(fest.getContactPhone())
                    .collegeName(college.getCname())
                    .collegeEmail(college.getUser().getEmail())
                    .eventCount(eventCount)
                    .registrationCount(registrationCount)
                    .build();
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    @Operation(summary = "Create a new fest for a college")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Fest created successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden: Only colleges can access this endpoint"),
        @ApiResponse(responseCode = "404", description = "College not found"),
        @ApiResponse(responseCode = "400", description = "Fest name already exists for this college or invalid date range")
    })
    public ResponseEntity<?> createFest(@AuthenticationPrincipal User user, @Valid @RequestBody FestRequest festRequest) {
        if (user == null || user.getRole() != User.Role.College) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden: Only colleges can access this endpoint"));
        }
        College college = getCollegeForUser(user);
        if (college == null) return ResponseEntity.notFound().build();
        // Duplicate name check
        boolean exists = festRepository.findByCollege(college).stream()
                .anyMatch(f -> f.getFname().equalsIgnoreCase(festRequest.getFname()));
        if (exists) {
            return ResponseEntity.badRequest().body(Map.of("error", "Fest name already exists for this college"));
        }
        // Date validation
        if (!isValidDateRange(festRequest.getStartDate(), festRequest.getEndDate())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Start date must be before end date"));
        }
        Fest fest = Fest.builder()
                .college(college)
                .fname(festRequest.getFname())
                .fdescription(festRequest.getFdescription())
                .startDate(festRequest.getStartDate())
                .endDate(festRequest.getEndDate())
                .festImageUrl(festRequest.getFestImageUrl())
                .festThumbnailUrl(festRequest.getFestThumbnailUrl())
                .city(festRequest.getCity())
                .state(festRequest.getState())
                .country(festRequest.getCountry())
                .mode(festRequest.getMode())
                .website(festRequest.getWebsite())
                .contactPhone(festRequest.getContactPhone())
                .approved(false) // Needs admin approval
                .active(true)
                .build();
        Fest saved = festRepository.save(fest);
        
        FestResponse response = FestResponse.builder()
                .fid(saved.getFid())
                .fname(saved.getFname())
                .fdescription(saved.getFdescription())
                .startDate(saved.getStartDate())
                .endDate(saved.getEndDate())
                .festImageUrl(saved.getFestImageUrl())
                .festThumbnailUrl(saved.getFestThumbnailUrl())
                .approved(saved.isApproved())
                .active(saved.isActive())
                .city(saved.getCity())
                .state(saved.getState())
                .country(saved.getCountry())
                .mode(saved.getMode())
                .website(saved.getWebsite())
                .contactPhone(saved.getContactPhone())
                .collegeName(college.getCname())
                .collegeEmail(college.getUser().getEmail())
                .eventCount(0)
                .registrationCount(0)
                .build();
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{fid}/image")
    @Operation(summary = "Upload an image for a fest")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Fest image uploaded successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden: Only colleges can upload fest images"),
        @ApiResponse(responseCode = "404", description = "Fest not found")
    })
    public ResponseEntity<?> uploadFestImage(@AuthenticationPrincipal User user, 
                                           @PathVariable Integer fid, 
                                           @RequestParam("image") MultipartFile image) {
        if (user == null || user.getRole() != User.Role.College) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden: Only colleges can upload fest images"));
        }
        
        Fest fest = festRepository.findById(fid).orElse(null);
        if (fest == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Fest not found"));
        }
        
        College college = getCollegeForUser(user);
        if (college == null || !fest.getCollege().getCid().equals(college.getCid())) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden: You can only upload images for your own fests"));
        }
        
        try {
            // Delete old image if exists
            if (fest.getFestImageUrl() != null) {
                fileStorageService.deleteFile(fest.getFestImageUrl());
            }
            
            // Store new image
            String imageUrl = fileStorageService.storeFestImage(image);
            String thumbnailUrl = imageUrl; // For now, use same image as thumbnail
            
            // Update fest
            fest.setFestImageUrl(imageUrl);
            fest.setFestThumbnailUrl(thumbnailUrl);
            festRepository.save(fest);
            
            return ResponseEntity.ok(Map.of(
                "message", "Fest image uploaded successfully",
                "imageUrl", imageUrl,
                "thumbnailUrl", thumbnailUrl
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to upload image: " + e.getMessage()));
        }
    }

    @PutMapping("/{fid}")
    @Operation(summary = "Update an existing fest")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Fest updated successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden: Only colleges can access this endpoint"),
        @ApiResponse(responseCode = "404", description = "Fest not found or not owned by this college"),
        @ApiResponse(responseCode = "400", description = "Fest name already exists for this college or invalid date range")
    })
    public ResponseEntity<?> updateFest(@AuthenticationPrincipal User user, @PathVariable Integer fid, @Valid @RequestBody FestRequest festRequest) {
        if (user == null || user.getRole() != User.Role.College) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden: Only colleges can access this endpoint"));
        }
        College college = getCollegeForUser(user);
        if (college == null) return ResponseEntity.notFound().build();
        Fest fest = festRepository.findById(fid).orElse(null);
        if (fest == null || !fest.getCollege().getCid().equals(college.getCid())) {
            return ResponseEntity.status(404).body(Map.of("error", "Fest not found or not owned by this college"));
        }
        // Duplicate name check (excluding self)
        boolean exists = festRepository.findByCollege(college).stream()
                .anyMatch(f -> !f.getFid().equals(fid) && f.getFname().equalsIgnoreCase(festRequest.getFname()));
        if (exists) {
            return ResponseEntity.badRequest().body(Map.of("error", "Fest name already exists for this college"));
        }
        // Date validation
        if (!isValidDateRange(festRequest.getStartDate(), festRequest.getEndDate())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Start date must be before end date"));
        }
        
        fest.setFname(festRequest.getFname());
        fest.setFdescription(festRequest.getFdescription());
        fest.setStartDate(festRequest.getStartDate());
        fest.setEndDate(festRequest.getEndDate());
        fest.setFestImageUrl(festRequest.getFestImageUrl());
        fest.setFestThumbnailUrl(festRequest.getFestThumbnailUrl());
        fest.setCity(festRequest.getCity());
        fest.setState(festRequest.getState());
        fest.setCountry(festRequest.getCountry());
        fest.setMode(festRequest.getMode());
        fest.setWebsite(festRequest.getWebsite());
        fest.setContactPhone(festRequest.getContactPhone());
        festRepository.save(fest);
        
        int eventCount = eventRepository.findByFest(fest).size();
        int registrationCount = eventRepository.findByFest(fest).stream()
                .mapToInt(event -> eventRegistrationRepository.findByEvent(event).size())
                .sum();
        
        FestResponse response = FestResponse.builder()
                .fid(fest.getFid())
                .fname(fest.getFname())
                .fdescription(fest.getFdescription())
                .startDate(fest.getStartDate())
                .endDate(fest.getEndDate())
                .festImageUrl(fest.getFestImageUrl())
                .festThumbnailUrl(fest.getFestThumbnailUrl())
                .approved(fest.isApproved())
                .active(fest.isActive())
                .city(fest.getCity())
                .state(fest.getState())
                .country(fest.getCountry())
                .mode(fest.getMode())
                .website(fest.getWebsite())
                .contactPhone(fest.getContactPhone())
                .collegeName(college.getCname())
                .collegeEmail(college.getUser().getEmail())
                .eventCount(eventCount)
                .registrationCount(registrationCount)
                .build();
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{fid}")
    @Operation(summary = "Delete a fest")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Fest deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden: Only colleges can access this endpoint"),
        @ApiResponse(responseCode = "404", description = "Fest not found or not owned by this college")
    })
    public ResponseEntity<?> deleteFest(@AuthenticationPrincipal User user, @PathVariable Integer fid) {
        if (user == null || user.getRole() != User.Role.College) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden: Only colleges can access this endpoint"));
        }
        College college = getCollegeForUser(user);
        if (college == null) return ResponseEntity.notFound().build();
        Fest fest = festRepository.findById(fid).orElse(null);
        if (fest == null || !fest.getCollege().getCid().equals(college.getCid())) {
            return ResponseEntity.status(404).body(Map.of("error", "Fest not found or not owned by this college"));
        }
        festRepository.delete(fest);
        return ResponseEntity.ok(Map.of("message", "Fest deleted successfully"));
    }

    @GetMapping("/{fid}/events")
    @Operation(summary = "Get all events for a fest")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved events"),
        @ApiResponse(responseCode = "403", description = "Forbidden: Only colleges can access this endpoint"),
        @ApiResponse(responseCode = "404", description = "Fest not found or not owned by this college")
    })
    public ResponseEntity<?> getFestEvents(@AuthenticationPrincipal User user, @PathVariable Integer fid) {
        if (user == null || user.getRole() != User.Role.College) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden: Only colleges can access this endpoint"));
        }
        College college = getCollegeForUser(user);
        if (college == null) return ResponseEntity.notFound().build();
        Fest fest = festRepository.findById(fid).orElse(null);
        if (fest == null || !fest.getCollege().getCid().equals(college.getCid())) {
            return ResponseEntity.status(404).body(Map.of("error", "Fest not found or not owned by this college"));
        }
        
        List<Event> events = eventRepository.findByFest(fest);
        List<EventResponse> responses = events.stream().map(event -> {
            int registrationCount = eventRegistrationRepository.findByEvent(event).size();
            int daysLeft = (int) java.time.temporal.ChronoUnit.DAYS.between(
                java.time.LocalDate.now(), 
                java.time.LocalDate.parse(event.getEventDate())
            );
            
            return EventResponse.builder()
                    .eid(event.getEid())
                    .ename(event.getEname())
                    .edescription(event.getEdescription())
                    .eventDate(event.getEventDate())
                    .fees(event.getFees())
                    .location(event.getLocation())
                    .capacity(event.getCapacity())
                    .teamIsAllowed(event.getTeamIsAllowed())
                    .category(event.getCategory())
                    .mode(event.getMode())
                    .posterUrl(event.getPosterUrl())
                    .posterThumbnailUrl(event.getPosterThumbnailUrl())
                    .posterApproved(event.isPosterApproved())
                    .approved(event.isApproved())
                    .active(event.isActive())
                    .cashPrize(event.getCashPrize())
                    .firstPrize(event.getFirstPrize())
                    .secondPrize(event.getSecondPrize())
                    .thirdPrize(event.getThirdPrize())
                    .city(event.getCity())
                    .state(event.getState())
                    .country(event.getCountry())
                    .eventWebsite(event.getEventWebsite())
                    .contactPhone(event.getContactPhone())
                    .organizerName(event.getOrganizerName())
                    .organizerEmail(event.getOrganizerEmail())
                    .organizerPhone(event.getOrganizerPhone())
                    .rules(event.getRules())
                    .requirements(event.getRequirements())
                    .registrationDeadline(event.getRegistrationDeadline())
                    .registrationOpen(event.isRegistrationOpen())
                    .collegeName(college.getCname())
                    .collegeEmail(college.getUser().getEmail())
                    .festName(fest.getFname())
                    .registrationCount(registrationCount)
                    .daysLeft(daysLeft)
                    .build();
        }).collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("festId", fest.getFid());
        response.put("festName", fest.getFname());
        response.put("totalEvents", events.size());
        response.put("events", responses);
        
        return ResponseEntity.ok(response);
    }

    private boolean isValidDateRange(String start, String end) {
        try {
            LocalDate s = LocalDate.parse(start);
            LocalDate e = LocalDate.parse(end);
            return s.isBefore(e) || s.isEqual(e);
        } catch (Exception ex) {
            throw new RuntimeException("Invalid date format for start date '" + start + "' or end date '" + end + "'.");
        }
    }
} 