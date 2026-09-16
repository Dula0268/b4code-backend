package com.b4code.backend.service;

import com.b4code.backend.dto.owner.OwnerReservationDto;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OwnerExportService {

    private final OwnerReservationService ownerReservationService;

    public byte[] exportReservationsToPdf(String ownerEmail, String search, String statusParam) {
        List<OwnerReservationDto> reservations = ownerReservationService.listReservations(ownerEmail, search, statusParam);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, baos);
            document.open();

            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
            Paragraph title = new Paragraph("Bookings & Reservations Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20f);
            document.add(title);

            // Table
            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);
            table.setWidths(new float[]{1f, 2f, 1.5f, 1.5f, 2f, 2f, 1.5f, 1.5f}); // Adjust column widths

            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE);
            Font tableBodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

            // Table Header
            String[] headers = {"ID", "Guest Name", "Check-in", "Check-out", "Room Type", "Property", "Status", "Payout"};
            Color headerBgColor = new Color(192, 86, 33); // #C05621
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, tableHeaderFont));
                cell.setBackgroundColor(headerBgColor);
                cell.setPadding(8);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            // Table Body
            for (OwnerReservationDto r : reservations) {
                String payout = r.getTotalAmount() != null ? "$" + r.getTotalAmount() : "N/A";
                String[] row = {
                        String.valueOf(r.getId()),
                        r.getGuestName(),
                        r.getCheckIn() != null ? r.getCheckIn() : "",
                        r.getCheckOut() != null ? r.getCheckOut() : "",
                        r.getRoomName(),
                        r.getPropertyName(),
                        r.getStatus() != null ? r.getStatus() : "",
                        payout
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
            log.error("Error generating Reservations PDF", e);
            throw new RuntimeException("Could not generate Reservations PDF", e);
        }
    }
}
