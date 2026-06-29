package com.b4code.backend.service;

import com.b4code.backend.dao.DisputeRepository;
import com.b4code.backend.dao.FlaggedReviewRepository;
import com.b4code.backend.dto.DisputeDto;
import com.b4code.backend.dto.FlaggedReviewDto;
import com.b4code.backend.models.Dispute;
import com.b4code.backend.models.FlaggedReview;
import com.b4code.backend.models.enums.DisputeStatus;
import com.b4code.backend.models.enums.FlagType;
import com.b4code.backend.models.enums.ReviewStatus;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ModerationExportService {

    private final FlaggedReviewRepository reviewRepository;
    private final DisputeRepository disputeRepository;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss");

    // ==========================================
    // REVIEWS EXPORT
    // ==========================================
    
    @Transactional(readOnly = true)
    public List<FlaggedReviewDto> getFlaggedReviewsUnpaged(FlagType flagType, Integer rating) {
        Page<FlaggedReview> result = reviewRepository.findAllWithFilters(
                null, flagType, rating, null, Pageable.unpaged());
        return result.map(FlaggedReviewDto::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public byte[] exportReviewsToCsv(FlagType flagType, Integer rating) {
        List<FlaggedReviewDto> logs = getFlaggedReviewsUnpaged(flagType, rating);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStreamWriter osw = new OutputStreamWriter(baos);
             com.opencsv.ICSVWriter csvWriter = new com.opencsv.CSVWriterBuilder(osw).build()) {

            String[] header = {"Flagged By", "Rating", "Property", "Content Snippet", "Flag Status", "Date"};
            csvWriter.writeNext(header);

            for (FlaggedReviewDto logDto : logs) {
                String[] data = {
                        logDto.getOwnerName() != null ? logDto.getOwnerName() : "System",
                        String.valueOf(logDto.getRating()),
                        logDto.getPropertyName() + " (ID: " + logDto.getPropertyId() + ")",
                        logDto.getReviewText(),
                        logDto.getFlagType(),
                        logDto.getFlaggedAt()
                };
                csvWriter.writeNext(data);
            }
            csvWriter.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating Reviews CSV", e);
            throw new RuntimeException("Could not generate Reviews CSV", e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] exportReviewsToPdf(FlagType flagType, Integer rating) {
        List<FlaggedReviewDto> logs = getFlaggedReviewsUnpaged(flagType, rating);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, baos);
            document.open();

            // Header Font
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, Color.DARK_GRAY);
            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE);
            Font tableBodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

            // Title
            Paragraph title = new Paragraph("PrimeStay Review Queue", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(5);
            document.add(title);

            // Subtitle
            String filterText = String.format("Filters applied - Flag Type: %s, Rating: %s",
                    flagType != null ? flagType.name() : "All",
                    rating != null ? rating + " Star(s)" : "Any");
            Paragraph subtitle = new Paragraph(filterText, subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(20);
            document.add(subtitle);

            // Table
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.5f, 1f, 2f, 3.5f, 1.5f, 1.5f});

            // Table Header
            String[] headers = {"Flagged By", "Rating", "Property", "Content Snippet", "Flag Status", "Date"};
            Color headerBgColor = new Color(192, 86, 33); // #C05621
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, tableHeaderFont));
                cell.setBackgroundColor(headerBgColor);
                cell.setPadding(8);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            // Table Body
            for (FlaggedReviewDto logDto : logs) {
                String[] row = {
                        logDto.getOwnerName() != null ? logDto.getOwnerName() : "System",
                        String.valueOf(logDto.getRating()),
                        logDto.getPropertyName() + "\nID: " + logDto.getPropertyId(),
                        logDto.getReviewText(),
                        logDto.getFlagType(),
                        logDto.getFlaggedAt()
                };
                for (String cellData : row) {
                    PdfPCell cell = new PdfPCell(new Phrase(cellData != null ? cellData : "", tableBodyFont));
                    cell.setPadding(6);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    table.addCell(cell);
                }
            }

            document.add(table);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating Reviews PDF", e);
            throw new RuntimeException("Could not generate Reviews PDF", e);
        }
    }

    // ==========================================
    // DISPUTES EXPORT
    // ==========================================

    @Transactional(readOnly = true)
    public List<DisputeDto> getDisputesUnpaged(DisputeStatus status, String search) {
        Page<Dispute> result = disputeRepository.findAllWithFilters(
                status, search, Pageable.unpaged());
        return result.map(DisputeDto::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public byte[] exportDisputesToCsv(DisputeStatus status, String search) {
        List<DisputeDto> logs = getDisputesUnpaged(status, search);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStreamWriter osw = new OutputStreamWriter(baos);
             com.opencsv.ICSVWriter csvWriter = new com.opencsv.CSVWriterBuilder(osw).build()) {

            String[] header = {"Dispute ID", "Guest", "Property", "Reason", "Amount", "Status"};
            csvWriter.writeNext(header);

            for (DisputeDto logDto : logs) {
                String[] data = {
                        logDto.getDisputeId(),
                        logDto.getGuestName(),
                        logDto.getPropertyName(),
                        logDto.getReason(),
                        logDto.getAmount(),
                        logDto.getStatus()
                };
                csvWriter.writeNext(data);
            }
            csvWriter.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating Disputes CSV", e);
            throw new RuntimeException("Could not generate Disputes CSV", e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] exportDisputesToPdf(DisputeStatus status, String search) {
        List<DisputeDto> logs = getDisputesUnpaged(status, search);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, baos);
            document.open();

            // Header Font
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, Color.DARK_GRAY);
            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE);
            Font tableBodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

            // Title
            Paragraph title = new Paragraph("PrimeStay Dispute Resolution Hub", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(5);
            document.add(title);

            // Subtitle
            String filterText = String.format("Filters applied - Status: %s, Search: %s",
                    status != null ? status.name() : "All Open",
                    search != null && !search.isEmpty() ? search : "None");
            Paragraph subtitle = new Paragraph(filterText, subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(20);
            document.add(subtitle);

            // Table
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.5f, 2f, 2f, 2f, 1f, 1.5f});

            // Table Header
            String[] headers = {"Dispute ID", "Guest", "Property", "Reason", "Amount", "Status"};
            Color headerBgColor = new Color(192, 86, 33); // #C05621
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, tableHeaderFont));
                cell.setBackgroundColor(headerBgColor);
                cell.setPadding(8);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            // Table Body
            for (DisputeDto logDto : logs) {
                String[] row = {
                        logDto.getDisputeId(),
                        logDto.getGuestName(),
                        logDto.getPropertyName(),
                        logDto.getReason(),
                        logDto.getAmount(),
                        logDto.getStatus()
                };
                for (String cellData : row) {
                    PdfPCell cell = new PdfPCell(new Phrase(cellData != null ? cellData : "", tableBodyFont));
                    cell.setPadding(6);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    table.addCell(cell);
                }
            }

            document.add(table);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating Disputes PDF", e);
            throw new RuntimeException("Could not generate Disputes PDF", e);
        }
    }
}
