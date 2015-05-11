package com.instano.retailer.instano.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.widget.ImageView;
import android.widget.TextView;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.application.BaseActivity;
import com.instano.retailer.instano.application.network.NetworkRequestsManager;
import com.instano.retailer.instano.utilities.model.Seller;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
/**
 * Created by Dheeraj on 06-May-15.
 */
public class SellerDetailActivity extends BaseActivity {

    @InjectView(R.id.shop_details) TextView sellerDetail;
    @InjectView(R.id.shop_image) ImageView shopImage;
    @InjectView(R.id.dealHeadingStoreFooter) TextView dealHeading;
    @InjectView(R.id.dealSubheadingStoreFooter) TextView dealSubheading;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);
        Bundle bundle = getIntent().getExtras();

        ButterKnife.inject(this);

        Observable<Seller> seller = NetworkRequestsManager.instance().getSeller(bundle.getInt("seller_id"));
        dealHeading.setText(bundle.getString("heading"));
        dealSubheading.setText(bundle.getString("subheading"));

        retryableError(seller, seller1 -> {

            String s = seller1.name_of_shop;
            String s2 = seller1.outlets.get(0).getPrettyDistanceFromLocation();
            String s3 = seller1.outlets.get(0).address;

            SpannableString s1 = null;

            if (s2 == null)
                s1 = new SpannableString(s + "," + s3);
            else
                s1 = new SpannableString(s + "," + s2 + "," + s3);

            s1.setSpan(new RelativeSizeSpan(1.3f), 0, s.length(), 0);  // 18 is the no. of character that is to be resized
            s1.setSpan(new ForegroundColorSpan(Color.BLACK), 0, 0, 0);
            sellerDetail.setText(s1);

            Picasso.with(this)
                    .load(seller1.image).fit().centerInside()
                    .into(shopImage);
        });
    }
   }
