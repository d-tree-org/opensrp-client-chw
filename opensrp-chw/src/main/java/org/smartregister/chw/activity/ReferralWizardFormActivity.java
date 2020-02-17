package org.smartregister.chw.activity;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.smartregister.chw.fragment.ReferralJsonWizardFormFragment;
import org.smartregister.family.activity.FamilyWizardFormActivity;

public class ReferralWizardFormActivity extends FamilyWizardFormActivity {

    @Override
    public void initializeFormFragment() {
        ReferralJsonWizardFormFragment jsonWizardFormFragment = ReferralJsonWizardFormFragment.getFormFragment(JsonFormConstants.FIRST_STEP_NAME);

        getSupportFragmentManager().beginTransaction()
                .add(com.vijay.jsonwizard.R.id.container, jsonWizardFormFragment).commit();
    }
}
