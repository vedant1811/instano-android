package com.instano.retailer.instano.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.widget.TextView;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.application.BaseActivity;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Dheeraj on 06-May-15.
 */
public class SellerDetailActivity extends BaseActivity {

     @InjectView(R.id.shop_details) TextView text;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);
        ButterKnife.inject(this);
        String s = (String) text.getText();
        SpannableString s1 = new SpannableString(s);
        s1.setSpan(new RelativeSizeSpan(1.3f),0,18,0);  // 18 is the no. of character that is to be resized
        s1.setSpan(new ForegroundColorSpan(Color.BLACK),0,0,0);
        text.setText(s1);
   }
}
