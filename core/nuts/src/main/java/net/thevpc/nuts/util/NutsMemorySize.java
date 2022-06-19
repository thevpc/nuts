package net.thevpc.nuts.util;

import net.thevpc.nuts.NutsFormat;
import net.thevpc.nuts.NutsFormattable;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.elem.NutsMapBy;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.spi.NutsFormatSPI;

import java.io.Serializable;
import java.util.Arrays;

public class NutsMemorySize implements Serializable, NutsFormattable {
    private final long[] values = new long[NutsMemoryUnit.values().length];
    private final NutsMemoryUnit smallestUnit;
    private final NutsMemoryUnit largestUnit;
    private final long bytes;
    private final int bits;
    private final long KB;
    private final boolean iec;

    @NutsMapBy
    public NutsMemorySize(
            @NutsMapBy(name = "bits") long bits,
            @NutsMapBy(name = "bytes") long bytes,
            @NutsMapBy(name = "kiloBytes") long kiloBytes,
            @NutsMapBy(name = "megaBytes") long megaBytes,
            @NutsMapBy(name = "teraBytes") long teraBytes,
            @NutsMapBy(name = "petaBytes") long petaBytes,
            @NutsMapBy(name = "zetaBytes") long zetaBytes,
            @NutsMapBy(name = "smallestUnit") NutsMemoryUnit smallestUnit,
            @NutsMapBy(name = "largestUnit") NutsMemoryUnit largestUnit,
            @NutsMapBy(name = "iec") boolean iec
    ) {
        this.iec = iec;
        this.KB = iec ? 1000 : 1024;
        this.values[NutsMemoryUnit.BIT.ordinal()] = bits;
        this.values[NutsMemoryUnit.BYTE.ordinal()] = bytes;
        this.values[NutsMemoryUnit.KILO_BYTE.ordinal()] = kiloBytes;
        this.values[NutsMemoryUnit.MEGA_BYTE.ordinal()] = megaBytes;
        this.values[NutsMemoryUnit.TERA_BYTE.ordinal()] = teraBytes;
        this.values[NutsMemoryUnit.PETA_BYTE.ordinal()] = petaBytes;
        this.values[NutsMemoryUnit.ZETA_BYTE.ordinal()] = zetaBytes;
        this.bytes = rebuildSizeBytes();
        this.bits = rebuildSizeBits();
        this.smallestUnit = smallestUnit == null ? detectSmallestUnit() : normalize(smallestUnit);
        largestUnit = largestUnit == null ? detectLargestUnit() : normalize(largestUnit);
        if (largestUnit.ordinal() < this.smallestUnit.ordinal()) {
            largestUnit = this.smallestUnit;
        }
        this.largestUnit = largestUnit;
        applyUnits();
    }

    private int rebuildSizeBits() {
        return (int) (this.values[NutsMemoryUnit.BIT.ordinal()] % 8);
    }

    public NutsMemorySize(long[] values, NutsMemoryUnit smallestUnit, NutsMemoryUnit largestUnit, boolean iec) {
        this.iec = iec;
        this.KB = iec ? 1000 : 1024;
        for (int i = 0; i < this.values.length; i++) {
            this.values[i] = values[i];
        }
        this.bytes = rebuildSizeBytes();
        this.bits = rebuildSizeBits();
        this.smallestUnit = smallestUnit == null ? detectSmallestUnit() : normalize(smallestUnit);
        largestUnit = largestUnit == null ? detectLargestUnit() : normalize(largestUnit);
        if (largestUnit.ordinal() < this.smallestUnit.ordinal()) {
            largestUnit = this.smallestUnit;
        }
        this.largestUnit = largestUnit;
        applyUnits();
    }

    private long rebuildSizeBytes() {
        return values[NutsMemoryUnit.BYTE.ordinal()]
                + values[NutsMemoryUnit.KILO_BYTE.ordinal()] * KB
                + values[NutsMemoryUnit.MEGA_BYTE.ordinal()] * KB * KB
                + values[NutsMemoryUnit.TERA_BYTE.ordinal()] * KB * KB * KB
                + values[NutsMemoryUnit.PETA_BYTE.ordinal()] * KB * KB * KB * KB
                + values[NutsMemoryUnit.ZETA_BYTE.ordinal()] * KB * KB * KB * KB * KB;
    }

    public NutsMemorySize(long memBytes, int memBits, boolean iec) {
        this.iec = iec;
        this.KB = iec ? 1000 : 1024;
        memBytes += memBits / 8;
        memBits = memBits % 8;
        this.bytes = memBytes;
        this.bits = memBits;

        values[NutsMemoryUnit.ZETA_BYTE.ordinal()] = memBytes / (KB * KB * KB * KB * KB);
        memBytes = memBytes % (KB * KB * KB * KB * KB);
        values[NutsMemoryUnit.PETA_BYTE.ordinal()] = memBytes / (KB * KB * KB * KB);
        memBytes = memBytes % (KB * KB * KB * KB);
        values[NutsMemoryUnit.TERA_BYTE.ordinal()] = memBytes / (KB * KB * KB);
        memBytes = memBytes % (KB * KB * KB);
        values[NutsMemoryUnit.MEGA_BYTE.ordinal()] = memBytes / (KB * KB);
        memBytes = memBytes % (KB * KB);
        values[NutsMemoryUnit.KILO_BYTE.ordinal()] = memBytes / (KB);
        memBytes = memBytes % (KB);
        values[NutsMemoryUnit.BYTE.ordinal()] = memBytes;
        this.smallestUnit = detectSmallestUnit();
        this.largestUnit = detectLargestUnit();
    }

    public NutsMemorySize(long memBytes, int memBits, NutsMemoryUnit smallestUnit, NutsMemoryUnit largestUnit, boolean iec) {
        this.iec = iec;
        this.KB = iec ? 1000 : 1024;
        memBytes += memBits / 8;
        memBits = memBits % 8;
        this.bytes = memBytes;
        this.bits = memBits;
        if (smallestUnit != null && largestUnit != null) {
            this.smallestUnit = normalize(smallestUnit);
            largestUnit = normalize(largestUnit);
            if (largestUnit.ordinal() < this.smallestUnit.ordinal()) {
                largestUnit = this.smallestUnit;
            }
            this.largestUnit = largestUnit;
            int largestUnitOrdinal = this.largestUnit.ordinal();
            int smallestUnitOrdinal = this.smallestUnit.ordinal();
            if (smallestUnitOrdinal <= NutsMemoryUnit.BIT.ordinal()) {
                if (largestUnitOrdinal > NutsMemoryUnit.BIT.ordinal()) {
                    this.values[NutsMemoryUnit.BIT.ordinal()] = memBits;
                } else {
                    this.values[NutsMemoryUnit.BIT.ordinal()] = memBits + memBytes * 8;
                    return;
                }
            }
            if (smallestUnitOrdinal <= NutsMemoryUnit.BYTE.ordinal()) {
                if (largestUnitOrdinal > NutsMemoryUnit.BYTE.ordinal()) {
                    this.values[NutsMemoryUnit.BYTE.ordinal()] = memBytes % KB;
                } else {
                    this.values[NutsMemoryUnit.BYTE.ordinal()] = memBytes;
                    return;
                }
            }
            if (smallestUnitOrdinal <= NutsMemoryUnit.KILO_BYTE.ordinal()) {
                if (largestUnitOrdinal > NutsMemoryUnit.KILO_BYTE.ordinal()) {
                    this.values[NutsMemoryUnit.KILO_BYTE.ordinal()] = (int) ((memBytes / KB) % KB);
                } else {
                    this.values[NutsMemoryUnit.KILO_BYTE.ordinal()] = memBytes / KB;
                    return;
                }
            }
            if (smallestUnitOrdinal <= NutsMemoryUnit.MEGA_BYTE.ordinal()) {
                if (largestUnitOrdinal > NutsMemoryUnit.MEGA_BYTE.ordinal()) {
                    this.values[NutsMemoryUnit.MEGA_BYTE.ordinal()] = (int) ((memBytes / (KB * KB)) % KB);
                } else {
                    this.values[NutsMemoryUnit.MEGA_BYTE.ordinal()] = memBytes / (KB * KB);
                    return;
                }
            }
            if (smallestUnitOrdinal <= NutsMemoryUnit.TERA_BYTE.ordinal()) {
                if (largestUnitOrdinal > NutsMemoryUnit.TERA_BYTE.ordinal()) {
                    this.values[NutsMemoryUnit.TERA_BYTE.ordinal()] = (int) ((memBytes / (KB * KB * KB)) % KB);
                } else {
                    this.values[NutsMemoryUnit.TERA_BYTE.ordinal()] = memBytes / (KB * KB * KB);
                    return;
                }
            }
            if (smallestUnitOrdinal <= NutsMemoryUnit.PETA_BYTE.ordinal()) {
                if (largestUnitOrdinal > NutsMemoryUnit.PETA_BYTE.ordinal()) {
                    this.values[NutsMemoryUnit.PETA_BYTE.ordinal()] = (int) ((memBytes / (KB * KB * KB * KB)) % KB);
                } else {
                    this.values[NutsMemoryUnit.PETA_BYTE.ordinal()] = memBytes / (KB * KB * KB * KB);
                    return;
                }
            }
            if (smallestUnitOrdinal <= NutsMemoryUnit.ZETA_BYTE.ordinal()) {
//                if (largestUnitOrdinal > NutsMemoryUnit.ZETA_BYTE.ordinal()) {
//                    this.values[NutsMemoryUnit.ZETA_BYTE.ordinal()] = (int) ((memBytes / (KB*KB*KB*KB*KB)) % KB);
//                } else {
                this.values[NutsMemoryUnit.ZETA_BYTE.ordinal()] = memBytes / (KB * KB * KB * KB * KB);
//                    return;
//                }
            }
        } else {
            values[NutsMemoryUnit.ZETA_BYTE.ordinal()] = memBytes / (KB * KB * KB * KB * KB);
            memBytes = memBytes % (KB * KB * KB * KB * KB);
            values[NutsMemoryUnit.PETA_BYTE.ordinal()] = memBytes / (KB * KB * KB * KB);
            memBytes = memBytes % (KB * KB * KB * KB);
            values[NutsMemoryUnit.TERA_BYTE.ordinal()] = memBytes / (KB * KB * KB);
            memBytes = memBytes % (KB * KB * KB);
            values[NutsMemoryUnit.MEGA_BYTE.ordinal()] = memBytes / (KB * KB);
            memBytes = memBytes % (KB * KB);
            values[NutsMemoryUnit.KILO_BYTE.ordinal()] = memBytes / (KB);
            memBytes = memBytes % (KB);
            values[NutsMemoryUnit.BYTE.ordinal()] = memBytes;

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
        for (int i = 0; i < uo; i++) {
            values[i] = 0;
        }
        switch (this.largestUnit) {
            case ZETA_BYTE: {
                //do nothing
                break;
            }
            case PETA_BYTE: {
                add(NutsMemoryUnit.PETA_BYTE, KB * get(NutsMemoryUnit.ZETA_BYTE));
                set(NutsMemoryUnit.ZETA_BYTE, 0);
                break;
            }
            case TERA_BYTE: {
                add(NutsMemoryUnit.TERA_BYTE,
                        KB * get(NutsMemoryUnit.PETA_BYTE)
                                + KB * KB * get(NutsMemoryUnit.ZETA_BYTE)
                );
                set(NutsMemoryUnit.PETA_BYTE, 0);
                set(NutsMemoryUnit.ZETA_BYTE, 0);
                break;
            }
            case MEGA_BYTE: {
                add(NutsMemoryUnit.MEGA_BYTE,
                        KB * get(NutsMemoryUnit.TERA_BYTE)
                                + KB * KB * get(NutsMemoryUnit.PETA_BYTE)
                                + KB * KB * KB * get(NutsMemoryUnit.ZETA_BYTE)
                );
                set(NutsMemoryUnit.TERA_BYTE, 0);
                set(NutsMemoryUnit.ZETA_BYTE, 0);
                set(NutsMemoryUnit.PETA_BYTE, 0);
                break;
            }
            case KILO_BYTE: {
                add(NutsMemoryUnit.KILO_BYTE,
                        KB * get(NutsMemoryUnit.MEGA_BYTE)
                                + KB * KB * get(NutsMemoryUnit.TERA_BYTE)
                                + KB * KB * KB * get(NutsMemoryUnit.PETA_BYTE)
                                + KB * KB * KB * KB * get(NutsMemoryUnit.ZETA_BYTE)
                );
                set(NutsMemoryUnit.MEGA_BYTE, 0);
                set(NutsMemoryUnit.TERA_BYTE, 0);
                set(NutsMemoryUnit.ZETA_BYTE, 0);
                set(NutsMemoryUnit.PETA_BYTE, 0);
                break;
            }
            case BYTE: {
                add(NutsMemoryUnit.BYTE,
                        KB * get(NutsMemoryUnit.KILO_BYTE)
                                + KB * KB * get(NutsMemoryUnit.MEGA_BYTE)
                                + KB * KB * KB * get(NutsMemoryUnit.TERA_BYTE)
                                + KB * KB * KB * KB * get(NutsMemoryUnit.PETA_BYTE)
                                + KB * KB * KB * KB * KB * get(NutsMemoryUnit.ZETA_BYTE)
                );
                set(NutsMemoryUnit.KILO_BYTE, 0);
                set(NutsMemoryUnit.MEGA_BYTE, 0);
                set(NutsMemoryUnit.TERA_BYTE, 0);
                set(NutsMemoryUnit.ZETA_BYTE, 0);
                set(NutsMemoryUnit.PETA_BYTE, 0);
                break;
            }
            case BIT: {
                add(NutsMemoryUnit.BIT,
                        8L * get(NutsMemoryUnit.BYTE)
                                + 8L * KB * get(NutsMemoryUnit.KILO_BYTE)
                                + 8L * KB * KB * get(NutsMemoryUnit.MEGA_BYTE)
                                + 8L * KB * KB * KB * get(NutsMemoryUnit.TERA_BYTE)
                                + 8L * KB * KB * KB * KB * get(NutsMemoryUnit.PETA_BYTE)
                                + 8L * KB * KB * KB * KB * KB * get(NutsMemoryUnit.ZETA_BYTE)
                );
                set(NutsMemoryUnit.BYTE, 0);
                set(NutsMemoryUnit.KILO_BYTE, 0);
                set(NutsMemoryUnit.MEGA_BYTE, 0);
                set(NutsMemoryUnit.TERA_BYTE, 0);
                set(NutsMemoryUnit.ZETA_BYTE, 0);
                set(NutsMemoryUnit.PETA_BYTE, 0);
                break;
            }
        }
    }

    private boolean isZero0() {
        for (long value : values) {
            if (value != 0) {
                return false;
            }
        }
        return true;
    }

    private NutsMemoryUnit detectSmallestUnit() {
        if (isZero0()) {
            return NutsMemoryUnit.BIT;
        } else {
            for (int i = 0; i < values.length; i++) {
                long value = values[i];
                if (value != 0) {
                    return NutsMemoryUnit.values()[i];
                }
            }
            return NutsMemoryUnit.ZETA_BYTE;
        }
    }

    private NutsMemoryUnit detectLargestUnit() {
        if (isZero0()) {
            return NutsMemoryUnit.BIT;
        } else {
            for (int i = values.length - 1; i >= 0; i--) {
                long value = values[i];
                if (value != 0) {
                    return NutsMemoryUnit.values()[i];
                }
            }
            return NutsMemoryUnit.ZETA_BYTE;
        }
    }


    static NutsMemoryUnit normalize(NutsMemoryUnit smallestUnit) {
        switch (smallestUnit) {
            default:
                return smallestUnit;
        }
    }

    public static NutsMemorySize ofBits(long bits, boolean iec) {
        long bytes = bits / 8;
        int b = (int) (bits % 8);
        return new NutsMemorySize(bytes, b, iec);
    }

    public static NutsMemorySize ofBits(long bits, NutsMemoryUnit smallestUnit, NutsMemoryUnit largestUnit) {
        return ofBits(bits, smallestUnit, largestUnit, false);
    }

    public static NutsMemorySize ofBits(long bits, NutsMemoryUnit smallestUnit, NutsMemoryUnit largestUnit, boolean iec) {
        long bytes = bits / 8;
        int b = (int) (bits % 8);
        return new NutsMemorySize(bytes, b, smallestUnit, largestUnit, iec);
    }

    public static NutsMemorySize ofBitsOnly(long value) {
        return ofBitsOnly(value, false);
    }

    public static NutsMemorySize ofBytesOnly(long value) {
        return ofBytesOnly(value, false);
    }

    public static NutsMemorySize ofKiloBytesOnly(long value) {
        return ofKiloBytesOnly(value, false);
    }

    public static NutsMemorySize ofMegaBytesOnly(long value) {
        return ofMegaBytesOnly(value, false);
    }

    public static NutsMemorySize ofTeraBytesOnly(long value) {
        return ofTeraBytesOnly(value, false);
    }

    public static NutsMemorySize ofPetaBytesOnly(long value) {
        return ofPetaBytesOnly(value, false);
    }

    public static NutsMemorySize ofZetaBytesOnly(long value) {
        return ofZetaBytesOnly(value, false);
    }

    public static NutsMemorySize ofBits(long value) {
        return ofBits(value, false);
    }

    public static NutsMemorySize ofBytes(long value) {
        return ofBytes(value, false);
    }

    public static NutsMemorySize ofKiloBytes(long value) {
        return ofKiloBytes(value, false);
    }

    public static NutsMemorySize ofMegaBytes(long value) {
        return ofMegaBytes(value, false);
    }

    public static NutsMemorySize ofTeraBytes(long value) {
        return ofTeraBytes(value, false);
    }

    public static NutsMemorySize ofPetaBytes(long value) {
        return ofPetaBytes(value, false);
    }

    public static NutsMemorySize ofZetaBytes(long value) {
        return ofZetaBytes(value, false);
    }

    public static NutsMemorySize ofBitsOnly(long value, boolean iec) {
        return ofUnitOnly(value, NutsMemoryUnit.BIT, iec);
    }

    public static NutsMemorySize ofBytesOnly(long value, boolean iec) {
        return ofUnitOnly(value, NutsMemoryUnit.BYTE, iec);
    }

    public static NutsMemorySize ofKiloBytesOnly(long value, boolean iec) {
        return ofUnitOnly(value, NutsMemoryUnit.KILO_BYTE, iec);
    }

    public static NutsMemorySize ofMegaBytesOnly(long value, boolean iec) {
        return ofUnitOnly(value, NutsMemoryUnit.MEGA_BYTE, iec);
    }

    public static NutsMemorySize ofTeraBytesOnly(long value, boolean iec) {
        return ofUnitOnly(value, NutsMemoryUnit.TERA_BYTE, iec);
    }

    public static NutsMemorySize ofPetaBytesOnly(long value, boolean iec) {
        return ofUnitOnly(value, NutsMemoryUnit.PETA_BYTE, iec);
    }

    public static NutsMemorySize ofZetaBytesOnly(long value, boolean iec) {
        return ofUnitOnly(value, NutsMemoryUnit.ZETA_BYTE, iec);
    }

    public static NutsMemorySize ofBytes(long value, boolean iec) {
        return ofUnit(value, NutsMemoryUnit.BYTE, iec);
    }

    public static NutsMemorySize ofKiloBytes(long value, boolean iec) {
        return ofUnit(value, NutsMemoryUnit.KILO_BYTE, iec);
    }

    public static NutsMemorySize ofMegaBytes(long value, boolean iec) {
        return ofUnit(value, NutsMemoryUnit.MEGA_BYTE, iec);
    }

    public static NutsMemorySize ofTeraBytes(long value, boolean iec) {
        return ofUnit(value, NutsMemoryUnit.TERA_BYTE, iec);
    }

    public static NutsMemorySize ofPetaBytes(long value, boolean iec) {
        return ofUnit(value, NutsMemoryUnit.PETA_BYTE, iec);
    }

    public static NutsMemorySize ofZetaBytes(long value, boolean iec) {
        return ofUnit(value, NutsMemoryUnit.ZETA_BYTE, iec);
    }

    public static NutsMemorySize ofUnitOnly(long value, NutsMemoryUnit unit, boolean iec) {
        long[] values = new long[NutsMemoryUnit.values().length];
        values[unit.ordinal()] = value;
        return new NutsMemorySize(values, null, null, iec);
    }

    public static NutsMemorySize ofUnit(long valueInUnit, NutsMemoryUnit unit, boolean iec) {
        return ofUnitOnly(valueInUnit, unit, iec).normalize();
    }

    public static NutsMemorySize ofBytes(long durationMillis, NutsMemoryUnit smallestUnit, NutsMemoryUnit largestUnit, boolean iec) {
        return new NutsMemorySize(durationMillis, 0, smallestUnit, largestUnit, iec);
    }


    public static NutsMemorySize ofBytesAndBits(long bytes, int bits, boolean iec) {
        return new NutsMemorySize(bytes, bits, iec);
    }

    public static NutsMemorySize of(long[] values, NutsMemoryUnit smallestUnit, NutsMemoryUnit largestUnit, boolean iec) {
        return new NutsMemorySize(values, smallestUnit, largestUnit, iec);
    }

    public static NutsMemorySize of(long[] values, boolean iec) {
        return of(values, null, null, iec);
    }


    public NutsMemoryUnit firstNonZeroUp(NutsMemoryUnit unit) {
        NutsMemoryUnit[] values = NutsMemoryUnit.values();
        int o = unit.ordinal();
        while (o < values.length) {
            if (get(values[o]) != 0) {
                return values[o];
            }
            o++;
        }
        return null;
    }

    public NutsMemoryUnit firstNonZeroDown(NutsMemoryUnit unit) {
        NutsMemoryUnit[] values = NutsMemoryUnit.values();
        int o = unit.ordinal();
        while (o > 0) {
            if (get(values[o]) != 0) {
                return values[o];
            }
            o--;
        }
        return null;
    }

    public boolean isZero(NutsMemoryUnit fromIclusive, NutsMemoryUnit toInclusive) {
        for (int i = fromIclusive.ordinal(); i <= toInclusive.ordinal(); i++) {
            if (get(NutsMemoryUnit.values()[i]) != 0) {
                return false;
            }
        }
        return true;
    }

    public boolean isZeroDown(NutsMemoryUnit unit) {
        return isZero(NutsMemoryUnit.BIT, unit);
    }

    public boolean isZeroUp(NutsMemoryUnit unit) {
        return isZero(unit, NutsMemoryUnit.TERA_BYTE);
    }

    public long getAs(NutsMemoryUnit unit) {
        switch (unit) {
            case ZETA_BYTE:
                return getAsZetaBytes();
            case PETA_BYTE:
                return getAsPetaBytes();
            case TERA_BYTE:
                return getAsTeraBytes();
            case MEGA_BYTE:
                return getAsMegaBytes();
            case KILO_BYTE:
                return getAsKiloBytes();
            case BYTE:
                return getAsBytes();
            case BIT:
                return getAsBits();
        }
        return 0;
    }

    private void add(NutsMemoryUnit unit, long value) {
        this.values[unit.ordinal()] += value;
    }

    private void set(NutsMemoryUnit unit, long value) {
        this.values[unit.ordinal()] = value;
    }

    public long get(NutsMemoryUnit unit) {
        return values[unit.ordinal()];
    }

    public long getBits() {
        return get(NutsMemoryUnit.BIT);
    }

    public long getBytes() {
        return get(NutsMemoryUnit.BYTE);
    }

    public long getKiloBytes() {
        return get(NutsMemoryUnit.KILO_BYTE);
    }

    public long getMegaBytes() {
        return get(NutsMemoryUnit.MEGA_BYTE);
    }

    public long getTeraBytes() {
        return get(NutsMemoryUnit.TERA_BYTE);
    }

    public long getPetaBytes() {
        return get(NutsMemoryUnit.PETA_BYTE);
    }

    public long getZetaBytes() {
        return get(NutsMemoryUnit.ZETA_BYTE);
    }

    public NutsMemoryUnit getLargestUnit() {
        return largestUnit;
    }

    public NutsMemoryUnit getSmallestUnit() {
        return smallestUnit;
    }

    public long getAsZetaBytes() {
        return bytes / (KB * KB * KB * KB * KB);
    }

    public long getAsPetaBytes() {
        return bytes / (KB * KB * KB * KB);
    }

    public long getAsTeraBytes() {
        return bytes / (KB * KB * KB);
    }

    public long getAsMegaBytes() {
        return bytes / (KB * KB);
    }

    public long getAsKiloBytes() {
        return bytes / KB;
    }

    public long getAsBytes() {
        return bytes;
    }

    public long getAsBits() {
        return bytes * 8 + bits;
    }

    public long getMemoryBytes() {
        return bytes;
    }

    public int getMemoryBits() {
        return bits;
    }


    public boolean isIEC() {
        return iec;
    }

    public NutsMemorySize withIEC(boolean iec) {
        if (this.iec == iec) {
            return this;
        }
        return new NutsMemorySize(toUnitsArray(), smallestUnit, largestUnit, iec);
    }

    public NutsMemorySize withSmallestUnit(NutsMemoryUnit smallestUnit) {
        NutsMemorySize d = new NutsMemorySize(toUnitsArray(), smallestUnit, largestUnit, iec);
        if (this.bytes != d.bytes || this.bits != d.bits) {
            throw new IllegalArgumentException("unexpected");
        }
        return d;
    }

    public NutsMemorySize withLargestUnit(NutsMemoryUnit largestUnit) {
        NutsMemorySize d = new NutsMemorySize(toUnitsArray(), smallestUnit, largestUnit, iec);
        if (this.bytes != d.bytes || this.bits != d.bits) {
            throw new IllegalArgumentException("unexpected");
        }
        return d;
    }

    public NutsMemorySize withUnits(NutsMemoryUnit smallestUnit, NutsMemoryUnit largestUnit) {
        NutsMemorySize d = new NutsMemorySize(toUnitsArray(), smallestUnit, largestUnit, iec);
        if (this.bytes != d.bytes || this.bits != d.bits) {
            throw new IllegalArgumentException("unexpected");
        }
        return d;
    }

    private boolean normalizeNegativeUnit(long[] values, NutsMemoryUnit curr, NutsMemoryUnit next, long multiplier) {
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

    public NutsMemorySize neg() {
        long[] a = toUnitsArray();
        for (int i = 0; i < a.length; i++) {
            a[i] = -a[i];
        }
        return of(a, smallestUnit, largestUnit, iec);
    }

    public NutsMemorySize add(NutsMemorySize other) {
        long[] a = toUnitsArray();
        long[] b = other.toUnitsArray();
        for (int i = 0; i < a.length; i++) {
            a[i] += b[i];
        }
        return of(a,
                smallestUnit.compareTo(other.getSmallestUnit()) < 0 ? smallestUnit : other.smallestUnit,
                largestUnit.compareTo(other.getSmallestUnit()) > 0 ? largestUnit : other.smallestUnit
                , iec
        );
    }

    public NutsMemorySize mul(double other) {
        double ms = bytes * other;
        long msL = (long) (bytes * other);
        long ns = (long) (bits * other + (ms - msL) * 8);
        return ofBytesAndBits(msL, (int) ns, iec).withUnits(smallestUnit, largestUnit);
    }

    public NutsMemorySize mul(long other) {
        long[] a = toUnitsArray();
        for (int i = 0; i < a.length; i++) {
            a[i] *= other;
        }
        return of(a, smallestUnit, largestUnit, iec);
    }

    public NutsMemorySize subtract(NutsMemorySize other) {
        long[] a = toUnitsArray();
        long[] b = other.toUnitsArray();
        for (int i = 0; i < a.length; i++) {
            a[i] -= b[i];
        }
        return of(a,
                smallestUnit.compareTo(other.getSmallestUnit()) < 0 ? smallestUnit : other.smallestUnit,
                largestUnit.compareTo(other.getSmallestUnit()) > 0 ? largestUnit : other.smallestUnit
                , iec
        );
    }

    public NutsMemorySize normalize() {
        long[] values = toUnitsArray();
        if (normalizeNegativeUnit(values, NutsMemoryUnit.BIT, NutsMemoryUnit.BYTE, 8L)) {
            if (normalizeNegativeUnit(values, NutsMemoryUnit.BIT, NutsMemoryUnit.KILO_BYTE, 8L * KB)) {
                if (normalizeNegativeUnit(values, NutsMemoryUnit.BIT, NutsMemoryUnit.MEGA_BYTE, 8L * KB * KB)) {
                    if (normalizeNegativeUnit(values, NutsMemoryUnit.BIT, NutsMemoryUnit.TERA_BYTE, 8L * KB * KB * KB)) {
                        if (normalizeNegativeUnit(values, NutsMemoryUnit.BIT, NutsMemoryUnit.PETA_BYTE, 8L * KB * KB * KB * KB)) {
                            if (normalizeNegativeUnit(values, NutsMemoryUnit.BIT, NutsMemoryUnit.ZETA_BYTE, 8L * KB * KB * KB * KB * KB)) {
                            }
                        }
                    }
                }
            }
        }
        if (normalizeNegativeUnit(values, NutsMemoryUnit.BYTE, NutsMemoryUnit.KILO_BYTE, KB)) {
            if (normalizeNegativeUnit(values, NutsMemoryUnit.BYTE, NutsMemoryUnit.MEGA_BYTE, KB * KB)) {
                if (normalizeNegativeUnit(values, NutsMemoryUnit.BYTE, NutsMemoryUnit.TERA_BYTE, KB * KB * KB)) {
                    if (normalizeNegativeUnit(values, NutsMemoryUnit.BYTE, NutsMemoryUnit.PETA_BYTE, KB * KB * KB * KB)) {
                        if (normalizeNegativeUnit(values, NutsMemoryUnit.BYTE, NutsMemoryUnit.ZETA_BYTE, KB * KB * KB * KB * KB)) {
                        }
                    }
                }
            }
        }
        if (normalizeNegativeUnit(values, NutsMemoryUnit.KILO_BYTE, NutsMemoryUnit.MEGA_BYTE, KB)) {
            if (normalizeNegativeUnit(values, NutsMemoryUnit.KILO_BYTE, NutsMemoryUnit.TERA_BYTE, KB * KB)) {
                if (normalizeNegativeUnit(values, NutsMemoryUnit.KILO_BYTE, NutsMemoryUnit.PETA_BYTE, KB * KB * KB)) {
                    if (normalizeNegativeUnit(values, NutsMemoryUnit.KILO_BYTE, NutsMemoryUnit.ZETA_BYTE, KB * KB * KB * KB)) {
                    }
                }
            }
        }
        if (normalizeNegativeUnit(values, NutsMemoryUnit.MEGA_BYTE, NutsMemoryUnit.TERA_BYTE, KB)) {
            if (normalizeNegativeUnit(values, NutsMemoryUnit.MEGA_BYTE, NutsMemoryUnit.PETA_BYTE, KB * KB)) {
                if (normalizeNegativeUnit(values, NutsMemoryUnit.MEGA_BYTE, NutsMemoryUnit.ZETA_BYTE, KB * KB * KB)) {
                }
            }
        }
        if (normalizeNegativeUnit(values, NutsMemoryUnit.TERA_BYTE, NutsMemoryUnit.PETA_BYTE, KB)) {
            if (normalizeNegativeUnit(values, NutsMemoryUnit.TERA_BYTE, NutsMemoryUnit.ZETA_BYTE, KB * KB)) {
            }
        }
        if (normalizeNegativeUnit(values, NutsMemoryUnit.PETA_BYTE, NutsMemoryUnit.ZETA_BYTE, KB)) {
        }
        NutsMemoryUnit[] memUnits = NutsMemoryUnit.values();
        for (int i = 0; i < memUnits.length - 1; i++) {
            long n = values[i];
            if (n < 0) {
                long mul = 1;
                for (int j = i + 1; j < memUnits.length; j++) {
                    mul *= (j == 1 ? 9 : KB);
                    long p = values[j];
                    if (p > 0) {
                        long u = (-n) / mul;
                        if ((-n) % mul > 0) {
                            u++;
                        }
                        long x = Math.min(p, u);
                        n += x * mul;
                        values[j] -= x;
                        values[i] = n;
                        if (n >= 0) {
                            break;
                        }
                    }
                }
            }
        }
        for (int i = 0; i < memUnits.length - 1; i++) {
            long n = values[i];
            long r = (i == 0) ? 8 : KB;
            if (n >= r) {
                values[i] = n % r;
                values[i + 1] += n / r;
            }
        }
        NutsMemorySize d = new NutsMemorySize(values, smallestUnit, largestUnit, iec);
        if (this.bytes != d.bytes || this.bits != d.bits) {
            throw new IllegalArgumentException("unexpected");
        }
        return d;
    }

    public boolean isZero() {
        return ((bytes | bits) == 0);
    }

    public long[] toUnitsArray() {
        return Arrays.copyOf(values, values.length);
    }

    @Override
    public String toString() {
        return NutsMemorySizeFormat.DEFAULT.format(this);
    }

    public String toString(boolean fixed, Boolean iec) {
        return NutsMemorySizeFormat.of(fixed, iec).format(this);
    }

    public String toString(boolean fixed) {
        return NutsMemorySizeFormat.of(fixed, null).format(this);
    }

    @Override
    public NutsFormat formatter(NutsSession session) {
        return NutsFormat.of(session, new NutsFormatSPI() {
            private Boolean iec;
            private boolean fixed;

            @Override
            public String getName() {
                return "memory-size";
            }

            @Override
            public void print(NutsPrintStream out) {
                NutsMemorySizeFormat.of(fixed, iec).print(NutsMemorySize.this, out);
            }

            @Override
            public boolean configureFirst(NutsCommandLine commandLine) {
                NutsArgument a = commandLine.peek().get(session);
                switch (a.key()) {
                    case "--iec": {
                        a = commandLine.nextBoolean().get(session);
                        if (a.isActive()) {
                            iec = a.getBooleanValue().get();
                        }
                        return true;
                    }
                    case "--fixed": {
                        a = commandLine.nextBoolean().get(session);
                        if (a.isActive()) {
                            fixed = a.getBooleanValue().get();
                        }
                        return true;
                    }
                }
                return false;
            }
        });
    }
}
