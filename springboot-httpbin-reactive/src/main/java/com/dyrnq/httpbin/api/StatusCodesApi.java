/**
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech) (7.9.0-SNAPSHOT).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
package com.dyrnq.httpbin.api;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.http.codec.multipart.Part;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.annotation.Generated;

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.9.0-SNAPSHOT")
@Validated
@Tag(name = "Status codes", description = "Generates responses with given status code")
public interface StatusCodesApi {

    /**
     * DELETE /status/{codes} : Return status code or random status code if more than one are given
     *
     * @param codes  (required)
     * @return Informational responses (status code 100)
     *         or Success (status code 200)
     *         or Redirection (status code 300)
     *         or Client Errors (status code 400)
     *         or Server Errors (status code 500)
     */
    @Operation(
        operationId = "statusCodesDelete",
        summary = "Return status code or random status code if more than one are given",
        tags = { "Status codes" },
        responses = {
            @ApiResponse(responseCode = "100", description = "Informational responses"),
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "300", description = "Redirection"),
            @ApiResponse(responseCode = "400", description = "Client Errors"),
            @ApiResponse(responseCode = "500", description = "Server Errors")
        }
    )
    @RequestMapping(
        method = RequestMethod.DELETE,
        value = "/status/{codes}"
    )
    
    default Mono<ResponseEntity<Void>> statusCodesDelete(
        @Parameter(name = "codes", description = "", required = true, in = ParameterIn.PATH) @PathVariable("codes") String codes,
        @Parameter(hidden = true) final ServerWebExchange exchange
    ) throws Exception {
        Mono<Void> result = Mono.empty();
        exchange.getResponse().setStatusCode(HttpStatus.NOT_IMPLEMENTED);
        return result.then(Mono.empty());

    }


    /**
     * GET /status/{codes} : Return status code or random status code if more than one are given
     *
     * @param codes  (required)
     * @return Informational responses (status code 100)
     *         or Success (status code 200)
     *         or Redirection (status code 300)
     *         or Client Errors (status code 400)
     *         or Server Errors (status code 500)
     */
    @Operation(
        operationId = "statusCodesGet",
        summary = "Return status code or random status code if more than one are given",
        tags = { "Status codes" },
        responses = {
            @ApiResponse(responseCode = "100", description = "Informational responses"),
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "300", description = "Redirection"),
            @ApiResponse(responseCode = "400", description = "Client Errors"),
            @ApiResponse(responseCode = "500", description = "Server Errors")
        }
    )
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/status/{codes}"
    )
    
    default Mono<ResponseEntity<Void>> statusCodesGet(
        @Parameter(name = "codes", description = "", required = true, in = ParameterIn.PATH) @PathVariable("codes") String codes,
        @Parameter(hidden = true) final ServerWebExchange exchange
    ) throws Exception {
        Mono<Void> result = Mono.empty();
        exchange.getResponse().setStatusCode(HttpStatus.NOT_IMPLEMENTED);
        return result.then(Mono.empty());

    }


    /**
     * PATCH /status/{codes} : Return status code or random status code if more than one are given
     *
     * @param codes  (required)
     * @return Informational responses (status code 100)
     *         or Success (status code 200)
     *         or Redirection (status code 300)
     *         or Client Errors (status code 400)
     *         or Server Errors (status code 500)
     */
    @Operation(
        operationId = "statusCodesPatch",
        summary = "Return status code or random status code if more than one are given",
        tags = { "Status codes" },
        responses = {
            @ApiResponse(responseCode = "100", description = "Informational responses"),
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "300", description = "Redirection"),
            @ApiResponse(responseCode = "400", description = "Client Errors"),
            @ApiResponse(responseCode = "500", description = "Server Errors")
        }
    )
    @RequestMapping(
        method = RequestMethod.PATCH,
        value = "/status/{codes}"
    )
    
    default Mono<ResponseEntity<Void>> statusCodesPatch(
        @Parameter(name = "codes", description = "", required = true, in = ParameterIn.PATH) @PathVariable("codes") String codes,
        @Parameter(hidden = true) final ServerWebExchange exchange
    ) throws Exception {
        Mono<Void> result = Mono.empty();
        exchange.getResponse().setStatusCode(HttpStatus.NOT_IMPLEMENTED);
        return result.then(Mono.empty());

    }


    /**
     * POST /status/{codes} : Return status code or random status code if more than one are given
     *
     * @param codes  (required)
     * @return Informational responses (status code 100)
     *         or Success (status code 200)
     *         or Redirection (status code 300)
     *         or Client Errors (status code 400)
     *         or Server Errors (status code 500)
     */
    @Operation(
        operationId = "statusCodesPost",
        summary = "Return status code or random status code if more than one are given",
        tags = { "Status codes" },
        responses = {
            @ApiResponse(responseCode = "100", description = "Informational responses"),
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "300", description = "Redirection"),
            @ApiResponse(responseCode = "400", description = "Client Errors"),
            @ApiResponse(responseCode = "500", description = "Server Errors")
        }
    )
    @RequestMapping(
        method = RequestMethod.POST,
        value = "/status/{codes}"
    )
    
    default Mono<ResponseEntity<Void>> statusCodesPost(
        @Parameter(name = "codes", description = "", required = true, in = ParameterIn.PATH) @PathVariable("codes") String codes,
        @Parameter(hidden = true) final ServerWebExchange exchange
    ) throws Exception {
        Mono<Void> result = Mono.empty();
        exchange.getResponse().setStatusCode(HttpStatus.NOT_IMPLEMENTED);
        return result.then(Mono.empty());

    }


    /**
     * PUT /status/{codes} : Return status code or random status code if more than one are given
     *
     * @param codes  (required)
     * @return Informational responses (status code 100)
     *         or Success (status code 200)
     *         or Redirection (status code 300)
     *         or Client Errors (status code 400)
     *         or Server Errors (status code 500)
     */
    @Operation(
        operationId = "statusCodesPut",
        summary = "Return status code or random status code if more than one are given",
        tags = { "Status codes" },
        responses = {
            @ApiResponse(responseCode = "100", description = "Informational responses"),
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "300", description = "Redirection"),
            @ApiResponse(responseCode = "400", description = "Client Errors"),
            @ApiResponse(responseCode = "500", description = "Server Errors")
        }
    )
    @RequestMapping(
        method = RequestMethod.PUT,
        value = "/status/{codes}"
    )
    
    default Mono<ResponseEntity<Void>> statusCodesPut(
        @Parameter(name = "codes", description = "", required = true, in = ParameterIn.PATH) @PathVariable("codes") String codes,
        @Parameter(hidden = true) final ServerWebExchange exchange
    ) throws Exception {
        Mono<Void> result = Mono.empty();
        exchange.getResponse().setStatusCode(HttpStatus.NOT_IMPLEMENTED);
        return result.then(Mono.empty());

    }

}
