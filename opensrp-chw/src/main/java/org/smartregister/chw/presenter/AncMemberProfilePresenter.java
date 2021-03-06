package org.smartregister.chw.presenter;

import android.app.Activity;

import org.json.JSONObject;
import org.smartregister.chw.activity.AncMemberProfileActivity;
import org.smartregister.chw.activity.ReferralRegistrationActivity;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.presenter.BaseAncMemberProfilePresenter;
import org.smartregister.chw.core.contract.AncMemberProfileContract;
import org.smartregister.chw.core.presenter.CoreAncMemberProfilePresenter;
import org.smartregister.chw.model.ReferralTypeModel;
import org.smartregister.chw.util.Constants;
import org.smartregister.chw.util.Utils;
import org.smartregister.domain.db.Client;
import org.smartregister.util.FormUtils;

import java.util.List;

import timber.log.Timber;

public class AncMemberProfilePresenter extends CoreAncMemberProfilePresenter
        implements org.smartregister.chw.contract.AncMemberProfileContract.Presenter, org.smartregister.chw.contract.AncMemberProfileContract.InteractorCallback{

    private List<ReferralTypeModel> referralTypeModels;
    org.smartregister.chw.contract.AncMemberProfileContract.Interactor interactor;

    public AncMemberProfilePresenter(AncMemberProfileContract.View view, org.smartregister.chw.contract.AncMemberProfileContract.Interactor interactor,
                                     MemberObject memberObject) {
        super(view, (AncMemberProfileContract.Interactor) interactor, memberObject);
        this.interactor = interactor;
    }

    @Override
    public void referToFacility() {
        referralTypeModels = ((AncMemberProfileActivity) getView()).getReferralTypeModels();
        if (referralTypeModels.size() == 1) {
            startAncReferralForm();
        } else {
            Utils.launchClientReferralActivity((Activity) getView(), referralTypeModels, getEntityId());
        }
    }

    @Override
    public void verifyFingerprint() {
        interactor.getFingerprintForVerification(getEntityId(), this);
    }

    @Override
    public void startAncReferralForm() {
        try {
            Activity context = ((Activity) getView());
            JSONObject formJson = FormUtils.getInstance(context).getFormJson(
                    Constants.JSON_FORM.getAncReferralForm());
            formJson.put(Constants.REFERRAL_TASK_FOCUS, referralTypeModels.get(0).getReferralType());
            ReferralRegistrationActivity.startGeneralReferralFormActivityForResults(context,
                    getEntityId(), formJson,null);
        } catch (Exception ex) {
            Timber.e(ex);
        }

    }

    @Override
    public void onFingerprintFetched(String fingerprint, boolean hasFingerprint, Client client) {
        if (hasFingerprint){
            //Call Verification
            getview().callFingerprintVerification(fingerprint);
        }else{
            //Call Registration
            getview().callFingerprintRegistration(client);
        }
    }

    private org.smartregister.chw.contract.AncMemberProfileContract.View getview(){
        return (org.smartregister.chw.contract.AncMemberProfileContract.View) getView();
    }

}
