package net.thevpc.nuts.util;

import net.thevpc.nuts.format.NutsPositionType;
import net.thevpc.nuts.io.NutsPlainPrintStream;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.text.NutsTextStyle;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

public class DefaultNutsDurationFormat {
    private NutsDurationFormatMode mode;
    private boolean frozen = false;

    public static final DefaultNutsDurationFormat DEFAULT = new DefaultNutsDurationFormat(NutsDurationFormatMode.DEFAULT).freeze();
    public static final DefaultNutsDurationFormat FIXED = new DefaultNutsDurationFormat(NutsDurationFormatMode.FIXED).freeze();
    public static final DefaultNutsDurationFormat CLOCK = new DefaultNutsDurationFormat(NutsDurationFormatMode.CLOCK).freeze();
    private static DecimalFormat F2 = new DecimalFormat("00");
    private static DecimalFormat F3 = new DecimalFormat("000");
    private static DecimalFormat F9 = new DecimalFormat("000000000");
    private static DecimalFormat F6 = new DecimalFormat("000000");

    public DefaultNutsDurationFormat(DefaultNutsDurationFormat other) {
        this.mode = other.mode;
    }

    public static DefaultNutsDurationFormat of(NutsDurationFormatMode mode) {
        if (mode != null) {
            switch (mode) {
                case DEFAULT:
                    return DEFAULT;
                case FIXED:
                    return FIXED;
                case CLOCK:
                    return CLOCK;
            }
        }
        return DEFAULT;
    }

    public DefaultNutsDurationFormat(NutsDurationFormatMode mode) {
        this.mode = mode == null ? NutsDurationFormatMode.DEFAULT : mode;
    }

    //    @Override
    public String formatMillis(long millis) {
        return format(millis, 0);
    }


    public String formatNanos(long nanos) {
        return format(nanos / 1000000, (int) (nanos % 1000000));
    }

    //    @Override
    public String format(long millis, int nanos) {
        if (millis < 0) {
            throw new IllegalArgumentException("invalid millis " + millis);
        }
        if (nanos < 0 || nanos > 999999) {
            throw new IllegalArgumentException("invalid nanos " + millis);
        }
        return format(NutsDuration.ofMillisAndNanos(millis, nanos));
    }

    public String format(Duration duration) {
        return format(NutsDuration.ofDuration(duration));
    }

    public void formatUnit(NutsDuration duration, ChronoUnit unit, Set<ChronoUnit> processed, NutsPrintStream out) {
        int uordinal = unit.ordinal();
        long unitValue = duration.get(unit);
        ChronoUnit[] chronoValues = ChronoUnit.values();
        boolean nextIsZero = uordinal > 0 && duration.isZeroDown(chronoValues[uordinal - 1]);
        boolean empty = processed.isEmpty();
        if (unitValue == 0) {
            if (mode == NutsDurationFormatMode.DEFAULT) {
                boolean canSkip = true;
                if (!empty && nextIsZero) {
                    canSkip = false;
                }
                if (canSkip) {
                    return;
                }
            }
        }
        if (uordinal > duration.getLargestUnit().ordinal()
                || uordinal < duration.getSmallestUnit().ordinal()
        ) {
            return;
        }
        switch (unit) {
            case YEARS:
            case MONTHS:
            case WEEKS:
            case DAYS: {
                if (accept(unit, duration)) {
                    if (!empty) {
                        out.append(' ');
                    }
                    out.append(formatNumber(unitValue, unit),NutsTextStyle.number());
                    out.append(unitString(unit),NutsTextStyle.info());
                    processed.add(unit);
                }
                break;
            }
            case HOURS: {
                if (mode == NutsDurationFormatMode.CLOCK) {
                    if (!empty) {
                        out.append(' ');
                    }
                    out.append(formatNumber(unitValue, unit),NutsTextStyle.number());
                    processed.add(unit);
                } else if (accept(unit, duration)) {
                    if (!empty) {
                        out.append(' ');
                    }
                    out.append(formatNumber(unitValue, unit),NutsTextStyle.number());
                    out.append(unitString(unit),NutsTextStyle.info());
                    processed.add(unit);
                }
                break;
            }
            case MINUTES:
            case SECONDS: {
                if (mode == NutsDurationFormatMode.CLOCK) {
                    if (processed.contains(chronoValues[unit.ordinal() + 1])) {
                        out.append(':');
                    }
                    out.append(formatNumber(unitValue, unit),NutsTextStyle.number());
                    processed.add(unit);
                } else {
                    if (accept(unit, duration)) {
                        if (!empty) {
                            out.append(' ');
                        }
                        out.append(formatNumber(unitValue, unit),NutsTextStyle.number());
                        out.append(unitString(unit),NutsTextStyle.info());
                        processed.add(unit);
                    }
                }
                break;
            }
            case MILLIS: {
                if (mode == NutsDurationFormatMode.CLOCK) {
                    if (!processed.contains(ChronoUnit.SECONDS)) {
                        out.append("00.",NutsTextStyle.number());
                    } else {
                        out.append('.',NutsTextStyle.number());
                    }
                    out.append(formatNumber(unitValue, unit),NutsTextStyle.number());
                    processed.add(unit);
                } else {
                    if (accept(unit, duration)) {
                        if (!processed.isEmpty()) {
                            out.append(" ");
                        }
                        out.append(formatNumber(unitValue, unit),NutsTextStyle.number());
                        out.append(unitString(unit),NutsTextStyle.info());
                        processed.add(unit);
                    }
                }
                break;
            }
            case MICROS:
            case NANOS: {
                if (mode == NutsDurationFormatMode.CLOCK) {
                    out.append(formatNumber(unitValue, unit),NutsTextStyle.number());
                    processed.add(unit);
                } else {
                    if (accept(unit, duration)) {
                        if (!processed.isEmpty()) {
                            out.append(" ");
                        }
                        out.append(formatNumber(unitValue, unit),NutsTextStyle.number());
                        out.append(unitString(unit),NutsTextStyle.info());
                        processed.add(unit);
                    }
                }
                break;
            }
        }
    }

    private boolean accept(ChronoUnit c, NutsDuration duration) {
        if (c.ordinal() < duration.getSmallestUnit().ordinal()) {
            return false;
        }
        switch (mode) {
            case DEFAULT:
                return duration.get(c) != 0;
            case CLOCK: {
                return true;
            }
        }
        return true;
    }

    public String format(NutsDuration duration) {
        NutsPrintStream sb = new NutsPlainPrintStream();
        print(duration, sb);
        return sb.toString();
    }

    public void print(NutsDuration duration, NutsPrintStream out) {
        HashSet<ChronoUnit> processed = new HashSet<>();
        for (ChronoUnit chronoUnit : new ChronoUnit[]{
                ChronoUnit.YEARS, ChronoUnit.MONTHS, ChronoUnit.WEEKS, ChronoUnit.DAYS,
                ChronoUnit.HOURS, ChronoUnit.MINUTES, ChronoUnit.SECONDS, ChronoUnit.MILLIS,
                ChronoUnit.MICROS, ChronoUnit.NANOS
        }) {
            formatUnit(duration, chronoUnit, processed, out);
        }
        if (processed.isEmpty()) {
            out.append(formatNumber(0, duration.getSmallestUnit()),NutsTextStyle.number());
            out.append(unitString(duration.getSmallestUnit()),NutsTextStyle.info());
        } else if (mode == NutsDurationFormatMode.CLOCK) {
            if (processed.contains(ChronoUnit.MILLIS)) {
                processed.add(ChronoUnit.SECONDS);
                processed.remove(ChronoUnit.MILLIS);
            }
            if (processed.contains(ChronoUnit.MICROS)) {
                processed.add(ChronoUnit.SECONDS);
                processed.remove(ChronoUnit.MICROS);
            }
            if (processed.contains(ChronoUnit.NANOS)) {
                processed.add(ChronoUnit.SECONDS);
                processed.remove(ChronoUnit.NANOS);
            }
            if (processed.size() == 1) {
                for (ChronoUnit chronoUnit : processed) {
                    if (chronoUnit.ordinal() <= ChronoUnit.HOURS.ordinal()) {
                        out.append(unitString(chronoUnit),NutsTextStyle.info());
                    }
                }
            }
        }
    }


    private int sizeOf(ChronoUnit unit) {
        switch (unit) {
            case NANOS:
            case MICROS:
            case MILLIS:
                return 3;
            default:
                return 2;
        }
    }

    private String formatNumber(long number, ChronoUnit unit) {
        int size = sizeOf(unit);
        switch (mode) {
            case DEFAULT:
                return String.valueOf(number);
            case FIXED:
                return NutsStringUtils.formatAlign("" + number, size, NutsPositionType.LAST);
            case CLOCK: {
                switch (unit) {
                    case HOURS:
                    case MINUTES:
                    case SECONDS:
                    case MILLIS:
                    case MICROS:
                    case NANOS: {
                        switch (size) {
                            case 2: {
                                return F2.format(number);
                            }
                            case 3: {
                                return F3.format(number);
                            }
                            case 6: {
                                return F6.format(number);
                            }
                            case 9: {
                                return F9.format(number);
                            }
                        }
                        break;
                    }
                }
                return NutsStringUtils.formatAlign("" + number, size, NutsPositionType.LAST);
            }
        }
        throw new IllegalArgumentException("unsupported");
    }

    public DefaultNutsDurationFormat copy() {
        return new DefaultNutsDurationFormat(this);
    }

    public DefaultNutsDurationFormat freeze() {
        if (!frozen) {
            this.frozen = true;
        }
        return this;
    }

    public NutsDurationFormatMode getMode() {
        return mode;
    }

    public DefaultNutsDurationFormat setMode(NutsDurationFormatMode mode) {
        if (this.mode != mode) {
            if (frozen) {
                throw new IllegalArgumentException("This instance is immutable and cannot be updated");
            }
            this.mode = mode;
        }
        return this;
    }

    public String unitString(ChronoUnit unit) {
        switch (unit) {
            case YEARS:
                return ("y");
            case MONTHS:
                return ("m");
            case WEEKS:
                return ("w");
            case DAYS:
                return ("d");
            case HOURS:
                return ("h");
            case MINUTES:
                return ("mn");
            case SECONDS:
                return ("s");
            case MILLIS:
                return ("ms");
            case MICROS:
                return ("us");
            case NANOS:
                return ("ns");
        }
        return "";
    }
}
