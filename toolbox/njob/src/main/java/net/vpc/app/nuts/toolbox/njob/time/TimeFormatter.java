package net.vpc.app.nuts.toolbox.njob.time;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

public class TimeFormatter {
    boolean fuzzy=true;
    boolean daySuffix=true;

    protected String extractTimeName2(LocalTime d) {
        if (d.getMinute() == 0 && d.getHour() == 0) {
            return "00:00";
        }
        if (d.getMinute() == 59 && d.getHour() == 23) {
            return "midnight";
        }
        DecimalFormat df2 = new DecimalFormat("00");
        return df2.format(d.getHour()) + ":" + df2.format(d.getMinute());
    }

    protected String extractTodayTimeName(LocalTime d) {
        if (d.getMinute() == 0 && d.getHour() == 0) {
            return "00:00";
        }
        if (d.getMinute() == 59 && d.getHour() == 23) {
            return "midnight";
        }
        if (fuzzy) {
            LocalTime n = LocalTime.now();
            long u = ChronoUnit.MINUTES.between(n,d);
            if(Math.abs(u)<60*4){

                if (u > 0 && u < 600) {
                    return "in "+new TimePeriod(u, ChronoUnit.MINUTES).toTimePeriods().toString();
                }
                if (-u > 0 && -u < 600) {
                    return (-u) + "mn ago";
                }
            }
        }
        DecimalFormat df2 = new DecimalFormat("00");
        return df2.format(d.getHour()) + ":" + df2.format(d.getMinute());
    }

    protected boolean isToday(LocalDate d) {
        LocalDate n = LocalDateTime.now().toLocalDate();
        if (n.equals(d)) {
            return true;
        }
        return false;
    }

    protected String extractDateName(LocalDate d,String qualifier) {
        LocalDate n = LocalDateTime.now().toLocalDate();
        if (d.equals(n)) {
            if(qualifier!=null){
                if(qualifier.equals("midnight")){
                    return qualifier;
                }
                if(qualifier.equals("night")){
                    return "tonight";
                }
                return "this "+qualifier;
            }
            return "today";
        }
        if (d.equals(n.plus(-1, ChronoUnit.DAYS))) {
            String t = "yesterday";
            if(qualifier!=null){
                return "yesterday "+qualifier;
            }
            return t;
        }
        if (d.equals(n.plus(1, ChronoUnit.DAYS))) {
            String t = "tomorrow";
            if(qualifier!=null){
                return "tomorrow "+qualifier;
            }
            return t;
        }
        if (d.equals(n.plus(-2, ChronoUnit.DAYS))) {
            return "two days ago";
        }
        if (d.equals(n.plus(-3, ChronoUnit.DAYS))) {
            return "three days ago";
        }
        if (d.equals(n.plus(-4, ChronoUnit.DAYS))) {
            return "four days ago";
        }
        if (d.equals(n.plus(-5, ChronoUnit.DAYS))) {
            return "five days ago";
        }
        if (d.equals(n.plus(-6, ChronoUnit.DAYS))) {
            return "six days ago";
        }
        if (d.equals(n.plus(2, ChronoUnit.DAYS))) {
            return "in two days";
        }
        if (d.equals(n.plus(3, ChronoUnit.DAYS))) {
            return "in three days";
        }
        if (d.equals(n.plus(4, ChronoUnit.DAYS))) {
            return "in four days";
        }
        if (d.equals(n.plus(5, ChronoUnit.DAYS))) {
            return "in five days";
        }
        if (d.equals(n.plus(6, ChronoUnit.DAYS))) {
            return "in six days";
        }
        if (d.equals(n.plus(-7, ChronoUnit.DAYS))) {
            return "last week";
        }
        if (d.equals(n.plus(+7, ChronoUnit.DAYS))) {
            return "next week";
        }
        DecimalFormat df2 = new DecimalFormat("00");
        DecimalFormat df4 = new DecimalFormat("0000");
        return df4.format(d.getYear()) + "-" + df2.format(d.getMonth().getValue()) + "-" + df2.format(d.getDayOfMonth());
    }

    public String format(LocalDateTime d) {
        String r=null;
        if(d.getMinute()<=1 && d.getHour()==0){
            r= extractDateName(d.toLocalDate().minus(1,ChronoUnit.DAYS),"midnight");
        }else {
            boolean today = isToday(d.toLocalDate());
            int h = d.toLocalTime().getHour();
            String t = today ? extractTodayTimeName(d.toLocalTime()) : extractTimeName2(d.toLocalTime());
            if (t.equals("midnight")) {
                r= extractDateName(d.toLocalDate(), t);
            }else {
                String dd = extractDateName(d.toLocalDate(),
                        (h < 12) ? "morning" :
                                (h >= 14 && h < 18) ? "afternoon" :
                                (h >= 18 && h < 20) ? "evening" :
                                        (h >= 20) ? "night" : null
                );
                r = dd + " " + t;
            }
        }
        if(daySuffix){
            if(r.equals("midnight") && (d.getMinute()<=1 && d.getHour()==0)){
                r = r + " " + d.toLocalDate().minus(1,ChronoUnit.DAYS).getDayOfWeek().toString().toLowerCase().substring(0, 3);
            }else {
                r = r + " " + d.getDayOfWeek().toString().toLowerCase().substring(0, 3);
            }
        }
        return r;

    }
}
