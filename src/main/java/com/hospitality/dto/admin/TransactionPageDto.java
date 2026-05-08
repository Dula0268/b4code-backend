package com.hospitality.dto.admin;

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
