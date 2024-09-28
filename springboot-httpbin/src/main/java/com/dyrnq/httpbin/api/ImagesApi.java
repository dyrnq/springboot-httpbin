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
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.annotation.Generated;

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.9.0-SNAPSHOT")
@Validated
@Tag(name = "Images", description = "Returns different image formats")
public interface ImagesApi {

    default Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    /**
     * GET /image : Returns a simple image of the type suggest by the Accept header.
     *
     * @return An image. (status code 200)
     */
    @Operation(
        operationId = "imageGet",
        summary = "Returns a simple image of the type suggest by the Accept header.",
        tags = { "Images" },
        responses = {
            @ApiResponse(responseCode = "200", description = "An image.")
        }
    )
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/image",
        produces = { "image/*" }
    )
    
    default ResponseEntity<Void> imageGet(
        
    ) throws Exception {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }


    /**
     * GET /image/jpeg : Returns a simple JPEG image.
     *
     * @return A JPEG image. (status code 200)
     */
    @Operation(
        operationId = "imageJpegGet",
        summary = "Returns a simple JPEG image.",
        tags = { "Images" },
        responses = {
            @ApiResponse(responseCode = "200", description = "A JPEG image.")
        }
    )
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/image/jpeg"
    )
    
    default ResponseEntity<Void> imageJpegGet(
        
    ) throws Exception {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }


    /**
     * GET /image/png : Returns a simple PNG image.
     *
     * @return A PNG image. (status code 200)
     */
    @Operation(
        operationId = "imagePngGet",
        summary = "Returns a simple PNG image.",
        tags = { "Images" },
        responses = {
            @ApiResponse(responseCode = "200", description = "A PNG image.")
        }
    )
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/image/png"
    )
    
    default ResponseEntity<Void> imagePngGet(
        
    ) throws Exception {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }


    /**
     * GET /image/svg : Returns a simple SVG image.
     *
     * @return An SVG image. (status code 200)
     */
    @Operation(
        operationId = "imageSvgGet",
        summary = "Returns a simple SVG image.",
        tags = { "Images" },
        responses = {
            @ApiResponse(responseCode = "200", description = "An SVG image.")
        }
    )
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/image/svg"
    )
    
    default ResponseEntity<Void> imageSvgGet(
        
    ) throws Exception {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }


    /**
     * GET /image/webp : Returns a simple WEBP image.
     *
     * @return A WEBP image. (status code 200)
     */
    @Operation(
        operationId = "imageWebpGet",
        summary = "Returns a simple WEBP image.",
        tags = { "Images" },
        responses = {
            @ApiResponse(responseCode = "200", description = "A WEBP image.")
        }
    )
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/image/webp"
    )
    
    default ResponseEntity<Void> imageWebpGet(
        
    ) throws Exception {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

}
