package com.accengage.samples.push;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;

import com.accengage.samples.R;
import com.accengage.samples.backstack.BackstackActivity;
import com.accengage.samples.backstack.BackstackActivityWithParent;
import com.accengage.samples.backstack.BackstackPreferencesActivity;
import com.ad4screen.sdk.Log;
import com.ad4screen.sdk.service.modules.push.NotificationClientCreator;

import java.net.URISyntaxException;

public class MyNotificationClientCreator implements NotificationClientCreator {

    public MyNotificationClientCreator() {
        Log.info("MyNotificationClientCreator|MyNotificationClientCreator called");
    }

    @Override
    public TaskStackBuilder getTaskStackBuilder(Context context, Bundle customParams) {
        Log.info("MyNotificationClientCreator|getTaskStackBuilder called, bundle = " + customParams.toString());

        SharedPreferences prefs = context.getSharedPreferences(BackstackPreferencesActivity.BACKSTACK_PREFERENCES_FILE_NAME, Context.MODE_MULTI_PROCESS);
        Resources res = context.getResources();
        boolean isEnabled = prefs.getBoolean(res.getString(R.string.pref_notif_msg_backstack_enable_key), false);
        if (!isEnabled)
            return null;

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
        boolean isWithParentActivity = prefs.getBoolean(res.getString(R.string.pref_notif_msg_backstack_with_parent_enable_key), false);
        if (isWithParentActivity) {
            Intent intent = new Intent(context, BackstackActivityWithParent.class);
            // If you need to pass extra parameters inside intents to your parent activities please use addNextIntent
            // instead of addNextIntentWithParentStack.
            taskStackBuilder.addNextIntentWithParentStack(intent);
        } else {
            int activitiesAmount = Integer.valueOf(prefs.getString(res.getString(R.string.pref_notif_msg_backstack_amount_key), "1"));
            Log.debug("MyNotificationClientCreator|getTaskStackBuilder activitiesAmount=" + activitiesAmount);
            for (int i = 0; i < activitiesAmount; i++) {
                Intent intent = new Intent(context, BackstackActivity.class);
                intent.putExtra(BackstackActivity.EXTRA_PARAM_KEY, "#" + String.valueOf(i+1));
                taskStackBuilder.addNextIntent(intent);
            }
        }

        boolean urlToHandle = prefs.getBoolean(res.getString(R.string.pref_notif_msg_backstack_handle_url_key), false);
        if (urlToHandle) {
            String url = customParams.getString("a4surl");
            try {
                Intent targetIntent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                taskStackBuilder.addNextIntent(targetIntent);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        return taskStackBuilder;
    }

    @Override
    public Intent getMainIntent(Context context, Bundle customParams) {
        Log.info("MyNotificationClientCreator|getMainIntent called");
        return null;
    }

    @Override
    public TaskStackBuilder getTaskStackBuilderForButton(Context context, int indexButton, Bundle customParams) {
        Log.info("MyNotificationClientCreator|getTaskStackBuilderForButton called with index " + indexButton +
                ", bundle = " + customParams.toString());

        SharedPreferences prefs = context.getSharedPreferences(BackstackPreferencesActivity.BACKSTACK_PREFERENCES_FILE_NAME, Context.MODE_MULTI_PROCESS);
        Resources res = context.getResources();
        int enableKey;
        int withParentKey;
        int amountKey;
        int handleUrlKey;

        switch (indexButton) {
            case 1:
                enableKey = R.string.pref_notif_button1_backstack_enable_key;
                withParentKey = R.string.pref_notif_button1_backstack_with_parent_enable_key;
                amountKey = R.string.pref_notif_button1_backstack_amount_key;
                handleUrlKey = R.string.pref_notif_button1_backstack_handle_url_key;
                break;
            case 2:
                enableKey = R.string.pref_notif_button2_backstack_enable_key;
                withParentKey = R.string.pref_notif_button2_backstack_with_parent_enable_key;
                amountKey = R.string.pref_notif_button2_backstack_amount_key;
                handleUrlKey = R.string.pref_notif_button2_backstack_handle_url_key;
                break;
            default:
                return null;
        }

        boolean isEnabled = prefs.getBoolean(res.getString(enableKey), false);
        if (!isEnabled)
            return null;

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);

        boolean isWithParentActivity = prefs.getBoolean(res.getString(withParentKey), false);
        if (isWithParentActivity) {
            Intent intent = new Intent(context, BackstackActivityWithParent.class);
            // If you need to pass extra parameters inside intents to your parent activities please use addNextIntent
            // instead of addNextIntentWithParentStack.
            taskStackBuilder.addNextIntentWithParentStack(intent);
        } else {
            int activitiesAmount = Integer.valueOf(prefs.getString(res.getString(amountKey), "1"));
            Log.debug("MyNotificationClientCreator|getTaskStackBuilderForButton activitiesAmount=" + activitiesAmount);
            for (int i = 0; i < activitiesAmount; i++) {
                Intent intent = new Intent(context, BackstackActivity.class);
                intent.putExtra(BackstackActivity.EXTRA_PARAM_KEY, "#" + String.valueOf(i+1));
                taskStackBuilder.addNextIntent(intent);
            }
        }


        boolean urlToHandle = prefs.getBoolean(res.getString(handleUrlKey), false);
        if (urlToHandle) {
            String url = customParams.getString("a4surl");
            try {
                Intent targetIntent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                taskStackBuilder.addNextIntent(targetIntent);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        return taskStackBuilder;
    }

    @Override
    public Intent getIntentForButton(Context context, int indexButton, Bundle customParams) {
        Log.info("MyNotificationClientCreator|getIntentForButton with index " + indexButton + " called");
        return null;
    }

    @Override
    public Bitmap getLargeIcon(Context context, Bundle customParams) {
        Log.info("MyNotificationClientCreator|getLargeIcon called");
        return null;
    }

    @Override
    public String getLargeIconUrl(Context context, Bundle customParams) {
        Log.info("MyNotificationClientCreator|getLargeIconUrl called");
        return null;
    }

    @Override
    public NotificationCompat.Builder getNotificationBuilder(Context context, NotificationCompat.Builder notificationBuilder, Bundle customParams) {
        Log.info("MyNotificationClientCreator|getNotificationBuilder called");
        return null;
    }

    @Override
    public Notification getNotification(Context context, Notification notification, Bundle customParams) {
        Log.info("MyNotificationClientCreator|getNotification called");
        return null;
    }

}
