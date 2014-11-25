package com.instano.retailer.instano.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.buyerDashboard.QuotationListActivity;
import com.instano.retailer.instano.search.SearchTabsActivity;

public class HomeActivity extends Activity {

    public void previousQueriesClicked(View view) {
        startActivity(new Intent(this, QuotationListActivity.class));
    }

    public void newSearchClicked(View view) {
        startActivity(new Intent(this, SearchTabsActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
