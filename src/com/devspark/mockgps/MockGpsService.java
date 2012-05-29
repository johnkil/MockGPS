package com.devspark.mockgps;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;

/**
 * 
 * @author johnkil
 *
 */
public class MockGpsService extends Service {
	private static final String LOG_TAG = MockGpsService.class.getSimpleName();
	
	@SuppressWarnings("rawtypes")
	private static final Class[] mStartForegroundSignature = new Class[] {
        int.class, Notification.class};
    @SuppressWarnings("rawtypes")
	private static final Class[] mStopForegroundSignature = new Class[] {
        boolean.class};
    
    private static final int NOTIFICATION_ID = R.string.notification;
    
    private NotificationManager mNM;
    private Method mStartForeground;
    private Method mStopForeground;
    private Object[] mStartForegroundArgs = new Object[2];
    private Object[] mStopForegroundArgs = new Object[1];
    
    private MockGpsProvider mMockGpsProvider;

    @Override
	public void onCreate() {
		Log.v(LOG_TAG, "onCreate() called");
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        try {
            mStartForeground = getClass().getMethod("startForeground", mStartForegroundSignature);
            mStopForeground = getClass().getMethod("stopForeground", mStopForegroundSignature);
        } catch (NoSuchMethodException e) {
            // Running on an older platform.
            mStartForeground = mStopForeground = null;
        }
        startMockLocation();
        startForegroundCompat(NOTIFICATION_ID, getNotification(getString(R.string.notification)));
	}
    
    // This is the old onStart method that will be called on the pre-2.0
    // platform.  On 2.0 or later we override onStartCommand() so this
    // method will not be called.
    @Override
    public void onStart(Intent intent, int startId) {
    	Log.v(LOG_TAG, "onStart() called");
        handleCommand(intent);
    }
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(LOG_TAG, "onStartCommand() called");
		if (intent != null) {
			handleCommand(intent);
		}
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }
	
	private void handleCommand(Intent intent) {
		String action = intent.getAction();
		Log.v(LOG_TAG, String.format("handleCommand() called: action=[%s]", action));
    }
	
	private Notification getNotification(String contentText) {
		Log.v(LOG_TAG, "getNotification() called");
		// In this sample, we'll use the same text for the ticker and the expanded notification
		/*CharSequence text = getText(R.string.notification);*/
		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.ic_stat_mock_gps, contentText, 0);
		// The PendingIntent to launch our activity if the user selects this notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MockGpsActivity.class), 0);
		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this, getText(R.string.app_name), contentText, contentIntent);
		
		return notification;
	}
	
	/**
     * This is a wrapper around the new startForeground method, using the older
     * APIs if it is not available.
     */
    private void startForegroundCompat(int id, Notification notification) {
    	Log.v(LOG_TAG, "startForegroundCompat() called");
        // If we have the new startForeground API, then use it.
        if (mStartForeground != null) {
            mStartForegroundArgs[0] = Integer.valueOf(id);
            mStartForegroundArgs[1] = notification;
            try {
                mStartForeground.invoke(this, mStartForegroundArgs);
            } catch (InvocationTargetException e) {
                // Should not happen.
                Log.w(LOG_TAG, "Unable to invoke startForeground", e);
            } catch (IllegalAccessException e) {
                // Should not happen.
                Log.w(LOG_TAG, "Unable to invoke startForeground", e);
            }
            return;
        }
        /*
        // Fall back on the old API.
        setForeground(true);
        mNM.notify(id, notification);
        */
    }
    
    /**
     * This is a wrapper around the new stopForeground method, using the older
     * APIs if it is not available.
     */
    private void stopForegroundCompat(int id) {
    	Log.v(LOG_TAG, "stopForegroundCompat() called");
        // If we have the new stopForeground API, then use it.
        if (mStopForeground != null) {
            mStopForegroundArgs[0] = Boolean.TRUE;
            try {
                mStopForeground.invoke(this, mStopForegroundArgs);
            } catch (InvocationTargetException e) {
                // Should not happen.
                Log.w(LOG_TAG, "Unable to invoke stopForeground", e);
            } catch (IllegalAccessException e) {
                // Should not happen.
                Log.w(LOG_TAG, "Unable to invoke stopForeground", e);
            }
            return;
        }
        /*
        // Fall back on the old API.  Note to cancel BEFORE changing the
        // foreground state, since we could be killed at that point.
        mNM.cancel(id);
        setForeground(false);
        */
    }
    
    private boolean startMockLocation() {
    	Log.v(LOG_TAG, "startMockLocation() called");
    	LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    	mMockGpsProvider = new MockGpsProvider(MockGpsService.this);
    	if (!mLocationManager.isProviderEnabled(MockGpsProvider.GPS_MOCK_PROVIDER)) {
        	// otherwise enable the mock GPS provider
    		mLocationManager.addTestProvider(MockGpsProvider.GPS_MOCK_PROVIDER, false, false,
        			false, false, true, false, false, 0, 5);
    		mLocationManager.setTestProviderEnabled(MockGpsProvider.GPS_MOCK_PROVIDER, true);
        }  
        
        if (!mLocationManager.isProviderEnabled(MockGpsProvider.GPS_MOCK_PROVIDER)) {
        	return false;
        }
        try {
    		mMockGpsProvider.requestMockLocationUpdates();
    	} catch (IOException e) {
			Log.w(LOG_TAG, e);
			return false;
		}
    	return true;
    }
    
    private void stopMockLocation() {
    	Log.v(LOG_TAG, "stopMockLocation() called");
    	LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    	mMockGpsProvider.removeMockLocationUpdates();
		try {
			mLocationManager.removeTestProvider(MockGpsProvider.GPS_MOCK_PROVIDER);
		} catch (Exception e){
			Log.w(LOG_TAG, e);
		}
    }
	
	@Override
	public void onDestroy() {
		Log.v(LOG_TAG, "onDestroy() called");
		stopMockLocation();
		// Make sure our notification is gone.
        stopForegroundCompat(NOTIFICATION_ID);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	

}
