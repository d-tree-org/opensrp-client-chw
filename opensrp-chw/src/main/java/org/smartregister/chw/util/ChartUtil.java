package org.smartregister.chw.util;

import android.graphics.Color;

/**
 * The ChartUtil provides util functions to work with Charts
 *
 * @author allan
 */
public class ChartUtil {

    // Sample indicator keys
    public static String numericIndicatorKey = "S_IND_001";
    public static String pieChartYesIndicatorKey = "S_IND_002";
    public static String pieChartNoIndicatorKey = "S_IND_001";

    public static String currentMonthRegistrationsIndicatorKey = "CURR_MONTH_REG";
    public static String lastMonthRegistrationsIndicatorKey = "LAST_MONTH_REG";
    public static String currentMonthVisitsIndicatorKey = "CURR_MONTH_VISITS";
    public static String lastsMonthVisitsIndicatorKey = "LAST_MONTH_VISITS";

    // Color definitions for the chart slices. This could essentially be defined in colors.xml
    public static final int YES_GREEN_SLICE_COLOR = Color.parseColor("#99CC00");
    public static final int NO_RED_SLICE_COLOR = Color.parseColor("#FF4444");
}
