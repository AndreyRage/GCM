package com.rightutils.gcm.services.gcm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.rightutils.rightutils.utils.RightUtils;
import java.io.IOException;

/**
 * Created by Anton Maniskevich on 5/7/15.
 */
public class RegGCMReceiver extends BroadcastReceiver {

	private static final String TAG = RegGCMReceiver.class.getSimpleName();
	public static final String PROPERTY_REG_ID = "registration_id";
	public static final String PROPERTY_APP_VERSION = "appVersion";
	public static final int REG_GCM_INTERVAL = 20 * 1000;
	private GoogleCloudMessaging gcm;
	//NB your id from google api console
	public static final String SENDER_ID = "559174343034";

	@Override
	public void onReceive(final Context context, Intent intent) {
		Log.i(TAG, "REG ID receiver running = " + RightUtils.isOnline(context));
		if (RightUtils.isOnline(context)) {
			if (checkPlayServices(context)) {
				gcm = GoogleCloudMessaging.getInstance(context);
				final String redId = getRegistrationId(context);
				if (redId.isEmpty()) {
					registerInBackground(context);
				} else {
					new AsyncTask<Void, Void, Void>() {
						@Override
						protected Void doInBackground(Void... params) {
							sendRegistrationIdToBackend(context, redId);
							return null;
						}
						@Override
						protected void onPostExecute(Void param) {
						}
					}.execute();
				}
			} else {
				Log.i(TAG, "No valid Google Play Services APK found.");
			}
		}
	}

	private boolean checkPlayServices(Context context) {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (!GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				Log.i(TAG, "This device is not supported.");
				cancelLoop(context);
			}
			return false;
		}
		return true;
	}

	private String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGCMPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId == null || registrationId.isEmpty()) {
			Log.i(TAG, "Registration not found.");
			return "";
		}
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Log.i(TAG, "App version changed.");
			return "";
		}
		return registrationId;
	}

	private SharedPreferences getGCMPreferences(Context context) {
		return context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
	}

	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (Exception e) {
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	private void registerInBackground(final Context context) {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg;
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(context);
					}
					final String pushId = gcm.register(SENDER_ID);
					msg = "Device registered, registration ID=" + pushId;
					sendRegistrationIdToBackend(context, pushId);
					storeRegistrationId(context, pushId);
				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
				}
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				Log.i(TAG, msg);
			}
		}.execute();
	}

	private void storeRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGCMPreferences(context);
		int appVersion = getAppVersion(context);
		Log.i(TAG, "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.apply();
	}

	private void cancelLoop(Context context) {
		Intent alarmIntent = new Intent(context, RegGCMReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
		AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		manager.cancel(pendingIntent);
	}

	private void sendRegistrationIdToBackend(Context context, String redId) {
		//TODO send regid to server
		Log.i(TAG, redId);
		cancelLoop(context);
	}

}
