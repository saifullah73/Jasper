package com.example.jasper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;

import android.content.Context;
import android.content.Intent;
import android.os.Build;


import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.view.View;

import com.example.jasper.Activities.MainActivity.MainActivity;

public class Notification {

    public static final String KEY_TEXT_REPLY = "key_text_reply";
    public static final String CHANNEL_ID = "channel1";
    Context context;

    public Notification(Context context) {
        this.context=context;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel1";
            String description = "This is channel 1";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
             NotificationManager notificationManager =  context.getSystemService(NotificationManager.class);

            notificationManager.createNotificationChannel(channel);
        }


    }
    //displayNotification method should have two argument like mentioned in next line
//public void displayNotification(String title,String message){
    public void displayNotification(){
        createNotificationChannel();

        //here MainActivity.class should be replaced with the activity which you want to open when you tap the notification
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);





        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                //Replace "Azam" with sender's name
                .setContentTitle("Azam")
                //Replace "Hello Nibba!!!" with sender's Message
                .setContentText("Hello Nibba!!!")
                .setPriority(NotificationCompat.PRIORITY_MAX )
                .setContentIntent(pendingIntent);


        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){

            // Here "KEY_TEXT_REPLY" is the ID for the reply
            RemoteInput remoteInput=new RemoteInput.Builder(KEY_TEXT_REPLY)
                    .setLabel("Message").build();


            //here "MainActivity.class" should be replaced with the broadcast receiver or activity where you want to display the message sent
            Intent replyIntent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent replyPendingIntent = PendingIntent.getActivity(context, 0, replyIntent, PendingIntent.FLAG_ONE_SHOT);

            NotificationCompat.Action action= new NotificationCompat.Action.Builder(R.mipmap.ic_launcher,"Reply",replyPendingIntent)
                    .addRemoteInput(remoteInput).build();
            builder.addAction(action);


        }


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

// notificationId is a unique int for each notification that you must define
        notificationManager.notify(1, builder.build());
    }

}
