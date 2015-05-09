package com.instano.retailer.instano.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.instano.retailer.instano.R;

/**
 * Created by Dheeraj on 09-May-15.
 */
public class NavigationTest extends GlobalMenuActivity {
    public void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_home_navigation_drawer);
    }
}
