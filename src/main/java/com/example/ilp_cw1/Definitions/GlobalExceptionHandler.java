package com.example.ilp_cw1.Definitions;

import com.fasterxml.jackson.databind.JsonMappingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(JsonMappingException.class)
    public ResponseEntity<Object> handleJsonMappingException(JsonMappingException e) {
        return new ResponseEntity<>(e + ": Invalid input", HttpStatus.BAD_REQUEST);
    }
}