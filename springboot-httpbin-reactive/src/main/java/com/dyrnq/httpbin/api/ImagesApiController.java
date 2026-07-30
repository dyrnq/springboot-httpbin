package com.dyrnq.httpbin.api;

import com.dyrnq.httpbin.component.ImagesService;
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
public class ImagesApiController implements ImagesApi {

    @Autowired
    ImagesService imagesService;

    @Override
    public Mono<ResponseEntity<Void>> imageGet(ServerWebExchange exchange) throws Exception {
        Mono<Void> result = imagesService.image(exchange);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> imageJpegGet(ServerWebExchange exchange) throws Exception {
        Mono<Void> result = imagesService.imageJpeg(exchange);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> imagePngGet(ServerWebExchange exchange) throws Exception {
        Mono<Void> result = imagesService.imagePng(exchange);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> imageSvgGet(ServerWebExchange exchange) throws Exception {
        Mono<Void> result = imagesService.imageSvg(exchange);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> imageWebpGet(ServerWebExchange exchange) throws Exception {
        Mono<Void> result = imagesService.imageWebp(exchange);
        return result.then(Mono.empty());
    }
}
