package org.smartregister.chw.application;

import org.smartregister.CoreLibrary;

public class DefaultChwSyncConfigurationFlv implements ChwSyncConfiguration.Flavor{
    @Override
    public String getSyncFilterValueForSettings() {
        String providerId = CoreLibrary.getInstance().context().allSharedPreferences().fetchRegisteredANM();
        String locationId = CoreLibrary.getInstance().context().allSharedPreferences().fetchUserLocalityId(providerId);
        return locationId;
    }
}
