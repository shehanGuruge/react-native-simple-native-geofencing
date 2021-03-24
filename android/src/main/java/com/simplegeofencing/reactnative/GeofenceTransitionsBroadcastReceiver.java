package com.simplegeofencing.reactnative;


import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.app.LocalActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.facebook.react.bridge.ReactApplicationContext;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class GeofenceTransitionsBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "GeofenceBroadcastReceiver";
    private final String CHANNEL_ID = "channel_01";
    private static final String NOTIFICATION_TAG = "GeofenceNotification";
    private static final int NOTIFICATION_ID = 101;
    private NotificationChannel channel;
    private static final String PREFERENCE_LAST_NOTIF_ID = "PREFERENCE_LAST_NOTIF_ID";
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.mContext = context;
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = "Error Code: " + String.valueOf(geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }
        ArrayList<String> geofenceAttachments = new ArrayList<String>();
        Log.i(TAG, "herhehr " );

        System.out.println("keeps recienvinb");
        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            final String geofenceTransitionDetails = getGeofenceTransitionDetails(
                    geofenceTransition,
                    triggeringGeofences
            );

            Log.i(TAG, "Trigeering ge" + triggeringGeofences);
            //Check Monitor
            if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT){
                for (Geofence geofence : triggeringGeofences) {
                    if(geofence.getRequestId().equals("monitor")){
                        Log.i(TAG, "Outside Monitor");

                        //SEND CALLBACK
                        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this.mContext);
                        Intent customEvent= new Intent("outOfMonitorGeofence");
                        customEvent.putExtra("startTime", intent.getLongExtra("startTime", System.currentTimeMillis()));
                        customEvent.putExtra("duration", intent.getIntExtra("duration", 3000));
                        localBroadcastManager.sendBroadcast(customEvent);

                    }
                }
            }

            //Get (only) last triggered geofence which is not monitor
            ArrayList<Geofence> geofencesWithoutMonitor = new ArrayList<>();
            for (Geofence geofence : triggeringGeofences) {
                Log.i("GEOFENCIE: ", geofence + "geofw" + geofence.getRequestId());
                if(!geofence.getRequestId().equals("monitor")){
                    geofencesWithoutMonitor.add(geofence);
                }
            }

            //Check entering a Geofence
            if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER && geofencesWithoutMonitor.size() > 0){
                if(intent.getBooleanExtra("notifyEnter", false)){
                    Geofence geofence = geofencesWithoutMonitor.get(0);
                    String title = intent.getStringExtra("notifyEnterStringTitle");
                    String description = intent.getStringExtra("notifyEnterStringDescription");
                    String attachmentURL = null;
                    Log.i("hshs","Title and descriptopn: "+ title + " " + description);
                    ArrayList<String> geofenceValues = intent.getStringArrayListExtra("geofenceValues");
                    ArrayList<String> geofenceKeys = intent.getStringArrayListExtra("geofenceKeys");
                    geofenceAttachments = intent.getStringArrayListExtra("geofenceAttachments");
                    int index = geofenceKeys.indexOf(geofence.getRequestId());
                    try {
                        Log.i("hshs","Geofence values: "+ geofenceValues + index + geofence.getRequestId());
                        Log.i("hshs","Geofence keys: "+ geofenceKeys);

                        description = description.replace("[value]", geofenceValues.get(index));
                        title = title.replace("[value]", geofenceValues.get(index));
                        attachmentURL = geofenceAttachments.get(index);
                    } catch (IndexOutOfBoundsException e) {
                        Log.i(TAG, "No value set");
                    }

                    postNotification(
                            title,
                            description,
                            intent.getStringExtra("notifyChannelStringTitle"),
                            intent.getStringExtra("notifyChannelStringDescription"),
                            intent,
                            attachmentURL
                    );
                }else{
                    clearNotification();
                }
            }
            //Check exiting a Geofence
            if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT && geofencesWithoutMonitor.size() > 0){

                if (intent.getBooleanExtra("notifyExit", false)){
                    Geofence geofence = geofencesWithoutMonitor.get(0);
                    String title = intent.getStringExtra("notifyExitStringTitle");
                    String description = intent.getStringExtra("notifyExitStringDescription");
                    String attachmentURL = null;
                    ArrayList<String> geofenceValues = intent.getStringArrayListExtra("geofenceValues");
                    ArrayList<String> geofenceKeys = intent.getStringArrayListExtra("geofenceKeys");
                    int index = geofenceKeys.indexOf(geofence.getRequestId());
                    try {
                        Log.i("hshs","Geofence values: "+ geofenceValues);
                        Log.i("hshs","Geofence keys: "+ geofenceKeys);
                        description = description.replace("[value]", geofenceValues.get(index));
                        title = title.replace("[value]", geofenceValues.get(index));
                        attachmentURL = geofenceAttachments.get(index);
                    } catch (IndexOutOfBoundsException e) {
                        Log.i(TAG, "No value set");
                    }

                    postNotification(
                            title,
                            description,
                            intent.getStringExtra("notifyChannelStringTitle"),
                            intent.getStringExtra("notifyChannelStringDescription"),
                            intent,
                            attachmentURL
                    );
                }else{
                    clearNotification();
                }

            }

            Log.i(TAG, geofenceTransitionDetails);
            //RNSimpleNativeGeofencingModule.postNotification(TAG, geofenceTransitionDetails);
        } else {
            // Log the error.
            Log.e(TAG, "Invalid transition");
            /*
            postNotification(
                    "Error",
                    "Inside Handel",
                    intent.getStringExtra("notifyChannelStringTitle"),
                    intent.getStringExtra("notifyChannelStringDescription")
            );
            */
        }
    }

    /*
        Help functions for logging
     */
    private String getGeofenceTransitionDetails(
            int geofenceTransition,
            List<Geofence> triggeringGeofences) {

        String geofenceTransitionString = getTransitionString(geofenceTransition);

        // Get the Ids of each geofence that was triggered.
        ArrayList<String> triggeringGeofencesIdsList = new ArrayList<>();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ",  triggeringGeofencesIdsList);
        Log.i("logos","jnj");
        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }

    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "entered Geofence";
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "exit Geofence";
            default:
                return "unknown Transition";
        }
    }

    /*
       Notifications
    */
    private NotificationCompat.Builder getNotificationBuilder(String title,
                                                              String content,
                                                              String channelTitle,
                                                              String channelDescription,
                                                              Intent pIntent,
                                                              String attachmentURL
    ) {
        NotificationCompat.Builder notification = null;
        //Onclick
        Intent intent = new Intent(this.mContext, getMainActivityClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(this.mContext, 0, intent, 0);
        //Intent intent = new Intent(this.mContext, NotificationEventReceiver.class);
        //PendingIntent contentIntent = PendingIntent.getBroadcast(this.mContext, NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if(attachmentURL == null || attachmentURL == ""){
           notification = new NotificationCompat.Builder(this.mContext, CHANNEL_ID)
                    .setContentTitle(title)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                    .setContentText(content)
                    .setSmallIcon(this.mContext.getApplicationInfo().icon)
                    .setContentIntent(contentIntent)
                    .setOngoing(true)
                    .setAutoCancel(true);
        }else{
            Bitmap bitmap = null;
            try{
                AsyncTask<String, Void, Bitmap> request = new FetchImage().execute(attachmentURL);
                bitmap = request.get();
            }catch (Exception e){
                System.out.println("Failed to fint the image");
            }
            //Build notification
            notification = new NotificationCompat.Builder(this.mContext, CHANNEL_ID)
                    .setContentTitle(title)
                    .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap))
                    .setContentText(content)
                    .setSmallIcon(this.mContext.getApplicationInfo().icon)
                    .setContentIntent(contentIntent)
                    .setOngoing(true)
                    .setAutoCancel(true);
        }

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && channel==null) {
            CharSequence name = channelTitle;
            String description = channelDescription;
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = this.mContext.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            notification.setChannelId(CHANNEL_ID);
        }
        return notification;
    }

    public void postNotification(String title,
                                 String content,
                                 String channelTitle,
                                 String channelDescription,
                                 Intent pIntent,
                                 String attachmentURL
    ){
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this.mContext);

        // notificationId is a unique int for each notification that you must define
        Notification n = getNotificationBuilder(title, content, channelTitle, channelDescription, pIntent, attachmentURL).build();
        n.flags = Notification.FLAG_ONGOING_EVENT;
        notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID,
               n);
    }
    public void clearNotification(){
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this.mContext);
        notificationManager.cancel(NOTIFICATION_TAG, NOTIFICATION_ID);
    }

    public Class getMainActivityClass() {
        String packageName = this.mContext.getPackageName();
        Intent launchIntent = this.mContext.getApplicationContext().getPackageManager().getLaunchIntentForPackage(packageName);
        String className = launchIntent.getComponent().getClassName();

        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}