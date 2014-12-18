package com.instano.retailer.instano.utilities;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.instano.retailer.instano.R;

/**
 * Created by vedant on 15/12/14.
 */
public class GlobalMenuActivity extends Activity {

    private static final int SHARE_REQUEST_CODE = 998;
    private static final int MESSAGE_REQUEST_CODE = 997;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // TODO: handle separately
            case SHARE_REQUEST_CODE:
            case MESSAGE_REQUEST_CODE:
                if (resultCode == RESULT_OK)
                    Toast.makeText(this, "success", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intent;
        switch (id) {

//            case R.id.action_about_us:
//                return true;

            case R.id.action_share:
                intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Instano Retailer");
                String sAux = "Let me recommend you this application\n";
                sAux = sAux + "http://play.google.com/store/apps/details?id=com.instano.retailer";
                intent.putExtra(Intent.EXTRA_TEXT, sAux);
                intent = Intent.createChooser(intent, "choose one");
                try {
                    startActivityForResult(intent, SHARE_REQUEST_CODE);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(this, "There are no email clients installed", Toast.LENGTH_SHORT).show();
                }
                return true;

            case R.id.action_message:
                intent = new Intent(Intent.ACTION_SEND);
                intent.setType("message/rfc822");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"contact@instano.in"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Contacting instano");
                intent = Intent.createChooser(intent, "Send mail");
                try {
                    startActivityForResult(intent, MESSAGE_REQUEST_CODE);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(this, "There are no email clients installed", Toast.LENGTH_SHORT).show();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.global, menu);

        return true;
    }

}
