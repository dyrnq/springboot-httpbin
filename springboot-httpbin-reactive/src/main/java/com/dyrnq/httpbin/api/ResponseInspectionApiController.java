package com.dyrnq.httpbin.api;

import com.dyrnq.httpbin.component.ResponseInspectionService;
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
public class ResponseInspectionApiController implements ResponseInspectionApi {

    @Autowired
    ResponseInspectionService responseInspectionService;

    @Override
    public Mono<ResponseEntity<Void>> cacheGet(String ifModifiedSince, String ifNoneMatch, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = responseInspectionService.cache(exchange);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> cacheValueGet(Integer value, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = responseInspectionService.cacheValue(exchange, value);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> etagEtagGet(String etag, String ifNoneMatch, String ifMatch, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = responseInspectionService.etag(exchange, etag);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> responseHeadersGet(String freeform, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = responseInspectionService.responseHeaders(exchange);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> responseHeadersPost(String freeform, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = responseInspectionService.responseHeaders(exchange);
        return result.then(Mono.empty());
    }
}
