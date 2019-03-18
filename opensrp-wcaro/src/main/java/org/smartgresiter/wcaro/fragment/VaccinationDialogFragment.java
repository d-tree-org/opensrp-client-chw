package org.smartgresiter.wcaro.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vijay.jsonwizard.customviews.CheckBox;
import com.vijay.jsonwizard.customviews.RadioButton;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.smartgresiter.wcaro.R;
import org.smartgresiter.wcaro.application.WcaroApplication;
import org.smartgresiter.wcaro.contract.HomeVisitImmunizationContract;
import org.smartregister.domain.Alert;
import org.smartregister.family.util.DBConstants;
import org.smartregister.immunization.db.VaccineRepo;
import org.smartregister.immunization.domain.ServiceSchedule;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.domain.VaccineSchedule;
import org.smartregister.immunization.domain.VaccineWrapper;
import org.smartregister.immunization.util.Utils;
import org.smartregister.util.DatePickerUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.smartgresiter.wcaro.util.ChildUtils.fixVaccineCasing;

@SuppressLint("ValidFragment")
public class VaccinationDialogFragment extends ChildImmunizationFragment implements View.OnClickListener {
    private List<VaccineWrapper> tags;
    private Date dateOfBirth;
    private List<Vaccine> issuedVaccines;
    public static final String DIALOG_TAG = "VaccinationDialogFragment";
    public static final String WRAPPER_TAG = "tag";
    private HomeVisitImmunizationContract.View homeVisitImmunizationView;
    private int selectCount=0;
    private Button saveBtn;
    private LinearLayout multipleVaccineDatePickerView,singleVaccineAddView,vaccinationNameLayout;
    private CheckBox checkBoxNoVaccine;
    private LayoutInflater inflater;
    private DatePicker earlierDatePicker;
    private TextView textViewAddDate;
    private Map<VaccineWrapper,DatePicker> singleVaccineMap=new LinkedHashMap<>();

    public static VaccinationDialogFragment newInstance(Date dateOfBirth,
                                                        List<Vaccine> issuedVaccines,
                                                        ArrayList<VaccineWrapper> tags) {

        VaccinationDialogFragment customVaccinationDialogFragment = new VaccinationDialogFragment();

        Bundle args = new Bundle();
        args.putSerializable(WRAPPER_TAG, tags);
        customVaccinationDialogFragment.setArguments(args);
        customVaccinationDialogFragment.setDateOfBirth(dateOfBirth);
        customVaccinationDialogFragment.setIssuedVaccines(issuedVaccines);
        customVaccinationDialogFragment.setDisableConstraints(true);

        return customVaccinationDialogFragment;
    }

    public static VaccinationDialogFragment newInstance(Date dateOfBirth,
                                                        List<Vaccine> issuedVaccines,
                                                        ArrayList<VaccineWrapper> tags, boolean disableConstraints) {

        VaccinationDialogFragment customVaccinationDialogFragment = new VaccinationDialogFragment();

        Bundle args = new Bundle();
        args.putSerializable(WRAPPER_TAG, tags);
        customVaccinationDialogFragment.setArguments(args);
        customVaccinationDialogFragment.setDateOfBirth(dateOfBirth);
        customVaccinationDialogFragment.setIssuedVaccines(issuedVaccines);
        customVaccinationDialogFragment.setDisableConstraints(disableConstraints);

        return customVaccinationDialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_NoActionBar);
    }
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        this.inflater = inflater;
        ViewGroup dialogView = (ViewGroup) inflater.inflate(R.layout.fragment_vaccination_dialog_view, container, false);
        initUi(dialogView);
        return dialogView;
    }

    @Override
    public void onViewCreated(View view,  Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        parseBundleData();
        updateDatePicker(earlierDatePicker);
        updateVaccineList();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.add_date_separately:
                if(selectCount == 0) return;
                showSingleVaccineDetailsView();
                break;
            case R.id.checkbox_no_vaccination:
                checkBoxNoVaccine.toggle();
                break;
            case R.id.save_btn:
                saveData(earlierDatePicker,singleVaccineMap,selectCount,
                        singleVaccineAddView.getVisibility() == View.VISIBLE,dateOfBirth,findUnSelectedCheckBoxes(vaccinationNameLayout),findSelectedCheckBoxes(vaccinationNameLayout));
                dismiss();
                break;
            case R.id.close:
                dismiss();
                break;
        }
    }
    private void saveData(DatePicker earlierDatePicker,Map<VaccineWrapper,DatePicker> singleVaccineMap
            ,int selectCount,boolean isVisibleSingleVaccineView,Date dateOfBirth,List<String> unselectedCheckBox,List<String> selectedCheckBox){
        int day = earlierDatePicker.getDayOfMonth();
        int month = earlierDatePicker.getMonth();
        int year = earlierDatePicker.getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        DateTime dateTime = new DateTime(calendar.getTime());
        if(selectCount == 0){
            handleNotGivenVaccines(dateTime,dateOfBirth,unselectedCheckBox);
            dismiss();
            return;
        }

        if(isVisibleSingleVaccineView && singleVaccineMap.size()>0){
            handleSingleVaccineLogic(singleVaccineMap,dateOfBirth);
            handleNotGivenVaccines(dateTime,dateOfBirth,unselectedCheckBox);
            dismiss();
            return;
        }
        handleMultipleVaccineGiven(dateTime,dateOfBirth,selectedCheckBox);
        handleNotGivenVaccines(dateTime,dateOfBirth,unselectedCheckBox);
    }

    private void handleSingleVaccineLogic(Map<VaccineWrapper,DatePicker> singleVaccineMap,Date dateOfBirth) {
        ArrayList<VaccineWrapper> tagsToUpdate = new ArrayList<>();
        for (VaccineWrapper wrapper:singleVaccineMap.keySet()){
            DatePicker datePicker=singleVaccineMap.get(wrapper);
            if (datePicker != null) {
                int day = datePicker.getDayOfMonth();
                int month = datePicker.getMonth();
                int year = datePicker.getYear();

                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);
                DateTime dateTime = new DateTime(calendar.getTime());
                if (validateVaccinationDate(dateOfBirth,dateTime.toDate())) {
                    wrapper.setUpdatedVaccineDate(dateTime, false);
                    tagsToUpdate.add(wrapper);
                } else {
                    showToast(String.format(getString(R.string.cannot_record_vaccine),
                            wrapper.getName()));
                }
            }
        }
        onVaccinateEarlier(tagsToUpdate);
        homeVisitImmunizationView.getPresenter().assigntoGivenVaccines(tagsToUpdate);

    }
    private void handleMultipleVaccineGiven(DateTime dateTime,Date dateOfBirth,List<String> selectedCheckboxes){
        ArrayList<VaccineWrapper> tagsToUpdate = new ArrayList<>();
        for (String checkedName : selectedCheckboxes) {
            VaccineWrapper tag = searchWrapperByName(checkedName);
            if (tag != null) {
                if (validateVaccinationDate(dateOfBirth,dateTime.toDate())) {
                    tag.setUpdatedVaccineDate(dateTime, false);
                    tagsToUpdate.add(tag);
                } else {
                    showToast( String.format(getString(R.string.cannot_record_vaccine),
                            tag.getName()));
                }
            }
        }
        if(tagsToUpdate.size() > 0){
            onVaccinateEarlier(tagsToUpdate);
            homeVisitImmunizationView.getPresenter().assigntoGivenVaccines(tagsToUpdate);
        }

    }
    private void handleNotGivenVaccines(DateTime dateTime,Date dateOfBirth,List<String> UnselectedCheckboxes){
        ArrayList<VaccineWrapper> UngiventagsToUpdate = new ArrayList<>();
        for (String uncheckedName : UnselectedCheckboxes) {
            VaccineWrapper untag = searchWrapperByName(uncheckedName);
            if (untag != null) {
                if (validateVaccinationDate(dateOfBirth,dateTime.toDate())) {
                    untag.setUpdatedVaccineDate(dateTime, false);
                    UngiventagsToUpdate.add(untag);
                } else {
                    showToast(String.format(getString(R.string.cannot_record_vaccine),
                            untag.getName()));
                }
            }
        }
        for(VaccineWrapper tags:UngiventagsToUpdate) {
            homeVisitImmunizationView.getPresenter().updateNotGivenVaccine(tags);
        }

    }
    private Context getApplicationContext(){
        return WcaroApplication.getInstance().getApplicationContext();
    }
    private void showToast(String message){
        Toast.makeText(getApplicationContext(),message, Toast.LENGTH_LONG).show();

    }
    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setIssuedVaccines(List<Vaccine> issuedVaccines) {
        this.issuedVaccines = issuedVaccines;
    }

    public void setDisableConstraints(boolean disableConstraints) {
        if (disableConstraints) {
            Calendar dcToday = Calendar.getInstance();
            VaccineSchedule.standardiseCalendarDate(dcToday);
        }
    }
    private void parseBundleData(){
        Bundle bundle = getArguments();
        final Serializable serializable = bundle.getSerializable(WRAPPER_TAG);
        if (serializable != null && serializable instanceof ArrayList) {
            tags = (ArrayList<VaccineWrapper>) serializable;
        }

        if (tags == null || tags.isEmpty()) {
            return;
        }
    }
    private void initUi(View dialogView){
        multipleVaccineDatePickerView=dialogView.findViewById(R.id.multiple_vaccine_date_pickerview);
        singleVaccineAddView=dialogView.findViewById(R.id.single_vaccine_add_layout);
        saveBtn=dialogView.findViewById(R.id.save_btn);
        View noVaccineLayout=dialogView.findViewById(R.id.checkbox_no_vaccination);
        noVaccineLayout.setOnClickListener(this);
        checkBoxNoVaccine=noVaccineLayout.findViewById(R.id.select);
        checkBoxNoVaccine.setChecked(false);
        checkBoxNoVaccine.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    resetAllSelectedVaccine();
                }else {
                    updateSaveButton();
                }

            }
        });
        saveBtn= (Button) dialogView.findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(this);
        vaccinationNameLayout= (LinearLayout) dialogView.findViewById(R.id.vaccination_name_layout);
        earlierDatePicker= (DatePicker) dialogView.findViewById(R.id.earlier_date_picker);
        textViewAddDate= (TextView) dialogView.findViewById(R.id.add_date_separately);
        textViewAddDate.setOnClickListener(this);
        ((ImageView) dialogView.findViewById(R.id.close)).setOnClickListener(this);
    }
    private void resetAllSelectedVaccine(){
        Map<CheckBox,String> getSelectedCheckBox=getSelectedCheckBoxes();
        for (CheckBox checkBox:getSelectedCheckBox.keySet()){
            checkBox.toggle();
        }
        multipleVaccineDatePickerView.setAlpha(0.3f);

    }
    private void updateVaccineList(){
        if(tags==null) return;
        for (VaccineWrapper vaccineWrapper : tags) {

            View vaccinationName = inflater.inflate(R.layout.custom_vaccine_name_check, null);
            TextView vaccineView = (TextView) vaccinationName.findViewById(R.id.vaccine);


            VaccineRepo.Vaccine vaccine = vaccineWrapper.getVaccine();
            if (vaccineWrapper.getVaccine() != null) {
                vaccineView.setText(fixVaccineCasing(vaccine.display()));
            } else {
                vaccineView.setText(vaccineWrapper.getName());
            }
            vaccinationNameLayout.addView(vaccinationName);
        }

        selectCount=vaccinationNameLayout.getChildCount();
        for (int i = 0; i < vaccinationNameLayout.getChildCount(); i++) {
            View childView = vaccinationNameLayout.getChildAt(i);
            final CheckBox childSelect = (CheckBox) childView.findViewById(R.id.select);
            childSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        selectCount++;
                    }else{
                        selectCount--;
                    }
                    updateSaveButton();
                }
            });
            childView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    childSelect.toggle();
                }
            });
        }
    }

    private void showSingleVaccineDetailsView(){
        multipleVaccineDatePickerView.setVisibility(View.GONE);
        ArrayList<VaccineWrapper> vaccineWrappers = new ArrayList<VaccineWrapper>();
        List<String> selectedCheckboxes = findSelectedCheckBoxes(vaccinationNameLayout);
        singleVaccineAddView.removeAllViews();
        singleVaccineMap.clear();
        for (String checkedName : selectedCheckboxes) {
            singleVaccineAddView.setVisibility(View.VISIBLE);
            VaccineWrapper tag = searchWrapperByName(checkedName);
            String dobString = org.smartregister.util.Utils.getValue(getChildDetails().getColumnmaps(), DBConstants.KEY.DOB, false);

            if (tag != null) {
                if (!TextUtils.isEmpty(dobString)) {
                    View layout = inflater.inflate(R.layout.custom_single_vaccine_view, null);
                    TextView question=layout.findViewById(R.id.vaccines_given_when_title_question);
                    DatePicker datePicker=layout.findViewById(R.id.earlier_date_picker);
                    question.setText(getString(R.string.when_vaccine,checkedName));
                    updateDatePicker(datePicker);

                    vaccineWrappers.add(tag);
                    singleVaccineMap.put(tag,datePicker);
                    singleVaccineAddView.addView(layout);

                }
            }
        }

    }

    private void updateSaveButton(){
        if(saveBtn!=null){
            if(selectCount==0){
                checkBoxNoVaccine.setChecked(true);
                multipleVaccineDatePickerView.setVisibility(View.VISIBLE);
                multipleVaccineDatePickerView.setAlpha(0.3f);
                singleVaccineAddView.setVisibility(View.GONE);
                //saveBtn.setAlpha(0.3f);
            }else{
                checkBoxNoVaccine.setChecked(false);
                multipleVaccineDatePickerView.setAlpha(1.0f);
                if(singleVaccineAddView.getVisibility() == View.VISIBLE){
                    showSingleVaccineDetailsView();
                }
                saveBtn.setAlpha(1.0f);
            }

        }
    }
    private void updateDatePicker(DatePicker datePicker) {
        DateTime minDate = new DateTime(dateOfBirth);
        DateTime dcToday = ServiceSchedule.standardiseDateTime(DateTime.now());
        DateTime maxDate = ServiceSchedule.standardiseDateTime(dcToday);

        datePicker.setMinDate(minDate.getMillis());
        datePicker.setMaxDate(maxDate.getMillis());

    }

    @Override
    public void onResume() {
        super.onResume();
        updateSaveButton();
    }

    private boolean validateVaccinationDate(Date dateOfBirth, Date date) {
        // Assuming that the vaccine wrapper provided to this method isn't one where there's more than one vaccine in a wrapper
        Date minDate;
        Date maxDate;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateOfBirth);
        Calendar dcToday = Calendar.getInstance();
        VaccineSchedule.standardiseCalendarDate(calendar);
        minDate = calendar.getTime();
        maxDate = dcToday.getTime();
        Calendar vaccineDate = Calendar.getInstance();
        vaccineDate.setTime(date);
        VaccineSchedule.standardiseCalendarDate(vaccineDate);
        boolean result;

        // A null min date means the vaccine is not due (probably because the prerequisite hasn't been done yet)
        result = minDate != null;

        // Check if vaccination was done before min date
        if (minDate != null) {
            Calendar min = Calendar.getInstance();
            min.setTime(minDate);
            VaccineSchedule.standardiseCalendarDate(min);
            result = min.getTimeInMillis() <= vaccineDate.getTimeInMillis();
        }

        // A null max date means the vaccine doesn't have a max date check
        //Check if vaccination was done after the max date
        if (maxDate != null) {
            Calendar max = Calendar.getInstance();
            max.setTime(maxDate);
            VaccineSchedule.standardiseCalendarDate(max);

            result = result && vaccineDate.getTimeInMillis() <= max.getTimeInMillis();
        }

        return result;
    }

    @Override
    public void onStart() {
        super.onStart();

        // without a handler, the window sizes itself correctly
        // but the keyboard does not show up
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Window window = null;
                if (getDialog() != null) {
                    window = getDialog().getWindow();
                }

                if (window == null) {
                    return;
                }

                Point size = new Point();

                Display display = window.getWindowManager().getDefaultDisplay();
                display.getSize(size);

                window.setLayout(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                window.setGravity(Gravity.CENTER);
            }
        });
    }

    private VaccineWrapper searchWrapperByName(String name) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }

        for (VaccineWrapper tag : tags) {
            if (tag.getVaccine() != null) {
                if (tag.getVaccine().display().equalsIgnoreCase(name)) {
                    return tag;
                }
            } else {
                if (tag.getName().equalsIgnoreCase(name)) {
                    return tag;
                }
            }
        }
        return null;
    }
    private Map<CheckBox,String> getSelectedCheckBoxes(){
        Map<CheckBox,String> vaccineCheckMap=new LinkedHashMap<>();
        for (int i = 0; i < vaccinationNameLayout.getChildCount(); i++) {
            View chilView = vaccinationNameLayout.getChildAt(i);
            CheckBox selectChild = (CheckBox) chilView.findViewById(R.id.select);
            if (selectChild.isChecked()) {
                TextView childVaccineView = (TextView) chilView.findViewById(R.id.vaccine);
                String checkedName = childVaccineView.getText().toString();
                vaccineCheckMap.put(selectChild,checkedName);
            }
        }
        return vaccineCheckMap;
    }


    private List<String> findSelectedCheckBoxes(LinearLayout vaccinationNameLayout) {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < vaccinationNameLayout.getChildCount(); i++) {
            View chilView = vaccinationNameLayout.getChildAt(i);
            CheckBox selectChild = (CheckBox) chilView.findViewById(R.id.select);
            if (selectChild.isChecked()) {
                TextView childVaccineView = (TextView) chilView.findViewById(R.id.vaccine);
                String checkedName = childVaccineView.getText().toString();
                names.add(checkedName);
            }
        }

        return names;
    }

    private List<String> findUnSelectedCheckBoxes(LinearLayout vaccinationNameLayout) {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < vaccinationNameLayout.getChildCount(); i++) {
            View chilView = vaccinationNameLayout.getChildAt(i);
            CheckBox selectChild = (CheckBox) chilView.findViewById(R.id.select);
            if (!selectChild.isChecked()) {
                TextView childVaccineView = (TextView) chilView.findViewById(R.id.vaccine);
                String checkedName = childVaccineView.getText().toString();
                names.add(checkedName);
            }
        }

        return names;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }


    public void setView(HomeVisitImmunizationContract.View homeVisitImmunizationView) {
        this.homeVisitImmunizationView = homeVisitImmunizationView;
    }


}