package com.instano.retailer.instano.application.controller.model;

import com.instano.retailer.instano.utilities.model.Outlet;

/**
 * Created by vedant on 5/5/15.
 */
public class QuotationMarker {
    public final Outlet outlet;
    public final int price;

    public QuotationMarker(Outlet outlet, int price) {
        this.outlet = outlet;
        this.price = price;
    }
}
