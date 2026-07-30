package com.dyrnq.httpbin.component;

import cn.hutool.core.util.URLUtil;
import org.json.JSONObject;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reactive (WebFlux) port of the servlet
 * {@code com.dyrnq.httpbin.component.CookiesService}.
 *
 * <p>Endpoints exposed:
 * <ul>
 *   <li>{@code GET /cookies} — returns JSON mapping cookie name →
 *       decoded value;</li>
 *   <li>{@code GET /cookies/set} — sets cookies for every query
 *       parameter, then 302-redirects to {@code /cookies};</li>
 *   <li>{@code GET /cookies/set/{name}/{value}} — sets a single
 *       cookie from path variables;</li>
 *   <li>{@code GET /cookies/delete} — clears every query-parameter
 *       cookie via {@code Set-Cookie: name=; Path=/}, then redirects
 *       to {@code /cookies}.</li>
 * </ul>
 */
@Component
public class CookiesService extends ReactiveBaseService {

    /**
     * Reactive equivalent of the servlet
     * {@code cookies()} method.
     */
    public Mono<Void> cookies(ServerWebExchange exchange) {
        JSONObject cookies = readCookiesAsJSON(exchange.getRequest());
        JSONObject response = new JSONObject().put("cookies", cookies);
        return rsOk(exchange, response);
    }

    /**
     * Reactive equivalent of the servlet
     * {@code cookiesSet()} (no-arg) method — sets every query parameter
     * as a cookie and redirects.
     */
    public Mono<Void> cookiesSetFromQuery(ServerWebExchange exchange) {
        final ServerHttpRequest request = exchange.getRequest();
        return drainBody(exchange)
                .then(Mono.fromRunnable(() ->
                        request.getQueryParams().forEach((name, values) -> {
                            // Each (name, value) becomes a Set-Cookie header
                            for (String value : values) {
                                addSetCookie(exchange.getResponse(),
                                        URLUtil.encode(name),
                                        URLUtil.encode(value));
                            }
                        })))
                .then(redirectTo(exchange, "/cookies"));
    }

    /**
     * Reactive equivalent of the servlet
     * {@code cookiesSet(String name, String value)} method.
     */
    public Mono<Void> cookiesSet(ServerWebExchange exchange, String name, String value) {
        return drainBody(exchange)
                .then(Mono.fromRunnable(() ->
                        addSetCookie(exchange.getResponse(),
                                URLUtil.encode(name),
                                URLUtil.encode(value))))
                .then(redirectTo(exchange, "/cookies"));
    }

    /**
     * Reactive equivalent of the servlet
     * {@code cookiesDelete()} method — clears every query-parameter
     * cookie.
     */
    public Mono<Void> cookiesDelete(ServerWebExchange exchange) {
        final ServerHttpRequest request = exchange.getRequest();
        return drainBody(exchange)
                .then(Mono.fromRunnable(() -> {
                    // De-duplicate by name (Servlet's getParameterMap uses
                    // unique keys, but the reactive MultiValueMap also
                    // exposes a unique key set).
                    Set<String> names = new HashSet<>(request.getQueryParams().keySet());
                    for (String name : names) {
                        clearSetCookie(exchange.getResponse(),
                                URLUtil.encode(name));
                    }
                }))
                .then(redirectTo(exchange, "/cookies"));
    }
}
