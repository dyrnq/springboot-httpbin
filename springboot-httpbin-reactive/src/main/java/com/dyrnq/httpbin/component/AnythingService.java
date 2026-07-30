package com.dyrnq.httpbin.component;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Reactive (WebFlux) port of the servlet
 * {@code com.dyrnq.httpbin.component.AnythingService}.
 *
 * <p>All {@code /anything} and {@code /anything/{anything}} endpoints
 * delegate to {@link ReactiveBaseService#anything(ServerWebExchange)}.
 * The {@code anything} path variable is accepted but ignored, matching
 * the servlet behaviour (it was also unused).
 */
@Component
public class AnythingService extends ReactiveBaseService {

    /**
     * Handles both {@code /anything} (no path variable) and
     * {@code /anything/{anything}} (with path variable). The
     * {@code anything} parameter is ignored for parity with the
     * servlet version.
     */
    public Mono<Void> anything(ServerWebExchange exchange, String anything) {
        return super.anything(exchange);
    }

    /**
     * Convenience overload used by {@code /anything} endpoints
     * without a path variable.
     */
    public Mono<Void> anything(ServerWebExchange exchange) {
        return super.anything(exchange);
    }
}
