package com.devspark.mockgps;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/**
 * 
 * @author johnkil
 *
 */
public class MockGpsActivity extends Activity {
	private static final String LOG_TAG = MockGpsActivity.class.getSimpleName();
	
	private Button startBtn;
	private Button stopBtn;
	private TextView speedTxt;
	private SeekBar seekBar;
	private ViewGroup manual;
	
	private PreferenceHelper preferenceHelper;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(LOG_TAG, "onCreate() called");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		startBtn 	= (Button) findViewById(R.id.start_btn);
		stopBtn		= (Button) findViewById(R.id.stop_btn);
		speedTxt	= (TextView) findViewById(R.id.speed_txt);
		seekBar		= (SeekBar) findViewById(R.id.seek_bar);
		manual 		= (ViewGroup) findViewById(R.id.manual);
		
		if (isMyServiceRunning()) {
			stopBtn.setVisibility(View.VISIBLE);
		} else {
			startBtn.setVisibility(View.VISIBLE);
		}
		
		preferenceHelper = new PreferenceHelper(this);
		seekBar.setProgress(preferenceHelper.getSpeed());
		speedTxt.setText(String.format(getString(R.string.speed_format), preferenceHelper.getSpeed()));
		
		seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
	}
	
	@Override
	protected void onStart() {
		Log.v(LOG_TAG, "");
		super.onStart();
		String providers = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		Log.i(LOG_TAG, "LOCATION_PROVIDERS_ALLOWED: " + providers);
		if (providers.contains(LocationManager.GPS_PROVIDER) || 
				Settings.Secure.getString(getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION).equals("0")) {
			startBtn.setEnabled(false);
			manual.setVisibility(View.VISIBLE);
		} else {
			startBtn.setEnabled(true);
			manual.setVisibility(View.GONE);
		}
	}
	
	public void startService(View v) {
		Intent mIntent = new Intent(getBaseContext(), MockGpsService.class);
		startService(mIntent);
		startBtn.setVisibility(View.GONE);
		stopBtn.setVisibility(View.VISIBLE);
	}
	
	public void stopService(View v) {
		Intent mIntent = new Intent(getBaseContext(), MockGpsService.class);
		stopService(mIntent);
		stopBtn.setVisibility(View.GONE);
		startBtn.setVisibility(View.VISIBLE);
	}

	private boolean isMyServiceRunning() {
	    ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	    for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if ("com.devspark.mockgps.MockGpsService".equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
	
	private OnSeekBarChangeListener onSeekBarChangeListener = new OnSeekBarChangeListener() {
		
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			preferenceHelper.saveSpeed(seekBar.getProgress());
		}
		
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {}
		
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			speedTxt.setText(String.format(getString(R.string.speed_format), progress));
		}
	};
}