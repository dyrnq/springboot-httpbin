package com.dyrnq.httpbin.component;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Reactive (WebFlux) port of the servlet
 * {@code com.dyrnq.httpbin.component.ImagesService}.
 *
 * <p>Serves binary image content based on a {@code type} path variable
 * (or, for {@code /image}, the request's {@code Accept} header).
 */
@Component
public class ImagesService extends ReactiveBaseService {

    private final Map<String, String[]> images = new HashMap<>() {{
        put("image/jpeg", new String[]{"image/jpeg", "images/jackal.jpg"});
        put("image/jpg", new String[]{"image/jpeg", "images/jackal.jpg"});
        put("image/webp", new String[]{"image/webp", "images/wolf_1.webp"});
        put("image/svg+xml", new String[]{"image/svg+xml", "images/svg_logo.svg"});
        put("image/png", new String[]{"image/png", "images/pig_icon.png"});
        put("*/*", new String[]{"image/webp", "images/wolf_1.webp"});
        put("image/*", new String[]{"image/webp", "images/wolf_1.webp"});
    }};

    /** {@code GET /image} — choose format from {@code Accept} header. */
    public Mono<Void> image(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        String acceptHeader = request.getHeaders().getFirst("Accept");
        String accept = acceptHeader != null ? acceptHeader : "*/*";
        accept = StringUtils.lowerCase(accept);
        return serveImage(exchange, accept);
    }

    /** {@code GET /image/png} — always return pig_icon.png. */
    public Mono<Void> imagePng(ServerWebExchange exchange) {
        return drainBody(exchange).then(serveImage(exchange, "image/png"));
    }

    /** {@code GET /image/jpeg} — always return jackal.jpg. */
    public Mono<Void> imageJpeg(ServerWebExchange exchange) {
        return drainBody(exchange).then(serveImage(exchange, "image/jpeg"));
    }

    /** {@code GET /image/svg} — always return svg_logo.svg. */
    public Mono<Void> imageSvg(ServerWebExchange exchange) {
        return drainBody(exchange).then(serveImage(exchange, "image/svg+xml"));
    }

    /** {@code GET /image/webp} — always return wolf_1.webp. */
    public Mono<Void> imageWebp(ServerWebExchange exchange) {
        return drainBody(exchange).then(serveImage(exchange, "image/webp"));
    }

    private Mono<Void> serveImage(ServerWebExchange exchange, String accept) {
        return drainBody(exchange).then(Mono.defer(() -> {
            if (!images.containsKey(accept)) {
                exchange.getResponse().setStatusCode(HttpStatus.NOT_ACCEPTABLE);
                return Mono.empty();
            }
            String[] entry = images.get(accept);
            String contentType = entry[0];
            String resourcePath = entry[1];
            exchange.getResponse().getHeaders().setContentType(MediaType.parseMediaType(contentType));
            exchange.getResponse().setStatusCode(HttpStatus.OK);
            return Mono.fromCallable(() -> {
                Resource resource = new ClassPathResource(resourcePath);
                try (InputStream is = resource.getInputStream()) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    org.springframework.util.StreamUtils.copy(is, baos);
                    return baos.toByteArray();
                }
            }).flatMap(bytes -> rsByte(exchange, bytes,
                    HttpStatus.OK.value(), contentType));
        }));
    }
}
