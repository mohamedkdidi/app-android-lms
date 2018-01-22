package com.uvt.miniprojet;

import android.app.Application;
import android.content.Intent;

import com.crittercism.app.Crittercism;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import org.json.JSONObject;

/**
 * @author Mohamed Kdidi
 * Copyright 2017
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //Auto-error reporting
        Crittercism.initialize(this, "fdfe66d685a640d3a36c2649a4e6c49b00555300");

        //OneSignal Push
        OneSignal.startInit(this)
                .setNotificationOpenedHandler(new NotificationHandler())
                .init();
    }

    // This fires when a notification is opened by tapping on it or one is received while the app is running.
    class NotificationHandler implements OneSignal.NotificationOpenedHandler {
        // This fires when a notification is opened by tapping on it.
        @Override
        public void notificationOpened(OSNotificationOpenResult result) {
            try {
                JSONObject data = result.notification.payload.additionalData;
                String webViewUrl = (data != null) ? data.optString("url", null) : null;

                String browserUrl = result.notification.payload.launchURL;
                if (webViewUrl != null || browserUrl != null) {
                    if (webViewUrl != null){
                        HolderActivity.startWebViewActivity(App.this, webViewUrl, false, false, null, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                    } else {
                        HolderActivity.startWebViewActivity(App.this, browserUrl, true, false, null, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                    }
                } else if (!result.notification.isAppInFocus) {
                    Intent mainIntent;
                    mainIntent = new Intent(App.this, MainActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(mainIntent);
                }

            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

    }
}