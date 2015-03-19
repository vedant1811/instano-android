package com.instano.retailer.instano.utilities.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * Created by ROHIT on 08-Mar-15.
 */

@JsonRootName(value = "device")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Device {

    private String gcm_registration_id;
    private String session_id;

    public String getGcm_registration_id() {
        return gcm_registration_id;
    }

    public void setGcm_registration_id(String gcm_registration_id) {
        this.gcm_registration_id = gcm_registration_id;
    }

    public String getSession_id() {
        return session_id;
    }

    public void setSession_id(String session_id) {
        this.session_id = session_id;
    }
}
