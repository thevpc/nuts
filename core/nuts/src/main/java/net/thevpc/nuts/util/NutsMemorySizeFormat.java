package net.thevpc.nuts.util;

import net.thevpc.nuts.format.NutsPositionType;
import net.thevpc.nuts.io.NutsPlainPrintStream;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.text.NutsTextStyle;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class NutsMemorySizeFormat {
    private Boolean iec;
    private boolean fixed;
    private boolean frozen = false;

    public static final NutsMemorySizeFormat DEFAULT = new NutsMemorySizeFormat(null, false).freeze();
    public static final NutsMemorySizeFormat IEC = new NutsMemorySizeFormat(true, false).freeze();
    public static final NutsMemorySizeFormat NON_IEC = new NutsMemorySizeFormat(false, false).freeze();
    public static final NutsMemorySizeFormat FIXED = new NutsMemorySizeFormat(null, true).freeze();
    public static final NutsMemorySizeFormat FIXED_IEC = new NutsMemorySizeFormat(true, true).freeze();
    public static final NutsMemorySizeFormat FIXED_NON_IEC = new NutsMemorySizeFormat(false, true).freeze();
    private static DecimalFormat F2 = new DecimalFormat("00");
    private static DecimalFormat F3 = new DecimalFormat("000");
    private static DecimalFormat F9 = new DecimalFormat("000000000");
    private static DecimalFormat F6 = new DecimalFormat("000000");

    public NutsMemorySizeFormat(NutsMemorySizeFormat other) {
        this.iec = other.iec;
    }

    public static NutsMemorySizeFormat of(boolean fixed, Boolean iec) {
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

    public NutsMemorySizeFormat(Boolean iec, boolean fixed) {
        this.iec = iec;
        this.fixed = fixed;
    }

    public void formatUnit(NutsMemorySize memorySize, NutsMemoryUnit unit, Set<NutsMemoryUnit> processed, NutsPrintStream out) {
        if (iec != null) {
            memorySize = memorySize.withIEC(iec);
        }
        boolean iiec = memorySize.isIEC();
        int uordinal = unit.ordinal();
        long unitValue = memorySize.get(unit);
        NutsMemoryUnit[] chronoValues = NutsMemoryUnit.values();
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
                out.append(' ');
            }
            out.append(formatNumber(unitValue, unit), NutsTextStyle.number());
            out.append(unitString(unit, iiec), NutsTextStyle.info());
            processed.add(unit);
        }
    }

    private boolean accept(NutsMemoryUnit c, NutsMemorySize memorySize) {
        if (c.ordinal() < memorySize.getSmallestUnit().ordinal()) {
            return false;
        }
        return memorySize.get(c) != 0;
    }

    public String format(NutsMemorySize memorySize) {
        NutsPrintStream sb = new NutsPlainPrintStream();
        print(memorySize, sb);
        return sb.toString();
    }

    public void print(NutsMemorySize memorySize, NutsPrintStream out) {
        if (iec != null) {
            memorySize = memorySize.withIEC(iec);
        }
        HashSet<NutsMemoryUnit> processed = new HashSet<>();
        NutsMemoryUnit[] values0 = NutsMemoryUnit.values();
        NutsMemoryUnit[] values = new NutsMemoryUnit[values0.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = values0[values.length - i - 1];
        }
        for (NutsMemoryUnit chronoUnit : values) {
            formatUnit(memorySize, chronoUnit, processed, out);
        }
        if (processed.isEmpty()) {
            out.append(formatNumber(0, memorySize.getSmallestUnit()), NutsTextStyle.number());
            out.append(unitString(memorySize.getSmallestUnit(), memorySize.isIEC()), NutsTextStyle.info());
        }
    }


    private int sizeOf(NutsMemoryUnit unit) {
        switch (unit) {
            case BIT:
                return 1;
            default:
                return 3;
        }
    }

    private String formatNumber(long number, NutsMemoryUnit unit) {
        int size = sizeOf(unit);
        if (fixed) {
            return NutsStringUtils.formatAlign("" + number, size, NutsPositionType.LAST);
        }
        return String.valueOf(number);
    }

    public NutsMemorySizeFormat copy() {
        return new NutsMemorySizeFormat(this);
    }

    public NutsMemorySizeFormat freeze() {
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

    public NutsMemorySizeFormat setIEC(Boolean iec) {
        if (Objects.equals(this.iec, iec)) {
            if (frozen) {
                throw new IllegalArgumentException("This instance is immutable and cannot be updated");
            }
            this.iec = iec;
        }
        return this;
    }

    public NutsMemorySizeFormat setFixed(Boolean fixed) {
        if (this.fixed != fixed) {
            if (frozen) {
                throw new IllegalArgumentException("This instance is immutable and cannot be updated");
            }
            this.fixed = fixed;
        }
        return this;
    }

    public String unitString(NutsMemoryUnit unit, boolean iec) {
        switch (unit) {
            case BIT:
                return ("bits");
            case BYTE:
                return ("B");
            case KILO_BYTE:
                return iec ? "KiB" : "KB";
            case MEGA_BYTE:
                return iec ? "MiB" : "MB";
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
