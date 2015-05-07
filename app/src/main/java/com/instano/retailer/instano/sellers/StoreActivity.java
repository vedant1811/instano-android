package com.instano.retailer.instano.sellers;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.widget.TextView;
import android.widget.Toast;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.activities.GlobalMenuActivity;

/**
 * Created by Dheeraj on 06-May-15.
 */
public class StoreActivity extends GlobalMenuActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        TextView text = (TextView) findViewById(R.id.shop_details);
        String s = (String) text.getText();
        SpannableString s1 = new SpannableString(s);
        s1.setSpan(new RelativeSizeSpan(1.3f),0,18,0);
        s1.setSpan(new ForegroundColorSpan(Color.BLACK),0,0,0);
        text.setText(s1);


    }
}
