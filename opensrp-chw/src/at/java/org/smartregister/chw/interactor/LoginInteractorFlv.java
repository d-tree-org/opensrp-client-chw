package org.smartregister.chw.interactor;

import org.smartregister.chw.BuildConfig;
import org.smartregister.chw.job.AfyatekSyncSettingsServiceJob;

import java.util.concurrent.TimeUnit;

public class LoginInteractorFlv extends DefaultLoginInteractorFlv {
    @Override
    public void getJobsToSyncPeriodically() {
        super.getJobsToSyncPeriodically();
        AfyatekSyncSettingsServiceJob.scheduleJob(AfyatekSyncSettingsServiceJob.TAG, TimeUnit.MINUTES.toMinutes(BuildConfig.DATA_SYNC_DURATION_MINUTES), getFlexValue(
                BuildConfig.DATA_SYNC_DURATION_MINUTES
        ));
    }

    @Override
    public void getJobsToSyncImmediately() {
        super.getJobsToSyncImmediately();
        AfyatekSyncSettingsServiceJob.scheduleJobImmediately(AfyatekSyncSettingsServiceJob.TAG);
    }
}
