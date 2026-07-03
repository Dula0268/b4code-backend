package com.b4code.backend.dto.owner;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class OwnerReservationPageDto {
    private List<OwnerReservationDto> reservations;
    private int totalItems;
    private int totalPages;
    private int currentPage;
    private int confirmed;
    private int pending;
    private int checkInsToday;
    private int cancellations;
    private int totalBookingsThisMonth;
}
