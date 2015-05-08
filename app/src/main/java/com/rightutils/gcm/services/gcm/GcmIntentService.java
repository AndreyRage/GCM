package com.rightutils.gcm.services.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import java.util.Random;

import com.rightutils.gcm.R;
import com.rightutils.gcm.activities.MainActivity;

/**
 * Created by eKreative on 2/26/14.
 */
public class GcmIntentService extends IntentService {

	public static final String TAG = GcmIntentService.class.getName();
	public static final String DEVICE_UUID_KEY = "d";
	public static final String COMMAND = "command";
	private static final String MESSAGE = "message";
	private Handler handler;

	public GcmIntentService() {
		super(GcmIntentService.class.getSimpleName());
		this.handler = new Handler();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		if (!extras.isEmpty()) {
			for (String key : extras.keySet()) {
				Object value = extras.get(key);
				Log.d(TAG, String.format("%s %s (%s)", key, value.toString(), value.getClass().getName()));
			}
			defaultPushMessage(extras);
		}
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	private void defaultPushMessage(final Bundle extras) {
		try {
			final String message = extras.getString(MESSAGE, "");
			if (!message.isEmpty()) {
				sendNotification(getString(R.string.app_name), message, extras);
			}
		} catch (Exception e) {
			Log.e(TAG, "defaultPushMessage", e);
		}
	}

	private void sendNotification(String title, String message, final Bundle extras) {
		NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(this, new Random().nextInt(10000), new Intent(this, MainActivity.class).putExtras(extras), 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
				.setSmallIcon(R.mipmap.ic_launcher)
				.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
				.setContentTitle(title)
				.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
				.setContentText(message)
				.setAutoCancel(true);

		mBuilder.setContentIntent(contentIntent);
		notificationManager.notify(new Random().nextInt(10000), mBuilder.build());
	}

}