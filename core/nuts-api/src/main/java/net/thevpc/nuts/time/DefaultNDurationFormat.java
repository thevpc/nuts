package net.thevpc.nuts.time;

import net.thevpc.nuts.core.NI18n;
import net.thevpc.nuts.format.NPositionType;
import net.thevpc.nuts.io.NMemoryPrintStream;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

public class DefaultNDurationFormat {
    private NDurationFormatMode mode;
    private boolean frozen = false;

    public static final DefaultNDurationFormat DEFAULT = new DefaultNDurationFormat(NDurationFormatMode.DEFAULT).freeze();
    public static final DefaultNDurationFormat FIXED = new DefaultNDurationFormat(NDurationFormatMode.FIXED).freeze();
    public static final DefaultNDurationFormat CLOCK = new DefaultNDurationFormat(NDurationFormatMode.CLOCK).freeze();
    private static DecimalFormat F2 = new DecimalFormat("00");
    private static DecimalFormat F3 = new DecimalFormat("000");
    private static DecimalFormat F9 = new DecimalFormat("000000000");
    private static DecimalFormat F6 = new DecimalFormat("000000");

    public DefaultNDurationFormat(DefaultNDurationFormat other) {
        this.mode = other.mode;
    }

    public static DefaultNDurationFormat of(NDurationFormatMode mode) {
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

    public DefaultNDurationFormat(NDurationFormatMode mode) {
        this.mode = mode == null ? NDurationFormatMode.DEFAULT : mode;
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
            throw new IllegalArgumentException(NMsg.ofC(NI18n.of("invalid millis %s"), millis).toString());
        }
        if (nanos < 0 || nanos > 999999) {
            throw new IllegalArgumentException(NMsg.ofC(NI18n.of("invalid nanos %s"), millis).toString());
        }
        return format(NDuration.ofMillisAndNanos(millis, nanos));
    }

    public String format(Duration duration) {
        return format(NDuration.ofDuration(duration));
    }

    public void formatUnit(NDuration duration, ChronoUnit unit, Set<ChronoUnit> processed, NPrintStream out) {
        int uordinal = unit.ordinal();
        long unitValue = duration.get(unit);
        ChronoUnit[] chronoValues = ChronoUnit.values();
        boolean nextIsZero = uordinal > 0 && duration.isZeroDown(chronoValues[uordinal - 1]);
        boolean empty = processed.isEmpty();
        if (unitValue == 0) {
            if (mode == NDurationFormatMode.DEFAULT) {
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
                        out.print(' ');
                    }
                    out.print(formatNumber(unitValue, unit), NTextStyle.number());
                    out.print(unitString(unit), NTextStyle.info());
                    processed.add(unit);
                }
                break;
            }
            case HOURS: {
                if (mode == NDurationFormatMode.CLOCK) {
                    if (!empty) {
                        out.print(' ');
                    }
                    out.print(formatNumber(unitValue, unit), NTextStyle.number());
                    processed.add(unit);
                } else if (accept(unit, duration)) {
                    if (!empty) {
                        out.print(' ');
                    }
                    out.print(formatNumber(unitValue, unit), NTextStyle.number());
                    out.print(unitString(unit), NTextStyle.info());
                    processed.add(unit);
                }
                break;
            }
            case MINUTES:
            case SECONDS: {
                if (mode == NDurationFormatMode.CLOCK) {
                    if (processed.contains(chronoValues[unit.ordinal() + 1])) {
                        out.print(':');
                    }
                    out.print(formatNumber(unitValue, unit), NTextStyle.number());
                    processed.add(unit);
                } else {
                    if (accept(unit, duration)) {
                        if (!empty) {
                            out.print(' ');
                        }
                        out.print(formatNumber(unitValue, unit), NTextStyle.number());
                        out.print(unitString(unit), NTextStyle.info());
                        processed.add(unit);
                    }
                }
                break;
            }
            case MILLIS: {
                if (mode == NDurationFormatMode.CLOCK) {
                    if (!processed.contains(ChronoUnit.SECONDS)) {
                        out.print("00.", NTextStyle.number());
                    } else {
                        out.print('.', NTextStyle.number());
                    }
                    out.print(formatNumber(unitValue, unit), NTextStyle.number());
                    processed.add(unit);
                } else {
                    if (accept(unit, duration)) {
                        if (!processed.isEmpty()) {
                            out.print(" ");
                        }
                        out.print(formatNumber(unitValue, unit), NTextStyle.number());
                        out.print(unitString(unit), NTextStyle.info());
                        processed.add(unit);
                    }
                }
                break;
            }
            case MICROS:
            case NANOS: {
                if (mode == NDurationFormatMode.CLOCK) {
                    out.print(formatNumber(unitValue, unit), NTextStyle.number());
                    processed.add(unit);
                } else {
                    if (accept(unit, duration)) {
                        if (!processed.isEmpty()) {
                            out.print(" ");
                        }
                        out.print(formatNumber(unitValue, unit), NTextStyle.number());
                        out.print(unitString(unit), NTextStyle.info());
                        processed.add(unit);
                    }
                }
                break;
            }
        }
    }

    private boolean accept(ChronoUnit c, NDuration duration) {
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

    public String format(NDuration duration) {
        NMemoryPrintStream sb = NPrintStream.ofMem(NTerminalMode.FILTERED);
        print(duration, sb);
        return sb.toString();
    }

    public void print(NDuration duration, NPrintStream out) {
        HashSet<ChronoUnit> processed = new HashSet<>();
        for (ChronoUnit chronoUnit : new ChronoUnit[]{
                ChronoUnit.YEARS, ChronoUnit.MONTHS, ChronoUnit.WEEKS, ChronoUnit.DAYS,
                ChronoUnit.HOURS, ChronoUnit.MINUTES, ChronoUnit.SECONDS, ChronoUnit.MILLIS,
                ChronoUnit.MICROS, ChronoUnit.NANOS
        }) {
            formatUnit(duration, chronoUnit, processed, out);
        }
        if (processed.isEmpty()) {
            out.print(formatNumber(0, duration.getSmallestUnit()), NTextStyle.number());
            out.print(unitString(duration.getSmallestUnit()), NTextStyle.info());
        } else if (mode == NDurationFormatMode.CLOCK) {
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
                        out.print(unitString(chronoUnit), NTextStyle.info());
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
                return NStringUtils.formatAlign("" + number, size, NPositionType.LAST);
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
                return NStringUtils.formatAlign("" + number, size, NPositionType.LAST);
            }
        }
        throw new IllegalArgumentException("unsupported");
    }

    public DefaultNDurationFormat copy() {
        return new DefaultNDurationFormat(this);
    }

    public DefaultNDurationFormat freeze() {
        if (!frozen) {
            this.frozen = true;
        }
        return this;
    }

    public NDurationFormatMode getMode() {
        return mode;
    }

    public DefaultNDurationFormat setMode(NDurationFormatMode mode) {
        if (this.mode != mode) {
            if (frozen) {
                throw new IllegalArgumentException(NI18n.of("This instance is immutable and cannot be updated"));
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
