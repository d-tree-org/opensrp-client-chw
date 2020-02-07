package org.smartregister.chw.service;

import android.util.Pair;

import org.joda.time.DateTime;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.dao.ChildDao;
import org.smartregister.chw.core.domain.Child;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.VaccineScheduleUtil;
import org.smartregister.chw.core.utils.VisitVaccineUtil;
import org.smartregister.domain.Alert;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.domain.VaccineSchedule;
import org.smartregister.immunization.domain.jsonmapping.VaccineGroup;
import org.smartregister.immunization.util.VaccinatorUtils;
import org.smartregister.repository.AlertRepository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class ChildAlertService {

    private static List<org.smartregister.immunization.domain.jsonmapping.Vaccine> _specialVaccines;
    private static List<VaccineGroup> _vaccineGroups;

    private static List<org.smartregister.immunization.domain.jsonmapping.Vaccine> getSpecialVaccines() {
        if (_specialVaccines == null)
            _specialVaccines = VaccinatorUtils.getSpecialVaccines(CoreChwApplication.getInstance().getApplicationContext());

        return _specialVaccines;
    }

    private static List<VaccineGroup> getVaccineGroups() {
        if (_vaccineGroups == null)
            _vaccineGroups = VaccineScheduleUtil.getVaccineGroups(CoreChwApplication.getInstance().getApplicationContext(), CoreConstants.SERVICE_GROUPS.CHILD);

        return _vaccineGroups;
    }

    public static void updateAlerts(String baseEntityID) {

        Child child = ChildDao.getChild(baseEntityID);
        if (child != null) {
            try {
                Pair<List<Vaccine>, Map<String, Date>> issuedVaccines = VisitVaccineUtil.getIssuedVaccinesList(baseEntityID, true);

                /// compute the alerts
                HashMap<String, HashMap<String, VaccineSchedule>> vaccineSchedules =
                        VisitVaccineUtil.getSchedule(getVaccineGroups(), getSpecialVaccines(), CoreConstants.SERVICE_GROUPS.CHILD);

                List<Alert> alerts =
                        VisitVaccineUtil.getInMemoryAlerts(vaccineSchedules, baseEntityID, new DateTime(child.getDateOfBirth()), CoreConstants.SERVICE_GROUPS.CHILD, issuedVaccines.first);

                AlertRepository repository = new AlertRepository();
                repository.updateMasterRepository(ChwApplication.getInstance().getRepository());

                // delete old vaccine alerts
                repository.deleteAllAlertsForEntity(baseEntityID);

                // save the alerts
                for (Alert a : alerts) {
                    repository.createAlert(a);
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }
}
