package com.dyrnq.httpbin.api;

import com.dyrnq.httpbin.component.ResponseFormatsService;
import jakarta.annotation.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.9.0-SNAPSHOT")
@Controller
@RequestMapping("${openapi.springboot-httpbin.base-path:}")
public class ResponseFormatsApiController implements ResponseFormatsApi {

    @Autowired
    ResponseFormatsService responseFormatsService;

    @Override
    public Mono<ResponseEntity<Void>> brotliGet(ServerWebExchange exchange) throws Exception {
        Mono<Void> result = responseFormatsService.brotli(exchange);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> deflateGet(ServerWebExchange exchange) throws Exception {
        Mono<Void> result = responseFormatsService.deflate(exchange);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> denyGet(ServerWebExchange exchange) throws Exception {
        Mono<Void> result = responseFormatsService.deny(exchange);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> encodingUtf8Get(ServerWebExchange exchange) throws Exception {
        Mono<Void> result = responseFormatsService.encodingUtf8(exchange);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> gzipGet(ServerWebExchange exchange) throws Exception {
        Mono<Void> result = responseFormatsService.gzip(exchange);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> htmlGet(ServerWebExchange exchange) throws Exception {
        Mono<Void> result = responseFormatsService.html(exchange);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> jsonGet(ServerWebExchange exchange) throws Exception {
        Mono<Void> result = responseFormatsService.json(exchange);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> robotsTxtGet(ServerWebExchange exchange) throws Exception {
        Mono<Void> result = responseFormatsService.robots(exchange);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> xmlGet(ServerWebExchange exchange) throws Exception {
        Mono<Void> result = responseFormatsService.xml(exchange);
        return result.then(Mono.empty());
    }
}
