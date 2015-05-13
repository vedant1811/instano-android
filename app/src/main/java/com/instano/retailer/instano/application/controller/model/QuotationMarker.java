package com.instano.retailer.instano.application.controller.model;

import android.support.annotation.Nullable;

import com.instano.retailer.instano.utilities.model.Outlet;
import com.instano.retailer.instano.utilities.model.Quotation;

/**
 * Created by vedant on 5/5/15.
 */
public class QuotationMarker {
    public final Outlet outlet;
    public final Integer price;

    /**
     *
     * @param outlet
     * @param quotation if null, price is set to null
     */
    public QuotationMarker(Outlet outlet, @Nullable Quotation quotation) {
        this.outlet = outlet;
        if (quotation == null)
            price = null;
        else
            price = quotation.price;
    }
}
