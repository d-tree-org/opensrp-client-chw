package org.smartregister.chw.interactor;

import org.smartregister.chw.BuildConfig;
import org.smartregister.chw.core.job.ChwIndicatorGeneratingJob;
import org.smartregister.chw.core.job.HomeVisitServiceJob;
import org.smartregister.chw.core.job.VaccineRecurringServiceJob;
import org.smartregister.chw.job.AfyatekSyncSettingsServiceJob;
import org.smartregister.chw.job.BasePncCloseJob;
import org.smartregister.chw.job.ScheduleJob;
import org.smartregister.immunization.job.VaccineServiceJob;
import org.smartregister.job.DistrictFacilitiesServiceJob;
import org.smartregister.job.ImageUploadServiceJob;
import org.smartregister.job.PlanIntentServiceJob;
import org.smartregister.job.PullUniqueIdsServiceJob;
import org.smartregister.job.SyncServiceJob;
import org.smartregister.job.SyncSettingsServiceJob;
import org.smartregister.job.SyncTaskServiceJob;
import org.smartregister.login.interactor.BaseLoginInteractor;
import org.smartregister.view.contract.BaseLoginContract;

import java.util.concurrent.TimeUnit;


public class LoginInteractor extends BaseLoginInteractor implements BaseLoginContract.Interactor {

    private LoginInteractor.Flavor flavor;
    public LoginInteractor(BaseLoginContract.Presenter loginPresenter) {

        super(loginPresenter);
        this.flavor = new LoginInteractorFlv();
    }

    @Override
    protected void scheduleJobsPeriodically() {
        this.flavor.getJobsToSyncPeriodically();
    }

    @Override
    protected void scheduleJobsImmediately() {
        super.scheduleJobsImmediately();
        // Run initial job immediately on log in since the job will run a bit later (~ 15 mins +)
        this.flavor.getJobsToSyncImmediately();
    }

    public interface Flavor {
        void getJobsToSyncPeriodically();
        void getJobsToSyncImmediately();
        long getFlexValue(int value);
    }
}
