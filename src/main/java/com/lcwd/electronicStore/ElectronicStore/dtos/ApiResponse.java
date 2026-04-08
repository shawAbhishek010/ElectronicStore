package com.lcwd.electronicStore.ElectronicStore.dtos;

import lombok.*;
import org.springframework.http.HttpStatus;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class ApiResponse {
    private String message;
    private boolean successs;
    private HttpStatus status;
}
