package com.instano.retailer.instano.application.controller;

import com.instano.retailer.instano.application.controller.model.QuotationCard;
import com.instano.retailer.instano.application.network.NetworkRequestsManager;
import com.instano.retailer.instano.utilities.library.Log;

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

    public static Quotations controller() {
        if (sInstance == null)
            sInstance = new Quotations();
        return sInstance;
    }

    private Quotations() {

    }

}
