package com.dyrnq.httpbin.component;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Reactive (WebFlux) port of the servlet
 * {@code com.dyrnq.httpbin.component.RedirectsService}.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code /redirect-to} (GET/POST/PUT/PATCH/DELETE) — 3xx
 *       redirect to a caller-supplied URL with a configurable status
 *       code;</li>
 *   <li>{@code /redirect/{n}} — chain of n relative redirects
 *       ending at {@code /get};</li>
 *   <li>{@code /absolute-redirect/{n}} — chain of n absolute
 *       redirects ending at {@code /get};</li>
 *   <li>{@code /relative-redirect/{n}} — same as {@code /redirect/{n}}.</li>
 * </ul>
 */
@Component
public class RedirectsService extends ReactiveBaseService {

    /** {@code GET /redirect-to} — drain body, then 3xx to {@code url}. */
    public Mono<Void> redirectToGet(ServerWebExchange exchange, String url, Integer statusCode) {
        int code = statusCode != null ? statusCode : HttpStatus.FOUND.value();
        return drainBody(exchange).then(redirectTo(exchange, url, code));
    }

    /**
     * Generic {@code /redirect-to} handler used by all verbs that don't
     * accept query parameters (POST/PUT also accept {@code url} via
     * multipart). Reads the body for the verb and 3xx-redirects.
     *
     * <p>Distinct method name to avoid overload ambiguity with the
     * inherited {@link ReactiveBaseService#redirectTo(ServerWebExchange, String)}.
     */
    public Mono<Void> redirectToNoUrl(ServerWebExchange exchange, Integer statusCode) {
        int code = statusCode != null ? statusCode : HttpStatus.FOUND.value();
        return drainBody(exchange).then(redirectTo(exchange, (String) null, code));
    }

    /**
     * Chain of {@code n} relative 302 redirects. If {@code n > 20}
     * respond 422 with {@code "n > 20"}; if {@code n > 1} redirect to
     * {@code /redirect/{n-1}}; if {@code n <= 1} redirect to
     * {@code /get}.
     */
    public Mono<Void> redirect(ServerWebExchange exchange, int n) {
        return drainBody(exchange).then(Mono.defer(() -> {
            if (n > 20) {
                return tooLarge(exchange);
            }
            if (n > 1) {
                return redirectTo(exchange, "/redirect/" + (n - 1));
            }
            return redirectTo(exchange, "/get");
        }));
    }

    /** Chain of {@code n} absolute 302 redirects. */
    public Mono<Void> absoluteRedirect(ServerWebExchange exchange, int n) {
        return drainBody(exchange).then(Mono.defer(() -> {
            if (n > 20) {
                return tooLarge(exchange);
            }
            if (n > 1) {
                return redirectTo(exchange, "/absolute-redirect/" + (n - 1),
                        HttpStatus.FOUND.value(), /*absolute=*/ true);
            }
            return redirectTo(exchange, "/get",
                    HttpStatus.FOUND.value(), /*absolute=*/ true);
        }));
    }

    /** Chain of {@code n} relative 302 redirects (alias of {@link #redirect}). */
    public Mono<Void> relativeRedirect(ServerWebExchange exchange, int n) {
        return redirect(exchange, n);
    }

    /** Helper: 422 with {@code "n > 20"} body. */
    private Mono<Void> tooLarge(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNPROCESSABLE_ENTITY);
        exchange.getResponse().getHeaders().setContentType(MediaType.TEXT_PLAIN);
        return rsByte(exchange, "n > 20".getBytes(StandardCharsets.UTF_8),
                HttpStatus.UNPROCESSABLE_ENTITY.value(), MediaType.TEXT_PLAIN_VALUE);
    }
}
