package net.thevpc.nuts.util;

import java.util.*;

abstract class NColorBase implements NColor {
    private static final java.util.List<NColor> _ALL_REGISTERED = new ArrayList<>();
    private static final java.util.List<NColor> _ALL_CANONICAL = new ArrayList<>();
    private static final Map<String, NColor> _ALL_BY_NAME = new LinkedHashMap<>();
    private static final Map<String, List<NColor>> _ALL_BY_CANONICAL_NAME = new LinkedHashMap<>();
    public static final List<NColor> ALL = Collections.unmodifiableList(_ALL_REGISTERED);
    public static final List<NColor> ALL_CANONICAL = Collections.unmodifiableList(_ALL_CANONICAL);
    public static final Map<String, NColor> BY_NAME = Collections.unmodifiableMap(_ALL_BY_NAME);
    /**
     * ANSI COLORS (4 bits) as 32bits
     */
    public static java.util.List<NColor> ANSI_COLORS_16 = Collections.unmodifiableList(Arrays.asList(
            NColor.of32(0, 0, 0),         // 0: Black
            NColor.of32(128, 0, 0),       // 1: Red
            NColor.of32(0, 128, 0),       // 2: Green
            NColor.of32(128, 128, 0),     // 3: Yellow
            NColor.of32(0, 0, 128),       // 4: Blue
            NColor.of32(128, 0, 128),     // 5: Magenta
            NColor.of32(0, 128, 128),     // 6: Cyan
            NColor.of32(192, 192, 192),   // 7: White (light gray)
            NColor.of32(128, 128, 128),   // 8: Bright Black (dark gray)
            NColor.of32(255, 0, 0),       // 9: Bright Red
            NColor.of32(0, 255, 0),       //10: Bright Green
            NColor.of32(255, 255, 0),     //11: Bright Yellow
            NColor.of32(0, 0, 255),       //12: Bright Blue
            NColor.of32(255, 0, 255),     //13: Bright Magenta
            NColor.of32(0, 255, 255),     //14: Bright Cyan
            NColor.of32(255, 255, 255)    //15: Bright White
    ));
    public static final List<NColor> ANSI_COLORS_256;

    static {
        List<NColor> ansiColors = new ArrayList<>(256);
        ansiColors.addAll(ANSI_COLORS_16);
        // 16–231: 6×6×6 RGB cube
        int[] levels = {0, 95, 135, 175, 215, 255};
        int index = 16;
        for (int r = 0; r < 6; r++) {
            for (int g = 0; g < 6; g++) {
                for (int b = 0; b < 6; b++) {
                    ansiColors.add(NColor.of32(levels[r], levels[g], levels[b]));
                }
            }
        }
        // 232–255: Grayscale from 8 to 238 in steps of 10
        for (int i = 0; i < 24; i++) {
            int gray = 8 + i * 10;
            ansiColors.add(NColor.of32(gray, gray, gray));
        }
        ANSI_COLORS_256 = Collections.unmodifiableList(ansiColors);
    }


    /**
     * _reg2.
     *
     * @param canonicalName canonical name
     * @param color color
     * @return _reg2 result
     */
    static NColor _reg2(String canonicalName, NColor color) {
        String name = color.name();
        NAssert.requireNamedNonBlank(name, "color name");
        name = normalizeName(name);
        canonicalName = normalizeName(canonicalName);
        if (_ALL_BY_NAME.containsKey(name)) {
            /**
             * Illegal argument exception.
             *
             * @param name name
             * @return illegal argument exception result
             */
            throw new IllegalArgumentException("invalid duplicate name " + name);
        }
        _ALL_REGISTERED.add(color);
        _ALL_BY_NAME.put(name, color);
        _ALL_BY_CANONICAL_NAME.computeIfAbsent(canonicalName, v -> new ArrayList<>()).add(color);
        if (canonicalName.equals(name)) {
            _ALL_CANONICAL.add(color);
        }
        return color;
    }

    /**
     * Deterministic mapping from int → NColor.
     * <p>
     * Two-step mapping ensures colors are evenly chosen across canonical colors,
     * so grays (which are more numerous) don’t dominate the distribution.
     */
    static NColor pickColor(int hashCode) {
        int a = Math.abs(hashCode);
        NColor c = _ALL_CANONICAL.get(a % _ALL_CANONICAL.size());
        List<NColor> li = _ALL_BY_CANONICAL_NAME.get(normalizeName(c.name()));
        return li.get(a % li.size());
    }

    /**
     * _reg.
     *
     * @param name name
     * @param canonicalName canonical name
     * @param rgb rgb
     * @return _reg result
     */
    static NColor _reg(String name, String canonicalName, int rgb) {
        /**
         * _reg2.
         *
         * @param canonicalName canonical name
         * @param name) name)
         * @return _reg2 result
         */
        return _reg2(canonicalName, NColor.of32(rgb, name));
    }
    /**
     * _reg.
     *
     * @param name name
     * @param canonicalName canonical name
     * @param r r
     * @param g g
     * @param b b
     * @return _reg result
     */
    static NColor _reg(String name, String canonicalName, int r, int g, int b) {
        /**
         * _reg2.
         *
         * @param canonicalName canonical name
         * @param name) name)
         * @return _reg2 result
         */
        return _reg2(canonicalName, NColor.of32(r, g, b, name));
    }

    /**
     * _reg gray.
     *
     * @param percent percent
     * @return _reg gray result
     */
    static NColor _regGray(int percent) {
        int v = Math.round(percent * 255 / 100f);
        /**
         * _reg2.
         *
         * @param "Gray" " gray"
         * @param percent) percent)
         * @return _reg2 result
         */
        return _reg2("Gray", NColor.of32(v, v, v, "Gray" + percent));
    }

    /**
     * Creates a new instance of of4.
     *
     * @param color color
     * @return of4 result
     */
    public static NColor of4(int color) {
        return new NColorBase.NColor4(color);
    }

    /**
     * Normalize name.
     *
     * @param name name
     * @return normalize name result
     */
    private static String normalizeName(String name) {
        return name == null ? null : NNameFormat.CLASS_NAME.format(NStringUtils.strip(name)).toLowerCase();
    }


    /**
     * Creates a new instance of of name.
     *
     * @param name name
     * @return of name result
     */
    public static NOptional<NColor> ofName(String name) {
        return NOptional.ofNamed(BY_NAME.get(normalizeName(name)), "color " + name);
    }

    /**
     * Creates a new instance of of canonical name.
     *
     * @param name name
     * @return of canonical name result
     */
    public static NOptional<List<NColor>> ofCanonicalName(String name) {
        return NOptional.ofNamed(_ALL_BY_CANONICAL_NAME.get(normalizeName(name)), "color " + name);
    }

    /**
     * Converts to html hex.
     *
     * @param cl cl
     * @return to html hex result
     */
    public static String toHtmlHex(NColor cl) {
        return String.format("#%02X%02X%02X", cl.red(), cl.green(), cl.blue());
    }

    /**
     * Ansi to color.
     *
     * @param index index
     * @return ansi to color result
     */
    public static NColor ansiToColor(int index) {
        if (index < 0 || index > 255) {
            /**
             * Illegal argument exception.
             *
             * @param 255" 255"
             * @return illegal argument exception result
             */
            throw new IllegalArgumentException("ANSI color index must be between 0 and 255");
        }

        if (index < 16) {
            // Basic colors
            int[] basicColors = {
                    0x000000, 0x800000, 0x008000, 0x808000,
                    0x000080, 0x800080, 0x008080, 0xC0C0C0,
                    0x808080, 0xFF0000, 0x00FF00, 0xFFFF00,
                    0x0000FF, 0xFF00FF, 0x00FFFF, 0xFFFFFF
            };
            return NColor.of32(basicColors[index]);
        } else if (index < 232) {
            // 6x6x6 color cube
            int level = index - 16;
            int r = (level / 36) % 6 * 51;
            int g = (level / 6) % 6 * 51;
            int b = level % 6 * 51;
            return NColor.of32(r, g, b);
        } else {
            // Grayscale colors
            int gray = (index - 232) * 10 + 8;
            return NColor.of32(gray, gray, gray);
        }
    }


    /**
     * Creates a new instance of of4.
     *
     * @param color color
     * @param name name
     * @return of4 result
     */
    public static NColor of4(int color, String name) {
        name = NStringUtils.stripToNull(name);
        if (name == null) {
            /**
             * Creates a new instance of of4.
             *
             * @param color color
             * @return of4 result
             */
            return of4(color);
        }
        return new NColorBase.NColor4Named(color, name);
    }

    /**
     * Creates a new instance of of8.
     *
     * @param color color
     * @return of8 result
     */
    public static NColor of8(int color) {
        return new NColorBase.NColor8(color);
    }

    /**
     * Creates a new instance of of8.
     *
     * @param color color
     * @param name name
     * @return of8 result
     */
    public static NColor of8(int color, String name) {
        name = NStringUtils.stripToNull(name);
        if (name == null) {
            /**
             * Creates a new instance of of8.
             *
             * @param color color
             * @return of8 result
             */
            return of8(color);
        }
        return new NColorBase.NColor8Named(color, name);
    }

    /**
     * Creates a new instance of of16.
     *
     * @param color color
     * @return of16 result
     */
    public static NColor of16(int color) {
        return new NColorBase.NColor16(color);
    }

    /**
     * Creates a new instance of of16.
     *
     * @param color color
     * @param name name
     * @return of16 result
     */
    public static NColor of16(int color, String name) {
        name = NStringUtils.stripToNull(name);
        if (name == null) {
            /**
             * Creates a new instance of of16.
             *
             * @param color color
             * @return of16 result
             */
            return of16(color);
        }
        return new NColorBase.NColor16Named(color, name);
    }

    /**
     * Creates a new instance of of24.
     *
     * @param color color
     * @param name name
     * @return of24 result
     */
    public static NColor of24(int color, String name) {
        name = NStringUtils.stripToNull(name);
        if (name == null) {
            /**
             * Creates a new instance of of24.
             *
             * @param color color
             * @return of24 result
             */
            return of24(color);
        }
        return new NColorBase.NColor24Named(color, name);
    }

    /**
     * Creates a new instance of of24.
     *
     * @param color color
     * @return of24 result
     */
    public static NColor of24(int color) {
        return new NColorBase.NColor24(color);
    }

    /**
     * Creates a new instance of of32.
     *
     * @param r r
     * @param g g
     * @param b b
     * @param name name
     * @return of32 result
     */
    public static NColor of32(int r, int g, int b, String name) {
        name = NStringUtils.stripToNull(name);
        if (name == null) {
            /**
             * Creates a new instance of of32.
             *
             * @param r r
             * @param g g
             * @param b b
             * @return of32 result
             */
            return of32(r, g, b);
        }
        return new NColorBase.NColor32Named(r, g, b, name);
    }

    /**
     * Creates a new instance of of32.
     *
     * @param r r
     * @param g g
     * @param b b
     * @return of32 result
     */
    public static NColor of32(int r, int g, int b) {
        return new NColorBase.NColor32(r, g, b);
    }

    /**
     * Creates a new instance of of32.
     *
     * @param r r
     * @param g g
     * @param b b
     * @param a a
     * @return of32 result
     */
    public static NColor of32(int r, int g, int b, int a) {
        return new NColorBase.NColor32(r, g, b, a);
    }

    /**
     * Creates a new instance of of32.
     *
     * @param r r
     * @param g g
     * @param b b
     * @param a a
     * @param name name
     * @return of32 result
     */
    public static NColor of32(int r, int g, int b, int a, String name) {
        name = NStringUtils.stripToNull(name);
        if (name == null) {
            /**
             * Creates a new instance of of32.
             *
             * @param r r
             * @param g g
             * @param b b
             * @param a a
             * @return of32 result
             */
            return of32(r, g, b, a);
        }
        return new NColorBase.NColor32Named(r, g, b, a, name);
    }

    /**
     * Creates a new instance of of32.
     *
     * @param color color
     * @return of32 result
     */
    public static NColor of32(int color) {
        return new NColorBase.NColor32(color);
    }

    /**
     * Creates a new instance of of32.
     *
     * @param color color
     * @param name name
     * @return of32 result
     */
    public static NColor of32(int color, String name) {
        name = NStringUtils.stripToNull(name);
        if (name == null) {
            /**
             * Creates a new instance of of32.
             *
             * @param color color
             * @return of32 result
             */
            return of32(color);
        }
        return new NColorBase.NColor32Named(color,name);
    }

    /**
     * Creates a new instance of of64.
     *
     * @param color color
     * @return of64 result
     */
    public static NColor of64(long color) {
        return new NColorBase.NColor64(color);
    }

    /**
     * Creates a new instance of of64.
     *
     * @param color color
     * @param name name
     * @return of64 result
     */
    public static NColor of64(long color, String name) {
        name = NStringUtils.stripToNull(name);
        if (name == null) {
            /**
             * Creates a new instance of of64.
             *
             * @param color color
             * @return of64 result
             */
            return of64(color);
        }
        return new NColorBase.NColor64Named(color, name);
    }

    /**
     * Red.
     *
     * @return red result
     */
    public int red() {
        return this.rgb() >> 16 & 255;
    }

    /**
     * Green.
     *
     * @return green result
     */
    public int green() {
        return this.rgb() >> 8 & 255;
    }

    /**
     * Blue.
     *
     * @return blue result
     */
    public int blue() {
        return this.rgb() >> 0 & 255;
    }

    /**
     * Alpha.
     *
     * @return alpha result
     */
    public int alpha() {
        return this.rgb() >> 24 & 255;
    }

    static class NColor4 extends NColorBase {
        private final byte color;

        /**
         * N color4.
         *
         * @param color color
         * @return n color4 result
         */
        public NColor4(int color) {
            this.color = (byte) (color & 0xF);
        }

        @Override
        public NColor toColor32() {
            int c = (int) color;
            if (c >= 0 && c < 16) {
                return ANSI_COLORS_16.get(c);
            }
            return ANSI_COLORS_16.get(0);
        }

        /**
         * Bits.
         *
         * @return bits result
         */
        public NColorBits bits() {
            return NColorBits.BITS_4;
        }

        @Override
        public NColor withName(String name) {
            name = NStringUtils.stripToNull(name);
            if (name == null) {
                return this;
            }
            return NColor.of4(color, name);
        }

        @Override
        public int rgb() {
            /**
             * Converts to color32.
             *
             * @param ).rgb( ).rgb(
             * @return to color32 result
             */
            return toColor32().rgb();
        }

        @Override
        public int intColor() {
            return (int) color;
        }

        @Override
        public long longColor() {
            return (long) color;
        }

        /**
         * Name.
         *
         * @return name result
         */
        public String name() {
            return null;
        }
    }

    static class NColor4Named extends NColorBase {
        private final byte color;
        private final String name;

        /**
         * N color4 named.
         *
         * @param color color
         * @param name name
         * @return n color4 named result
         */
        public NColor4Named(int color, String name) {
            this.color = (byte) (color & 0xF);
            this.name = name;
        }

        @Override
        public NColor toColor32() {
            int c = (int) color;
            if (c >= 0 && c < 16) {
                return ANSI_COLORS_16.get(c).withName(name);
            }
            return ANSI_COLORS_16.get(0).withName(name);
        }

        /**
         * Bits.
         *
         * @return bits result
         */
        public NColorBits bits() {
            return NColorBits.BITS_4;
        }

        @Override
        public NColor withName(String name) {
            name = NStringUtils.stripToNull(name);
            if (name == null) {
                return NColor.of4(color);
            }
            if (name.equals(this.name)) {
                return this;
            }
            return NColor.of4(color, name);
        }

        @Override
        public int rgb() {
            /**
             * Converts to color32.
             *
             * @param ).rgb( ).rgb(
             * @return to color32 result
             */
            return toColor32().rgb();
        }

        @Override
        public int intColor() {
            return (int) color;
        }

        @Override
        public long longColor() {
            return (long) color;
        }

        /**
         * Name.
         *
         * @return name result
         */
        public String name() {
            return name;
        }
    }

    static class NColor8 extends NColorBase {
        private final short color;

        /**
         * N color8.
         *
         * @param color color
         * @return n color8 result
         */
        public NColor8(int color) {
            this.color = (short) (color & 0xFF);
        }

        @Override
        public NColor toColor32() {
            int c = (int) color;
            if (c >= 0 && c <= 255) {
                return ANSI_COLORS_256.get(c);
            }
            return ANSI_COLORS_256.get(0);
        }

        /**
         * Bits.
         *
         * @return bits result
         */
        public NColorBits bits() {
            return NColorBits.BITS_8;
        }

        @Override
        public NColor withName(String name) {
            name = NStringUtils.stripToNull(name);
            if (name == null) {
                return this;
            }
            return NColor.of8(color, name);
        }

        @Override
        public int rgb() {
            /**
             * Converts to color32.
             *
             * @param ).rgb( ).rgb(
             * @return to color32 result
             */
            return toColor32().rgb();
        }

        @Override
        public int intColor() {
            return (int) color;
        }

        @Override
        public long longColor() {
            return (long) color;
        }

        /**
         * Name.
         *
         * @return name result
         */
        public String name() {
            return null;
        }
    }

    static class NColor8Named extends NColorBase {
        private final short color;
        private final String name;

        /**
         * N color8 named.
         *
         * @param color color
         * @param name name
         * @return n color8 named result
         */
        public NColor8Named(int color, String name) {
            this.color = (short) (color & 0xFF);
            this.name = name;
        }

        @Override
        public NColor toColor32() {
            int c = (int) color;
            if (c >= 0 && c < 255) {
                return ANSI_COLORS_256.get(c).withName(name);
            }
            return ANSI_COLORS_256.get(0).withName(name);
        }

        /**
         * Bits.
         *
         * @return bits result
         */
        public NColorBits bits() {
            return NColorBits.BITS_8;
        }

        @Override
        public NColor withName(String name) {
            name = NStringUtils.stripToNull(name);
            if (name == null) {
                return NColor.of8(color);
            }
            if (name.equals(this.name)) {
                return this;
            }
            return NColor.of8(color, name);
        }

        @Override
        public int rgb() {
            /**
             * Converts to color32.
             *
             * @param ).rgb( ).rgb(
             * @return to color32 result
             */
            return toColor32().rgb();
        }

        @Override
        public int intColor() {
            return (int) color;
        }

        @Override
        public long longColor() {
            return (long) color;
        }

        /**
         * Name.
         *
         * @return name result
         */
        public String name() {
            return name;
        }
    }

    static class NColor16 extends NColorBase {
        private final int color;

        /**
         * N color16.
         *
         * @param color color
         * @return n color16 result
         */
        public NColor16(int color) {
            this.color = color & 0xFFFF;
        }

        @Override
        public NColor toColor32() {
            return NColor.of32(rgb());
        }


        /**
         * Bits.
         *
         * @return bits result
         */
        public NColorBits bits() {
            return NColorBits.BITS_16;
        }

        @Override
        public NColor withName(String name) {
            name = NStringUtils.stripToNull(name);
            if (name == null) {
                return this;
            }
            return NColor.of16(color, name);
        }

        @Override
        public int intColor() {
            return (int) color;
        }

        @Override
        public long longColor() {
            return (long) color;
        }

        @Override
        public int rgb() {
            int rgb565 = color & 0xFFFF;

            int r5 = (rgb565 >> 11) & 0x1F;
            int g6 = (rgb565 >> 5) & 0x3F;
            int b5 = rgb565 & 0x1F;

            int r8 = (r5 << 3) | (r5 >> 2);
            int g8 = (g6 << 2) | (g6 >> 4);
            int b8 = (b5 << 3) | (b5 >> 2);

            int argb32 = (255 << 24) | (r8 << 16) | (g8 << 8) | b8;
            return argb32;
        }

        /**
         * Name.
         *
         * @return name result
         */
        public String name() {
            return null;
        }
    }

    static class NColor16Named extends NColorBase {
        private final int color;
        private final String name;

        /**
         * N color16 named.
         *
         * @param color color
         * @param name name
         * @return n color16 named result
         */
        public NColor16Named(int color, String name) {
            this.color = color & 0xFFFF;
            this.name = name;
        }

        /**
         * Bits.
         *
         * @return bits result
         */
        public NColorBits bits() {
            return NColorBits.BITS_16;
        }

        @Override
        public int rgb() {
            int rgb565 = color & 0xFFFF;

            int r5 = (rgb565 >> 11) & 0x1F;
            int g6 = (rgb565 >> 5) & 0x3F;
            int b5 = rgb565 & 0x1F;

            int r8 = (r5 << 3) | (r5 >> 2);
            int g8 = (g6 << 2) | (g6 >> 4);
            int b8 = (b5 << 3) | (b5 >> 2);

            int argb32 = (255 << 24) | (r8 << 16) | (g8 << 8) | b8;
            return argb32;
        }

        @Override
        public NColor toColor32() {
            return NColor.of32(rgb(), name);
        }

        @Override
        public NColor withName(String name) {
            name = NStringUtils.stripToNull(name);
            if (name == null) {
                return NColor.of16(color);
            }
            if (name.equals(this.name)) {
                return this;
            }
            return NColor.of16(color, name);
        }

        @Override
        public int intColor() {
            return (int) color;
        }

        @Override
        public long longColor() {
            return (long) color;
        }

        /**
         * Name.
         *
         * @return name result
         */
        public String name() {
            return name;
        }
    }

    static class NColor24 extends NColorBase {
        private final int color;

        /**
         * N color24.
         *
         * @param color color
         * @return n color24 result
         */
        public NColor24(int color) {
            this.color = color & 0xFFFFFF;
        }

        @Override
        public NColor toColor32() {
            //just ignore alpha
            return NColor.of32(rgb());
        }

        @Override
        public int rgb() {
            // We take the 24-bit color and OR it with 255 shifted to the Alpha position
          /**
           * Return.
           *
           * @param 0xFFFFFF 0x ffffff
           */
            return (255 << 24) | (color & 0xFFFFFF);
        }

        /**
         * Bits.
         *
         * @return bits result
         */
        public NColorBits bits() {
            return NColorBits.BITS_24;
        }

        @Override
        public NColor withName(String name) {
            name = NStringUtils.stripToNull(name);
            if (name == null) {
                return this;
            }
            return NColor.of24(color, name);
        }

        @Override
        public int intColor() {
            return (int) color;
        }

        @Override
        public long longColor() {
            return (long) color;
        }


        /**
         * Name.
         *
         * @return name result
         */
        public String name() {
            return null;
        }
    }

    static class NColor24Named extends NColorBase {
        private final int color;
        private final String name;

        /**
         * N color24 named.
         *
         * @param color color
         * @param name name
         * @return n color24 named result
         */
        public NColor24Named(int color, String name) {
            this.color = color & 0xFFFFFF;
            this.name = name;
        }

        @Override
        public NColor toColor32() {
            //just ignore alpha
            return NColor.of32(rgb(), name);
        }

        @Override
        public int rgb() {
            // We take the 24-bit color and OR it with 255 shifted to the Alpha position
          /**
           * Return.
           *
           * @param 0xFFFFFF 0x ffffff
           */
            return (255 << 24) | (color & 0xFFFFFF);
        }

        /**
         * Bits.
         *
         * @return bits result
         */
        public NColorBits bits() {
            return NColorBits.BITS_24;
        }

        @Override
        public NColor withName(String name) {
            name = NStringUtils.stripToNull(name);
            if (name == null) {
                return NColor.of24(color);
            }
            if (name.equals(this.name)) {
                return this;
            }
            return NColor.of24(color, name);
        }

        @Override
        public int intColor() {
            return (int) color;
        }

        @Override
        public long longColor() {
            return (long) color;
        }

        /**
         * Name.
         *
         * @return name result
         */
        public String name() {
            return name;
        }
    }

    static class NColor32 extends NColorBase {
        private final int color;

        /**
         * N color32.
         *
         * @param color color
         * @return n color32 result
         */
        public NColor32(int color) {
            this.color = color;
        }

        /**
         * N color32.
         *
         * @param r r
         * @param g g
         * @param b b
         * @return n color32 result
         */
        public NColor32(int r, int g, int b) {
            this.color = (255 << 24) | (r & 255) << 16 | (g & 255) << 8 | (b & 255);
        }

        /**
         * N color32.
         *
         * @param r r
         * @param g g
         * @param b b
         * @param a a
         * @return n color32 result
         */
        public NColor32(int r, int g, int b, int a) {
            this.color = (a & 255) << 24 | (r & 255) << 16 | (g & 255) << 8 | (b & 255);
        }

        /**
         * Bits.
         *
         * @return bits result
         */
        public NColorBits bits() {
            return NColorBits.BITS_32;
        }

        @Override
        public NColor toColor32() {
            return this;
        }

        @Override
        public NColor withName(String name) {
            name = NStringUtils.stripToNull(name);
            if (name == null) {
                return this;
            }
            return NColor.of32(color, name);
        }

        @Override
        public int rgb() {
            return color;
        }

        @Override
        public int intColor() {
            return (int) color;
        }

        @Override
        public long longColor() {
            return (long) color;
        }

        /**
         * Name.
         *
         * @return name result
         */
        public String name() {
            return null;
        }
    }

    static class NColor32Named extends NColorBase {
        private final int color;
        private final String name;

        /**
         * N color32 named.
         *
         * @param color color
         * @param name name
         * @return n color32 named result
         */
        public NColor32Named(int color, String name) {
            this.color = color;
            this.name = name;
        }

        /**
         * N color32 named.
         *
         * @param r r
         * @param g g
         * @param b b
         * @param name name
         * @return n color32 named result
         */
        public NColor32Named(int r, int g, int b, String name) {
            this.color = (r & 255) << 16 | (g & 255) << 8 | (b & 255) << 0;
            this.name = name;
        }

        /**
         * N color32 named.
         *
         * @param r r
         * @param g g
         * @param b b
         * @param a a
         * @param name name
         * @return n color32 named result
         */
        public NColor32Named(int r, int g, int b, int a, String name) {
            this.color = (a & 255) << 24 | (r & 255) << 16 | (g & 255) << 8 | (b & 255) << 0;
            this.name = name;
        }

        /**
         * Bits.
         *
         * @return bits result
         */
        public NColorBits bits() {
            return NColorBits.BITS_32;
        }

        @Override
        public NColor toColor32() {
            return this;
        }

        @Override
        public NColor withName(String name) {
            name = NStringUtils.stripToNull(name);
            if (name == null) {
                return NColor.of32(color);
            }
            if (name.equals(this.name)) {
                return this;
            }
            return NColor.of32(color, name);
        }

        @Override
        public int rgb() {
            return color;
        }

        @Override
        public int intColor() {
            return (int) color;
        }

        @Override
        public long longColor() {
            return (long) color;
        }

        /**
         * Name.
         *
         * @return name result
         */
        public String name() {
            return name;
        }
    }

    static class NColor64 extends NColorBase {
        private final long color;

        /**
         * N color64.
         *
         * @param color color
         * @return n color64 result
         */
        public NColor64(long color) {
            this.color = color;
        }

        /**
         * Bits.
         *
         * @return bits result
         */
        public NColorBits bits() {
            return NColorBits.BITS_64;
        }

        @Override
        public NColor withName(String name) {
            name = NStringUtils.stripToNull(name);
            if (name == null) {
                return this;
            }
            return NColor.of64(color, name);
        }

        @Override
        public int intColor() {
            return (int) color;
        }

        @Override
        public long longColor() {
            return (long) color;
        }

        @Override
        public int rgb() {
            return
                    ((int) (color >> 56) & 0xFF) << 24 |
                            ((int) (color >> 40) & 0xFF) << 16 |
                            ((int) (color >> 24) & 0xFF) << 8 |
                            ((int) (color >> 8) & 0xFF);
        }

        @Override
        public NColor toColor32() {
            return NColor.of32(rgb());
        }

        /**
         * Name.
         *
         * @return name result
         */
        public String name() {
            return null;
        }
    }

    static class NColor64Named extends NColorBase {
        private final long color;
        private final String name;

        /**
         * N color64 named.
         *
         * @param color color
         * @param name name
         * @return n color64 named result
         */
        public NColor64Named(long color, String name) {
            this.color = color;
            this.name = name;
        }

        /**
         * Bits.
         *
         * @return bits result
         */
        public NColorBits bits() {
            return NColorBits.BITS_64;
        }

        @Override
        public NColor withName(String name) {
            name = NStringUtils.stripToNull(name);
            if (name == null) {
                return NColor.of64(color);
            }
            if (name.equals(this.name)) {
                return this;
            }
            return NColor.of64(color, name);
        }

        @Override
        public int intColor() {
            return (int) color;
        }

        @Override
        public long longColor() {
            return (long) color;
        }

        @Override
        public int rgb() {
            return
                    ((int) (color >> 56) & 0xFF) << 24 |
                            ((int) (color >> 40) & 0xFF) << 16 |
                            ((int) (color >> 24) & 0xFF) << 8 |
                            ((int) (color >> 8) & 0xFF);
        }

        @Override
        public NColor toColor32() {
            return NColor.of32(rgb(), name);
        }

        /**
         * Name.
         *
         * @return name result
         */
        public String name() {
            return name;
        }
    }

}
