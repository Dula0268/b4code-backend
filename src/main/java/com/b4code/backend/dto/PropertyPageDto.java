package com.b4code.backend.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyPageDto {
    private List<PropertyDto> content;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
}

