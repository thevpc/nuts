package net.thevpc.nuts.runtime.core.common;

import net.thevpc.nuts.NutsBlankable;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsParseException;
import net.thevpc.nuts.NutsSession;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimePeriod {
    private static Pattern PATTERN = Pattern.compile("(<?val>[-+]?[0-9]+)[ ]*(<?unit>([a-zA-Z]+))?");
    private long unitCount;
    private TimeUnit unit;

    public TimePeriod(long unitCount, TimeUnit unit) {
        this.unitCount = unitCount;
        this.unit = unit;
    }

    public static TimePeriod parse(String str, TimeUnit defaultUnit, NutsSession session) {
        TimePeriod v = parseLenient(str, defaultUnit, session);
        if(v==null){
            throw new NutsParseException(session, NutsMessage.cstyle("invalid Time period %s", str));
        }
        return v;
    }

    public static TimePeriod parseLenient(String str, TimeUnit defaultUnit, NutsSession session) {
        return parseLenient(str,null,null,defaultUnit,session);
    }

    public static TimePeriod parseLenient(String str, TimePeriod emptyValue,TimePeriod errorValue,TimeUnit defaultUnit, NutsSession session) {
        if (NutsBlankable.isBlank(str)) {
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
                return null;
            }
            if (unitCount < 0) {
                return null;
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
            return new TimePeriod(unitCount, unit);
        }
        return null;
    }

    public long getUnitCount() {
        return unitCount;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(unitCount, unit);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimePeriod that = (TimePeriod) o;
        return unitCount == that.unitCount && unit == that.unit;
    }

    @Override
    public String toString() {
        return unitCount + unit.name().toLowerCase();
    }
}
