package com.instano.retailer.instano.activities;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.application.ServicesSingleton;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.models.Buyer;

import rx.Observable;
import rx.android.events.OnClickEvent;
import rx.android.observables.ViewObservable;
import rx.subjects.PublishSubject;

/**
 * Created by vedant on 26/12/14.
 */
public class ErrorDialogFragment extends DialogFragment implements View.OnClickListener {
    private static final int ERROR_DIALOG_DELAY_MILLIS = 3500;

    private static final String KEY_ARG_HEADING = "ArgumentHeading";
    private static final String KEY_ARG_SUBHEADING = "ArgumentTitle";
    public static final String KEY_ARG_TYPE = "ArgumentType";
    private CharSequence mHeading;
    private CharSequence mSubheading;
    public int mViewFlipperState;
    public ViewFlipper mViewFlipper;
    private TextView mTitleTextView;
    private Button mTryAgainButton;
    private final PublishSubject<OnClickEvent> mEventSubject;

    public ErrorDialogFragment() {
        mEventSubject = PublishSubject.create();
        mEventSubject.doOnSubscribe(() -> Log.d("ViewObservable", "subscribed"));
    }

    public static ErrorDialogFragment newInstance(String heading, String subheading) {
        Bundle arguments = new Bundle();
        arguments.putString(KEY_ARG_HEADING, heading);
        arguments.putString(KEY_ARG_SUBHEADING, subheading);
        ErrorDialogFragment fragment = new ErrorDialogFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    public boolean isHeadingSameAs(CharSequence sequence) {
        return TextUtils.equals(sequence, mHeading);
    }

    public Observable<OnClickEvent> observeTryAgainClicks() {
        return mEventSubject;
    }

    public void showProgressBar(boolean show) {
        int child = show? 1 : 0;
        if (isResumed() && mViewFlipper != null) // unlikely but dont take chances
            mViewFlipper.setDisplayedChild(child);
        else
            mViewFlipperState = child;
    }

    @Override
    public void onClick(View v) {
        String whatsAppId = ServicesSingleton.instance().getInstanoWhatsappId();
        String mobileNumber = "+" + whatsAppId;
        switch (v.getId()) {

            case R.id.buttonSendMail:
                Intent mailIntent;
                mailIntent = new Intent(Intent.ACTION_SEND);
                mailIntent.setType("message/rfc822");
                mailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"contact@instano.in"});
                mailIntent.putExtra(Intent.EXTRA_SUBJECT, "Contacting instano");
                Buyer buyer = ServicesSingleton.instance().getBuyer();
                String append = "";
                if (buyer != null)
                    mailIntent.putExtra(Intent.EXTRA_TEXT, "\n\nRegards,\n" + buyer.getName());
                try {
                    startActivity(Intent.createChooser(mailIntent, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getActivity(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                return; // we do not know what to do with this click
        }
        dismiss();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHeading = getArguments().getString(KEY_ARG_HEADING);
        mSubheading = getArguments().getString(KEY_ARG_SUBHEADING);
        setCancelable(false);
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_message_dialog, container, false);

        mTitleTextView = (TextView) rootView.findViewById(R.id.titleTextView);
        Button sendEmailButton = (Button) rootView.findViewById(R.id.buttonSendMail);
        mTryAgainButton = (Button) rootView.findViewById(R.id.tryAgainButton);
        mViewFlipper = (ViewFlipper) rootView.findViewById(R.id.messageDialogFragmentViewFlipper);
        ProgressBar progressBar = (ProgressBar) rootView.findViewById(R.id.messageDialogProgressBar);
        sendEmailButton.setOnClickListener(this);
        ViewObservable.clicks(mTryAgainButton)
                .subscribe((t1) -> {
                    Log.d("ViewObservable", "mTryAgainButton clicked");
                    mEventSubject.onNext(t1);
                });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewFlipper.setDisplayedChild(mViewFlipperState);
        getDialog().setTitle(mHeading);
        mTitleTextView.setText(mSubheading);
    }

    @Override
    public void onPause() {
        super.onPause();
        mViewFlipperState = mViewFlipper.getDisplayedChild();
        // headings do not change after creation so we do not need to save state for them
    }
}
