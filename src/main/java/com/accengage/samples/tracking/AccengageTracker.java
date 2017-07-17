package com.accengage.samples.tracking;

import android.content.Context;
import android.os.Bundle;

import com.ad4screen.sdk.A4S;
import com.ad4screen.sdk.analytics.Cart;
import com.ad4screen.sdk.analytics.Lead;
import com.ad4screen.sdk.analytics.Purchase;


public class AccengageTracker implements Tracker {

    protected Context mContext;

    public AccengageTracker(Context context) {
        mContext = context;
    }

    public void trackEvent(long id, String value) {
        A4S.get(mContext).trackEvent(id, value);
    }

    public void trackLead(Lead lead) {
        A4S.get(mContext).trackLead(lead);
    }

    public void trackCart(Cart cart) {
        A4S.get(mContext).trackAddToCart(cart);
    }

    public void trackPurchase(Purchase purchase) {
        A4S.get(mContext).trackPurchase(purchase);
    }

    public void updateDeviceInfo(String key, String value) {
        Bundle udis = new Bundle();
        udis.putString(key, value);
        A4S.get(mContext).updateDeviceInfo(udis);
    }

    @Override
    public void trackMessageDisplay(String messageId) {
        //A4S.get(mContext).trackInboxDisplay(messageId); // TODO make public in Acc.java
    }

    @Override
    public void trackMessageClick(String messageId) {
        //A4S.get(mContext).trackInboxClick(messageId); // TODO make public in Acc.java
    }

    @Override
    public void trackMessageButtonClick(String messageId, String buttonId, String title) {
        //A4S.get(mContext).trackInboxButtonClick(messageId, buttonId); // TODO make public in Acc.java
    }

}
