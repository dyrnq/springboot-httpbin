package com.dyrnq.httpbin.component;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reactive (WebFlux) port of the servlet
 * {@code com.dyrnq.httpbin.component.DynamicDataService}.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code /delay/{delay}} (GET/POST/PUT/PATCH/DELETE) — pause
 *       for {@code delay} seconds, then return request metadata;</li>
 *   <li>{@code /drip} — write {@code numbytes} bytes spaced evenly
 *       over {@code duration} seconds;</li>
 *   <li>{@code /stream/{n}} — emit JSON once per second for n iterations;</li>
 *   <li>{@code /stream-bytes/{n}} — random bytes chunked by
 *       {@code chunkSize};</li>
 *   <li>{@code /uuid} — random UUID;</li>
 *   <li>{@code /base64/{value}} — decode the supplied base64 value;</li>
 *   <li>{@code /bytes/{n}} — n random bytes (deterministic if {@code seed} given);</li>
 *   <li>{@code /range/{n}} — Range header handling (200/206/416);</li>
 *   <li>{@code /links/{n}/{offset}} — HTML page of links.</li>
 * </ul>
 */
@Component
public class DynamicDataService extends ReactiveBaseService {

    private static final int MAX_DELAY_MS = 10 * 1000;

    // =========================================================================
    // /delay/{delay}
    // =========================================================================

    public Mono<Void> delay(ServerWebExchange exchange, int delaySeconds) {
        final int delayMs = Math.min(delaySeconds * 1000, MAX_DELAY_MS);
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();
        final String contentType;
        String raw = headers.getFirst(HttpHeaders.CONTENT_TYPE);
        contentType = (raw != null) ? raw.toLowerCase() : null;

        Mono<String> bodyMono = request.getBody()
                .map(this::dataBufferToString)
                .reduce("", (a, b) -> a + b)
                .defaultIfEmpty("");
        Mono<MultiValueMap<String, Part>> multiMono = exchange.getMultipartData()
                .defaultIfEmpty(new LinkedMultiValueMap<>());

        return Mono.zip(Mono.delay(Duration.ofMillis(delayMs)), bodyMono, multiMono)
                .flatMap(tuple -> {
                    String body = tuple.getT2();
                    MultiValueMap<String, Part> parts = tuple.getT3();
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
                        } catch (Exception ignored) {
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

    // =========================================================================
    // /drip
    // =========================================================================

    public Mono<Void> drip(ServerWebExchange exchange,
                           java.math.BigDecimal duration,
                           Integer numbytes,
                           Integer code,
                           java.math.BigDecimal delay) {
        ServerHttpRequest request = exchange.getRequest();
        Map<String, List<String>> params = queryParamMap(request);
        double durationSec = duration != null ? duration.doubleValue()
                : getDoubleParam(params, "duration", 0.0);
        int numBytes = numbytes != null ? numbytes : getIntParam(params, "numbytes", 10);
        int statusCode = code != null ? code : getIntParam(params, "code", 200);
        double delaySec = delay != null ? delay.doubleValue()
                : getDoubleParam(params, "delay", 0.0);
        long durationMs = (long) (1000 * durationSec);
        int delaySeconds = (int) delaySec;

        if (numBytes <= 0) {
            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
            return Mono.empty();
        }
        return drainBody(exchange).then(Mono.defer(() -> {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.valueOf(statusCode));
            response.getHeaders().setContentType(MediaType.APPLICATION_OCTET_STREAM);
            long perByteMs = Math.max(durationMs / Math.max(numBytes, 1), 1L);
            Flux<DataBuffer> body = Flux.range(0, numBytes)
                    .delayElements(Duration.ofMillis(perByteMs))
                    .delaySubscription(Duration.ofSeconds(delaySeconds))
                    .map(i -> wrapSingleByte(response.bufferFactory(), (byte) '*'));
            return response.writeWith(body);
        }));
    }

    // =========================================================================
    // /stream/{n}
    // =========================================================================

    public Mono<Void> stream(ServerWebExchange exchange, int n) {
        return drainBody(exchange).then(Mono.defer(() -> {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.OK);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            AtomicInteger counter = new AtomicInteger();
            Flux<DataBuffer> body = Flux.interval(Duration.ofSeconds(1))
                    .take(n)
                    .map(i -> {
                        JSONObject obj = new JSONObject();
                        obj.put("args", listMapToJSON(queryParamMap(exchange.getRequest())));
                        obj.put("headers", mapHeadersToJSON(exchange.getRequest().getHeaders()));
                        obj.put("origin", getOrigin(exchange));
                        obj.put("url", getFullURL(exchange));
                        obj.put("id", counter.getAndIncrement());
                        byte[] payload = (obj.toString() + "\n").getBytes(StandardCharsets.UTF_8);
                        return wrapBytes(response.bufferFactory(), payload);
                    });
            return response.writeWith(body);
        }));
    }

    // =========================================================================
    // /stream-bytes/{n}
    // =========================================================================

    public Mono<Void> streamByte(ServerWebExchange exchange, int numBytes) {
        ServerHttpRequest request = exchange.getRequest();
        Map<String, List<String>> params = queryParamMap(request);
        int seed = getIntParam(params, "seed", -1);
        int chunkSize = getIntParam(params, "chunkSize", 200);

        return drainBody(exchange).then(Mono.defer(() -> {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.OK);
            response.getHeaders().setContentType(MediaType.APPLICATION_OCTET_STREAM);
            Random random = seed == -1 ? new Random() : new Random(seed);

            // Emit chunks
            long chunks = (numBytes + chunkSize - 1) / chunkSize;
            Flux<DataBuffer> body = Flux.range(0, (int) chunks)
                    .map(i -> {
                        long offset = (long) i * chunkSize;
                        int remaining = (int) Math.min(chunkSize, numBytes - offset);
                        byte[] buf = new byte[remaining];
                        random.nextBytes(buf);
                        return wrapBytes(response.bufferFactory(), buf);
                    });
            return response.writeWith(body);
        }));
    }

    // =========================================================================
    // /uuid
    // =========================================================================

    public Mono<Void> uuid(ServerWebExchange exchange) {
        return drainBody(exchange).then(Mono.fromCallable(() -> {
            JSONObject response = new JSONObject();
            response.put("uuid", UUID.randomUUID().toString());
            return response;
        })).flatMap(response -> rsOk(exchange, response));
    }

    // =========================================================================
    // /base64/{value}
    // =========================================================================

    public Mono<Void> base64(ServerWebExchange exchange, String value) {
        return drainBody(exchange).then(Mono.fromCallable(() -> {
            byte[] body = "Incorrect Base64 data try: SFRUUEJJTiBpcyBhd2Vzb21l"
                    .getBytes(StandardCharsets.UTF_8);
            try {
                if (isBase64Encoded(value)) {
                    body = Base64.decodeBase64(value);
                }
            } catch (Throwable ignored) {
                // fall through with default body
            }
            exchange.getResponse().setStatusCode(HttpStatus.OK);
            return body;
        })).flatMap(body -> rsByte(exchange, body, HttpStatus.OK.value(),
                MediaType.APPLICATION_OCTET_STREAM_VALUE));
    }

    // =========================================================================
    // /bytes/{n}
    // =========================================================================

    public Mono<Void> bytes(ServerWebExchange exchange, int numBytes) {
        ServerHttpRequest request = exchange.getRequest();
        Map<String, List<String>> params = queryParamMap(request);
        int seed = getIntParam(params, "seed", -1);
        Random random = seed != -1 ? new Random(seed) : new Random();

        return drainBody(exchange).then(Mono.defer(() -> {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.OK);
            response.getHeaders().setContentType(MediaType.APPLICATION_OCTET_STREAM);
            response.getHeaders().setContentLength(numBytes);
            byte[] buffer = new byte[Math.min(numBytes, 4096)];
            long remaining = numBytes;
            AtomicInteger cursor = new AtomicInteger(0);
            // Emit chunks until we've covered all numBytes bytes
            return response.writeWith(Flux.generate(() -> remaining,
                    (state, sink) -> {
                        long left = state;
                        if (left <= 0) {
                            sink.complete();
                            return 0L;
                        }
                        int chunk = (int) Math.min(buffer.length, left);
                        random.nextBytes(buffer);
                        byte[] out = new byte[chunk];
                        System.arraycopy(buffer, 0, out, 0, chunk);
                        sink.next(wrapBytes(response.bufferFactory(), out));
                        cursor.addAndGet(chunk);
                        return left - chunk;
                    }));
        }));
    }

    // =========================================================================
    // /range/{n}
    // =========================================================================

    public Mono<Void> range(ServerWebExchange exchange, int numBytes) {
        return drainBody(exchange).then(Mono.defer(() -> {
            ServerHttpResponse response = exchange.getResponse();
            long size = numBytes;
            long start;
            long end;
            String range = exchange.getRequest().getHeaders().getFirst("Range");
            if (range != null && range.startsWith("bytes=")) {
                String r = range.substring("bytes=".length());
                String[] ranges = r.split("-", 2);
                if (ranges[0].isEmpty()) {
                    start = size - Long.parseLong(ranges[1]);
                    end = size - 1;
                } else if (ranges[1].isEmpty()) {
                    start = Long.parseLong(ranges[0]);
                    end = size - 1;
                } else {
                    start = Long.parseLong(ranges[0]);
                    end = Long.parseLong(ranges[1]);
                }
                if (end + 1 > size || start > end) {
                    response.setStatusCode(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE);
                    response.getHeaders().add("ETag", "range" + size);
                    response.getHeaders().add("Content-Range", "bytes */" + size);
                    return Mono.<Void>empty();
                }
                response.setStatusCode(HttpStatus.PARTIAL_CONTENT);
            } else {
                start = 0;
                end = size - 1;
                response.setStatusCode(HttpStatus.OK);
            }
            response.getHeaders().add("ETag", "range" + size);
            response.getHeaders().add("Content-Length", String.valueOf(end - start + 1));
            response.getHeaders().add("Content-Range", "bytes " + start + "-" + end + "/" + size);
            response.getHeaders().add("Accept-Ranges", "bytes");
            // Build body
            int length = (int) (end - start + 1);
            byte[] body = new byte[length];
            for (long i = start; i <= end; ++i) {
                body[(int) (i - start)] = (byte) ('a' + (i % 26));
            }
            return rsByte(exchange, body, response.getStatusCode().value(),
                    MediaType.APPLICATION_OCTET_STREAM_VALUE);
        }));
    }

    // =========================================================================
    // /links/{n}/{offset}
    // =========================================================================

    public Mono<Void> links(ServerWebExchange exchange, int n, int offset) {
        return drainBody(exchange).then(Mono.fromCallable(() -> {
            StringBuilder body = new StringBuilder();
            body.append("<html><head><title>Links</title></head><body>");
            for (int i = 0; i < n; i++) {
                if (i == offset) {
                    body.append(String.format("%d ", i));
                } else {
                    body.append(String.format(
                            "<a href=\"%s/links/%d/%d\">%d</a> ",
                            "", n, i, i));
                }
            }
            body.append("</body></html>");
            return body.toString().getBytes(StandardCharsets.UTF_8);
        })).flatMap(bytes -> rsByte(exchange, bytes, HttpStatus.OK.value(),
                "text/html; charset=utf-8"));
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private int getIntParam(Map<String, List<String>> params, String name, int def) {
        List<String> v = params.get(name);
        if (v == null || v.isEmpty()) {
            return def;
        }
        try {
            return Integer.parseInt(v.get(0));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private double getDoubleParam(Map<String, List<String>> params, String name, double def) {
        List<String> v = params.get(name);
        if (v == null || v.isEmpty()) {
            return def;
        }
        try {
            return Double.parseDouble(v.get(0));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private DataBuffer wrapSingleByte(DataBufferFactory factory, byte b) {
        return wrapBytes(factory, new byte[]{b});
    }

    private DataBuffer wrapBytes(DataBufferFactory factory, byte[] bytes) {
        DataBuffer buf = factory.allocateBuffer(bytes.length);
        buf.write(bytes);
        return buf;
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
                    if (existing instanceof org.json.JSONArray) {
                        ((org.json.JSONArray) existing).put(value);
                    } else {
                        org.json.JSONArray arr = new org.json.JSONArray();
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

    private String dataBufferToString(DataBuffer buffer) {
        byte[] bytes = new byte[buffer.readableByteCount()];
        buffer.read(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static final Pattern BASE64_PATTERN = Pattern.compile(
            "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$");

    private static boolean isBase64Encoded(String s) {
        if (s == null) {
            return false;
        }
        Matcher m = BASE64_PATTERN.matcher(s);
        return m.find();
    }
}
