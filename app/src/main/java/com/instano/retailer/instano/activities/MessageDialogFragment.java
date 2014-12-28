package com.instano.retailer.instano.activities;

import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.application.ServicesSingleton;
import com.instano.retailer.instano.utilities.GlobalMenuActivity;

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
        String whatsAppId = ServicesSingleton.instance().getInstanoWhatsappId();
        String mobileNumber = "+" + whatsAppId;
        switch (v.getId()) {
            case R.id.buttonSendSms:
                Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                smsIntent.setType("vnd.android-dir/mms-sms");
                smsIntent.putExtra("address", mobileNumber);
                startActivityForResult(smsIntent, GlobalMenuActivity.SEND_SMS_REQUEST_CODE);
                break;

            case R.id.buttonSendWhatsapp:
                Uri mUri = Uri.parse("smsto:+" + whatsAppId);
                Intent whatsApp = new Intent(Intent.ACTION_SENDTO, mUri);
                whatsApp.putExtra("chat", true);
                whatsApp.setPackage("com.whatsapp");
                startActivity(whatsApp);
                break;

            case R.id.buttonAddContact:
                String displayName = "Instano shopping app";
                String email = "contact@instano.in";

                Intent intent = new Intent(Intent.ACTION_INSERT);
                intent.setType(ContactsContract.Contacts.CONTENT_TYPE);

                // Just two examples of information you can send to pre-fill out data for the
                // user.  See android.provider.ContactsContract.Intents.Insert for the complete
                // list.
                intent.putExtra(ContactsContract.Intents.Insert.NAME, displayName);
                intent.putExtra(ContactsContract.Intents.Insert.PHONE, mobileNumber);
                intent.putExtra(ContactsContract.Intents.Insert.EMAIL, email);

                // Send with it a unique request code, so when you get called back, you can
                // check to make sure it is from the intent you launched (ideally should be
                // some public static final so receiver can check against it)
                // callback is handled by {@link GlobalMenuActivity#onActivityResult}
                getActivity().startActivityForResult(intent, GlobalMenuActivity.PICK_CONTACT_REQUEST_CODE);
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
        View rootView = inflater.inflate(R.layout.fragment_message_dialog, container, false);

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
