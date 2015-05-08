package com.rightutils.gcm.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import com.rightutils.gcm.R;
import com.rightutils.gcm.services.gcm.RegGCMReceiver;
import com.rightutils.gcm.utils.GcmUtils;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		GcmUtils.checkPlayServices(MainActivity.this);
		startGCM();
	}

	private void startGCM() {
		Intent alarmIntent = new Intent(MainActivity.this, RegGCMReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, alarmIntent, 0);
		AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		manager.cancel(pendingIntent);
		manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), RegGCMReceiver.REG_GCM_INTERVAL, pendingIntent);
	}
}
