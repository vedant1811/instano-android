package com.instano.retailer.instano.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.widget.TextView;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.application.network.NetworkRequestsManager;
import com.instano.retailer.instano.utilities.model.Seller;

import rx.Observable;

/**
 * Created by Dheeraj on 06-May-15.
 */
public class SellerDetailActivity extends GlobalMenuActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);
        Bundle bundle = getIntent().getExtras();

        TextView text = (TextView) findViewById(R.id.shop_details);
        String s = (String) text.getText();
        SpannableString s1 = new SpannableString(s);
        s1.setSpan(new RelativeSizeSpan(1.3f), 0, 18, 0);
        s1.setSpan(new ForegroundColorSpan(Color.BLACK), 0, 0, 0);
        text.setText(s1);

        Observable<Seller> seller = NetworkRequestsManager.instance().getSeller(bundle.getInt("seller_id"));

        retryableError(seller, seller1 -> {

        });
    }
}
