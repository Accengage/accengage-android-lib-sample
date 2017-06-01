package com.accengage.samples.tracking;

import android.content.Context;
import android.os.Bundle;

import com.ad4screen.sdk.analytics.Cart;
import com.ad4screen.sdk.analytics.Item;
import com.ad4screen.sdk.analytics.Lead;
import com.ad4screen.sdk.analytics.Purchase;
import com.google.firebase.analytics.FirebaseAnalytics;

public class FirebaseTracker implements Tracker {

    private static final String ACC_EVENT = "Accengage#";
    private static final String ACC_EVENT_ID = "acc_event_id";

    private FirebaseAnalytics mFirebaseAnalytics;
    protected Context mContext;

    public FirebaseTracker(Context context) {
        mContext = context;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mContext);
    }

    @Override
    public void trackEvent(long id, String value) {
        Bundle bundle = new Bundle();
        String eventId = Long.toString(id);
        bundle.putString(ACC_EVENT_ID, eventId);
        bundle.putString(FirebaseAnalytics.Param.VALUE, value);
        mFirebaseAnalytics.logEvent(ACC_EVENT + eventId, bundle);
    }

    @Override
    public void trackLead(Lead lead) {
        Bundle bundle = new Bundle();
        bundle.putString(ACC_EVENT_ID, "10");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, lead.getLabel());
        bundle.putString(FirebaseAnalytics.Param.VALUE, lead.getValue());
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.GENERATE_LEAD, bundle);
    }

    @Override
    public void trackCart(Cart cart) {
        Bundle bundle = new Bundle();
        bundle.putString(ACC_EVENT_ID, "30");
        bundle.putString(Cart.KEY_ID, cart.getId());
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, cart.getItem().getId());
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, cart.getItem().getLabel());
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, cart.getItem().getCategory());
        bundle.putString(FirebaseAnalytics.Param.CURRENCY, cart.getItem().getCurrency());
        bundle.putInt(FirebaseAnalytics.Param.QUANTITY, cart.getItem().getQuantity());
        bundle.putDouble(FirebaseAnalytics.Param.PRICE, cart.getItem().getPrice());
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.ADD_TO_CART, bundle);
    }

    @Override
    public void trackPurchase(Purchase purchase) {
        Bundle bundle = new Bundle();

        bundle.putString(ACC_EVENT_ID, "50");
        bundle.putString(Purchase.KEY_ID, purchase.getId());
        Item[] items = purchase.getItems();
        if (items != null && items.length > 0) {
            int i = 0;
            for (Item item : items) {
                Bundle itemBundle = new Bundle();
                itemBundle.putString(FirebaseAnalytics.Param.ITEM_ID, item.getId());
                itemBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, item.getLabel());
                itemBundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, item.getCategory());
                itemBundle.putString(FirebaseAnalytics.Param.CURRENCY, item.getCurrency());
                itemBundle.putInt(FirebaseAnalytics.Param.QUANTITY, item.getQuantity());
                itemBundle.putDouble(FirebaseAnalytics.Param.PRICE, item.getPrice());
                bundle.putBundle("acc_purchase_item#" + i++, itemBundle);
            }
        }
        bundle.putString(FirebaseAnalytics.Param.CURRENCY, purchase.getCurrency());
        bundle.putDouble(FirebaseAnalytics.Param.PRICE, purchase.getTotalPrice());
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.ECOMMERCE_PURCHASE, bundle);
    }

    @Override
    public void updateDeviceInfo(String key, String value) {
        mFirebaseAnalytics.setUserProperty(key, value);
    }
}
