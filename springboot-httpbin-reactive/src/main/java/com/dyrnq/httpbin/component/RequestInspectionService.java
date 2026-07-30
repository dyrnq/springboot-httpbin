package com.dyrnq.httpbin.component;

import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Reactive (WebFlux) port of the servlet
 * {@code com.dyrnq.httpbin.component.RequestInspectionService}.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code GET /headers} — returns the request headers as JSON;</li>
 *   <li>{@code GET /ip} — returns the client {@code origin} IP;</li>
 *   <li>{@code GET /user-agent} — returns the {@code User-Agent}
 *       header.</li>
 * </ul>
 */
@Component
public class RequestInspectionService extends ReactiveBaseService {

    /** Reactive equivalent of servlet {@code ip()}. */
    public Mono<Void> ip(ServerWebExchange exchange) {
        JSONObject response = new JSONObject();
        response.put("origin", getOrigin(exchange));
        return drainBody(exchange).then(rsOk(exchange, response));
    }

    /** Reactive equivalent of servlet {@code userAgent()}. */
    public Mono<Void> userAgent(ServerWebExchange exchange) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        JSONObject response = new JSONObject();
        response.put("user-agent", headers.getFirst("User-Agent"));
        return drainBody(exchange).then(rsOk(exchange, response));
    }

    /** Reactive equivalent of servlet {@code headers()}. */
    public Mono<Void> headers(ServerWebExchange exchange) {
        JSONObject response = new JSONObject();
        response.put("headers", mapHeadersToJSON(exchange.getRequest().getHeaders()));
        return drainBody(exchange).then(rsOk(exchange, response));
    }
}
