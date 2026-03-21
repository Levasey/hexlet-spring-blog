package io.hexletspringblog.handler;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.Map;

/**
 * Единый ответ об ошибке: {@code application/problem+json} (RFC 9457) плюс поля {@code code},
 * {@code message} (дублирует {@code detail}) и при необходимости {@code fieldErrors}.
 */
public final class ApiProblem {

    public static final MediaType MEDIA_TYPE = MediaType.parseMediaType("application/problem+json");

    private static final URI ABOUT_BLANK = URI.create("about:blank");

    private ApiProblem() {
    }

    public static ResponseEntity<ProblemDetail> respond(ProblemDetail problem) {
        return ResponseEntity.status(problem.getStatus())
                .contentType(MEDIA_TYPE)
                .body(problem);
    }

    public static ProblemDetail of(
            HttpStatusCode status,
            String code,
            String title,
            String message,
            Map<String, String> fieldErrors
    ) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, message);
        pd.setTitle(title);
        pd.setType(ABOUT_BLANK);
        pd.setProperty("code", code);
        pd.setProperty("message", message);
        if (fieldErrors != null && !fieldErrors.isEmpty()) {
            pd.setProperty("fieldErrors", fieldErrors);
        }
        return pd;
    }
}
