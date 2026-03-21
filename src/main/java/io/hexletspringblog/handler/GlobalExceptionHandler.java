package io.hexletspringblog.handler;

import io.hexletspringblog.exception.AccessForbiddenException;
import io.hexletspringblog.exception.ResourceAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

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
