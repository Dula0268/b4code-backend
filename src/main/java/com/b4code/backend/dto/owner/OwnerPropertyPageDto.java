package com.b4code.backend.dto.owner;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerPropertyPageDto {

    private List<OwnerPropertyDto> properties;
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private int pageSize;
}
