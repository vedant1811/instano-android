package com.instano.retailer.instano.sellers;

import android.app.ActionBar;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.widget.TextView;
import android.widget.Toast;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.activities.GlobalMenuActivity;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by Dheeraj on 06-May-15.
 */
public class StoreActivity extends GlobalMenuActivity {

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
