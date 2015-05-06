package com.instano.retailer.instano.application.controller;

import com.instano.retailer.instano.application.controller.model.QuotationCard;
import com.instano.retailer.instano.application.controller.model.QuotationMarker;
import com.instano.retailer.instano.application.network.NetworkRequestsManager;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.model.Outlet;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;

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
        SerializedSubject<QuotationMarker, QuotationMarker> subject = new SerializedSubject<> (PublishSubject.create());
        Log.v(TAG, "fetchQuotationMarkersForProduct" + subject.hasObservers());

        return subject
                .doOnSubscribe(() -> {
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

                });
//                .doOnError(throwable -> Log.fatalError(new RuntimeException(
//                        "error response in subscribe after doOnSubscribe",
//                        throwable)))
//                        // combine with another observable that emits items regularly (every 100ms)
//                        // so that a new event is received every 100ms :
//                        // also, first event itself is delayed.
//                .onBackpressureBuffer() // prevent zipWith Observer.interval from throwing MissingBackpressureException s
//                .zipWith(Observable.interval(150, TimeUnit.MILLISECONDS),
//                        (seller, aLong) -> seller)
//                .onBackpressureBlock()
//                .doOnError(throwable -> Log.fatalError(new RuntimeException(
//                        "error response after onBackpressureBlock()",
//                        throwable)));
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
