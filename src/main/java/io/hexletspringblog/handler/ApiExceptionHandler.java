package io.hexletspringblog.handler;

import io.hexletspringblog.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Единый ответ API для {@link ResourceNotFoundException} и ошибок валидации:
 * {@code 404} + {@code code: NOT_FOUND}; {@code 422} + {@code code: VALIDATION_FAILED} и {@code fieldErrors}.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiExceptionHandler {

    private static final String CODE_NOT_FOUND = "NOT_FOUND";
    private static final String CODE_VALIDATION_FAILED = "VALIDATION_FAILED";

    private final MessageSource messageSource;

    public ApiExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        ProblemDetail pd = ApiProblem.of(
                HttpStatus.NOT_FOUND,
                CODE_NOT_FOUND,
                "Not Found",
                ex.getMessage(),
                null
        );
        setInstance(pd, request);
        return ApiProblem.respond(pd);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        return respondValidation(fieldErrorsFromBinding(ex), request);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ProblemDetail> handleHandlerMethodValidation(
            HandlerMethodValidationException ex,
            HttpServletRequest request
    ) {
        if (ex.isForReturnValue()) {
            ProblemDetail pd = ApiProblem.of(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "INTERNAL_ERROR",
                    "Internal Server Error",
                    "Return value validation failed",
                    null
            );
            setInstance(pd, request);
            return ApiProblem.respond(pd);
        }
        return respondValidation(fieldErrorsFromMethodValidation(ex), request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        return respondValidation(fieldErrorsFromConstraintViolations(ex.getConstraintViolations()), request);
    }

    private ResponseEntity<ProblemDetail> respondValidation(Map<String, String> fieldErrors, HttpServletRequest request) {
        ProblemDetail pd = ApiProblem.of(
                HttpStatus.UNPROCESSABLE_ENTITY,
                CODE_VALIDATION_FAILED,
                "Validation Failed",
                "One or more fields have invalid values",
                fieldErrors
        );
        setInstance(pd, request);
        return ApiProblem.respond(pd);
    }

    private static Map<String, String> fieldErrorsFromBinding(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            fieldErrors.put(error.getObjectName(), error.getDefaultMessage());
        }
        return fieldErrors;
    }

    private Map<String, String> fieldErrorsFromMethodValidation(HandlerMethodValidationException ex) {
        var locale = LocaleContextHolder.getLocale();
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (ParameterValidationResult result : ex.getParameterValidationResults()) {
            if (result instanceof ParameterErrors pe) {
                for (FieldError fe : pe.getFieldErrors()) {
                    fieldErrors.put(fe.getField(), fe.getDefaultMessage());
                }
                for (ObjectError oe : pe.getGlobalErrors()) {
                    fieldErrors.put(oe.getObjectName(), oe.getDefaultMessage());
                }
            } else {
                MethodParameter mp = result.getMethodParameter();
                String base = mp.getParameterName() != null ? mp.getParameterName() : "param";
                List<MessageSourceResolvable> resolvables = result.getResolvableErrors();
                for (int i = 0; i < resolvables.size(); i++) {
                    MessageSourceResolvable r = resolvables.get(i);
                    String key = resolvables.size() == 1 ? base : base + "[" + i + "]";
                    fieldErrors.put(key, messageSource.getMessage(r, locale));
                }
            }
        }
        List<MessageSourceResolvable> cross = ex.getCrossParameterValidationResults();
        for (int i = 0; i < cross.size(); i++) {
            fieldErrors.put("crossParameter[" + i + "]", messageSource.getMessage(cross.get(i), locale));
        }
        return fieldErrors;
    }

    private static Map<String, String> fieldErrorsFromConstraintViolations(
            Iterable<ConstraintViolation<?>> violations
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (ConstraintViolation<?> v : violations) {
            String field = lastPathNodeName(v.getPropertyPath());
            fieldErrors.merge(field, v.getMessage(), (a, b) -> a + "; " + b);
        }
        return fieldErrors;
    }

    private static String lastPathNodeName(Path propertyPath) {
        String field = "value";
        for (Path.Node node : propertyPath) {
            if (node.getName() != null) {
                field = node.getName();
            }
        }
        return field;
    }

    private static void setInstance(ProblemDetail pd, HttpServletRequest request) {
        if (request == null) {
            return;
        }
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        pd.setInstance(URI.create(query != null && !query.isEmpty() ? uri + "?" + query : uri));
    }
}
