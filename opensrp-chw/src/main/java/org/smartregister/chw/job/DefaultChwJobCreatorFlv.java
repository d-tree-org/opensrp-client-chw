package org.smartregister.chw.job;

import com.evernote.android.job.Job;

import org.smartregister.chw.core.job.ChwIndicatorGeneratingJob;
import org.smartregister.chw.core.job.HomeVisitServiceJob;
import org.smartregister.chw.core.job.VaccineRecurringServiceJob;
import org.smartregister.chw.sync.ChwSyncIntentService;
import org.smartregister.job.ExtendedSyncServiceJob;
import org.smartregister.job.ImageUploadServiceJob;
import org.smartregister.job.P2pServiceJob;
import org.smartregister.job.PullUniqueIdsServiceJob;
import org.smartregister.job.SyncServiceJob;
import org.smartregister.job.SyncTaskServiceJob;
import org.smartregister.job.ValidateSyncDataServiceJob;
import org.smartregister.sync.intent.SyncTaskIntentService;

import timber.log.Timber;

public class DefaultChwJobCreatorFlv implements ChwJobCreator.Flavor {

    @Override
    public Job getJob(String tag) {
        switch (tag) {
            case SyncServiceJob.TAG:
                return new SyncServiceJob(ChwSyncIntentService.class);
            case ExtendedSyncServiceJob.TAG:
                return new ExtendedSyncServiceJob();
            case PullUniqueIdsServiceJob.TAG:
                return new PullUniqueIdsServiceJob();
            case ValidateSyncDataServiceJob.TAG:
                return new ValidateSyncDataServiceJob();
            case VaccineRecurringServiceJob.TAG:
                return new VaccineRecurringServiceJob();
            case ImageUploadServiceJob.TAG:
                return new ImageUploadServiceJob();
            case P2pServiceJob.TAG:
                return new P2pServiceJob();
            case HomeVisitServiceJob.TAG:
                return new HomeVisitServiceJob();
            case ChwIndicatorGeneratingJob.TAG:
                return new ChwIndicatorGeneratingJob();
            case BasePncCloseJob.TAG:
                return new BasePncCloseJob();
            case SyncTaskServiceJob.TAG:
                return new SyncTaskServiceJob(SyncTaskIntentService.class);
            case ScheduleJob.TAG:
                return new ScheduleJob();
            //TODO uncomment to enable plans
            /*case PlanIntentServiceJob.TAG:
                return new PlanIntentServiceJob();*/
            default:
                Timber.d("Looks like you tried to create a job " + tag + " that is not declared in the Chw Job Creator");
                return null;
        }
    }
}
