package com.instano.retailer.instano.utilities.models;

import com.instano.retailer.instano.application.ServicesSingleton;

/**
 * Created by vedant on 7/1/15.
 */
public class Deal {
    public int id;
    public String heading;
    public String subheading;
    public long updatedAt;
    public long expiresAt;
    public int sellerId;

    public String expiresAt() {
        return "expires " + ServicesSingleton.instance().getPrettyTimeElapsed(expiresAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Deal deal = (Deal) o;

        if (id != deal.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
