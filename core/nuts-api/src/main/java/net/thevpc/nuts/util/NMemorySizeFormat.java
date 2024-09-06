package net.thevpc.nuts.util;

import net.thevpc.nuts.format.NPositionType;
import net.thevpc.nuts.io.NPlainPrintStream;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.text.NTextStyle;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class NMemorySizeFormat {
    private Boolean iec;
    private boolean fixed;
    private boolean frozen = false;

    public static final NMemorySizeFormat DEFAULT = new NMemorySizeFormat(null, false).freeze();
    public static final NMemorySizeFormat IEC = new NMemorySizeFormat(true, false).freeze();
    public static final NMemorySizeFormat NON_IEC = new NMemorySizeFormat(false, false).freeze();
    public static final NMemorySizeFormat FIXED = new NMemorySizeFormat(null, true).freeze();
    public static final NMemorySizeFormat FIXED_IEC = new NMemorySizeFormat(true, true).freeze();
    public static final NMemorySizeFormat FIXED_NON_IEC = new NMemorySizeFormat(false, true).freeze();
    private static DecimalFormat F2 = new DecimalFormat("00");
    private static DecimalFormat F3 = new DecimalFormat("000");
    private static DecimalFormat F9 = new DecimalFormat("000000000");
    private static DecimalFormat F6 = new DecimalFormat("000000");

    public NMemorySizeFormat(NMemorySizeFormat other) {
        this.iec = other.iec;
    }

    public static NMemorySizeFormat of(boolean fixed, Boolean iec) {
        if (fixed) {
            if (iec == null) {
                return FIXED;
            }
            if (iec) {
                return FIXED_IEC;
            }
            return FIXED_NON_IEC;
        } else {
            if (iec == null) {
                return DEFAULT;
            }
            if (iec) {
                return IEC;
            }
            return NON_IEC;
        }
    }

    public NMemorySizeFormat(Boolean iec, boolean fixed) {
        this.iec = iec;
        this.fixed = fixed;
    }

    public void formatUnit(NMemorySize memorySize, NMemoryUnit unit, Set<NMemoryUnit> processed, NPrintStream out) {
        if (iec != null) {
            memorySize = memorySize.withIEC(iec);
        }
        boolean iiec = memorySize.isIEC();
        int uordinal = unit.ordinal();
        long unitValue = memorySize.get(unit);
        NMemoryUnit[] chronoValues = NMemoryUnit.values();
        boolean nextIsZero = uordinal > 0 && memorySize.isZeroDown(chronoValues[uordinal - 1]);
        boolean empty = processed.isEmpty();
        if (unitValue == 0) {
            boolean canSkip = true;
            if (!empty && nextIsZero) {
                canSkip = false;
            }
            if (canSkip) {
                return;
            }
        }
        if (uordinal > memorySize.getLargestUnit().ordinal()
                || uordinal < memorySize.getSmallestUnit().ordinal()
        ) {
            return;
        }
        if (accept(unit, memorySize)) {
            if (!empty) {
                out.print(' ');
            }
            out.print(formatNumber(unitValue, unit), NTextStyle.number());
            out.print(unitString(unit, iiec), NTextStyle.info());
            processed.add(unit);
        }
    }

    private boolean accept(NMemoryUnit c, NMemorySize memorySize) {
        if (c.ordinal() < memorySize.getSmallestUnit().ordinal()) {
            return false;
        }
        return memorySize.get(c) != 0;
    }

    public String format(NMemorySize memorySize) {
        NPrintStream sb = new NPlainPrintStream();
        print(memorySize, sb);
        return sb.toString();
    }

    public void print(NMemorySize memorySize, NPrintStream out) {
        if (iec != null) {
            memorySize = memorySize.withIEC(iec);
        }
        HashSet<NMemoryUnit> processed = new HashSet<>();
        NMemoryUnit[] values0 = NMemoryUnit.values();
        NMemoryUnit[] values = new NMemoryUnit[values0.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = values0[values.length - i - 1];
        }
        for (NMemoryUnit chronoUnit : values) {
            formatUnit(memorySize, chronoUnit, processed, out);
        }
        if (processed.isEmpty()) {
            out.print(formatNumber(0, memorySize.getSmallestUnit()), NTextStyle.number());
            out.print(unitString(memorySize.getSmallestUnit(), memorySize.isIEC()), NTextStyle.info());
        }
    }


    private int sizeOf(NMemoryUnit unit) {
        switch (unit) {
            case BIT:
                return 1;
            default:
                return 3;
        }
    }

    private String formatNumber(long number, NMemoryUnit unit) {
        int size = sizeOf(unit);
        if (fixed) {
            return NStringUtils.formatAlign("" + number, size, NPositionType.LAST);
        }
        return String.valueOf(number);
    }

    public NMemorySizeFormat copy() {
        return new NMemorySizeFormat(this);
    }

    public NMemorySizeFormat freeze() {
        if (!frozen) {
            this.frozen = true;
        }
        return this;
    }

    public boolean isFixed() {
        return fixed;
    }

    public Boolean getIEC() {
        return iec;
    }

    public NMemorySizeFormat setIEC(Boolean iec) {
        if (Objects.equals(this.iec, iec)) {
            if (frozen) {
                throw new IllegalArgumentException("This instance is immutable and cannot be updated");
            }
            this.iec = iec;
        }
        return this;
    }

    public NMemorySizeFormat setFixed(Boolean fixed) {
        if (this.fixed != fixed) {
            if (frozen) {
                throw new IllegalArgumentException("This instance is immutable and cannot be updated");
            }
            this.fixed = fixed;
        }
        return this;
    }

    public String unitString(NMemoryUnit unit, boolean iec) {
        switch (unit) {
            case BIT:
                return ("bits");
            case BYTE:
                return ("B");
            case KILO_BYTE:
                return iec ? "KiB" : "KB";
            case MEGA_BYTE:
                return iec ? "MiB" : "MB";
            case GIGA_BYTE:
                return iec ? "GiB" : "GB";
            case TERA_BYTE:
                return iec ? "TiB" : "TB";
            case PETA_BYTE:
                return iec ? "PiB" : "PB";
            case ZETA_BYTE:
                return iec ? "ZiB" : "ZB";
        }
        return "";
    }
}
