package net.thevpc.nuts.time;

import net.thevpc.nuts.elem.NElementSimple;
import net.thevpc.nuts.elem.NMapBy;
import net.thevpc.nuts.mon.NDurationFormatMode;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringUtils;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * NDuration represents a comprehensive and precise time duration
 * with support for multiple temporal units ranging from nanoseconds
 * up to years. Unlike {@link java.time.Duration}, which is limited
 * to seconds and nanoseconds, NDuration allows tracking weeks, months,
 * and years, providing full temporal granularity for long-term
 * scheduling, reporting, and time calculations.
 * <p>
 * <b>Key Features:</b>
 * <ul>
 *   <li>Supports all standard temporal units: nanoseconds, microseconds,
 *       milliseconds, seconds, minutes, hours, days, weeks, months, and years.</li>
 *   <li>Immutable once constructed.</li>
 *   <li>Normalization support to ensure unit values are consistent with
 *       conventional temporal rules (e.g., 1000 milliseconds → 1 second).</li>
 *   <li>Support for arithmetic operations: addition, subtraction, multiplication,
 *       negation.</li>
 *   <li>Conversion methods to {@link Duration}, milliseconds, seconds, or
 *       nanoseconds.</li>
 *   <li>Support for defining smallest and largest considered units for calculations.</li>
 * </ul>
 * <p>
 * <b>Usage Examples:</b>
 * <pre>{@code
 * // Create a duration of 2 hours, 30 minutes, and 45 seconds
 * NDuration duration = new NDuration(0, 0, 0, 0, 2, 30, 45, 0, 0, 0, ChronoUnit.NANOS, ChronoUnit.HOURS);
 *
 * // Convert to java.time.Duration
 * Duration javaDuration = duration.toDuration();
 *
 * // Add another duration
 * NDuration combined = duration.add(NDuration.ofUnit(15, ChronoUnit.MINUTES));
 *
 * // Normalize duration so that all unit values are consistent
 * NDuration normalized = combined.normalize();
 *
 * // Multiply duration by 2
 * NDuration doubled = normalized.mul(2);
 * }</pre>
 * <p>
 * Internally, NDuration stores milliseconds and nanoseconds for precision,
 * while keeping separate fields for each larger temporal unit. It can
 * operate with partial durations (e.g., only hours and minutes) or full
 * durations covering all units.
 */
public class NDuration implements Serializable, NElementSimple {
    /**
     * A constant representing a duration of zero.
     */
    public static final NDuration ZERO = ofMillis(0);

    // ----------------------
    // Internal Fields
    // ----------------------

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

    // ----------------------
    // Constructors
    // ----------------------

    /**
     * Constructs a NDuration with all possible units specified.
     * <p>
     * <b>Note:</b> The provided units are not automatically normalized.
     * Use {@link #normalize()} if normalization is required.
     *
     * @param years        number of years
     * @param months       number of months
     * @param weeks        number of weeks
     * @param days         number of days
     * @param hours        number of hours
     * @param minutes      number of minutes
     * @param seconds      number of seconds
     * @param milliSeconds number of milliseconds
     * @param micros       number of microseconds
     * @param nanos        number of nanoseconds
     * @param smallestUnit the smallest temporal unit considered in calculations
     * @param largestUnit  the largest temporal unit considered in calculations
     */
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
      /**
       * Apply units.
       */
        applyUnits();
    }

    /**
     * Constructs a NDuration from an array of unit values.
     *
     * @param values       array of unit values ordered by {@link ChronoUnit} ordinal
     * @param smallestUnit the smallest unit considered
     * @param largestUnit  the largest unit considered
     */
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
      /**
       * Apply units.
       */
        applyUnits();
    }

    /**
     * Constructs a NDuration from milliseconds and nanoseconds.
     *
     * @param timeMillis total milliseconds
     * @param timeNanos  remaining nanoseconds
     */
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


    /**
     * Constructs a NDuration from milliseconds and nanoseconds with specified
     * smallest and largest units.
     *
     * @param timeMillis   total milliseconds
     * @param timeNanos    remaining nanoseconds
     * @param smallestUnit the smallest unit considered
     * @param largestUnit  the largest unit considered
     */
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
          /**
           * Apply units.
           */
            applyUnits();
        }
    }


    // ----------------------
    // Factory Methods
    // ----------------------

    /**
     * Returns a NDuration representing the duration between two instants.
     *
     * @param start the start instant
     * @param end   the end instant
     * @return duration between start and end
     */
    public static NDuration between(Instant start, Instant end) {
        return NDuration.ofDuration(Duration.between(start, end));
    }

    /**
     * Rebuild time nanos.
     *
     * @return rebuild time nanos result
     */
    private int rebuildTimeNanos() {
      /**
       * Return.
       *
       * @param 1000 1000
       */
        return (int) (nanos + micros * 1000);
    }

    /**
     * Rebuild time millis.
     *
     * @return rebuild time millis result
     */
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

    /**
     * Apply units.
     *
     * @return apply units result
     */
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

    /**
     * Detect smallest unit.
     *
     * @return detect smallest unit result
     */
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
                                                                                          /**
                                                                                           * Normalize.
                                                                                           *
                                                                                           * @param ChronoUnit.FOREVER chrono unit.forever
                                                                                           */
                                                                                            normalize(ChronoUnit.FOREVER);
        }
    }

    /**
     * Detect largest unit.
     *
     * @return detect largest unit result
     */
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
                                                                                          /**
                                                                                           * Normalize.
                                                                                           *
                                                                                           * @param ChronoUnit.FOREVER chrono unit.forever
                                                                                           */
                                                                                            normalize(ChronoUnit.FOREVER);
        }
    }


    /**
     * Normalize.
     *
     * @param smallestUnit smallest unit
     * @return normalize result
     */
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

    /**
     * Creates a NDuration from total nanoseconds.
     *
     * @param durationNanos total nanoseconds
     * @return corresponding NDuration
     */
    public static NDuration ofNanos(long durationNanos) {
        long ms = durationNanos / 1000000;
        int ns = (int) (durationNanos % 1000000);
        return new NDuration(ms, ns);
    }

    /**
     * Creates a new instance of of nanos.
     *
     * @param durationNanos duration nanos
     * @param smallestUnit smallest unit
     * @param largestUnit largest unit
     * @return of nanos result
     */
    public static NDuration ofNanos(long durationNanos, ChronoUnit smallestUnit, ChronoUnit largestUnit) {
        long ms = durationNanos / 1000000;
        int ns = (int) (durationNanos % 1000000);
        return new NDuration(ms, ns, smallestUnit, largestUnit);
    }

    /**
     * Creates a new instance of of nanos only.
     *
     * @param durationNanos duration nanos
     * @return of nanos only result
     */
    public static NDuration ofNanosOnly(long durationNanos) {
        /**
         * Creates a new instance of of unit only.
         *
         * @param durationNanos duration nanos
         * @param ChronoUnit.MILLIS chrono unit.millis
         * @return of unit only result
         */
        return ofUnitOnly(durationNanos, ChronoUnit.MILLIS);
    }

    /**
     * Creates a new instance of of millis only.
     *
     * @param durationMillis duration millis
     * @return of millis only result
     */
    public static NDuration ofMillisOnly(long durationMillis) {
        /**
         * Creates a new instance of of unit only.
         *
         * @param durationMillis duration millis
         * @param ChronoUnit.MILLIS chrono unit.millis
         * @return of unit only result
         */
        return ofUnitOnly(durationMillis, ChronoUnit.MILLIS);
    }

    /**
     * Creates a new instance of of seconds only.
     *
     * @param durationSeconds duration seconds
     * @return of seconds only result
     */
    public static NDuration ofSecondsOnly(long durationSeconds) {
        /**
         * Creates a new instance of of unit only.
         *
         * @param durationSeconds duration seconds
         * @param ChronoUnit.SECONDS chrono unit.seconds
         * @return of unit only result
         */
        return ofUnitOnly(durationSeconds, ChronoUnit.SECONDS);
    }

    /**
     * Creates a new instance of of minutes only.
     *
     * @param durationMinutes duration minutes
     * @return of minutes only result
     */
    public static NDuration ofMinutesOnly(long durationMinutes) {
        /**
         * Creates a new instance of of unit only.
         *
         * @param durationMinutes duration minutes
         * @param ChronoUnit.MINUTES chrono unit.minutes
         * @return of unit only result
         */
        return ofUnitOnly(durationMinutes, ChronoUnit.MINUTES);
    }

    /**
     * Creates a new instance of of hours only.
     *
     * @param durationHours duration hours
     * @return of hours only result
     */
    public static NDuration ofHoursOnly(long durationHours) {
        /**
         * Creates a new instance of of unit only.
         *
         * @param durationHours duration hours
         * @param ChronoUnit.HOURS chrono unit.hours
         * @return of unit only result
         */
        return ofUnitOnly(durationHours, ChronoUnit.HOURS);
    }

    /**
     * Creates a new instance of of days only.
     *
     * @param durationDays duration days
     * @return of days only result
     */
    public static NDuration ofDaysOnly(long durationDays) {
        /**
         * Creates a new instance of of unit only.
         *
         * @param durationDays duration days
         * @param ChronoUnit.DAYS chrono unit.days
         * @return of unit only result
         */
        return ofUnitOnly(durationDays, ChronoUnit.DAYS);
    }

    /**
     * Creates a new instance of of weeks only.
     *
     * @param durationWeeks duration weeks
     * @return of weeks only result
     */
    public static NDuration ofWeeksOnly(long durationWeeks) {
        /**
         * Creates a new instance of of unit only.
         *
         * @param durationWeeks duration weeks
         * @param ChronoUnit.WEEKS chrono unit.weeks
         * @return of unit only result
         */
        return ofUnitOnly(durationWeeks, ChronoUnit.WEEKS);
    }

    /**
     * Creates a new instance of of month only.
     *
     * @param durationMonths duration months
     * @return of month only result
     */
    public static NDuration ofMonthOnly(long durationMonths) {
        /**
         * Creates a new instance of of unit only.
         *
         * @param durationMonths duration months
         * @param ChronoUnit.MONTHS chrono unit.months
         * @return of unit only result
         */
        return ofUnitOnly(durationMonths, ChronoUnit.MONTHS);
    }

    /**
     * Creates a new instance of of years only.
     *
     * @param durationYears duration years
     * @return of years only result
     */
    public static NDuration ofYearsOnly(long durationYears) {
        /**
         * Creates a new instance of of unit only.
         *
         * @param durationYears duration years
         * @param ChronoUnit.YEARS chrono unit.years
         * @return of unit only result
         */
        return ofUnitOnly(durationYears, ChronoUnit.YEARS);
    }

    /**
     * Creates a new instance of of seconds.
     *
     * @param durationSeconds duration seconds
     * @return of seconds result
     */
    public static NDuration ofSeconds(long durationSeconds) {
        /**
         * Creates a new instance of of unit.
         *
         * @param durationSeconds duration seconds
         * @param ChronoUnit.SECONDS chrono unit.seconds
         * @return of unit result
         */
        return ofUnit(durationSeconds, ChronoUnit.SECONDS);
    }

    /**
     * Creates a new instance of of minutes.
     *
     * @param durationMinutes duration minutes
     * @return of minutes result
     */
    public static NDuration ofMinutes(long durationMinutes) {
        /**
         * Creates a new instance of of unit.
         *
         * @param durationMinutes duration minutes
         * @param ChronoUnit.MINUTES chrono unit.minutes
         * @return of unit result
         */
        return ofUnit(durationMinutes, ChronoUnit.MINUTES);
    }

    /**
     * Creates a new instance of of hours.
     *
     * @param durationHours duration hours
     * @return of hours result
     */
    public static NDuration ofHours(long durationHours) {
        /**
         * Creates a new instance of of unit.
         *
         * @param durationHours duration hours
         * @param ChronoUnit.HOURS chrono unit.hours
         * @return of unit result
         */
        return ofUnit(durationHours, ChronoUnit.HOURS);
    }

    /**
     * Creates a new instance of of days.
     *
     * @param durationDays duration days
     * @return of days result
     */
    public static NDuration ofDays(long durationDays) {
        /**
         * Creates a new instance of of unit.
         *
         * @param durationDays duration days
         * @param ChronoUnit.DAYS chrono unit.days
         * @return of unit result
         */
        return ofUnit(durationDays, ChronoUnit.DAYS);
    }

    /**
     * Creates a new instance of of weeks.
     *
     * @param durationWeeks duration weeks
     * @return of weeks result
     */
    public static NDuration ofWeeks(long durationWeeks) {
        /**
         * Creates a new instance of of unit.
         *
         * @param durationWeeks duration weeks
         * @param ChronoUnit.WEEKS chrono unit.weeks
         * @return of unit result
         */
        return ofUnit(durationWeeks, ChronoUnit.WEEKS);
    }

    /**
     * Creates a new instance of of month.
     *
     * @param durationMonths duration months
     * @return of month result
     */
    public static NDuration ofMonth(long durationMonths) {
        /**
         * Creates a new instance of of unit.
         *
         * @param durationMonths duration months
         * @param ChronoUnit.MONTHS chrono unit.months
         * @return of unit result
         */
        return ofUnit(durationMonths, ChronoUnit.MONTHS);
    }

    /**
     * Creates a new instance of of years.
     *
     * @param durationYears duration years
     * @return of years result
     */
    public static NDuration ofYears(long durationYears) {
        /**
         * Creates a new instance of of unit.
         *
         * @param durationYears duration years
         * @param ChronoUnit.YEARS chrono unit.years
         * @return of unit result
         */
        return ofUnit(durationYears, ChronoUnit.YEARS);
    }

    /**
     * Creates a new instance of of unit only.
     *
     * @param durationInUnit duration in unit
     * @param unit unit
     * @return of unit only result
     */
    public static NDuration ofUnitOnly(long durationInUnit, ChronoUnit unit) {
        long[] values = new long[ChronoUnit.values().length];
        values[unit.ordinal()] = durationInUnit;
        return new NDuration(values, null, null);
    }

    /**
     * Creates a new instance of of unit.
     *
     * @param durationInUnit duration in unit
     * @param unit unit
     * @return of unit result
     */
    public static NDuration ofUnit(long durationInUnit, ChronoUnit unit) {
        /**
         * Creates a new instance of of unit only.
         *
         * @param durationInUnit duration in unit
         * @param unit).normalize( unit).normalize(
         * @return of unit only result
         */
        return ofUnitOnly(durationInUnit, unit).normalize();
    }

    /**
     * Creates a new instance of of millis.
     *
     * @param durationMillis duration millis
     * @return of millis result
     */
    public static NDuration ofMillis(long durationMillis) {
        return new NDuration(durationMillis, 0);
    }

    /**
     * Creates a new instance of of millis.
     *
     * @param durationMillis duration millis
     * @param smallestUnit smallest unit
     * @param largestUnit largest unit
     * @return of millis result
     */
    public static NDuration ofMillis(long durationMillis, ChronoUnit smallestUnit, ChronoUnit largestUnit) {
        return new NDuration(durationMillis, 0, smallestUnit, largestUnit);
    }

    /**
     * Creates a new instance of of duration.
     *
     * @param duration duration
     * @return of duration result
     */
    public static NDuration ofDuration(Duration duration) {
        return ofSecondsAndNanos(
                duration.getSeconds(),
                duration.getNano()
        );
    }

    /**
     * Creates a new instance of of millis and nanos.
     *
     * @param durationMillis duration millis
     * @param nanos nanos
     * @return of millis and nanos result
     */
    public static NDuration ofMillisAndNanos(long durationMillis, int nanos) {
        return new NDuration(durationMillis, nanos);
    }

    /**
     * Creates a new instance of of.
     *
     * @param values values
     * @param smallestUnit smallest unit
     * @param largestUnit largest unit
     * @return of result
     */
    public static NDuration of(long[] values, ChronoUnit smallestUnit, ChronoUnit largestUnit) {
        return new NDuration(values, smallestUnit, largestUnit);
    }

    /**
     * Creates a new instance of of.
     *
     * @param values values
     * @return of result
     */
    public static NDuration of(long[] values) {
        /**
         * Creates a new instance of of.
         *
         * @param values values
         * @param null null
         * @param null null
         * @return of result
         */
        return of(values, null, null);
    }

    /**
     * Creates a new instance of of seconds and nanos.
     *
     * @param durationSeconds duration seconds
     * @param nanos nanos
     * @return of seconds and nanos result
     */
    public static NDuration ofSecondsAndNanos(long durationSeconds, long nanos) {
        long millis = durationSeconds * 1000 + nanos / 1000000;
        int ns = (int) (nanos % 1000000);
        /**
         * Creates a new instance of of millis and nanos.
         *
         * @param millis millis
         * @param ns ns
         * @return of millis and nanos result
         */
        return ofMillisAndNanos(millis, ns);
    }

    /**
     * Nanos.
     *
     * @return nanos result
     */
    public long nanos() {
        return nanos;
    }

    /**
     * First non zero up.
     *
     * @param unit unit
     * @return first non zero up result
     */
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

    /**
     * First non zero down.
     *
     * @param unit unit
     * @return first non zero down result
     */
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

    /**
     * Checks if is zero down.
     *
     * @param unit unit
     * @return is zero down result
     */
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

    /**
     * Checks if is zero up.
     *
     * @param unit unit
     * @return is zero up result
     */
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

    /**
     * Converts to to.
     *
     * @param unit unit
     * @return to result
     */
    public long to(ChronoUnit unit) {
        switch (unit) {
            case YEARS:
                /**
                 * Converts to years.
                 *
                 * @return to years result
                 */
                return toYears();
            case MONTHS:
                /**
                 * Converts to months.
                 *
                 * @return to months result
                 */
                return toMonths();
            case WEEKS:
                /**
                 * Converts to weeks.
                 *
                 * @return to weeks result
                 */
                return toWeeks();
            case DAYS:
                /**
                 * Converts to days.
                 *
                 * @return to days result
                 */
                return toDays();
            case HOURS:
                /**
                 * Converts to hours.
                 *
                 * @return to hours result
                 */
                return toHours();
            case MINUTES:
                /**
                 * Converts to minutes.
                 *
                 * @return to minutes result
                 */
                return toMinutes();
            case SECONDS:
                /**
                 * Converts to seconds.
                 *
                 * @return to seconds result
                 */
                return toSeconds();
            case MILLIS:
                /**
                 * Converts to millis.
                 *
                 * @return to millis result
                 */
                return toMillis();
            case MICROS:
                /**
                 * Converts to micros.
                 *
                 * @return to micros result
                 */
                return toMicros();
            case NANOS:
                /**
                 * Converts to nanos.
                 *
                 * @return to nanos result
                 */
                return toNanos();
        }
        return 0;
    }

    /**
     * Returns the get.
     *
     * @param unit unit
     * @return get result
     */
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

    /**
     * Micros.
     *
     * @return micros result
     */
    public long micros() {
        return micros;
    }

    /**
     * Milli seconds.
     *
     * @return milli seconds result
     */
    public long milliSeconds() {
        return milliSeconds;
    }

    /**
     * Seconds.
     *
     * @return seconds result
     */
    public long seconds() {
        return seconds;
    }

    /**
     * Minutes.
     *
     * @return minutes result
     */
    public long minutes() {
        return minutes;
    }

    /**
     * Hours.
     *
     * @return hours result
     */
    public long hours() {
        return hours;
    }

    /**
     * Days.
     *
     * @return days result
     */
    public long days() {
        return days;
    }

    /**
     * Years.
     *
     * @return years result
     */
    public long years() {
        return years;
    }

    /**
     * Months.
     *
     * @return months result
     */
    public long months() {
        return months;
    }

    /**
     * Weeks.
     *
     * @return weeks result
     */
    public long weeks() {
        return weeks;
    }

    /**
     * Largest unit.
     *
     * @return largest unit result
     */
    public ChronoUnit largestUnit() {
        return largestUnit;
    }

    /**
     * Smallest unit.
     *
     * @return smallest unit result
     */
    public ChronoUnit smallestUnit() {
        return smallestUnit;
    }

    /**
     * Converts to years.
     *
     * @return to years result
     */
    public long toYears() {
        return timeMillis / 1000 / 3600 / 24 / 365;
    }

    /**
     * Converts to months.
     *
     * @return to months result
     */
    public long toMonths() {
        return timeMillis / 1000 / 3600 / 24 / 30;
    }

    /**
     * Converts to weeks.
     *
     * @return to weeks result
     */
    public long toWeeks() {
        return timeMillis / 1000 / 3600 / 24 / 7;
    }

    /**
     * Converts to days.
     *
     * @return to days result
     */
    public long toDays() {
        return timeMillis / 1000 / 3600 / 24;
    }

    /**
     * Converts to hours.
     *
     * @return to hours result
     */
    public long toHours() {
        return timeMillis / 1000 / 3600;
    }

    /**
     * Converts to minutes.
     *
     * @return to minutes result
     */
    public long toMinutes() {
        return timeMillis / 1000 / 60;
    }

    /**
     * Converts to seconds.
     *
     * @return to seconds result
     */
    public long toSeconds() {
        return timeMillis / 1000;
    }

    /**
     * Time as double seconds.
     *
     * @return time as double seconds result
     */
    public double timeAsDoubleSeconds() {
        return timeMillis / 1000.0 + ((double) timeNanos) / 1E9;
    }

    /**
     * Converts to millis.
     *
     * @return to millis result
     */
    public long toMillis() {
        return timeMillis;
    }

    /**
     * Converts to micros.
     *
     * @return to micros result
     */
    public long toMicros() {
        return timeMillis * 1000 + timeNanos / 1000;
    }

    /**
     * Converts to nanos.
     *
     * @return to nanos result
     */
    public long toNanos() {
        return timeMillis * 1000000 + timeNanos;
    }

    /**
     * Time millis.
     *
     * @return time millis result
     */
    public long timeMillis() {
        return timeMillis;
    }

    /**
     * Time nanos.
     *
     * @return time nanos result
     */
    public int timeNanos() {
        return timeNanos;
    }

    /**
     * Converts to duration.
     *
     * @return to duration result
     */
    public Duration toDuration() {
        long millis = this.timeMillis;
        long nanos = timeNanos;
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


    /**
     * Truncated to millis.
     *
     * @return truncated to millis result
     */
    public NDuration truncatedToMillis() {
        /**
         * Truncated to.
         *
         * @param ChronoUnit.MILLIS chrono unit.millis
         * @return truncated to result
         */
        return truncatedTo(ChronoUnit.MILLIS);
    }

    /**
     * Truncated to seconds.
     *
     * @return truncated to seconds result
     */
    public NDuration truncatedToSeconds() {
        /**
         * Truncated to.
         *
         * @param ChronoUnit.SECONDS chrono unit.seconds
         * @return truncated to result
         */
        return truncatedTo(ChronoUnit.SECONDS);
    }

    /**
     * Truncated to minutes.
     *
     * @return truncated to minutes result
     */
    public NDuration truncatedToMinutes() {
        /**
         * Truncated to.
         *
         * @param ChronoUnit.MINUTES chrono unit.minutes
         * @return truncated to result
         */
        return truncatedTo(ChronoUnit.MINUTES);
    }

    /**
     * Truncated to days.
     *
     * @return truncated to days result
     */
    public NDuration truncatedToDays() {
        /**
         * Truncated to.
         *
         * @param ChronoUnit.DAYS chrono unit.days
         * @return truncated to result
         */
        return truncatedTo(ChronoUnit.DAYS);
    }

    /**
     * Truncated to.
     *
     * @param smallestUnit smallest unit
     * @return truncated to result
     */
    public NDuration truncatedTo(ChronoUnit smallestUnit) {
        /**
         * With smallest unit.
         *
         * @param smallestUnit smallest unit
         * @return with smallest unit result
         */
        return withSmallestUnit(smallestUnit);
    }

    /**
     * With smallest unit.
     *
     * @param smallestUnit smallest unit
     * @return with smallest unit result
     */
    public NDuration withSmallestUnit(ChronoUnit smallestUnit) {
        NDuration d = new NDuration(toUnitsArray(), smallestUnit, largestUnit);
//        if (this.timeMillis != d.timeMillis || this.timeNanos != d.timeNanos) {
//            throw new IllegalArgumentException("unexpected");
//        }
        return d;
    }

    /**
     * Collapsed to.
     *
     * @param largestUnit largest unit
     * @return collapsed to result
     */
    public NDuration collapsedTo(ChronoUnit largestUnit) {
        /**
         * With largest unit.
         *
         * @param largestUnit largest unit
         * @return with largest unit result
         */
        return withLargestUnit(largestUnit);
    }

    /**
     * With largest unit.
     *
     * @param largestUnit largest unit
     * @return with largest unit result
     */
    public NDuration withLargestUnit(ChronoUnit largestUnit) {
        NDuration d = new NDuration(toUnitsArray(), smallestUnit, largestUnit);
//        if (this.timeMillis != d.timeMillis || this.timeNanos != d.timeNanos) {
//            throw new IllegalArgumentException("unexpected");
//        }
        return d;
    }

    /**
     * Clamped to.
     *
     * @param smallestUnit smallest unit
     * @param largestUnit largest unit
     * @return clamped to result
     */
    public NDuration clampedTo(ChronoUnit smallestUnit, ChronoUnit largestUnit) {
        /**
         * With units.
         *
         * @param smallestUnit smallest unit
         * @param largestUnit largest unit
         * @return with units result
         */
        return withUnits(smallestUnit, largestUnit);
    }

    /**
     * With units.
     *
     * @param smallestUnit smallest unit
     * @param largestUnit largest unit
     * @return with units result
     */
    public NDuration withUnits(ChronoUnit smallestUnit, ChronoUnit largestUnit) {
        NDuration d = new NDuration(toUnitsArray(), smallestUnit, largestUnit);
//        if (this.timeMillis != d.timeMillis || this.timeNanos != d.timeNanos) {
//            throw new IllegalArgumentException("unexpected");
//        }
        return d;
    }

    /**
     * Normalize negative unit.
     *
     * @param values values
     * @param curr curr
     * @param next next
     * @param multiplier multiplier
     * @return normalize negative unit result
     */
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

    /**
     * Neg.
     *
     * @return neg result
     */
    public NDuration neg() {
        long[] a = toUnitsArray();
        for (int i = 0; i < a.length; i++) {
            a[i] = -a[i];
        }
        /**
         * Creates a new instance of of.
         *
         * @param a a
         * @param smallestUnit smallest unit
         * @param largestUnit largest unit
         * @return of result
         */
        return of(a, smallestUnit, largestUnit);
    }

    /**
     * Adds add.
     *
     * @param other other
     * @return add result
     */
    public NDuration add(NDuration other) {
        long[] a = toUnitsArray();
        long[] b = other.toUnitsArray();
        for (int i = 0; i < a.length; i++) {
            a[i] += b[i];
        }
        return of(a,
                smallestUnit.compareTo(other.smallestUnit()) < 0 ? smallestUnit : other.smallestUnit,
                largestUnit.compareTo(other.smallestUnit()) > 0 ? largestUnit : other.smallestUnit
        );
    }

    /**
     * Mul.
     *
     * @param other other
     * @return mul result
     */
    public NDuration mul(double other) {
        double ms = timeMillis * other;
        long msL = (long) (timeMillis * other);
        long ns = (long) (timeNanos * other + (ms - msL) * 1000000);
        /**
         * Creates a new instance of of millis and nanos.
         *
         * @param msL ms l
         * @param ns).withUnits(smallestUnit ns).with units(smallest unit
         * @param largestUnit largest unit
         * @return of millis and nanos result
         */
        return ofMillisAndNanos(msL, (int) ns).withUnits(smallestUnit, largestUnit);
    }

    /**
     * Mul.
     *
     * @param other other
     * @return mul result
     */
    public NDuration mul(long other) {
        long[] a = toUnitsArray();
        for (int i = 0; i < a.length; i++) {
            a[i] *= other;
        }
        /**
         * Creates a new instance of of.
         *
         * @param a a
         * @param smallestUnit smallest unit
         * @param largestUnit largest unit
         * @return of result
         */
        return of(a, smallestUnit, largestUnit);
    }

    /**
     * Subtract.
     *
     * @param other other
     * @return subtract result
     */
    public NDuration subtract(NDuration other) {
        long[] a = toUnitsArray();
        long[] b = other.toUnitsArray();
        for (int i = 0; i < a.length; i++) {
            a[i] -= b[i];
        }
        return of(a,
                smallestUnit.compareTo(other.smallestUnit()) < 0 ? smallestUnit : other.smallestUnit,
                largestUnit.compareTo(other.smallestUnit()) > 0 ? largestUnit : other.smallestUnit
        );
    }

    /**
     * Normalize.
     *
     * @return normalize result
     */
    public NDuration normalize() {
        long[] values = toUnitsArray();

        // Step 1: Carry overflow values upward (handle both positive and negative)
        // Only normalize between units that are within our smallest/largest range
        if (shouldNormalizeUnit(ChronoUnit.NANOS, ChronoUnit.MICROS)) {
          /**
           * Carry overflow.
           *
           * @param values values
           * @param ChronoUnit.NANOS chrono unit.nanos
           * @param ChronoUnit.MICROS chrono unit.micros
           * @param 1000L 1000 l
           */
            carryOverflow(values, ChronoUnit.NANOS, ChronoUnit.MICROS, 1000L);
        }
        if (shouldNormalizeUnit(ChronoUnit.MICROS, ChronoUnit.MILLIS)) {
          /**
           * Carry overflow.
           *
           * @param values values
           * @param ChronoUnit.MICROS chrono unit.micros
           * @param ChronoUnit.MILLIS chrono unit.millis
           * @param 1000L 1000 l
           */
            carryOverflow(values, ChronoUnit.MICROS, ChronoUnit.MILLIS, 1000L);
        }
        if (shouldNormalizeUnit(ChronoUnit.MILLIS, ChronoUnit.SECONDS)) {
          /**
           * Carry overflow.
           *
           * @param values values
           * @param ChronoUnit.MILLIS chrono unit.millis
           * @param ChronoUnit.SECONDS chrono unit.seconds
           * @param 1000L 1000 l
           */
            carryOverflow(values, ChronoUnit.MILLIS, ChronoUnit.SECONDS, 1000L);
        }
        if (shouldNormalizeUnit(ChronoUnit.SECONDS, ChronoUnit.MINUTES)) {
          /**
           * Carry overflow.
           *
           * @param values values
           * @param ChronoUnit.SECONDS chrono unit.seconds
           * @param ChronoUnit.MINUTES chrono unit.minutes
           * @param 60L 60 l
           */
            carryOverflow(values, ChronoUnit.SECONDS, ChronoUnit.MINUTES, 60L);
        }
        if (shouldNormalizeUnit(ChronoUnit.MINUTES, ChronoUnit.HOURS)) {
          /**
           * Carry overflow.
           *
           * @param values values
           * @param ChronoUnit.MINUTES chrono unit.minutes
           * @param ChronoUnit.HOURS chrono unit.hours
           * @param 60L 60 l
           */
            carryOverflow(values, ChronoUnit.MINUTES, ChronoUnit.HOURS, 60L);
        }
        if (shouldNormalizeUnit(ChronoUnit.HOURS, ChronoUnit.DAYS)) {
          /**
           * Carry overflow.
           *
           * @param values values
           * @param ChronoUnit.HOURS chrono unit.hours
           * @param ChronoUnit.DAYS chrono unit.days
           * @param 24L 24 l
           */
            carryOverflow(values, ChronoUnit.HOURS, ChronoUnit.DAYS, 24L);
        }
        if (shouldNormalizeUnit(ChronoUnit.DAYS, ChronoUnit.WEEKS)) {
          /**
           * Carry overflow.
           *
           * @param values values
           * @param ChronoUnit.DAYS chrono unit.days
           * @param ChronoUnit.WEEKS chrono unit.weeks
           * @param 7L 7 l
           */
            carryOverflow(values, ChronoUnit.DAYS, ChronoUnit.WEEKS, 7L);
        }
        if (shouldNormalizeUnit(ChronoUnit.WEEKS, ChronoUnit.MONTHS)) {
          /**
           * Carry overflow.
           *
           * @param values values
           * @param ChronoUnit.WEEKS chrono unit.weeks
           * @param ChronoUnit.MONTHS chrono unit.months
           * @param 4L 4 l
           */
            carryOverflow(values, ChronoUnit.WEEKS, ChronoUnit.MONTHS, 4L);
        }
        if (shouldNormalizeUnit(ChronoUnit.MONTHS, ChronoUnit.YEARS)) {
          /**
           * Carry overflow.
           *
           * @param values values
           * @param ChronoUnit.MONTHS chrono unit.months
           * @param ChronoUnit.YEARS chrono unit.years
           * @param 12L 12 l
           */
            carryOverflow(values, ChronoUnit.MONTHS, ChronoUnit.YEARS, 12L);
        }

        // Step 2: Normalize mixed signs (e.g., -5 seconds + 200 millis → -4 seconds - 800 millis)
        // Only between units within our range
        if (shouldNormalizeUnit(ChronoUnit.NANOS, ChronoUnit.MICROS)) {
          /**
           * Normalize mixed signs.
           *
           * @param values values
           * @param ChronoUnit.NANOS chrono unit.nanos
           * @param ChronoUnit.MICROS chrono unit.micros
           * @param 1000L 1000 l
           */
            normalizeMixedSigns(values, ChronoUnit.NANOS, ChronoUnit.MICROS, 1000L);
        }
        if (shouldNormalizeUnit(ChronoUnit.MICROS, ChronoUnit.MILLIS)) {
          /**
           * Normalize mixed signs.
           *
           * @param values values
           * @param ChronoUnit.MICROS chrono unit.micros
           * @param ChronoUnit.MILLIS chrono unit.millis
           * @param 1000L 1000 l
           */
            normalizeMixedSigns(values, ChronoUnit.MICROS, ChronoUnit.MILLIS, 1000L);
        }
        if (shouldNormalizeUnit(ChronoUnit.MILLIS, ChronoUnit.SECONDS)) {
          /**
           * Normalize mixed signs.
           *
           * @param values values
           * @param ChronoUnit.MILLIS chrono unit.millis
           * @param ChronoUnit.SECONDS chrono unit.seconds
           * @param 1000L 1000 l
           */
            normalizeMixedSigns(values, ChronoUnit.MILLIS, ChronoUnit.SECONDS, 1000L);
        }
        if (shouldNormalizeUnit(ChronoUnit.SECONDS, ChronoUnit.MINUTES)) {
          /**
           * Normalize mixed signs.
           *
           * @param values values
           * @param ChronoUnit.SECONDS chrono unit.seconds
           * @param ChronoUnit.MINUTES chrono unit.minutes
           * @param 60L 60 l
           */
            normalizeMixedSigns(values, ChronoUnit.SECONDS, ChronoUnit.MINUTES, 60L);
        }
        if (shouldNormalizeUnit(ChronoUnit.MINUTES, ChronoUnit.HOURS)) {
          /**
           * Normalize mixed signs.
           *
           * @param values values
           * @param ChronoUnit.MINUTES chrono unit.minutes
           * @param ChronoUnit.HOURS chrono unit.hours
           * @param 60L 60 l
           */
            normalizeMixedSigns(values, ChronoUnit.MINUTES, ChronoUnit.HOURS, 60L);
        }
        if (shouldNormalizeUnit(ChronoUnit.HOURS, ChronoUnit.DAYS)) {
          /**
           * Normalize mixed signs.
           *
           * @param values values
           * @param ChronoUnit.HOURS chrono unit.hours
           * @param ChronoUnit.DAYS chrono unit.days
           * @param 24L 24 l
           */
            normalizeMixedSigns(values, ChronoUnit.HOURS, ChronoUnit.DAYS, 24L);
        }
        if (shouldNormalizeUnit(ChronoUnit.DAYS, ChronoUnit.WEEKS)) {
          /**
           * Normalize mixed signs.
           *
           * @param values values
           * @param ChronoUnit.DAYS chrono unit.days
           * @param ChronoUnit.WEEKS chrono unit.weeks
           * @param 7L 7 l
           */
            normalizeMixedSigns(values, ChronoUnit.DAYS, ChronoUnit.WEEKS, 7L);
        }
        if (shouldNormalizeUnit(ChronoUnit.WEEKS, ChronoUnit.MONTHS)) {
          /**
           * Normalize mixed signs.
           *
           * @param values values
           * @param ChronoUnit.WEEKS chrono unit.weeks
           * @param ChronoUnit.MONTHS chrono unit.months
           * @param 4L 4 l
           */
            normalizeMixedSigns(values, ChronoUnit.WEEKS, ChronoUnit.MONTHS, 4L);
        }
        if (shouldNormalizeUnit(ChronoUnit.MONTHS, ChronoUnit.YEARS)) {
          /**
           * Normalize mixed signs.
           *
           * @param values values
           * @param ChronoUnit.MONTHS chrono unit.months
           * @param ChronoUnit.YEARS chrono unit.years
           * @param 12L 12 l
           */
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
//        if (this.timeMillis != d.timeMillis || this.timeNanos != d.timeNanos) {
//            throw new IllegalArgumentException(NI18n.of("unexpected chronometer value: expected " +
//                    timeMillis + "ms " + timeNanos + "ns, got " + d.timeMillis + "ms " + d.timeNanos + "ns"));
//        }
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

    /**
     * Checks if is zero.
     *
     * @return is zero result
     */
    public boolean isZero() {
      /**
       * Return.
       *
       * @param 0 0
       */
        return ((timeMillis | timeNanos) == 0);
    }

    /**
     * Converts to units array.
     *
     * @return to units array result
     */
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

    /**
     * Returns formatted duration string according to specified format mode.
     *
     * @param formatMode duration format mode
     * @return formatted duration string
     */
    public String toString(NDurationFormatMode formatMode) {
        return DefaultNDurationFormat.of(formatMode).format(this);
    }

    @Override
    public String toString() {
        return DefaultNDurationFormat.DEFAULT.format(this);
    }

    /**
     * Parse.
     *
     * @param any any
     * @return parse result
     */
    public static NOptional<NDuration> parse(String any) {
        if (NStringUtils.isBlank(any)) {
            return NOptional.ofEmpty();
        }

        String input = NStringUtils.strip(any).toLowerCase();

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
            input = NStringUtils.strip(input.replace(',', '.')
                    .replace("  ", " ") // collapse multiple spaces
                    );

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
                        durationValues[ChronoUnit.NANOS.ordinal()] += (long) value;
                        break;
                    case "us":
                    case "micro":
                    case "micros":
                        durationValues[ChronoUnit.MICROS.ordinal()] += (long) value;
                        break;
                    case "ms":
                    case "milli":
                    case "millis":
                        durationValues[ChronoUnit.MILLIS.ordinal()] += (long) value;
                        break;
                    case "s":
                    case "sec":
                    case "secs":
                    case "second":
                    case "seconds":
                        durationValues[ChronoUnit.SECONDS.ordinal()] += (long) value;
                        break;
                    case "m":
                    case "mn":
                    case "min":
                    case "mins":
                    case "minute":
                    case "minutes":
                        durationValues[ChronoUnit.MINUTES.ordinal()] += (long) value;
                        break;
                    case "h":
                    case "hr":
                    case "hrs":
                    case "hour":
                    case "hours":
                        durationValues[ChronoUnit.HOURS.ordinal()] += (long) value;
                        break;
                    case "d":
                    case "day":
                    case "days":
                        durationValues[ChronoUnit.DAYS.ordinal()] += (long) value;
                        break;
                    case "w":
                    case "week":
                    case "weeks":
                        durationValues[ChronoUnit.WEEKS.ordinal()] += (long) value;
                        break;
                    case "mon":
                    case "month":
                    case "months":
                        durationValues[ChronoUnit.MONTHS.ordinal()] += (long) value;
                        break;
                    case "y":
                    case "year":
                    case "years":
                        durationValues[ChronoUnit.YEARS.ordinal()] += (long) value;
                        break;
                    default:
                        return NOptional.ofNamedEmpty(NMsg.ofC("invalid unit value: %s", unit));
                }
            }

            // If no units found, try parsing as plain number (assume millis)
            if (!found) {
                try {
                    double millis = Double.parseDouble(input);
                    durationValues[ChronoUnit.MILLIS.ordinal()] = (long) millis;
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
