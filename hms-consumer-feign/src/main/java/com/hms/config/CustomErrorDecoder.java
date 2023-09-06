package com.hms.config;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.hms.dto.JwtResponse;

import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;

@Component
public class CustomErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus responseStatus = HttpStatus.valueOf(response.status());
        String responseBody = "";

        if (responseStatus == HttpStatus.UNAUTHORIZED) {
            return new CustomFeignException(response.status(), "Bad credentials");
        }

        if (responseStatus.is5xxServerError()) {
            try {
                if (response.body() != null) {
                    responseBody = Util.toString(response.body().asReader());
                    System.out.println("res: " + responseBody);
                    System.out.println("res class: " + responseBody.getClass());
                }
            } catch (IOException e) {
                return new CustomFeignException(response.status(), e.getMessage());
            }
            return new CustomFeignException(response.status(), responseBody);
        } else if (responseStatus.is4xxClientError()) {
            try {
                if (response.body() != null) {
                    responseBody = Util.toString(response.body().asReader());
                    System.out.println("res: " + responseBody);
                    System.out.println("res class: " + responseBody.getClass());
                }
            } catch (IOException e) {
                return new CustomFeignException(response.status(), e.getMessage());
            }
            return new CustomFeignException(response.status(), responseBody);
        } else {
            return new Exception("Generic exception");
        }
    }

}
