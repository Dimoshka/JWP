package com.dimoshka.ua.classes;

import android.app.Application;
import com.dimoshka.ua.jwp.BuildConfig;
import com.dimoshka.ua.jwp.R;
import org.acra.ACRA;
import org.acra.ErrorReporter;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

//@ReportsCrashes(formKey = "", // will not be used
//mailTo = "dimoshka.ua@gmail.com", mode = ReportingInteractionMode.TOAST, resToastText = R.string.send_bug_report)
//formUri = "http://dimoshka.pp.ua/acra/" + BuildConfig.DEBUG,

@ReportsCrashes(
        formKey = "",
        formUri = "http://dimoshka.pp.ua/acra/crash.php?debug=" + BuildConfig.DEBUG,
        //reportType = org.acra.sender.HttpSender.Type.JSON,
        //httpMethod = org.acra.sender.HttpSender.Method.PUT,
        mode = ReportingInteractionMode.TOAST,
        //reportType = org.acra.sender.HttpSender.Type.JSON,
        resToastText = R.string.send_bug_report)

public class class_myapplication extends Application {
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate() {
        ACRA.init(this);
        ErrorReporter.getInstance().checkReportsOnApplicationStart();
        super.onCreate();
    }
}