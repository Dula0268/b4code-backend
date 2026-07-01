package com.b4code.backend.service;

import com.b4code.backend.dao.PayoutRepository;
import com.b4code.backend.dto.PayoutDto;
import com.b4code.backend.models.Payout;
import com.b4code.backend.models.enums.PayoutStatus;
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
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FinanceExportService {

    private final PayoutRepository payoutRepository;

    @Transactional(readOnly = true)
    public List<PayoutDto> getPayoutsUnpaged(PayoutStatus status, String search) {
        Page<Payout> result = payoutRepository.findAllWithFilters(
                status, search, Pageable.unpaged());
        return result.map(PayoutDto::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public byte[] exportPayoutsToCsv(PayoutStatus status, String search) {
        List<PayoutDto> payouts = getPayoutsUnpaged(status, search);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStreamWriter osw = new OutputStreamWriter(baos);
             com.opencsv.ICSVWriter csvWriter = new com.opencsv.CSVWriterBuilder(osw).build()) {

            String[] header = {"Payout ID", "Property", "Host", "Amount", "Status", "Period"};
            csvWriter.writeNext(header);

            for (PayoutDto p : payouts) {
                String[] data = {
                        String.valueOf(p.getId()),
                        p.getPropertyName(),
                        p.getHostName(),
                        String.valueOf(p.getAmount()),
                        p.getStatus() != null ? p.getStatus().name() : "N/A",
                        p.getPeriod()
                };
                csvWriter.writeNext(data);
            }
            csvWriter.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating Payouts CSV", e);
            throw new RuntimeException("Could not generate Payouts CSV", e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] exportPayoutsToPdf(PayoutStatus status, String search) {
        List<PayoutDto> payouts = getPayoutsUnpaged(status, search);
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
            Paragraph title = new Paragraph("PrimeStay Payouts Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(5);
            document.add(title);

            // Subtitle
            String filterText = String.format("Filters applied - Status: %s, Search: %s",
                    status != null ? status.name() : "All",
                    search != null && !search.isEmpty() ? search : "None");
            Paragraph subtitle = new Paragraph(filterText, subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(20);
            document.add(subtitle);

            // Table
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.5f, 2.5f, 2f, 1.5f, 1.5f, 1f});

            // Table Header
            String[] headers = {"Payout ID", "Property", "Host", "Amount", "Status", "Period"};
            Color headerBgColor = new Color(192, 86, 33); // #C05621
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, tableHeaderFont));
                cell.setBackgroundColor(headerBgColor);
                cell.setPadding(8);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            // Table Body
            for (PayoutDto p : payouts) {
                String[] row = {
                        String.valueOf(p.getId()),
                        p.getPropertyName(),
                        p.getHostName(),
                        String.valueOf(p.getAmount()),
                        p.getStatus() != null ? p.getStatus().name() : "N/A",
                        p.getPeriod()
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
            log.error("Error generating Payouts PDF", e);
            throw new RuntimeException("Could not generate Payouts PDF", e);
        }
    }
}
