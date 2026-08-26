package net.thevpc.nuts.time;

import java.time.*;
import java.time.temporal.TemporalAdjusters;

/**
 * NTimeUtils class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NTimeUtils {
    /**
     * Converts to start of day.
     *
     * @param instant instant
     * @return to start of day result
     */
    public static Instant toStartOfDay(Instant instant) {
        /**
         * Converts to start of day.
         *
         * @param instant instant
         * @param null null
         * @return to start of day result
         */
        return toStartOfDay(instant, null);
    }

    /**
     * Converts to end of day.
     *
     * @param instant instant
     * @return to end of day result
     */
    public static Instant toEndOfDay(Instant instant) {
        /**
         * Converts to end of day.
         *
         * @param instant instant
         * @param null null
         * @return to end of day result
         */
        return toEndOfDay(instant, null);
    }


    /**
     * Converts to start of week.
     *
     * @param instant instant
     * @return to start of week result
     */
    public static Instant toStartOfWeek(Instant instant) {
        /**
         * Converts to start of week.
         *
         * @param instant instant
         * @param null null
         * @return to start of week result
         */
        return toStartOfWeek(instant, null);
    }

    /**
     * Converts to end of week.
     *
     * @param instant instant
     * @return to end of week result
     */
    public static Instant toEndOfWeek(Instant instant) {
        /**
         * Converts to end of week.
         *
         * @param instant instant
         * @param null null
         * @return to end of week result
         */
        return toEndOfWeek(instant, null);
    }

    /**
     * Converts to start of month.
     *
     * @param instant instant
     * @return to start of month result
     */
    public static Instant toStartOfMonth(Instant instant) {
        /**
         * Converts to start of month.
         *
         * @param instant instant
         * @param null null
         * @return to start of month result
         */
        return toStartOfMonth(instant, null);
    }

    /**
     * Converts to end of month.
     *
     * @param instant instant
     * @return to end of month result
     */
    public static Instant toEndOfMonth(Instant instant) {
        /**
         * Converts to end of month.
         *
         * @param instant instant
         * @param null null
         * @return to end of month result
         */
        return toEndOfMonth(instant, null);
    }

    /**
     * Converts to start of year.
     *
     * @param instant instant
     * @return to start of year result
     */
    public static Instant toStartOfYear(Instant instant) {
        /**
         * Converts to start of year.
         *
         * @param instant instant
         * @param null null
         * @return to start of year result
         */
        return toStartOfYear(instant, null);
    }

    /**
     * Converts to end of year.
     *
     * @param instant instant
     * @return to end of year result
     */
    public static Instant toEndOfYear(Instant instant) {
        /**
         * Converts to end of year.
         *
         * @param instant instant
         * @param null null
         * @return to end of year result
         */
        return toEndOfYear(instant, null);
    }

    /**
     * Converts to start of semester.
     *
     * @param instant instant
     * @return to start of semester result
     */
    public static Instant toStartOfSemester(Instant instant) {
        /**
         * Converts to start of semester.
         *
         * @param instant instant
         * @param null null
         * @return to start of semester result
         */
        return toStartOfSemester(instant, null);
    }

    /**
     * Converts to end of semester.
     *
     * @param instant instant
     * @return to end of semester result
     */
    public static Instant toEndOfSemester(Instant instant) {
        /**
         * Converts to end of semester.
         *
         * @param instant instant
         * @param null null
         * @return to end of semester result
         */
        return toEndOfSemester(instant, null);
    }

    /**
     * Converts to start of quarter.
     *
     * @param instant instant
     * @return to start of quarter result
     */
    public static Instant toStartOfQuarter(Instant instant) {
        /**
         * Converts to start of quarter.
         *
         * @param instant instant
         * @param null null
         * @return to start of quarter result
         */
        return toStartOfQuarter(instant, null);
    }

    /**
     * Converts to end of quarter.
     *
     * @param instant instant
     * @return to end of quarter result
     */
    public static Instant toEndOfQuarter(Instant instant) {
        /**
         * Converts to end of quarter.
         *
         * @param instant instant
         * @param null null
         * @return to end of quarter result
         */
        return toEndOfQuarter(instant, null);
    }


    /// ////////////////////////////////////////
    /// With ZoneId
    /// ////////////////////////////////////////

    /**
     * Converts to start of day.
     *
     * @param instant instant
     * @param zone zone
     * @return to start of day result
     */
    public static Instant toStartOfDay(Instant instant, ZoneId zone) {
        if (instant == null) {
            return null;
        }
        if (zone == null) {
            zone = ZoneId.systemDefault();
        }
        ZonedDateTime zonedDateTime = instant.atZone(zone);
        LocalDate date = zonedDateTime.toLocalDate();
        return date.atStartOfDay(zone).toInstant();
    }

    /**
     * Converts to end of day.
     *
     * @param instant instant
     * @param zone zone
     * @return to end of day result
     */
    public static Instant toEndOfDay(Instant instant, ZoneId zone) {
        if (instant == null) {
            return null;
        }
        if (zone == null) {
            zone = ZoneId.systemDefault();
        }
        ZonedDateTime zonedDateTime = instant.atZone(zone);
        LocalDate date = zonedDateTime.toLocalDate();
        return LocalDateTime.of(date, LocalTime.MAX).atZone(zone).toInstant();
    }

    /**
     * Converts to start of week.
     *
     * @param instant instant
     * @param zone zone
     * @return to start of week result
     */
    public static Instant toStartOfWeek(Instant instant, ZoneId zone) {
        if (instant == null) {
            return null;
        }
        if (zone == null) {
            zone = ZoneId.systemDefault();
        }
        return instant.atZone(zone)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .toLocalDate()
                .atStartOfDay(zone)
                .toInstant();
    }

    /**
     * Converts to end of week.
     *
     * @param instant instant
     * @param zone zone
     * @return to end of week result
     */
    public static Instant toEndOfWeek(Instant instant, ZoneId zone) {
        if (instant == null) {
            return null;
        }
        if (zone == null) {
            zone = ZoneId.systemDefault();
        }
        LocalDate endOfWeekDate = instant.atZone(zone)
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                .toLocalDate();

        LocalDateTime endOfDay = LocalDateTime.of(endOfWeekDate, LocalTime.MAX);
        return endOfDay.atZone(zone).toInstant();
    }

    /**
     * Converts to start of month.
     *
     * @param instant instant
     * @param zone zone
     * @return to start of month result
     */
    public static Instant toStartOfMonth(Instant instant, ZoneId zone) {
        if (instant == null) {
            return null;
        }
        if (zone == null) {
            zone = ZoneId.systemDefault();
        }
        return instant.atZone(zone)
                .with(TemporalAdjusters.firstDayOfMonth())
                .toLocalDate()
                .atStartOfDay(zone)
                .toInstant();
    }

    /**
     * Converts to end of month.
     *
     * @param instant instant
     * @param zone zone
     * @return to end of month result
     */
    public static Instant toEndOfMonth(Instant instant, ZoneId zone) {
        if (instant == null) {
            return null;
        }
        if (zone == null) {
            zone = ZoneId.systemDefault();
        }
        LocalDate endOfMonthDate = instant.atZone(zone)
                .with(TemporalAdjusters.lastDayOfMonth())
                .toLocalDate();

        LocalDateTime endOfDay = LocalDateTime.of(endOfMonthDate, LocalTime.MAX);
        return endOfDay.atZone(zone).toInstant();
    }

    /**
     * Converts to start of year.
     *
     * @param instant instant
     * @param zone zone
     * @return to start of year result
     */
    public static Instant toStartOfYear(Instant instant, ZoneId zone) {
        if (instant == null) {
            return null;
        }
        if (zone == null) {
            zone = ZoneId.systemDefault();
        }
        return instant.atZone(zone)
                .with(TemporalAdjusters.firstDayOfYear())
                .toLocalDate()
                .atStartOfDay(zone)
                .toInstant();
    }

    /**
     * Converts to end of year.
     *
     * @param instant instant
     * @param zone zone
     * @return to end of year result
     */
    public static Instant toEndOfYear(Instant instant, ZoneId zone) {
        if (instant == null) {
            return null;
        }
        if (zone == null) {
            zone = ZoneId.systemDefault();
        }
        LocalDate endOfYearDate = instant.atZone(zone)
                .with(TemporalAdjusters.lastDayOfYear())
                .toLocalDate();

        LocalDateTime endOfDay = LocalDateTime.of(endOfYearDate, LocalTime.MAX);
        return endOfDay.atZone(zone).toInstant();
    }

    /**
     * Converts to start of semester.
     *
     * @param instant instant
     * @param zone zone
     * @return to start of semester result
     */
    public static Instant toStartOfSemester(Instant instant, ZoneId zone) {
        if (instant == null) {
            return null;
        }
        if (zone == null) {
            zone = ZoneId.systemDefault();
        }
        int month = instant.atZone(zone).getMonthValue();
        LocalDate startOfSemester;
        if (month <= 6) {
            startOfSemester = LocalDate.of(instant.atZone(zone).getYear(), 1, 1); // January 1st
        } else {
            startOfSemester = LocalDate.of(instant.atZone(zone).getYear(), 7, 1); // July 1st
        }
        return startOfSemester.atStartOfDay(zone).toInstant();
    }

    /**
     * Converts to end of semester.
     *
     * @param instant instant
     * @param zone zone
     * @return to end of semester result
     */
    public static Instant toEndOfSemester(Instant instant, ZoneId zone) {
        if (instant == null) {
            return null;
        }
        if (zone == null) {
            zone = ZoneId.systemDefault();
        }
        int month = instant.atZone(zone).getMonthValue();
        LocalDate endOfSemester;
        if (month <= 6) {
            endOfSemester = LocalDate.of(instant.atZone(zone).getYear(), 6, 30); // June 30th
        } else {
            endOfSemester = LocalDate.of(instant.atZone(zone).getYear(), 12, 31); // December 31st
        }
        LocalDateTime endOfDay = LocalDateTime.of(endOfSemester, LocalTime.MAX);
        return endOfDay.atZone(zone).toInstant();
    }

    /**
     * Converts to start of quarter.
     *
     * @param instant instant
     * @param zone zone
     * @return to start of quarter result
     */
    public static Instant toStartOfQuarter(Instant instant, ZoneId zone) {
        if (instant == null) {
            return null;
        }
        if (zone == null) {
            zone = ZoneId.systemDefault();
        }
        int month = instant.atZone(zone).getMonthValue();
        LocalDate startOfQuarter;
        if (month <= 3) {
            startOfQuarter = LocalDate.of(instant.atZone(zone).getYear(), 1, 1);
        } else if (month <= 6) {
            startOfQuarter = LocalDate.of(instant.atZone(zone).getYear(), 4, 1);
        } else if (month <= 9) {
            startOfQuarter = LocalDate.of(instant.atZone(zone).getYear(), 7, 1);
        } else {
            startOfQuarter = LocalDate.of(instant.atZone(zone).getYear(), 10, 1);
        }
        return startOfQuarter.atStartOfDay(zone).toInstant();
    }

    /**
     * Converts to end of quarter.
     *
     * @param instant instant
     * @param zone zone
     * @return to end of quarter result
     */
    public static Instant toEndOfQuarter(Instant instant, ZoneId zone) {
        if (instant == null) {
            return null;
        }
        if (zone == null) {
            zone = ZoneId.systemDefault();
        }
        int month = instant.atZone(zone).getMonthValue();
        LocalDate endOfQuarter;
        if (month <= 3) {
            endOfQuarter = LocalDate.of(instant.atZone(zone).getYear(), 3, 31);
        } else if (month <= 6) {
            endOfQuarter = LocalDate.of(instant.atZone(zone).getYear(), 6, 30);
        } else if (month <= 9) {
            endOfQuarter = LocalDate.of(instant.atZone(zone).getYear(), 9, 30);
        } else {
            endOfQuarter = LocalDate.of(instant.atZone(zone).getYear(), 12, 31);
        }
        LocalDateTime endOfDay = LocalDateTime.of(endOfQuarter, LocalTime.MAX);
        return endOfDay.atZone(zone).toInstant();
    }
}
