package com.hms.config;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;

@Component
public class CustomErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus responseStatus = HttpStatus.valueOf(response.status());
        String responseBody = "";

        try {
            if (response.body() != null) {
                responseBody = Util.toString(response.body().asReader());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (responseStatus.is5xxServerError()) {
            return new CustomFeignException(response.status(), responseBody);
        } else if (responseStatus.is4xxClientError()) {
            return new CustomFeignException(response.status(), responseBody);
        } else {
            return new Exception("Generic exception");
        }
    }

}
