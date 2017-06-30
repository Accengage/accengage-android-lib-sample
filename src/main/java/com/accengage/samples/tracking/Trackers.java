package com.accengage.samples.tracking;


import android.content.Context;

import com.ad4screen.sdk.analytics.Cart;
import com.ad4screen.sdk.analytics.Lead;
import com.ad4screen.sdk.analytics.Purchase;

import java.util.ArrayList;
import java.util.List;

public class Trackers implements Tracker {

    private List<Tracker> mTrackers;

    public Trackers() {

    }

    public Trackers(Context context) {
        mTrackers = new ArrayList<>(2);
        mTrackers.add(new AccengageTracker(context));
        mTrackers.add(new FirebaseTracker(context));
    }

    @Override
    public void trackEvent(long id, String value) {
        for(Tracker tracker : mTrackers) {
            tracker.trackEvent(id, value);
        }
    }

    @Override
    public void trackLead(Lead lead) {
        for(Tracker tracker : mTrackers) {
            tracker.trackLead(lead);
        }
    }

    @Override
    public void trackCart(Cart cart) {
        for(Tracker tracker : mTrackers) {
            tracker.trackCart(cart);
        }
    }

    @Override
    public void trackPurchase(Purchase purchase) {
        for(Tracker tracker : mTrackers) {
            tracker.trackPurchase(purchase);
        }
    }

    @Override
    public void updateDeviceInfo(String key, String value) {
        for(Tracker tracker : mTrackers) {
            tracker.updateDeviceInfo(key, value);
        }
    }

    @Override
    public void trackMessageDisplay(String messageId) {
        for(Tracker tracker : mTrackers) {
            tracker.trackMessageDisplay(messageId);
        }
    }

    @Override
    public void trackMessageClick(String messageId) {
        for(Tracker tracker : mTrackers) {
            tracker.trackMessageClick(messageId);
        }
    }
}
