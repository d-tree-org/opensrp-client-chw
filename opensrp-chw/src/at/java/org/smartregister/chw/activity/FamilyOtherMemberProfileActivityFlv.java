package org.smartregister.chw.activity;

import android.app.Activity;
import android.view.Menu;
import android.widget.Toast;

import org.smartregister.chw.R;
import org.smartregister.chw.core.fragment.FamilyCallDialogFragment;
import org.smartregister.chw.core.listener.OnClickFloatingMenu;
import org.smartregister.chw.dao.MalariaDao;
import org.smartregister.commonregistry.CommonPersonObjectClient;

import static org.smartregister.chw.core.utils.Utils.isWomanOfReproductiveAge;

public class FamilyOtherMemberProfileActivityFlv implements FamilyOtherMemberProfileActivity.Flavor{

    @Override
    public OnClickFloatingMenu getOnClickFloatingMenu(Activity activity, String familyBaseEntityId) {
        return viewId -> {
            switch (viewId){
                case R.id.call_layout:
                    FamilyCallDialogFragment.launchDialog(activity, familyBaseEntityId);
                    break;
                case R.id.refer_to_facility_fab:
                    Toast.makeText(activity, "Refer to Facility", Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        };
    }

    @Override
    public Boolean onCreateOptionsMenu(Menu menu, String baseEntityId) {
        if (MalariaDao.isRegisteredForMalaria(baseEntityId))
            menu.findItem(R.id.action_malaria_registration).setVisible(false);
        else
            menu.findItem(R.id.action_malaria_registration).setVisible(true);
        menu.findItem(R.id.action_malaria_followup_visit).setVisible(false);
        return true;
    }

    @Override
    public boolean isWra(CommonPersonObjectClient commonPersonObject) {
        return isWomanOfReproductiveAge(commonPersonObject, 10, 49);
    }
}
