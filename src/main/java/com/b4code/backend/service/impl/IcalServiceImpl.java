package com.b4code.backend.service.impl;

import com.b4code.backend.dao.*;
import com.b4code.backend.dto.owner.IcalSyncDto;
import com.b4code.backend.dto.owner.IcalSyncRequest;
import com.b4code.backend.exceptions.CustomException;
import com.b4code.backend.models.*;
import com.b4code.backend.service.IcalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class IcalServiceImpl implements IcalService {

    private final IcalSyncRepository icalSyncRepository;
    private final PropertyRepository propertyRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final BookingRepository bookingRepository;
    private final ReservationRestrictionRepository restrictionRepository;
    private final UserRepository userRepository;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    private static final DateTimeFormatter BASIC_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter UTC_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    @Override
    @Transactional(readOnly = true)
    public String generateIcsFeed(Long propertyId, Long roomTypeId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new CustomException("Property not found", HttpStatus.NOT_FOUND));

        RoomType roomType = null;
        if (roomTypeId != null) {
            roomType = roomTypeRepository.findById(roomTypeId)
                    .orElseThrow(() -> new CustomException("Room type not found", HttpStatus.NOT_FOUND));
        }

        String calName = property.getName() + (roomType != null ? " - " + roomType.getName() : " - All Rooms");
        StringBuilder sb = new StringBuilder();
        String nowUtc = LocalDateTime.now().format(UTC_TIMESTAMP);

        sb.append("BEGIN:VCALENDAR\r\n");
        sb.append("VERSION:2.0\r\n");
        sb.append("PRODID:-//PrimeStay SDP//Property Calendar 1.0//EN\r\n");
        sb.append("CALSCALE:GREGORIAN\r\n");
        sb.append("METHOD:PUBLISH\r\n");
        sb.append("X-WR-CALNAME:").append(calName).append("\r\n");
        sb.append("X-WR-TIMEZONE:UTC\r\n");

        // 1. Confirmed Bookings
        LocalDate cutoff = LocalDate.now().minusDays(30);
        List<Booking> bookings = bookingRepository.findActiveBookingsForIcal(propertyId, roomTypeId, cutoff);
        for (Booking b : bookings) {
            sb.append("BEGIN:VEVENT\r\n");
            sb.append("UID:booking-").append(b.getId()).append("@primestay.com\r\n");
            sb.append("DTSTAMP:").append(nowUtc).append("\r\n");
            sb.append("DTSTART;VALUE=DATE:").append(b.getCheckIn().format(BASIC_DATE)).append("\r\n");
            sb.append("DTEND;VALUE=DATE:").append(b.getCheckOut().format(BASIC_DATE)).append("\r\n");
            sb.append("SUMMARY:Reserved (").append(b.getRoomType() != null ? b.getRoomType().getName() : "Room").append(")\r\n");
            sb.append("DESCRIPTION:Confirmation Code: ").append(b.getConfirmationCode()).append("\r\n");
            sb.append("STATUS:CONFIRMED\r\n");
            sb.append("END:VEVENT\r\n");
        }

        // 2. Active Blackouts & Restrictions
        List<ReservationRestriction> restrictions = restrictionRepository.findByPropertyIdOrderByStartDateDesc(propertyId);
        for (ReservationRestriction r : restrictions) {
            if (Boolean.TRUE.equals(r.getIsActive())) {
                if (roomTypeId != null && r.getRoomType() != null && !r.getRoomType().getId().equals(roomTypeId)) {
                    continue; // Skip if restricted for a different room
                }
                sb.append("BEGIN:VEVENT\r\n");
                sb.append("UID:restriction-").append(r.getId()).append("@primestay.com\r\n");
                sb.append("DTSTAMP:").append(nowUtc).append("\r\n");
                sb.append("DTSTART;VALUE=DATE:").append(r.getStartDate().format(BASIC_DATE)).append("\r\n");
                // iCal DTEND is exclusive
                LocalDate endInclusive = r.getEndDate() != null ? r.getEndDate().plusDays(1) : r.getStartDate().plusDays(1);
                sb.append("DTEND;VALUE=DATE:").append(endInclusive.format(BASIC_DATE)).append("\r\n");
                sb.append("SUMMARY:").append(r.getName() != null ? r.getName() : "Blocked").append("\r\n");
                sb.append("DESCRIPTION:").append(r.getReason() != null ? r.getReason() : "Date blocked by host").append("\r\n");
                sb.append("STATUS:CONFIRMED\r\n");
                sb.append("END:VEVENT\r\n");
            }
        }

        sb.append("END:VCALENDAR\r\n");
        return sb.toString();
    }

    @Override
    @Transactional(readOnly = true)
    public List<IcalSyncDto> getSyncFeeds(String ownerEmail, Long propertyId) {
        verifyOwner(ownerEmail, propertyId);
        return icalSyncRepository.findByPropertyIdOrderByCreatedAtDesc(propertyId)
                .stream()
                .map(s -> IcalSyncDto.fromEntity(s, frontendUrl))
                .toList();
    }

    @Override
    @Transactional
    public IcalSyncDto addSyncFeed(String ownerEmail, IcalSyncRequest request) {
        Property property = verifyOwner(ownerEmail, request.getPropertyId());

        URI validatedUri = validateAndSanitizeFeedUri(request.getFeedUrl());

        RoomType roomType = null;
        if (request.getRoomTypeId() != null) {
            roomType = roomTypeRepository.findById(request.getRoomTypeId())
                    .orElseThrow(() -> new CustomException("Room type not found", HttpStatus.NOT_FOUND));
        }

        PropertyIcalSync sync = PropertyIcalSync.builder()
                .property(property)
                .roomType(roomType)
                .channelName(request.getChannelName() != null ? request.getChannelName().trim() : "External Channel")
                .feedUrl(validatedUri.toString())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .syncStatus("PENDING")
                .eventsImported(0)
                .build();

        PropertyIcalSync saved = icalSyncRepository.save(sync);

        // Attempt immediate initial sync
        try {
            executeFeedSync(saved);
        } catch (Exception ex) {
            log.warn("Initial sync attempt failed for feed {}: {}", saved.getId(), ex.getMessage());
        }

        return IcalSyncDto.fromEntity(saved, frontendUrl);
    }

    @Override
    @Transactional
    public IcalSyncDto syncFeedNow(String ownerEmail, Long syncId) {
        PropertyIcalSync sync = icalSyncRepository.findById(syncId)
                .orElseThrow(() -> new CustomException("Sync feed not found", HttpStatus.NOT_FOUND));
        verifyOwner(ownerEmail, sync.getProperty().getId());

        executeFeedSync(sync);
        return IcalSyncDto.fromEntity(sync, frontendUrl);
    }

    @Override
    @Transactional
    public void deleteSyncFeed(String ownerEmail, Long syncId) {
        PropertyIcalSync sync = icalSyncRepository.findById(syncId)
                .orElseThrow(() -> new CustomException("Sync feed not found", HttpStatus.NOT_FOUND));
        verifyOwner(ownerEmail, sync.getProperty().getId());

        // Remove restrictions associated with this feed
        String prefix = "[" + sync.getChannelName() + " Sync]";
        List<ReservationRestriction> list = restrictionRepository.findByPropertyIdOrderByStartDateDesc(sync.getProperty().getId());
        for (ReservationRestriction r : list) {
            if (r.getName() != null && r.getName().startsWith(prefix)) {
                restrictionRepository.delete(r);
            }
        }

        icalSyncRepository.delete(sync);
    }

    private static final Set<String> ALLOWED_HOSTS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "airbnb.com",
            "www.airbnb.com",
            "ical.airbnb.com",
            "abnb.me",
            "www.abnb.me",
            "booking.com",
            "www.booking.com",
            "admin.booking.com",
            "partner.booking.com",
            "ical.booking.com",
            "calendar.google.com",
            "google.com",
            "www.google.com",
            "vrbo.com",
            "www.vrbo.com",
            "homeaway.com",
            "www.homeaway.com",
            "tripadvisor.com",
            "www.tripadvisor.com",
            "expedia.com",
            "www.expedia.com",
            "agoda.com",
            "www.agoda.com",
            "hotels.com",
            "www.hotels.com",
            "icalendar.org",
            "www.icalendar.org"
    )));

    /**
     * Validates an external iCal feed URL to prevent Server-Side Request Forgery (SSRF) attacks.
     * Enforces HTTPS/HTTP scheme, trusted provider domain allowlist, and rejects internal/loopback/private IPs.
     */
    private URI validateAndSanitizeFeedUri(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            throw new CustomException("Feed URL cannot be empty", HttpStatus.BAD_REQUEST);
        }

        URI uri;
        try {
            uri = URI.create(rawUrl.trim());
        } catch (IllegalArgumentException e) {
            throw new CustomException("Invalid URL syntax: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        String scheme = uri.getScheme();
        if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
            throw new CustomException("Invalid URL scheme. Only HTTP and HTTPS are permitted.", HttpStatus.BAD_REQUEST);
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new CustomException("Feed URL must specify a valid host.", HttpStatus.BAD_REQUEST);
        }

        String lowerHost = host.toLowerCase(Locale.ROOT);

        // Disallow localhost or internal network domain names
        if (lowerHost.equals("localhost") || lowerHost.endsWith(".localhost") 
                || lowerHost.endsWith(".internal") || lowerHost.endsWith(".local")) {
            throw new CustomException("Internal network hosts are not permitted.", HttpStatus.BAD_REQUEST);
        }

        // Whitelist check for authorized calendar providers to prevent SSRF
        if (!ALLOWED_HOSTS.contains(lowerHost)) {
            throw new CustomException("Host '" + host + "' is not an authorized iCal calendar provider. Supported providers include Airbnb, Booking.com, VRBO, Google Calendar, Expedia, and TripAdvisor.", HttpStatus.BAD_REQUEST);
        }

        // Validate port if explicitly specified: only standard web ports allowed
        int port = uri.getPort();
        if (port != -1 && port != 80 && port != 443 && port != 8080 && port != 8443) {
            throw new CustomException("Port " + port + " is not permitted for iCal feeds.", HttpStatus.BAD_REQUEST);
        }

        // Resolve DNS and ensure the destination is not loopback, private, link-local, or multicast (SSRF prevention)
        try {
            InetAddress[] addresses = InetAddress.getAllByName(lowerHost);
            for (InetAddress addr : addresses) {
                if (addr.isLoopbackAddress() || addr.isSiteLocalAddress() 
                        || addr.isLinkLocalAddress() || addr.isAnyLocalAddress() 
                        || addr.isMulticastAddress()) {
                    throw new CustomException("Access to internal/private IP addresses is forbidden.", HttpStatus.BAD_REQUEST);
                }
            }
        } catch (UnknownHostException e) {
            throw new CustomException("Could not resolve host: " + lowerHost, HttpStatus.BAD_REQUEST);
        }

        // Return sanitized URI
        try {
            return new URI(
                    scheme.toLowerCase(Locale.ROOT),
                    null,
                    lowerHost,
                    uri.getPort(),
                    uri.getPath(),
                    uri.getQuery(),
                    null
            );
        } catch (Exception e) {
            throw new CustomException("Error constructing sanitized feed URI: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private void executeFeedSync(PropertyIcalSync sync) {
        try {
            URI validatedUri = validateAndSanitizeFeedUri(sync.getFeedUrl());

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .followRedirects(HttpClient.Redirect.NEVER)
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(validatedUri)
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", "PrimeStay-iCal-Sync/1.0")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                sync.setSyncStatus("FAILED");
                sync.setSyncError("Remote server returned HTTP " + response.statusCode());
                sync.setLastSyncAt(LocalDateTime.now());
                icalSyncRepository.save(sync);
                return;
            }

            String body = response.body();
            int imported = parseAndApplyIcalEvents(body, sync);

            sync.setSyncStatus("SUCCESS");
            sync.setSyncError(null);
            sync.setEventsImported(imported);
            sync.setLastSyncAt(LocalDateTime.now());
            icalSyncRepository.save(sync);

        } catch (Exception ex) {
            log.error("Failed to sync iCal feed ID {}: {}", sync.getId(), ex.getMessage());
            sync.setSyncStatus("FAILED");
            sync.setSyncError(ex.getMessage() != null ? ex.getMessage() : "Connection failed");
            sync.setLastSyncAt(LocalDateTime.now());
            icalSyncRepository.save(sync);
        }
    }

    private int parseAndApplyIcalEvents(String icsContent, PropertyIcalSync sync) {
        // Clear previous synced restrictions for this channel tag
        String prefix = "[" + sync.getChannelName() + " Sync]";
        List<ReservationRestriction> existing = restrictionRepository.findByPropertyIdOrderByStartDateDesc(sync.getProperty().getId());
        for (ReservationRestriction r : existing) {
            if (r.getName() != null && r.getName().startsWith(prefix)) {
                restrictionRepository.delete(r);
            }
        }

        int count = 0;
        Pattern eventPattern = Pattern.compile("BEGIN:VEVENT([\\s\\S]*?)END:VEVENT", Pattern.CASE_INSENSITIVE);
        Matcher matcher = eventPattern.matcher(icsContent);

        Pattern dtstartPattern = Pattern.compile("DTSTART(?:;[^:]+)?:(\\d{8}(?:T\\d{6}Z?)?)", Pattern.CASE_INSENSITIVE);
        Pattern dtendPattern = Pattern.compile("DTEND(?:;[^:]+)?:(\\d{8}(?:T\\d{6}Z?)?)", Pattern.CASE_INSENSITIVE);
        Pattern summaryPattern = Pattern.compile("SUMMARY:(.*)", Pattern.CASE_INSENSITIVE);

        LocalDate today = LocalDate.now();

        while (matcher.find()) {
            String block = matcher.group(1);

            Matcher mStart = dtstartPattern.matcher(block);
            Matcher mEnd = dtendPattern.matcher(block);
            Matcher mSum = summaryPattern.matcher(block);

            if (mStart.find()) {
                String rawStart = mStart.group(1).substring(0, 8);
                LocalDate startDate = LocalDate.parse(rawStart, BASIC_DATE);

                LocalDate endDate = startDate;
                if (mEnd.find()) {
                    String rawEnd = mEnd.group(1).substring(0, 8);
                    // iCal DTEND is exclusive, so inclusive end date is minus 1 day
                    LocalDate parsedEnd = LocalDate.parse(rawEnd, BASIC_DATE);
                    endDate = parsedEnd.isAfter(startDate) ? parsedEnd.minusDays(1) : startDate;
                }

                // Only import current or future events
                if (endDate.isBefore(today.minusDays(7))) {
                    continue;
                }

                String summary = mSum.find() ? mSum.group(1).trim() : "External Booking";

                ReservationRestriction r = ReservationRestriction.builder()
                        .property(sync.getProperty())
                        .roomType(sync.getRoomType())
                        .name(prefix + " " + summary)
                        .type("BLACKOUT")
                        .startDate(startDate)
                        .endDate(endDate)
                        .reason("Auto-locked via " + sync.getChannelName() + " iCal synchronization")
                        .isActive(true)
                        .build();

                restrictionRepository.save(r);
                count++;
            }
        }
        return count;
    }

    private Property verifyOwner(String email, Long propertyId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new CustomException("Property not found", HttpStatus.NOT_FOUND));

        if (!user.getId().equals(property.getOwnerId())) {
            throw new CustomException("Access denied: You do not own this property", HttpStatus.FORBIDDEN);
        }
        return property;
    }
}
