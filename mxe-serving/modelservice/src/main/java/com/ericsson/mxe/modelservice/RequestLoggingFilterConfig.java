package com.ericsson.mxe.modelservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class RequestLoggingFilterConfig {

    @Bean
    public CommonsRequestLoggingFilter logFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter() {
            @Override
            protected void beforeRequest(HttpServletRequest request, String message) {
                String username = request.getHeader("x-auth-userid");
                logger.debug("MXE Incoming request (username = " + username + "): " + request.getMethod() + message);
            }

            @Override
            protected boolean shouldLog(HttpServletRequest request) {
                return (super.shouldLog(request) && !"/healthz".equals(request.getRequestURI()));
            }
        };
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setIncludeClientInfo(true);
        filter.setIncludeHeaders(false);

        filter.setMaxPayloadLength(10000);
        filter.setBeforeMessagePrefix(" [");
        filter.setAfterMessagePrefix("MXE Request Data: [");
        return filter;
    }
}
