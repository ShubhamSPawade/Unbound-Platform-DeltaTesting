package com.unbound.backend.controller;

import com.unbound.backend.dto.EventRegistrationRequest;
import com.unbound.backend.dto.RegistrationResponse;
import com.unbound.backend.entity.*;
import com.unbound.backend.repository.*;
import com.unbound.backend.service.EmailService;
import com.unbound.backend.service.StudentDashboardService;
import com.unbound.backend.service.CertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import jakarta.persistence.EntityNotFoundException;
import com.unbound.backend.exception.RegistrationClosedException;
import com.unbound.backend.exception.StudentNotFoundException;
import com.unbound.backend.exception.EventNotFoundException;
import com.unbound.backend.exception.ForbiddenActionException;

@RestController
@RequestMapping("/api/student/events")
@Tag(name = "Student Event APIs", description = "APIs for student event operations (Student access required)")
@SecurityRequirement(name = "bearerAuth")
public class StudentEventController {
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private EventRegistrationRepository eventRegistrationRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private TeamMembersRepository teamMembersRepository;
    @Autowired
    private EventReviewRepository eventReviewRepository;
    @Autowired
    private StudentDashboardService studentDashboardService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private CertificateService certificateService;

    private Student getStudentForUser(User user) {
        return studentRepository.findAll().stream()
                .filter(s -> s.getUser().getUid().equals(user.getUid()))
                .findFirst().orElse(null);
    }

    @PostMapping("/register")
    @Operation(summary = "Register for an event", description = "Allows students to register for a specific event.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Registration successful"),
        @ApiResponse(responseCode = "400", description = "Invalid registration type, deadline passed, or event full"),
        @ApiResponse(responseCode = "403", description = "Only students can register"),
        @ApiResponse(responseCode = "404", description = "Event or Student not found")
    })
    public ResponseEntity<?> registerForEvent(@AuthenticationPrincipal User user, @RequestBody EventRegistrationRequest req) {
        if (user == null || user.getRole() != User.Role.Student) {
            throw new ForbiddenActionException("Only students can register for events.");
        }
        Student student = getStudentForUser(user);
        if (student == null) throw new StudentNotFoundException("Student not found.");
        Long eventId = req.getEventId();
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException("Event not found"));
        
        // Check if event is approved and active
        if (!event.isApproved() || !event.isActive()) {
            throw new EventNotFoundException("Event is not available for registration.");
        }
        
        // Check if registration is open
        boolean registrationClosed = !event.isRegistrationOpen();
        if (registrationClosed) {
            throw new RegistrationClosedException("Registration for this event is closed.");
        }
        
        // Check registration deadline
        try {
            LocalDate deadline = LocalDate.parse(event.getRegistrationDeadline());
            if (LocalDate.now().isAfter(deadline)) {
                throw new RegistrationClosedException("Registration deadline has passed.");
            }
        } catch (Exception e) {
            throw new RegistrationClosedException("Invalid registration deadline.");
        }
        
        // Check for duplicate registration
        if (eventRegistrationRepository.findByEventAndStudent(event, student).isPresent()) {
            throw new RegistrationClosedException("Already registered for this event.");
        }
        
        // Check event capacity
        long regCount = eventRegistrationRepository.findByEvent(event).stream().count();
        if (regCount >= event.getCapacity()) {
            throw new RegistrationClosedException("Event is full.");
        }
        
        // Solo registration
        if ("solo".equalsIgnoreCase(req.getRegistrationType())) {
            if (event.getTeamIsAllowed()) {
                throw new EntityNotFoundException("This event requires team registration.");
            }
            EventRegistration registration = EventRegistration.builder()
                    .event(event)
                    .student(student)
                    .erdateTime(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_DATE_TIME))
                    .status("registered")
                    .paymentStatus(event.getFees() > 0 ? "pending" : "paid")
                    .build();
            eventRegistrationRepository.save(registration);
            
            // Generate receipt number
            String receiptNumber = "RCP" + System.currentTimeMillis();
            
            // Send detailed registration confirmation email
            String emailBody = generateRegistrationEmailBody(student, event, registration, receiptNumber, "solo", null);
            emailService.sendEmail(
                student.getUser().getEmail(),
                "Registration Confirmation - " + event.getEname(),
                emailBody
            );
            
            // Create response
            RegistrationResponse response = RegistrationResponse.builder()
                    .registrationId(registration.getRid())
                    .eventName(event.getEname())
                    .eventDate(event.getEventDate())
                    .eventLocation(event.getLocation())
                    .fees(event.getFees())
                    .registrationType("solo")
                    .teamName(null)
                    .registrationStatus(registration.getStatus())
                    .paymentStatus(registration.getPaymentStatus())
                    .registrationDateTime(registration.getErdateTime())
                    .studentName(student.getSname())
                    .studentEmail(student.getUser().getEmail())
                    .collegeName(event.getCollege().getCname())
                    .festName(event.getFest() != null ? event.getFest().getFname() : null)
                    .cashPrize(event.getCashPrize())
                    .firstPrize(event.getFirstPrize())
                    .secondPrize(event.getSecondPrize())
                    .thirdPrize(event.getThirdPrize())
                    .registrationDeadline(event.getRegistrationDeadline())
                    .daysLeft((int) java.time.temporal.ChronoUnit.DAYS.between(
                        java.time.LocalDate.now(), 
                        java.time.LocalDate.parse(event.getEventDate())
                    ))
                    .receiptNumber(receiptNumber)
                    .message("Registration successful! Check your email for receipt.")
                    .success(true)
                    .build();
            
            return ResponseEntity.ok(response);
        }
        
        // Team registration
        if ("team".equalsIgnoreCase(req.getRegistrationType())) {
            if (!event.getTeamIsAllowed()) {
                throw new EntityNotFoundException("This event does not allow team registration.");
            }
            Team team = null;
            // Join existing team
            if (req.getTeamId() != null) {
                Long teamId = req.getTeamId();
                Optional<Team> teamOpt = teamRepository.findById(teamId);
                if (teamOpt.isEmpty()) throw new EntityNotFoundException("Team not found.");
                team = teamOpt.get();
                // Check if already a member
                if (teamMembersRepository.findByTeamAndStudent(team, student).isPresent()) {
                    throw new EntityNotFoundException("Already a member of this team.");
                }
                // Add to team
                TeamMembers teamMember = TeamMembers.builder()
                        .team(team)
                        .student(student)
                        .build();
                teamMembersRepository.save(teamMember);
            } else {
                // Create new team
                team = Team.builder()
                        .event(event)
                        .creator(student)
                        .tname(req.getTeamName())
                        .build();
                team = teamRepository.save(team);
                // Add creator as member
                TeamMembers teamMember = TeamMembers.builder()
                        .team(team)
                        .student(student)
                        .build();
                teamMembersRepository.save(teamMember);
            }
            
            EventRegistration registration = EventRegistration.builder()
                    .event(event)
                    .student(student)
                    .team(team)
                    .erdateTime(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_DATE_TIME))
                    .status("registered")
                    .paymentStatus(event.getFees() > 0 ? "pending" : "paid")
                    .build();
            eventRegistrationRepository.save(registration);
            
            // Generate receipt number
            String receiptNumber = "RCP" + System.currentTimeMillis();
            
            // Send detailed registration confirmation email
            String emailBody = generateRegistrationEmailBody(student, event, registration, receiptNumber, "team", team);
            emailService.sendEmail(
                student.getUser().getEmail(),
                "Registration Confirmation - " + event.getEname(),
                emailBody
            );
            
            // Create response
            RegistrationResponse response = RegistrationResponse.builder()
                    .registrationId(registration.getRid())
                    .eventName(event.getEname())
                    .eventDate(event.getEventDate())
                    .eventLocation(event.getLocation())
                    .fees(event.getFees())
                    .registrationType("team")
                    .teamName(team.getTname())
                    .registrationStatus(registration.getStatus())
                    .paymentStatus(registration.getPaymentStatus())
                    .registrationDateTime(registration.getErdateTime())
                    .studentName(student.getSname())
                    .studentEmail(student.getUser().getEmail())
                    .collegeName(event.getCollege().getCname())
                    .festName(event.getFest() != null ? event.getFest().getFname() : null)
                    .cashPrize(event.getCashPrize())
                    .firstPrize(event.getFirstPrize())
                    .secondPrize(event.getSecondPrize())
                    .thirdPrize(event.getThirdPrize())
                    .registrationDeadline(event.getRegistrationDeadline())
                    .daysLeft((int) java.time.temporal.ChronoUnit.DAYS.between(
                        java.time.LocalDate.now(), 
                        java.time.LocalDate.parse(event.getEventDate())
                    ))
                    .receiptNumber(receiptNumber)
                    .message("Team registration successful! Check your email for receipt.")
                    .success(true)
                    .build();
            
            return ResponseEntity.ok(response);
        }
        
        throw new EntityNotFoundException("Invalid registration type.");
    }

    private String generateRegistrationEmailBody(Student student, Event event, EventRegistration registration, String receiptNumber, String registrationType, Team team) {
        StringBuilder emailBody = new StringBuilder();
        emailBody.append("Dear ").append(student.getSname()).append(",\n\n");
        emailBody.append("Thank you for registering for the event!\n\n");
        
        emailBody.append("=== REGISTRATION RECEIPT ===\n");
        emailBody.append("Receipt Number: ").append(receiptNumber).append("\n");
        emailBody.append("Registration Date: ").append(registration.getErdateTime()).append("\n");
        emailBody.append("Registration Type: ").append(registrationType.toUpperCase()).append("\n\n");
        
        emailBody.append("=== EVENT DETAILS ===\n");
        emailBody.append("Event Name: ").append(event.getEname()).append("\n");
        emailBody.append("Event Date: ").append(event.getEventDate()).append("\n");
        emailBody.append("Event Time: ").append(event.getEventDate()).append("\n");
        emailBody.append("Location: ").append(event.getLocation()).append("\n");
        emailBody.append("Category: ").append(event.getCategory()).append("\n");
        emailBody.append("Mode: ").append(event.getMode()).append("\n");
        emailBody.append("Entry Fee: ₹").append(event.getFees()).append("\n");
        emailBody.append("Registration Deadline: ").append(event.getRegistrationDeadline()).append("\n\n");
        
        if (event.getCashPrize() != null && !event.getCashPrize().isEmpty()) {
            emailBody.append("=== PRIZES ===\n");
            emailBody.append("Cash Prize: ").append(event.getCashPrize()).append("\n");
            if (event.getFirstPrize() != null) emailBody.append("1st Prize: ").append(event.getFirstPrize()).append("\n");
            if (event.getSecondPrize() != null) emailBody.append("2nd Prize: ").append(event.getSecondPrize()).append("\n");
            if (event.getThirdPrize() != null) emailBody.append("3rd Prize: ").append(event.getThirdPrize()).append("\n");
            emailBody.append("\n");
        }
        
        emailBody.append("=== ORGANIZER DETAILS ===\n");
        emailBody.append("College: ").append(event.getCollege().getCname()).append("\n");
        if (event.getFest() != null) {
            emailBody.append("Fest: ").append(event.getFest().getFname()).append("\n");
        }
        if (event.getOrganizerName() != null) {
            emailBody.append("Organizer: ").append(event.getOrganizerName()).append("\n");
        }
        if (event.getOrganizerEmail() != null) {
            emailBody.append("Organizer Email: ").append(event.getOrganizerEmail()).append("\n");
        }
        if (event.getOrganizerPhone() != null) {
            emailBody.append("Organizer Phone: ").append(event.getOrganizerPhone()).append("\n");
        }
        emailBody.append("\n");
        
        if ("team".equals(registrationType) && team != null) {
            emailBody.append("=== TEAM DETAILS ===\n");
            emailBody.append("Team Name: ").append(team.getTname()).append("\n");
            emailBody.append("Team Creator: ").append(team.getCreator().getSname()).append("\n");
            emailBody.append("\n");
        }
        
        if (event.getRules() != null && !event.getRules().isEmpty()) {
            emailBody.append("=== EVENT RULES ===\n");
            emailBody.append(event.getRules()).append("\n\n");
        }
        
        if (event.getRequirements() != null && !event.getRequirements().isEmpty()) {
            emailBody.append("=== EVENT REQUIREMENTS ===\n");
            emailBody.append(event.getRequirements()).append("\n\n");
        }
        
        emailBody.append("=== PAYMENT STATUS ===\n");
        emailBody.append("Status: ").append(registration.getPaymentStatus().toUpperCase()).append("\n");
        if ("pending".equalsIgnoreCase(registration.getPaymentStatus()) && event.getFees() > 0) {
            emailBody.append("Please complete your payment to confirm your registration.\n");
        }
        emailBody.append("\n");
        
        emailBody.append("=== IMPORTANT NOTES ===\n");
        emailBody.append("- Please arrive 15 minutes before the event start time\n");
        emailBody.append("- Bring your college ID card for verification\n");
        emailBody.append("- Check your email for any updates or changes\n");
        emailBody.append("- Contact the organizer if you have any questions\n\n");
        
        emailBody.append("We look forward to seeing you at the event!\n\n");
        emailBody.append("Best regards,\n");
        emailBody.append("Unbound Platform Team\n");
        emailBody.append("Email: support@unbound.com\n");
        emailBody.append("Phone: +91-XXXXXXXXXX\n");
        
        return emailBody.toString();
    }

    @GetMapping("/my")
    @Operation(summary = "Get my registered events", description = "Retrieves all events a student has registered for.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved registrations"),
        @ApiResponse(responseCode = "403", description = "Only students can view their registrations"),
        @ApiResponse(responseCode = "404", description = "Student not found")
    })
    public ResponseEntity<?> myRegistrations(@AuthenticationPrincipal User user) {
        if (user == null || user.getRole() != User.Role.Student) {
            throw new EntityNotFoundException("Only students can view their registrations.");
        }
        Student student = getStudentForUser(user);
        if (student == null) throw new EntityNotFoundException("Student not found.");
        return ResponseEntity.ok(studentDashboardService.getMyRegistrations(student));
    }

    @GetMapping("/dashboard/stats")
    @Operation(summary = "Get student dashboard statistics", description = "Retrieves various statistics for the authenticated student.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved dashboard stats"),
        @ApiResponse(responseCode = "403", description = "Only students can view dashboard stats"),
        @ApiResponse(responseCode = "404", description = "Student not found")
    })
    public ResponseEntity<?> getStudentDashboardStats(@AuthenticationPrincipal User user) {
        if (user == null || user.getRole() != User.Role.Student) {
            throw new EntityNotFoundException("Only students can view dashboard stats.");
        }
        Student student = getStudentForUser(user);
        if (student == null) throw new EntityNotFoundException("Student not found.");
        return ResponseEntity.ok(studentDashboardService.getStudentDashboardStats(student));
    }

    @GetMapping("/{eventId}/certificate")
    @Operation(summary = "Download event certificate", description = "Allows students to download their event certificate if they are a registered and paid participant.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Certificate downloaded successfully"),
        @ApiResponse(responseCode = "403", description = "Only students can download certificates or not a registered and paid participant"),
        @ApiResponse(responseCode = "404", description = "Event or Student not found")
    })
    public ResponseEntity<?> downloadCertificate(@AuthenticationPrincipal User user, @PathVariable("eventId") Long eventId) {
        if (user == null || user.getRole() != User.Role.Student) {
            throw new EntityNotFoundException("Only students can download certificates.");
        }
        Student student = getStudentForUser(user);
        if (student == null) throw new EntityNotFoundException("Student not found.");
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("Event not found"));
        Optional<EventRegistration> regOpt = eventRegistrationRepository.findByEventAndStudent(event, student);
        if (regOpt.isEmpty() || (!"paid".equalsIgnoreCase(regOpt.get().getPaymentStatus()) && event.getFees() > 0)) {
            throw new EntityNotFoundException("You must be a registered and paid participant to download certificate.");
        }
        if (!regOpt.get().isCertificateApproved()) {
            throw new EntityNotFoundException("Certificate not yet approved by college.");
        }
        // Only after event is completed
        try {
            if (!java.time.LocalDate.now().isAfter(java.time.LocalDate.parse(event.getEventDate()))) {
                throw new EntityNotFoundException("Certificate available only after event completion.");
            }
        } catch (Exception e) {
            throw new EntityNotFoundException("Invalid event date for event ID " + eventId + ": " + e.getMessage());
        }
        try {
            byte[] pdf = certificateService.generateCertificate(
                student.getSname(),
                event.getEname(),
                event.getFest() != null ? event.getFest().getFname() : null,
                event.getEventDate()
            );
            return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=certificate.pdf")
                .body(pdf);
        } catch (Exception e) {
            throw new EntityNotFoundException("Failed to generate certificate for student ID " + student.getSid() + ", event ID " + eventId + ": " + e.getMessage());
        }
    }
} 