package com.instano.retailer.instano.utilities.models;

import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * Created by ROHIT on 08-Mar-15.
 */

@JsonRootName(value = "device")
public class Device {

    private String gcm_registration_id;
    private String sessionId = "sklksal";

    public String getGcm_registration_id() {
        return gcm_registration_id;
    }

    public void setGcm_registration_id(String gcm_registration_id) {
        this.gcm_registration_id = gcm_registration_id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
