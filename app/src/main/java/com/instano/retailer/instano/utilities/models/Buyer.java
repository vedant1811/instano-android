package com.instano.retailer.instano.utilities.models;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Created by vedant on 18/12/14.
 */
public class Buyer {
    public final int id;
    public final String name;
    public final String phone;

    ObjectMapper mapper = new ObjectMapper(); // create once, reuse
    private static JsonFactory jsonFactory = new JsonFactory();

    /*public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public int getId() {
        //     id=-1;
        return id;
    }
*/

    public Buyer(JSONObject jsonObject) throws JSONException {
        id = jsonObject.getInt("id");
        name = jsonObject.getString("name");
        phone = jsonObject.getString("phone");
    }

    public  String toJsonObject() throws JSONException, IOException {
        /*JSONObject data = new JSONObject()
                .put("name", name)
                .put("phone", phone);
        if (id != -1)
            data.put("id", id);
        JSONObject buyer = new JSONObject()
                .put("buyer", data);
        */
        StringWriter sw = new StringWriter();
        JsonGenerator jg = jsonFactory.createGenerator(sw);
        try{

            if(this.id != -1)
            {
                jg.writeRawValue("id");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sw.toString();


    }

    public static String toJson()throws JSONException, IOException {

        StringWriter sw = new StringWriter();
        JsonGenerator jg = jsonFactory.createGenerator(sw);

        return sw.toString();

    }

}
