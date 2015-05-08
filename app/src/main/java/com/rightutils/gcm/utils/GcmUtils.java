package com.rightutils.gcm.utils;

import android.app.Activity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Created by Anton Maniskevich on 5/8/15.
 */
public class GcmUtils {

	private static final String TAG = GcmUtils.class.getSimpleName();

	public static boolean checkPlayServices(Activity activity) {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, activity, 9000).show();
			} else {
				Log.i(TAG, "This device is not supported.");
				activity.finish();
			}
			return false;
		}
		return true;
	}
}
