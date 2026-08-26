package net.thevpc.nuts.time;

import net.thevpc.nuts.mon.NDurationFormatMode;
import net.thevpc.nuts.text.NI18n;
import net.thevpc.nuts.text.NPositionType;
import net.thevpc.nuts.io.NMemoryPrintStream;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NStringUtils;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

/**
 * DefaultNDurationFormat class.
 *
 * @author thevpc
 * @since 0.8.0
 */
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

    /**
     * Default n duration format.
     *
     * @param other other
     * @return default n duration format result
     */
    public DefaultNDurationFormat(DefaultNDurationFormat other) {
        this.mode = other.mode;
    }

    /**
     * Creates a new instance of.
     *
     * @param mode mode
     * @return of result
     */
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

    /**
     * Default n duration format.
     *
     * @param mode mode
     * @return default n duration format result
     */
    public DefaultNDurationFormat(NDurationFormatMode mode) {
        this.mode = mode == null ? NDurationFormatMode.DEFAULT : mode;
    }

    //    @Override
    /**
     * Format millis.
     *
     * @param millis millis
     * @return format millis result
     */
    public String formatMillis(long millis) {
        /**
         * Format.
         *
         * @param millis millis
         * @param 0 0
         * @return format result
         */
        return format(millis, 0);
    }


    /**
     * Format nanos.
     *
     * @param nanos nanos
     * @return format nanos result
     */
    public String formatNanos(long nanos) {
        /**
         * Format.
         *
         * @param 1000000 1000000
         * @param 1000000) 1000000)
         * @return format result
         */
        return format(nanos / 1000000, (int) (nanos % 1000000));
    }

    //    @Override
    /**
     * Format.
     *
     * @param millis millis
     * @param nanos nanos
     * @return format result
     */
    public String format(long millis, int nanos) {
        if (millis < 0) {
            /**
             * Illegal argument exception.
             *
             * @param millis).toString() millis).to string()
             * @return illegal argument exception result
             */
            throw new IllegalArgumentException(NMsg.ofC(NI18n.of("invalid millis %s"), millis).toString());
        }
        if (nanos < 0 || nanos > 999999) {
            /**
             * Illegal argument exception.
             *
             * @param millis).toString() millis).to string()
             * @return illegal argument exception result
             */
            throw new IllegalArgumentException(NMsg.ofC(NI18n.of("invalid nanos %s"), millis).toString());
        }
        /**
         * Format.
         *
         * @param nanos) nanos)
         * @return format result
         */
        return format(NDuration.ofMillisAndNanos(millis, nanos));
    }

    /**
     * Format.
     *
     * @param duration duration
     * @return format result
     */
    public String format(Duration duration) {
        /**
         * Format.
         *
         * @param NDuration.ofDuration(duration) n duration.of duration(duration)
         * @return format result
         */
        return format(NDuration.ofDuration(duration));
    }

    /**
     * Format unit.
     *
     * @param duration duration
     * @param unit unit
     * @param processed processed
     * @param out out
     */
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
        if (uordinal > duration.largestUnit().ordinal()
                || uordinal < duration.smallestUnit().ordinal()
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

    /**
     * Accept.
     *
     * @param c c
     * @param duration duration
     * @return accept result
     */
    private boolean accept(ChronoUnit c, NDuration duration) {
        if (c.ordinal() < duration.smallestUnit().ordinal()) {
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

    /**
     * Format.
     *
     * @param duration duration
     * @return format result
     */
    public String format(NDuration duration) {
        NMemoryPrintStream sb = NPrintStream.ofMem(NTerminalMode.FILTERED);
      /**
       * Print.
       *
       * @param duration duration
       * @param sb sb
       */
        print(duration, sb);
        return sb.toString();
    }

    /**
     * Print.
     *
     * @param duration duration
     * @param out out
     */
    public void print(NDuration duration, NPrintStream out) {
        HashSet<ChronoUnit> processed = new HashSet<>();
        for (ChronoUnit chronoUnit : new ChronoUnit[]{
                ChronoUnit.YEARS, ChronoUnit.MONTHS, ChronoUnit.WEEKS, ChronoUnit.DAYS,
                ChronoUnit.HOURS, ChronoUnit.MINUTES, ChronoUnit.SECONDS, ChronoUnit.MILLIS,
                ChronoUnit.MICROS, ChronoUnit.NANOS
        }) {
          /**
           * Format unit.
           *
           * @param duration duration
           * @param chronoUnit chrono unit
           * @param processed processed
           * @param out out
           */
            formatUnit(duration, chronoUnit, processed, out);
        }
        if (processed.isEmpty()) {
            out.print(formatNumber(0, duration.smallestUnit()), NTextStyle.number());
            out.print(unitString(duration.smallestUnit()), NTextStyle.info());
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


    /**
     * Size of.
     *
     * @param unit unit
     * @return size of result
     */
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

    /**
     * Format number.
     *
     * @param number number
     * @param unit unit
     * @return format number result
     */
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
        /**
         * Illegal argument exception.
         *
         * @param "unsupported" "unsupported"
         * @return illegal argument exception result
         */
        throw new IllegalArgumentException("unsupported");
    }

    /**
     * Copy.
     *
     * @return copy result
     */
    public DefaultNDurationFormat copy() {
        return new DefaultNDurationFormat(this);
    }

    /**
     * Freeze.
     *
     * @return freeze result
     */
    public DefaultNDurationFormat freeze() {
        if (!frozen) {
            this.frozen = true;
        }
        return this;
    }

    /**
     * Mode.
     *
     * @return mode result
     */
    public NDurationFormatMode mode() {
        return mode;
    }

    /**
     * Mode.
     *
     * @param mode mode
     * @return mode result
     */
    public DefaultNDurationFormat mode(NDurationFormatMode mode) {
        if (this.mode != mode) {
            if (frozen) {
                /**
                 * Illegal argument exception.
                 *
                 * @param updated") updated")
                 * @return illegal argument exception result
                 */
                throw new IllegalArgumentException(NI18n.of("This instance is immutable and cannot be updated"));
            }
            this.mode = mode;
        }
        return this;
    }

    /**
     * Unit string.
     *
     * @param unit unit
     * @return unit string result
     */
    public String unitString(ChronoUnit unit) {
        switch (unit) {
            case YEARS:
              /**
               * Return.
               *
               * @param "y" "y"
               */
                return ("y");
            case MONTHS:
              /**
               * Return.
               *
               * @param "m" "m"
               */
                return ("m");
            case WEEKS:
              /**
               * Return.
               *
               * @param "w" "w"
               */
                return ("w");
            case DAYS:
              /**
               * Return.
               *
               * @param "d" "d"
               */
                return ("d");
            case HOURS:
              /**
               * Return.
               *
               * @param "h" "h"
               */
                return ("h");
            case MINUTES:
              /**
               * Return.
               *
               * @param "mn" "mn"
               */
                return ("mn");
            case SECONDS:
              /**
               * Return.
               *
               * @param "s" "s"
               */
                return ("s");
            case MILLIS:
              /**
               * Return.
               *
               * @param "ms" "ms"
               */
                return ("ms");
            case MICROS:
              /**
               * Return.
               *
               * @param "us" "us"
               */
                return ("us");
            case NANOS:
              /**
               * Return.
               *
               * @param "ns" "ns"
               */
                return ("ns");
        }
        return "";
    }
}
