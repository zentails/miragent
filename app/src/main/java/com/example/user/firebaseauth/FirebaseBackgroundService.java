package com.example.user.firebaseauth;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.provider.ContactsContract;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseBackgroundService extends Service {
    public FirebaseBackgroundService() {
    }

    private FirebaseUser user;
    private DatabaseReference mDatabase,childRef;
    private ValueEventListener handler;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseApp.initializeApp(this);
        user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        childRef = mDatabase.child("users").child(user.getUid()).child("notifications");

        handler = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot arg0) {
                if(arg0 != null) {
                    com.example.user.firebaseauth.Notification temp;
                    temp = arg0.child(String.valueOf(arg0.getChildrenCount())).getValue(com.example.user.firebaseauth.Notification.class);
                    if(temp != null){
                        String tt = temp.tt;
                        String body = temp.body;
                        if(temp.read == false){
                            postNotif(tt,body);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError e) {
            }
        };

        childRef.addValueEventListener(handler);

        }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return Service.START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent broadcastIntent = new Intent("com.example.user.firebaseauth.RESTART_SERVICE");
        sendBroadcast(broadcastIntent);
    }

    private void postNotif(String title, String body) {
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, ShowProfile.class);
        // use System.currentTimeMillis() to have a unique ID for the pending intent
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

        // build notification
// the addAction re-use the same intent to keep the example short
        Notification n  = new Notification.Builder(this)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .addAction(R.drawable.common_google_signin_btn_icon_dark, "Call", pIntent)
                .addAction(R.drawable.common_google_signin_btn_icon_dark, "More", pIntent)
                .addAction(R.drawable.common_google_signin_btn_icon_dark, "And more", pIntent).build();

        notificationManager.notify(0, n);



    }
}
