package com.dyrnq.httpbin.api;

import com.dyrnq.httpbin.component.AuthService;
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
public class AuthApiController implements AuthApi {

    @Autowired
    AuthService authService;

    @Override
    public Mono<ResponseEntity<Void>> basicAuthUserPasswdGet(String user, String passwd, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = authService.basicAuth(exchange, user, passwd);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> bearerGet(String authorization, String token, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = authService.bearer(exchange, authorization, token);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> digestAuthQopUserPasswdAlgorithmGet(String qop, String user, String passwd, String algorithm, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = authService.digestAuth(exchange, qop, user, passwd, algorithm, "never");
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> digestAuthQopUserPasswdAlgorithmStaleAfterGet(String qop, String user, String passwd, String algorithm, String staleAfter, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = authService.digestAuth(exchange, qop, user, passwd, algorithm, staleAfter);
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> digestAuthQopUserPasswdGet(String qop, String user, String passwd, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = authService.digestAuth(exchange, qop, user, passwd, "MD5", "never");
        return result.then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<Void>> hiddenBasicAuthUserPasswdGet(String user, String passwd, ServerWebExchange exchange) throws Exception {
        Mono<Void> result = authService.hiddenBasicAuth(exchange, user, passwd);
        return result.then(Mono.empty());
    }
}
