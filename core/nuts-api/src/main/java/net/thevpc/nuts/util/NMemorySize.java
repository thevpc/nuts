package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NMapBy;
import net.thevpc.nuts.expr.NToken;
import net.thevpc.nuts.io.NStreamTokenizer;
import net.thevpc.nuts.text.NMsg;

import java.io.Serializable;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.Arrays;

/**
 * NMemorySize class.
 *
 * @author thevpc
 * @since 0.8.0
 */
@NImmutable
public class NMemorySize implements Serializable{
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
        this.smallestUnit = smallestUnit == null ? detectSmallestUnit() : smallestUnit;
        largestUnit = largestUnit == null ? detectLargestUnit() : largestUnit;
        if (largestUnit.ordinal() < this.smallestUnit.ordinal()) {
            largestUnit = this.smallestUnit;
        }
        this.largestUnit = largestUnit;
      /**
       * Apply units.
       */
        applyUnits();
      /**
       * Check me.
       */
        checkMe();
    }


    /**
     * N memory size.
     *
     * @param memBytes mem bytes
     * @param memBits mem bits
     * @param iec iec
     * @return n memory size result
     */
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
      /**
       * Check me.
       */
        checkMe();
    }

    /**
     * N memory size.
     *
     * @param memBytes mem bytes
     * @param memBits mem bits
     * @param smallestUnit smallest unit
     * @param largestUnit largest unit
     * @param iec iec
     * @return n memory size result
     */
    public NMemorySize(long memBytes, int memBits, NMemoryUnit smallestUnit, NMemoryUnit largestUnit, boolean iec) {
        this.iec = iec;
        this.KB = iec ? 1000 : 1024;
        memBytes += memBits / 8;
        memBits = memBits % 8;
        this.bytes = memBytes;
        this.bits = memBits;
        if (smallestUnit != null && largestUnit != null) {
            this.smallestUnit = smallestUnit;
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

            this.smallestUnit = smallestUnit == null ? detectSmallestUnit() : smallestUnit;
            largestUnit = largestUnit == null ? detectLargestUnit() : largestUnit;
            if (largestUnit.ordinal() < this.smallestUnit.ordinal()) {
                largestUnit = this.smallestUnit;
            }
            this.largestUnit = largestUnit;
          /**
           * Apply units.
           */
            applyUnits();
        }
      /**
       * Check me.
       */
        checkMe();
    }

    /**
     * N memory size.
     *
     * @param values values
     * @param smallestUnit smallest unit
     * @param largestUnit largest unit
     * @param iec iec
     * @return n memory size result
     */
    public NMemorySize(long[] values, NMemoryUnit smallestUnit, NMemoryUnit largestUnit, boolean iec) {
        this.iec = iec;
        this.KB = iec ? 1000 : 1024;
        for (int i = 0; i < this.values.length; i++) {
            this.values[i] = values[i];
        }
        this.smallestUnit = smallestUnit == null ? detectSmallestUnit() : smallestUnit;
        largestUnit = largestUnit == null ? detectLargestUnit() : largestUnit;
        if (largestUnit.ordinal() < this.smallestUnit.ordinal()) {
            largestUnit = this.smallestUnit;
        }
        this.largestUnit = largestUnit;
      /**
       * Apply units.
       */
        applyUnits();
        this.bytes = rebuildSizeBytes();
        this.bits = rebuildSizeBits();
    }

    /**
     * Check me.
     *
     * @return check me result
     */
    private void checkMe() {
        //active only in debug mode!
        if(false) {
            long t = rebuildSizeBytes();
            if (t != bytes) {
                throw NException.ofSafeAssertException(NMsg.ofC("why"));
            }
        }
    }


    /**
     * Rebuild size bits.
     *
     * @return rebuild size bits result
     */
    private int rebuildSizeBits() {
      /**
       * Return.
       *
       * @param 8 8
       */
        return (int) (this.values[NMemoryUnit.BIT.ordinal()] % 8);
    }

    /**
     * Rebuild size bytes.
     *
     * @return rebuild size bytes result
     */
    private long rebuildSizeBytes() {
        return values[NMemoryUnit.BYTE.ordinal()]
                + values[NMemoryUnit.KILO_BYTE.ordinal()] * KB
                + values[NMemoryUnit.MEGA_BYTE.ordinal()] * KB * KB
                + values[NMemoryUnit.GIGA_BYTE.ordinal()] * KB * KB * KB
                + values[NMemoryUnit.TERA_BYTE.ordinal()] * KB * KB * KB * KB
                + values[NMemoryUnit.PETA_BYTE.ordinal()] * KB * KB * KB * KB * KB
                + values[NMemoryUnit.ZETA_BYTE.ordinal()] * KB * KB * KB * KB * KB * KB;
    }


    /**
     * Apply units.
     *
     * @return apply units result
     */
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
              /**
               * Adds add.
               *
               * @param NMemoryUnit.PETA_BYTE n memory unit.peta_byte
               * @param get(NMemoryUnit.ZETA_BYTE) get(n memory unit.zeta_byte)
               */
                add(NMemoryUnit.PETA_BYTE, KB * get(NMemoryUnit.ZETA_BYTE));
              /**
               * Sets the set.
               *
               * @param NMemoryUnit.ZETA_BYTE n memory unit.zeta_byte
               * @param 0 0
               */
                set(NMemoryUnit.ZETA_BYTE, 0);
                break;
            }
            case TERA_BYTE: {
                add(NMemoryUnit.TERA_BYTE,
                        KB * get(NMemoryUnit.PETA_BYTE)
                                + KB * KB * get(NMemoryUnit.ZETA_BYTE)
                );
              /**
               * Sets the set.
               *
               * @param NMemoryUnit.PETA_BYTE n memory unit.peta_byte
               * @param 0 0
               */
                set(NMemoryUnit.PETA_BYTE, 0);
              /**
               * Sets the set.
               *
               * @param NMemoryUnit.ZETA_BYTE n memory unit.zeta_byte
               * @param 0 0
               */
                set(NMemoryUnit.ZETA_BYTE, 0);
                break;
            }
            case GIGA_BYTE: {
                add(NMemoryUnit.TERA_BYTE,
                        KB * get(NMemoryUnit.TERA_BYTE)
                                + KB * KB * get(NMemoryUnit.PETA_BYTE)
                                + KB * KB * KB * get(NMemoryUnit.ZETA_BYTE)
                );
              /**
               * Sets the set.
               *
               * @param NMemoryUnit.TERA_BYTE n memory unit.tera_byte
               * @param 0 0
               */
                set(NMemoryUnit.TERA_BYTE, 0);
              /**
               * Sets the set.
               *
               * @param NMemoryUnit.PETA_BYTE n memory unit.peta_byte
               * @param 0 0
               */
                set(NMemoryUnit.PETA_BYTE, 0);
              /**
               * Sets the set.
               *
               * @param NMemoryUnit.ZETA_BYTE n memory unit.zeta_byte
               * @param 0 0
               */
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
              /**
               * Sets the set.
               *
               * @param NMemoryUnit.GIGA_BYTE n memory unit.giga_byte
               * @param 0 0
               */
                set(NMemoryUnit.GIGA_BYTE, 0);
              /**
               * Sets the set.
               *
               * @param NMemoryUnit.TERA_BYTE n memory unit.tera_byte
               * @param 0 0
               */
                set(NMemoryUnit.TERA_BYTE, 0);
              /**
               * Sets the set.
               *
               * @param NMemoryUnit.ZETA_BYTE n memory unit.zeta_byte
               * @param 0 0
               */
                set(NMemoryUnit.ZETA_BYTE, 0);
              /**
               * Sets the set.
               *
               * @param NMemoryUnit.PETA_BYTE n memory unit.peta_byte
               * @param 0 0
               */
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
              /**
               * Sets the set.
               *
               * @param NMemoryUnit.MEGA_BYTE n memory unit.mega_byte
               * @param 0 0
               */
                set(NMemoryUnit.MEGA_BYTE, 0);
              /**
               * Sets the set.
               *
               * @param NMemoryUnit.GIGA_BYTE n memory unit.giga_byte
               * @param 0 0
               */
                set(NMemoryUnit.GIGA_BYTE, 0);
              /**
               * Sets the set.
               *
               * @param NMemoryUnit.TERA_BYTE n memory unit.tera_byte
               * @param 0 0
               */
                set(NMemoryUnit.TERA_BYTE, 0);
              /**
               * Sets the set.
               *
               * @param NMemoryUnit.ZETA_BYTE n memory unit.zeta_byte
               * @param 0 0
               */
                set(NMemoryUnit.ZETA_BYTE, 0);
              /**
               * Sets the set.
               *
               * @param NMemoryUnit.PETA_BYTE n memory unit.peta_byte
               * @param 0 0
               */
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
              /**
               * Sets the set.
               *
               * @param NMemoryUnit.KILO_BYTE n memory unit.kilo_byte
               * @param 0 0
               */
                set(NMemoryUnit.KILO_BYTE, 0);
              /**
               * Sets the set.
               *
               * @param NMemoryUnit.MEGA_BYTE n memory unit.mega_byte
               * @param 0 0
               */
                set(NMemoryUnit.MEGA_BYTE, 0);
              /**
               * Sets the set.
               *
               * @param NMemoryUnit.GIGA_BYTE n memory unit.giga_byte
               * @param 0 0
               */
                set(NMemoryUnit.GIGA_BYTE, 0);
              /**
               * Sets the set.
               *
               * @param NMemoryUnit.TERA_BYTE n memory unit.tera_byte
               * @param 0 0
               */
                set(NMemoryUnit.TERA_BYTE, 0);
              /**
               * Sets the set.
               *
               * @param NMemoryUnit.ZETA_BYTE n memory unit.zeta_byte
               * @param 0 0
               */
                set(NMemoryUnit.ZETA_BYTE, 0);
              /**
               * Sets the set.
               *
               * @param NMemoryUnit.PETA_BYTE n memory unit.peta_byte
               * @param 0 0
               */
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
              /**
               * Sets the set.
               *
               * @param NMemoryUnit.BYTE n memory unit.byte
               * @param 0 0
               */
                set(NMemoryUnit.BYTE, 0);
              /**
               * Sets the set.
               *
               * @param NMemoryUnit.KILO_BYTE n memory unit.kilo_byte
               * @param 0 0
               */
                set(NMemoryUnit.KILO_BYTE, 0);
              /**
               * Sets the set.
               *
               * @param NMemoryUnit.MEGA_BYTE n memory unit.mega_byte
               * @param 0 0
               */
                set(NMemoryUnit.MEGA_BYTE, 0);
              /**
               * Sets the set.
               *
               * @param NMemoryUnit.GIGA_BYTE n memory unit.giga_byte
               * @param 0 0
               */
                set(NMemoryUnit.GIGA_BYTE, 0);
              /**
               * Sets the set.
               *
               * @param NMemoryUnit.TERA_BYTE n memory unit.tera_byte
               * @param 0 0
               */
                set(NMemoryUnit.TERA_BYTE, 0);
              /**
               * Sets the set.
               *
               * @param NMemoryUnit.ZETA_BYTE n memory unit.zeta_byte
               * @param 0 0
               */
                set(NMemoryUnit.ZETA_BYTE, 0);
              /**
               * Sets the set.
               *
               * @param NMemoryUnit.PETA_BYTE n memory unit.peta_byte
               * @param 0 0
               */
                set(NMemoryUnit.PETA_BYTE, 0);
                break;
            }
        }
    }

    /**
     * Checks if is zero0.
     *
     * @return is zero0 result
     */
    private boolean isZero0() {
        for (long value : values) {
            if (value != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Detect smallest unit.
     *
     * @return detect smallest unit result
     */
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

    /**
     * Detect largest unit.
     *
     * @return detect largest unit result
     */
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


    /**
     * Creates a new instance of of bits.
     *
     * @param bits bits
     * @param iec iec
     * @return of bits result
     */
    public static NMemorySize ofBits(long bits, boolean iec) {
        long bytes = bits / 8;
        int b = (int) (bits % 8);
        return new NMemorySize(bytes, b, iec);
    }

    /**
     * Creates a new instance of of bits.
     *
     * @param bits bits
     * @param smallestUnit smallest unit
     * @param largestUnit largest unit
     * @return of bits result
     */
    public static NMemorySize ofBits(long bits, NMemoryUnit smallestUnit, NMemoryUnit largestUnit) {
        /**
         * Creates a new instance of of bits.
         *
         * @param bits bits
         * @param smallestUnit smallest unit
         * @param largestUnit largest unit
         * @param false false
         * @return of bits result
         */
        return ofBits(bits, smallestUnit, largestUnit, false);
    }

    /**
     * Creates a new instance of of bits.
     *
     * @param bits bits
     * @param smallestUnit smallest unit
     * @param largestUnit largest unit
     * @param iec iec
     * @return of bits result
     */
    public static NMemorySize ofBits(long bits, NMemoryUnit smallestUnit, NMemoryUnit largestUnit, boolean iec) {
        long bytes = bits / 8;
        int b = (int) (bits % 8);
        return new NMemorySize(bytes, b, smallestUnit, largestUnit, iec);
    }

    /**
     * Creates a new instance of of bits only.
     *
     * @param value value
     * @return of bits only result
     */
    public static NMemorySize ofBitsOnly(long value) {
        /**
         * Creates a new instance of of bits only.
         *
         * @param value value
         * @param false false
         * @return of bits only result
         */
        return ofBitsOnly(value, false);
    }

    /**
     * Creates a new instance of of bytes only.
     *
     * @param value value
     * @return of bytes only result
     */
    public static NMemorySize ofBytesOnly(long value) {
        /**
         * Creates a new instance of of bytes only.
         *
         * @param value value
         * @param false false
         * @return of bytes only result
         */
        return ofBytesOnly(value, false);
    }

    /**
     * Creates a new instance of of kilo bytes only.
     *
     * @param value value
     * @return of kilo bytes only result
     */
    public static NMemorySize ofKiloBytesOnly(long value) {
        /**
         * Creates a new instance of of kilo bytes only.
         *
         * @param value value
         * @param false false
         * @return of kilo bytes only result
         */
        return ofKiloBytesOnly(value, false);
    }

    /**
     * Creates a new instance of of mega bytes only.
     *
     * @param value value
     * @return of mega bytes only result
     */
    public static NMemorySize ofMegaBytesOnly(long value) {
        /**
         * Creates a new instance of of mega bytes only.
         *
         * @param value value
         * @param false false
         * @return of mega bytes only result
         */
        return ofMegaBytesOnly(value, false);
    }

    /**
     * Creates a new instance of of tera bytes only.
     *
     * @param value value
     * @return of tera bytes only result
     */
    public static NMemorySize ofTeraBytesOnly(long value) {
        /**
         * Creates a new instance of of tera bytes only.
         *
         * @param value value
         * @param false false
         * @return of tera bytes only result
         */
        return ofTeraBytesOnly(value, false);
    }

    /**
     * Creates a new instance of of peta bytes only.
     *
     * @param value value
     * @return of peta bytes only result
     */
    public static NMemorySize ofPetaBytesOnly(long value) {
        /**
         * Creates a new instance of of peta bytes only.
         *
         * @param value value
         * @param false false
         * @return of peta bytes only result
         */
        return ofPetaBytesOnly(value, false);
    }

    /**
     * Creates a new instance of of zeta bytes only.
     *
     * @param value value
     * @return of zeta bytes only result
     */
    public static NMemorySize ofZetaBytesOnly(long value) {
        /**
         * Creates a new instance of of zeta bytes only.
         *
         * @param value value
         * @param false false
         * @return of zeta bytes only result
         */
        return ofZetaBytesOnly(value, false);
    }

    /**
     * Creates a new instance of of bits.
     *
     * @param value value
     * @return of bits result
     */
    public static NMemorySize ofBits(long value) {
        /**
         * Creates a new instance of of bits.
         *
         * @param value value
         * @param false false
         * @return of bits result
         */
        return ofBits(value, false);
    }

    /**
     * Creates a new instance of of bytes.
     *
     * @param value value
     * @return of bytes result
     */
    public static NMemorySize ofBytes(long value) {
        /**
         * Creates a new instance of of bytes.
         *
         * @param value value
         * @param false false
         * @return of bytes result
         */
        return ofBytes(value, false);
    }

    /**
     * Creates a new instance of of kilo bytes.
     *
     * @param value value
     * @return of kilo bytes result
     */
    public static NMemorySize ofKiloBytes(long value) {
        /**
         * Creates a new instance of of kilo bytes.
         *
         * @param value value
         * @param false false
         * @return of kilo bytes result
         */
        return ofKiloBytes(value, false);
    }

    /**
     * Creates a new instance of of mega bytes.
     *
     * @param value value
     * @return of mega bytes result
     */
    public static NMemorySize ofMegaBytes(long value) {
        /**
         * Creates a new instance of of mega bytes.
         *
         * @param value value
         * @param false false
         * @return of mega bytes result
         */
        return ofMegaBytes(value, false);
    }

    /**
     * Creates a new instance of of tera bytes.
     *
     * @param value value
     * @return of tera bytes result
     */
    public static NMemorySize ofTeraBytes(long value) {
        /**
         * Creates a new instance of of tera bytes.
         *
         * @param value value
         * @param false false
         * @return of tera bytes result
         */
        return ofTeraBytes(value, false);
    }

    /**
     * Creates a new instance of of peta bytes.
     *
     * @param value value
     * @return of peta bytes result
     */
    public static NMemorySize ofPetaBytes(long value) {
        /**
         * Creates a new instance of of peta bytes.
         *
         * @param value value
         * @param false false
         * @return of peta bytes result
         */
        return ofPetaBytes(value, false);
    }

    /**
     * Creates a new instance of of zeta bytes.
     *
     * @param value value
     * @return of zeta bytes result
     */
    public static NMemorySize ofZetaBytes(long value) {
        /**
         * Creates a new instance of of zeta bytes.
         *
         * @param value value
         * @param false false
         * @return of zeta bytes result
         */
        return ofZetaBytes(value, false);
    }

    /**
     * Creates a new instance of of bits only.
     *
     * @param value value
     * @param iec iec
     * @return of bits only result
     */
    public static NMemorySize ofBitsOnly(long value, boolean iec) {
        /**
         * Creates a new instance of of unit only.
         *
         * @param value value
         * @param NMemoryUnit.BIT n memory unit.bit
         * @param iec iec
         * @return of unit only result
         */
        return ofUnitOnly(value, NMemoryUnit.BIT, iec);
    }

    /**
     * Creates a new instance of of bytes only.
     *
     * @param value value
     * @param iec iec
     * @return of bytes only result
     */
    public static NMemorySize ofBytesOnly(long value, boolean iec) {
        /**
         * Creates a new instance of of unit only.
         *
         * @param value value
         * @param NMemoryUnit.BYTE n memory unit.byte
         * @param iec iec
         * @return of unit only result
         */
        return ofUnitOnly(value, NMemoryUnit.BYTE, iec);
    }

    /**
     * Creates a new instance of of kilo bytes only.
     *
     * @param value value
     * @param iec iec
     * @return of kilo bytes only result
     */
    public static NMemorySize ofKiloBytesOnly(long value, boolean iec) {
        /**
         * Creates a new instance of of unit only.
         *
         * @param value value
         * @param NMemoryUnit.KILO_BYTE n memory unit.kilo_byte
         * @param iec iec
         * @return of unit only result
         */
        return ofUnitOnly(value, NMemoryUnit.KILO_BYTE, iec);
    }

    /**
     * Creates a new instance of of mega bytes only.
     *
     * @param value value
     * @param iec iec
     * @return of mega bytes only result
     */
    public static NMemorySize ofMegaBytesOnly(long value, boolean iec) {
        /**
         * Creates a new instance of of unit only.
         *
         * @param value value
         * @param NMemoryUnit.MEGA_BYTE n memory unit.mega_byte
         * @param iec iec
         * @return of unit only result
         */
        return ofUnitOnly(value, NMemoryUnit.MEGA_BYTE, iec);
    }

    /**
     * Creates a new instance of of tera bytes only.
     *
     * @param value value
     * @param iec iec
     * @return of tera bytes only result
     */
    public static NMemorySize ofTeraBytesOnly(long value, boolean iec) {
        /**
         * Creates a new instance of of unit only.
         *
         * @param value value
         * @param NMemoryUnit.TERA_BYTE n memory unit.tera_byte
         * @param iec iec
         * @return of unit only result
         */
        return ofUnitOnly(value, NMemoryUnit.TERA_BYTE, iec);
    }

    /**
     * Creates a new instance of of peta bytes only.
     *
     * @param value value
     * @param iec iec
     * @return of peta bytes only result
     */
    public static NMemorySize ofPetaBytesOnly(long value, boolean iec) {
        /**
         * Creates a new instance of of unit only.
         *
         * @param value value
         * @param NMemoryUnit.PETA_BYTE n memory unit.peta_byte
         * @param iec iec
         * @return of unit only result
         */
        return ofUnitOnly(value, NMemoryUnit.PETA_BYTE, iec);
    }

    /**
     * Creates a new instance of of zeta bytes only.
     *
     * @param value value
     * @param iec iec
     * @return of zeta bytes only result
     */
    public static NMemorySize ofZetaBytesOnly(long value, boolean iec) {
        /**
         * Creates a new instance of of unit only.
         *
         * @param value value
         * @param NMemoryUnit.ZETA_BYTE n memory unit.zeta_byte
         * @param iec iec
         * @return of unit only result
         */
        return ofUnitOnly(value, NMemoryUnit.ZETA_BYTE, iec);
    }

    /**
     * Creates a new instance of of bytes.
     *
     * @param value value
     * @param iec iec
     * @return of bytes result
     */
    public static NMemorySize ofBytes(long value, boolean iec) {
        /**
         * Creates a new instance of of unit.
         *
         * @param value value
         * @param NMemoryUnit.BYTE n memory unit.byte
         * @param iec iec
         * @return of unit result
         */
        return ofUnit(value, NMemoryUnit.BYTE, iec);
    }

    /**
     * Creates a new instance of of kilo bytes.
     *
     * @param value value
     * @param iec iec
     * @return of kilo bytes result
     */
    public static NMemorySize ofKiloBytes(long value, boolean iec) {
        /**
         * Creates a new instance of of unit.
         *
         * @param value value
         * @param NMemoryUnit.KILO_BYTE n memory unit.kilo_byte
         * @param iec iec
         * @return of unit result
         */
        return ofUnit(value, NMemoryUnit.KILO_BYTE, iec);
    }

    /**
     * Creates a new instance of of mega bytes.
     *
     * @param value value
     * @param iec iec
     * @return of mega bytes result
     */
    public static NMemorySize ofMegaBytes(long value, boolean iec) {
        /**
         * Creates a new instance of of unit.
         *
         * @param value value
         * @param NMemoryUnit.MEGA_BYTE n memory unit.mega_byte
         * @param iec iec
         * @return of unit result
         */
        return ofUnit(value, NMemoryUnit.MEGA_BYTE, iec);
    }

    /**
     * Creates a new instance of of tera bytes.
     *
     * @param value value
     * @param iec iec
     * @return of tera bytes result
     */
    public static NMemorySize ofTeraBytes(long value, boolean iec) {
        /**
         * Creates a new instance of of unit.
         *
         * @param value value
         * @param NMemoryUnit.TERA_BYTE n memory unit.tera_byte
         * @param iec iec
         * @return of unit result
         */
        return ofUnit(value, NMemoryUnit.TERA_BYTE, iec);
    }

    /**
     * Creates a new instance of of peta bytes.
     *
     * @param value value
     * @param iec iec
     * @return of peta bytes result
     */
    public static NMemorySize ofPetaBytes(long value, boolean iec) {
        /**
         * Creates a new instance of of unit.
         *
         * @param value value
         * @param NMemoryUnit.PETA_BYTE n memory unit.peta_byte
         * @param iec iec
         * @return of unit result
         */
        return ofUnit(value, NMemoryUnit.PETA_BYTE, iec);
    }

    /**
     * Creates a new instance of of zeta bytes.
     *
     * @param value value
     * @param iec iec
     * @return of zeta bytes result
     */
    public static NMemorySize ofZetaBytes(long value, boolean iec) {
        /**
         * Creates a new instance of of unit.
         *
         * @param value value
         * @param NMemoryUnit.ZETA_BYTE n memory unit.zeta_byte
         * @param iec iec
         * @return of unit result
         */
        return ofUnit(value, NMemoryUnit.ZETA_BYTE, iec);
    }

    /**
     * Creates a new instance of of unit only.
     *
     * @param value value
     * @param unit unit
     * @param iec iec
     * @return of unit only result
     */
    public static NMemorySize ofUnitOnly(long value, NMemoryUnit unit, boolean iec) {
        long[] values = new long[NMemoryUnit.values().length];
        values[unit.ordinal()] = value;
        return new NMemorySize(values, null, null, iec);
    }

    /**
     * Creates a new instance of of unit.
     *
     * @param valueInUnit value in unit
     * @param unit unit
     * @param iec iec
     * @return of unit result
     */
    public static NMemorySize ofUnit(long valueInUnit, NMemoryUnit unit, boolean iec) {
        /**
         * Creates a new instance of of unit only.
         *
         * @param valueInUnit value in unit
         * @param unit unit
         * @param iec).canonicalize( iec).canonicalize(
         * @return of unit only result
         */
        return ofUnitOnly(valueInUnit, unit, iec).canonicalize();
    }

    /**
     * Creates a new instance of of bytes.
     *
     * @param durationMillis duration millis
     * @param smallestUnit smallest unit
     * @param largestUnit largest unit
     * @param iec iec
     * @return of bytes result
     */
    public static NMemorySize ofBytes(long durationMillis, NMemoryUnit smallestUnit, NMemoryUnit largestUnit, boolean iec) {
        return new NMemorySize(durationMillis, 0, smallestUnit, largestUnit, iec);
    }


    /**
     * Creates a new instance of of bytes and bits.
     *
     * @param bytes bytes
     * @param bits bits
     * @param iec iec
     * @return of bytes and bits result
     */
    public static NMemorySize ofBytesAndBits(long bytes, int bits, boolean iec) {
        return new NMemorySize(bytes, bits, iec);
    }

    /**
     * Creates a new instance of of.
     *
     * @param values values
     * @param smallestUnit smallest unit
     * @param largestUnit largest unit
     * @param iec iec
     * @return of result
     */
    public static NMemorySize of(long[] values, NMemoryUnit smallestUnit, NMemoryUnit largestUnit, boolean iec) {
        return new NMemorySize(values, smallestUnit, largestUnit, iec);
    }

    /**
     * Creates a new instance of of.
     *
     * @param values values
     * @param iec iec
     * @return of result
     */
    public static NMemorySize of(long[] values, boolean iec) {
        /**
         * Creates a new instance of of.
         *
         * @param values values
         * @param null null
         * @param null null
         * @param iec iec
         * @return of result
         */
        return of(values, null, null, iec);
    }


    /**
     * First non zero up.
     *
     * @param unit unit
     * @return first non zero up result
     */
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

    /**
     * First non zero down.
     *
     * @param unit unit
     * @return first non zero down result
     */
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

    /**
     * Checks if is zero.
     *
     * @param fromIclusive from iclusive
     * @param toInclusive to inclusive
     * @return is zero result
     */
    public boolean isZero(NMemoryUnit fromIclusive, NMemoryUnit toInclusive) {
        for (int i = fromIclusive.ordinal(); i <= toInclusive.ordinal(); i++) {
            if (get(NMemoryUnit.values()[i]) != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if is zero down.
     *
     * @param unit unit
     * @return is zero down result
     */
    public boolean isZeroDown(NMemoryUnit unit) {
        /**
         * Checks if is zero.
         *
         * @param NMemoryUnit.BIT n memory unit.bit
         * @param unit unit
         * @return is zero result
         */
        return isZero(NMemoryUnit.BIT, unit);
    }

    /**
     * Checks if is zero up.
     *
     * @param unit unit
     * @return is zero up result
     */
    public boolean isZeroUp(NMemoryUnit unit) {
        /**
         * Checks if is zero.
         *
         * @param unit unit
         * @param NMemoryUnit.TERA_BYTE n memory unit.tera_byte
         * @return is zero result
         */
        return isZero(unit, NMemoryUnit.TERA_BYTE);
    }

    /**
     * Returns the as.
     *
     * @param unit unit
     * @return get as result
     */
    public long getAs(NMemoryUnit unit) {
        switch (unit) {
            case ZETA_BYTE:
                /**
                 * As zeta bytes.
                 *
                 * @return as zeta bytes result
                 */
                return asZetaBytes();
            case PETA_BYTE:
                /**
                 * As peta bytes.
                 *
                 * @return as peta bytes result
                 */
                return asPetaBytes();
            case TERA_BYTE:
                /**
                 * As tera bytes.
                 *
                 * @return as tera bytes result
                 */
                return asTeraBytes();
            case MEGA_BYTE:
                /**
                 * As mega bytes.
                 *
                 * @return as mega bytes result
                 */
                return asMegaBytes();
            case KILO_BYTE:
                /**
                 * Ass kilo bytes.
                 *
                 * @return ass kilo bytes result
                 */
                return assKiloBytes();
            case BYTE:
                /**
                 * As bytes.
                 *
                 * @return as bytes result
                 */
                return asBytes();
            case BIT:
                /**
                 * As bits.
                 *
                 * @return as bits result
                 */
                return asBits();
        }
        return 0;
    }

    /**
     * Adds add.
     *
     * @param unit unit
     * @param value value
     * @return add result
     */
    private void add(NMemoryUnit unit, long value) {
        this.values[unit.ordinal()] += value;
    }

    /**
     * Sets the set.
     *
     * @param unit unit
     * @param value value
     * @return set result
     */
    private void set(NMemoryUnit unit, long value) {
        this.values[unit.ordinal()] = value;
    }

    /**
     * Returns the get.
     *
     * @param unit unit
     * @return get result
     */
    public long get(NMemoryUnit unit) {
        return values[unit.ordinal()];
    }

    /**
     * Bits.
     *
     * @return bits result
     */
    public long bits() {
        /**
         * Returns the get.
         *
         * @param NMemoryUnit.BIT n memory unit.bit
         * @return get result
         */
        return get(NMemoryUnit.BIT);
    }

    /**
     * Bytes.
     *
     * @return bytes result
     */
    public long bytes() {
        /**
         * Returns the get.
         *
         * @param NMemoryUnit.BYTE n memory unit.byte
         * @return get result
         */
        return get(NMemoryUnit.BYTE);
    }

    /**
     * Kilo bytes.
     *
     * @return kilo bytes result
     */
    public long kiloBytes() {
        /**
         * Returns the get.
         *
         * @param NMemoryUnit.KILO_BYTE n memory unit.kilo_byte
         * @return get result
         */
        return get(NMemoryUnit.KILO_BYTE);
    }

    /**
     * Mega bytes.
     *
     * @return mega bytes result
     */
    public long megaBytes() {
        /**
         * Returns the get.
         *
         * @param NMemoryUnit.MEGA_BYTE n memory unit.mega_byte
         * @return get result
         */
        return get(NMemoryUnit.MEGA_BYTE);
    }

    /**
     * Tera bytes.
     *
     * @return tera bytes result
     */
    public long teraBytes() {
        /**
         * Returns the get.
         *
         * @param NMemoryUnit.TERA_BYTE n memory unit.tera_byte
         * @return get result
         */
        return get(NMemoryUnit.TERA_BYTE);
    }

    /**
     * Peta bytes.
     *
     * @return peta bytes result
     */
    public long petaBytes() {
        /**
         * Returns the get.
         *
         * @param NMemoryUnit.PETA_BYTE n memory unit.peta_byte
         * @return get result
         */
        return get(NMemoryUnit.PETA_BYTE);
    }

    /**
     * Zeta bytes.
     *
     * @return zeta bytes result
     */
    public long zetaBytes() {
        /**
         * Returns the get.
         *
         * @param NMemoryUnit.ZETA_BYTE n memory unit.zeta_byte
         * @return get result
         */
        return get(NMemoryUnit.ZETA_BYTE);
    }

    /**
     * Largest unit.
     *
     * @return largest unit result
     */
    public NMemoryUnit largestUnit() {
        return largestUnit;
    }

    /**
     * Smallest unit.
     *
     * @return smallest unit result
     */
    public NMemoryUnit smallestUnit() {
        return smallestUnit;
    }

    /**
     * As zeta bytes.
     *
     * @return as zeta bytes result
     */
    public long asZetaBytes() {
        return bytes / (KB * KB * KB * KB * KB);
    }

    /**
     * As peta bytes.
     *
     * @return as peta bytes result
     */
    public long asPetaBytes() {
        return bytes / (KB * KB * KB * KB);
    }

    /**
     * As tera bytes.
     *
     * @return as tera bytes result
     */
    public long asTeraBytes() {
        return bytes / (KB * KB * KB);
    }

    /**
     * As mega bytes.
     *
     * @return as mega bytes result
     */
    public long asMegaBytes() {
        return bytes / (KB * KB);
    }

    /**
     * Ass kilo bytes.
     *
     * @return ass kilo bytes result
     */
    public long assKiloBytes() {
        return bytes / KB;
    }

    /**
     * As bytes.
     *
     * @return as bytes result
     */
    public long asBytes() {
        return bytes;
    }

    /**
     * As bits.
     *
     * @return as bits result
     */
    public long asBits() {
        return bytes * 8 + bits;
    }

    /**
     * Memory bytes.
     *
     * @return memory bytes result
     */
    public long memoryBytes() {
        return bytes;
    }

    /**
     * Memory bits.
     *
     * @return memory bits result
     */
    public int memoryBits() {
        return bits;
    }


    /**
     * Iec.
     *
     * @return iec result
     */
    public boolean iec() {
        return iec;
    }

    /**
     * With iec.
     *
     * @param iec iec
     * @return with iec result
     */
    public NMemorySize withIEC(boolean iec) {
        if (this.iec == iec) {
            return this;
        }
        return new NMemorySize(toUnitsArray(), smallestUnit, largestUnit, iec);
    }

    /**
     * With smallest unit.
     *
     * @param smallestUnit smallest unit
     * @return with smallest unit result
     */
    public NMemorySize withSmallestUnit(NMemoryUnit smallestUnit) {
        if (smallestUnit == smallestUnit()) {
            return this;
        }
        NMemorySize d = new NMemorySize(toUnitsArray(), smallestUnit, largestUnit, iec);
//        if (this.bytes != d.bytes || this.bits != d.bits) {
//            throw new IllegalArgumentException("unexpected");
//        }
        return d.canonicalize();
    }

    /**
     * Normalize.
     *
     * @return normalize result
     */
    public NMemorySize normalize() {
        /**
         * With units.
         *
         * @param NMemoryUnit.BIT n memory unit.bit
         * @param NMemoryUnit.ZETA_BYTE).canonicalize( n memory unit.zeta_byte).canonicalize(
         * @return with units result
         */
        return withUnits(NMemoryUnit.BIT,NMemoryUnit.ZETA_BYTE).canonicalize();
    }

    /**
     * With largest unit.
     *
     * @return with largest unit result
     */
    public NMemorySize withLargestUnit() {
        /**
         * With largest unit.
         *
         * @param NMemoryUnit.ZETA_BYTE n memory unit.zeta_byte
         * @return with largest unit result
         */
        return withLargestUnit(NMemoryUnit.ZETA_BYTE);
    }
    /**
     * With smallest unit.
     *
     * @return with smallest unit result
     */
    public NMemorySize withSmallestUnit() {
        /**
         * With smallest unit.
         *
         * @param NMemoryUnit.BIT n memory unit.bit
         * @return with smallest unit result
         */
        return withSmallestUnit(NMemoryUnit.BIT);
    }
    /**
     * With largest unit.
     *
     * @param largestUnit largest unit
     * @return with largest unit result
     */
    public NMemorySize withLargestUnit(NMemoryUnit largestUnit) {
        if (largestUnit == largestUnit()) {
            return this;
        }
        NMemorySize d = new NMemorySize(toUnitsArray(), smallestUnit, largestUnit, iec);
//        if (this.bytes != d.bytes || this.bits != d.bits) {
//            throw new IllegalArgumentException("unexpected");
//        }
        return d.canonicalize();
    }

    /**
     * With units.
     *
     * @param smallestUnit smallest unit
     * @param largestUnit largest unit
     * @return with units result
     */
    public NMemorySize withUnits(NMemoryUnit smallestUnit, NMemoryUnit largestUnit) {
        if(smallestUnit==null){
            smallestUnit= smallestUnit();
        }
        if(largestUnit==null){
            largestUnit= largestUnit();
        }
        if (smallestUnit == smallestUnit() && largestUnit == largestUnit()) {
            return this;
        }
        NMemorySize d = new NMemorySize(toUnitsArray(), smallestUnit, largestUnit, iec);
        if (this.bytes != d.bytes || this.bits != d.bits) {
            /**
             * Illegal argument exception.
             *
             * @param "unexpected" "unexpected"
             * @return illegal argument exception result
             */
            throw new IllegalArgumentException("unexpected");
        }
        return d.canonicalize();
    }

    /**
     * Normalize negative unit.
     *
     * @param values values
     * @param curr curr
     * @param next next
     * @param multiplier multiplier
     * @return normalize negative unit result
     */
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

    /**
     * Neg.
     *
     * @return neg result
     */
    public NMemorySize neg() {
        long[] a = toUnitsArray();
        for (int i = 0; i < a.length; i++) {
            a[i] = -a[i];
        }
        /**
         * Creates a new instance of of.
         *
         * @param a a
         * @param smallestUnit smallest unit
         * @param largestUnit largest unit
         * @param iec iec
         * @return of result
         */
        return of(a, smallestUnit, largestUnit, iec);
    }

    /**
     * Adds add.
     *
     * @param other other
     * @return add result
     */
    public NMemorySize add(NMemorySize other) {
        long[] a = toUnitsArray();
        long[] b = other.toUnitsArray();
        for (int i = 0; i < a.length; i++) {
            a[i] += b[i];
        }
        return of(a,
                smallestUnit.compareTo(other.smallestUnit()) < 0 ? smallestUnit : other.smallestUnit,
                largestUnit.compareTo(other.smallestUnit()) > 0 ? largestUnit : other.smallestUnit
                , iec
        );
    }

    /**
     * Mul.
     *
     * @param other other
     * @return mul result
     */
    public NMemorySize mul(double other) {
        double ms = bytes * other;
        long msL = (long) (bytes * other);
        long ns = (long) (bits * other + (ms - msL) * 8);
        /**
         * Creates a new instance of of bytes and bits.
         *
         * @param msL ms l
         * @param ns ns
         * @param iec).withUnits(smallestUnit iec).with units(smallest unit
         * @param largestUnit largest unit
         * @return of bytes and bits result
         */
        return ofBytesAndBits(msL, (int) ns, iec).withUnits(smallestUnit, largestUnit);
    }

    /**
     * Mul.
     *
     * @param other other
     * @return mul result
     */
    public NMemorySize mul(long other) {
        long[] a = toUnitsArray();
        for (int i = 0; i < a.length; i++) {
            a[i] *= other;
        }
        /**
         * Creates a new instance of of.
         *
         * @param a a
         * @param smallestUnit smallest unit
         * @param largestUnit largest unit
         * @param iec iec
         * @return of result
         */
        return of(a, smallestUnit, largestUnit, iec);
    }

    /**
     * Subtract.
     *
     * @param other other
     * @return subtract result
     */
    public NMemorySize subtract(NMemorySize other) {
        long[] a = toUnitsArray();
        long[] b = other.toUnitsArray();
        for (int i = 0; i < a.length; i++) {
            a[i] -= b[i];
        }
        return of(a,
                smallestUnit.compareTo(other.smallestUnit()) < 0 ? smallestUnit : other.smallestUnit,
                largestUnit.compareTo(other.smallestUnit()) > 0 ? largestUnit : other.smallestUnit
                , iec
        );
    }

    /**
     * Checks if canonicalize.
     *
     * @return canonicalize result
     */
    public NMemorySize canonicalize() {
        long[] values = toUnitsArray();
        NMemoryUnit[] mUnits = NMemoryUnit.values();
        for (int i = 0; i < mUnits.length - 1; i++) {
            NMemoryUnit value = mUnits[i];
            long mul = i == 0 ? 8 : KB;
            for (int j = i; j < mUnits.length; j++) {
                if (!normalizeNegativeUnit(values, value, mUnits[j], mul)) {
                    break;
                }
                mul *= KB;
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
            /**
             * Illegal argument exception.
             *
             * @param this this
             * @return illegal argument exception result
             */
            throw new IllegalArgumentException("unexpected " + d + "<>" + this);
        }
        return d;
    }

    /**
     * Checks if is zero.
     *
     * @return is zero result
     */
    public boolean isZero() {
      /**
       * Return.
       *
       * @param 0 0
       */
        return ((bytes | bits) == 0);
    }

    /**
     * Converts to units array.
     *
     * @return to units array result
     */
    public long[] toUnitsArray() {
        return Arrays.copyOf(values, values.length);
    }

    @Override
    public String toString() {
        return NMemoryFormat.DEFAULT.format(this);
    }

    /**
     * Returns string representation of memory size with fixed format and IEC settings.
     *
     * @param fixed fixed width flag
     * @param iec IEC binary prefix flag
     * @return formatted memory size string
     */
    public String toString(boolean fixed, Boolean iec) {
        return NMemoryFormat.of(fixed, iec).format(this);
    }

    /**
     * Returns string representation of memory size with fixed format setting.
     *
     * @param fixed fixed width flag
     * @return formatted memory size string
     */
    public String toString(boolean fixed) {
        return NMemoryFormat.of(fixed, null).format(this);
    }

    /**
     * Parse.
     *
     * @param value value
     * @param defaultUnit default unit
     * @return parse result
     */
    public static NOptional<NMemorySize> parse(String value, NMemoryUnit defaultUnit) {
        if (defaultUnit == null) {
            defaultUnit = NMemoryUnit.BYTE;
        }
        value = NStringUtils.stripToNull(value);
        if (value == null) {
            return NOptional.ofNull();
        }
        NStreamTokenizer st = new NStreamTokenizer(new StringReader(value));
        try {
            int r = st.nextToken();
            switch (r) {
                case NToken.TT_NUMBER:
                case NToken.TT_FLOAT:
                case NToken.TT_INT:
                case NToken.TT_LONG:
                case NToken.TT_BIG_DECIMAL:
                case NToken.TT_BIG_INT:
                case NToken.TT_DOUBLE: {
                    Number nval = st.nval;
                    StringBuilder sb = new StringBuilder();
                    while (true) {
                        r = st.nextToken();
                        if (r == StreamTokenizer.TT_EOF) {
                            break;
                        }else  if (r == StreamTokenizer.TT_WORD) {
                            sb.append(st.sval);
                        } else if (r == ' ') {
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
                    NMemoryUnit u = NMemoryUnit.parse(sb.toString()).orNull();
                    if(u!=null){
                        return NOptional.of(NMemorySize.ofUnit(nval.longValue(), u, false));
                    }
                    if(sb.toString().endsWith("i")){
                        u = NMemoryUnit.parse(sb.toString().substring(0,sb.length()-1)).orNull();
                        if(u!=null){
                            return NOptional.of(NMemorySize.ofUnit(nval.longValue(), u, true));
                        }
                    }
                    String finalValue2 = value;
                    return NOptional.ofError(() -> NMsg.ofC(
                            "erroneous memory size : %s",
                            finalValue2
                    ));
                }
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

    /**
     * Reduce to largest unit.
     *
     * @return reduce to largest unit result
     */
    public NMemorySize reduceToLargestUnit() {
        /**
         * With smallest unit.
         *
         * @param largestUnit() largest unit()
         * @return with smallest unit result
         */
        return withSmallestUnit(largestUnit());
    }

    /**
     * Reduce to smallest unit.
     *
     * @return reduce to smallest unit result
     */
    public NMemorySize reduceToSmallestUnit() {
        /**
         * With smallest unit.
         *
         * @param smallestUnit() smallest unit()
         * @return with smallest unit result
         */
        return withSmallestUnit(smallestUnit());
    }

    /**
     * Reduce to unit.
     *
     * @param unit unit
     * @return reduce to unit result
     */
    public NMemorySize reduceToUnit(NMemoryUnit unit) {
        if (unit == null) {
            return this;
        }
        return new NMemorySize(toUnitsArray(), unit, unit, iec);
    }
}
