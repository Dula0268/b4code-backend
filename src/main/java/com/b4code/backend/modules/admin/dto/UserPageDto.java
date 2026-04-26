package com.b4code.backend.modules.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPageDto {

    private List<UserDto> content;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
}
