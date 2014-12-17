package com.instano.retailer.instano.utilities.library;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Pirated by vedant from http://stackoverflow.com/questions/5015686/android-spinner-with-multiple-choice on 28/9/14.
 */
public class MultiSpinner extends Spinner implements
        DialogInterface.OnMultiChoiceClickListener, DialogInterface.OnCancelListener {

    private List<String> mItems;

    private boolean[] mSelected;
    private String mDefaultText;
    private MultiSpinnerListener mListener;

    public MultiSpinner(Context context) {
        super(context);
    }

    public MultiSpinner(Context arg0, AttributeSet arg1) {
        super(arg0, arg1);
    }

    public MultiSpinner(Context arg0, AttributeSet arg1, int arg2) {
        super(arg0, arg1, arg2);
    }

    @Override
    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        mSelected[which] = isChecked;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        refreshView();
        mListener.onItemsSelected(mSelected);
    }

    private void refreshView() {
        // refresh text on spinner
        StringBuilder spinnerBuffer = new StringBuilder();
        boolean noneSelected = true;
        for (int i = 0; i < mItems.size(); i++) {
            if (mSelected[i]) {
                spinnerBuffer.append(mItems.get(i));
                spinnerBuffer.append(", ");
                noneSelected = false;
            }
        }
        String spinnerText;
        if (!noneSelected) {
            spinnerText = spinnerBuffer.toString();
            if (spinnerText.length() > 2)
                spinnerText = spinnerText.substring(0, spinnerText.length() - 2);
        } else {
            spinnerText = mDefaultText;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item,
                new String[] { spinnerText });
        setAdapter(adapter);
    }

    @Override
    public boolean performClick() {
        if (mItems == null)
            return false;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMultiChoiceItems(
                mItems.toArray(new CharSequence[mItems.size()]), mSelected, this);
        builder.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.setOnCancelListener(this);
        builder.show();
        return true;
    }

    public void setItems(List<String> items, boolean[] selected, String defaultText,
                         MultiSpinnerListener listener) {
        mItems = items;
        mDefaultText = defaultText;
        mListener = listener;

        if (selected == null) {
            // all selected by default
            mSelected = new boolean[items.size()];
            Arrays.fill(mSelected, true);
        } else {
            mSelected = selected;
        }
        refreshView();
    }

    public void setItems(List<String> mItems, boolean[] selected) {
        if (mDefaultText == null || mListener == null)
            throw new IllegalStateException("mDefaultText == null || mListener == null");
        setItems(mItems, selected, mDefaultText, mListener);
    }

    public interface MultiSpinnerListener {
        public void onItemsSelected(boolean[] selected);
    }

    public ArrayList<String> getListOfSelected() {
        ArrayList<String> strings = new ArrayList<String>();
        for (int i = 0; i < mSelected.length; i++)
            if(mSelected[i])
                strings.add(mItems.get(i));
        return strings;
    }

    public boolean[] getSelected() {
        return mSelected;
    }
}
