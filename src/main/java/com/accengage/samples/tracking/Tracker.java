package com.accengage.samples.tracking;


import com.ad4screen.sdk.analytics.Cart;
import com.ad4screen.sdk.analytics.Lead;
import com.ad4screen.sdk.analytics.Purchase;

public interface Tracker {

    void trackEvent(long id, String value);

    void trackLead(Lead lead);

    void trackCart(Cart cart);

    void trackPurchase(Purchase purchase);

    void updateDeviceInfo(String key, String value);

    void trackMessageDisplay(String messageId);

    void trackMessageClick(String messageId);

    void trackMessageButtonClick(String messageId, String buttonId, String title);

}
