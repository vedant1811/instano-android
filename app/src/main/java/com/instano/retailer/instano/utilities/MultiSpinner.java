package com.instano.retailer.instano.utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.util.Log;
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

    private List<String> items;

    private boolean[] selected;
    private String defaultText;
    private MultiSpinnerListener listener;

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
        selected[which] = isChecked;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        refreshView();
        listener.onItemsSelected(selected);
    }

    private void refreshView() {
        // refresh text on spinner
        StringBuffer spinnerBuffer = new StringBuffer();
        boolean noneSelected = true;
        for (int i = 0; i < items.size(); i++) {
            if (selected[i]) {
                spinnerBuffer.append(items.get(i));
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
            spinnerText = defaultText;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item,
                new String[] { spinnerText });
        setAdapter(adapter);

        Log.d("Multispinner", "setting text to " + spinnerText);
    }

    @Override
    public boolean performClick() {
        if (items == null)
            return false;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMultiChoiceItems(
                items.toArray(new CharSequence[items.size()]), selected, this);
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
        this.items = items;
        this.defaultText = defaultText;
        this.listener = listener;

        if (selected == null) {
            // all selected by default
            this.selected = new boolean[items.size()];
            Arrays.fill(this.selected, true);
        } else {
            this.selected = selected;
        }
        refreshView();
    }

    public interface MultiSpinnerListener {
        public void onItemsSelected(boolean[] selected);
    }

    public ArrayList<String> getListOfSelected() {
        ArrayList<String> strings = new ArrayList<String>();
        for (int i = 0; i < selected.length; i++)
            if(selected[i])
                strings.add(items.get(i));
        return strings;
    }

    public boolean[] getSelected() {
        return selected;
    }
}
