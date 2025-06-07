package net.thevpc.nuts.util;

import java.time.*;
import java.time.temporal.TemporalAdjusters;

public class NDateUtils {
    public static Instant toStartOfDay(Instant instant) {
        return toStartOfDay(instant, null);
    }

    public static Instant toEndOfDay(Instant instant) {
        return toEndOfDay(instant, null);
    }


    public static Instant toStartOfWeek(Instant instant) {
        return toStartOfWeek(instant, null);
    }

    public static Instant toEndOfWeek(Instant instant) {
        return toEndOfWeek(instant, null);
    }

    public static Instant toStartOfMonth(Instant instant) {
        return toStartOfMonth(instant, null);
    }

    public static Instant toEndOfMonth(Instant instant) {
        return toEndOfMonth(instant, null);
    }

    public static Instant toStartOfYear(Instant instant) {
        return toStartOfYear(instant, null);
    }

    public static Instant toEndOfYear(Instant instant) {
        return toEndOfYear(instant, null);
    }

    public static Instant toStartOfSemester(Instant instant) {
        return toStartOfSemester(instant, null);
    }

    public static Instant toEndOfSemester(Instant instant) {
        return toEndOfSemester(instant, null);
    }

    public static Instant toStartOfQuarter(Instant instant) {
        return toStartOfQuarter(instant, null);
    }

    public static Instant toEndOfQuarter(Instant instant) {
        return toEndOfQuarter(instant, null);
    }


    /// ////////////////////////////////////////
    /// With ZoneId
    /// ////////////////////////////////////////

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
