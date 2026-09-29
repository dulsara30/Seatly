package com.seatly.backend.common.exception;

import com.seatly.backend.common.payload.ResponseEntityDto;
import com.seatly.backend.common.type.CommonMessageKey;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ModuleException.class)
    public ResponseEntity<ResponseEntityDto<ResponseEntityDto.ErrorMessage>> handleModuleException(ModuleException ex) {
        return ResponseEntity.status(ex.getHttpStatus()).body(ResponseEntityDto.error(ex.getMessageKey()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseEntityDto<ResponseEntityDto.FieldValidationError>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex) {
        List<ResponseEntityDto.FieldValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new ResponseEntityDto.FieldValidationError(fieldError.getField(), fieldError.getDefaultMessage()))
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseEntityDto.validationError(errors));
    }

    // Constraints on @RequestParam / @PathVariable (e.g. page size bounds).
    // Spring MVC validates those itself and raises this, not the exception above.
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ResponseEntityDto<ResponseEntityDto.ErrorMessage>> handleHandlerMethodValidation(
            HandlerMethodValidationException ex) {
        List<String> messages = ex.getAllErrors().stream().map(MessageSourceResolvable::getDefaultMessage).toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseEntityDto.errors(messages));
    }

    // A parameter that can't be converted to its type: ?mode=online, /events/abc.
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResponseEntityDto<ResponseEntityDto.ErrorMessage>> handleArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseEntityDto.error(CommonMessageKey.INVALID_PARAMETER));
    }

    // Unparseable JSON, or a body value Jackson can't convert (unknown enum,
    // badly formatted date). Never echo ex's message — it quotes the raw input.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseEntityDto<ResponseEntityDto.ErrorMessage>> handleMessageNotReadable(
            HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseEntityDto.error(CommonMessageKey.MALFORMED_REQUEST_BODY));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseEntityDto<ResponseEntityDto.ErrorMessage>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseEntityDto.error(CommonMessageKey.ACCESS_DENIED));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ResponseEntityDto<ResponseEntityDto.ErrorMessage>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex) {
        log.warn("Data integrity violation", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ResponseEntityDto.error(CommonMessageKey.DATA_INTEGRITY_VIOLATION));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseEntityDto<ResponseEntityDto.ErrorMessage>> handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseEntityDto.error(CommonMessageKey.INTERNAL_SERVER_ERROR));
    }
}
