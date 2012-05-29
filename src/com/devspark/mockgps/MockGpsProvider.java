package com.devspark.mockgps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.xml.sax.SAXException;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;

/**
 * 
 * @author johnkil
 *
 */
public class MockGpsProvider {
	private static final String TAG = MockGpsProvider.class.getSimpleName();
	
	public static final String GPS_MOCK_PROVIDER = LocationManager.GPS_PROVIDER;
	
	private static final String KML_GPS_MOCK_FILE_NAME = "mock_gps_data.kml";
	private static final String CSV_GPS_MOCK_FILE_NAME = "mock_gps_data.csv";
	private static final String PATH_CACHE_FOLDER = Environment.getExternalStorageDirectory() + "/Android/data/com.devspark.mockgps/cache/";
	
	private final Context mContext;
	private final PreferenceHelper preferenceHelper;
	private MockGpsProviderTask mMockGpsProviderTask;
	
	public MockGpsProvider(Context context) {
		mContext = context;
		preferenceHelper = new PreferenceHelper(mContext);
	}
	
	/**
	 * Load mock GPS data from file and create mock GPS provider.
	 * @throws IOException
	 */
	public void requestMockLocationUpdates() throws IOException {
		Log.v(TAG, "requestMockLocationUpdates() called");
		
		try {
			//List<String> data = getCoordinatesFromKml();
			List<String> data = getCoordinatesFromCsv();
			// convert to a simple array so we can pass it to the AsyncTask
			String[] coordinates = new String[data.size()];
			data.toArray(coordinates);
			
			// create new AsyncTask and pass the list of GPS coordinates
			mMockGpsProviderTask = new MockGpsProviderTask();
			mMockGpsProviderTask.execute(coordinates);
		} catch (Exception e) {
			Log.w(TAG, e);
		}
	}
	
	public void removeMockLocationUpdates() {
		Log.v(TAG, "removeMockLocationUpdates() called");
		// stop the mock GPS provider by calling the 'cancel(true)' method
    	try {
    		mMockGpsProviderTask.cancel(true);
    		mMockGpsProviderTask = null;
    	} catch (Exception e) {
    		Log.w(TAG, e);
    	}
	}
	
	private List<String> getCoordinatesFromKml() throws IOException, SAXException {
		Log.v(TAG, "getCoordinatesFromKml() called");
		
		File mockGpsFile = new File(PATH_CACHE_FOLDER + KML_GPS_MOCK_FILE_NAME);
		if (!mockGpsFile.exists()) {
			createGpsMockDataFile(KML_GPS_MOCK_FILE_NAME);
		}
		InputStream is = new FileInputStream(mockGpsFile);
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		KmlHandler kmlHandler = new KmlHandler();
		Xml.parse(reader, kmlHandler);
		return kmlHandler.getCoordinates();
	}
	
	private List<String> getCoordinatesFromCsv() throws IOException, SAXException {
		Log.v(TAG, "getCoordinatesFromCsv() called");
		
		File mockGpsFile = new File(PATH_CACHE_FOLDER + CSV_GPS_MOCK_FILE_NAME);
		if (!mockGpsFile.exists()) {
			createGpsMockDataFile(CSV_GPS_MOCK_FILE_NAME);
		}
		InputStream is = new FileInputStream(mockGpsFile);
		Scanner scanner = new Scanner(is);
		List<String> coordinates = new ArrayList<String>();
		while (scanner.hasNextLine()) {
			coordinates.add(scanner.nextLine());
		}
		scanner.close();
		is.close();
		return coordinates;
	}
	
	private void createGpsMockDataFile(String gpsMockFileName) throws IOException {
		Log.v(TAG, "createGpsMockDataFile() called");
		
		File rootFolder = new File(PATH_CACHE_FOLDER);
		if (!rootFolder.exists()) {
			rootFolder.mkdirs();
		}
		File gpsMockDataFile = new File(PATH_CACHE_FOLDER + gpsMockFileName);
		gpsMockDataFile.createNewFile();
		
		InputStream mInputStream = mContext.getAssets().open(gpsMockFileName);
		OutputStream mOutputStream = new FileOutputStream(gpsMockDataFile);
		byte[] buffer = new byte[1024];
		int length = 0;
		while( (length = mInputStream.read(buffer)) > 0){
			mOutputStream.write(buffer, 0, length);
		}
		mOutputStream.flush();
		mOutputStream.close();
		mInputStream.close();
	}

	private class MockGpsProviderTask extends AsyncTask<String, Integer, Void> {
		
		// private final double step = 0.0005;
		private final long timeInterval = 1000; // ms
		
		@Override
		protected void onCancelled() {
			Log.v(TAG, "onCancelled() called");
			super.onCancelled();
		}
	
		@Override
		protected Void doInBackground(String... data) {
			Log.v(TAG, "doInBackground() called");
			double lat_1, lng_1;
			double lat_2, lng_2;
			double traveledDist = 0;
			// process data
			for (int i = 1; i < data.length; i++) {
				String[] parts;
				// initialization start point
				parts = data[i-1].split(",");
				lat_1 = Double.valueOf(parts[1]);
				lng_1 = Double.valueOf(parts[0]);
				// initialization end point
				parts = data[i].split(",");
				lat_2 = Double.valueOf(parts[1]);
				lng_2 = Double.valueOf(parts[0]);
				double bigStep = Math.sqrt((lat_2 - lat_1)*(lat_2 - lat_1) + (lng_2 - lng_1)*(lng_2 - lng_1));
				Log.i(TAG, String.format("Setup segment: start=[%f; %f], end=[%f; %f], bigStep=[%f], traveledDist=[%f]", lat_1, lng_1, lat_2, lng_2, bigStep, traveledDist));
				do {
					// calculation of the coordinates
					double lat = traveledDist * (lat_2 - lat_1) / bigStep + lat_1;
					double lng = traveledDist * (lng_2 - lng_1) / bigStep + lng_1;
					setupLocation(lat, lng);
					// sleep for a while before providing next location
					try {
						Thread.sleep(timeInterval);
						// gracefully handle Thread interruption (important!)
						//if(Thread.currentThread().isInterrupted())  *old realization
						if(isCancelled())
							throw new InterruptedException("");
					} catch (InterruptedException e) {
						return null;
					}
					traveledDist += getStep();
					Log.d(TAG, String.format("traveledDist=[%f]", traveledDist));
				} while (traveledDist < bigStep);
				traveledDist = traveledDist - bigStep;
			}
	
			return null;
		}
		
		private void setupLocation(double lat, double lng) {
			Log.v(TAG, String.format("setupLocation() called: lat=[%f] lng=[%f]", lat, lng));
			// translate to actual GPS location
			Location location = new Location(GPS_MOCK_PROVIDER);
			location.setLatitude(lat);
			location.setLongitude(lng);
			// location.setAltitude(altitude);
			location.setTime(System.currentTimeMillis());

			// show debug message in log
			Log.d(TAG, location.toString());

			// provide the new location
			LocationManager mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
			mLocationManager.setTestProviderLocation(GPS_MOCK_PROVIDER, location);
		}
		
		private double getStep() {
			return ((double) preferenceHelper.getSpeed())/100000;
		}
		
	}
	
}
