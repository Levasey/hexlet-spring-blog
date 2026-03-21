package io.hexletspringblog.config.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "app.rate-limit")
@Getter
@Setter
public class RateLimitProperties {

    private boolean enabled = true;

    /**
     * Учитывать первый адрес из {@code X-Forwarded-For}. Включайте только за доверенным reverse-proxy.
     */
    private boolean trustXForwardedFor = false;

    private Login login = new Login();

    private PublicGet publicGet = new PublicGet();

    @Getter
    @Setter
    public static class Login {

        private boolean enabled = true;

        private int requestsPerMinute = 20;
    }

    @Getter
    @Setter
    public static class PublicGet {

        /**
         * Лимит на публичные GET {@code /api/posts}, {@code /api/tags} (и элементы по id).
         */
        private boolean enabled = false;

        private int requestsPerMinute = 300;
    }
}
