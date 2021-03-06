package org.smartregister.chw.rule;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.smartregister.chw.core.contract.RegisterAlert;
import org.smartregister.chw.core.rule.ICommonRule;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.family.util.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AdolescentVisitAlertRule implements ICommonRule, RegisterAlert {

    private final int[] monthNames = {org.smartregister.chw.core.R.string.january, org.smartregister.chw.core.R.string.february, org.smartregister.chw.core.R.string.march, org.smartregister.chw.core.R.string.april, org.smartregister.chw.core.R.string.may, org.smartregister.chw.core.R.string.june, org.smartregister.chw.core.R.string.july, org.smartregister.chw.core.R.string.august, org.smartregister.chw.core.R.string.september, org.smartregister.chw.core.R.string.october, org.smartregister.chw.core.R.string.november, org.smartregister.chw.core.R.string.december};
    public String buttonStatus = CoreConstants.VisitType.DUE.name();
    public String noOfMonthDue;
    public String noOfDayDue;
    public String visitMonthName;
    private LocalDate dateCreated;
    private final LocalDate todayDate;
    private LocalDate lastVisitDate;
    private LocalDate visitNotDoneDate;
    private final Integer yearOfBirth;
    private final Context context;
    private boolean visitDoneLessThan24hours = false;


    public AdolescentVisitAlertRule(Context context, String yearOfBirthString, long lastVisitDateLong, long visitNotDoneValue, long dateCreatedLong) {
        yearOfBirth = Utils.getAgeFromDate(yearOfBirthString);
        this.context = context;

        this.todayDate = new LocalDate();
        visitDoneLessThan24hours = false;
        if (lastVisitDateLong > 0) {
            int hoursSinceLastVisit = (int) ((new Date().getTime() - lastVisitDateLong) / (60*60*1000));
            this.lastVisitDate = new LocalDate(lastVisitDateLong);
            if(hoursSinceLastVisit > 24) {
                noOfDayDue = dayDifference(lastVisitDate, todayDate) + " " + context.getString(org.smartregister.chw.core.R.string.days);
            } else {
                visitDoneLessThan24hours = true;
                noOfDayDue = context.getString(org.smartregister.chw.core.R.string.less_than_twenty_four);
            }
        } else {
            noOfDayDue = dayDifference(new LocalDate(dateCreatedLong), todayDate) + " "+ context.getString(org.smartregister.chw.core.R.string.days);
        }

        if (visitNotDoneValue > 0) {
            this.visitNotDoneDate = new LocalDate(visitNotDoneValue);
        }

        if (dateCreatedLong > 0) {
            this.dateCreated = new LocalDate(dateCreatedLong);
        }

    }

    private int dayDifference(LocalDate date1, LocalDate date2) {
        return Days.daysBetween(date1, date2).getDays();
    }

    public boolean isVisitNotDone() {
        return (visitNotDoneDate != null && getMonthsDifference(visitNotDoneDate, todayDate) < 1);
    }

    private int getMonthsDifference(LocalDate date1, LocalDate date2) {
        return Months.monthsBetween(
                date1.withDayOfMonth(1),
                date2.withDayOfMonth(1)).getMonths();
    }

    public boolean isExpiry(Integer calYr) {
        return (yearOfBirth != null && yearOfBirth > calYr);
    }

    public boolean isOverdueWithinMonth(Integer value) {
        LocalDate overdue = LocalDate.parse(new SimpleDateFormat("yyyy-MM-dd").format(getOverDueDate()));
        int diff = getMonthsDifference(overdue, todayDate);
        if (diff >= value) {
            noOfMonthDue = diff + StringUtils.upperCase(context.getString(org.smartregister.chw.core.R.string.abbrv_months));
            return true;
        }
        return false;
    }

    public boolean isDueWithinMonth() {
        if (todayDate.getDayOfMonth() == 1) {
            return true;
        }
        if (lastVisitDate == null) {
            return !isVisitThisMonth(dateCreated, todayDate);
        }

        return !isVisitThisMonth(lastVisitDate, todayDate);
    }

    private boolean isVisitThisMonth(LocalDate lastVisit, LocalDate todayDate) {
        return getMonthsDifference(lastVisit, todayDate) < 1;
    }

    public boolean isVisitWithinTwentyFour() {
        visitMonthName = theMonth(todayDate.getMonthOfYear() - 1);
        return visitDoneLessThan24hours;
    }

    private String theMonth(int month) {
        return context.getResources().getString(monthNames[month]);
    }

    public boolean isVisitWithinThisMonth() {
        return (lastVisitDate != null) && isVisitThisMonth(lastVisitDate, todayDate);
    }

    public Date getOverDueDate() {
        if (lastVisitDate == null) {
            return (visitNotDoneDate != null) ? visitNotDoneDate.toDate() : getLastDayOfMonth(dateCreated.toDate());
        } else {
            if (visitNotDoneDate == null || lastVisitDate.isAfter(visitNotDoneDate)) {
                int monthsDiff = getMonthsDifference(lastVisitDate, todayDate);
                return monthsDiff > 1 ? getLastDayOfMonth(lastVisitDate.plusMonths(6).toDate()) : getLastDayOfMonth(todayDate.plusMonths(6).toDate());
            } else if (visitNotDoneDate != null && visitNotDoneDate.isAfter(lastVisitDate)) {
                return visitNotDoneDate.toDate();
            } else {
                return getLastDayOfMonth(lastVisitDate.plusMonths(6).toDate());
            }
        }
    }

    private Date getFirstDayOfMonth(Date refDate) {
        return new DateTime(refDate).withDayOfMonth(1).toDate();
    }

    protected Date getLastDayOfMonth(Date refDate) {
        DateTime first = new DateTime(refDate).withDayOfMonth(1);
        return first.plusMonths(1).minusDays(1).toDate();
    }

    @Override
    public String getNumberOfMonthsDue() {
        return noOfMonthDue;
    }

    @Override
    public String getNumberOfDaysDue() {
        return noOfDayDue;
    }

    @Override
    public String getVisitMonthName() {
        return visitMonthName;
    }

    @Override
    public String getRuleKey() {
        return "adolescentVisitAlertRule";
    }

    @Override
    public String getButtonStatus() {
        return buttonStatus;
    }

    public Date getDueDate() {
        Date lastDueDate = getLastDueDate();

        if (lastDueDate.getTime() < getFirstDayOfMonth(new Date()).getTime()) {
            return getFirstDayOfMonth(new Date());
        } else {
            return lastDueDate;
        }
    }

    private Date getLastDueDate() {
        if (lastVisitDate != null && getFirstDayOfMonth(lastVisitDate.toDate()).getTime() > dateCreated.toDate().getTime()) {
            return getFirstDayOfMonth(lastVisitDate.plusMonths(6).toDate());
        } else {
            return dateCreated.toDate();
        }
    }

    public Date getNotDoneDate() {
        if (getCompletionDate() == null && visitNotDoneDate != null) {
            return visitNotDoneDate.toDate().getTime() > getDueDate().getTime() ? visitNotDoneDate.toDate() : null;
        }
        return null;
    }

    public Date getCompletionDate() {
        if (lastVisitDate != null && lastVisitDate.toDate().getTime() >= getDueDate().getTime()) {
            return lastVisitDate.toDate();
        }
        return null;
    }

    // Expiry date 1 month after overdue date
    public Date getExpiryDate() {
        return getLastDayOfMonth(new DateTime(getOverDueDate()).plusMonths(1).toDate());
    }
}
