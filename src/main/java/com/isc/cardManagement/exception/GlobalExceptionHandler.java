package com.isc.cardManagement.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.isc.cardManagement.dto.ErrorResponseDTO;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {


    private final MessageSource messageSource;


  /*  @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleValidationException(MethodArgumentNotValidException ex) {
        log.error("Validation error occurred", ex);

        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> {
                    String messageKey = error.getDefaultMessage();
                    String translatedMessage = translateMessage(messageKey);
                    return error.getField() + ": " + translatedMessage;
                })
                .collect(Collectors.joining(", "));

        return ErrorResponseDTO.builder()
                .error("خزای اعتبارسنجی")
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
    }*/

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleConstraintViolation(ConstraintViolationException ex) {
        log.error("Constraint violation error", ex);

        String details = ex.getConstraintViolations().stream()
                .map(violation -> {
                    String messageKey = violation.getMessage();
                    String translatedMessage = translateMessage(messageKey);
                    String fieldName = violation.getPropertyPath().toString();
                    return fieldName + ": " + translatedMessage;
                })
                .collect(Collectors.joining(", "));

        return ErrorResponseDTO.builder()
                .error("خطای اعتبارسنجی")
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {

        log.error("Failed to read request", ex);

        String details = "فرمت درخواست نامعتبر است";

        if (ex.getCause() instanceof InvalidFormatException) {
            InvalidFormatException ifx = (InvalidFormatException) ex.getCause();
            details = String.format("مقدار نامعتبر برای فیلد '%s': %s",
                    ifx.getPath().get(0).getFieldName(),
                    ifx.getValue());
        }

        return ErrorResponseDTO.builder()
                .error("خطای خواندن درخواست")
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDTO handleNotFoundException(NotFoundException ex) {
        log.error("Resource not found", ex);

        return ErrorResponseDTO.builder()
                .error("یافت نشد")
                .details(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponseDTO handleBusinessException(BusinessException ex) {
        log.error("Business rule violation", ex);

        return ErrorResponseDTO.builder()
                .error("خطای منطق کسب‌وکار")
                .details(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponseDTO handleGeneralException(Exception ex) {

        log.error("Unexpected error occurred", ex);

        return ErrorResponseDTO.builder()
                .error("خطای سرور")
                .details("خطای غیر منتظره ای رخ داده")
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     *  ترجمه کلید پیام به متن فارسی
     */
    private String translateMessage(String messageKey) {
        try {
            // حذف {} از کلید
            String cleanKey = messageKey
                    .replace("{", "")
                    .replace("}", "")
                    .trim();

            // تلاش برای ترجمه
            return messageSource.getMessage(
                    cleanKey,
                    null,
                    Locale.getDefault()
            );

        } catch (NoSuchMessageException e) {
            // اگر کلید یافت نشد، خود پیام را برگردان
            log.warn("Message key not found: {}", messageKey);
            return messageKey;
        }
    }



 /*   @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(NotFoundException ex) {

        log.warn("NotFound: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO(ex.getMessage()));
    }*/


    // ========== Validation Exceptions ==========
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleBadRequest(BadRequestException ex) {

        log.warn("BadRequest: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
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


/*    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleAll(Exception ex) {
        log.error("Unhandled exception: ", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO("خطای داخلی سرور", ex.getMessage()));
    }*/



   @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Locale locale = LocaleContextHolder.getLocale();

        String details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> {
                    String message = resolveMessage(error.getDefaultMessage(), locale);
                    return error.getField() + ": " + message;
                })
                .collect(Collectors.joining("; "));

        log.warn("Validation error: {}", details);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO("خطای اعتبارسنجی", details));
    }

    private String resolveMessage(String messageTemplate, Locale locale) {
        if (messageTemplate == null) {
            return "خطای نامشخص";
        }

        if (messageTemplate.startsWith("{") && messageTemplate.endsWith("}")) {
            String key = messageTemplate.substring(1, messageTemplate.length() - 1);
            try {
                return messageSource.getMessage(key, null, locale);
            } catch (Exception e) {
                log.debug("Could not resolve message key: {}", key);
                return messageTemplate; // fallback
            }
        }

        return messageTemplate;
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

