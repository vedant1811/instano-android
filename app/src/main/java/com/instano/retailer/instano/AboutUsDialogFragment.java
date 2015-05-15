package com.instano.retailer.instano;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Dheeraj on 14-May-15.
 */
public class AboutUsDialogFragment extends DialogFragment {

    public AboutUsDialogFragment(){

    }

    public void OnCreate(Bundle savedInstanceStates){

    }

    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceStates){

        View rootview = inflater.inflate(R.layout.fragment_about_us_dialog,container,false);
        return rootview;
    }


    public static AboutUsDialogFragment newInstnace() {
        AboutUsDialogFragment fragment = new AboutUsDialogFragment();
        return fragment;
    }
}
