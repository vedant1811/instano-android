package com.instano.retailer.instano.application.controller.model;

import com.instano.retailer.instano.utilities.model.Quotation;
import com.instano.retailer.instano.utilities.model.Seller;

/**
 * Created by vedant on 5/2/15.
 */
public class QuotationCard {
    public final Seller seller;
    public final Quotation quotation;

    public QuotationCard(Seller seller, Quotation quotation) {
        this.seller = seller;
        this.quotation = quotation;
    }
}
