package com.dyrnq.httpbin.component;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Reactive (WebFlux) port of the servlet
 * {@code com.dyrnq.httpbin.component.StatusCodesService}.
 *
 * <p>Handles the {@code /status/{codes}} endpoints. The behaviour mirrors
 * the servlet version:
 * <ul>
 *   <li>read &amp; discard the request body ({@code Utils.copy(is, NULL_OUTPUT_STREAM)});</li>
 *   <li>set the response status to the parsed {@code codes} value;</li>
 *   <li>if the status code falls in the {@code 3xx} range, additionally
 *       set a {@code Location: /get} header.</li>
 * </ul>
 *
 * <p>Returning {@link Mono Mono&lt;Void&gt;} (instead of {@code Mono.just(ResponseEntity)})
 * tells WebFlux to commit whatever has been written to
 * {@code exchange.getResponse()} — equivalent to the servlet idiom of
 * mutating {@code HttpServletResponse} directly and returning {@code null}.
 */
@Component
public class StatusCodesService {

    /**
     * Reactive equivalent of the servlet
     * {@code status(String codes)} method.
     *
     * <p>The body is drained (via {@code .then()} on
     * {@code exchange.getRequest().getBody()}) so that the HTTP client
     * sees the server consume its request before receiving the status
     * reply. Then status / Location headers are applied to the
     * exchange.
     *
     * @param codes    the status code path variable (e.g. {@code "404"})
     * @param exchange the current WebFlux exchange
     * @return a {@link Mono Mono&lt;Void&gt;} that completes once the
     *         response has been fully configured
     */
    public Mono<Void> status(String codes, ServerWebExchange exchange) {
        return exchange.getRequest().getBody()
                // drain the request body without writing it anywhere
                // (Spring releases the DataBuffers automatically)
                .then()
                .then(Mono.fromRunnable(() -> {
                    int status = Integer.parseInt(codes);
                    if (status >= 300 && status < 400) {
                        // 3xx → set Location header (matches servlet's redirectTo("/get", status))
                        exchange.getResponse().getHeaders().set("Location", "/get");
                    }
                    exchange.getResponse().setStatusCode(HttpStatus.valueOf(status));
                }));
    }
}
