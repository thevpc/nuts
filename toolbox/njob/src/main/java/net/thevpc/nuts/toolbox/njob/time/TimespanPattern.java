package net.thevpc.nuts.toolbox.njob.time;

public class TimespanPattern {
    public static final TimespanPattern DEFAULT = new TimespanPattern(24, 7, 30, 12);
    public static final TimespanPattern WORK = new TimespanPattern(8, 5, 22, 11);
    private int hoursPerDay;
    private int daysPerWeek;
    private int daysPerMonth;
    private int monthsPerYear;

    public TimespanPattern(int hoursPerDay, int daysPerWeek, int daysPerMonth, int monthsPerYear) {
        this.hoursPerDay = hoursPerDay;
        this.daysPerWeek = daysPerWeek;
        this.daysPerMonth = daysPerMonth;
        this.monthsPerYear = monthsPerYear;
    }

    public int getHoursPerDay() {
        return hoursPerDay;
    }

    public int getDaysPerWeek() {
        return daysPerWeek;
    }

    public int getDaysPerMonth() {
        return daysPerMonth;
    }

    public int getMonthsPerYear() {
        return monthsPerYear;
    }
}
