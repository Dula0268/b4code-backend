package com.hospitality.dto.admin;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KpiValueDto {
    private String value;       
    private String change;      
    private boolean positive;   
}
