package com.accengage.samples.tracking;

import android.content.Context;
import android.os.Bundle;

import com.ad4screen.sdk.Acc;
import com.ad4screen.sdk.analytics.Cart;
import com.ad4screen.sdk.analytics.Lead;
import com.ad4screen.sdk.analytics.Purchase;


public class AccengageTracker implements Tracker {

    protected Context mContext;

    public AccengageTracker(Context context) {
        mContext = context;
    }

    public void trackEvent(long id, String value) {
        Acc.get(mContext).trackEvent(id, value);
    }

    public void trackLead(Lead lead) {
        Acc.get(mContext).trackLead(lead);
    }

    public void trackCart(Cart cart) {
        Acc.get(mContext).trackAddToCart(cart);
    }

    public void trackPurchase(Purchase purchase) {
        Acc.get(mContext).trackPurchase(purchase);
    }

    public void updateDeviceInfo(String key, String value) {
        Bundle udis = new Bundle();
        udis.putString(key, value);
        Acc.get(mContext).updateDeviceInfo(udis);
    }

    @Override
    public void trackMessageDisplay(String messageId) {
        //Acc.get(mContext).trackInboxDisplay(messageId); // TODO make public in Acc.java
    }

    @Override
    public void trackMessageClick(String messageId) {
        //Acc.get(mContext).trackInboxClick(messageId); // TODO make public in Acc.java
    }

}
