package org.smartregister.chw.job;



import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

public class ChwJobCreator implements JobCreator {

    private ChwJobCreator.Flavor flavor;

    public ChwJobCreator() {
        this.flavor = new ChwJobCreatorFlv();
    }

    public Flavor getFlavor() {
        return flavor;
    }

    public void setFlavor(Flavor flavor) {
        this.flavor = flavor;
    }

    @Nullable
    @Override
    public Job create(@NonNull String tag) {
        return this.flavor.getJob(tag);
    }

    public interface Flavor {
        Job getJob(String tag);

    }
}
