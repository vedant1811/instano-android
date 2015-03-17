package com.instano.retailer.instano.sellers;

import android.app.Fragment;

/**
 * Created by vedant on 3/17/15.
 */
public abstract class AbstractFilterableFragment extends Fragment{
    /* package private */
    abstract void filterBasedOnCategory(String category);
}
