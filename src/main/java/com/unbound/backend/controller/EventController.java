package com.unbound.backend.controller;

import com.unbound.backend.dto.EventRequest;
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
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Event Management APIs", description = "APIs for managing events (College access required)")
@SecurityRequirement(name = "bearerAuth")
public class EventController {
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private CollegeRepository collegeRepository;
    @Autowired
    private FestRepository festRepository;
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
    @Operation(summary = "List events for a college")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved events"),
        @ApiResponse(responseCode = "403", description = "Forbidden: Only colleges can access this endpoint"),
        @ApiResponse(responseCode = "404", description = "College not found")
    })
    public ResponseEntity<?> listEvents(@AuthenticationPrincipal User user) {
        if (user == null || user.getRole() != User.Role.College) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden: Only colleges can access this endpoint"));
        }
        College college = getCollegeForUser(user);
        if (college == null) return ResponseEntity.notFound().build();
        List<Event> events = eventRepository.findByCollege(college);
        
        List<EventResponse> responses = events.stream().map(event -> {
            int registrationCount = eventRegistrationRepository.findByEvent(event).size();
            int daysLeft = (int) ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(event.getEventDate()));
            
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
                    .festName(event.getFest() != null ? event.getFest().getFname() : null)
                    .registrationCount(registrationCount)
                    .daysLeft(daysLeft)
                    .build();
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    @Operation(summary = "Create a new event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Event created successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden: Only colleges can access this endpoint"),
        @ApiResponse(responseCode = "404", description = "College not found"),
        @ApiResponse(responseCode = "400", description = "Event name already exists for this college")
    })
    public ResponseEntity<?> createEvent(@AuthenticationPrincipal User user, @Valid @RequestBody EventRequest eventRequest) {
        if (user == null || user.getRole() != User.Role.College) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden: Only colleges can access this endpoint"));
        }
        College college = getCollegeForUser(user);
        if (college == null) return ResponseEntity.notFound().build();
        // Duplicate name check
        boolean exists = eventRepository.findByCollege(college).stream()
                .anyMatch(e -> e.getEname().equalsIgnoreCase(eventRequest.getEname()));
        if (exists) {
            return ResponseEntity.badRequest().body(Map.of("error", "Event name already exists for this college"));
        }
        // Fest linkage and event date validation
        Fest fest = null;
        if (eventRequest.getFestId() != null) {
            fest = festRepository.findById(eventRequest.getFestId()).orElse(null);
            if (fest == null || !fest.getCollege().getCid().equals(college.getCid())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid fest for this college"));
            }
            if (!isDateWithinRange(eventRequest.getEventDate(), fest.getStartDate(), fest.getEndDate())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Event date must be within fest date range"));
            }
        }
        Event event = Event.builder()
                .college(college)
                .fest(fest)
                .ename(eventRequest.getEname())
                .edescription(eventRequest.getEdescription())
                .eventDate(eventRequest.getEventDate())
                .fees(eventRequest.getFees())
                .location(eventRequest.getLocation())
                .capacity(eventRequest.getCapacity())
                .teamIsAllowed(eventRequest.getTeamIsAllowed())
                .category(eventRequest.getCategory())
                .mode(eventRequest.getMode())
                .posterUrl(eventRequest.getPosterUrl())
                .cashPrize(eventRequest.getCashPrize())
                .firstPrize(eventRequest.getFirstPrize())
                .secondPrize(eventRequest.getSecondPrize())
                .thirdPrize(eventRequest.getThirdPrize())
                .city(eventRequest.getCity())
                .state(eventRequest.getState())
                .country(eventRequest.getCountry())
                .eventWebsite(eventRequest.getEventWebsite())
                .contactPhone(eventRequest.getContactPhone())
                .organizerName(eventRequest.getOrganizerName())
                .organizerEmail(eventRequest.getOrganizerEmail())
                .organizerPhone(eventRequest.getOrganizerPhone())
                .rules(eventRequest.getRules())
                .requirements(eventRequest.getRequirements())
                .registrationDeadline(eventRequest.getRegistrationDeadline())
                .registrationOpen(eventRequest.getRegistrationOpen())
                .approved(false) // Needs admin approval
                .active(true)
                .build();
        Event saved = eventRepository.save(event);
        
        EventResponse response = EventResponse.builder()
                .eid(saved.getEid())
                .ename(saved.getEname())
                .edescription(saved.getEdescription())
                .eventDate(saved.getEventDate())
                .fees(saved.getFees())
                .location(saved.getLocation())
                .capacity(saved.getCapacity())
                .teamIsAllowed(saved.getTeamIsAllowed())
                .category(saved.getCategory())
                .mode(saved.getMode())
                .posterUrl(saved.getPosterUrl())
                .posterThumbnailUrl(saved.getPosterThumbnailUrl())
                .posterApproved(saved.isPosterApproved())
                .approved(saved.isApproved())
                .active(saved.isActive())
                .cashPrize(saved.getCashPrize())
                .firstPrize(saved.getFirstPrize())
                .secondPrize(saved.getSecondPrize())
                .thirdPrize(saved.getThirdPrize())
                .city(saved.getCity())
                .state(saved.getState())
                .country(saved.getCountry())
                .eventWebsite(saved.getEventWebsite())
                .contactPhone(saved.getContactPhone())
                .organizerName(saved.getOrganizerName())
                .organizerEmail(saved.getOrganizerEmail())
                .organizerPhone(saved.getOrganizerPhone())
                .rules(saved.getRules())
                .requirements(saved.getRequirements())
                .registrationDeadline(saved.getRegistrationDeadline())
                .registrationOpen(saved.isRegistrationOpen())
                .collegeName(college.getCname())
                .collegeEmail(college.getUser().getEmail())
                .festName(fest != null ? fest.getFname() : null)
                .registrationCount(0)
                .daysLeft((int) ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(saved.getEventDate())))
                .build();
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{eid}")
    @Operation(summary = "Update an existing event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Event updated successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden: Only colleges can access this endpoint"),
        @ApiResponse(responseCode = "404", description = "Event not found or not owned by this college")
    })
    public ResponseEntity<?> updateEvent(@AuthenticationPrincipal User user, @PathVariable Integer eid, @Valid @RequestBody EventRequest eventRequest) {
        if (user == null || user.getRole() != User.Role.College) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden: Only colleges can access this endpoint"));
        }
        College college = getCollegeForUser(user);
        if (college == null) return ResponseEntity.notFound().build();
        Event event = eventRepository.findById(eid).orElse(null);
        if (event == null || !event.getCollege().getCid().equals(college.getCid())) {
            return ResponseEntity.status(404).body(Map.of("error", "Event not found or not owned by this college"));
        }
        // Duplicate name check (excluding self)
        boolean exists = eventRepository.findByCollege(college).stream()
                .anyMatch(e -> !e.getEid().equals(eid) && e.getEname().equalsIgnoreCase(eventRequest.getEname()));
        if (exists) {
            return ResponseEntity.badRequest().body(Map.of("error", "Event name already exists for this college"));
        }
        // Fest linkage and event date validation
        Fest fest = null;
        if (eventRequest.getFestId() != null) {
            fest = festRepository.findById(eventRequest.getFestId()).orElse(null);
            if (fest == null || !fest.getCollege().getCid().equals(college.getCid())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid fest for this college"));
            }
            if (!isDateWithinRange(eventRequest.getEventDate(), fest.getStartDate(), fest.getEndDate())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Event date must be within fest date range"));
            }
        }
        event.setFest(fest);
        event.setEname(eventRequest.getEname());
        event.setEdescription(eventRequest.getEdescription());
        event.setEventDate(eventRequest.getEventDate());
        event.setFees(eventRequest.getFees());
        event.setLocation(eventRequest.getLocation());
        event.setCapacity(eventRequest.getCapacity());
        event.setTeamIsAllowed(eventRequest.getTeamIsAllowed());
        event.setCategory(eventRequest.getCategory());
        event.setMode(eventRequest.getMode());
        event.setPosterUrl(eventRequest.getPosterUrl());
        event.setCashPrize(eventRequest.getCashPrize());
        event.setFirstPrize(eventRequest.getFirstPrize());
        event.setSecondPrize(eventRequest.getSecondPrize());
        event.setThirdPrize(eventRequest.getThirdPrize());
        event.setCity(eventRequest.getCity());
        event.setState(eventRequest.getState());
        event.setCountry(eventRequest.getCountry());
        event.setEventWebsite(eventRequest.getEventWebsite());
        event.setContactPhone(eventRequest.getContactPhone());
        event.setOrganizerName(eventRequest.getOrganizerName());
        event.setOrganizerEmail(eventRequest.getOrganizerEmail());
        event.setOrganizerPhone(eventRequest.getOrganizerPhone());
        event.setRules(eventRequest.getRules());
        event.setRequirements(eventRequest.getRequirements());
        event.setRegistrationDeadline(eventRequest.getRegistrationDeadline());
        event.setRegistrationOpen(eventRequest.getRegistrationOpen());
        eventRepository.save(event);
        
        int registrationCount = eventRegistrationRepository.findByEvent(event).size();
        int daysLeft = (int) ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(event.getEventDate()));
        
        EventResponse response = EventResponse.builder()
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
                .festName(fest != null ? fest.getFname() : null)
                .registrationCount(registrationCount)
                .daysLeft(daysLeft)
                .build();
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{eid}")
    @Operation(summary = "Delete an event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Event deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden: Only colleges can access this endpoint"),
        @ApiResponse(responseCode = "404", description = "Event not found or not owned by this college")
    })
    public ResponseEntity<?> deleteEvent(@AuthenticationPrincipal User user, @PathVariable Integer eid) {
        if (user == null || user.getRole() != User.Role.College) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden: Only colleges can access this endpoint"));
        }
        College college = getCollegeForUser(user);
        if (college == null) return ResponseEntity.notFound().build();
        Event event = eventRepository.findById(eid).orElse(null);
        if (event == null || !event.getCollege().getCid().equals(college.getCid())) {
            return ResponseEntity.status(404).body(Map.of("error", "Event not found or not owned by this college"));
        }
        eventRepository.delete(event);
        return ResponseEntity.ok(Map.of("message", "Event deleted successfully"));
    }

    @PostMapping("/{eid}/poster")
    @Operation(summary = "Upload event poster")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Event poster uploaded successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden: Only colleges can upload event posters"),
        @ApiResponse(responseCode = "404", description = "Event not found or not owned by this college")
    })
    public ResponseEntity<?> uploadEventPoster(@AuthenticationPrincipal User user, 
                                             @PathVariable Integer eid, 
                                             @RequestParam("file") MultipartFile file) {
        if (user == null || user.getRole() != User.Role.College) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden: Only colleges can upload event posters"));
        }
        
        College college = getCollegeForUser(user);
        if (college == null) return ResponseEntity.notFound().build();
        
        Event event = eventRepository.findById(eid).orElse(null);
        if (event == null || !event.getCollege().getCid().equals(college.getCid())) {
            return ResponseEntity.status(404).body(Map.of("error", "Event not found or not owned by this college"));
        }
        
        try {
            // Delete old poster if exists
            if (event.getPosterUrl() != null) {
                fileStorageService.deleteFile(event.getPosterUrl());
            }
            
            // Store new poster
            String posterUrl = fileStorageService.storeEventPoster(file);
            String posterThumbnailUrl = posterUrl; // For now, use same image as thumbnail
            
            // Update event
            event.setPosterUrl(posterUrl);
            event.setPosterThumbnailUrl(posterThumbnailUrl);
            event.setPosterApproved(false); // Needs college approval
            eventRepository.save(event);
            
            return ResponseEntity.ok(Map.of(
                "message", "Event poster uploaded successfully",
                "posterUrl", posterUrl,
                "posterThumbnailUrl", posterThumbnailUrl
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to upload poster: " + e.getMessage()));
        }
    }

    @PostMapping("/{eid}/poster/approve")
    @Operation(summary = "Approve event poster")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Poster approved successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden: Only colleges can approve posters"),
        @ApiResponse(responseCode = "404", description = "Event not found or not owned by this college")
    })
    public ResponseEntity<?> approveEventPoster(@AuthenticationPrincipal User user, @PathVariable Integer eid) {
        if (user == null || user.getRole() != User.Role.College) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden: Only colleges can approve posters"));
        }
        College college = getCollegeForUser(user);
        if (college == null) return ResponseEntity.notFound().build();
        Event event = eventRepository.findById(eid).orElse(null);
        if (event == null || !event.getCollege().getCid().equals(college.getCid())) {
            return ResponseEntity.status(404).body(Map.of("error", "Event not found or not owned by this college"));
        }
        event.setPosterApproved(true);
        eventRepository.save(event);
        return ResponseEntity.ok(Map.of("message", "Poster approved successfully"));
    }

    @PostMapping("/{eid}/poster/reject")
    @Operation(summary = "Reject event poster")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Poster rejected"),
        @ApiResponse(responseCode = "403", description = "Forbidden: Only colleges can reject posters"),
        @ApiResponse(responseCode = "404", description = "Event not found or not owned by this college")
    })
    public ResponseEntity<?> rejectEventPoster(@AuthenticationPrincipal User user, @PathVariable Integer eid, @RequestParam String reason) {
        if (user == null || user.getRole() != User.Role.College) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden: Only colleges can reject posters"));
        }
        College college = getCollegeForUser(user);
        if (college == null) return ResponseEntity.notFound().build();
        Event event = eventRepository.findById(eid).orElse(null);
        if (event == null || !event.getCollege().getCid().equals(college.getCid())) {
            return ResponseEntity.status(404).body(Map.of("error", "Event not found or not owned by this college"));
        }
        event.setPosterApproved(false);
        eventRepository.save(event);
        return ResponseEntity.ok(Map.of("message", "Poster rejected", "reason", reason));
    }

    @DeleteMapping("/{eid}/poster")
    @Operation(summary = "Delete event poster")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Poster deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden: Only colleges can delete posters"),
        @ApiResponse(responseCode = "404", description = "Event not found or not owned by this college")
    })
    public ResponseEntity<?> deleteEventPoster(@AuthenticationPrincipal User user, @PathVariable Integer eid) {
        if (user == null || user.getRole() != User.Role.College) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden: Only colleges can delete posters"));
        }
        College college = getCollegeForUser(user);
        if (college == null) return ResponseEntity.notFound().build();
        Event event = eventRepository.findById(eid).orElse(null);
        if (event == null || !event.getCollege().getCid().equals(college.getCid())) {
            return ResponseEntity.status(404).body(Map.of("error", "Event not found or not owned by this college"));
        }
        event.setPosterUrl(null);
        event.setPosterThumbnailUrl(null);
        event.setPosterApproved(false);
        eventRepository.save(event);
        return ResponseEntity.ok(Map.of("message", "Poster deleted successfully"));
    }

    @GetMapping("/{eid}/poster/audit-logs")
    @Operation(summary = "Get event poster audit logs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden: Only colleges can view audit logs"),
        @ApiResponse(responseCode = "404", description = "Event not found or not owned by this college")
    })
    public ResponseEntity<?> getEventPosterAuditLogs(@AuthenticationPrincipal User user, @PathVariable Integer eid) {
        if (user == null || user.getRole() != User.Role.College) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden: Only colleges can view audit logs"));
        }
        College college = getCollegeForUser(user);
        if (college == null) return ResponseEntity.notFound().build();
        Event event = eventRepository.findById(eid).orElse(null);
        if (event == null || !event.getCollege().getCid().equals(college.getCid())) {
            return ResponseEntity.status(404).body(Map.of("error", "Event not found or not owned by this college"));
        }
        // Return audit log information
        return ResponseEntity.ok(Map.of(
            "eventId", eid,
            "posterUrl", event.getPosterUrl(),
            "posterApproved", event.isPosterApproved(),
            "lastUpdated", System.currentTimeMillis()
        ));
    }

    private boolean isDateWithinRange(String eventDate, String startDate, String endDate) {
        try {
            LocalDate event = LocalDate.parse(eventDate);
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            return !event.isBefore(start) && !event.isAfter(end);
        } catch (Exception e) {
            return false;
        }
    }
} 