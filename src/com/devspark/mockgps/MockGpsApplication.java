package com.devspark.mockgps;

import org.acra.ACRA;
import org.acra.ErrorReporter;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

@ReportsCrashes(formUri = "http://www.bugsense.com/api/acra?api_key=d907d9c6", formKey="")
public class MockGpsApplication extends Application {
	
	@Override
	public void onCreate() {
		
		// The following line triggers the initialization of ACRA
		ACRA.init(this);
        ErrorReporter.getInstance().checkReportsOnApplicationStart();
        
        super.onCreate();
	}

}
