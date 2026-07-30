package com.dyrnq.httpbin.api;

import com.dyrnq.httpbin.component.RedirectsService;
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
public class RedirectsApiController implements RedirectsApi {

    @Autowired
    RedirectsService redirectsService;

    @Override
    public Mono<ResponseEntity<Void>> absoluteRedirectNGet(Integer n, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = redirectsService.absoluteRedirect(exchange, n);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> redirectNGet(Integer n, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = redirectsService.redirect(exchange, n);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> redirectToDelete(ServerWebExchange exchange) throws Exception {
        Mono<Void> result = redirectsService.redirectToNoUrl(exchange, null);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> redirectToGet(String url, Integer statusCode, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = redirectsService.redirectToGet(exchange, url, statusCode);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> redirectToPatch(ServerWebExchange exchange) throws Exception {
        Mono<Void> result = redirectsService.redirectToNoUrl(exchange, null);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> redirectToPost(String url, Integer statusCode, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = redirectsService.redirectToGet(exchange, url, statusCode);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> redirectToPut(String url, Integer statusCode, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = redirectsService.redirectToGet(exchange, url, statusCode);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> relativeRedirectNGet(Integer n, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = redirectsService.relativeRedirect(exchange, n);
        return result.then(Mono.empty());
    }
}
