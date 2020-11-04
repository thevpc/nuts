package net.thevpc.nuts.toolbox.njob.time;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimePeriod implements Comparable<TimePeriod> {
    private static Pattern PATTERN = Pattern.compile("(?<val>[0-9]+([.][0-9]+)?)[ ]*(?<unit>([a-zA-Z]+))");
    private double count;
    private ChronoUnit unit;

    //    public static void main(String[] args) {
//        System.out.println("1ms".matches("(<?val>([0-9]+([.][0-9]+)?))[ ]*(<?unit>([a-zA-Z]+))"));
//        System.out.println("1".matches("(?<a>[0-9])"));
//    }
    public TimePeriod(double count, ChronoUnit unit) {
        this.count = count;
        this.unit = unit;
    }

    public static Instant parseOpPeriodAsInstant(String str, Instant initial,boolean lenient) {
        if(str.startsWith("+")) {
            TimePeriod parse = TimePeriod.parse(str.substring(1), lenient);
            if(lenient){
                return null;
            }
            return parse
                    .addTo(initial, TimespanPattern.WORK);
        }else if(str.startsWith("-")){
            TimePeriod parse = TimePeriod.parse(str.substring(1), lenient);
            return parse
                    .neg()
                    .addTo(initial, TimespanPattern.WORK);
        }else {
            return new TimeParser().parseInstant(str,lenient);
        }
    }

    public static Predicate<TimePeriod> parseFilter(String s, boolean lenient) {
        if (s.endsWith(" ago")) {
            TimePeriod p = TimePeriod.parse(s.substring(0, s.length() - " ago".length()), lenient);
            if (p == null) {
                return null;
            }
            return x -> x != null && x.compareTo(p) >= 0;
        }
        if (s.startsWith(">=")) {
            TimePeriod p = TimePeriod.parse(s.substring(2), lenient);
            if (p == null) {
                return null;
            }
            return x -> x != null && x.compareTo(p) >= 0;
        } else if (s.startsWith(">")) {
            TimePeriod p = TimePeriod.parse(s.substring(1), lenient);
            if (p == null) {
                return null;
            }
            return x -> x != null && x.compareTo(p) > 0;
        } else if (s.startsWith("<=")) {
            TimePeriod p = TimePeriod.parse(s.substring(2), lenient);
            if (p == null) {
                return null;
            }
            return x -> x != null && x.compareTo(p) <= 0;
        } else if (s.startsWith("<")) {
            TimePeriod p = TimePeriod.parse(s.substring(1), lenient);
            if (p == null) {
                return null;
            }
            return x -> x != null && x.compareTo(p) < 0;
        } else {
            TimePeriod p = TimePeriod.parse(s, lenient);
            if (p == null) {
                return null;
            }
            return x -> x != null && x.compareTo(p) == 0;
        }
    }

    public static ChronoUnit parseUnit(String u, boolean lenient) {
        switch (u.toLowerCase()) {
            case "ns":
            case "nanos":
            case "nano":
            case "nanosecond":
            case "nanoseconds": {
                return ChronoUnit.NANOS;
            }
            case "ms":
            case "milli":
            case "millis":
            case "millisecond":
            case "milliseconds": {
                return ChronoUnit.MILLIS;
            }
            case "s":
            case "sec":
            case "secs":
            case "second":
            case "seconds": {
                return ChronoUnit.SECONDS;
            }
            case "mn":
            case "min":
            case "mins":
            case "minute":
            case "minutes": {
                return ChronoUnit.MINUTES;
            }
            case "h":
            case "hour":
            case "hours": {
                return ChronoUnit.HOURS;
            }
            case "d":
            case "day":
            case "days": {
                return ChronoUnit.DAYS;
            }
        }
        if (lenient) {
            return null;
        }
        throw new IllegalArgumentException("Unsupported time unit " + u);
    }

    public static TimePeriod parse(String str, boolean lenient) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        ChronoUnit defaultUnit = null;
        if (defaultUnit == null) {
            defaultUnit = ChronoUnit.MILLIS;
        }
        Matcher matcher = PATTERN.matcher(str);
        if (matcher.find()) {
            double unitCount = 0;
            try {
                unitCount = Double.parseDouble(matcher.group("val"));
            } catch (Exception ex) {
                if (lenient) {
                    return null;
                }
                throw new IllegalArgumentException("Invalid Time period " + matcher.group("val"));
            }
            if (unitCount < 0) {
                if (lenient) {
                    return null;
                }
                throw new IllegalArgumentException("Invalid Time period " + matcher.group("val"));
            }
            String u = matcher.group("unit");
            if (u == null) {
                u = "ms";
            }
            ChronoUnit unit = parseUnit(u, lenient);
            return new TimePeriod(unitCount, unit);
        }
        if (lenient) {
            return null;
        }
        throw new IllegalArgumentException("Invalid Time period format " + str);
    }

    public TimePeriod neg() {
        if (count == 0) {
            return this;
        }
        return new TimePeriod(-count, unit);
    }

    public double getCount() {
        return count;
    }

    public ChronoUnit getUnit() {
        return unit;
    }


    @Override
    public String toString() {
        String v = (count == (int) count) ? String.valueOf((int) count) : String.valueOf(count);
        switch (unit) {
            case DAYS:
                return v + "d";
            case HOURS:
                return v + "h";
            case MINUTES:
                return v + "mn";
            case SECONDS:
                return v + "s";
            case MILLIS:
                return v + "ms";
            case MICROS:
                return v + "us";
            case NANOS:
                return v + "ns";
        }
        return String.valueOf((int) count) + unit.toString().toLowerCase();
    }

    public TimePeriod toUnit(ChronoUnit u, TimespanPattern pattern) {
        double hoursPerDay = pattern.getHoursPerDay();
        switch (unit) {
            case DAYS: {
                switch (u) {
                    case DAYS: {
                        return this;
                    }
                    case HOURS: {
                        return new TimePeriod(hoursPerDay * count, ChronoUnit.HOURS);
                    }
                    case MINUTES: {
                        return new TimePeriod(count * 60 * hoursPerDay, ChronoUnit.MINUTES);
                    }
                    case SECONDS: {
                        return new TimePeriod(count * 3600 * hoursPerDay, ChronoUnit.SECONDS);
                    }
                    case MILLIS: {
                        return new TimePeriod(count * 3600000 * hoursPerDay, ChronoUnit.MILLIS);
                    }
                    case MICROS: {
                        return new TimePeriod(count * 3600000000L * hoursPerDay, ChronoUnit.MICROS);
                    }
                    case NANOS: {
                        return new TimePeriod(count * 3600000000000L * hoursPerDay, ChronoUnit.NANOS);
                    }
                }
                break;
            }
            case HOURS: {
                switch (u) {
                    case DAYS: {
                        return new TimePeriod(count / hoursPerDay, ChronoUnit.DAYS);
                    }
                    case HOURS: {
                        return this;
                    }
                    case MINUTES: {
                        return new TimePeriod(count * 60, ChronoUnit.MINUTES);
                    }
                    case SECONDS: {
                        return new TimePeriod(count * 3600, ChronoUnit.SECONDS);
                    }
                    case MILLIS: {
                        return new TimePeriod(count * 3600000, ChronoUnit.MILLIS);
                    }
                    case MICROS: {
                        return new TimePeriod(count * 3600000000L, ChronoUnit.MICROS);
                    }
                    case NANOS: {
                        return new TimePeriod(count * 3600000000000L, ChronoUnit.NANOS);
                    }
                }
                break;
            }
            case MINUTES: {
                switch (u) {
                    case DAYS: {
                        return new TimePeriod(count / 60 / hoursPerDay, ChronoUnit.DAYS);
                    }
                    case HOURS: {
                        return new TimePeriod(count / 60, ChronoUnit.HOURS);
                    }
                    case MINUTES: {
                        return this;
                    }
                    case SECONDS: {
                        return new TimePeriod(count * 60, ChronoUnit.SECONDS);
                    }
                    case MILLIS: {
                        return new TimePeriod(count * 60000, ChronoUnit.MILLIS);
                    }
                    case MICROS: {
                        return new TimePeriod(count * 60000000L, ChronoUnit.MICROS);
                    }
                    case NANOS: {
                        return new TimePeriod(count * 60000000000L, ChronoUnit.NANOS);
                    }
                }
                break;
            }
            case SECONDS: {
                switch (u) {
                    case DAYS: {
                        return new TimePeriod(count / 3600 / hoursPerDay, ChronoUnit.DAYS);
                    }
                    case HOURS: {
                        return new TimePeriod(count / 3600, ChronoUnit.HOURS);
                    }
                    case MINUTES: {
                        return new TimePeriod(count / 60, ChronoUnit.MINUTES);
                    }
                    case SECONDS: {
                        return this;
                    }
                    case MILLIS: {
                        return new TimePeriod(count * 1000, ChronoUnit.MILLIS);
                    }
                    case MICROS: {
                        return new TimePeriod(count * 1000000L, ChronoUnit.MICROS);
                    }
                    case NANOS: {
                        return new TimePeriod(count * 1000000000L, ChronoUnit.NANOS);
                    }
                }
                break;
            }
            case MILLIS: {
                switch (u) {
                    case DAYS: {
                        return new TimePeriod(count / 3600000 / hoursPerDay, ChronoUnit.DAYS);
                    }
                    case HOURS: {
                        return new TimePeriod(count / 3600000, ChronoUnit.HOURS);
                    }
                    case MINUTES: {
                        return new TimePeriod(count / 60000, ChronoUnit.MINUTES);
                    }
                    case SECONDS: {
                        return new TimePeriod(count / 1000, ChronoUnit.SECONDS);
                    }
                    case MILLIS: {
                        return this;
                    }
                    case MICROS: {
                        return new TimePeriod(count * 1000L, ChronoUnit.MICROS);
                    }
                    case NANOS: {
                        return new TimePeriod(count * 1000000L, ChronoUnit.NANOS);
                    }
                }
                break;
            }
            case MICROS: {
                switch (u) {
                    case DAYS: {
                        return new TimePeriod(count / 3600000000L / hoursPerDay, ChronoUnit.DAYS);
                    }
                    case HOURS: {
                        return new TimePeriod(count / 3600000000L, ChronoUnit.HOURS);
                    }
                    case MINUTES: {
                        return new TimePeriod(count / 60000000, ChronoUnit.MINUTES);
                    }
                    case SECONDS: {
                        return new TimePeriod(count / 1000000, ChronoUnit.SECONDS);
                    }
                    case MILLIS: {
                        return new TimePeriod(count / 1000, ChronoUnit.MILLIS);
                    }
                    case MICROS: {
                        return this;
                    }
                    case NANOS: {
                        return new TimePeriod(count * 1000, ChronoUnit.NANOS);
                    }
                }
                break;
            }
            case NANOS: {
                switch (u) {
                    case DAYS: {
                        return new TimePeriod(count / 3600000000000L / hoursPerDay, ChronoUnit.DAYS);
                    }
                    case HOURS: {
                        return new TimePeriod(count / 3600000000000L, ChronoUnit.HOURS);
                    }
                    case MINUTES: {
                        return new TimePeriod(count / 60000000000L, ChronoUnit.MINUTES);
                    }
                    case SECONDS: {
                        return new TimePeriod(count / 1000000000, ChronoUnit.SECONDS);
                    }
                    case MILLIS: {
                        return new TimePeriod(count / 1000000, ChronoUnit.MILLIS);
                    }
                    case MICROS: {
                        return new TimePeriod(count / 1000, ChronoUnit.MICROS);
                    }
                    case NANOS: {
                        return this;
                    }
                }
                break;
            }
        }
        throw new IllegalArgumentException("Unsupported conversion");
    }

    public int compareTo(TimePeriod p) {
        if (p == null) {
            return -1;
        }
        ChronoUnit t0 = getUnit();
        if (getUnit().compareTo(p.getUnit()) > 0) {
            t0 = p.getUnit();
        }
        return Double.compare(toUnit(t0, TimespanPattern.DEFAULT).count, p.toUnit(t0, TimespanPattern.DEFAULT).count);
    }

    public Instant addTo(Instant i, TimespanPattern pattern) {
        TimePeriod[] periodArray = toTimePeriods().getPeriodArray();
        for (TimePeriod timePeriod : periodArray) {
            switch (timePeriod.getUnit()){
                case DAYS:{
                    i= i.plus((int) timePeriod.getCount(),ChronoUnit.DAYS);
                    break;
                }
                case HOURS:{
                    i= i.plus((int) timePeriod.getCount(),ChronoUnit.HOURS);
                    break;
                }
                case MINUTES:{
                    i= i.plus((int) timePeriod.getCount(),ChronoUnit.MINUTES);
                    break;
                }
                case SECONDS:{
                    i= i.plus((int) timePeriod.getCount(),ChronoUnit.SECONDS);
                    break;
                }
                case MILLIS:{
                    i= i.plus((int) timePeriod.getCount(),ChronoUnit.MILLIS);
                    break;
                }
                case MICROS:{
                    i= i.plus((int) timePeriod.getCount(),ChronoUnit.MICROS);
                    break;
                }
                case NANOS:{
                    i= i.plus((int) timePeriod.getCount(),ChronoUnit.NANOS);
                    break;
                }
            }
        }
        return i;
    }

    public TimePeriods toTimePeriods() {
        switch (unit) {
            case DAYS: {
                double c = count < 0 ? -count : count;
                int c0 = (int) c;
                double f = (c - c0) * 24;
                TimePeriods s = new TimePeriods();
                s.add(c0, ChronoUnit.DAYS);
                if (f != 0) {
                    TimePeriods pp = new TimePeriod(f, ChronoUnit.MINUTES).toTimePeriods();
                    for (TimePeriod tt : Arrays.asList(pp.getPeriodArray())) {
                        if (c >= 0) {
                            s.add(tt);
                        } else {
                            s.add(tt.neg());
                        }
                    }
                }
                return s;
            }
            case HOURS: {
                double c = count < 0 ? -count : count;
                int c0 = (int) c;
                double f = (c - c0) * 60;
                int d = c0 / (24);
                int h = (c0 % (24));

                TimePeriods s = new TimePeriods();
                s.add(d, ChronoUnit.DAYS);
                s.add(h, ChronoUnit.HOURS);
                if (f != 0) {
                    TimePeriods pp = new TimePeriod(f, ChronoUnit.MINUTES).toTimePeriods();
                    for (TimePeriod tt : Arrays.asList(pp.getPeriodArray())) {
                        if (c >= 0) {
                            s.add(tt);
                        } else {
                            s.add(tt.neg());
                        }
                    }
                }
                return s;
            }
            case MINUTES: {
                double c = count < 0 ? -count : count;
                int c0 = (int) c;
                double f = (c - c0) * 60;
                int d = c0 / (60 * 24);
                int h = (c0 % (60 * 24)) / 60;
                int mn = ((c0 % (60))) ;

                TimePeriods s = new TimePeriods();
                s.add(d, ChronoUnit.DAYS);
                s.add(h, ChronoUnit.HOURS);
                s.add(mn, ChronoUnit.MINUTES);
                if (f != 0) {
                    TimePeriods pp = new TimePeriod(f, ChronoUnit.SECONDS).toTimePeriods();
                    for (TimePeriod tt : Arrays.asList(pp.getPeriodArray())) {
                        if (c >= 0) {
                            s.add(tt);
                        } else {
                            s.add(tt.neg());
                        }
                    }
                }
                return s;
            }
            case SECONDS: {
                double c = count < 0 ? -count : count;
                long c0 = (int) c;
                double f = (c - c0) * 1000;
                int d = (int) (c0 / (24* 3600));
                int h = (int) ((c0 % (24* 3600)) / 3600);
                int mn = ((int) (((c0 % (3600)))))/60;
                int sec = (int) (((c0 % (60))));

                TimePeriods s = new TimePeriods();
                s.add(d, ChronoUnit.DAYS);
                s.add(h, ChronoUnit.HOURS);
                s.add(mn, ChronoUnit.MINUTES);
                s.add(sec, ChronoUnit.SECONDS);
                if (f != 0) {
                    TimePeriods pp = new TimePeriod(f, ChronoUnit.MILLIS).toTimePeriods();
                    for (TimePeriod tt : Arrays.asList(pp.getPeriodArray())) {
                        if (c >= 0) {
                            s.add(tt);
                        } else {
                            s.add(tt.neg());
                        }
                    }
                }
                return s;
            }
            case MILLIS: {
                double c = count < 0 ? -count : count;
                long c0 = (int) c;
                double f = (c - c0) * 1000;
                int d = (int) (c0 / (24* 3600*1000));
                int h = (int) ((c0 % (24* 3600*1000)) / 3600000);
                int mn = ((int) (((c0 % (3600*1000)))))/60000;
                int sec = ((int) (((c0 % (3600*1000)))))/1000;
                int msec = (int) (((c0 % (1000))));

                TimePeriods s = new TimePeriods();
                s.add(d, ChronoUnit.DAYS);
                s.add(h, ChronoUnit.HOURS);
                s.add(mn, ChronoUnit.MINUTES);
                s.add(sec, ChronoUnit.SECONDS);
                s.add(msec, ChronoUnit.MILLIS);
                if (f != 0) {
                    TimePeriods pp = new TimePeriod(f, ChronoUnit.MICROS).toTimePeriods();
                    for (TimePeriod tt : Arrays.asList(pp.getPeriodArray())) {
                        if (c >= 0) {
                            s.add(tt);
                        } else {
                            s.add(tt.neg());
                        }
                    }
                }
                return s;
            }
            case MICROS: {
                double c = count < 0 ? -count : count;
                long c0 = (int) c;
                double f = (c - c0) * 1000;
                int d = (int) (c0 / (24* 3600*1000000L));
                int h = (int) ((c0 % (24* 3600*1000000L)) / 3600000000L);
                int mn = ((int) (((c0 % (3600*1000000L)))))/60000000;
                int sec = ((int) (((c0 % (3600*1000000L)))))/1000000;
                int msec = (int) (((c0 % (1000000L))))/1000;
                int micsec = (int) (c0 %1000);

                TimePeriods s = new TimePeriods();
                s.add(d, ChronoUnit.DAYS);
                s.add(h, ChronoUnit.HOURS);
                s.add(mn, ChronoUnit.MINUTES);
                s.add(sec, ChronoUnit.SECONDS);
                s.add(msec, ChronoUnit.MILLIS);
                s.add(micsec, ChronoUnit.MICROS);
                if (f != 0) {
                    TimePeriods pp = new TimePeriod(f, ChronoUnit.NANOS).toTimePeriods();
                    for (TimePeriod tt : Arrays.asList(pp.getPeriodArray())) {
                        if (c >= 0) {
                            s.add(tt);
                        } else {
                            s.add(tt.neg());
                        }
                    }
                }
                return s;
            }
            case NANOS: {
                double c = count < 0 ? -count : count;
                long c0 = (int) c;
                int d = (int) (c0 / (24* 3600*1000000000L));
                int h = (int) ((c0 % (24* 3600*1000000000L)) / 3600000000000L);
                int mn = (int)(( (((c0 % (3600*1000000000L)))))/60000000000L);
                int sec = (int)( ((((c0 % (3600*1000000L)))))/1000000000L);
                int msec = (int) (((c0 % (1000000L))))%1000000;
                int micsec = (int) (c0 %1000000)/1000;
                int nano = (int) (c0 %1000);

                TimePeriods s = new TimePeriods();
                s.add(d, ChronoUnit.DAYS);
                s.add(h, ChronoUnit.HOURS);
                s.add(mn, ChronoUnit.MINUTES);
                s.add(sec, ChronoUnit.SECONDS);
                s.add(msec, ChronoUnit.MILLIS);
                s.add(micsec, ChronoUnit.MICROS);
                s.add(nano, ChronoUnit.NANOS);
                return s;
            }
        }
        throw new IllegalArgumentException("Unsupported " + unit);
    }
}



