package org.smartregister.chw.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import net.sqlcipher.database.SQLiteDatabase;

import org.joda.time.DateTime;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.chw.R;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.contract.ChildProfileContract;
import org.smartregister.chw.core.activity.CoreChildProfileActivity;
import org.smartregister.chw.core.activity.CoreUpcomingServicesActivity;
import org.smartregister.chw.core.listener.OnClickFloatingMenu;
import org.smartregister.chw.core.model.CoreChildProfileModel;
import org.smartregister.chw.core.presenter.CoreChildProfilePresenter;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.custom_view.FamilyMemberFloatingMenu;
import org.smartregister.chw.dao.MalariaDao;
import org.smartregister.chw.model.ReferralTypeModel;
import org.smartregister.chw.presenter.ChildProfilePresenter;
import org.smartregister.chw.schedulers.ChwScheduleTaskExecutor;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.domain.Task;
import org.smartregister.domain.db.Client;
import org.smartregister.family.util.Constants;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.ClientRepository;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.simprint.SimPrintsConstantHelper;
import org.smartregister.simprint.SimPrintsHelper;
import org.smartregister.simprint.SimPrintsHelperResearch;
import org.smartregister.simprint.SimPrintsIdentifyActivity;
import org.smartregister.simprint.SimPrintsRegisterActivity;
import org.smartregister.simprint.SimPrintsRegistration;
import org.smartregister.simprint.SimPrintsVerification;
import org.smartregister.simprint.SimPrintsVerifyActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.smartregister.AllConstants.ROWID;
import static org.smartregister.chw.anc.util.Constants.ANC_MEMBER_OBJECTS.MEMBER_PROFILE_OBJECT;

public class ChildProfileActivity extends CoreChildProfileActivity implements ChildProfileContract.View {
    public FamilyMemberFloatingMenu familyFloatingMenu;
    private Flavor flavor = new ChildProfileActivityFlv();
    private List<ReferralTypeModel> referralTypeModels = new ArrayList<>();

    private static final int VERIFY_RESULT_CODE = 8379;
    private static final int REGISTER_RESULT_CODE = 5361;

    Client currentClient;

    public List<ReferralTypeModel> getReferralTypeModels() {
        return referralTypeModels;
    }

    private TextView verifyChildFingerprint;

    @Override
    protected void onCreation() {
        super.onCreation();
        initializePresenter();
        onClickFloatingMenu = flavor.getOnClickFloatingMenu(this, (ChildProfilePresenter) presenter);
        setupViews();
        setUpToolbar();
        registerReceiver(mDateTimeChangedReceiver, sIntentFilter);
        if (((ChwApplication) ChwApplication.getInstance()).hasReferrals()) {
            addChildReferralTypes();
        }
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        int i = view.getId();
        if (i == R.id.last_visit_row) {
            openMedicalHistoryScreen();
        } else if (i == R.id.most_due_overdue_row) {
            openUpcomingServicePage();
        } else if (i == R.id.textview_record_visit || i == R.id.record_visit_done_bar) {
            openVisitHomeScreen(false);
        } else if (i == R.id.family_has_row) {
            openFamilyDueTab();
        } else if (i == R.id.textview_edit) {
            openVisitHomeScreen(true);
        }
        if (i == R.id.textview_visit_not) {
            presenter().updateVisitNotDone(System.currentTimeMillis());
            imageViewCrossChild.setVisibility(View.VISIBLE);
            imageViewCrossChild.setImageResource(R.drawable.activityrow_notvisited);
        } else if (i == R.id.textview_undo) {
            presenter().updateVisitNotDone(0);
        } else if(i == R.id.referral_row) {
            Task task = (Task) view.getTag();
            ReferralFollowupActivity.startReferralFollowupActivity(this, task.getIdentifier());
        }else if (i == R.id.textview_verify_fingerprint){
            //Call out verification
            org.smartregister.chw.presenter.ChildProfilePresenter presenter = (org.smartregister.chw.presenter.ChildProfilePresenter) presenter();
            presenter.verifyChildFingerprint();
        }
    }

    @Override
    protected void initializePresenter() {
        String familyName = getIntent().getStringExtra(Constants.INTENT_KEY.FAMILY_NAME);
        if (familyName == null) {
            familyName = "";
        }

        presenter = new ChildProfilePresenter(this, new CoreChildProfileModel(familyName), childBaseEntityId);
    }

    @Override
    protected void setupViews() {
        super.setupViews();
        verifyChildFingerprint = findViewById(R.id.textview_verify_fingerprint);
        verifyChildFingerprint.setOnClickListener(this);
        fetchProfileData();
    }

    @Override
    public void updateHasPhone(boolean hasPhone) {
        if (familyFloatingMenu != null) {
            familyFloatingMenu.reDraw(hasPhone);
        }
    }

    @Override
    public void setVisitAboveTwentyFourView() {
        super.setVisitAboveTwentyFourView();
        findViewById(R.id.textview_visit_not).setVisibility(View.GONE);
        findViewById(R.id.textview_record_visit).setBackgroundResource(R.drawable.btn_drawable_primary);
        TextView tv = (TextView)findViewById(R.id.textview_record_visit);
        tv.setTextColor(this.getResources().getColor(R.color.light_grey_text));
    }

    @Override
    public void callFingerprintRegister(Client client) {

        currentClient = client;

        try {
            DateTime birthdate = client.getBirthdate();
            SimpleDateFormat dateFormatForRiddler = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat dateFormatFromNativeForms = new SimpleDateFormat("dd-MM-yyyy");
            String formatedDate = "";
            formatedDate = dateFormatForRiddler.format(birthdate.toDate());

            JSONObject metadata = new JSONObject();
            metadata.put("DOB", formatedDate);

            String moduleId = CoreLibrary.getInstance().context().allSharedPreferences().fetchUserLocalityName("");
            if (moduleId == null || moduleId.isEmpty()){
                moduleId = "global_module";
            }

            SimPrintsRegisterActivity.startSimprintsRegisterActivity(this,
                    moduleId, REGISTER_RESULT_CODE, metadata);

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void callFingerprintVerification(String fingerprintId) {
        String moduleId = CoreLibrary.getInstance().context().allSharedPreferences().fetchUserLocalityName("");
        if (moduleId == null || moduleId.isEmpty()){
            moduleId = "global_module";
        }
        SimPrintsVerifyActivity.startSimprintsVerifyActivity(this,
                moduleId, fingerprintId, VERIFY_RESULT_CODE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_malaria_registration:
                MalariaRegisterActivity.startMalariaRegistrationActivity(ChildProfileActivity.this,
                        ((CoreChildProfilePresenter) presenter()).getChildClient().getCaseId());
                return true;
            case R.id.action_remove_member:
                IndividualProfileRemoveActivity.startIndividualProfileActivity(ChildProfileActivity.this, ((ChildProfilePresenter) presenter()).getChildClient(),
                        ((ChildProfilePresenter) presenter()).getFamilyID()
                        , ((ChildProfilePresenter) presenter()).getFamilyHeadID(), ((ChildProfilePresenter) presenter()).getPrimaryCareGiverID(), ChildRegisterActivity.class.getCanonicalName());

                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.action_sick_child_form).setVisible(ChwApplication.getApplicationFlavor().hasChildSickForm());
        menu.findItem(R.id.action_sick_child_follow_up).setVisible(false);
        menu.findItem(R.id.action_malaria_diagnosis).setVisible(false);
        menu.findItem(R.id.action_malaria_followup_visit).setVisible(false);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CoreConstants.ProfileActivityResults.CHANGE_COMPLETED && resultCode == Activity.RESULT_OK) {
            Intent intent = new Intent(ChildProfileActivity.this, ChildProfileActivity.class);
            intent.putExtras(getIntent().getExtras());
            startActivity(intent);
            finish();
        }else if (requestCode == VERIFY_RESULT_CODE && resultCode == Activity.RESULT_OK){

            if (data.getExtras() != null){
                SimPrintsVerification simprintsVerification = (SimPrintsVerification) data.getSerializableExtra(SimPrintsConstantHelper.INTENT_DATA);
                switch (simprintsVerification.getMaskedTier()){
                    case TIER_3:
                        showSnackBar("Fingerprint Matched");
                        break;
                    case TIER_2:
                        showSnackBar("Fingerprint Matched");
                        break;
                    case TIER_1:
                        showSnackBar("Fingerprint Matched");
                        break;
                    default:
                        showSnackBar("Fingerprint did not match!");
                }
            }
        }else if(requestCode == REGISTER_RESULT_CODE && resultCode == RESULT_OK){
            if (data.getExtras() != null){

                SimPrintsRegistration simPrintsRegistration = (SimPrintsRegistration) data.getSerializableExtra(SimPrintsConstantHelper.INTENT_DATA);
                String guid = simPrintsRegistration.getGuid();

                Map<String, String> identifier = new HashMap<>();
                identifier.put("simprints_guid", guid);

                currentClient.setIdentifiers(identifier);

                JSONObject object = CoreLibrary.getInstance().context().getEventClientRepository().convertToJson(currentClient);

                CoreLibrary.getInstance().context().getEventClientRepository().addorUpdateClient(currentClient.getBaseEntityId(), object);

                showSnackBar("Child's Fingerprint enrolled successfully");

            }
        }
        ChwScheduleTaskExecutor.getInstance().execute(memberObject.getBaseEntityId(), CoreConstants.EventType.CHILD_HOME_VISIT, new Date());
    }

    private void showSnackBar(String message){
        View parentLayout = findViewById(android.R.id.content);
        Snackbar.make(parentLayout, message, Snackbar.LENGTH_INDEFINITE)
                .setAction("CLOSE", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                })
                .setActionTextColor(getResources().getColor(android.R.color.holo_red_light ))
                .show();
    }

    private void openMedicalHistoryScreen() {
        ChildMedicalHistoryActivity.startMe(this, memberObject);
    }

    private void openUpcomingServicePage() {
        MemberObject memberObject = new MemberObject(((ChildProfilePresenter) presenter()).getChildClient());
        CoreUpcomingServicesActivity.startMe(this, memberObject);
    }

    private void openVisitHomeScreen(boolean isEditMode) {
        ChildHomeVisitActivity.startMe(this, memberObject, isEditMode, ChildHomeVisitActivity.class);
    }

    private void openFamilyDueTab() {
        Intent intent = new Intent(this, FamilyProfileActivity.class);

        intent.putExtra(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID, ((ChildProfilePresenter) presenter()).getFamilyId());
        intent.putExtra(Constants.INTENT_KEY.FAMILY_HEAD, ((ChildProfilePresenter) presenter()).getFamilyHeadID());
        intent.putExtra(Constants.INTENT_KEY.PRIMARY_CAREGIVER, ((ChildProfilePresenter) presenter()).getPrimaryCareGiverID());
        intent.putExtra(Constants.INTENT_KEY.FAMILY_NAME, ((ChildProfilePresenter) presenter()).getFamilyName());

        intent.putExtra(org.smartregister.chw.util.Constants.INTENT_KEY.SERVICE_DUE, true);
        startActivity(intent);
    }

    private void addChildReferralTypes() {
        referralTypeModels.add(new ReferralTypeModel(getString(R.string.sick_child),
                org.smartregister.chw.util.Constants.JSON_FORM.getChildReferralForm()));
        if (MalariaDao.isRegisteredForMalaria(childBaseEntityId)) {
            referralTypeModels.add(new ReferralTypeModel(getString(R.string.client_malaria_follow_up), null));
        }
    }

    @Override
    protected View.OnClickListener getSickListener() {
        return v -> {
            Intent intent = new Intent(getApplication(), SickFormMedicalHistory.class);
            intent.putExtra(MEMBER_PROFILE_OBJECT, memberObject);
            startActivity(intent);
        };
    }

    public interface Flavor {
        OnClickFloatingMenu getOnClickFloatingMenu(Activity activity, ChildProfilePresenter presenter);
    }
}
