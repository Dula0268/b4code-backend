package com.b4code.backend.modules.admin.dto;

// Phase 3 — Finance: Paginated transaction list

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TransactionPageDto {
    private List<TransactionDto> content;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
}
