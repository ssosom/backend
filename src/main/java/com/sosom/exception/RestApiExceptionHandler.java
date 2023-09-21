package com.sosom.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestApiExceptionHandler {
    @ExceptionHandler(value = {CustomException.class})
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(CustomException ex){
        ErrorCode errorCode = ex.getErrorCode();
        return new ResponseEntity<>(new ErrorResponse(errorCode.name(),errorCode.getMessage()),errorCode.getStatus());
    }


    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex){
        ObjectError error = ex.getBindingResult().getAllErrors().get(0);
        return new ResponseEntity<>(new ErrorResponse(HttpStatus.BAD_REQUEST.name(), error.getDefaultMessage()), HttpStatus.BAD_REQUEST);
    }
}
