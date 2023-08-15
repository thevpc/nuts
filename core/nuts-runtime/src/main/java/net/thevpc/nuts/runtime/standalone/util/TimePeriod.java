package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimePeriod {
    private static Pattern PATTERN = Pattern.compile("(<?val>[-+]?[0-9]+)[ ]*(<?unit>([a-zA-Z]+))?");
    private long count;
    private TimeUnit unit;

    public TimePeriod(long count, TimeUnit unit) {
        this.count = count;
        this.unit = unit;
    }

    public static NOptional<TimePeriod> parse(String str, TimeUnit defaultUnit) {
        if (NBlankable.isBlank(str)) {
            return NOptional.ofEmpty(s -> NMsg.ofPlain(TimePeriod.class.getSimpleName() + " is empty"));
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
                return NOptional.ofError(s -> NMsg.ofC(TimePeriod.class.getSimpleName() + " invalid value : %s", str),ex);
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
                    return null;
                }
            }
            return NOptional.of(new TimePeriod(unitCount, unit));
        }
        return NOptional.ofError(s -> NMsg.ofC(TimePeriod.class.getSimpleName() + " invalid value : %s", str));
    }

    public long getCount() {
        return count;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, unit);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimePeriod that = (TimePeriod) o;
        return count == that.count && unit == that.unit;
    }

    @Override
    public String toString() {
        return count + unit.name().toLowerCase();
    }
}
