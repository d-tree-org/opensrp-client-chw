package org.smartregister.chw.actionhelper;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.R;
import org.smartregister.chw.anc.actionhelper.HomeVisitActionHelper;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.anc.util.JsonFormUtils;
import org.smartregister.immunization.domain.ServiceWrapper;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

public class ChildPlayAssessmentCounselingActionHelper extends HomeVisitActionHelper {
    private final Context context;

    private final String visitId;

    private final Map<String, Boolean> visitNumberMap = new HashMap<>();

    private final ServiceWrapper serviceWrapper;

    private String jsonString;

    private String spend_time_with;

    public ChildPlayAssessmentCounselingActionHelper(Context context, String visitId, ServiceWrapper serviceWrapper) {
        this.context = context;
        this.visitId = visitId;
        this.serviceWrapper = serviceWrapper;
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            spend_time_with = org.smartregister.chw.util.JsonFormUtils.getCheckBoxValue(jsonObject, "spend_time_with");
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    @Override
    public void onJsonFormLoaded(String jsonString, Context context, Map<String, List<VisitDetail>> details) {
        this.jsonString = jsonString;
    }

    @Override
    public String getPreProcessed() {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray fields = JsonFormUtils.fields(jsonObject);
            populateVisitNumber();
            for (Map.Entry<String, Boolean> entry : visitNumberMap.entrySet()) {
                if (entry.getValue()) {
                    JsonFormUtils.getFieldJSONObject(fields, entry.getKey()).put("value", "true");
                }
            }
            String pages = bangoKitataPages();
            if (!pages.isEmpty()) {
                JSONObject bango_kitita_reference_skip_logic = JsonFormUtils.getFieldJSONObject(fields, "bango_kitita_reference_skip_logic");
                if (bango_kitita_reference_skip_logic != null) {
                    bango_kitita_reference_skip_logic.put("value", "true");
                }
                JSONObject bango_kitita_reference = JsonFormUtils.getFieldJSONObject(fields, "bango_kitita_reference");
                if (bango_kitita_reference != null) {
                    bango_kitita_reference.put("text", MessageFormat.format("{0}: {1}", context.getString(R.string.bango_kitita_message), pages));
                }
            }
            return jsonObject.toString();
        } catch (JSONException e) {
            Timber.e(e);
        }
        return super.getPreProcessed();
    }

    @Override
    public String evaluateSubTitle() {
        return null;
    }

    @Override
    public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
        if (StringUtils.isBlank(spend_time_with)) {
            return BaseAncHomeVisitAction.Status.PENDING;
        } else {
            return BaseAncHomeVisitAction.Status.COMPLETED;
        }
    }

    private void populateVisitNumber() {
        int visitNumber = visitNumber();
        if (visitNumber == 2) {
            visitNumberMap.put("visit_2_visit_25", true);
        } else if (visitNumber >= 3 && visitNumber <= 16) {
            visitNumberMap.put("visit_3_visit_16", true);
            visitNumberMap.put("visit_3_visit_17", true);
            visitNumberMap.put("visit_2_visit_25", true);
            visitNumberMap.put("visit_3_visit_25", true);
        } else if (visitNumber == 17) {
            visitNumberMap.put("visit_3_visit_17", true);
            visitNumberMap.put("visit_2_visit_25", true);
            visitNumberMap.put("visit_3_visit_25", true);
            visitNumberMap.put("visit_17_visit_25", true);
        } else if (visitNumber > 17 && visitNumber <= 25) {
            visitNumberMap.put("visit_2_visit_25", true);
            visitNumberMap.put("visit_3_visit_25", true);
            visitNumberMap.put("visit_17_visit_25", true);
        }
    }

    private int visitNumber() {
        if (this.visitId != null) {
            return getPncHomeVisitNumber();
        } else {
            return getChildHomeVisitNumber();
        }
    }

    private int getPncHomeVisitNumber() {
        switch (this.visitId) {
            case "1":
                return 1;
            case "3":
                return 2;
            case "8":
                return 3;
            case "21 - 27":
                return 4;
            case "35 - 41":
                return 5;
            default:
                return 0;
        }
    }

    private int getChildHomeVisitNumber() {
        final Pattern lastIntPattern = Pattern.compile("[^0-9]+([0-9]+)$");
        Matcher matcher = lastIntPattern.matcher(serviceWrapper.getName());
        if (matcher.find()) {
            String someNumberStr = matcher.group(1);
            if (someNumberStr != null) {
                return Integer.parseInt(someNumberStr);
            }
        }
        return 0;
    }

    private String bangoKitataPages() {
        int visitNumber = visitNumber();
        Map<Integer, String> bangoKititaPages = new HashMap<>();
        bangoKititaPages.put(3, String.format(context.getString(R.string.bango_kitita_two_pages), "17", "19"));
        bangoKititaPages.put(4, String.format(context.getString(R.string.bango_kitita_one_page), "21"));
        bangoKititaPages.put(5, String.format(context.getString(R.string.bango_kitita_one_page), "27"));
        bangoKititaPages.put(6, String.format(context.getString(R.string.bango_kitita_two_pages), "33", "37"));
        bangoKititaPages.put(7, String.format(context.getString(R.string.bango_kitita_two_pages), "39", "43"));
        bangoKititaPages.put(8, String.format(context.getString(R.string.bango_kitita_two_pages), "45", "49"));
        bangoKititaPages.put(9, String.format(context.getString(R.string.bango_kitita_two_pages), "51", "57"));
        bangoKititaPages.put(10, String.format(context.getString(R.string.bango_kitita_two_pages), "59", "61"));
        bangoKititaPages.put(11, String.format(context.getString(R.string.bango_kitita_two_pages), "65", "67"));
        bangoKititaPages.put(12, String.format(context.getString(R.string.bango_kitita_one_page), "75"));
        bangoKititaPages.put(13, String.format(context.getString(R.string.bango_kitita_one_page), "79"));
        bangoKititaPages.put(14, String.format(context.getString(R.string.bango_kitita_one_page), "85"));
        bangoKititaPages.put(15, String.format(context.getString(R.string.bango_kitita_one_page), "93"));
        String pages = bangoKititaPages.get(visitNumber);
        return (pages != null) ? pages : "";
    }
}
