package com.instano.retailer.instano.utilities.library;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.instano.retailer.instano.application.NetworkRequestsManager;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ROHIT on 18-Mar-15.
 */


public class AuthenticatedJsonRequest extends JsonObjectRequest {

    public interface Listener {
        public void onResponse(NetworkRequestsManager.ResponseError error, JSONObject response);
    }

    private String mSessionId;

    public AuthenticatedJsonRequest(String url, JSONObject request, final Listener listener, String sessionId) {
        super(url, request, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                listener.onResponse(NetworkRequestsManager.ResponseError.NO_ERROR, response);
            }
        },
        new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onResponse(NetworkRequestsManager.ResponseError.UNKNOWN_ERROR, null);
            }
        }
        );
        mSessionId = sessionId;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        if(mSessionId.isEmpty())
            throw new AuthFailureError("session is empty");
        Map<String,String> headers = new HashMap<String,String>();
        headers.put("Session-Id", mSessionId);
        return headers;
    }
}
