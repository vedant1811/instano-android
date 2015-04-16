package com.instano.retailer.instano.application.network;

import com.instano.retailer.instano.utilities.library.Log;

import org.apache.http.HttpStatus;

import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedInput;

/**
 * Created by vedant on 4/4/15.
 */
public class ResponseError extends Throwable {
    private final String TAG = getClass().getSimpleName();

    private final Type mType;

    public Type getErrorType() {
        return mType;
    }

    public static enum Type {
        UNKNOWN_ERROR,
        // authorization errors (422):
        NO_GCM_ID,
        INVALID_GCM_ID,
        GCM_NOT_REGISTERED,
        OTHER_GCM_ERROR,

        // authorization errors (403/401):
        NO_SESSION_ID,
        INCORRECT_SESSION_ID,
        NO_BUYER_ASSOCIATED,
        SOME_OTHER_401,
        SIGN_IN_FAILED,

        // Not acceptable (406):
        INCORRECT_API_KEY,
        SOME_OTHER_406,

        // network errors:
        SERVER_HANG_UP, // 502 a.k.a. bad gateway
        NETWORK_RESPONSE_NULL,
        VOLLEY_TIMEOUT,

        // fatal
        BAD_REQUEST, // 400
        SERVER_ERROR,
        // 422 specific errors:
        PHONE_EXISTS,
        SOME_OTHER_422;

        public boolean is(Throwable throwable) {
            if (throwable instanceof ResponseError) {
                ResponseError responseError = (ResponseError) throwable;
                return (this == responseError.getErrorType());
            }
            else
                return false;
        }

        public boolean shouldRefreshGcmId() {
            if (this.toString().contains("GCM"))
                return true;
            else
                return false;
        }

        public boolean shouldRefreshSessionId() {
            if (this == NO_SESSION_ID || this == INCORRECT_SESSION_ID)
                return true;
            else
                return false;
        }

        public boolean isLongWaiting() {
            if (this == SERVER_HANG_UP || this == VOLLEY_TIMEOUT)
                return true;
            else
                return false;
        }

        public boolean isFatal() {
            if (this == BAD_REQUEST || this == SERVER_ERROR || this == SERVER_HANG_UP)
                return true;
            else
                return false;
        }
    }

    public ResponseError(RetrofitError cause) {
        super(cause);

        cause.printStackTrace();

        if (cause.getKind() == RetrofitError.Kind.UNEXPECTED)
            throw new RuntimeException("Unexpected retrofit error", cause);

        Type error = Type.UNKNOWN_ERROR;
        Response networkResponse = cause.getResponse();
        String responseString="";
        if (networkResponse != null) {
            Log.v(TAG, ".getResponseError code :" + networkResponse.getStatus());
            Log.v(TAG, ".getResponseError reason :" + networkResponse.getReason());
            try {
                TypedInput body = networkResponse.getBody();
                Log.v(TAG, ".getResponseError body :" + body);
                TypedByteArray body1 = (TypedByteArray) body;
                Log.v(TAG, ".getResponseError TypedByteArray :" + body);
                responseString = new String(body1.getBytes());
            } catch (Exception e) {
                Log.fatalError(e);
            }
            Log.v(TAG, ".getResponseError body :" + responseString);
            switch (networkResponse.getStatus()) {
                case HttpStatus.SC_UNAUTHORIZED: // volley fails for 401
                case HttpStatus.SC_FORBIDDEN:
                    if (responseString.contains("incorrect session_id"))
                        error = Type.INCORRECT_SESSION_ID;
                    else if (responseString.contains("no buyer associated"))
                        error = Type.NO_BUYER_ASSOCIATED;
                    else
                        error = Type.SOME_OTHER_401;
                    break;

                case HttpStatus.SC_NOT_ACCEPTABLE:
                    if (responseString.contains("incorrect api_key"))
                        error = Type.INCORRECT_API_KEY;
                    else
                        error = Type.SOME_OTHER_406;
                    break;

                case HttpStatus.SC_UNPROCESSABLE_ENTITY:
                    if (responseString.contains("GCM errors")) {
                        if (responseString.contains("NotRegistered"))
                            error = Type.GCM_NOT_REGISTERED;
                        else if (responseString.contains("InvalidRegistration"))
                            error = Type.INVALID_GCM_ID;
                        else
                            error = Type.OTHER_GCM_ERROR;
                    }
                    else
                        error = Type.SOME_OTHER_422;
                    break;

                case HttpStatus.SC_BAD_REQUEST:
                    error = Type.BAD_REQUEST;
                    break;

                case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                    error = Type.SERVER_ERROR;
                    break;

                case HttpStatus.SC_BAD_GATEWAY:
                    error = Type.SERVER_HANG_UP;
                    break;
            }
        }
        else
            error = Type.NETWORK_RESPONSE_NULL;

        if (error.isFatal())
            Log.e(TAG, ".getResponseError returned " + error);
        else
            Log.v(TAG, ".getResponseError returned " + error);
        mType = error;
    }
}
