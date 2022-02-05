package com.gabriel.aranias.go4lunch_v2.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;

import com.gabriel.aranias.go4lunch_v2.R;
import com.gabriel.aranias.go4lunch_v2.service.user.UserHelper;
import com.gabriel.aranias.go4lunch_v2.utils.Constants;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class NotificationReceiver extends BroadcastReceiver {

    private Context context;
    private final UserHelper userHelper = UserHelper.getInstance();

    public NotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;

        if (userHelper.isCurrentUserLoggedIn()) {
            // Check if user has chosen a place for lunch
            userHelper.getUserData().addOnSuccessListener(user -> {
                String lunchSpotId = user.getLunchSpotId();
                if (lunchSpotId != null) {
                    // Check if notifications are enabled for user
                    Boolean isNotificationEnabled = user.getNotificationEnabled();
                    if (isNotificationEnabled) {
                        getJoiningWorkmates(lunchSpotId);
                    }
                }
            });
        }
    }

    private void getJoiningWorkmates(String lunchSpotId) {
        ArrayList<String> workmates = new ArrayList<>();
        userHelper.getUserCollection().get().addOnCompleteListener(task -> {
            if (task.getResult() != null) {
                for (DocumentSnapshot doc : task.getResult()) {
                    String placeId = doc.getString(Constants.LUNCH_SPOT_ID_FIELD);
                    if (placeId != null) {
                        if (placeId.equals(lunchSpotId)) {
                            // Add name of each other workmate going to this place for lunch
                            String userId = doc.getString(Constants.USERNAME_FIELD);
                            if (!Objects.requireNonNull(userId).equals(
                                    userHelper.getCurrentUser().getUid())) {
                                workmates.add(userId);
                            }
                        }
                    }
                }
            }
            // Improve display of workmate list
            StringBuilder builder = new StringBuilder();
            int i = 0;
            for (String str : workmates) {
                builder.append(str);
                if (i != workmates.size() - 1) {
                    builder.append(R.string.and);
                } else {
                    builder.append(".");
                }
                i++;
            }
            // Create msg w/ info
            userHelper.getUserData().addOnSuccessListener(user ->  {
               String names = workmates.toString();
               String message = context.getString(R.string.notification_text, user.getLunchSpotName(),
                       user.getLunchSpotAddress(), names);
               showNotification(message);
            });
        });
    }

    private void showNotification(String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context, Constants.NOTIFICATION_CHANNEL_ID);
        builder.setSmallIcon(R.drawable.logo)
                .setContentTitle(context.getString(R.string.notification_title))
                .setAutoCancel(false)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

        NotificationManager manager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(
                    Constants.NOTIFICATION_CHANNEL_ID, Constants.NOTIFICATION_CHANNEL_NAME, importance);
            channel.enableLights(true);
            channel.enableVibration(true);
            if (manager != null) {
                builder.setChannelId(Constants.NOTIFICATION_CHANNEL_ID);
                manager.createNotificationChannel(channel);
            }
        }
        if (manager != null) {
            manager.notify(Constants.NOTIFICATION_ID, builder.build());
        }
    }
}
