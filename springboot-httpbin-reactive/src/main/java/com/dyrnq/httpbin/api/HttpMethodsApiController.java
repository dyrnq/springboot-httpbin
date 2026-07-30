package com.dyrnq.httpbin.api;

import com.dyrnq.httpbin.component.HttpMethodsService;
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
public class HttpMethodsApiController implements HttpMethodsApi {

    @Autowired
    HttpMethodsService httpMethodsService;

    @Override
    public Mono<ResponseEntity<Void>> deleteDelete(ServerWebExchange exchange) throws Exception {
        Mono<Void> result = httpMethodsService.httpMethods(exchange);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> getGet(ServerWebExchange exchange) throws Exception {
        Mono<Void> result = httpMethodsService.httpMethods(exchange);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> patchPatch(ServerWebExchange exchange) throws Exception {
        Mono<Void> result = httpMethodsService.httpMethods(exchange);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> postPost(ServerWebExchange exchange) throws Exception {
        Mono<Void> result = httpMethodsService.httpMethods(exchange);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> putPut(ServerWebExchange exchange) throws Exception {
        Mono<Void> result = httpMethodsService.httpMethods(exchange);
        return result.then(Mono.empty());
    }
}
