package com.instano.retailer.instano.application.network;

import retrofit.ErrorHandler;
import retrofit.RetrofitError;

/**
 * Created by vedant on 4/4/15.
 */
public class ResponseErrorHandler implements ErrorHandler {
    @Override
    public Throwable handleError(RetrofitError cause) {
        return new ResponseError(cause);
    }
}
