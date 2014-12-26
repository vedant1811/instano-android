package com.instano.retailer.instano.activities;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.instano.retailer.instano.R;

/**
 * Created by vedant on 26/12/14.
 */
public class MessageDialogFragment extends DialogFragment implements View.OnClickListener {

    private static final String KEY_ARG_HEADING = "ArgumentHeading";
    private static final String KEY_ARG_TITLE = "ArgumentTitle";
    private CharSequence mHeading;
    private CharSequence mTitle;

    public static MessageDialogFragment newInstance(String heading, String title) {
        Bundle arguments = new Bundle();
        arguments.putString(KEY_ARG_HEADING, heading);
        arguments.putString(KEY_ARG_TITLE, title);
        MessageDialogFragment fragment = new MessageDialogFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonSendSms:
                break;

            case R.id.buttonSendWhatsapp:
                break;

            case R.id.buttonAddContact:
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHeading = getArguments().getString(KEY_ARG_HEADING);
        mTitle = getArguments().getString(KEY_ARG_TITLE);
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_send_message, container, false);

        Button sendSmsButton = (Button) rootView.findViewById(R.id.buttonSendSms);
        Button sendWhatsAppButton = (Button) rootView.findViewById(R.id.buttonSendWhatsapp);
        Button addContactButton = (Button) rootView.findViewById(R.id.buttonAddContact);
        TextView titleTextView = (TextView) rootView.findViewById(R.id.titleTextView);

        getDialog().setTitle(mHeading);
        titleTextView.setText(mTitle);
        sendSmsButton.setOnClickListener(this);
        sendWhatsAppButton.setOnClickListener(this);
        addContactButton.setOnClickListener(this);
        return rootView;
    }
}
