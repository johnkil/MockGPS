package com.devspark.mockgps;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

public class PreferenceHelper {
	private static final String TAG = PreferenceHelper.class.getSimpleName();

	private static final String PREF_SPEED = "speed";
	private static final int DEFAULT_SPEED = 60;

	private final Context mContext;
	private final SharedPreferences mPrefs;

	public PreferenceHelper(Context context) {
		mContext = context;
		mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
	}

	public void saveSpeed(int speed) {
		Log.v(TAG, String.format("saveSpeed() called: speed=[%d]", speed));
		Editor mEditor = mPrefs.edit();
		mEditor.putInt(PREF_SPEED, speed);
		mEditor.commit();
	}

	public int getSpeed() {
		Log.v(TAG, "getSpeed() called");
		int speed = mPrefs.getInt(PREF_SPEED, DEFAULT_SPEED);
		return speed;
	}
}