package com.katlego.task_tracking_api.exceptionHandler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatusCode;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private String message;
    private int status;
    private HttpStatusCode error;
    private LocalDateTime timestamp;
}