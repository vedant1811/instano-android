package com.instano.retailer.instano.search;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.instano.retailer.instano.R;

/**
 * Created by vedant on 26/12/14.
 */
public class NoLocationErrorDialogFragment extends DialogFragment implements View.OnClickListener {

    private static final String ENTER_ADDRESS = "Entering an address without GPS";
    private static final String DONE = "Done";

    private Button mSelectLocationButton;
    private Button mEnterAddressButton;
    private EditText mAddressEditText;
    private Callbacks mCallbacks = sDummyCallbacks;

    private CharSequence mAddress;
    private boolean mAddressEditTextVisible;

    public interface Callbacks {
        public void selectLocationClicked();
        public void addressEntered(@NonNull String address);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void selectLocationClicked() {
        }
        @Override
        public void addressEntered(@NonNull String address) {
        }
    };

    public static NoLocationErrorDialogFragment newInstance() {
        NoLocationErrorDialogFragment fragment = new NoLocationErrorDialogFragment();
        return fragment;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.gpsSettingsButton:
                Intent gpsOptionsIntent = new Intent(
                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                try {
                    startActivity(gpsOptionsIntent);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getActivity(), "Cannot start settings. Try doing it manually",
                            Toast.LENGTH_LONG).show();
                }
                dismiss();
                break;

            case R.id.selectLocationButton:
                mCallbacks.selectLocationClicked();
                dismiss();
                break;

            case R.id.enterAddressButton:
                if (mAddressEditTextVisible) { // "Done" has been clicked
                    Editable text = mAddressEditText.getText();
                    if(TextUtils.isEmpty(text))
                        mAddressEditText.setError("Cannot be empty");
                    else {
                        mCallbacks.addressEntered(text.toString());
                        dismiss();
                    }
                }
                else {
                    setEnterAddressState(true);
                }
                break;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_no_location_error_dialog, container, false);

        Button gpsSettingsButton = (Button) rootView.findViewById(R.id.gpsSettingsButton);
        mSelectLocationButton = (Button) rootView.findViewById(R.id.selectLocationButton);
        mEnterAddressButton = (Button) rootView.findViewById(R.id.enterAddressButton);
        mAddressEditText = (EditText) rootView.findViewById(R.id.addressEditText);

        mAddressEditTextVisible = false;
        mAddress = null;
        getDialog().setTitle("GPS is unavailable");
        gpsSettingsButton.setOnClickListener(this);
        mSelectLocationButton.setOnClickListener(this);
        mEnterAddressButton.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onResume(){
        super.onResume();
        setEnterAddressState(mAddressEditTextVisible);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAddressEditText.getVisibility() == View.VISIBLE) {
            mAddressEditTextVisible = true;
            mAddress = mAddressEditText.getText();
        }
        else {
            mAddressEditTextVisible = false;
            mAddress = null;
        }
    }

    private void setEnterAddressState(boolean shown) {
        mAddressEditTextVisible = shown;
        if (shown) {
            mAddressEditText.setVisibility(View.VISIBLE);
            mAddressEditText.setText(mAddress);
            mEnterAddressButton.setText(DONE);
        }
        else {
            mAddressEditText.setVisibility(View.GONE);
            mEnterAddressButton.setText(ENTER_ADDRESS);
        }
    }
}
