package com.isc.cardManagement.exception;

import com.isc.cardManagement.dto.ErrorResponseDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(NotFoundException ex) {

        log.warn("NotFound: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO(ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleBadRequest(BadRequestException ex) {

        log.warn("BadRequest: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO(ex.getMessage()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseDTO> handleBusinessException(BusinessException ex) {
        log.warn("BusinessException: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponseDTO(ex.getMessage()));
    }



    // ========== Database Exceptions ==========

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataIntegrityViolation(
            DataIntegrityViolationException ex) {

        String message = extractConstraintMessage(ex);
        log.warn("DataIntegrityViolation: {}", message);

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponseDTO(message));
    }

    // ========== Validation Exceptions ==========

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolation(
            ConstraintViolationException ex) {

        String details = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));

        log.warn("Validation error: {}", details);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO("خطای اعتبارسنجی", details));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        log.warn("Validation error: {}", details);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO("خطای اعتبارسنجی", details));
    }

    // ========== Type Conversion Exceptions ==========

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {

        String message = String.format("پارامتر نامعتبر: %s", ex.getName());
        log.warn("TypeMismatch: {} -> {}", ex.getName(), ex.getValue());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO(message));
    }

    @ExceptionHandler(HttpMessageConversionException.class)
    public ResponseEntity<ErrorResponseDTO> handleConversion(
            HttpMessageConversionException ex) {

        log.warn("HttpMessageConversionException: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO("فرمت درخواست نامعتبر است"));
    }

    // ========== Generic Exception ==========

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleAll(Exception ex) {
        log.error("Unhandled exception: ", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO("خطای داخلی سرور", ex.getMessage()));
    }

    // ========== Helper Methods ==========

    private String extractConstraintMessage(DataIntegrityViolationException ex) {

        String message = ex.getMessage();

        if (message == null) {
            return "خطای یکتایی داده";
        }

        // شماره کارت تکراری
        if (message.contains("TBL_CARD") && message.contains("CARD_NUMBER")) {
            return extractCardNumber(message)
                    .map(cardNumber -> String.format("شماره کارت تکراری: %s", cardNumber))
                    .orElse("شماره کارت تکراری است");
        }

        // کدملی تکراری
        if (message.contains("TBL_PERSON") && message.contains("NATIONAL_CODE")) {
            return extractNationalCode(message)
                    .map(nationalCode -> String.format("کدملی تکراری: %s", nationalCode))
                    .orElse("کدملی تکراری است");
        }

        // شماره حساب تکراری
        if (message.contains("TBL_ACCOUNT") && message.contains("ACCOUNT_NUMBER")) {
            return extractAccountNumber(message)
                    .map(accountNumber -> String.format("شماره حساب تکراری: %s", accountNumber))
                    .orElse("شماره حساب تکراری است");
        }

        // کد صادرکننده تکراری
        if (message.contains("TBL_ISSUER") && message.contains("ISSUER_CODE")) {
            return extractIssuerCode(message)
                    .map(issuerCode -> String.format("کد صادرکننده تکراری: %s", issuerCode))
                    .orElse("کد صادرکننده تکراری است");
        }

        // Foreign key constraint
        if (message.contains("foreign key")) {
            return "ارجاع نامعتبر به رکورد مرتبط";
        }

        return "خطای یکتایی داده";
    }

    private Optional<String> extractCardNumber(String message) {
        try {
            // Extract: VALUES ( /* 1 */ '1234567812345678' )
            Pattern pattern = Pattern.compile("VALUES.*?'(\\d{16})'");
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                return Optional.of(matcher.group(1));
            }
        } catch (Exception e) {
            log.debug("Could not extract card number", e);
        }
        return Optional.empty();
    }

    private Optional<String> extractNationalCode(String message) {
        try {
            Pattern pattern = Pattern.compile("VALUES.*?'(\\d{10})'");
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                return Optional.of(matcher.group(1));
            }
        } catch (Exception e) {
            log.debug("Could not extract national code", e);
        }
        return Optional.empty();
    }

    private Optional<String> extractAccountNumber(String message) {
        try {
            Pattern pattern = Pattern.compile("VALUES.*?'(\\d{10})'");
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                return Optional.of(matcher.group(1));
            }
        } catch (Exception e) {
            log.debug("Could not extract account number", e);
        }
        return Optional.empty();
    }

    private Optional<String> extractIssuerCode(String message) {
        try {
            Pattern pattern = Pattern.compile("VALUES.*?'(\\d{6})'");
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                return Optional.of(matcher.group(1));
            }
        } catch (Exception e) {
            log.debug("Could not extract issuer code", e);
        }
        return Optional.empty();
    }

}

