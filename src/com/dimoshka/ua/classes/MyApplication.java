package com.dimoshka.ua.classes;

import org.acra.*;
import org.acra.annotation.*;

import android.app.Application;

import com.dimoshka.ua.jwp.R;

@ReportsCrashes(formKey = "", // will not be used
mailTo = "dimoshka.ua@gmail.com", mode = ReportingInteractionMode.TOAST, resToastText = R.string.send_bug_report)
public class MyApplication extends Application {
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate() {
		ACRA.init(this);
		ErrorReporter.getInstance().checkReportsOnApplicationStart();
		super.onCreate();
	}
}