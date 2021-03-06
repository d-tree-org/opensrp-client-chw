package org.smartregister.chw.presenter;

import android.app.Activity;
import android.util.Pair;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.json.JSONObject;
import org.smartregister.chw.R;
import org.smartregister.chw.activity.ChildProfileActivity;
import org.smartregister.chw.activity.ReferralRegistrationActivity;
import org.smartregister.chw.contract.ChildProfileContract;
import org.smartregister.chw.core.contract.CoreChildProfileContract;
import org.smartregister.chw.core.interactor.CoreChildProfileInteractor;
import org.smartregister.chw.core.presenter.CoreChildProfilePresenter;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.dataloader.FamilyMemberDataLoader;
import org.smartregister.chw.form_data.NativeFormsDataBinder;
import org.smartregister.chw.interactor.ChildProfileInteractor;
import org.smartregister.chw.interactor.FamilyProfileInteractor;
import org.smartregister.chw.model.ChildRegisterModel;
import org.smartregister.chw.model.ReferralTypeModel;
import org.smartregister.chw.util.Constants;
import org.smartregister.chw.util.JsonFormUtils;
import org.smartregister.chw.util.Utils;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.util.DBConstants;
import org.smartregister.util.FormUtils;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

public class ChildProfilePresenter extends CoreChildProfilePresenter implements ChildProfileContract.Presenter, ChildProfileContract.InteractorCallback {

    private List<ReferralTypeModel> referralTypeModels;

    public ChildProfilePresenter(CoreChildProfileContract.View childView, CoreChildProfileContract.Model model, String childBaseEntityId) {
        super(childView, model, childBaseEntityId);
        setView(new WeakReference<>(childView));
        setInteractor(new ChildProfileInteractor());
        getInteractor().setChildBaseEntityId(childBaseEntityId);
        setModel(model);
    }

    @Override
    public void verifyChildFingerprint() {
        /**
         * Get child profile
         * Check if has fingerprint registered
         * if not -> Call fingerprint enrollment
         * If yes -> Get the child's fingerprint
         *  Call simprints verification
         */
        getAtInteractor().getChildFingerprintForVerification(childBaseEntityId, this);

    }

    @Override
    public void callFingerprintEnrollment(org.smartregister.domain.db.Client client) {
        getAtView().callFingerprintRegister(client);
    }

    @Override
    public void callFingerprintVerification(String clientFingerprint) {
        getAtView().callFingerprintVerification(clientFingerprint);
    }

    @Override
    public void verifyHasPhone() {
        new FamilyProfileInteractor().verifyHasPhone(familyID, this);
    }

    @Override
    public void notifyHasPhone(boolean hasPhone) {
        if (getView() != null) {
            getView().updateHasPhone(hasPhone);
        }
    }

    @Override
    public void updateChildProfile(String jsonString) {
        getView().showProgressDialog(R.string.updating);
        Pair<Client, Event> pair = new ChildRegisterModel().processRegistration(jsonString);
        if (pair == null) {
            return;
        }

        getInteractor().saveRegistration(pair, jsonString, true, this);
    }

    @Override
    public void startSickChildReferralForm() {
        try {
            JSONObject formJson = FormUtils.getInstance(getView().getContext())
                    .getFormJson(Constants.JSON_FORM.getChildReferralForm());
            formJson.put(Constants.REFERRAL_TASK_FOCUS, referralTypeModels.get(0).getReferralType());
            ReferralRegistrationActivity.startGeneralReferralFormActivityForResults((Activity) getView().getContext(),
                    getChildBaseEntityId(), formJson, null);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void startFormForEdit(String title, CommonPersonObjectClient client) {
        JSONObject form = getInteractor().getAutoPopulatedJsonEditFormString(CoreConstants.JSON_FORM.getChildRegister(), title, getView().getApplicationContext(), client);
        try {


            if (!StringUtils.isBlank(client.getColumnmaps().get(ChildDBConstants.KEY.RELATIONAL_ID))) {
                JSONObject metaDataJson = form.getJSONObject("metadata");
                JSONObject lookup = metaDataJson.getJSONObject("look_up");
                lookup.put("entity_id", "family");
                lookup.put("value", client.getColumnmaps().get(ChildDBConstants.KEY.RELATIONAL_ID));
            }

            //TODO Check if fingerprint value was captured successfully, if not fetch it manually from the client object

            getView().startFormActivity(form);
        } catch (Exception e) {
            Timber.e(e, "CoreChildProfilePresenter --> startFormForEdit");
        }
    }

    @Override
    public void startSickChildForm(CommonPersonObjectClient client) {
        try {
            getView().setProgressBarState(true);
            JSONObject jsonObject = this.getFormUtils().getFormJson(CoreConstants.JSON_FORM.getChildSickForm());
            jsonObject.put(CoreConstants.ENTITY_ID, Utils.getValue(client.getColumnmaps(), DBConstants.KEY.BASE_ENTITY_ID, false));

            String dobStr = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.DOB, false);
            Date dobDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(dobStr);

            LocalDate date1 = LocalDate.fromDateFields(dobDate);
            LocalDate date2 = LocalDate.now();
            int months = Months.monthsBetween(date1, date2).getMonths();

            Map<String, String> valueMap = new HashMap<>();
            valueMap.put("age_in_months", String.valueOf(months));
            valueMap.put("child_first_name", Utils.getValue(client.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true));

            JsonFormUtils.populatedJsonForm(jsonObject, valueMap);

            this.getView().startFormActivity(jsonObject);
        } catch (Exception var3) {
            Timber.e(var3);
        } finally {
            if (getView() != null)
                getView().setProgressBarState(false);
        }
    }

    @Override
    public void getChildVerifyFingerprintPermission(String childBaseEntityId) {
        getAtInteractor().verifyChildFingerprintPermission(childBaseEntityId, this);
    }

    @Override
    public void setVerifyFingerprintPermission(boolean permission) {
        getAtView().updateChildFingerprintVerificationPermission(permission);
    }

    public void referToFacility() {
        referralTypeModels = ((ChildProfileActivity) getView()).getReferralTypeModels();
        if (referralTypeModels.size() == 1) {
            startSickChildReferralForm();
        } else {
            Utils.launchClientReferralActivity((Activity) getView(), referralTypeModels, childBaseEntityId);
        }
    }

    ChildProfileContract.Interactor getAtInteractor(){
        return (ChildProfileContract.Interactor) getInteractor();
    }


    ChildProfileContract.View getAtView(){
        return (ChildProfileContract.View) getView();
    }
}
