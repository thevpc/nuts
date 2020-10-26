package net.vpc.toolbox.ntasks;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NTimePeriod implements Comparable<NTimePeriod> {
    private static Pattern PATTERN = Pattern.compile("(?<val>[0-9]+([.][0-9]+)?)[ ]*(?<unit>([a-zA-Z]+))");
    private double count;
    private TimeUnit unit;

    //    public static void main(String[] args) {
//        System.out.println("1ms".matches("(<?val>([0-9]+([.][0-9]+)?))[ ]*(<?unit>([a-zA-Z]+))"));
//        System.out.println("1".matches("(?<a>[0-9])"));
//    }
    public NTimePeriod(double count, TimeUnit unit) {
        this.count = count;
        this.unit = unit;
    }

    public static TimeUnit parseUnit(String u, boolean lenient) {
        switch (u.toLowerCase()) {
            case "ns":
            case "nanos":
            case "nano":
            case "nanosecond":
            case "nanoseconds": {
                return TimeUnit.NANOSECONDS;
            }
            case "ms":
            case "milli":
            case "millis":
            case "millisecond":
            case "milliseconds": {
                return TimeUnit.MILLISECONDS;
            }
            case "s":
            case "sec":
            case "secs":
            case "second":
            case "seconds": {
                return TimeUnit.SECONDS;
            }
            case "mn":
            case "min":
            case "mins":
            case "minute":
            case "minutes": {
                return TimeUnit.MINUTES;
            }
            case "h":
            case "hour":
            case "hours": {
                return TimeUnit.HOURS;
            }
            case "d":
            case "day":
            case "days": {
                return TimeUnit.DAYS;
            }
        }
        if (lenient) {
            return null;
        }
        throw new IllegalArgumentException("Unsupported time unit " + u);
    }

    public static NTimePeriod parse(String str, boolean lenient) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        TimeUnit defaultUnit = null;
        if (defaultUnit == null) {
            defaultUnit = TimeUnit.MILLISECONDS;
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
            TimeUnit unit = parseUnit(u, lenient);
            return new NTimePeriod(unitCount, unit);
        }
        if (lenient) {
            return null;
        }
        throw new IllegalArgumentException("Invalid Time period format " + str);
    }

    public double getCount() {
        return count;
    }

    public TimeUnit getUnit() {
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
            case MILLISECONDS:
                return v + "ms";
            case MICROSECONDS:
                return v + "us";
            case NANOSECONDS:
                return v + "ns";
        }
        return String.valueOf((int) count) + unit.toString().toLowerCase();
    }

    public NTimePeriod toUnit(TimeUnit u, double hoursPerDay) {
        switch (unit) {
            case DAYS: {
                switch (u) {
                    case DAYS: {
                        return this;
                    }
                    case HOURS: {
                        return new NTimePeriod(hoursPerDay * count, TimeUnit.HOURS);
                    }
                    case MINUTES: {
                        return new NTimePeriod(count * 60 * hoursPerDay, TimeUnit.MINUTES);
                    }
                    case SECONDS: {
                        return new NTimePeriod(count * 3600 * hoursPerDay, TimeUnit.SECONDS);
                    }
                    case MILLISECONDS: {
                        return new NTimePeriod(count * 3600000 * hoursPerDay, TimeUnit.MILLISECONDS);
                    }
                    case MICROSECONDS: {
                        return new NTimePeriod(count * 3600000000L * hoursPerDay, TimeUnit.MICROSECONDS);
                    }
                    case NANOSECONDS: {
                        return new NTimePeriod(count * 3600000000000L * hoursPerDay, TimeUnit.NANOSECONDS);
                    }
                }
                break;
            }
            case HOURS: {
                switch (u) {
                    case DAYS: {
                        return new NTimePeriod(count / hoursPerDay, TimeUnit.DAYS);
                    }
                    case HOURS: {
                        return this;
                    }
                    case MINUTES: {
                        return new NTimePeriod(count * 60, TimeUnit.MINUTES);
                    }
                    case SECONDS: {
                        return new NTimePeriod(count * 3600, TimeUnit.SECONDS);
                    }
                    case MILLISECONDS: {
                        return new NTimePeriod(count * 3600000, TimeUnit.MILLISECONDS);
                    }
                    case MICROSECONDS: {
                        return new NTimePeriod(count * 3600000000L, TimeUnit.MICROSECONDS);
                    }
                    case NANOSECONDS: {
                        return new NTimePeriod(count * 3600000000000L, TimeUnit.NANOSECONDS);
                    }
                }
                break;
            }
            case MINUTES: {
                switch (u) {
                    case DAYS: {
                        return new NTimePeriod(count / 60 / hoursPerDay, TimeUnit.DAYS);
                    }
                    case HOURS: {
                        return new NTimePeriod(count / 60, TimeUnit.HOURS);
                    }
                    case MINUTES: {
                        return this;
                    }
                    case SECONDS: {
                        return new NTimePeriod(count * 60, TimeUnit.SECONDS);
                    }
                    case MILLISECONDS: {
                        return new NTimePeriod(count * 60000, TimeUnit.MILLISECONDS);
                    }
                    case MICROSECONDS: {
                        return new NTimePeriod(count * 60000000L, TimeUnit.MICROSECONDS);
                    }
                    case NANOSECONDS: {
                        return new NTimePeriod(count * 60000000000L, TimeUnit.NANOSECONDS);
                    }
                }
                break;
            }
            case SECONDS: {
                switch (u) {
                    case DAYS: {
                        return new NTimePeriod(count / 3600 / hoursPerDay, TimeUnit.DAYS);
                    }
                    case HOURS: {
                        return new NTimePeriod(count / 3600, TimeUnit.HOURS);
                    }
                    case MINUTES: {
                        return new NTimePeriod(count / 60, TimeUnit.MINUTES);
                    }
                    case SECONDS: {
                        return this;
                    }
                    case MILLISECONDS: {
                        return new NTimePeriod(count * 1000, TimeUnit.MILLISECONDS);
                    }
                    case MICROSECONDS: {
                        return new NTimePeriod(count * 1000000L, TimeUnit.MICROSECONDS);
                    }
                    case NANOSECONDS: {
                        return new NTimePeriod(count * 1000000000L, TimeUnit.NANOSECONDS);
                    }
                }
                break;
            }
            case MILLISECONDS: {
                switch (u) {
                    case DAYS: {
                        return new NTimePeriod(count / 3600000 / hoursPerDay, TimeUnit.DAYS);
                    }
                    case HOURS: {
                        return new NTimePeriod(count / 3600000, TimeUnit.HOURS);
                    }
                    case MINUTES: {
                        return new NTimePeriod(count / 60000, TimeUnit.MINUTES);
                    }
                    case SECONDS: {
                        return new NTimePeriod(count / 1000, TimeUnit.SECONDS);
                    }
                    case MILLISECONDS: {
                        return this;
                    }
                    case MICROSECONDS: {
                        return new NTimePeriod(count * 1000L, TimeUnit.MICROSECONDS);
                    }
                    case NANOSECONDS: {
                        return new NTimePeriod(count * 1000000L, TimeUnit.NANOSECONDS);
                    }
                }
                break;
            }
            case MICROSECONDS: {
                switch (u) {
                    case DAYS: {
                        return new NTimePeriod(count / 3600000000L / hoursPerDay, TimeUnit.DAYS);
                    }
                    case HOURS: {
                        return new NTimePeriod(count / 3600000000L, TimeUnit.HOURS);
                    }
                    case MINUTES: {
                        return new NTimePeriod(count / 60000000, TimeUnit.MINUTES);
                    }
                    case SECONDS: {
                        return new NTimePeriod(count / 1000000, TimeUnit.SECONDS);
                    }
                    case MILLISECONDS: {
                        return new NTimePeriod(count / 1000, TimeUnit.MILLISECONDS);
                    }
                    case MICROSECONDS: {
                        return this;
                    }
                    case NANOSECONDS: {
                        return new NTimePeriod(count * 1000, TimeUnit.NANOSECONDS);
                    }
                }
                break;
            }
            case NANOSECONDS: {
                switch (u) {
                    case DAYS: {
                        return new NTimePeriod(count / 3600000000000L / hoursPerDay, TimeUnit.DAYS);
                    }
                    case HOURS: {
                        return new NTimePeriod(count / 3600000000000L, TimeUnit.HOURS);
                    }
                    case MINUTES: {
                        return new NTimePeriod(count / 60000000000L, TimeUnit.MINUTES);
                    }
                    case SECONDS: {
                        return new NTimePeriod(count / 1000000000, TimeUnit.SECONDS);
                    }
                    case MILLISECONDS: {
                        return new NTimePeriod(count / 1000000, TimeUnit.MILLISECONDS);
                    }
                    case MICROSECONDS: {
                        return new NTimePeriod(count / 1000, TimeUnit.MICROSECONDS);
                    }
                    case NANOSECONDS: {
                        return this;
                    }
                }
                break;
            }
        }
        throw new IllegalArgumentException("Unsupported conversion");
    }

    public int compareTo(NTimePeriod p) {
        if (p == null) {
            return -1;
        }
        TimeUnit t0 = getUnit();
        if (getUnit().compareTo(p.getUnit()) > 0) {
            t0 = p.getUnit();
        }
        return Double.compare(toUnit(t0, 24).count, p.toUnit(t0, 24).count);
    }
}



