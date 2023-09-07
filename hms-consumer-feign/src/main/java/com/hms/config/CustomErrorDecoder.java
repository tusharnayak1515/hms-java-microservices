package com.hms.config;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import feign.Response;
import feign.codec.ErrorDecoder;

@Component
public class CustomErrorDecoder implements ErrorDecoder {

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
}
