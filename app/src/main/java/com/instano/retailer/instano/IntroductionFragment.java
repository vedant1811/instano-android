package com.instano.retailer.instano;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class IntroductionFragment extends Fragment {

    TextView mTextView;
    Button mButton;

    public IntroductionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_introduction, container, false);

        mTextView = (TextView) view.findViewById(R.id.textView);
        mButton = (Button) view.findViewById(R.id.button);

        return view;
    }

    public void onProfileSetUp() {
        mTextView.setText("Your profile has been set up. You can Search for products by clicking the icon in the action bar");
    }
}
