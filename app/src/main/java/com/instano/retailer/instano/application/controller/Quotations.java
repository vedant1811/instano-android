package com.instano.retailer.instano.application.controller;

import android.util.SparseArray;

import com.instano.retailer.instano.application.controller.model.QuotationCard;
import com.instano.retailer.instano.application.controller.model.QuotationMarker;
import com.instano.retailer.instano.application.network.NetworkRequestsManager;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.model.Outlet;

import rx.Observable;
import rx.subjects.ReplaySubject;

/**
 * Created by vedant on 5/2/15.
 */
public class Quotations {
    private static final String TAG = "Quotations";
    private static Quotations sInstance;
    private SparseArray<ReplaySubject<QuotationCard>> mQuotationSubjects;

    public Observable<QuotationCard> fetchQuotationsForProduct(int productId) {
        ReplaySubject<QuotationCard> replaySubject = mQuotationSubjects.get(productId);
        if (replaySubject == null) {

            // now create a new subject
            final ReplaySubject<QuotationCard> finalReplaySubject = ReplaySubject.create();

            // fetch quotations
            NetworkRequestsManager.instance().queryQuotations(productId).subscribe(quotation -> {
                        Log.d(TAG, "new quotation " + quotation.hashCode());
                        // fetch sellers for each quotation
                        Sellers.controller().getSeller(quotation.sellerId)
                                .subscribe(seller -> finalReplaySubject.onNext(new QuotationCard(seller, quotation)),
                                        error -> Log.fatalError(new RuntimeException(error)));
                },
                error -> Log.fatalError(new RuntimeException(error)));

            // TODO: make conditional
            // fetch brand_name matching sellers without quotations
            NetworkRequestsManager.instance().querySellers(productId).subscribe(partialSeller -> {
                        Log.d(TAG, "new seller" + partialSeller.hashCode());
                        // get the seller for the returned ID
                        Sellers.controller().getSeller(partialSeller.id)
                                .subscribe(seller -> finalReplaySubject.onNext(new QuotationCard(seller, null)),
                                        error -> Log.fatalError(new RuntimeException(error)));
                    },
                    error -> Log.fatalError(new RuntimeException(error)));

            // subject created, now add it to the array
            replaySubject = finalReplaySubject;
            mQuotationSubjects.put(productId, replaySubject);
        }
        return replaySubject.asObservable();
    }

    public Observable<QuotationMarker> fetchQuotationMarkersForProduct(int productId) {
        return fetchQuotationsForProduct(productId)
                .flatMap(quotationCard -> Observable.create(subscriber -> {
                    for (Outlet outlet : quotationCard.seller.outlets) {
                        if (!subscriber.isUnsubscribed() && outlet.latitude != null && outlet.longitude != null)
                            subscriber.onNext(new QuotationMarker(outlet, quotationCard.quotation.price));
                    }
                }));
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
        mQuotationSubjects = new SparseArray<>();
    }

}
