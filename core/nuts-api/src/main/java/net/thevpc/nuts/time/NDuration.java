package net.thevpc.nuts.time;

import net.thevpc.nuts.text.NI18n;
import net.thevpc.nuts.elem.NMapBy;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class NDuration implements Serializable {
    public static final NDuration ZERO = ofMillis(0);
    private long nanos;
    private long micros;

    private long milliSeconds;

    private long seconds;

    private long minutes;

    private long hours;
    private long days;
    private long weeks;
    private long months;
    private long years;
    private final ChronoUnit smallestUnit;
    private final ChronoUnit largestUnit;
    private final long timeMillis;
    private final int timeNanos;

    @NMapBy
    public NDuration(
            @NMapBy(name = "years") long years,
            @NMapBy(name = "months") long months,
            @NMapBy(name = "weeks") long weeks,
            @NMapBy(name = "days") long days,
            @NMapBy(name = "hours") long hours,
            @NMapBy(name = "minutes") long minutes,
            @NMapBy(name = "seconds") long seconds,
            @NMapBy(name = "milliSeconds") long milliSeconds,
            @NMapBy(name = "micros") long micros,
            @NMapBy(name = "nanos") long nanos,
            @NMapBy(name = "smallestUnit") ChronoUnit smallestUnit,
            @NMapBy(name = "largestUnit") ChronoUnit largestUnit
    ) {
        this.nanos = nanos;
        this.micros = micros;
        this.milliSeconds = milliSeconds;
        this.seconds = seconds;
        this.minutes = minutes;
        this.hours = hours;
        this.days = days;
        this.weeks = weeks;
        this.months = months;
        this.years = years;
        this.timeMillis = rebuildTimeMillis();
        this.timeNanos = rebuildTimeNanos();
        this.smallestUnit = smallestUnit == null ? detectSmallestUnit() : normalize(smallestUnit);
        largestUnit = largestUnit == null ? detectLargestUnit() : normalize(largestUnit);
        if (largestUnit.ordinal() < this.smallestUnit.ordinal()) {
            largestUnit = this.smallestUnit;
        }
        this.largestUnit = largestUnit;
        applyUnits();
    }

    public NDuration(long[] values, ChronoUnit smallestUnit, ChronoUnit largestUnit) {
        this.nanos = values[ChronoUnit.NANOS.ordinal()];
        this.micros = values[ChronoUnit.MICROS.ordinal()];
        this.milliSeconds = values[ChronoUnit.MILLIS.ordinal()];
        this.seconds = values[ChronoUnit.SECONDS.ordinal()];
        this.minutes = values[ChronoUnit.MINUTES.ordinal()];
        this.hours = values[ChronoUnit.HOURS.ordinal()];
        this.days = values[ChronoUnit.DAYS.ordinal()];
        this.weeks = values[ChronoUnit.WEEKS.ordinal()];
        this.months = values[ChronoUnit.MONTHS.ordinal()];
        this.years = values[ChronoUnit.YEARS.ordinal()];
        this.timeMillis = rebuildTimeMillis();
        this.timeNanos = rebuildTimeNanos();
        this.smallestUnit = smallestUnit == null ? detectSmallestUnit() : normalize(smallestUnit);
        largestUnit = largestUnit == null ? detectLargestUnit() : normalize(largestUnit);
        if (largestUnit.ordinal() < this.smallestUnit.ordinal()) {
            largestUnit = this.smallestUnit;
        }
        this.largestUnit = largestUnit;
        applyUnits();
    }

    public static NDuration between(Instant startCreateTime, Instant endCreateTime) {
        return NDuration.ofDuration(Duration.between(startCreateTime, endCreateTime));
    }

    private int rebuildTimeNanos() {
        return (int) (nanos + micros * 1000);
    }

    private long rebuildTimeMillis() {
        return milliSeconds
                + seconds * 1000
                + minutes * 1000 * 60
                + hours * 1000 * 60 * 60
                + days * 1000 * 60 * 60 * 24
                + weeks * 1000 * 60 * 60 * 24 * 7
                + months * 1000 * 60 * 60 * 24 * 30
                + years * 1000 * 60 * 60 * 24 * 365;
    }

    public NDuration(long timeMillis, int timeNanos) {
        this.timeMillis = timeMillis;
        this.timeNanos = timeNanos;
        this.nanos = (int) (timeNanos % 1000L);
        this.micros = (int) (timeNanos / 1000L);
        this.milliSeconds = (int) (timeMillis % 1000L);
        this.seconds = (int) ((timeMillis / 1000) % 60);
        this.minutes = (int) ((timeMillis / 60000) % 60);
        this.hours = (int) ((timeMillis / 3600000) % 24);

        long dd = ((timeMillis / 3600000 / 24));
        this.years = dd / 365;
        dd = dd % 365;
        this.months = dd / 30;
        dd = dd % 30;
        this.weeks = dd / 7;
        dd = dd % 7;
        this.days = dd;
        this.smallestUnit = detectSmallestUnit();
        this.largestUnit = detectLargestUnit();
    }

    public NDuration(long timeMillis, int timeNanos, ChronoUnit smallestUnit, ChronoUnit largestUnit) {
        this.timeMillis = timeMillis;
        this.timeNanos = timeNanos;
        if (smallestUnit != null && largestUnit != null) {
            this.smallestUnit = normalize(smallestUnit);
            largestUnit = normalize(largestUnit);
            if (largestUnit.ordinal() < this.smallestUnit.ordinal()) {
                largestUnit = this.smallestUnit;
            }
            this.largestUnit = largestUnit;
            int largestUnitOrdinal = this.largestUnit.ordinal();
            int smallestUnitOrdinal = this.smallestUnit.ordinal();
            if (smallestUnitOrdinal <= ChronoUnit.NANOS.ordinal()) {
                if (largestUnitOrdinal > ChronoUnit.NANOS.ordinal()) {
                    this.nanos = (int) (timeNanos % 1000L);
                } else {
                    this.nanos = timeNanos + timeMillis * 1000000;
                    return;
                }
            }
            if (smallestUnitOrdinal <= ChronoUnit.MICROS.ordinal()) {
                if (largestUnitOrdinal > ChronoUnit.MICROS.ordinal()) {
                    this.micros = (int) (timeNanos / 1000L);
                } else {
                    this.micros = (int) (timeNanos / 1000L) + timeMillis * 1000;
                    return;
                }
            }
            if (smallestUnitOrdinal <= ChronoUnit.MILLIS.ordinal()) {
                if (largestUnitOrdinal > ChronoUnit.MILLIS.ordinal()) {
                    this.milliSeconds = (int) (timeMillis % 1000L);
                } else {
                    this.milliSeconds = timeMillis;
                    return;
                }
            }
            if (smallestUnitOrdinal <= ChronoUnit.SECONDS.ordinal()) {
                if (largestUnitOrdinal > ChronoUnit.SECONDS.ordinal()) {
                    this.seconds = (int) ((timeMillis / 1000) % 60);
                } else {
                    this.seconds = timeMillis / 1000;
                    return;
                }
            }
            if (smallestUnitOrdinal <= ChronoUnit.MINUTES.ordinal()) {
                if (largestUnitOrdinal > ChronoUnit.MINUTES.ordinal()) {
                    this.minutes = (int) ((timeMillis / 60000) % 60);
                } else {
                    this.minutes = timeMillis / 60000;
                    return;
                }
            }
            if (smallestUnitOrdinal <= ChronoUnit.HOURS.ordinal()) {
                if (largestUnitOrdinal > ChronoUnit.HOURS.ordinal()) {
                    this.hours = (int) ((timeMillis / 3600000) % 24);
                } else {
                    this.hours = (int) (timeMillis / 3600000);
                    return;
                }
            }

            long dd = ((timeMillis / 3600000 / 24));
            if (largestUnitOrdinal >= ChronoUnit.YEARS.ordinal()) {
                if (smallestUnitOrdinal <= ChronoUnit.YEARS.ordinal()) {
                    this.years = dd / 365;
                }
                dd = dd % 365;
            }
            if (largestUnitOrdinal >= ChronoUnit.MONTHS.ordinal()) {
                if (smallestUnitOrdinal <= ChronoUnit.MONTHS.ordinal()) {
                    this.months = dd / 30;
                }
                dd = dd % 30;
            }
            if (largestUnitOrdinal >= ChronoUnit.WEEKS.ordinal()) {
                if (smallestUnitOrdinal <= ChronoUnit.WEEKS.ordinal()) {
                    this.weeks = dd / 7;
                }
                dd = dd % 7;
            }
            if (smallestUnitOrdinal <= ChronoUnit.DAYS.ordinal()) {
                this.days = dd;
            }
        } else {
            this.nanos = (int) (timeNanos % 1000L);
            this.micros = (int) (timeNanos / 1000L);
            this.milliSeconds = (int) (timeMillis % 1000L);
            this.seconds = (int) ((timeMillis / 1000) % 60);
            this.minutes = (int) ((timeMillis / 60000) % 60);
            this.hours = (int) ((timeMillis / 3600000) % 24);

            long dd = ((timeMillis / 3600000 / 24));
            this.years = dd / 365;
            dd = dd % 365;
            this.months = dd / 30;
            dd = dd % 30;
            this.weeks = dd / 7;
            dd = dd % 7;
            this.days = dd;
            this.smallestUnit = smallestUnit == null ? detectSmallestUnit() : normalize(smallestUnit);
            largestUnit = largestUnit == null ? detectLargestUnit() : normalize(largestUnit);
            if (largestUnit.ordinal() < this.smallestUnit.ordinal()) {
                largestUnit = this.smallestUnit;
            }
            this.largestUnit = largestUnit;
            applyUnits();
        }
    }

    private void applyUnits() {
        int uo = this.smallestUnit.ordinal();
        if (uo > ChronoUnit.NANOS.ordinal()) {
            nanos = 0;
        }
        if (uo > ChronoUnit.MICROS.ordinal()) {
            micros = 0;
        }
        if (uo > ChronoUnit.MILLIS.ordinal()) {
            milliSeconds = 0;
        }
        if (uo > ChronoUnit.SECONDS.ordinal()) {
            seconds = 0;
        }
        if (uo > ChronoUnit.MINUTES.ordinal()) {
            minutes = 0;
        }
        if (uo > ChronoUnit.HOURS.ordinal()) {
            hours = 0;
        }
        if (uo > ChronoUnit.DAYS.ordinal()) {
            days = 0;
        }
        if (uo > ChronoUnit.WEEKS.ordinal()) {
            weeks = 0;
        }
        if (uo > ChronoUnit.MONTHS.ordinal()) {
            months = 0;
        }
        if (uo > ChronoUnit.MONTHS.ordinal()) {
            months = 0;
        }
        if (uo > ChronoUnit.YEARS.ordinal()) {
            years = 0;
        }
        switch (this.largestUnit) {
            case YEARS: {
                //do nothing
                break;
            }
            case MONTHS: {
                months += 12 * years;
                years = 0;
                break;
            }
            case WEEKS: {
                weeks += 4 * months + 52 * years;
                years = 0;
                months = 0;
                break;
            }
            case DAYS: {
                days += 7 * weeks + 30 * months + 365 * years;
                years = 0;
                months = 0;
                weeks = 0;
                break;
            }
            case HOURS: {
                hours += (7 * weeks + 30 * months + 365 * years + days) * 24;
                years = 0;
                months = 0;
                weeks = 0;
                days = 0;
                break;
            }
            case MINUTES: {
                minutes += ((7 * weeks + 30 * months + 365 * years + days) * 24 + hours) * 60;
                years = 0;
                months = 0;
                weeks = 0;
                days = 0;
                hours = 0;
                break;
            }
            case SECONDS: {
                seconds += (((7 * weeks + 30 * months + 365 * years + days) * 24 + hours) * 60 + minutes) * 60;
                years = 0;
                months = 0;
                weeks = 0;
                days = 0;
                hours = 0;
                minutes = 0;
                break;
            }
            case MILLIS: {
                milliSeconds += ((((7 * weeks + 30 * months + 365 * years + days) * 24 + hours) * 60 + minutes) * 60 + seconds) * 1000;
                years = 0;
                months = 0;
                weeks = 0;
                days = 0;
                hours = 0;
                minutes = 0;
                seconds = 0;
                break;
            }
            case MICROS: {
                micros += (((((7 * weeks + 30 * months + 365 * years + days) * 24 + hours)
                        * 60 + minutes) * 60 + seconds) * 1000 + milliSeconds) * 1000;
                years = 0;
                months = 0;
                weeks = 0;
                days = 0;
                hours = 0;
                minutes = 0;
                seconds = 0;
                milliSeconds = 0;
                break;
            }
            case NANOS: {
                nanos += ((((((7 * weeks + 30 * months + 365 * years + days) * 24 + hours)
                        * 60 + minutes) * 60 + seconds) * 1000 + milliSeconds) * 1000 + micros) * 1000;
                years = 0;
                months = 0;
                weeks = 0;
                days = 0;
                hours = 0;
                minutes = 0;
                seconds = 0;
                milliSeconds = 0;
                micros = 0;
                break;
            }
        }
    }

    private ChronoUnit detectSmallestUnit() {
        if ((nanos | micros | milliSeconds | seconds | minutes | hours | days | weeks | months | years) == 0) {
            return ChronoUnit.NANOS;
        } else {
            return nanos != 0 ? ChronoUnit.NANOS :
                    micros != 0 ? ChronoUnit.MICROS :
                    milliSeconds != 0 ? ChronoUnit.MILLIS :
                    seconds != 0 ? ChronoUnit.SECONDS :
                    minutes != 0 ? ChronoUnit.MINUTES :
                    hours != 0 ? ChronoUnit.HOURS :
                    days != 0 ? ChronoUnit.DAYS :
                    weeks != 0 ? ChronoUnit.WEEKS :
                    months != 0 ? ChronoUnit.MONTHS :
                    years != 0 ? ChronoUnit.YEARS :
                    normalize(ChronoUnit.FOREVER);
        }
    }

    private ChronoUnit detectLargestUnit() {
        if ((nanos | micros | milliSeconds | seconds | minutes | hours | days | weeks | months | years) == 0) {
            return ChronoUnit.NANOS;
        } else {
            return years != 0 ? ChronoUnit.YEARS :
                    months != 0 ? ChronoUnit.MONTHS :
                    weeks != 0 ? ChronoUnit.WEEKS :
                    days != 0 ? ChronoUnit.DAYS :
                    hours != 0 ? ChronoUnit.HOURS :
                    minutes != 0 ? ChronoUnit.MINUTES :
                    seconds != 0 ? ChronoUnit.SECONDS :
                    milliSeconds != 0 ? ChronoUnit.MILLIS :
                    micros != 0 ? ChronoUnit.MICROS :
                    nanos != 0 ? ChronoUnit.NANOS :
                    normalize(ChronoUnit.FOREVER);
        }
    }


    static ChronoUnit normalize(ChronoUnit smallestUnit) {
        switch (smallestUnit) {
            case NANOS:
            case MICROS:
            case MILLIS:
            case SECONDS:
            case MINUTES:
            case HOURS:
            case DAYS:
            case WEEKS:
            case YEARS:
                return smallestUnit;
            case HALF_DAYS:
                return ChronoUnit.DAYS;
            default:
                return ChronoUnit.YEARS;
        }
    }

    public static NDuration ofNanos(long durationNanos) {
        long ms = durationNanos / 1000000;
        int ns = (int) (durationNanos % 1000000);
        return new NDuration(ms, ns);
    }

    public static NDuration ofNanos(long durationNanos, ChronoUnit smallestUnit, ChronoUnit largestUnit) {
        long ms = durationNanos / 1000000;
        int ns = (int) (durationNanos % 1000000);
        return new NDuration(ms, ns, smallestUnit, largestUnit);
    }

    public static NDuration ofNanosOnly(long durationNanos) {
        return ofUnitOnly(durationNanos, ChronoUnit.MILLIS);
    }

    public static NDuration ofMillisOnly(long durationMillis) {
        return ofUnitOnly(durationMillis, ChronoUnit.MILLIS);
    }

    public static NDuration ofSecondsOnly(long durationSeconds) {
        return ofUnitOnly(durationSeconds, ChronoUnit.SECONDS);
    }

    public static NDuration ofMinutesOnly(long durationMinutes) {
        return ofUnitOnly(durationMinutes, ChronoUnit.MINUTES);
    }

    public static NDuration ofHoursOnly(long durationHours) {
        return ofUnitOnly(durationHours, ChronoUnit.HOURS);
    }

    public static NDuration ofDaysOnly(long durationDays) {
        return ofUnitOnly(durationDays, ChronoUnit.DAYS);
    }

    public static NDuration ofWeeksOnly(long durationWeeks) {
        return ofUnitOnly(durationWeeks, ChronoUnit.WEEKS);
    }

    public static NDuration ofMonthOnly(long durationMonths) {
        return ofUnitOnly(durationMonths, ChronoUnit.MONTHS);
    }

    public static NDuration ofYearsOnly(long durationYears) {
        return ofUnitOnly(durationYears, ChronoUnit.YEARS);
    }

    public static NDuration ofSeconds(long durationSeconds) {
        return ofUnit(durationSeconds, ChronoUnit.SECONDS);
    }

    public static NDuration ofMinutes(long durationMinutes) {
        return ofUnit(durationMinutes, ChronoUnit.MINUTES);
    }

    public static NDuration ofHours(long durationHours) {
        return ofUnit(durationHours, ChronoUnit.HOURS);
    }

    public static NDuration ofDays(long durationDays) {
        return ofUnit(durationDays, ChronoUnit.DAYS);
    }

    public static NDuration ofWeeks(long durationWeeks) {
        return ofUnit(durationWeeks, ChronoUnit.WEEKS);
    }

    public static NDuration ofMonth(long durationMonths) {
        return ofUnit(durationMonths, ChronoUnit.MONTHS);
    }

    public static NDuration ofYears(long durationYears) {
        return ofUnit(durationYears, ChronoUnit.YEARS);
    }

    public static NDuration ofUnitOnly(long durationInUnit, ChronoUnit unit) {
        long[] values = new long[ChronoUnit.values().length];
        values[unit.ordinal()] = durationInUnit;
        return new NDuration(values, null, null);
    }

    public static NDuration ofUnit(long durationInUnit, ChronoUnit unit) {
        return ofUnitOnly(durationInUnit, unit).normalize();
    }

    public static NDuration ofMillis(long durationMillis) {
        return new NDuration(durationMillis, 0);
    }

    public static NDuration ofMillis(long durationMillis, ChronoUnit smallestUnit, ChronoUnit largestUnit) {
        return new NDuration(durationMillis, 0, smallestUnit, largestUnit);
    }

    public static NDuration ofDuration(Duration duration) {
        return ofSecondsAndNanos(
                duration.getSeconds(),
                duration.getNano()
        );
    }

    public static NDuration ofMillisAndNanos(long durationMillis, int nanos) {
        return new NDuration(durationMillis, nanos);
    }

    public static NDuration of(long[] values, ChronoUnit smallestUnit, ChronoUnit largestUnit) {
        return new NDuration(values, smallestUnit, largestUnit);
    }

    public static NDuration of(long[] values) {
        return of(values, null, null);
    }

    public static NDuration ofSecondsAndNanos(long durationSeconds, long nanos) {
        long millis = durationSeconds * 1000 + nanos / 1000000;
        int ns = (int) (nanos % 1000000);
        return ofMillisAndNanos(millis, ns);
    }

    public long getNanos() {
        return nanos;
    }

    public ChronoUnit firstNonZeroUp(ChronoUnit unit) {
        ChronoUnit[] values = ChronoUnit.values();
        int o = unit.ordinal();
        while (o < values.length) {
            if (get(values[o]) != 0) {
                return values[o];
            }
            o++;
        }
        return null;
    }

    public ChronoUnit firstNonZeroDown(ChronoUnit unit) {
        ChronoUnit[] values = ChronoUnit.values();
        int o = unit.ordinal();
        while (o > 0) {
            if (get(values[o]) != 0) {
                return values[o];
            }
            o--;
        }
        return null;
    }

    public boolean isZeroDown(ChronoUnit unit) {
        switch (unit) {
            case CENTURIES:
            case ERAS:
            case FOREVER:
            case MILLENNIA:
            case DECADES:
            case YEARS:
                return (years | months | weeks | days | hours | minutes | seconds | milliSeconds | micros | nanos) == 0;
            case MONTHS:
                return (months | weeks | days | hours | minutes | seconds | milliSeconds | micros | nanos) == 0;
            case WEEKS:
                return (weeks | days | hours | minutes | seconds | milliSeconds | micros | nanos) == 0;
            case DAYS:
                return (days | hours | minutes | seconds | milliSeconds | micros | nanos) == 0;
            case HOURS:
                return (hours | minutes | seconds | milliSeconds | micros | nanos) == 0;
            case MINUTES:
                return (minutes | seconds | milliSeconds | micros | nanos) == 0;
            case SECONDS:
                return (seconds | milliSeconds | micros | nanos) == 0;
            case MILLIS:
                return (milliSeconds | micros | nanos) == 0;
            case MICROS:
                return (micros | nanos) == 0;
            case NANOS:
                return (nanos) == 0;
        }
        return false;
    }

    public boolean isZeroUp(ChronoUnit unit) {
        switch (unit) {
            case CENTURIES:
            case ERAS:
            case FOREVER:
            case MILLENNIA:
            case DECADES:
            case YEARS:
                return (years) == 0;
            case MONTHS:
                return (years | months) == 0;
            case WEEKS:
                return (years | months | weeks) == 0;
            case DAYS:
                return (years | months | weeks | days) == 0;
            case HOURS:
                return (years | months | weeks | days | hours) == 0;
            case MINUTES:
                return (years | months | weeks | days | hours | minutes) == 0;
            case SECONDS:
                return (years | months | weeks | days | hours | minutes | seconds) == 0;
            case MILLIS:
                return (years | months | weeks | days | hours | minutes | seconds | milliSeconds) == 0;
            case MICROS:
                return (years | months | weeks | days | hours | minutes | seconds | milliSeconds | micros) == 0;
            case NANOS:
                return (years | months | weeks | days | hours | minutes | seconds | milliSeconds | micros | nanos) == 0;
        }
        return false;
    }

    public long to(ChronoUnit unit) {
        switch (unit) {
            case YEARS:
                return toYears();
            case MONTHS:
                return toMonths();
            case WEEKS:
                return toWeeks();
            case DAYS:
                return toDays();
            case HOURS:
                return toHours();
            case MINUTES:
                return toMinutes();
            case SECONDS:
                return toSeconds();
            case MILLIS:
                return toMillis();
            case MICROS:
                return toMicros();
            case NANOS:
                return toNanos();
        }
        return 0;
    }

    public long get(ChronoUnit unit) {
        switch (unit) {
            case YEARS:
                return years;
            case MONTHS:
                return months;
            case WEEKS:
                return weeks;
            case DAYS:
                return days;
            case HOURS:
                return hours;
            case MINUTES:
                return minutes;
            case SECONDS:
                return seconds;
            case MILLIS:
                return milliSeconds;
            case MICROS:
                return micros;
            case NANOS:
                return nanos;
        }
        return 0;
    }

    public long getMicros() {
        return micros;
    }

    public long getMilliSeconds() {
        return milliSeconds;
    }

    public long getSeconds() {
        return seconds;
    }

    public long getMinutes() {
        return minutes;
    }

    public long getHours() {
        return hours;
    }

    public long getDays() {
        return days;
    }

    public long getYears() {
        return years;
    }

    public long getMonths() {
        return months;
    }

    public long getWeeks() {
        return weeks;
    }

    public ChronoUnit getLargestUnit() {
        return largestUnit;
    }

    public ChronoUnit getSmallestUnit() {
        return smallestUnit;
    }

    public long toYears() {
        return timeMillis / 1000 / 3600 / 24 / 365;
    }

    public long toMonths() {
        return timeMillis / 1000 / 3600 / 24 / 30;
    }

    public long toWeeks() {
        return timeMillis / 1000 / 3600 / 24 / 7;
    }

    public long toDays() {
        return timeMillis / 1000 / 3600 / 24;
    }

    public long toHours() {
        return timeMillis / 1000 / 3600;
    }

    public long toMinutes() {
        return timeMillis / 1000 / 60;
    }

    public long toSeconds() {
        return timeMillis / 1000;
    }

    public double getTimeAsDoubleSeconds() {
        return timeMillis / 1000.0 + ((double) timeNanos) / 1E9;
    }

    public long toMillis() {
        return timeMillis;
    }

    public long toMicros() {
        return timeMillis * 1000 + timeNanos / 1000;
    }

    public long toNanos() {
        return timeMillis * 1000000 + timeNanos;
    }

    public long getTimeMillis() {
        return timeMillis;
    }

    public int getTimeNanos() {
        return timeNanos;
    }

    public Duration toDuration() {
        long millis=this.timeMillis;
        long nanos=timeNanos;
        // Separate seconds from milliseconds to avoid overflow
        long secondsFromMillis = millis / 1000;
        long remainingMillis = millis % 1000;
        long nanosFromMillis = remainingMillis * 1_000_000;

        // Total nanos = nanos from millis + additional nanos
        long totalNanos = nanosFromMillis + nanos;

        // Handle nano overflow by converting to additional seconds
        long secondsFromNanos = totalNanos / 1_000_000_000;
        long remainingNanos = totalNanos % 1_000_000_000;

        return Duration.ofSeconds(secondsFromMillis + secondsFromNanos, remainingNanos);
    }

    public NDuration withSmallestUnit(ChronoUnit smallestUnit) {
        NDuration d = new NDuration(toUnitsArray(), smallestUnit, largestUnit);
        if (this.timeMillis != d.timeMillis || this.timeNanos != d.timeNanos) {
            throw new IllegalArgumentException("unexpected");
        }
        return d;
    }

    public NDuration withLargestUnit(ChronoUnit largestUnit) {
        NDuration d = new NDuration(toUnitsArray(), smallestUnit, largestUnit);
        if (this.timeMillis != d.timeMillis || this.timeNanos != d.timeNanos) {
            throw new IllegalArgumentException("unexpected");
        }
        return d;
    }

    public NDuration withUnits(ChronoUnit smallestUnit, ChronoUnit largestUnit) {
        NDuration d = new NDuration(toUnitsArray(), smallestUnit, largestUnit);
        if (this.timeMillis != d.timeMillis || this.timeNanos != d.timeNanos) {
            throw new IllegalArgumentException("unexpected");
        }
        return d;
    }

    private boolean normalizeNegativeUnit(long[] values, ChronoUnit curr, ChronoUnit next, long multiplier) {
        if (values[curr.ordinal()] < 0) {
            if (values[curr.ordinal()] > 0) {
                long requiredMicros = (-values[next.ordinal()]) / multiplier;
                if (requiredMicros * multiplier < -values[next.ordinal()]) {
                    requiredMicros++;
                }
                requiredMicros = Math.min(requiredMicros, values[next.ordinal()]);
                if (requiredMicros > 0) {
                    values[curr.ordinal()] += requiredMicros * multiplier;
                    values[next.ordinal()] -= requiredMicros;
                }
                return values[curr.ordinal()] < 0;
            }
            return true;
        } else {
            return false;
        }
    }

    public NDuration neg() {
        long[] a = toUnitsArray();
        for (int i = 0; i < a.length; i++) {
            a[i] = -a[i];
        }
        return of(a, smallestUnit, largestUnit);
    }

    public NDuration add(NDuration other) {
        long[] a = toUnitsArray();
        long[] b = other.toUnitsArray();
        for (int i = 0; i < a.length; i++) {
            a[i] += b[i];
        }
        return of(a,
                smallestUnit.compareTo(other.getSmallestUnit()) < 0 ? smallestUnit : other.smallestUnit,
                largestUnit.compareTo(other.getSmallestUnit()) > 0 ? largestUnit : other.smallestUnit
        );
    }

    public NDuration mul(double other) {
        double ms = timeMillis * other;
        long msL = (long) (timeMillis * other);
        long ns = (long) (timeNanos * other + (ms - msL) * 1000000);
        return ofMillisAndNanos(msL, (int) ns).withUnits(smallestUnit, largestUnit);
    }

    public NDuration mul(long other) {
        long[] a = toUnitsArray();
        for (int i = 0; i < a.length; i++) {
            a[i] *= other;
        }
        return of(a, smallestUnit, largestUnit);
    }

    public NDuration subtract(NDuration other) {
        long[] a = toUnitsArray();
        long[] b = other.toUnitsArray();
        for (int i = 0; i < a.length; i++) {
            a[i] -= b[i];
        }
        return of(a,
                smallestUnit.compareTo(other.getSmallestUnit()) < 0 ? smallestUnit : other.smallestUnit,
                largestUnit.compareTo(other.getSmallestUnit()) > 0 ? largestUnit : other.smallestUnit
        );
    }

    public NDuration normalize() {
        long[] values = toUnitsArray();

        // Step 1: Carry overflow values upward (handle both positive and negative)
        // Only normalize between units that are within our smallest/largest range
        if (shouldNormalizeUnit(ChronoUnit.NANOS, ChronoUnit.MICROS)) {
            carryOverflow(values, ChronoUnit.NANOS, ChronoUnit.MICROS, 1000L);
        }
        if (shouldNormalizeUnit(ChronoUnit.MICROS, ChronoUnit.MILLIS)) {
            carryOverflow(values, ChronoUnit.MICROS, ChronoUnit.MILLIS, 1000L);
        }
        if (shouldNormalizeUnit(ChronoUnit.MILLIS, ChronoUnit.SECONDS)) {
            carryOverflow(values, ChronoUnit.MILLIS, ChronoUnit.SECONDS, 1000L);
        }
        if (shouldNormalizeUnit(ChronoUnit.SECONDS, ChronoUnit.MINUTES)) {
            carryOverflow(values, ChronoUnit.SECONDS, ChronoUnit.MINUTES, 60L);
        }
        if (shouldNormalizeUnit(ChronoUnit.MINUTES, ChronoUnit.HOURS)) {
            carryOverflow(values, ChronoUnit.MINUTES, ChronoUnit.HOURS, 60L);
        }
        if (shouldNormalizeUnit(ChronoUnit.HOURS, ChronoUnit.DAYS)) {
            carryOverflow(values, ChronoUnit.HOURS, ChronoUnit.DAYS, 24L);
        }
        if (shouldNormalizeUnit(ChronoUnit.DAYS, ChronoUnit.WEEKS)) {
            carryOverflow(values, ChronoUnit.DAYS, ChronoUnit.WEEKS, 7L);
        }
        if (shouldNormalizeUnit(ChronoUnit.WEEKS, ChronoUnit.MONTHS)) {
            carryOverflow(values, ChronoUnit.WEEKS, ChronoUnit.MONTHS, 4L);
        }
        if (shouldNormalizeUnit(ChronoUnit.MONTHS, ChronoUnit.YEARS)) {
            carryOverflow(values, ChronoUnit.MONTHS, ChronoUnit.YEARS, 12L);
        }

        // Step 2: Normalize mixed signs (e.g., -5 seconds + 200 millis → -4 seconds - 800 millis)
        // Only between units within our range
        if (shouldNormalizeUnit(ChronoUnit.NANOS, ChronoUnit.MICROS)) {
            normalizeMixedSigns(values, ChronoUnit.NANOS, ChronoUnit.MICROS, 1000L);
        }
        if (shouldNormalizeUnit(ChronoUnit.MICROS, ChronoUnit.MILLIS)) {
            normalizeMixedSigns(values, ChronoUnit.MICROS, ChronoUnit.MILLIS, 1000L);
        }
        if (shouldNormalizeUnit(ChronoUnit.MILLIS, ChronoUnit.SECONDS)) {
            normalizeMixedSigns(values, ChronoUnit.MILLIS, ChronoUnit.SECONDS, 1000L);
        }
        if (shouldNormalizeUnit(ChronoUnit.SECONDS, ChronoUnit.MINUTES)) {
            normalizeMixedSigns(values, ChronoUnit.SECONDS, ChronoUnit.MINUTES, 60L);
        }
        if (shouldNormalizeUnit(ChronoUnit.MINUTES, ChronoUnit.HOURS)) {
            normalizeMixedSigns(values, ChronoUnit.MINUTES, ChronoUnit.HOURS, 60L);
        }
        if (shouldNormalizeUnit(ChronoUnit.HOURS, ChronoUnit.DAYS)) {
            normalizeMixedSigns(values, ChronoUnit.HOURS, ChronoUnit.DAYS, 24L);
        }
        if (shouldNormalizeUnit(ChronoUnit.DAYS, ChronoUnit.WEEKS)) {
            normalizeMixedSigns(values, ChronoUnit.DAYS, ChronoUnit.WEEKS, 7L);
        }
        if (shouldNormalizeUnit(ChronoUnit.WEEKS, ChronoUnit.MONTHS)) {
            normalizeMixedSigns(values, ChronoUnit.WEEKS, ChronoUnit.MONTHS, 4L);
        }
        if (shouldNormalizeUnit(ChronoUnit.MONTHS, ChronoUnit.YEARS)) {
            normalizeMixedSigns(values, ChronoUnit.MONTHS, ChronoUnit.YEARS, 12L);
        }

        // Step 3: Zero out units outside our smallest/largest range
        for (ChronoUnit unit : ChronoUnit.values()) {
            if (unit.ordinal() < smallestUnit.ordinal() || unit.ordinal() > largestUnit.ordinal()) {
                values[unit.ordinal()] = 0;
            }
        }

        // Step 4: If we have overflow at the largest unit, keep it there
        // (don't carry beyond largestUnit)

        NDuration d = new NDuration(values, smallestUnit, largestUnit);
        if (this.timeMillis != d.timeMillis || this.timeNanos != d.timeNanos) {
            throw new IllegalArgumentException(NI18n.of("unexpected chronometer value: expected " +
                    timeMillis + "ms " + timeNanos + "ns, got " + d.timeMillis + "ms " + d.timeNanos + "ns"));
        }
        return d;
    }

    /**
     * Checks if we should normalize between two units based on smallestUnit and largestUnit.
     * We only normalize if both units are within the allowed range.
     */
    private boolean shouldNormalizeUnit(ChronoUnit current, ChronoUnit next) {
        int currentOrd = current.ordinal();
        int nextOrd = next.ordinal();
        int smallestOrd = smallestUnit.ordinal();
        int largestOrd = largestUnit.ordinal();

        // Both units must be within the range [smallestUnit, largestUnit]
        return currentOrd >= smallestOrd && currentOrd <= largestOrd &&
                nextOrd >= smallestOrd && nextOrd <= largestOrd;
    }

    /**
     * Carries overflow from current unit to next larger unit.
     * Handles both positive and negative values.
     */
    private void carryOverflow(long[] values, ChronoUnit current, ChronoUnit next, long multiplier) {
        long currentValue = values[current.ordinal()];

        if (currentValue >= multiplier || currentValue <= -multiplier) {
            long carry = currentValue / multiplier;
            long remainder = currentValue % multiplier;

            values[next.ordinal()] += carry;
            values[current.ordinal()] = remainder;
        }
    }

    /**
     * Normalizes mixed signs so that all units have the same sign.
     * For example: -5 seconds + 200 millis becomes -4 seconds - 800 millis
     * Or: 5 seconds - 200 millis becomes 4 seconds + 800 millis
     */
    private void normalizeMixedSigns(long[] values, ChronoUnit current, ChronoUnit next, long multiplier) {
        long currentValue = values[current.ordinal()];
        long nextValue = values[next.ordinal()];

        // If signs are different and current is non-zero
        if (currentValue != 0 && nextValue != 0) {
            boolean currentNegative = currentValue < 0;
            boolean nextNegative = nextValue < 0;

            if (currentNegative != nextNegative) {
                // They have different signs, normalize them
                if (nextNegative) {
                    // Next is negative, current is positive
                    // Borrow from next: convert positive current to negative
                    values[next.ordinal()] += 1;
                    values[current.ordinal()] -= multiplier;
                } else {
                    // Next is positive, current is negative
                    // Borrow from next: convert negative current to positive
                    values[next.ordinal()] -= 1;
                    values[current.ordinal()] += multiplier;
                }
            }
        }
    }

    public boolean isZero() {
        return ((timeMillis | timeNanos) == 0);
    }

    public long[] toUnitsArray() {
        long[] arr = new long[ChronoUnit.values().length];
        arr[ChronoUnit.NANOS.ordinal()] = nanos;
        arr[ChronoUnit.MICROS.ordinal()] = micros;
        arr[ChronoUnit.MILLIS.ordinal()] = milliSeconds;
        arr[ChronoUnit.SECONDS.ordinal()] = seconds;
        arr[ChronoUnit.MINUTES.ordinal()] = minutes;
        arr[ChronoUnit.HOURS.ordinal()] = hours;
        arr[ChronoUnit.DAYS.ordinal()] = days;
        arr[ChronoUnit.WEEKS.ordinal()] = weeks;
        arr[ChronoUnit.MONTHS.ordinal()] = months;
        arr[ChronoUnit.YEARS.ordinal()] = years;
        return arr;
    }

    public String toString(NDurationFormatMode formatMode) {
        return DefaultNDurationFormat.of(formatMode).format(this);
    }

    @Override
    public String toString() {
        return DefaultNDurationFormat.DEFAULT.format(this);
    }

    public static NOptional<NDuration> parse(String any) {
        if (any == null || any.trim().isEmpty()) {
            return NOptional.ofEmpty();
        }

        String input = any.trim().toLowerCase();

        try {
            // Try parsing as ISO-8601 duration format first
            if (input.startsWith("p")) {
                try {
                    java.time.Duration jdkDuration = java.time.Duration.parse(input);
                    long[] durationValues = new long[ChronoUnit.values().length];

                    long totalSeconds = jdkDuration.getSeconds();
                    long nanos = jdkDuration.getNano();

                    durationValues[ChronoUnit.SECONDS.ordinal()] = totalSeconds;

                    if (nanos > 0) {
                        durationValues[ChronoUnit.MILLIS.ordinal()] = nanos / 1_000_000L;
                    }

                    return NOptional.of(NDuration.of(durationValues));
                } catch (Exception e) {
                    return NOptional.ofEmpty();
                }
            }

            long[] durationValues = new long[ChronoUnit.values().length];

            // Parse simple duration formats with spaces and various abbreviations
            // Normalize the input: handle spaces, commas, and common abbreviations
            input = input.replace(',', '.')
                    .replace("  ", " ") // collapse multiple spaces
                    .trim();

            // Pattern to match number-unit pairs with optional spaces
            // This handles: "1mn 32s", "1h 30m 15s", "2d 5h", etc.
            String pattern = "(\\d+(?:\\.\\d+)?)\\s*([a-z]+)";
            java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher matcher = regex.matcher(input);

            boolean found = false;

            while (matcher.find()) {
                found = true;
                double value = Double.parseDouble(matcher.group(1));
                String unit = matcher.group(2);
                switch (unit) {
                    case "ns":
                    case "nano":
                    case "nanos":
                        durationValues[ChronoUnit.NANOS.ordinal()] += (long)value;
                        break;
                    case "us":
                    case "micro":
                    case "micros":
                        durationValues[ChronoUnit.MICROS.ordinal()] += (long)value;
                        break;
                    case "ms":
                    case "milli":
                    case "millis":
                        durationValues[ChronoUnit.MILLIS.ordinal()] += (long)value;
                        break;
                    case "s":
                    case "sec":
                    case "secs":
                    case "second":
                    case "seconds":
                        durationValues[ChronoUnit.SECONDS.ordinal()] += (long)value;
                        break;
                    case "m":
                    case "mn":
                    case "min":
                    case "mins":
                    case "minute":
                    case "minutes":
                        durationValues[ChronoUnit.MINUTES.ordinal()] += (long)value;
                        break;
                    case "h":
                    case "hr":
                    case "hrs":
                    case "hour":
                    case "hours":
                        durationValues[ChronoUnit.HOURS.ordinal()] += (long)value;
                        break;
                    case "d":
                    case "day":
                    case "days":
                        durationValues[ChronoUnit.DAYS.ordinal()] += (long)value;
                        break;
                    case "w":
                    case "week":
                    case "weeks":
                        durationValues[ChronoUnit.WEEKS.ordinal()] += (long)value;
                        break;
                    case "mon":
                    case "month":
                    case "months":
                        durationValues[ChronoUnit.MONTHS.ordinal()] += (long)value;
                        break;
                    case "y":
                    case "year":
                    case "years":
                        durationValues[ChronoUnit.YEARS.ordinal()] += (long)value;
                        break;
                    default:
                        return NOptional.ofNamedEmpty(NMsg.ofC("invalid unit value: %s", unit));
                }
            }

            // If no units found, try parsing as plain number (assume millis)
            if (!found) {
                try {
                    double millis = Double.parseDouble(input);
                    durationValues[ChronoUnit.MILLIS.ordinal()] = (long)millis;
                    return NOptional.of(NDuration.of(durationValues));
                } catch (NumberFormatException e) {
                    return NOptional.ofNamedEmpty(NMsg.ofC("invalid millis value: %s", input));
                }
            }
            return NOptional.of(NDuration.of(durationValues));
        } catch (Exception e) {
            return NOptional.ofNamedEmpty(NMsg.ofC("invalid value: %s", any));
        }
    }
}
