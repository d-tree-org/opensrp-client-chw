package org.smartregister.chw.sync;


import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.Context;
import org.smartregister.CoreLibrary;
import org.smartregister.sync.helper.SyncSettingsServiceHelper;
import org.smartregister.sync.intent.SettingsSyncIntentService;

import static org.smartregister.util.Log.logError;


public class AfyatekSettingsSyncIntentService extends SettingsSyncIntentService {

    private static final String TAG = AfyatekSettingsSyncIntentService.class.getCanonicalName();
    protected SyncSettingsServiceHelper syncSettingsServiceHelper;
    private SharedPreferences preferences;
    public static final String IS_SIMPRINTS_RESEARCH_ENABLED = "IS_SIMPRINTS_RESEARCH_ENABLED";

    public AfyatekSettingsSyncIntentService() {}

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("ssssssss", "In Settings Sync Intent Service...");
        if (intent != null) {
            try {
                int count = syncSettingsServiceHelper.processIntent();
                if (count > 0) {
                    intent.putExtra(AllConstants.INTENT_KEY.SYNC_TOTAL_RECORDS, count);
                    isSimprintsResearchActivated();
                }

            } catch (Exception e) {
                logError(TAG + " Error fetching client settings");
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Context context = CoreLibrary.getInstance().context();
        syncSettingsServiceHelper = new SyncSettingsServiceHelper(context.configuration().dristhiBaseURL(), context.getHttpAgent());
        preferences = getApplication().getSharedPreferences("AllSharedPreferences", MODE_PRIVATE);
    }

    private boolean isSimprintsResearchActivated() {
        boolean activate = false;

        String settingString = CoreLibrary.getInstance().context().allSettings().getSetting("global_configs").getValue();
        if (settingString != null)
            try {

                JSONObject settingsJSON = new JSONObject(settingString);
                JSONArray settings = settingsJSON.getJSONArray("settings");
                JSONObject setting = settings.getJSONObject(0);
                int value = Integer.parseInt(setting.getString("value"));
                if (value == 1) {
                    activate = true;
                }

                preferences.edit().putBoolean(IS_SIMPRINTS_RESEARCH_ENABLED, activate).commit();

            } catch (JSONException je) {
                je.printStackTrace();
            }

        return activate;
    }



}
