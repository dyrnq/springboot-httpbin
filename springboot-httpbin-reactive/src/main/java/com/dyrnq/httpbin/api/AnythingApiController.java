package com.dyrnq.httpbin.api;

import com.dyrnq.httpbin.component.AnythingService;
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
public class AnythingApiController implements AnythingApi {

    @Autowired
    AnythingService anythingService;

    @Override
    public Mono<ResponseEntity<Void>> anythingAnythingDelete(String anything, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = anythingService.anything(exchange, anything);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> anythingAnythingGet(String anything, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = anythingService.anything(exchange, anything);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> anythingAnythingPatch(String anything, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = anythingService.anything(exchange, anything);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> anythingAnythingPost(String anything, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = anythingService.anything(exchange, anything);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> anythingAnythingPut(String anything, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = anythingService.anything(exchange, anything);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> anythingDelete(ServerWebExchange exchange) throws Exception {
        Mono<Void> result = anythingService.anything(exchange);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> anythingGet(ServerWebExchange exchange) throws Exception {
        Mono<Void> result = anythingService.anything(exchange);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> anythingPatch(ServerWebExchange exchange) throws Exception {
        Mono<Void> result = anythingService.anything(exchange);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> anythingPost(ServerWebExchange exchange) throws Exception {
        Mono<Void> result = anythingService.anything(exchange);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> anythingPut(ServerWebExchange exchange) throws Exception {
        Mono<Void> result = anythingService.anything(exchange);
        return result.then(Mono.empty());
    }
}
