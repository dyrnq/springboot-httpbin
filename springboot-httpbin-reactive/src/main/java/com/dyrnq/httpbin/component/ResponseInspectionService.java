package com.dyrnq.httpbin.component;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Reactive (WebFlux) port of the servlet
 * {@code com.dyrnq.httpbin.component.ResponseInspectionService}.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code GET /cache} — emit cache headers (Last-Modified, ETag);
 *       return 304 if {@code If-Modified-Since} / {@code If-None-Match}
 *       is present.</li>
 *   <li>{@code GET /cache/{value}} — emit
 *       {@code Cache-Control: public, max-age=value}.</li>
 *   <li>{@code GET /etag/{etag}} — handle conditional requests via
 *       {@code If-None-Match} / {@code If-Match}.</li>
 *   <li>{@code GET|POST /response-headers} — echo query params as
 *       response headers and return a JSON description.</li>
 * </ul>
 */
@Component
public class ResponseInspectionService extends ReactiveBaseService {

    private static final Logger logger = LoggerFactory.getLogger(ResponseInspectionService.class);

    private static final SimpleDateFormat HTTP_DATE_FORMAT;
    static {
        HTTP_DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH);
        HTTP_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /** Reactive equivalent of servlet {@code cache()}. */
    public Mono<Void> cache(ServerWebExchange exchange) {
        final ServerHttpRequest request = exchange.getRequest();
        return drainBody(exchange).then(Mono.fromCallable(() -> {
            String ifModifiedSince = request.getHeaders().getFirst("If-Modified-Since");
            String ifNoneMatch = request.getHeaders().getFirst("If-None-Match");
            if (StringUtils.isNotBlank(ifModifiedSince) || StringUtils.isNotBlank(ifNoneMatch)) {
                exchange.getResponse().setStatusCode(HttpStatus.NOT_MODIFIED);
                return null;
            }
            return buildRequestMetadata(exchange, request);
        })).flatMap(response -> {
            if (response == null) {
                return Mono.empty();
            }
            // Set caching response headers
            exchange.getResponse().getHeaders().set("Last-Modified", HTTP_DATE_FORMAT.format(new Date()));
            exchange.getResponse().getHeaders().set("ETag",
                    UUID.randomUUID().toString().replace("-", ""));
            return rsOk(exchange, response);
        });
    }

    /** Reactive equivalent of servlet {@code cacheValue(Integer value)}. */
    public Mono<Void> cacheValue(ServerWebExchange exchange, Integer value) {
        final ServerHttpRequest request = exchange.getRequest();
        return drainBody(exchange).then(Mono.fromCallable(() -> {
            JSONObject response = buildRequestMetadata(exchange, request);
            exchange.getResponse().getHeaders()
                    .set("Cache-Control", "public, max-age=" + value);
            return response;
        })).flatMap(response -> rsOk(exchange, response));
    }

    /** Reactive equivalent of servlet {@code etag(String eTag)}. */
    public Mono<Void> etag(ServerWebExchange exchange, String eTag) {
        final HttpHeaders requestHeaders = exchange.getRequest().getHeaders();
        final List<String> ifNoneMatch = requestHeaders.get("If-None-Match");
        final List<String> ifMatch = requestHeaders.get("If-Match");
        logger.info("ifNoneMatch = {}", ifNoneMatch);
        logger.info("ifMatch = {}", ifMatch);

        return drainBody(exchange).then(Mono.fromCallable(() -> {
            if (ifNoneMatch != null && !ifNoneMatch.isEmpty()) {
                if (ifNoneMatch.contains(eTag) || ifNoneMatch.contains("*")) {
                    exchange.getResponse().setStatusCode(HttpStatus.NOT_MODIFIED);
                    exchange.getResponse().getHeaders().set("ETag", eTag);
                    return null;
                }
            } else if (ifMatch != null && !ifMatch.isEmpty()) {
                if (!ifMatch.contains(eTag) && !ifMatch.contains("*")) {
                    exchange.getResponse().setStatusCode(HttpStatus.PRECONDITION_FAILED);
                    return null;
                }
            }
            exchange.getResponse().getHeaders().set("ETag", eTag);
            return buildRequestMetadata(exchange, exchange.getRequest());
        })).flatMap(response -> response == null ? Mono.empty() : rsOk(exchange, response));
    }

    /** Reactive equivalent of servlet {@code responseHeaders()}. */
    public Mono<Void> responseHeaders(ServerWebExchange exchange) {
        final ServerHttpRequest request = exchange.getRequest();
        return drainBody(exchange).then(Mono.fromCallable(() -> {
            // Echo every query param as a response header
            request.getQueryParams().forEach((name, values) -> {
                for (String v : values) {
                    exchange.getResponse().getHeaders().add(name, v);
                }
            });

            // Build the JSON describing the response
            return buildRequestMetadata(exchange, request);
        })).flatMap(response -> rsOk(exchange, response));
    }

    /**
     * Build the JSON payload describing a request: {@code args},
     * {@code headers}, {@code origin}, {@code url}.
     */
    private JSONObject buildRequestMetadata(ServerWebExchange exchange, ServerHttpRequest request) {
        JSONObject response = new JSONObject();
        response.put("args", listMapToJSON(queryParamMap(request)));
        response.put("headers", mapHeadersToJSON(request.getHeaders()));
        response.put("origin", getOrigin(exchange));
        response.put("url", getFullURL(exchange));
        return response;
    }
}
