package com.instano.retailer.instano.application.controller;

import com.instano.retailer.instano.application.controller.model.QuotationCard;
import com.instano.retailer.instano.application.controller.model.QuotationMarker;
import com.instano.retailer.instano.application.network.NetworkRequestsManager;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.model.Outlet;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by vedant on 5/2/15.
 */
public class Quotations {
    private static final String TAG = "Quotations";
    private static Quotations sInstance;

    public Observable<QuotationCard> fetchQuotationsForProduct(int productId) {
        PublishSubject<QuotationCard> subject = PublishSubject.create();
        Log.v(TAG, "fetchQuotationsForProduct" + subject.hasObservers());
//        subject.doOnSubscribe(() ->
                NetworkRequestsManager.instance().queryQuotations(productId).subscribe(quotation -> {
                            Log.d(TAG, "new quotation " + quotation.hashCode());
                            NetworkRequestsManager.instance().getSeller(quotation.sellerId)
                                    .subscribe(seller -> subject.onNext(new QuotationCard(seller, quotation)),
                                            error -> Log.fatalError(new RuntimeException(error)));
                        },
                        error -> Log.fatalError(new RuntimeException(error)));
//        );
        return subject.asObservable();
    }

    public Observable<QuotationMarker> fetchQuotationMarkersForProduct(int productId) {
        PublishSubject<QuotationMarker> subject = PublishSubject.create();
        Log.v(TAG, "fetchQuotationMarkersForProduct" + subject.hasObservers());

        NetworkRequestsManager.instance().queryQuotations(productId).subscribe(quotation -> {
                    Log.d(TAG, "new quotation " + quotation.hashCode());
                    NetworkRequestsManager.instance().getSeller(quotation.sellerId)
                            .subscribe(seller -> {
                                        for (Outlet outlet : seller.outlets) {
                                            if (outlet.latitude != null && outlet.longitude != null)
                                                subject.onNext(new QuotationMarker(outlet, quotation.price));
                                        }
                                    },
                                    error -> Log.fatalError(new RuntimeException(error)));
                },
                error -> Log.fatalError(new RuntimeException(error)));

        return subject
                .doOnError(throwable -> Log.fatalError(new RuntimeException(
                        "error response in subscribe to getFilteredSellersObservable",
                        throwable)))
                        // combine with another observable that emits items regularly (every 100ms)
                        // so that a new seller object is received every 100ms :
                        // also, first event itself is delayed. makes sure sellers are added after map is cleared
                .zipWith(Observable.interval(150, TimeUnit.MILLISECONDS),
                        (seller, aLong) -> seller)
                .onBackpressureBlock() // prevent zipWith Observer.interval from throwing MissingBackpressureException s
                .doOnError(throwable -> Log.fatalError(new RuntimeException(
                        "error response in subscribe to getFilteredSellersObservable",
                        throwable)));
        // all this was done on a non-UI thread
    }

    public static Quotations controller() {
        if (sInstance == null)
            sInstance = new Quotations();
        return sInstance;
    }

    private Quotations() {

    }

}
