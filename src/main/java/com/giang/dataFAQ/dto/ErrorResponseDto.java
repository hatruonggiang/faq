package com.giang.dataFAQ.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponseDto {
    private int status;
    private String message;
    private String timestamp;
    private String path;

    public ErrorResponseDto(int status, String message, String path){
        this.status = status;
        this.message = message;
        this.path = path;
        this.timestamp = Instant.now().toString();

    }
}
