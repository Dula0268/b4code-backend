package com.b4code.backend.modules.admin.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KpiValueDto {
    private String value;       
    private String change;      
    private boolean positive;   
}
