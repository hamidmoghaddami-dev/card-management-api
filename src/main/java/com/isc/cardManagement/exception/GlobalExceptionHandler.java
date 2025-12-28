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


    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        log.error("Failed to read request", ex);

        String details = "فرمت درخواست نامعتبر است";

        if (ex.getCause() instanceof InvalidFormatException) {
            InvalidFormatException ifx = (InvalidFormatException) ex.getCause();
            if (!ifx.getPath().isEmpty()) {
                details = String.format("مقدار نامعتبر برای فیلد '%s': %s",
                        ifx.getPath().get(0).getFieldName(),
                        ifx.getValue());
            }
        }

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .error("خطای خواندن درخواست")
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

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

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .error("خطای اعتبارسنجی")
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    // ========== Custom Exception Handlers ==========

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

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleBadRequest(BadRequestException ex) {
        log.warn("BadRequest: {}", ex.getMessage());

        return ErrorResponseDTO.builder()
                .error("درخواست نامعتبر")
                .details(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponseDTO handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = extractConstraintMessage(ex);
        log.warn("DataIntegrityViolation: {}", message);

        return ErrorResponseDTO.builder()
                .error("خطای یکتایی داده")
                .details(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("پارامتر نامعتبر: %s", ex.getName());
        log.warn("TypeMismatch: {} -> {}", ex.getName(), ex.getValue());

        return ErrorResponseDTO.builder()
                .error("خطای نوع داده")
                .details(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(HttpMessageConversionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleConversion(HttpMessageConversionException ex) {
        log.warn("HttpMessageConversionException: {}", ex.getMessage());

        return ErrorResponseDTO.builder()
                .error("خطای تبدیل")
                .details("فرمت درخواست نامعتبر است")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponseDTO handleGeneralException(Exception ex) {
        log.error("Unexpected error occurred", ex);

        return ErrorResponseDTO.builder()
                .error("خطای سرور")
                .details("خطای غیرمنتظره‌ای رخ داده است")
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ========== Helper Methods ==========

    private String translateMessage(String messageKey) {
        if (messageKey == null || messageKey.trim().isEmpty()) {
            return messageKey;
        }

        try {
            String cleanKey = messageKey
                    .replace("{", "")
                    .replace("}", "")
                    .trim();

            if (cleanKey.isEmpty()) {
                return messageKey;
            }

            return messageSource.getMessage(
                    cleanKey,
                    null,
                    Locale.forLanguageTag("fa-IR")
            );

        } catch (NoSuchMessageException e) {
            log.warn("Message key not found: {}", messageKey);
            return messageKey;
        }
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
                return messageTemplate;
            }
        }

        return messageTemplate;
    }

    private String extractConstraintMessage(DataIntegrityViolationException ex) {
        String message = ex.getMessage();

        if (message == null) {
            return "خطای یکتایی داده";
        }

        if (message.contains("TBL_CARD") && message.contains("CARD_NUMBER")) {
            return extractCardNumber(message)
                    .map(cardNumber -> String.format("شماره کارت تکراری: %s", cardNumber))
                    .orElse("شماره کارت تکراری است");
        }

        if (message.contains("TBL_PERSON") && message.contains("NATIONAL_CODE")) {
            return extractNationalCode(message)
                    .map(nationalCode -> String.format("کدملی تکراری: %s", nationalCode))
                    .orElse("کدملی تکراری است");
        }

        if (message.contains("TBL_ACCOUNT") && message.contains("ACCOUNT_NUMBER")) {
            return extractAccountNumber(message)
                    .map(accountNumber -> String.format("شماره حساب تکراری: %s", accountNumber))
                    .orElse("شماره حساب تکراری است");
        }

        if (message.contains("TBL_ISSUER") && message.contains("ISSUER_CODE")) {
            return extractIssuerCode(message)
                    .map(issuerCode -> String.format("کد صادرکننده تکراری: %s", issuerCode))
                    .orElse("کد صادرکننده تکراری است");
        }

        if (message.contains("foreign key")) {
            return "ارجاع نامعتبر به رکورد مرتبط";
        }

        return "خطای یکتایی داده";
    }

    private Optional<String> extractCardNumber(String message) {
        return extractPattern(message, "VALUES.*?'(\\d{16})'");
    }

    private Optional<String> extractNationalCode(String message) {
        return extractPattern(message, "VALUES.*?'(\\d{10})'");
    }

    private Optional<String> extractAccountNumber(String message) {
        return extractPattern(message, "VALUES.*?'(\\d{10})'");
    }

    private Optional<String> extractIssuerCode(String message) {
        return extractPattern(message, "VALUES.*?'(\\d{6})'");
    }

    private Optional<String> extractPattern(String message, String regex) {
        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                return Optional.of(matcher.group(1));
            }
        } catch (Exception e) {
            log.debug("Could not extract pattern: {}", regex, e);
        }
        return Optional.empty();
    }


}

