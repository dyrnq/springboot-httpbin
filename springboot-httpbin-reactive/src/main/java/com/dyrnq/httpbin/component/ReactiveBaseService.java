package com.dyrnq.httpbin.component;

import cn.hutool.core.util.URLUtil;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reactive (WebFlux) port of the servlet
 * {@code com.dyrnq.httpbin.component.BaseService}.
 *
 * <p>Provides the same set of helpers as the servlet version, but
 * adapted to the reactive paradigm:
 * <ul>
 *   <li>{@link HttpServletRequest}/{@link HttpServletResponse}
 *       → {@link ServerWebExchange};</li>
 *   <li>blocking {@code InputStream} → {@code Flux<DataBuffer>};</li>
 *   <li>writing via {@code OutputStream} → reactive
 *       {@code response.writeWith(...)};</li>
 *   <li>direct mutation of {@code HttpServletResponse} → mutation of
 *       {@code exchange.getResponse()} (status, headers) and a final
 *       {@code Mono<Void>} completing the chain.</li>
 * </ul>
 *
 * <p>Concrete reactive services should {@code extends ReactiveBaseService}.
 */
@Component
public class ReactiveBaseService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${openapi.springboot-httpbin.base-path:}")
    private String openapiControllerBasePath;

    // =========================================================================
    // Body reading
    // =========================================================================

    /**
     * Drain the request body without keeping its content (matches the
     * servlet {@code Utils.copy(is, Utils.NULL_OUTPUT_STREAM)} idiom).
     */
    public Mono<Void> drainBody(ServerWebExchange exchange) {
        return exchange.getRequest().getBody().then();
    }

    /**
     * Read the request body as UTF-8 String. DataBuffers are released.
     */
    public Mono<String> readBody(ServerWebExchange exchange) {
        return exchange.getRequest().getBody()
                .map(this::dataBufferToString)
                .reduce("", (acc, s) -> acc + s)
                .defaultIfEmpty("");
    }

    private String dataBufferToString(DataBuffer buffer) {
        byte[] bytes = dataBufferToBytes(buffer);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private byte[] dataBufferToBytes(DataBuffer buffer) {
        try {
            byte[] bytes = new byte[buffer.readableByteCount()];
            buffer.read(bytes);
            return bytes;
        } finally {
            DataBufferUtils.release(buffer);
        }
    }

    // =========================================================================
    // Query parameter / Header helpers
    // =========================================================================

    /**
     * Parse the raw query string into a multi-value Map, matching the
     * servlet {@code queryParamMap()} behaviour.
     */
    public Map<String, List<String>> queryParamMap(ServerHttpRequest request) {
        Map<String, List<String>> q = new HashMap<>();
        URI uri = request.getURI();
        String rawQuery = uri.getRawQuery();
        if (rawQuery != null) {
            for (String param : rawQuery.split("&")) {
                String[] keyValue = param.split("=", 2);
                try {
                    String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                    String value = keyValue.length > 1
                            ? URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8)
                            : "";
                    q.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
                } catch (Exception e) {
                    logger.warn("Failed to parse query param: {}", param, e);
                }
            }
        }
        return q;
    }

    /**
     * Convert {@link HttpHeaders} to a JSON object (single value or
     * JSONArray for multi-valued headers). Mirrors the servlet
     * {@code mapHeadersToJSON()}.
     */
    public JSONObject mapHeadersToJSON(HttpHeaders headers) {
        JSONObject json = new JSONObject();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            String name = entry.getKey();
            List<String> values = entry.getValue();
            if (values.size() == 1) {
                json.put(name, values.get(0));
            } else {
                json.put(name, new JSONArray(values));
            }
        }
        return json;
    }

    /**
     * Convert a {@code Map<String, List<String>>} (e.g. query params)
     * to a JSON object, matching {@code jsonObject(Map)}.
     */
    public JSONObject listMapToJSON(Map<String, List<String>> map) {
        JSONObject json = new JSONObject();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            List<String> val = entry.getValue();
            if (val.size() > 1) {
                json.put(entry.getKey(), new JSONArray(val));
            } else {
                json.put(entry.getKey(), val.get(0));
            }
        }
        return json;
    }

    // =========================================================================
    // Cookie helpers
    // =========================================================================

    /**
     * Decode request cookies into a JSON object, matching the servlet
     * {@code cookies.put(URLUtil.decode(...), URLUtil.decode(...))} pattern.
     */
    public JSONObject readCookiesAsJSON(ServerHttpRequest request) {
        JSONObject cookies = new JSONObject();
        request.getCookies().forEach((name, httpCookies) -> {
            for (HttpCookie cookie : httpCookies) {
                cookies.put(URLUtil.decode(cookie.getName()), URLUtil.decode(cookie.getValue()));
            }
        });
        return cookies;
    }

    /**
     * Append a {@code Set-Cookie} header. Mirrors
     * {@code servletResponse.addHeader("Set-Cookie", ...)}.
     */
    public void addSetCookie(ServerHttpResponse response, String name, String value) {
        response.getHeaders().add("Set-Cookie",
                String.format("%s=%s; Path=/",
                        URLUtil.encode(name), URLUtil.encode(value)));
    }

    /**
     * Append a clearing {@code Set-Cookie} header.
     */
    public void clearSetCookie(ServerHttpResponse response, String name) {
        response.getHeaders().add("Set-Cookie",
                String.format("%s=; Path=/", URLUtil.encode(name)));
    }

    // =========================================================================
    // URL / Origin / Scheme
    // =========================================================================

    /**
     * Reconstruct the full request URL from the exchange URI.
     */
    public String getFullURL(ServerWebExchange exchange) {
        URI uri = exchange.getRequest().getURI();
        StringBuilder url = new StringBuilder();
        url.append(uri.getScheme()).append("://")
           .append(uri.getAuthority())
           .append(uri.getRawPath());
        if (uri.getRawQuery() != null) {
            url.append('?').append(uri.getRawQuery());
        }
        return url.toString();
    }

    /**
     * Best-effort client IP — checks the standard proxy headers and
     * falls back to the remote address.
     */
    public String getOrigin(ServerWebExchange exchange) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String ip = firstNonEmptyHeader(headers,
                "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP");
        if (ip != null) {
            return ip;
        }
        var remote = exchange.getRequest().getRemoteAddress();
        if (remote != null && remote.getAddress() != null) {
            return remote.getAddress().getHostAddress();
        }
        return "";
    }

    /**
     * Request scheme, honouring the {@code X-Forwarded-Proto} header.
     */
    public String getScheme(ServerWebExchange exchange) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String scheme = headers.getFirst("X-Forwarded-Proto");
        if (scheme == null || scheme.isEmpty() || "unknown".equalsIgnoreCase(scheme)) {
            return exchange.getRequest().getURI().getScheme();
        }
        return scheme;
    }

    private static String firstNonEmptyHeader(HttpHeaders headers, String... names) {
        for (String name : names) {
            String v = headers.getFirst(name);
            if (v != null && !v.isEmpty() && !"unknown".equalsIgnoreCase(v)) {
                return v;
            }
        }
        return null;
    }

    // =========================================================================
    // Response writing
    // =========================================================================

    /**
     * Convenience: 200 OK with a JSON body.
     */
    public Mono<Void> rsOk(ServerWebExchange exchange, JSONObject obj) {
        return rsJson(exchange, obj, HttpStatus.OK.value());
    }

    /**
     * Write a JSON object with the given status code.
     */
    public Mono<Void> rsJson(ServerWebExchange exchange, JSONObject obj, int code) {
        byte[] body = obj.toString(2).getBytes(StandardCharsets.UTF_8);
        return rsByte(exchange, body, code, MediaType.APPLICATION_JSON_VALUE);
    }

    /**
     * Write raw bytes with a status code and content type to the
     * reactive response. Sets status, content-length, content-type,
     * and writes the body via {@code response.writeWith(...)}.
     */
    public Mono<Void> rsByte(ServerWebExchange exchange, byte[] body, int code, String contentType) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentLength(body.length);
        if (contentType != null) {
            response.getHeaders().setContentType(MediaType.parseMediaType(contentType));
        }
        response.setStatusCode(HttpStatus.valueOf(code));
        DataBuffer buffer = response.bufferFactory().allocateBuffer(body.length);
        buffer.write(body);
        return response.writeWith(Mono.just(buffer));
    }

    // =========================================================================
    // Redirect
    // =========================================================================

    public Mono<Void> redirectTo(ServerWebExchange exchange, String location) {
        return redirectTo(exchange, location, HttpStatus.FOUND.value());
    }

    public Mono<Void> redirectTo(ServerWebExchange exchange, String location, int statusCode) {
        return redirectTo(exchange, location, statusCode, false);
    }

    public Mono<Void> redirectTo(ServerWebExchange exchange, String location, int statusCode, boolean absolute) {
        String fullLocation = location;
        if (location != null && !location.toLowerCase().startsWith("http")) {
            if (!Strings.isNullOrEmpty(openapiControllerBasePath)) {
                fullLocation = openapiControllerBasePath.replaceAll("/$", "") + location;
            }
            if (absolute) {
                URI uri = exchange.getRequest().getURI();
                fullLocation = uri.getScheme() + "://" + uri.getAuthority() + fullLocation;
            }
        }
        logger.debug("location={}, fullLocation={}", location, fullLocation);
        exchange.getResponse().getHeaders().set("Location", fullLocation);
        exchange.getResponse().setStatusCode(HttpStatus.valueOf(statusCode));
        return Mono.empty();
    }

    // =========================================================================
    // The big one: anything() — the mirror endpoint logic
    // =========================================================================

    /**
     * Reactive equivalent of the servlet {@code BaseService.anything()}
     * method. Returns a JSON object describing the request.
     *
     * <p>Three body-handling branches (matching the servlet):
     * <ul>
     *   <li>{@code multipart/form-data}: read via
     *       {@code exchange.getMultipartData()};</li>
     *   <li>{@code application/x-www-form-urlencoded}: read body bytes
     *       and parse as form data;</li>
     *   <li>otherwise: return body as-is, attempt JSON parse.</li>
     * </ul>
     */
    public Mono<Void> anything(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();
        final String contentType;
        String rawContentType = headers.getFirst(HttpHeaders.CONTENT_TYPE);
        contentType = (rawContentType != null) ? rawContentType.toLowerCase() : null;

        Mono<String> bodyMono = exchange.getRequest().getBody()
                .map(this::dataBufferToString)
                .reduce("", (a, b) -> a + b)
                .defaultIfEmpty("");
        Mono<MultiValueMap<String, Part>> multiMono = exchange.getMultipartData()
                .defaultIfEmpty(new LinkedMultiValueMap<>());

        return Mono.zip(bodyMono, multiMono).flatMap(tuple -> {
            String body = tuple.getT1();
            MultiValueMap<String, Part> parts = tuple.getT2();

            JSONObject response = new JSONObject();
            response.put("json", JSONObject.NULL);

            if (contentType != null && contentType.startsWith("multipart/form-data")) {
                response.put("data", "");
                response.put("form", multipartFormToJSON(parts));
                response.put("files", multipartFilesToJSON(parts));
            } else if (contentType != null && contentType.startsWith("application/x-www-form-urlencoded")) {
                response.put("data", "");
                response.put("files", new JSONObject());
                response.put("form", parseUrlEncoded(body));
            } else {
                response.put("form", new JSONObject());
                response.put("files", new JSONObject());
                response.put("data", body);
                try {
                    if (!body.isEmpty()) {
                        response.put("json", new JSONObject(body));
                    }
                } catch (JSONException e) {
                    // client can provide non-JSON data
                }
            }

            response.put("method", request.getMethod() != null
                    ? request.getMethod().name() : "");
            response.put("args", listMapToJSON(queryParamMap(request)));
            response.put("headers", mapHeadersToJSON(headers));
            response.put("origin", getOrigin(exchange));
            response.put("url", getFullURL(exchange));

            return rsOk(exchange, response);
        });
    }

    private JSONObject multipartFormToJSON(MultiValueMap<String, Part> parts) {
        JSONObject json = new JSONObject();
        parts.forEach((name, partList) -> {
            for (Part part : partList) {
                if (part instanceof FormFieldPart) {
                    json.put(name, ((FormFieldPart) part).value());
                }
            }
        });
        return json;
    }

    private JSONObject multipartFilesToJSON(MultiValueMap<String, Part> parts) {
        JSONObject json = new JSONObject();
        parts.forEach((name, partList) -> {
            for (Part part : partList) {
                if (part instanceof FilePart) {
                    json.put(name, ((FilePart) part).filename());
                }
            }
        });
        return json;
    }

    private JSONObject parseUrlEncoded(String data) {
        JSONObject json = new JSONObject();
        if (data != null && !data.isEmpty()) {
            for (String param : data.split("&")) {
                String[] kv = param.split("=", 2);
                String key = kv[0];
                String value = kv.length > 1 ? kv[1] : "";
                if (json.has(key)) {
                    Object existing = json.get(key);
                    if (existing instanceof JSONArray) {
                        ((JSONArray) existing).put(value);
                    } else {
                        JSONArray arr = new JSONArray();
                        arr.put(existing);
                        arr.put(value);
                        json.put(key, arr);
                    }
                } else {
                    json.put(key, value);
                }
            }
        }
        return json;
    }
}
