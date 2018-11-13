package com.ivorybridge.moabi.util;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class FormattedTime {
    
    public FormattedTime() {
    }

    public String getCurrentTimeAsHHMM() {
        DateFormat df = new SimpleDateFormat("HH:mm", Locale.US);
        return df.format(Calendar.getInstance().getTime());
    }

    public String getCurrentTimeAsHMMA() {
        DateFormat df = new SimpleDateFormat("h:mm a", Locale.US);
        return df.format(Calendar.getInstance().getTime());
    }

    public Long getCurrentTimeInMilliSecs() {
        LocalDateTime now = LocalDateTime.now();
        ZonedDateTime zdt = now.atZone(ZoneId.systemDefault());
        return zdt.toInstant().toEpochMilli();
    }

    public String getCurrentDateAsYYYYMMDD() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return mdformat.format(calendar.getTime());
    }

    public String getYesterdaysDateAsYYYYMMDD() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return mdformat.format(calendar.getTime());
    }

    public Long getStartOfDay(String date) {

        DateTimeFormatter dateFormatter
                = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US);
        return LocalDate.parse(date, dateFormatter)
                .atStartOfDay(ZoneOffset.systemDefault())
                .toInstant()
                .toEpochMilli();
    }

    public Long getEndOfDay(String date) {
        DateTimeFormatter dateFormatter
                = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US);
        return LocalDate.parse(date, dateFormatter)
                .atStartOfDay(ZoneOffset.systemDefault())
                .plusDays(1L)
                .minusNanos(1L)
                .toInstant()
                .toEpochMilli();
    }

    public Long getStartOfWeek(String date) {
        DateTimeFormatter dateFormatter
                = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US);
        return LocalDate.parse(date, dateFormatter)
                .atStartOfDay(ZoneOffset.systemDefault())
                .minusWeeks(1)
                .toInstant()
                .toEpochMilli();
    }

    public Long getStartOfDayBeforeSpecifiedNumberOfDays(String date, int numberOfDays) {
        DateTimeFormatter dateFormatter
                = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US);
        return LocalDate.parse(date, dateFormatter)
                .atStartOfDay(ZoneOffset.systemDefault())
                .minusDays(numberOfDays)
                .toInstant()
                .toEpochMilli();
    }

    public String getDateBeforeSpecifiedNumberOfDaysAsYYYYMMDD(String date, int numberOfDays) {
        DateTimeFormatter dateFormatter
                = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US);
        return LocalDate.parse(date, dateFormatter)
                .minusDays(numberOfDays)
                .format(dateFormatter);
    }

    public String getDateBeforeSpecifiedNumberOfDaysAsMMDD(String date, int numberOfDays) {
        DateTimeFormatter dateFormatter
                = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US);
        DateTimeFormatter newDateFormatter = DateTimeFormatter.ofPattern("MM-dd", Locale.US);
        return LocalDate.parse(date, dateFormatter)
                .minusDays(numberOfDays)
                .format(newDateFormatter);
    }

    public String getDateBeforeSpecifiedNumberOfDaysAsEEE(String date, int numberOfDays) {
        DateTimeFormatter dateFormatter
                = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US);
        DateTimeFormatter newDateFormatter = DateTimeFormatter.ofPattern("EEE", Locale.US);
        return LocalDate.parse(date, dateFormatter)
                .minusDays(numberOfDays)
                .format(newDateFormatter);
    }

    public String getDateBeforeSpecifiedNumberOfWeeksAsYYYYW(String date, int numberOfWeeks) {
        DateTimeFormatter dateFormatter
                = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US);
        Long time = LocalDate.parse(date, dateFormatter)
                .minusWeeks(numberOfWeeks)
                .atStartOfDay(ZoneOffset.systemDefault())
                .toInstant().toEpochMilli();
        Date newDate = new Date(time);
        Calendar cal = Calendar.getInstance();
        cal.setTime(newDate);
        int weekOfYear = cal.get(Calendar.WEEK_OF_YEAR);
        DateFormat df = new SimpleDateFormat("yyyy", Locale.US);
        if (weekOfYear < 10) {
            return df.format(newDate) + "-0"+weekOfYear;
        } else {
            return df.format(newDate) + "-"+weekOfYear;
        }
    }

    public String getDateBeforeSpecifiedNumberOfMonthsAsYYYYMM(String date, int numberOfMonths) {
        DateTimeFormatter dateFormatter
                = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US);
        DateTimeFormatter newDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM", Locale.US);
        return LocalDate.parse(date, dateFormatter)
                .minusMonths(numberOfMonths)
                .format(newDateFormatter);

    }

    public Long getEndOfDayBeforeSpecificedNumberOfDays(String date, int numberOfDays) {
        DateTimeFormatter dateFormatter
                = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US);
        return LocalDate.parse(date, dateFormatter)
                .atStartOfDay(ZoneOffset.systemDefault())
                .plusDays(1L)
                .minusNanos(1L)
                .minusDays(numberOfDays)
                .toInstant()
                .toEpochMilli();
    }

    public Long getStartOfMonth(String date) {
        DateTimeFormatter dateFormatter
                = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US);
        return LocalDate.parse(date, dateFormatter)
                .atStartOfDay(ZoneOffset.systemDefault())
                .minusMonths(1)
                .toInstant()
                .toEpochMilli();
    }

    public String convertLongToHHMM(Long time) {
        Date date = new Date(time);
        DateFormat df = new SimpleDateFormat("HH:mm", Locale.US);
        return df.format(date);
    }

    public String convertLongToHHMMSS(Long time) {
        Date date = new Date(time);
        DateFormat df = new SimpleDateFormat("HH:mm:ss", Locale.US);
        return df.format(date);
    }

    public String convertLongToHHMMaa(Long time) {
        Date date = new Date(time);
        DateFormat df = new SimpleDateFormat("hh:mm aa", Locale.US);
        return df.format(date);
    }

    public String convertLongToMDHHMMaa(Long time) {
        Date date = new Date(time);
        DateFormat df = new SimpleDateFormat("M/d hh:mm aa", Locale.US);
        return df.format(date);
    }

    public String convertLongToYYYYMMDD(Long time) {
        Date date = new Date(time);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return df.format(date);
    }

    public String convertLongToMMDD(Long time) {
        Date date = new Date(time);
        DateFormat df = new SimpleDateFormat("MM-dd", Locale.US);
        return df.format(date);
    }

    public String convertLongToMD(Long time) {
        Date date = new Date(time);
        DateFormat df = new SimpleDateFormat("M/d", Locale.US);
        return df.format(date);
    }

    public String convertLongToYYYYW(Long time) {
        Date date = new Date(time);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int weekOfYear = cal.get(Calendar.WEEK_OF_YEAR);
        DateFormat df = new SimpleDateFormat("yyyy", Locale.US);
        if (weekOfYear < 10) {
            return df.format(date) + "-0"+weekOfYear;
        } else {
            return df.format(date) + "-"+weekOfYear;
        }
    }

    public String convertLongToYYYYMM(Long time) {
        Date date = new Date(time);
        DateFormat df = new SimpleDateFormat("yyyy-MM", Locale.US);
        return df.format(date);
    }

    public String convertLongToMMMD(Long time) {
        Date date = new Date(time);
        DateFormat df = new SimpleDateFormat("MMM d", Locale.US);
        return df.format(date);
    }

    public String convertLongToMMMYYYY(Long time) {
        Date date = new Date(time);
        DateFormat df = new SimpleDateFormat("MMM, yyyy", Locale.US);
        return df.format(date);
    }

    public String convertStringHMToHMMAA(String date) {
        DateFormat df = new SimpleDateFormat("h:m", Locale.US);
        DateFormat df2 = new SimpleDateFormat("h:mm aa", Locale.US);
        try {
            Date d = df.parse(date);
            return df2.format(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public String convertStringMMDDToMMMDD(String date) {
        DateFormat df = new SimpleDateFormat("MM-dd", Locale.US);
        DateFormat df2 = new SimpleDateFormat("MMM dd", Locale.US);
        try {
            Date d = df.parse(date);
            return df2.format(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public String convertStringMMDDToDD(String date) {
        DateFormat df = new SimpleDateFormat("MM-dd", Locale.US);
        DateFormat df2 = new SimpleDateFormat("dd", Locale.US);
        try {
            Date d = df.parse(date);
            return df2.format(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public String convertStringYYYYMMDDToMMMDD(String date) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        DateFormat df2 = new SimpleDateFormat("MMM dd", Locale.US);
        try {
            Date d = df.parse(date);
            return df2.format(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }



    public String convertStringYYYYMMDDToMMMD(String date) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        DateFormat df2 = new SimpleDateFormat("MMM d", Locale.US);
        try {
            Date d = df.parse(date);
            return df2.format(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public String convertStringYYYYMMDDToE(String date) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        DateFormat df2 = new SimpleDateFormat("E", Locale.US);
        try {
            Date d = df.parse(date);
            return df2.format(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public String convertStringYYYYMMDDToEEE(String date) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        DateFormat df2 = new SimpleDateFormat("EEE", Locale.US);
        try {
            Date d = df.parse(date);
            return df2.format(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public String convertStringEEEToE(String date) {
        DateFormat df = new SimpleDateFormat("EEE", Locale.US);
        DateFormat df2 = new SimpleDateFormat("E", Locale.US);
        try {
            Date d = df.parse(date);
            return df2.format(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public String convertStringYYYYMMDDToMD(String date) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        DateFormat df2 = new SimpleDateFormat("M/d", Locale.US);
        try {
            Date d = df.parse(date);
            return df2.format(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }


    public String convertStringYYYYMMToMMM(String date) {
        DateFormat df = new SimpleDateFormat("yyyy-MM", Locale.US);
        DateFormat df2 = new SimpleDateFormat("MMM", Locale.US);
        try {
            Date d = df.parse(date);
            return df2.format(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public String convertStringYYYYMMToMMMYYYY(String date) {
        DateFormat df = new SimpleDateFormat("yyyy-MM", Locale.US);
        DateFormat df2 = new SimpleDateFormat("MMM, yyyy", Locale.US);
        try {
            Date d = df.parse(date);
            return df2.format(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public long convertStringYYYYMMDDToLong(String date) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        try {
            Date d = df.parse(date);
            return d.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public long convertStringYYYYMMDDhhmmToLong(String date) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.US);
        try {
            Date d = df.parse(date);
            return d.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public long getStartTimeOfYYYYW(int week, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.WEEK_OF_YEAR, week);
        calendar.set(Calendar.YEAR, year);
        Date startDate = calendar.getTime();
        return startDate.getTime();
    }

    public long getEndTimeOfYYYYW(int week, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.WEEK_OF_YEAR, week);
        calendar.set(Calendar.YEAR, year);
        calendar.add(Calendar.DATE, 6);
        Date enddate = calendar.getTime();
        return enddate.getTime();
    }

    public long getStartTimeOfYYYYMM(String date) {
        DateFormat df = new SimpleDateFormat("yyyy-MM", Locale.US);
        try {
            Date d = df.parse(date);
            Calendar cal = Calendar.getInstance();
            cal.clear();
            cal.setTime(d);
            Date startDate = cal.getTime();
            return startDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public long getEndTimeOfYYYYMM(String date) {
        DateFormat df = new SimpleDateFormat("yyyy-MM", Locale.US);
        try {
            Date d = df.parse(date);
            Calendar cal = Calendar.getInstance();
            cal.clear();
            cal.setTime(d);
            cal.add(Calendar.MONTH, 1);
            cal.add(Calendar.DAY_OF_MONTH, -1);
            Date endDate = cal.getTime();
            return endDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
