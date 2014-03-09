package com.phinmadvader.andcpp;

import org.acra.*;
import org.acra.annotation.*;

import android.app.Application;


@ReportsCrashes(
    formKey = "", // This is required for backward compatibility but not used
    formUri = "http://madhavan93.com/andcpp_crash/report.php"
)
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // The following line triggers the initialization of ACRA
        ACRA.init(this);
    }
}
