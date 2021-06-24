package com.robotemplates.webviewapp.fcm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.robotemplates.webviewapp.R;
import com.robotemplates.webviewapp.activity.MainActivity;
import com.robotemplates.webviewapp.utility.Preferences;

import org.alfonz.utility.Logcat;

public class WebViewAppFirebaseMessagingService extends FirebaseMessagingService {
	@Override
	public void onNewToken(@NonNull String token) {
		super.onNewToken(token);
		saveFcmRegistrationToken(token);
	}

	@Override
	public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
		super.onMessageReceived(remoteMessage);

		if (remoteMessage.getNotification() != null) {
			Logcat.d("title = " + remoteMessage.getNotification().getTitle());
			Logcat.d("body = " + remoteMessage.getNotification().getBody());
			Logcat.d("sound = " + remoteMessage.getNotification().getSound());

			String title = remoteMessage.getNotification().getTitle() != null ? remoteMessage.getNotification().getTitle() : getString(R.string.app_name);
			String text = remoteMessage.getNotification().getBody() != null ? remoteMessage.getNotification().getBody() : "";
			String sound = remoteMessage.getNotification().getSound();
			String url = null;

			if (remoteMessage.getData().get("url") != null) {
				Logcat.d("url = " + remoteMessage.getData().get("url"));
				url = remoteMessage.getData().get("url");
			}

			sendNotification(title, text, sound, url);
		}
	}

	private void sendNotification(String title, String text, String sound, String url) {
		String channelId = getString(R.string.fcm_default_notification_channel_id);
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		if (notificationManager != null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				String channelTitle = getString(R.string.fcm_default_notification_channel_title);
				NotificationChannel channel = new NotificationChannel(channelId, channelTitle, NotificationManager.IMPORTANCE_DEFAULT);
				notificationManager.createNotificationChannel(channel);
			}

			notificationManager.notify(0, createNotification(title, text, sound, url, channelId));
		}
	}

	private Notification createNotification(String title, String text, String sound, String url, String channelId) {
		int color = ContextCompat.getColor(this, R.color.global_color_accent);
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
				.setSmallIcon(R.drawable.ic_stat_notification)
				.setContentTitle(title)
				.setContentText(text)
				.setColor(color)
				.setAutoCancel(true)
				.setContentIntent(createPendingIntent(url));

		if (sound != null) {
			Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			notificationBuilder.setSound(soundUri);
		}

		return notificationBuilder.build();
	}

	private PendingIntent createPendingIntent(String url) {
		Intent intent = url == null ? MainActivity.newIntent(this) : MainActivity.newIntent(this, url);
		return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
	}

	private void saveFcmRegistrationToken(String token) {
		if (token != null) {
			Logcat.d("token = " + token);
			Preferences preferences = new Preferences();
			preferences.setFcmRegistrationToken(token);
		}
	}
}
