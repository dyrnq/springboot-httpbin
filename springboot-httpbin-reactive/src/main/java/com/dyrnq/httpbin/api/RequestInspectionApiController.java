package com.dyrnq.httpbin.api;

import com.dyrnq.httpbin.component.RequestInspectionService;
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
public class RequestInspectionApiController implements RequestInspectionApi {

    @Autowired
    RequestInspectionService requestInspectionService;

    @Override
    public Mono<ResponseEntity<Void>> headersGet(ServerWebExchange exchange) throws Exception {
        Mono<Void> result = requestInspectionService.headers(exchange);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> ipGet(ServerWebExchange exchange) throws Exception {
        Mono<Void> result = requestInspectionService.ip(exchange);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> userAgentGet(ServerWebExchange exchange) throws Exception {
        Mono<Void> result = requestInspectionService.userAgent(exchange);
        return result.then(Mono.empty());
    }
}
