package org.smartregister.chw.interactor;

import android.content.Context;

import org.jetbrains.annotations.NotNull;
import org.joda.time.LocalDate;
import org.smartregister.CoreLibrary;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.contract.BaseAncMemberProfileContract;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.model.BaseUpcomingService;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.chw.contract.PncMemberProfileContract;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.contract.AncMemberProfileContract;
import org.smartregister.chw.core.interactor.CoreAncMemberProfileInteractor;
import org.smartregister.chw.dao.FamilyDao;
import org.smartregister.dao.AbstractDao;
import org.smartregister.domain.Alert;
import org.smartregister.domain.AlertStatus;
import org.smartregister.domain.Task;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

public class AncMemberProfileInteractor extends CoreAncMemberProfileInteractor implements org.smartregister.chw.contract.AncMemberProfileContract.Interactor {

    public AncMemberProfileInteractor(Context context) {
        super(context);
    }

    /**
     * Compute and process the lower profile info
     *
     * @param memberObject
     * @param callback
     */
    @Override
    public void refreshProfileInfo(final MemberObject memberObject, final BaseAncMemberProfileContract.InteractorCallBack callback) {
        Runnable runnable = new Runnable() {

            Date lastVisitDate = getLastVisitDate(memberObject);
            AlertStatus familyAlert = FamilyDao.getFamilyAlertStatus(memberObject.getBaseEntityId());
            Alert upcomingService = getAlerts(context, memberObject);

            @Override
            public void run() {
                appExecutors.mainThread().execute(() -> {
                    callback.refreshLastVisit(lastVisitDate);
                    callback.refreshFamilyStatus(familyAlert);

                    if (upcomingService == null) {
                        callback.refreshUpComingServicesStatus("", AlertStatus.complete, new Date());
                    } else {
                        callback.refreshUpComingServicesStatus(upcomingService.scheduleName(), upcomingService.status(), new LocalDate(upcomingService.startDate()).toDate());
                    }
                });
            }
        };
        appExecutors.diskIO().execute(runnable);
    }

    private Alert getAlerts(Context context, MemberObject memberObject) {
        AncUpcomingServicesInteractorFlv upcomingServicesInteractor = new AncUpcomingServicesInteractorFlv();
        try {
            List<BaseUpcomingService> baseUpcomingServices = upcomingServicesInteractor.getMemberServices(context, memberObject);
            if (baseUpcomingServices.size() > 0) {
                Comparator<BaseUpcomingService> comparator = (o1, o2) -> o1.getServiceDate().compareTo(o2.getServiceDate());
                Collections.sort(baseUpcomingServices, comparator);

                BaseUpcomingService baseUpcomingService = baseUpcomingServices.get(0);
                return new Alert(
                        memberObject.getBaseEntityId(),
                        baseUpcomingService.getServiceName(),
                        baseUpcomingService.getServiceName(),
                        baseUpcomingService.getServiceDate().before(new LocalDate().toDate()) ? AlertStatus.urgent : AlertStatus.normal,
                        AbstractDao.getDobDateFormat().format(baseUpcomingService.getServiceDate()),
                        "",
                        true
                );
            }
        } catch (Exception e) {
            Timber.e(e);
        }

        return null;
    }

    protected Date getLastVisitDate(MemberObject memberObject) {
        Date lastVisitDate = null;
        Visit lastVisit = AncLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), Constants.EVENT_TYPE.ANC_HOME_VISIT);
        if (lastVisit != null) {
            lastVisitDate = lastVisit.getDate();
        }

        return lastVisitDate;
    }

    @Override
    public void getClientTasks(String planId, String baseEntityId, AncMemberProfileContract.@NotNull InteractorCallBack callback) {
        Set<Task> taskList = CoreChwApplication.getInstance().getTaskRepository().getTasksByEntityAndStatus(planId, baseEntityId, Task.TaskStatus.READY);
        Set<Task> inProgressTasks = CoreChwApplication.getInstance().getTaskRepository().getTasksByEntityAndStatus(planId, baseEntityId, Task.TaskStatus.IN_PROGRESS);
        taskList.addAll(inProgressTasks);

        callback.setClientTasks(taskList);
    }

    @Override
    public void getFingerprintForVerification(String baseEntityId, org.smartregister.chw.contract.AncMemberProfileContract.InteractorCallback callback) {
        org.smartregister.domain.db.Client client = CoreLibrary.getInstance().context().getEventClientRepository().fetchClientByBaseEntityId(baseEntityId);
        String simprintsId = client.getIdentifier("simprints_guid");
        if (simprintsId != null){
            callback.onFingerprintFetched(simprintsId, true, client);
        }else {
            callback.onFingerprintFetched("", false, client);
        }
    }

}
