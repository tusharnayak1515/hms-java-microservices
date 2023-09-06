package com.hms.config;

import feign.RetryableException;
import feign.Retryer;

public class CustomRetryer implements Retryer {

    @Override
    public void continueOrPropagate(RetryableException e) {
        System.out.println("exception type: "+(e.getCause() instanceof java.net.ConnectException));
        if (e.getCause() instanceof java.net.ConnectException) {
            throw e;
        }
    }

    @Override
    public Retryer clone() {
        return new CustomRetryer();
    }
}
