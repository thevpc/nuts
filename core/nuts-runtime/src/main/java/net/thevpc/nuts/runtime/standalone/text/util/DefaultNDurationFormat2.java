package net.thevpc.nuts.runtime.standalone.text.util;

import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.format.NPositionType;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.time.NDurationFormatMode;
import net.thevpc.nuts.util.NStringUtils;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

public class DefaultNDurationFormat2 {
    private NDurationFormatMode mode;

    private static DecimalFormat F2 = new DecimalFormat("00");
    private static DecimalFormat F3 = new DecimalFormat("000");
    private static DecimalFormat F9 = new DecimalFormat("000000000");
    private static DecimalFormat F6 = new DecimalFormat("000000");
    private NWorkspace workspace;
    public DefaultNDurationFormat2(NWorkspace workspace,NDurationFormatMode mode) {
        this.mode = mode == null ? NDurationFormatMode.DEFAULT : mode;
        this.workspace=workspace;
    }

    public DefaultNDurationFormat2(NWorkspace workspace,String pattern) {
        this.workspace=workspace;
        if (NBlankable.isBlank(pattern)) {
            this.mode = NDurationFormatMode.DEFAULT;
        } else {
            switch (pattern.toLowerCase().trim()) {
                case "clock": {
                    this.mode = NDurationFormatMode.CLOCK;
                    break;
                }
                case "fixed": {
                    this.mode = NDurationFormatMode.FIXED;
                    break;
                }
                default: {
                    this.mode = NDurationFormatMode.DEFAULT;
                }
            }
        }
    }

    //    @Override
    public NText formatMillis(long millis,NSession session) {
        return format(millis, 0);
    }


    public NText formatNanos(long nanos,NSession session) {
        return format(nanos / 1000000, (int) (nanos % 1000000));
    }

    //    @Override
    public NText format(long millis, int nanos) {
        if (millis < 0) {
            throw new IllegalArgumentException("invalid millis " + millis);
        }
        if (nanos < 0 || nanos > 999999) {
            throw new IllegalArgumentException("invalid nanos " + millis);
        }
        return format(NDuration.ofMillisAndNanos(millis, nanos));
    }

    public NText format(Duration duration) {
        return format(NDuration.ofDuration(duration));
    }

    public void formatUnit(NDuration duration, ChronoUnit unit, Set<ChronoUnit> processed, NTextBuilder out) {
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
                        out.append(" ");
                    }
                    out.append(formatNumber(unitValue, unit), NTextStyle.number());
                    out.append(unitString(unit), NTextStyle.info());
                    processed.add(unit);
                }
                break;
            }
            case HOURS: {
                if (mode == NDurationFormatMode.CLOCK) {
                    if (!empty) {
                        out.append(" ");
                    }
                    out.append(formatNumber(unitValue, unit), NTextStyle.number());
                    processed.add(unit);
                } else if (accept(unit, duration)) {
                    if (!empty) {
                        out.append(" ");
                    }
                    out.append(formatNumber(unitValue, unit), NTextStyle.number());
                    out.append(unitString(unit), NTextStyle.info());
                    processed.add(unit);
                }
                break;
            }
            case MINUTES:
            case SECONDS: {
                if (mode == NDurationFormatMode.CLOCK) {
                    if (processed.contains(chronoValues[unit.ordinal() + 1])) {
                        out.append(':',NTextStyle.separator());
                    }
                    out.append(formatNumber(unitValue, unit), NTextStyle.number());
                    processed.add(unit);
                } else {
                    if (accept(unit, duration)) {
                        if (!empty) {
                            out.append(' ');
                        }
                        out.append(formatNumber(unitValue, unit), NTextStyle.number());
                        out.append(unitString(unit), NTextStyle.info());
                        processed.add(unit);
                    }
                }
                break;
            }
            case MILLIS: {
                if (mode == NDurationFormatMode.CLOCK) {
                    if (!processed.contains(ChronoUnit.SECONDS)) {
                        out.append("00.", NTextStyle.number());
                    } else {
                        out.append('.', NTextStyle.number());
                    }
                    out.append(formatNumber(unitValue, unit), NTextStyle.number());
                    processed.add(unit);
                } else {
                    if (accept(unit, duration)) {
                        if (!processed.isEmpty()) {
                            out.append(" ");
                        }
                        out.append(formatNumber(unitValue, unit), NTextStyle.number());
                        out.append(unitString(unit), NTextStyle.info());
                        processed.add(unit);
                    }
                }
                break;
            }
            case MICROS:
            case NANOS: {
                if (mode == NDurationFormatMode.CLOCK) {
                    out.append(formatNumber(unitValue, unit), NTextStyle.number());
                    processed.add(unit);
                } else {
                    if (accept(unit, duration)) {
                        if (!processed.isEmpty()) {
                            out.append(" ");
                        }
                        out.append(formatNumber(unitValue, unit), NTextStyle.number());
                        out.append(unitString(unit), NTextStyle.info());
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

    public NText format(NDuration duration) {
        NTextBuilder sb = NTextBuilder.of();
        print(duration, sb);
        return sb.build();
    }

    public NText format(NDuration duration, NTexts texts) {
        NTextBuilder sb = texts.ofBuilder();
        print(duration, sb);
        return sb.build();
    }

    public void print(NDuration duration, NTextBuilder out) {
        HashSet<ChronoUnit> processed = new HashSet<>();
        for (ChronoUnit chronoUnit : new ChronoUnit[]{
                ChronoUnit.YEARS, ChronoUnit.MONTHS, ChronoUnit.WEEKS, ChronoUnit.DAYS,
                ChronoUnit.HOURS, ChronoUnit.MINUTES, ChronoUnit.SECONDS, ChronoUnit.MILLIS,
                ChronoUnit.MICROS, ChronoUnit.NANOS
        }) {
            formatUnit(duration, chronoUnit, processed, out);
        }
        if (processed.isEmpty()) {
            out.append(formatNumber(0, duration.getSmallestUnit()), NTextStyle.number());
            out.append(unitString(duration.getSmallestUnit()), NTextStyle.info());
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
                        out.append(unitString(chronoUnit), NTextStyle.info());
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


    public NDurationFormatMode getMode() {
        return mode;
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
