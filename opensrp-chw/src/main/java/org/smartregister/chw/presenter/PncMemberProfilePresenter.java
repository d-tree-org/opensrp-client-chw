package org.smartregister.chw.presenter;


import android.app.Activity;

import org.apache.commons.lang3.tuple.Triple;
import org.smartregister.chw.anc.contract.BaseAncMemberProfileContract;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.presenter.BaseAncMemberProfilePresenter;
import org.smartregister.chw.contract.PncMemberProfileContract;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.interactor.PncMemberProfileInteractor;
import org.smartregister.chw.model.ReferralTypeModel;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Task;
import org.smartregister.domain.db.Client;
import org.smartregister.family.contract.FamilyProfileContract;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.family.util.Utils;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.util.FormUtils;

import java.util.List;
import java.util.Set;

import timber.log.Timber;

public class PncMemberProfilePresenter extends BaseAncMemberProfilePresenter implements
        PncMemberProfileContract.Presenter, FamilyProfileContract.InteractorCallBack, PncMemberProfileContract.InteractorCallBack {

    private FormUtils formUtils;
    private String entityId;

    BaseAncMemberProfileContract.Interactor interactor;

    public PncMemberProfilePresenter(BaseAncMemberProfileContract.View view,
                                     BaseAncMemberProfileContract.Interactor interactor, MemberObject memberObject) {
        super(view, interactor, memberObject);
        this.interactor = interactor;
        setEntityId(memberObject.getBaseEntityId());
    }

    public void startFormForEdit(CommonPersonObjectClient commonPersonObject) {
//        TODO Implement
    }

    @Override
    public void refreshProfileTopSection(CommonPersonObjectClient client) {
//        TODO Implement
    }

    @Override
    public void onUniqueIdFetched(Triple<String, String, String> triple, String entityId) {
//        TODO Implement
        Timber.d("onUniqueIdFetched unimplemented");
    }

    @Override
    public void onNoUniqueId() {
//        TODO Implement
        Timber.d("onNoUniqueId unimplemented");
    }

    @Override
    public void onRegistrationSaved(boolean b, boolean b1, FamilyEventClient familyEventClient) {
        // TODO     
    }


    public PncMemberProfileContract.View getView() {
        if (view != null) {
            return (PncMemberProfileContract.View) view.get();
        } else {
            return null;
        }
    }

    @Override
    public void startPncReferralForm() {
        try {
            getView().startFormActivity(getFormUtils().getFormJson(CoreConstants.JSON_FORM.getPncReferralForm()));
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    private PncMemberProfileContract.Interactor getInteractor(){
        if (interactor != null)
            return (PncMemberProfileContract.Interactor) interactor;
        return new PncMemberProfileInteractor(this.getView().getContext());
    }

    @Override
    public void verifyFingerprint() {
        getInteractor().getFingerprintForVerification(getEntityId(), this);
    }

    @Override
    public void referToFacility() {
        List<ReferralTypeModel> referralTypeModels = getView().getReferralTypeModels();
        if (referralTypeModels.size() == 1) {
            startPncReferralForm();
        } else {
            org.smartregister.chw.util.Utils.launchClientReferralActivity((Activity) getView(), referralTypeModels, getEntityId());
        }
    }

    @Override
    public void createReferralEvent(AllSharedPreferences allSharedPreferences, String jsonString) throws Exception {
        ((PncMemberProfileContract.Interactor) interactor).createReferralEvent(allSharedPreferences, jsonString, getEntityId());
    }

    @Override
    public void saveForm(String json, String table) {
        ((PncMemberProfileContract.Interactor)interactor).updatePregnancyOutcome(json, this, table);
    }

    @Override
    public void onSaved(boolean result) {
        getView().onPregnancyOutcomeUpdated(true);
    }

    private FormUtils getFormUtils() {
        if (formUtils == null) {
            try {
                formUtils = FormUtils.getInstance(Utils.context().applicationContext());
            } catch (Exception e) {
                Timber.e(e);
            }
        }
        return formUtils;
    }

    public String getEntityId() {
        return entityId;
    }

    private void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public void fetchTasks(){
        ((PncMemberProfileContract.Interactor)interactor).getClientTasks(CoreConstants.REFERRAL_PLAN_ID, getEntityId(), this);
    }

    @Override
    public void setClientTasks(Set<Task> taskList) {
        if (getView() != null) {
            getView().setClientTasks(taskList);
        }
    }

    @Override
    public void onFingerprintFetched(String fingerprint, boolean hasFingerprint, Client client) {
        if (hasFingerprint){
            //Call Fingerprint Verification
            getView().callFingerprintVerification(fingerprint);
        }else{
            //Call Fingerprint Identification
            getView().callFingerprintRegistration(client);
        }
    }
}


