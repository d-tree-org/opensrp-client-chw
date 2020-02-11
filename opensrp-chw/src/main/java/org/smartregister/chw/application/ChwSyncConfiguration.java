package org.smartregister.chw.application;

import org.smartregister.SyncConfiguration;
import org.smartregister.SyncFilter;
import org.smartregister.chw.BuildConfig;
import org.smartregister.chw.core.utils.Utils;

/**
 * Created by samuelgithengi on 10/19/18.
 */
public class ChwSyncConfiguration extends SyncConfiguration {

    private static ChwSyncConfiguration.Flavor flavor = new ChwSyncConfigurationFlv();

    @Override
    public int getSyncMaxRetries() {
        return BuildConfig.MAX_SYNC_RETRIES;
    }

    @Override
    public SyncFilter getSyncFilterParam() {
        return SyncFilter.LOCATION;
    }

    @Override
    public String getSyncFilterValue() {

        return Utils.getSyncFilterValue();
    }

    @Override
    public int getUniqueIdSource() {
        return BuildConfig.OPENMRS_UNIQUE_ID_SOURCE;
    }

    @Override
    public int getUniqueIdBatchSize() {
        return BuildConfig.OPENMRS_UNIQUE_ID_BATCH_SIZE;
    }

    @Override
    public int getUniqueIdInitialBatchSize() {
        return BuildConfig.OPENMRS_UNIQUE_ID_INITIAL_BATCH_SIZE;
    }

    @Override
    public boolean isSyncSettings() {
        return BuildConfig.IS_SYNC_SETTINGS;
    }

    @Override
    public SyncFilter getEncryptionParam() {
        return SyncFilter.LOCATION;
    }

    @Override
    public boolean updateClientDetailsTable() {
        return false;
    }

    @Override
    public String getSettingsSyncFilterValue() {
        return this.flavor.getSyncFilterValueForSettings();
    }

    public boolean isSyncUsingPost() {
        return !BuildConfig.DEBUG;
    }

    public interface Flavor {
        String getSyncFilterValueForSettings();
    }
}
