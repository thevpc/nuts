package net.thevpc.nuts.runtime.core.common;

import net.thevpc.nuts.runtime.core.util.CoreStringUtils;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimePeriod {
    private static Pattern PATTERN = Pattern.compile("(<?val>[0-9]+)[ ]*(<?unit>([a-zA-Z]+))?");
    private long unitCount;
    private TimeUnit unit;

    public TimePeriod(long unitCount, TimeUnit unit) {
        this.unitCount = unitCount;
        this.unit = unit;
    }

    public static TimePeriod parse(String str, boolean lenient, TimeUnit defaultUnit) {
        if (CoreStringUtils.isBlank(str)) {
            return null;
        }
        if (defaultUnit == null) {
            defaultUnit = TimeUnit.MILLISECONDS;
        }
        Matcher matcher = PATTERN.matcher(str);
        if (matcher.find()) {
            long unitCount = 0;
            try {
                unitCount = Long.parseLong(matcher.group("val"));
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
            TimeUnit unit = defaultUnit;
            switch (u.toLowerCase()) {
                case "ns":
                case "nanos":
                case "nano":
                case "nanosecond":
                case "nanoseconds": {
                    unit = TimeUnit.NANOSECONDS;
                    break;
                }
                case "ms":
                case "milli":
                case "millis":
                case "millisecond":
                case "milliseconds": {
                    unit = TimeUnit.MILLISECONDS;
                    break;
                }
                case "s":
                case "sec":
                case "secs":
                case "second":
                case "seconds": {
                    unit = TimeUnit.SECONDS;
                    break;
                }
                case "mn":
                case "min":
                case "mins":
                case "minute":
                case "minutes": {
                    unit = TimeUnit.MINUTES;
                    break;
                }
                case "h":
                case "hour":
                case "hours": {
                    unit = TimeUnit.HOURS;
                    break;
                }
                case "d":
                case "day":
                case "days": {
                    unit = TimeUnit.DAYS;
                    break;
                }
                default: {
                    if (lenient) {
                        return null;
                    }
                    throw new IllegalArgumentException("Unsupported time unit " + u);
                }
            }
            return new TimePeriod(unitCount, unit);
        }
        if (lenient) {
            return null;
        }
        throw new IllegalArgumentException("Invalid Time period format " + str);
    }

    public long getUnitCount() {
        return unitCount;
    }

    public TimeUnit getUnit() {
        return unit;
    }

}
