package org.smartregister.chw.fragment;

import static org.smartregister.chw.util.FnInterfaces.KeyValue;
import static org.smartregister.clientandeventmodel.FormEntityConstants.Address.state;

import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.vijay.jsonwizard.customviews.CheckBox;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.chw.R;
import org.smartregister.chw.anc.contract.BaseAncHomeVisitContract;
import org.smartregister.chw.anc.domain.VaccineDisplay;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.util.JsonFormUtils;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.util.FnList;
import org.smartregister.chw.util.UtilsFlv;
import org.smartregister.view.customcontrols.CustomFontTextView;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseHomeVisitImmunizationFragmentFlv extends DefaultBaseHomeVisitImmunizationFragment {
    private View root;
    private LinearLayout datesContainer;
    private LinearLayout vaccinesContainer;
    private List<CheckBox> vaccineCheckboxes;
    private CustomFontTextView congratulationsView;
    public static BaseHomeVisitImmunizationFragmentFlv getInstance(final BaseAncHomeVisitContract.VisitView view, String baseEntityID, Map<String, List<VisitDetail>> details, List<VaccineDisplay> vaccineDisplays) {
        return getInstance(view, baseEntityID, details, vaccineDisplays, true);
    }

    public static BaseHomeVisitImmunizationFragmentFlv getInstance(final BaseAncHomeVisitContract.VisitView view, String baseEntityID, Map<String, List<VisitDetail>> details, List<VaccineDisplay> vaccineDisplays, boolean defaultChecked) {
        BaseHomeVisitImmunizationFragmentFlv fragment = new BaseHomeVisitImmunizationFragmentFlv();
        fragment.visitView = view;
        fragment.baseEntityID = baseEntityID;
        fragment.details = details;
        fragment.vaccinesDefaultChecked = defaultChecked;
        for (VaccineDisplay vaccineDisplay : vaccineDisplays) {
            fragment.vaccineDisplays.put(vaccineDisplay.getVaccineWrapper().getName(), vaccineDisplay);
        }

        if (details != null && details.size() > 0) {
            fragment.jsonObject = NCUtils.getVisitJSONFromVisitDetails(view.getMyContext(), baseEntityID, details, vaccineDisplays);
            JsonFormUtils.populateForm(fragment.jsonObject, details);
        }
        return fragment;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
    }


    private  void init(View view){
        root=view;
        datesContainer = root.getRootView().findViewById(R.id.single_vaccine_add_layout);
        vaccinesContainer = root.findViewById(R.id.vaccination_name_layout);
        vaccineCheckboxes = getAllVaccineCheckboxes().list();
        createUIForReasonsNoVaccines(view);
        addCongratulationsMessages(view);
        view.findViewById(R.id.save_btn).setOnClickListener(this::save);
    }

    private void createUIForReasonsNoVaccines(View root){
        CheckBox noVaccine=root.findViewById(R.id.checkbox_no_vaccination).findViewById(R.id.select);
        noVaccine.setOnClickListener(v->System.out.println("ignore"));
        noVaccine.setOnCheckedChangeListener((checkbox, b) -> onSelectingNoVaccination(b));

        ViewGroup layout = (ViewGroup)noVaccine.getParent().getParent();
        LinearLayout reasonsContainer = createContainerForReasonsNoVaccines(layout);
        reasonsContainer.addView(createTextViewUI(R.string.why_no_vaccine,reasonsContainer));

        new FnList<>(root.getResources().getStringArray(R.array.reason_no_vaccine))
                .map(KeyValue::create)
                .map(reason->createViewOptionForNoVaccines(reason,reasonsContainer))
                .forEachItem(reasonsContainer::addView);
        layout.addView(reasonsContainer);
    }

    private void addCongratulationsMessages(View root){
        ViewGroup parent = (ViewGroup)root.findViewById(R.id.vaccination_name_layout).getParent().getParent().getParent();
        congratulationsView = createTextViewUI(R.string.congratulate_vaccine_uptodate, parent);

        int pad = (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP,16, getResources().getDisplayMetrics());
        int color = ContextCompat.getColor(root.getContext(), R.color.congratulation_color);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,  // Width
                RelativeLayout.LayoutParams.WRAP_CONTENT   // Height
        );
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);


        congratulationsView.setLayoutParams(params);
        congratulationsView.setVisibility(View.GONE);
        congratulationsView.setPadding(pad,pad,pad,pad*2);
        congratulationsView.setTextColor(color);

        params = (RelativeLayout.LayoutParams)root.findViewById(R.id.scroll_layout).getLayoutParams();
        params.addRule(RelativeLayout.BELOW, congratulationsView.getId());
        parent.addView(congratulationsView);
    }


    private LinearLayout createContainerForReasonsNoVaccines(ViewGroup parent){
        LinearLayout reasonsContainer = (LinearLayout)LayoutInflater.from(parent.getContext()).inflate(R.layout.reasons_no_vaccination_container,parent,false);
        reasonsContainer.setId(R.id.reasons_no_vaccines);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params.addRule(RelativeLayout.BELOW, R.id.checkbox_no_vaccination);
        reasonsContainer.setLayoutParams(params);

        params = (RelativeLayout.LayoutParams)parent.findViewById(R.id.vaccination_name_layout).getLayoutParams();
        params.addRule(RelativeLayout.BELOW, reasonsContainer.getId());
        return reasonsContainer;
    }

    private View createViewOptionForNoVaccines(KeyValue<String> reason, ViewGroup parent){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_vaccine_name_check,parent,false);
        CustomFontTextView label = view.findViewById(R.id.vaccine);
        CheckBox checkBox = view.findViewById(R.id.select);
        checkBox.setChecked(false);
        checkBox.setTag(reason.key);
        label.setText(reason.value);
        return view;
    }

    private CustomFontTextView createTextViewUI(int stringResource, ViewGroup parent){
        CustomFontTextView label = (CustomFontTextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.congratulate_has_all_vaccine,parent,false);
        label.setText(stringResource);
        label.setId(View.generateViewId());
        return label;
    }


    protected void onSelectingNoVaccination(boolean showReasons){
        new Handler().postDelayed(() ->{
            onSomeVaccineNotSelected(showReasons);
            root.findViewById(R.id.multiple_vaccine_date_pickerview).setVisibility(showReasons?View.GONE:View.VISIBLE);
            root.findViewById(R.id.single_vaccine_add_layout).setVisibility(showReasons?View.GONE:View.VISIBLE);
            root.findViewById(R.id.vaccination_name_layout).setVisibility(showReasons?View.GONE:View.VISIBLE);
            if(showReasons){
                new FnList<>(vaccineCheckboxes).forEachItem(ch-> ch.setChecked(false));
                congratulationsView.setVisibility(View.GONE);
            }


        }, 600);
    }
    private void onSomeVaccineNotSelected(boolean showReasons){
        new Handler().postDelayed(() ->{
            root.getRootView().findViewById(R.id.reasons_no_vaccines).setVisibility(showReasons?View.VISIBLE:View.GONE);
            root.findViewById(R.id.multiple_vaccine_date_pickerview).setVisibility(showReasons?View.GONE:View.VISIBLE);
        }, 600);
    }

    private FnList<CheckBox> getAllVaccineCheckboxes(){
        LinearLayout layout = root.findViewById(R.id.vaccination_name_layout);
        return FnList.generate(layout::getChildAt)
                .map(v->{
                    String label = v.findViewById(R.id.vaccine).toString();
                    CheckBox checkBox = v.findViewById(R.id.select);
                    checkBox.setTag(label);
                    checkBox.setOnClickListener(ch->this.onVaccineSelectStatusChange());
                    return checkBox;
                });
    }

    private FnList<VaccineValue> getVaccineValues(){
        FnList<VaccineValue> vaccineV = FnList
                .generate(vaccinesContainer::getChildAt)
                .map(v->new VaccineValue(v,this));

        if(datesContainer.getVisibility() == View.VISIBLE) {
            FnList.generate(datesContainer::getChildAt)
                  .forEachItem(view -> vaccineV.forEachItem(vc -> vc.setDateFromMultiMode(view)));
        }
        return vaccineV;
    }

    private void onVaccineSelectStatusChange(){
        Map<String,String> selectedVaccine = getVaccineValues()
                .filter(v->v.selected)
                .reduce(new HashMap<>(),(m,v)->{
                    m.put(v.name,v.date.toString());
                    return m;
                });

        boolean allSelected = vaccinesContainer.getChildCount() == selectedVaccine.size() && !selectedVaccine.isEmpty();
        new Handler().postDelayed(() ->congratulationsView.setVisibility(allSelected?View.VISIBLE:View.GONE) , 600);
        onSomeVaccineNotSelected(!allSelected);
        if(allSelected) uncheckReasonCheckbox();
    }

    protected void uncheckReasonCheckbox() {
        LinearLayout layout = root.getRootView().findViewById(R.id.reasons_no_vaccines);
        FnList.generate(layout::getChildAt)
                .forEachItem(v->{
                    CheckBox ch=v.findViewById(R.id.select);
                    ch.setChecked(false);
                });
    }
    protected FnList<String> getSelectedReasonsNoVaccines() {
        LinearLayout layout = root.getRootView().findViewById(R.id.reasons_no_vaccines);
        return FnList.generate(layout::getChildAt)
                .map(v->{
                    CheckBox ch=v.findViewById(R.id.select);
                    return ch == null || !ch.isChecked() ? "" : ch.getTag().toString();
                }).filter(s->!s.isEmpty());
    }

    private void save(View view) {
        super.onClick(view);

        List<String> selected=getSelectedReasonsNoVaccines().list();
        if(selected.isEmpty())return;

        JSONObject field = new FnList<>( new String[]{
                "key: reasons_no_vaccination",
                "openmrs_entity_parent: vaccine",
                "openmrs_entity: concept",
                "type: select",
                "openmrs_entity_id: reasons_no_vaccination"
        })
                .map(KeyValue::create)
                .reduce(new JSONObject(),(map,p)->{
                    map.put(p.key,p.value);
                    return map;
                });

        UtilsFlv.ex(()->field.put("value",selected));
        super.visitView.onDialogOptionUpdated(addField(field));
    }

     private String addField(JSONObject value){
         JSONObject json=super.getJsonObject();
         JSONArray fields= UtilsFlv.coalesce(UtilsFlv.jsonGet(json,"step1.fields"),new JSONArray());
         fields.put(value);
         return json.toString();
     }

    private static class VaccineValue {
        String name;
        Date date;
        boolean selected;
        VaccineValue(View view,BaseHomeVisitImmunizationFragmentFlv fg){
            CustomFontTextView tv = view.findViewById(R.id.vaccine);
            DatePicker dp = view.getRootView().findViewById(R.id.earlier_date_picker);
            CheckBox cb = view.findViewById(R.id.select);
            cb.setOnClickListener(v->fg.onVaccineSelectStatusChange());

            this.name = tv.getText().toString();
            this.selected = cb.isChecked();
            this.date = dp == null ? new Date() : UtilsFlv.getDateFromDatePicker(dp);
        }
        void setDateFromMultiMode(View view) {
            DatePicker dt = view.findViewById(R.id.earlier_date_picker);
            CustomFontTextView tx = view.findViewById(R.id.vaccines_given_when_title_question);
            if(tx.getText().toString().contains(name)){
                this.date = UtilsFlv.getDateFromDatePicker(dt);
            }
        }
    }
}


