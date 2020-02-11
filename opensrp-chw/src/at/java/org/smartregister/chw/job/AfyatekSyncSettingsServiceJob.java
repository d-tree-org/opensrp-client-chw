package org.smartregister.chw.job;

import android.content.Intent;

import androidx.annotation.NonNull;

import org.smartregister.AllConstants;
import org.smartregister.chw.sync.AfyatekSettingsSyncIntentService;
import org.smartregister.job.SyncSettingsServiceJob;

public class AfyatekSyncSettingsServiceJob extends SyncSettingsServiceJob {

    public static final String TAG = "AfyatekSyncSettingsServiceJob";

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        Intent intent = new Intent(getApplicationContext(), AfyatekSettingsSyncIntentService.class);
        getApplicationContext().startService(intent);
        return params != null && params.getExtras().getBoolean(AllConstants.INTENT_KEY.TO_RESCHEDULE, false) ? Result.RESCHEDULE : Result.SUCCESS;
    }
}
