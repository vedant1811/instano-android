package com.instano.retailer.instano.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.instano.retailer.instano.R;
import com.instano.retailer.instano.application.BaseActivity;
import com.instano.retailer.instano.application.ServicesSingleton;
import com.instano.retailer.instano.application.network.NetworkRequestsManager;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.model.Seller;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
/**
 * Created by Dheeraj on 06-May-15.
 */
public class SellerDetailActivity extends BaseActivity {

    private static final String TAG = "SellerDetailActivity";
    @InjectView(R.id.shop_details) TextView sellerDetail;
    @InjectView(R.id.shop_image) ImageView shopImage;
    @InjectView(R.id.dealHeadingStoreFooter) TextView dealHeading;
    @InjectView(R.id.dealSubheadingStoreFooter) TextView dealSubheading;
    @InjectView(R.id.contactButtonStoreFooter) ImageButton contactButton;
    @InjectView(R.id.msgButton) ImageButton msgButton;
    @InjectView(R.id.mapDirection) ImageButton direction;
    @InjectView(R.id.specification_list) TextView shopSpecification;
    @InjectView(R.id.bookitButton) Button bookItButton;


    private static final LatLng BANGALORE_LOCATION = new LatLng(12.9539974, 77.6309395);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);
        Bundle bundle = getIntent().getExtras();

        ButterKnife.inject(this);

        Observable<Seller> sellerObservable = NetworkRequestsManager.instance().getSeller(bundle.getInt("seller_id"));
        dealHeading.setText(bundle.getString("heading"));
        dealSubheading.setText(bundle.getString("subheading"));

        retryableError(sellerObservable, seller -> {

            String s = seller.name_of_shop;
            String s2 = seller.outlets.get(0).getPrettyDistanceFromLocation();
            Log.v(TAG, "lat long of outlet : " + seller.outlets.get(0).latitude +" :::"+seller.outlets.get(0).longitude);
            Log.v(TAG, "outlet id : " + seller.outlets.get(0).id);
            Log.v(TAG, "pretty distance : " + s2);
            String s3 = seller.outlets.get(0).address;

            SpannableString s1 = null;

            if (s2 == null)
                s1 = new SpannableString(s + ",\n" + s3);
            else
                s1 = new SpannableString(s + ", " + s2 + ",\n" + s3);

            s1.setSpan(new RelativeSizeSpan(1.3f), 0, s.length(), 0);  // 18 is the no. of character that is to be resized
            s1.setSpan(new ForegroundColorSpan(Color.BLACK), 0, 0, 0);
            sellerDetail.setText(s1);

            Picasso.with(this)
                    .load(seller.image).fit().centerInside()
                    .placeholder(R.drawable.no_image_available).fit().centerInside()
                    .into(shopImage);

            if(seller.description != null)
                shopSpecification.setText(Html.fromHtml(seller.description));
            else
                shopSpecification.setText("Shop Specification");

            contactButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" +
                            seller.outlets.get(0).getPhone()));
                    startActivity(callIntent);
                }
            });

            msgButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent msgIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" +
                            seller.outlets.get(0).getPhone()));
                    msgIntent.putExtra("sms_body", bundle.getString("heading") + "\n" + bundle.getString("subheading"));
                    startActivity(msgIntent);
                }
            });

            bookItButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(SellerDetailActivity.this, "Book it CLICKED !!!", Toast.LENGTH_SHORT).show();
                }
            });

            direction.setOnClickListener(new View.OnClickListener() {
                public static final String TAG = "SellerDetailActivity";

                @Override
                public void onClick(View v) {
                    LatLng startLatLng;
                    Location userLocation = ServicesSingleton.instance().getUserLocation();
                    if (userLocation != null)
                        startLatLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
                    else
                        startLatLng = BANGALORE_LOCATION;

//                    Intent intent = new Intent(Intent.ACTION_VIEW);
//                    intent.setData(Uri.parse(String.format("%f,%f?z=%d",
//                            userLocation.getLatitude(), userLocation.getLatitude(), 11)));
//                    if (intent.resolveActivity(getPackageManager()) != null)
//                        startActivity(intent);
                    Uri uri ;
                    if (seller.outlets.get(0).latitude == null || seller.outlets.get(0).longitude == null) {

                        String outletAddress = seller.outlets.get(0).address;
                        outletAddress = seller.name_of_shop + ", " + outletAddress;
                        outletAddress = outletAddress.replace("#", "%23");
                        outletAddress = outletAddress.replace('\n', ' ');
                        outletAddress = outletAddress.replace(' ', '+');
                        outletAddress = outletAddress.replace("++", "+");

                        String address = String.format("http://maps.google.com/maps?saddr=%f,%f&daddr=%s"
                                , startLatLng.latitude, startLatLng.longitude
                                , outletAddress);
                        uri = Uri.parse(address);
                        Log.v(TAG, "URI : " + uri);
                    }
                    else {
                        String address = String.format("http://maps.google.com/maps?saddr=%f,%f&daddr=%f,%f"
                                , startLatLng.latitude, startLatLng.longitude
                                , seller.outlets.get(0).latitude, seller.outlets.get(0).longitude);
                        Log.v(TAG, address);
                        uri = Uri.parse(address);
                    }

                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,uri);
                    if (packageExists("com.google.android.apps.maps"))
                        intent.setComponent(new ComponentName("com.google.android.apps.maps",
                                "com.google.android.maps.MapsActivity"));
                    startActivity(intent);
                }
            });
        });
    }

    private boolean packageExists(String targetPackage){
        PackageManager pm=getPackageManager();
        try {
            PackageInfo info=pm.getPackageInfo(targetPackage,PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }
}