package com.instano.retailer.instano.application.controller.model;

import com.instano.retailer.instano.utilities.model.Deal;
import com.instano.retailer.instano.utilities.model.Seller;

/**
 * Created by vedant on 5/2/15.
 */
public class DealCard {
    public final Deal deal;
    public final Seller seller;

    public DealCard(Deal deal, Seller seller) {
        this.deal = deal;
        this.seller = seller;
    }
}
