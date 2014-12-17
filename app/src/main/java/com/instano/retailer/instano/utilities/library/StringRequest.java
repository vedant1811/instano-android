package com.instano.retailer.instano.utilities.library;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by vedant on 1/10/14.
 */
public class StringRequest extends JsonRequest<String> {

    /**
     * Creates a new request.
     * @param url URL to fetch the JSON from
     * @param listener Listener to receive the JSON response
     * @param errorListener Error listener, or null to ignore errors.
     */
    public StringRequest(int method, String url, JSONObject request, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, request.toString(), listener, errorListener);
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        try {
            String string =
                    new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success((string),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }
}
