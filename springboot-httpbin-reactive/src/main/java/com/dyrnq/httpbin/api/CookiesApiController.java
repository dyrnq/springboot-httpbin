package com.dyrnq.httpbin.api;

import com.dyrnq.httpbin.component.CookiesService;
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
public class CookiesApiController implements CookiesApi {

    @Autowired
    CookiesService cookiesService;

    @Override
    public Mono<ResponseEntity<Void>> cookiesDeleteGet(String freeform, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = cookiesService.cookiesDelete(exchange);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> cookiesGet(ServerWebExchange exchange) throws Exception {
        Mono<Void> result = cookiesService.cookies(exchange);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> cookiesSetGet(String freeform, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = cookiesService.cookiesSetFromQuery(exchange);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> cookiesSetNameValueGet(String name, String value, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = cookiesService.cookiesSet(exchange, name, value);
        return result.then(Mono.empty());
    }
}
