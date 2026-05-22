package com.b4code.backend.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KpiValueDto {
    private String value;       
    private String change;      
    private boolean positive;   
}

