package io.hexletspringblog.handler;

import io.hexletspringblog.exception.AccessForbiddenException;
import io.hexletspringblog.exception.ResourceAlreadyExistsException;
import io.hexletspringblog.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessForbiddenException.class)
    public ResponseEntity<ProblemDetail> handleForbidden(AccessForbiddenException ex, HttpServletRequest request) {
        ProblemDetail pd = ApiProblem.of(
                HttpStatus.FORBIDDEN,
                "FORBIDDEN",
                "Forbidden",
                ex.getMessage(),
                null
        );
        setInstance(pd, request);
        return ApiProblem.respond(pd);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        ProblemDetail pd = ApiProblem.of(
                HttpStatus.NOT_FOUND,
                "NOT_FOUND",
                "Not Found",
                ex.getMessage(),
                null
        );
        setInstance(pd, request);
        return ApiProblem.respond(pd);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleResourceAlreadyExists(
            ResourceAlreadyExistsException ex,
            HttpServletRequest request
    ) {
        ProblemDetail pd = ApiProblem.of(
                HttpStatus.CONFLICT,
                "CONFLICT",
                "Conflict",
                ex.getMessage(),
                null
        );
        setInstance(pd, request);
        return ApiProblem.respond(pd);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );
        ProblemDetail pd = ApiProblem.of(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "VALIDATION_FAILED",
                "Validation Failed",
                "One or more fields have invalid values",
                fieldErrors
        );
        setInstance(pd, request);
        return ApiProblem.respond(pd);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleUnreadableJson(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        ProblemDetail pd = ApiProblem.of(
                HttpStatus.BAD_REQUEST,
                "BAD_REQUEST",
                "Bad Request",
                "Request body is invalid or malformed",
                null
        );
        setInstance(pd, request);
        return ApiProblem.respond(pd);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request
    ) {
        ProblemDetail pd = ApiProblem.of(
                HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED",
                "Unauthorized",
                "Invalid credentials",
                null
        );
        setInstance(pd, request);
        return ApiProblem.respond(pd);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleOtherExceptions(Exception ex, HttpServletRequest request) {
        ProblemDetail pd = ApiProblem.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "Internal Server Error",
                "Something went wrong: " + ex.getMessage(),
                null
        );
        setInstance(pd, request);
        return ApiProblem.respond(pd);
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
