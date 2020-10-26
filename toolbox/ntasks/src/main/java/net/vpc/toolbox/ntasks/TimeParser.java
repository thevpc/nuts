package net.vpc.toolbox.ntasks;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeParser {
    boolean parseTimes=true;
    boolean parseDates=true;
//    public static void main(String[] args) {
//        System.out.println(new TimeParser().parseInstant("next tuesday 15h"));
//    }

    public Instant parseInstant(String a) {
        return parseInstant(new PatternStringBuilder(a));
    }

    public Instant parseInstant(PatternStringBuilder a) {
        try {
            if (a.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
                return Instant.parse(a + "T00:00:00Z");
            }
            if (a.matches("[0-9]{2}[hH][0-9]{2}")) {
                return Instant.parse(new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "T" + a.toString().replace("[hH]", ":") + "Z");
            }
            if (a.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}")) {
                return Instant.parse(a + ":00Z");
            }
            if (a.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}[T@][0-9]{2}:[0-9]{2}")) {
                return Instant.parse(a.toString().replace('@', 'T') + ":00Z");
            }
            if (a.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}[@T][0-9]{2}:[0-9]{2}:[0-9]{2}")) {
                return Instant.parse(a.toString().replace('@', 'T') + "Z");
            }

            if (a.matches("[a-zA-Z]+ [0-9]{2}:[0-9]{2}:[0-9]{2}")) {
                return Instant.parse(a.toString().replace('@', 'T') + "Z");
            }
            String s;
            String day = null;
            String time = null;
            boolean future = false;
            while (!a.isEmpty()) {
                if (parseDates && (s = a.readMatchAnyCase("today|yesterday|tomorrow|sunday|monday|tuesday|wednesday|thursday|friday|friday|saturday")) != null) {
                    day = s;
                } else if ((s = a.readMatchAnyCase("next")) != null) {
                    future = true;
                } else if ((s = a.readMatchAnyCase("last")) != null) {
                    future = false;
                } else if ((s = a.readMatchAnyCase("this")) != null) {
                    day="today";
                } else if ((s = a.readMatchAnyCase("morning|afternoon")) != null) {
                    time = s.equalsIgnoreCase("morning") ? "08:00:00" :
                            s.equalsIgnoreCase("afternoon") ? "14:00:00" :
                                    "00:00:00";
                } else if ((s = a.readMatchAnyCase("[0-9]{2}:[0-9]{2}:[0-9]{2}")) != null) {
                    time = s;
                } else if ((s = a.readMatchAnyCase("[0-9]{2}[hH][0-9]{2}")) != null) {
                    time = s.replace("[hH]", ":") + ":00";
                } else if ((s = a.readMatchAnyCase("[0-9]{1,2}[hH]")) != null) {
                    time = s.replace("[hH]", ":") + ":00:00";
                    if(time.length()==7){
                        time="0"+time;
                    }
                } else if ((s = a.readMatchAnyCase("[0-9]{1,2}am")) != null) {
                    int i = Integer.parseInt(s.substring(0, s.length() - 2));
                    if(i==12){
                        i=0;
                    }
                    time = i + ":00:00";
                    if(time.length()==7){
                        time="0"+time;
                    }
                } else if ((s = a.readMatchAnyCase("[0-9]{1,2}pm")) != null) {
                    int t = Integer.parseInt(s.substring(0, s.length() - 2));
                    t=t+12;
                    if(t==24){
                        t=0;
                    }
                    time = t + ":00:00";
                    if(time.length()==7){
                        time="0"+time;
                    }
                } else {
                    if (!a.trimStart()) {
                        throw new IllegalArgumentException("Unexpected " + a);
                    }
                }
            }
            Calendar c = Calendar.getInstance();
            c.set(Calendar.MILLISECOND, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.HOUR, 0);
            if (day != null) {
                switch (day.toLowerCase()) {
                    case "today": {
                        break;
                    }
                    case "yesterday": {
                        c.add(Calendar.DAY_OF_YEAR, -1);
                        break;
                    }
                    case "tomorrow": {
                        c.add(Calendar.DAY_OF_YEAR, 1);
                        break;
                    }
                    case "sunday": {
                        reachDayOfWeek(future, c, Calendar.SUNDAY);
                        break;
                    }
                    case "monday": {
                        reachDayOfWeek(future, c, Calendar.MONDAY);
                        break;
                    }
                    case "tuesday": {
                        reachDayOfWeek(future, c, Calendar.TUESDAY);
                        break;
                    }
                    case "wednesday": {
                        reachDayOfWeek(future, c, Calendar.WEDNESDAY);
                        break;
                    }
                    case "thursday": {
                        reachDayOfWeek(future, c, Calendar.THURSDAY);
                        break;
                    }
                    case "friday": {
                        reachDayOfWeek(future, c, Calendar.FRIDAY);
                        break;
                    }
                    case "saturday": {
                        reachDayOfWeek(future, c, Calendar.SATURDAY);
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException("Unsupported");
                    }
                }
                if (time != null) {
                    c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.substring(0, 2), 10));
                    c.set(Calendar.MINUTE, Integer.parseInt(time.substring(3, 5), 10));
                    c.set(Calendar.SECOND, Integer.parseInt(time.substring(6, 8), 10));
                }
                return c.toInstant();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static void reachDayOfWeek(boolean future, Calendar c, int dayOfWeek) {
        if (future) {
            while (c.get(Calendar.DAY_OF_WEEK) != dayOfWeek) {
                c.add(Calendar.DAY_OF_YEAR, 1);
            }
        } else {
            while (c.get(Calendar.DAY_OF_WEEK) != dayOfWeek) {
                c.add(Calendar.DAY_OF_YEAR, -1);
            }
        }
    }

    private boolean isWordChar(char wc) {
        return Character.isAlphabetic(wc);
    }

    private boolean isWord(String w, String where, int from) {
        if (from > 0 && isWordChar(where.charAt(from - 1))) {
            return false;
        }
        if (from + w.length() < where.length() && isWordChar(where.charAt(from - w.length()))) {
            return false;
        }
        return true;
    }

    private int indexOfWord(String w, String where) {
        int i = 0;
        while (i < where.length()) {
            int t = where.indexOf(w, i);
            if (isWord(w, where, t)) {
                return t;
            }
            i = t + w.length();
        }
        return -1;
    }

    public TimeParser setTimeOnly(boolean b) {
        this.parseDates=false;
        return this;
    }

    static class PatternStringBuilder {
        StringBuilder value = new StringBuilder();

        public PatternStringBuilder(String s) {
            value.append(s);
        }

        boolean matches(String pattern) {
            return value.toString().matches(pattern);
        }

        String readMatch(String pattern) {
            Pattern r = Pattern.compile("^(?<x>(" + pattern + ").*");
            Matcher m = r.matcher(value.toString());
            if (m.find()) {
                String y = m.group("x");
                value.delete(0, y.length());
                return y;
            }
            return null;
        }

        String readMatchAnyCase(String pattern) {
            Pattern r = Pattern.compile("^(?<x>(" + pattern + "))($|(\\W.*))", Pattern.CASE_INSENSITIVE);
            Matcher m = r.matcher(value.toString());
            if (m.find()) {
                String y = m.group("x");
                value.delete(0, y.length());
                return y;
            }
            return null;
        }

        @Override
        public String toString() {
            return value.toString();
        }

        boolean isEmpty() {
            return value.length() == 0;
        }

        public boolean trimStart() {
            if (value.length() > 0) {
                int ok = 0;
                while (ok < value.length() && value.charAt(ok) <= ' ') {
                    ok++;
                }
                if (ok > 0) {
                    value.delete(0, ok);
                    return true;
                }
            }
            return false;
        }
    }
}
