package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NMapBy;

import java.io.IOException;
import java.io.Serializable;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.Arrays;

public class NMemorySize implements Serializable {
    private final long[] values = new long[NMemoryUnit.values().length];
    private final NMemoryUnit smallestUnit;
    private final NMemoryUnit largestUnit;
    private final long bytes;
    private final int bits;
    private final long KB;
    private final boolean iec;

    @NMapBy
    public NMemorySize(
            @NMapBy(name = "bits") long bits,
            @NMapBy(name = "bytes") long bytes,
            @NMapBy(name = "kiloBytes") long kiloBytes,
            @NMapBy(name = "megaBytes") long megaBytes,
            @NMapBy(name = "teraBytes") long teraBytes,
            @NMapBy(name = "petaBytes") long petaBytes,
            @NMapBy(name = "zetaBytes") long zetaBytes,
            @NMapBy(name = "smallestUnit") NMemoryUnit smallestUnit,
            @NMapBy(name = "largestUnit") NMemoryUnit largestUnit,
            @NMapBy(name = "iec") boolean iec
    ) {
        this.iec = iec;
        this.KB = iec ? 1000 : 1024;
        this.values[NMemoryUnit.BIT.ordinal()] = bits;
        this.values[NMemoryUnit.BYTE.ordinal()] = bytes;
        this.values[NMemoryUnit.KILO_BYTE.ordinal()] = kiloBytes;
        this.values[NMemoryUnit.MEGA_BYTE.ordinal()] = megaBytes;
        this.values[NMemoryUnit.TERA_BYTE.ordinal()] = teraBytes;
        this.values[NMemoryUnit.PETA_BYTE.ordinal()] = petaBytes;
        this.values[NMemoryUnit.ZETA_BYTE.ordinal()] = zetaBytes;
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
        return (int) (this.values[NMemoryUnit.BIT.ordinal()] % 8);
    }

    public NMemorySize(long[] values, NMemoryUnit smallestUnit, NMemoryUnit largestUnit, boolean iec) {
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
        return values[NMemoryUnit.BYTE.ordinal()]
                + values[NMemoryUnit.KILO_BYTE.ordinal()] * KB
                + values[NMemoryUnit.MEGA_BYTE.ordinal()] * KB * KB
                + values[NMemoryUnit.GIGA_BYTE.ordinal()] * KB * KB * KB
                + values[NMemoryUnit.TERA_BYTE.ordinal()] * KB * KB * KB * KB
                + values[NMemoryUnit.PETA_BYTE.ordinal()] * KB * KB * KB * KB * KB
                + values[NMemoryUnit.ZETA_BYTE.ordinal()] * KB * KB * KB * KB * KB * KB;
    }

    public NMemorySize(long memBytes, int memBits, boolean iec) {
        this.iec = iec;
        this.KB = iec ? 1000 : 1024;
        memBytes += memBits / 8;
        memBits = memBits % 8;
        this.bytes = memBytes;
        this.bits = memBits;

        values[NMemoryUnit.ZETA_BYTE.ordinal()] = memBytes / (KB * KB * KB * KB * KB * KB);
        memBytes = memBytes % (KB * KB * KB * KB * KB * KB);
        values[NMemoryUnit.PETA_BYTE.ordinal()] = memBytes / (KB * KB * KB * KB * KB);
        memBytes = memBytes % (KB * KB * KB * KB * KB);
        values[NMemoryUnit.TERA_BYTE.ordinal()] = memBytes / (KB * KB * KB * KB);
        memBytes = memBytes % (KB * KB * KB * KB);
        values[NMemoryUnit.GIGA_BYTE.ordinal()] = memBytes / (KB * KB * KB);
        memBytes = memBytes % (KB * KB * KB);
        values[NMemoryUnit.MEGA_BYTE.ordinal()] = memBytes / (KB * KB);
        memBytes = memBytes % (KB * KB);
        values[NMemoryUnit.KILO_BYTE.ordinal()] = memBytes / (KB);
        memBytes = memBytes % (KB);
        values[NMemoryUnit.BYTE.ordinal()] = memBytes;
        this.smallestUnit = detectSmallestUnit();
        this.largestUnit = detectLargestUnit();
    }

    public NMemorySize(long memBytes, int memBits, NMemoryUnit smallestUnit, NMemoryUnit largestUnit, boolean iec) {
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
            if (smallestUnitOrdinal <= NMemoryUnit.BIT.ordinal()) {
                if (largestUnitOrdinal > NMemoryUnit.BIT.ordinal()) {
                    this.values[NMemoryUnit.BIT.ordinal()] = memBits;
                } else {
                    this.values[NMemoryUnit.BIT.ordinal()] = memBits + memBytes * 8;
                    return;
                }
            }
            if (smallestUnitOrdinal <= NMemoryUnit.BYTE.ordinal()) {
                if (largestUnitOrdinal > NMemoryUnit.BYTE.ordinal()) {
                    this.values[NMemoryUnit.BYTE.ordinal()] = memBytes % KB;
                } else {
                    this.values[NMemoryUnit.BYTE.ordinal()] = memBytes;
                    return;
                }
            }
            if (smallestUnitOrdinal <= NMemoryUnit.KILO_BYTE.ordinal()) {
                if (largestUnitOrdinal > NMemoryUnit.KILO_BYTE.ordinal()) {
                    this.values[NMemoryUnit.KILO_BYTE.ordinal()] = (int) ((memBytes / KB) % KB);
                } else {
                    this.values[NMemoryUnit.KILO_BYTE.ordinal()] = memBytes / KB;
                    return;
                }
            }
            if (smallestUnitOrdinal <= NMemoryUnit.MEGA_BYTE.ordinal()) {
                if (largestUnitOrdinal > NMemoryUnit.MEGA_BYTE.ordinal()) {
                    this.values[NMemoryUnit.MEGA_BYTE.ordinal()] = (int) ((memBytes / (KB * KB)) % KB);
                } else {
                    this.values[NMemoryUnit.MEGA_BYTE.ordinal()] = memBytes / (KB * KB);
                    return;
                }
            }
            if (smallestUnitOrdinal <= NMemoryUnit.GIGA_BYTE.ordinal()) {
                if (largestUnitOrdinal > NMemoryUnit.GIGA_BYTE.ordinal()) {
                    this.values[NMemoryUnit.GIGA_BYTE.ordinal()] = (int) ((memBytes / (KB * KB * KB)) % KB);
                } else {
                    this.values[NMemoryUnit.GIGA_BYTE.ordinal()] = memBytes / (KB * KB * KB);
                    return;
                }
            }
            if (smallestUnitOrdinal <= NMemoryUnit.TERA_BYTE.ordinal()) {
                if (largestUnitOrdinal > NMemoryUnit.TERA_BYTE.ordinal()) {
                    this.values[NMemoryUnit.TERA_BYTE.ordinal()] = (int) ((memBytes / (KB * KB * KB * KB)) % KB);
                } else {
                    this.values[NMemoryUnit.TERA_BYTE.ordinal()] = memBytes / (KB * KB * KB * KB);
                    return;
                }
            }
            if (smallestUnitOrdinal <= NMemoryUnit.PETA_BYTE.ordinal()) {
                if (largestUnitOrdinal > NMemoryUnit.PETA_BYTE.ordinal()) {
                    this.values[NMemoryUnit.PETA_BYTE.ordinal()] = (int) ((memBytes / (KB * KB * KB * KB * KB)) % KB);
                } else {
                    this.values[NMemoryUnit.PETA_BYTE.ordinal()] = memBytes / (KB * KB * KB * KB * KB);
                    return;
                }
            }
            if (smallestUnitOrdinal <= NMemoryUnit.ZETA_BYTE.ordinal()) {
//                if (largestUnitOrdinal > NutsMemoryUnit.ZETA_BYTE.ordinal()) {
//                    this.values[NutsMemoryUnit.ZETA_BYTE.ordinal()] = (int) ((memBytes / (KB*KB*KB*KB*KB)) % KB);
//                } else {
                this.values[NMemoryUnit.ZETA_BYTE.ordinal()] = memBytes / (KB * KB * KB * KB * KB * KB);
//                    return;
//                }
            }
        } else {
            values[NMemoryUnit.ZETA_BYTE.ordinal()] = memBytes / (KB * KB * KB * KB * KB * KB);
            memBytes = memBytes % (KB * KB * KB * KB * KB * KB);
            values[NMemoryUnit.PETA_BYTE.ordinal()] = memBytes / (KB * KB * KB * KB * KB);
            memBytes = memBytes % (KB * KB * KB * KB * KB);
            values[NMemoryUnit.TERA_BYTE.ordinal()] = memBytes / (KB * KB * KB * KB);
            memBytes = memBytes % (KB * KB * KB * KB);
            values[NMemoryUnit.GIGA_BYTE.ordinal()] = memBytes / (KB * KB * KB);
            memBytes = memBytes % (KB * KB * KB);
            values[NMemoryUnit.MEGA_BYTE.ordinal()] = memBytes / (KB * KB);
            memBytes = memBytes % (KB * KB);
            values[NMemoryUnit.KILO_BYTE.ordinal()] = memBytes / (KB);
            memBytes = memBytes % (KB);
            values[NMemoryUnit.BYTE.ordinal()] = memBytes;

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
                add(NMemoryUnit.PETA_BYTE, KB * get(NMemoryUnit.ZETA_BYTE));
                set(NMemoryUnit.ZETA_BYTE, 0);
                break;
            }
            case TERA_BYTE: {
                add(NMemoryUnit.TERA_BYTE,
                        KB * get(NMemoryUnit.PETA_BYTE)
                                + KB * KB * get(NMemoryUnit.ZETA_BYTE)
                );
                set(NMemoryUnit.PETA_BYTE, 0);
                set(NMemoryUnit.ZETA_BYTE, 0);
                break;
            }
            case GIGA_BYTE: {
                add(NMemoryUnit.TERA_BYTE,
                        KB * get(NMemoryUnit.TERA_BYTE)
                                + KB * KB * get(NMemoryUnit.PETA_BYTE)
                                + KB * KB * KB * get(NMemoryUnit.ZETA_BYTE)
                );
                set(NMemoryUnit.TERA_BYTE, 0);
                set(NMemoryUnit.PETA_BYTE, 0);
                set(NMemoryUnit.ZETA_BYTE, 0);
                break;
            }
            case MEGA_BYTE: {
                add(NMemoryUnit.MEGA_BYTE,
                        KB * get(NMemoryUnit.GIGA_BYTE)
                                + KB * KB * get(NMemoryUnit.TERA_BYTE)
                                + KB * KB * KB * get(NMemoryUnit.PETA_BYTE)
                                + KB * KB * KB * KB * get(NMemoryUnit.ZETA_BYTE)
                );
                set(NMemoryUnit.GIGA_BYTE, 0);
                set(NMemoryUnit.TERA_BYTE, 0);
                set(NMemoryUnit.ZETA_BYTE, 0);
                set(NMemoryUnit.PETA_BYTE, 0);
                break;
            }
            case KILO_BYTE: {
                add(NMemoryUnit.KILO_BYTE,
                        KB * get(NMemoryUnit.MEGA_BYTE)
                                + KB * KB * get(NMemoryUnit.GIGA_BYTE)
                                + KB * KB * KB * get(NMemoryUnit.TERA_BYTE)
                                + KB * KB * KB * KB * get(NMemoryUnit.PETA_BYTE)
                                + KB * KB * KB * KB * KB * get(NMemoryUnit.ZETA_BYTE)
                );
                set(NMemoryUnit.MEGA_BYTE, 0);
                set(NMemoryUnit.GIGA_BYTE, 0);
                set(NMemoryUnit.TERA_BYTE, 0);
                set(NMemoryUnit.ZETA_BYTE, 0);
                set(NMemoryUnit.PETA_BYTE, 0);
                break;
            }
            case BYTE: {
                add(NMemoryUnit.BYTE,
                        KB * get(NMemoryUnit.KILO_BYTE)
                                + KB * KB * get(NMemoryUnit.MEGA_BYTE)
                                + KB * KB * KB * get(NMemoryUnit.GIGA_BYTE)
                                + KB * KB * KB * KB * get(NMemoryUnit.TERA_BYTE)
                                + KB * KB * KB * KB * KB * get(NMemoryUnit.PETA_BYTE)
                                + KB * KB * KB * KB * KB * KB * get(NMemoryUnit.ZETA_BYTE)
                );
                set(NMemoryUnit.KILO_BYTE, 0);
                set(NMemoryUnit.MEGA_BYTE, 0);
                set(NMemoryUnit.GIGA_BYTE, 0);
                set(NMemoryUnit.TERA_BYTE, 0);
                set(NMemoryUnit.ZETA_BYTE, 0);
                set(NMemoryUnit.PETA_BYTE, 0);
                break;
            }
            case BIT: {
                add(NMemoryUnit.BIT,
                        8L * get(NMemoryUnit.BYTE)
                                + 8L * KB * get(NMemoryUnit.KILO_BYTE)
                                + 8L * KB * KB * get(NMemoryUnit.MEGA_BYTE)
                                + 8L * KB * KB * KB * get(NMemoryUnit.GIGA_BYTE)
                                + 8L * KB * KB * KB * KB * get(NMemoryUnit.TERA_BYTE)
                                + 8L * KB * KB * KB * KB * KB * get(NMemoryUnit.PETA_BYTE)
                                + 8L * KB * KB * KB * KB * KB * KB * get(NMemoryUnit.ZETA_BYTE)
                );
                set(NMemoryUnit.BYTE, 0);
                set(NMemoryUnit.KILO_BYTE, 0);
                set(NMemoryUnit.MEGA_BYTE, 0);
                set(NMemoryUnit.GIGA_BYTE, 0);
                set(NMemoryUnit.TERA_BYTE, 0);
                set(NMemoryUnit.ZETA_BYTE, 0);
                set(NMemoryUnit.PETA_BYTE, 0);
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

    private NMemoryUnit detectSmallestUnit() {
        if (isZero0()) {
            return NMemoryUnit.BIT;
        } else {
            for (int i = 0; i < values.length; i++) {
                long value = values[i];
                if (value != 0) {
                    return NMemoryUnit.values()[i];
                }
            }
            return NMemoryUnit.ZETA_BYTE;
        }
    }

    private NMemoryUnit detectLargestUnit() {
        if (isZero0()) {
            return NMemoryUnit.BIT;
        } else {
            for (int i = values.length - 1; i >= 0; i--) {
                long value = values[i];
                if (value != 0) {
                    return NMemoryUnit.values()[i];
                }
            }
            return NMemoryUnit.ZETA_BYTE;
        }
    }


    static NMemoryUnit normalize(NMemoryUnit smallestUnit) {
        switch (smallestUnit) {
            default:
                return smallestUnit;
        }
    }

    public static NMemorySize ofBits(long bits, boolean iec) {
        long bytes = bits / 8;
        int b = (int) (bits % 8);
        return new NMemorySize(bytes, b, iec);
    }

    public static NMemorySize ofBits(long bits, NMemoryUnit smallestUnit, NMemoryUnit largestUnit) {
        return ofBits(bits, smallestUnit, largestUnit, false);
    }

    public static NMemorySize ofBits(long bits, NMemoryUnit smallestUnit, NMemoryUnit largestUnit, boolean iec) {
        long bytes = bits / 8;
        int b = (int) (bits % 8);
        return new NMemorySize(bytes, b, smallestUnit, largestUnit, iec);
    }

    public static NMemorySize ofBitsOnly(long value) {
        return ofBitsOnly(value, false);
    }

    public static NMemorySize ofBytesOnly(long value) {
        return ofBytesOnly(value, false);
    }

    public static NMemorySize ofKiloBytesOnly(long value) {
        return ofKiloBytesOnly(value, false);
    }

    public static NMemorySize ofMegaBytesOnly(long value) {
        return ofMegaBytesOnly(value, false);
    }

    public static NMemorySize ofTeraBytesOnly(long value) {
        return ofTeraBytesOnly(value, false);
    }

    public static NMemorySize ofPetaBytesOnly(long value) {
        return ofPetaBytesOnly(value, false);
    }

    public static NMemorySize ofZetaBytesOnly(long value) {
        return ofZetaBytesOnly(value, false);
    }

    public static NMemorySize ofBits(long value) {
        return ofBits(value, false);
    }

    public static NMemorySize ofBytes(long value) {
        return ofBytes(value, false);
    }

    public static NMemorySize ofKiloBytes(long value) {
        return ofKiloBytes(value, false);
    }

    public static NMemorySize ofMegaBytes(long value) {
        return ofMegaBytes(value, false);
    }

    public static NMemorySize ofTeraBytes(long value) {
        return ofTeraBytes(value, false);
    }

    public static NMemorySize ofPetaBytes(long value) {
        return ofPetaBytes(value, false);
    }

    public static NMemorySize ofZetaBytes(long value) {
        return ofZetaBytes(value, false);
    }

    public static NMemorySize ofBitsOnly(long value, boolean iec) {
        return ofUnitOnly(value, NMemoryUnit.BIT, iec);
    }

    public static NMemorySize ofBytesOnly(long value, boolean iec) {
        return ofUnitOnly(value, NMemoryUnit.BYTE, iec);
    }

    public static NMemorySize ofKiloBytesOnly(long value, boolean iec) {
        return ofUnitOnly(value, NMemoryUnit.KILO_BYTE, iec);
    }

    public static NMemorySize ofMegaBytesOnly(long value, boolean iec) {
        return ofUnitOnly(value, NMemoryUnit.MEGA_BYTE, iec);
    }

    public static NMemorySize ofTeraBytesOnly(long value, boolean iec) {
        return ofUnitOnly(value, NMemoryUnit.TERA_BYTE, iec);
    }

    public static NMemorySize ofPetaBytesOnly(long value, boolean iec) {
        return ofUnitOnly(value, NMemoryUnit.PETA_BYTE, iec);
    }

    public static NMemorySize ofZetaBytesOnly(long value, boolean iec) {
        return ofUnitOnly(value, NMemoryUnit.ZETA_BYTE, iec);
    }

    public static NMemorySize ofBytes(long value, boolean iec) {
        return ofUnit(value, NMemoryUnit.BYTE, iec);
    }

    public static NMemorySize ofKiloBytes(long value, boolean iec) {
        return ofUnit(value, NMemoryUnit.KILO_BYTE, iec);
    }

    public static NMemorySize ofMegaBytes(long value, boolean iec) {
        return ofUnit(value, NMemoryUnit.MEGA_BYTE, iec);
    }

    public static NMemorySize ofTeraBytes(long value, boolean iec) {
        return ofUnit(value, NMemoryUnit.TERA_BYTE, iec);
    }

    public static NMemorySize ofPetaBytes(long value, boolean iec) {
        return ofUnit(value, NMemoryUnit.PETA_BYTE, iec);
    }

    public static NMemorySize ofZetaBytes(long value, boolean iec) {
        return ofUnit(value, NMemoryUnit.ZETA_BYTE, iec);
    }

    public static NMemorySize ofUnitOnly(long value, NMemoryUnit unit, boolean iec) {
        long[] values = new long[NMemoryUnit.values().length];
        values[unit.ordinal()] = value;
        return new NMemorySize(values, null, null, iec);
    }

    public static NMemorySize ofUnit(long valueInUnit, NMemoryUnit unit, boolean iec) {
        return ofUnitOnly(valueInUnit, unit, iec).normalize();
    }

    public static NMemorySize ofBytes(long durationMillis, NMemoryUnit smallestUnit, NMemoryUnit largestUnit, boolean iec) {
        return new NMemorySize(durationMillis, 0, smallestUnit, largestUnit, iec);
    }


    public static NMemorySize ofBytesAndBits(long bytes, int bits, boolean iec) {
        return new NMemorySize(bytes, bits, iec);
    }

    public static NMemorySize of(long[] values, NMemoryUnit smallestUnit, NMemoryUnit largestUnit, boolean iec) {
        return new NMemorySize(values, smallestUnit, largestUnit, iec);
    }

    public static NMemorySize of(long[] values, boolean iec) {
        return of(values, null, null, iec);
    }


    public NMemoryUnit firstNonZeroUp(NMemoryUnit unit) {
        NMemoryUnit[] values = NMemoryUnit.values();
        int o = unit.ordinal();
        while (o < values.length) {
            if (get(values[o]) != 0) {
                return values[o];
            }
            o++;
        }
        return null;
    }

    public NMemoryUnit firstNonZeroDown(NMemoryUnit unit) {
        NMemoryUnit[] values = NMemoryUnit.values();
        int o = unit.ordinal();
        while (o > 0) {
            if (get(values[o]) != 0) {
                return values[o];
            }
            o--;
        }
        return null;
    }

    public boolean isZero(NMemoryUnit fromIclusive, NMemoryUnit toInclusive) {
        for (int i = fromIclusive.ordinal(); i <= toInclusive.ordinal(); i++) {
            if (get(NMemoryUnit.values()[i]) != 0) {
                return false;
            }
        }
        return true;
    }

    public boolean isZeroDown(NMemoryUnit unit) {
        return isZero(NMemoryUnit.BIT, unit);
    }

    public boolean isZeroUp(NMemoryUnit unit) {
        return isZero(unit, NMemoryUnit.TERA_BYTE);
    }

    public long getAs(NMemoryUnit unit) {
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

    private void add(NMemoryUnit unit, long value) {
        this.values[unit.ordinal()] += value;
    }

    private void set(NMemoryUnit unit, long value) {
        this.values[unit.ordinal()] = value;
    }

    public long get(NMemoryUnit unit) {
        return values[unit.ordinal()];
    }

    public long getBits() {
        return get(NMemoryUnit.BIT);
    }

    public long getBytes() {
        return get(NMemoryUnit.BYTE);
    }

    public long getKiloBytes() {
        return get(NMemoryUnit.KILO_BYTE);
    }

    public long getMegaBytes() {
        return get(NMemoryUnit.MEGA_BYTE);
    }

    public long getTeraBytes() {
        return get(NMemoryUnit.TERA_BYTE);
    }

    public long getPetaBytes() {
        return get(NMemoryUnit.PETA_BYTE);
    }

    public long getZetaBytes() {
        return get(NMemoryUnit.ZETA_BYTE);
    }

    public NMemoryUnit getLargestUnit() {
        return largestUnit;
    }

    public NMemoryUnit getSmallestUnit() {
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

    public NMemorySize withIEC(boolean iec) {
        if (this.iec == iec) {
            return this;
        }
        return new NMemorySize(toUnitsArray(), smallestUnit, largestUnit, iec);
    }

    public NMemorySize withSmallestUnit(NMemoryUnit smallestUnit) {
        NMemorySize d = new NMemorySize(toUnitsArray(), smallestUnit, largestUnit, iec);
        if (this.bytes != d.bytes || this.bits != d.bits) {
            throw new IllegalArgumentException("unexpected");
        }
        return d;
    }

    public NMemorySize withLargestUnit(NMemoryUnit largestUnit) {
        NMemorySize d = new NMemorySize(toUnitsArray(), smallestUnit, largestUnit, iec);
        if (this.bytes != d.bytes || this.bits != d.bits) {
            throw new IllegalArgumentException("unexpected");
        }
        return d;
    }

    public NMemorySize withUnits(NMemoryUnit smallestUnit, NMemoryUnit largestUnit) {
        NMemorySize d = new NMemorySize(toUnitsArray(), smallestUnit, largestUnit, iec);
        if (this.bytes != d.bytes || this.bits != d.bits) {
            throw new IllegalArgumentException("unexpected");
        }
        return d;
    }

    private boolean normalizeNegativeUnit(long[] values, NMemoryUnit curr, NMemoryUnit next, long multiplier) {
        if (values[curr.ordinal()] < 0) {
            if (values[next.ordinal()] > 0) {
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

    public NMemorySize neg() {
        long[] a = toUnitsArray();
        for (int i = 0; i < a.length; i++) {
            a[i] = -a[i];
        }
        return of(a, smallestUnit, largestUnit, iec);
    }

    public NMemorySize add(NMemorySize other) {
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

    public NMemorySize mul(double other) {
        double ms = bytes * other;
        long msL = (long) (bytes * other);
        long ns = (long) (bits * other + (ms - msL) * 8);
        return ofBytesAndBits(msL, (int) ns, iec).withUnits(smallestUnit, largestUnit);
    }

    public NMemorySize mul(long other) {
        long[] a = toUnitsArray();
        for (int i = 0; i < a.length; i++) {
            a[i] *= other;
        }
        return of(a, smallestUnit, largestUnit, iec);
    }

    public NMemorySize subtract(NMemorySize other) {
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

    public NMemorySize normalize() {
        long[] values = toUnitsArray();
        NMemoryUnit[] mUnits = NMemoryUnit.values();
        for (int i = 0; i < mUnits.length-1; i++) {
            NMemoryUnit value = mUnits[i];
            long mul=i==0?8:KB;
            for (int j = i; j <mUnits.length; j++) {
                if (!normalizeNegativeUnit(values, value, mUnits[j], mul)) {
                    break;
                }
                mul*=KB;
            }
        }
        NMemoryUnit[] memUnits = mUnits;
        for (int i = 0; i < memUnits.length - 1; i++) {
            long n = values[i];
            if (n < 0) {
                long mul = 1;
                for (int j = i + 1; j < memUnits.length; j++) {
                    mul *= (j == 1 ? 8 : KB);
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
        NMemorySize d = new NMemorySize(values, smallestUnit, largestUnit, iec);
        if (this.bytes != d.bytes || this.bits != d.bits) {
            throw new IllegalArgumentException("unexpected " + d + "<>" + this);
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
        return NMemorySizeFormat.DEFAULT.format(this);
    }

    public String toString(boolean fixed, Boolean iec) {
        return NMemorySizeFormat.of(fixed, iec).format(this);
    }

    public String toString(boolean fixed) {
        return NMemorySizeFormat.of(fixed, null).format(this);
    }

    public static NOptional<NMemorySize> parse(String value, NMemoryUnit defaultUnit) {
        if (defaultUnit == null) {
            defaultUnit = NMemoryUnit.BYTE;
        }
        value = NStringUtils.trimToNull(value);
        if (value == null) {
            return NOptional.ofNull();
        }
        NStreamTokenizer st = new NStreamTokenizer(new StringReader(value));
        try {
            int r = st.nextToken();
            if (r == StreamTokenizer.TT_NUMBER) {
                Number nval = st.nval;
                StringBuilder sb = new StringBuilder();
                while (true) {
                    r = st.nextToken();
                    if (r == StreamTokenizer.TT_EOF) {
                        break;
                    }
                    if (r == ' ') {
                        //ignore
                    } else if (
                            (r >= 'a' && r <= 'z')
                                    || (r >= 'A' && r <= 'Z')
                    ) {
                        sb.append((char) r);
                    } else {
                        String finalValue = value;
                        int finalR = r;
                        return NOptional.ofError(() -> NMsg.ofC(
                                "unexpected char %s in memory size : %s",
                                String.valueOf((char) finalR),
                                String.valueOf(finalValue)
                        ));
                    }
                }
                String unitString = sb.toString();
                if (unitString.isEmpty()) {
                    return NOptional.of(NMemorySize.ofUnit(nval.longValue(), defaultUnit, false));
                }
                return NOptional.ofNull();
            }
        } catch (Exception ie) {
            String finalValue1 = value;
            return NOptional.ofError(() -> NMsg.ofC(
                    "erroneous memory size : %s",
                    String.valueOf(finalValue1)
            ), ie);
        }
        String finalValue1 = value;
        return NOptional.ofError(() -> NMsg.ofC(
                "erroneous memory size : %s",
                String.valueOf(finalValue1)
        ));
    }
}
