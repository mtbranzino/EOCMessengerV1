package com.huntloc.eocmessenger;


import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static final String REQUEST_ACCEPT = "REQUEST_ACCEPT";
    public MyFirebaseMessagingService() {

    }
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("onMessageReceived",remoteMessage.getData().get("data"));
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getBaseContext());
        Intent intent = new Intent(REQUEST_ACCEPT);
        intent.putExtra("data", remoteMessage.getData().get("data"));
        broadcastManager.sendBroadcast(intent);
    }
}
