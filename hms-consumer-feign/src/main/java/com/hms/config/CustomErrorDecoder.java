package com.hms.config;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hms.dto.JwtResponse;

import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;

@Component
public class CustomErrorDecoder implements ErrorDecoder {

    // @Override
    // public Exception decode(String methodKey, Response response) {
    //     HttpStatus responseStatus = HttpStatus.valueOf(response.status());
    //     String responseBody = "";

    //     if (responseStatus == HttpStatus.UNAUTHORIZED) {
    //         return new CustomFeignException(response.status(), "Bad credentials");
    //     }

    //     if (responseStatus.is5xxServerError()) {
    //         try {
    //             if (response.body() != null) {
    //                 InputStream responseBodyStream = response.body().asInputStream();
    //                 responseBody = new String(responseBodyStream.readAllBytes());
    //                 System.out.println("res: " + responseBody);
    //                 System.out.println("res class: " + responseBody.getClass());
    //             }
    //         } catch (IOException e) {
    //             return new CustomFeignException(response.status(), e.getMessage());
    //         }
    //         return new CustomFeignException(response.status(), responseBody);
    //     } else if (responseStatus.is4xxClientError()) {
    //         try {
    //             if (response.body() != null) {
    //                 InputStream responseBodyStream = response.body().asInputStream();
    //                 responseBody = new String(responseBodyStream.readAllBytes());
    //                 System.out.println("res: " + responseBody);
    //                 System.out.println("res class: " + responseBody.getClass());
    //             }
    //         } catch (IOException e) {
    //             return new CustomFeignException(response.status(), e.getMessage());
    //         }
    //         return new CustomFeignException(response.status(), responseBody);
    //     } else {
    //         return new Exception("Generic exception");
    //     }
    // }

    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus httpStatus = HttpStatus.valueOf(response.status());

        System.out.println("statusCode: "+httpStatus);
        System.out.println("status: "+(httpStatus.is4xxClientError() || httpStatus.is5xxServerError()));

        if (httpStatus.is4xxClientError() || httpStatus.is5xxServerError()) {
            if(response.body() == null) {
                System.out.println("----***yes**----");
                throw new CustomFeignException(httpStatus.value(), "Bad Request");
            }
            try {
                InputStream responseBodyStream = response.body().asInputStream();
                String responseBody = new String(responseBodyStream.readAllBytes());
                throw new CustomFeignException(httpStatus.value(), responseBody);
            } catch (IOException e) {
                throw new CustomFeignException(response.status(), e.getMessage());
            }
        }
        throw new CustomFeignException(response.status(), methodKey);
    }

    // private final ObjectMapper objectMapper = new ObjectMapper();

    // @Override
    // public Exception decode(String methodKey, Response response) {
    //     try {
    //         if (response.body() != null) {
    //             JwtResponse errorResponse = objectMapper.readValue(response.body().asInputStream(), JwtResponse.class);
    //             return new CustomFeignException(response.status(), errorResponse.getError());
    //         }
    //     } catch (IOException e) {
    //         throw new CustomFeignException(response.status(), methodKey);
    //     }
    //     return new Exception("Generic exception");
    // }

    // private final ErrorDecoder defaultErrorDecoder = new Default();

    // @Override
    // public Exception decode(String s, Response response) {
    //     Exception exception = defaultErrorDecoder.decode(s, response);

    //     if(exception instanceof RetryableException){
    //         return exception;
    //     }

    //     if(response.status() == 504){
    //         return new RetryableException(response.status(), "504 error", response.request().httpMethod(), null, null );
    //     }

    //     return exception;
    // }
}
