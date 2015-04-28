package com.instano.retailer.instano.utilities.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Rohit on 28/4/15.
 */
public class Product {

    @JsonProperty("id")
    public int id;
    @JsonProperty("name")
    public String name;
    @JsonProperty("brand_name")
    public Brand brand;
    @JsonProperty("image")
    public String image;
    @JsonProperty("features")
    public String features;


    public Product () {}
}

//"product": {
//        "id": 6739,
//        "name": "LG T80CME21P 7 kg Fully Automatic Top Loading Washing Machine",
//        "brand_name": {
//        "name": "LG",
//        "category": "Washing Machines"
//        },
//        "image": "http://s3.amazonaws.com/instano/staging/v1_products/images/medium/lg_t80cme21p_7_kg_fully_automatic_top_loading_washing_machine.jpeg?1429250783",
//        "features": "Capacity: 7 kg,Fully Automatic"
//        }
