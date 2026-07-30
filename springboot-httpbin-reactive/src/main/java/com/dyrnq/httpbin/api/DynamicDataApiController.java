package com.dyrnq.httpbin.api;

import com.dyrnq.httpbin.component.DynamicDataService;
import jakarta.annotation.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.9.0-SNAPSHOT")
@Controller
@RequestMapping("${openapi.springboot-httpbin.base-path:}")
public class DynamicDataApiController implements DynamicDataApi {

    @Autowired
    DynamicDataService dynamicDataService;

    @Override
    public Mono<ResponseEntity<Void>> base64ValueGet(String value, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = dynamicDataService.base64(exchange, value);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> bytesNGet(Integer n, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = dynamicDataService.bytes(exchange, n);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> delayDelayDelete(Integer delay, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = dynamicDataService.delay(exchange, delay);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> delayDelayGet(Integer delay, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = dynamicDataService.delay(exchange, delay);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> delayDelayPatch(Integer delay, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = dynamicDataService.delay(exchange, delay);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> delayDelayPost(Integer delay, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = dynamicDataService.delay(exchange, delay);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> delayDelayPut(Integer delay, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = dynamicDataService.delay(exchange, delay);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> dripGet(BigDecimal duration, Integer numbytes, Integer code, BigDecimal delay, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = dynamicDataService.drip(exchange, duration, numbytes, code, delay);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> linksNOffsetGet(Integer n, Integer offset, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = dynamicDataService.links(exchange, n, offset);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> rangeNumbytesGet(Integer numBytes, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = dynamicDataService.range(exchange, numBytes);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> streamBytesNGet(Integer n, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = dynamicDataService.streamByte(exchange, n);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> streamNGet(Integer n, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = dynamicDataService.stream(exchange, n);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> uuidGet(ServerWebExchange exchange) throws Exception {
        Mono<Void> result = dynamicDataService.uuid(exchange);
        return result.then(Mono.empty());
    }
}
