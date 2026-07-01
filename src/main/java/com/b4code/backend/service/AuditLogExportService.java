package com.b4code.backend.service;

import com.b4code.backend.dao.AuditLogRepository;
import com.b4code.backend.dto.AuditLogDto;
import com.b4code.backend.models.AuditLog;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogExportService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public List<AuditLogDto> getFilteredLogs(String role, String search) {
        String filterRole = (role != null && !role.equalsIgnoreCase("All")) ? role.toUpperCase() : null;
        String searchTerm = (search == null || search.isBlank()) ? null : search.trim();

        Page<AuditLog> result = auditLogRepository.findAllWithFilters(
                filterRole, searchTerm,
                org.springframework.data.domain.Pageable.unpaged()
        );
        return result.map(AuditLogDto::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public byte[] exportToCsv(String role, String search) {
        List<AuditLogDto> logs = getFilteredLogs(role, search);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStreamWriter osw = new OutputStreamWriter(baos);
             com.opencsv.ICSVWriter csvWriter = new com.opencsv.CSVWriterBuilder(osw).build()) {

            String[] header = {"User/Role", "Action", "Entity/Details", "Timestamp"};
            csvWriter.writeNext(header);

            for (AuditLogDto logDto : logs) {
                String[] data = {
                        logDto.getUserName() + " (" + logDto.getUserRole() + ")",
                        logDto.getAction(),
                        logDto.getEntity() + " - " + logDto.getEntityDetail(),
                        logDto.getTimestamp()
                };
                csvWriter.writeNext(data);
            }
            csvWriter.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating CSV", e);
            throw new RuntimeException("Could not generate CSV", e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] exportToPdf(String role, String search) {
        List<AuditLogDto> logs = getFilteredLogs(role, search);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Fonts
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

            // Title
            Paragraph title = new Paragraph("PrimeStay Audit Logs", headerFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);

            // Subtitle / Filters
            Paragraph subtitle = new Paragraph(
                    "Filters applied - Role: " + (role != null ? role : "All") +
                            ", Search: " + (search != null && !search.isBlank() ? search : "None"),
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, Color.DARK_GRAY));
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(20);
            document.add(subtitle);

            // Table
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2.5f, 1.5f, 3.5f, 2.5f});

            // Table Header
            String[] headers = {"User / Role", "Action", "Entity Details", "Timestamp"};
            Color headerBgColor = new Color(192, 86, 33); // #C05621
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, tableHeaderFont));
                cell.setBackgroundColor(headerBgColor);
                cell.setPadding(8);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(cell);
            }

            // Table Data
            boolean alternate = false;
            Color altBgColor = new Color(245, 245, 245);
            for (AuditLogDto logDto : logs) {
                String[] row = {
                        logDto.getUserName() + "\n" + logDto.getUserRole(),
                        logDto.getAction(),
                        logDto.getEntity() + "\n" + logDto.getEntityDetail(),
                        logDto.getTimestamp()
                };
                for (String cellData : row) {
                    PdfPCell cell = new PdfPCell(new Phrase(cellData != null ? cellData : "", cellFont));
                    cell.setPadding(6);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    if (alternate) {
                        cell.setBackgroundColor(altBgColor);
                    }
                    table.addCell(cell);
                }
                alternate = !alternate;
            }

            document.add(table);
            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating PDF", e);
            throw new RuntimeException("Could not generate PDF", e);
        }
    }
}
