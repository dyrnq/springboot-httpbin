package com.dyrnq.httpbin.api;

import com.dyrnq.httpbin.component.StatusCodesService;
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
public class StatusCodesApiController implements StatusCodesApi {

    @Autowired
    StatusCodesService statusCodesService;

    @Override
    public Mono<ResponseEntity<Void>> statusCodesDelete(String codes, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = statusCodesService.status(codes, exchange);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> statusCodesGet(String codes, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = statusCodesService.status(codes, exchange);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> statusCodesPatch(String codes, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = statusCodesService.status(codes, exchange);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> statusCodesPost(String codes, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = statusCodesService.status(codes, exchange);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> statusCodesPut(String codes, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = statusCodesService.status(codes, exchange);
        return result.then(Mono.empty());
    }
}
