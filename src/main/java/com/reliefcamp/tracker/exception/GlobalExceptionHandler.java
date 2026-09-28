package com.reliefcamp.tracker.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private ApiError error(int status,String name,String message,String path,List<String> details){
        return new ApiError(LocalDateTime.now(),status,name,message,path,details==null?List.of():details);
    }
    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiError> notFound(ResourceNotFoundException e,HttpServletRequest r){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(404,"Not Found",e.getMessage(),r.getRequestURI(),null));
    }
    @ExceptionHandler(BusinessRuleException.class)
    ResponseEntity<ApiError> business(BusinessRuleException e,HttpServletRequest r){
        return ResponseEntity.badRequest().body(error(400,"Business Rule Violation",e.getMessage(),r.getRequestURI(),null));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException e,HttpServletRequest r){
        List<String> details=e.getBindingResult().getFieldErrors().stream().map(x->x.getField()+": "+x.getDefaultMessage()).toList();
        return ResponseEntity.badRequest().body(error(400,"Validation Failed","Please correct the highlighted fields.",r.getRequestURI(),details));
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> integrity(DataIntegrityViolationException e,HttpServletRequest r){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error(409,"Conflict","The request conflicts with existing data (for example, a duplicate camp supply type).",r.getRequestURI(),null));
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> generic(Exception e,HttpServletRequest r){
        return ResponseEntity.status(500).body(error(500,"Internal Server Error","An unexpected error occurred.",r.getRequestURI(),null));
    }
}
