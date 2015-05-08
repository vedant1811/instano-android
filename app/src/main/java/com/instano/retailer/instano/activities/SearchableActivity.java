package com.instano.retailer.instano.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.SearchView;
import android.view.Menu;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.activities.search.ResultsActivity;
import com.instano.retailer.instano.application.BaseActivity;
import com.instano.retailer.instano.application.network.NetworkRequestsManager;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.model.Product;

import rx.Subscription;
import rx.android.observables.AndroidObservable;
import rx.subscriptions.BooleanSubscription;

/**
 * Created by vedant on 5/7/15.
 */
public abstract class SearchableActivity extends BaseActivity{
    public static final String KEY_PRODUCT = "SearchableActivity.product";
    public static final String KEY_PRODUCT_ID = "SearchableActivity.product_id";

    private static final int PRODUCT_NAME_INDEX = 1;
    private static final int ID_INDEX = 0;
    private static final String PRODUCT_NAME = "productName";

    private final String TAG = getClass().getSimpleName();
    private SimpleCursorAdapter mCursorAdapter;
    private Subscription mSuggestionsSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSuggestionsSubscription = BooleanSubscription.create();
        mCursorAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1,
                null,
                new String[] {PRODUCT_NAME},
                new int[] {android.R.id.text1},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onSearchRequested() {
        Log.d(TAG, "onSearchRequested");
        Cursor cursor = mCursorAdapter.getCursor();
//        Bundle bundle = new Bundle()
//                .put
        startSearch(cursor.getString(1), false, null, false);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_example).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        // TODO:
//        if(mQuery != null) {
//            searchView.setQuery(mQuery,true);
//        }
        searchView.setSuggestionsAdapter(mCursorAdapter);
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int i) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int i) {
                Cursor cursor = mCursorAdapter.getCursor();
                cursor.moveToPosition(i);
                searchView.setQuery(cursor.getString(PRODUCT_NAME_INDEX), true);
                Log.v(TAG, "Suggestion Clicked : " + cursor.getString(PRODUCT_NAME_INDEX));
                Intent intent = new Intent(SearchableActivity.this, ResultsActivity.class);
                intent.putExtra(KEY_PRODUCT, cursor.getString(PRODUCT_NAME_INDEX));
                intent.putExtra(KEY_PRODUCT_ID, cursor.getInt(ID_INDEX));
                startActivity(intent);
                return false;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Log.v(TAG, "Query submitted " + s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.v(TAG, "Query text changed");
                mSuggestionsSubscription.unsubscribe();
                mSuggestionsSubscription = AndroidObservable.bindActivity(SearchableActivity.this, NetworkRequestsManager.instance().queryProducts(s))
                        .subscribe(products -> {
                            MatrixCursor cursor = new MatrixCursor(new String[]{BaseColumns._ID, PRODUCT_NAME});
                            for (Product product : products) {
                                cursor.addRow(new Object[]{product.id, product.name});
                            }
                            mCursorAdapter.changeCursor(cursor);
                        }, throwable -> Log.fatalError(new RuntimeException(throwable)));

                return true;
            }
        });
        return true;
    }

}
