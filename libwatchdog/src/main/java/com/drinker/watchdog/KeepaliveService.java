package com.drinker.watchdog;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class KeepaliveService extends JobService {

    public KeepaliveService() {
    }

    @Override
    public boolean onStartJob(JobParameters params) {

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }


}
