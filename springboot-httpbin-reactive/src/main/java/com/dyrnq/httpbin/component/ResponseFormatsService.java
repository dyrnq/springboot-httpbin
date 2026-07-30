package com.dyrnq.httpbin.component;

import cn.hutool.core.util.ZipUtil;
import com.aayushatharva.brotli4j.Brotli4jLoader;
import com.aayushatharva.brotli4j.encoder.Encoder;
import com.github.luben.zstd.Zstd;
import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * Reactive (WebFlux) port of the servlet
 * {@code com.dyrnq.httpbin.component.ResponseFormatsService}.
 *
 * <p>Provides nine endpoints, each returning a specific response shape:
 * <ul>
 *   <li>{@code /gzip}, {@code /deflate}, {@code /brotli}, {@code /zstd}
 *       — return JSON metadata compressed with the respective algorithm
 *       (Content-Encoding header set);</li>
 *   <li>{@code /html}, {@code /xml}, {@code /encoding/utf8} — return
 *       static classpath resources;</li>
 *   <li>{@code /json} — returns a hard-coded JSON slideshow;</li>
 *   <li>{@code /robots.txt} and {@code /deny} — text/plain bodies.</li>
 * </ul>
 */
@Component
public class ResponseFormatsService extends ReactiveBaseService {

    private static final String APP_JSON = MediaType.APPLICATION_JSON_VALUE;

    /** {@code GET /gzip} — gzip-compressed JSON metadata. */
    public Mono<Void> gzip(ServerWebExchange exchange) {
        return drainBody(exchange)
                .then(Mono.fromCallable(() -> {
                    JSONObject response = buildMetadata(exchange);
                    response.put("gzipped", true);
                    byte[] uncompressed = response.toString(2).getBytes(StandardCharsets.UTF_8);
                    return ZipUtil.gzip(uncompressed);
                }))
                .flatMap(compressed -> {
                    exchange.getResponse().getHeaders().set("Content-Encoding", "gzip");
                    return rsByte(exchange, compressed, 200, APP_JSON);
                });
    }

    /** {@code GET /deflate} — deflate-compressed JSON metadata. */
    public Mono<Void> deflate(ServerWebExchange exchange) {
        return drainBody(exchange)
                .then(Mono.fromCallable(() -> {
                    JSONObject response = buildMetadata(exchange);
                    response.put("deflated", true);
                    byte[] uncompressed = response.toString(2).getBytes(StandardCharsets.UTF_8);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream(uncompressed.length);
                    try (DeflaterOutputStream dos = new DeflaterOutputStream(
                            baos, new Deflater(Deflater.DEFAULT_COMPRESSION, /*nowrap=*/ true))) {
                        dos.write(uncompressed);
                    }
                    return baos.toByteArray();
                }))
                .flatMap(compressed -> {
                    exchange.getResponse().getHeaders().set("Content-Encoding", "deflate");
                    return rsByte(exchange, compressed, 200, APP_JSON);
                });
    }

    /** {@code GET /brotli} — Brotli-compressed JSON metadata. */
    public Mono<Void> brotli(ServerWebExchange exchange) {
        return drainBody(exchange)
                .then(Mono.fromCallable(() -> {
                    Brotli4jLoader.ensureAvailability();
                    JSONObject response = buildMetadata(exchange);
                    response.put("brotli", true);
                    byte[] uncompressed = response.toString(2).getBytes(StandardCharsets.UTF_8);
                    return Encoder.compress(uncompressed);
                }))
                .flatMap(compressed -> {
                    exchange.getResponse().getHeaders().set("Content-Encoding", "br");
                    return rsByte(exchange, compressed, 200, APP_JSON);
                });
    }

    /** {@code GET /zstd} — Zstd-compressed JSON metadata. */
    public Mono<Void> zstd(ServerWebExchange exchange) {
        return drainBody(exchange)
                .then(Mono.fromCallable(() -> {
                    JSONObject response = buildMetadata(exchange);
                    response.put("zstded", true);
                    byte[] uncompressed = response.toString(2).getBytes(StandardCharsets.UTF_8);
                    return Zstd.compress(uncompressed);
                }))
                .flatMap(compressed -> {
                    exchange.getResponse().getHeaders().set("Content-Encoding", "zstd");
                    return rsByte(exchange, compressed, 200, APP_JSON);
                });
    }

    /** {@code GET /html} — serve {@code moby.html} from the classpath. */
    public Mono<Void> html(ServerWebExchange exchange) {
        return drainBody(exchange).then(copyClasspathResource(
                exchange, "moby.html", "text/html; charset=utf-8"));
    }

    /** {@code GET /xml} — serve {@code sample.xml} from the classpath. */
    public Mono<Void> xml(ServerWebExchange exchange) {
        return drainBody(exchange).then(copyClasspathResource(
                exchange, "sample.xml", MediaType.APPLICATION_XML_VALUE));
    }

    /** {@code GET /encoding/utf8} — serve {@code UTF-8-demo.txt}. */
    public Mono<Void> encodingUtf8(ServerWebExchange exchange) {
        return drainBody(exchange).then(copyClasspathResource(
                exchange, "UTF-8-demo.txt", "text/html; charset=utf-8"));
    }

    /** {@code GET /json} — hard-coded slideshow JSON. */
    public Mono<Void> json(ServerWebExchange exchange) {
        final String body = "{\n"
                + "  \"slideshow\": {\n"
                + "    \"author\": \"Yours Truly\",\n"
                + "    \"date\": \"date of publication\",\n"
                + "    \"slides\": [\n"
                + "      {\n"
                + "        \"title\": \"Wake up to WonderWidgets!\",\n"
                + "        \"type\": \"all\"\n"
                + "      },\n"
                + "      {\n"
                + "        \"items\": [\n"
                + "          \"Why <em>WonderWidgets</em> are great\",\n"
                + "          \"Who <em>buys</em> WonderWidgets\"\n"
                + "        ],\n"
                + "        \"title\": \"Overview\",\n"
                + "        \"type\": \"all\"\n"
                + "      }\n"
                + "    ],\n"
                + "    \"title\": \"Sample Slide Show\"\n"
                + "  }\n"
                + "}";
        return drainBody(exchange).then(rsByte(exchange,
                body.getBytes(StandardCharsets.UTF_8), 200, APP_JSON));
    }

    /** {@code GET /robots.txt} — robots.txt body. */
    public Mono<Void> robots(ServerWebExchange exchange) {
        return drainBody(exchange).then(rsByte(exchange,
                "User-agent: *\nDisallow: /deny\n".getBytes(StandardCharsets.UTF_8),
                200, MediaType.TEXT_PLAIN_VALUE));
    }

    /** {@code GET /deny} — ASCII-art response. */
    public Mono<Void> deny(ServerWebExchange exchange) {
        final String ANGRY_ASCII = "         .-''''''-.\n"
                + "       .' _      _ '.\n"
                + "      /   O      O   \\\n"
                + "     :                :\n"
                + "     |                |\n"
                + "     :       __       :\n"
                + "      \\  .-\"`  `\"-.  /\n"
                + "       '.          .'\n"
                + "         '-......-'\n"
                + "    YOU SHOULDN'T BE HERE\n";
        return drainBody(exchange).then(rsByte(exchange,
                ANGRY_ASCII.getBytes(StandardCharsets.UTF_8),
                200, MediaType.TEXT_PLAIN_VALUE));
    }

    /** Build the standard request-metadata JSON for compression endpoints. */
    private JSONObject buildMetadata(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        JSONObject response = new JSONObject();
        response.put("args", listMapToJSON(queryParamMap(request)));
        response.put("headers", mapHeadersToJSON(request.getHeaders()));
        response.put("origin", getOrigin(exchange));
        response.put("url", getFullURL(exchange));
        return response;
    }

    /**
     * Helper: stream a classpath resource to the response with the
     * given content type. Equivalent of the servlet
     * {@code copyResource(...)} helper.
     */
    private Mono<Void> copyClasspathResource(ServerWebExchange exchange,
                                             String path,
                                             String contentType) {
        return Mono.fromCallable(() -> {
            Resource resource = new ClassPathResource(path);
            try (InputStream is = resource.getInputStream()) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                org.springframework.util.StreamUtils.copy(is, baos);
                return baos.toByteArray();
            }
        }).flatMap(bytes -> rsByte(exchange, bytes, 200, contentType));
    }
}
