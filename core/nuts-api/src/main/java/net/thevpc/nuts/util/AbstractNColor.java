package net.thevpc.nuts.util;

import java.util.*;

abstract class AbstractNColor implements NColor {
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


    static NColor _reg2(String canonicalName, NColor color) {
        String name = color.getName();
        NAssert.requireNamedNonBlank(name, "color name");
        name = normalizeName(name);
        canonicalName = normalizeName(canonicalName);
        if (_ALL_BY_NAME.containsKey(name)) {
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
        List<NColor> li = _ALL_BY_CANONICAL_NAME.get(normalizeName(c.getName()));
        return li.get(a % li.size());
    }

    static NColor _reg(String name, String canonicalName, int r, int g, int b) {
        return _reg2(canonicalName, NColor.of32(r, g, b, name));
    }

    static NColor _regGray(int percent) {
        int v = Math.round(percent * 255 / 100f);
        return _reg2("Gray", NColor.of32(v, v, v, "Gray" + percent));
    }

    public static NColor of4(int color) {
        return new AbstractNColor.NColor4(color);
    }

    private static String normalizeName(String name) {
        return name == null ? null : NNameFormat.CLASS_NAME.format(name.trim()).toLowerCase();
    }


    public static NOptional<NColor> ofName(String name) {
        return NOptional.ofNamed(BY_NAME.get(normalizeName(name)), "color " + name);
    }

    public static NOptional<List<NColor>> ofCanonicalName(String name) {
        return NOptional.ofNamed(_ALL_BY_CANONICAL_NAME.get(normalizeName(name)), "color " + name);
    }

    public static String toHtmlHex(NColor cl) {
        return String.format("#%02X%02X%02X", cl.getRed(), cl.getGreen(), cl.getBlue());
    }

    public static NColor ansiToColor(int index) {
        if (index < 0 || index > 255) {
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


    public static NColor of4(int color, String name) {
        name = NStringUtils.trimToNull(name);
        if (name == null) {
            return of4(color);
        }
        return new AbstractNColor.NColor4Named(color, name);
    }

    public static NColor of8(int color) {
        return new AbstractNColor.NColor8(color);
    }

    public static NColor of8(int color, String name) {
        name = NStringUtils.trimToNull(name);
        if (name == null) {
            return of8(color);
        }
        return new AbstractNColor.NColor8Named(color, name);
    }

    public static NColor of16(int color) {
        return new AbstractNColor.NColor16(color);
    }

    public static NColor of16(int color, String name) {
        name = NStringUtils.trimToNull(name);
        if (name == null) {
            return of16(color);
        }
        return new AbstractNColor.NColor16Named(color, name);
    }

    public static NColor of24(int color, String name) {
        name = NStringUtils.trimToNull(name);
        if (name == null) {
            return of24(color);
        }
        return new AbstractNColor.NColor24Named(color, name);
    }

    public static NColor of24(int color) {
        return new AbstractNColor.NColor24(color);
    }

    public static NColor of32(int r, int g, int b, String name) {
        name = NStringUtils.trimToNull(name);
        if (name == null) {
            return of32(r, g, b);
        }
        return new AbstractNColor.NColor32Named(r, g, b, name);
    }

    public static NColor of32(int r, int g, int b) {
        return new AbstractNColor.NColor32(r, g, b);
    }

    public static NColor of32(int r, int g, int b, int a) {
        return new AbstractNColor.NColor32(r, g, b, a);
    }

    public static NColor of32(int r, int g, int b, int a, String name) {
        name = NStringUtils.trimToNull(name);
        if (name == null) {
            return of32(r, g, b, a);
        }
        return new AbstractNColor.NColor32Named(r, g, b, a, name);
    }

    public static NColor of32(int color) {
        return new AbstractNColor.NColor32(color);
    }

    public static NColor of32(int color, String name) {
        name = NStringUtils.trimToNull(name);
        if (name == null) {
            return of32(color);
        }
        return new AbstractNColor.NColor32(color);
    }

    public static NColor of64(long color) {
        return new AbstractNColor.NColor64(color);
    }

    public static NColor of64(long color, String name) {
        name = NStringUtils.trimToNull(name);
        if (name == null) {
            return of64(color);
        }
        return new AbstractNColor.NColor64Named(color, name);
    }

    public int getRed() {
        return this.getRGB() >> 16 & 255;
    }

    public int getGreen() {
        return this.getRGB() >> 8 & 255;
    }

    public int getBlue() {
        return this.getRGB() >> 0 & 255;
    }

    public int getAlpha() {
        return this.getRGB() >> 24 & 255;
    }

    static class NColor4 extends AbstractNColor {
        private final byte color;

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

        public Bits getBits() {
            return Bits.BITS_4;
        }

        @Override
        public NColor withName(String name) {
            name = NStringUtils.trimToNull(name);
            if (name == null) {
                return this;
            }
            return NColor.of4(color, name);
        }

        @Override
        public int getRGB() {
            return toColor32().getRGB();
        }

        @Override
        public int getIntColor() {
            return (int) color;
        }

        @Override
        public long getLongColor() {
            return (long) color;
        }

        public String getName() {
            return null;
        }
    }

    static class NColor4Named extends AbstractNColor {
        private final byte color;
        private final String name;

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

        public Bits getBits() {
            return Bits.BITS_4;
        }

        @Override
        public NColor withName(String name) {
            name = NStringUtils.trimToNull(name);
            if (name == null) {
                return NColor.of4(color);
            }
            if (name.equals(this.name)) {
                return this;
            }
            return NColor.of4(color, name);
        }

        @Override
        public int getRGB() {
            return toColor32().getRGB();
        }

        @Override
        public int getIntColor() {
            return (int) color;
        }

        @Override
        public long getLongColor() {
            return (long) color;
        }

        public String getName() {
            return name;
        }
    }

    static class NColor8 extends AbstractNColor {
        private final short color;

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

        public Bits getBits() {
            return Bits.BITS_8;
        }

        @Override
        public NColor withName(String name) {
            name = NStringUtils.trimToNull(name);
            if (name == null) {
                return this;
            }
            return NColor.of8(color, name);
        }

        @Override
        public int getRGB() {
            return toColor32().getRGB();
        }

        @Override
        public int getIntColor() {
            return (int) color;
        }

        @Override
        public long getLongColor() {
            return (long) color;
        }

        public String getName() {
            return null;
        }
    }

    static class NColor8Named extends AbstractNColor {
        private final short color;
        private final String name;

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

        public Bits getBits() {
            return Bits.BITS_8;
        }

        @Override
        public NColor withName(String name) {
            name = NStringUtils.trimToNull(name);
            if (name == null) {
                return NColor.of8(color);
            }
            if (name.equals(this.name)) {
                return this;
            }
            return NColor.of8(color, name);
        }

        @Override
        public int getRGB() {
            return toColor32().getRGB();
        }

        @Override
        public int getIntColor() {
            return (int) color;
        }

        @Override
        public long getLongColor() {
            return (long) color;
        }

        public String getName() {
            return name;
        }
    }

    static class NColor16 extends AbstractNColor {
        private final int color;

        public NColor16(int color) {
            this.color = color & 0xFFFF;
        }

        @Override
        public NColor toColor32() {
            return NColor.of32(getRGB());
        }


        public Bits getBits() {
            return Bits.BITS_16;
        }

        @Override
        public NColor withName(String name) {
            name = NStringUtils.trimToNull(name);
            if (name == null) {
                return this;
            }
            return NColor.of16(color, name);
        }

        @Override
        public int getIntColor() {
            return (int) color;
        }

        @Override
        public long getLongColor() {
            return (long) color;
        }

        @Override
        public int getRGB() {
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

        public String getName() {
            return null;
        }
    }

    static class NColor16Named extends AbstractNColor {
        private final int color;
        private final String name;

        public NColor16Named(int color, String name) {
            this.color = color & 0xFFFF;
            this.name = name;
        }

        public Bits getBits() {
            return Bits.BITS_16;
        }

        @Override
        public int getRGB() {
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
            return NColor.of32(getRGB(), name);
        }

        @Override
        public NColor withName(String name) {
            name = NStringUtils.trimToNull(name);
            if (name == null) {
                return NColor.of16(color);
            }
            if (name.equals(this.name)) {
                return this;
            }
            return NColor.of16(color, name);
        }

        @Override
        public int getIntColor() {
            return (int) color;
        }

        @Override
        public long getLongColor() {
            return (long) color;
        }

        public String getName() {
            return name;
        }
    }

    static class NColor24 extends AbstractNColor {
        private final int color;

        public NColor24(int color) {
            this.color = color & 0xFFFFFF;
        }

        @Override
        public NColor toColor32() {
            //just ignore alpha
            return NColor.of32(getRGB());
        }

        @Override
        public int getRGB() {
            // We take the 24-bit color and OR it with 255 shifted to the Alpha position
            return (255 << 24) | (color & 0xFFFFFF);
        }

        public Bits getBits() {
            return Bits.BITS_24;
        }

        @Override
        public NColor withName(String name) {
            name = NStringUtils.trimToNull(name);
            if (name == null) {
                return this;
            }
            return NColor.of24(color, name);
        }

        @Override
        public int getIntColor() {
            return (int) color;
        }

        @Override
        public long getLongColor() {
            return (long) color;
        }


        public String getName() {
            return null;
        }
    }

    static class NColor24Named extends AbstractNColor {
        private final int color;
        private final String name;

        public NColor24Named(int color, String name) {
            this.color = color & 0xFFFFFF;
            this.name = name;
        }

        @Override
        public NColor toColor32() {
            //just ignore alpha
            return NColor.of32(getRGB(), name);
        }

        @Override
        public int getRGB() {
            // We take the 24-bit color and OR it with 255 shifted to the Alpha position
            return (255 << 24) | (color & 0xFFFFFF);
        }

        public Bits getBits() {
            return Bits.BITS_24;
        }

        @Override
        public NColor withName(String name) {
            name = NStringUtils.trimToNull(name);
            if (name == null) {
                return NColor.of24(color);
            }
            if (name.equals(this.name)) {
                return this;
            }
            return NColor.of24(color, name);
        }

        @Override
        public int getIntColor() {
            return (int) color;
        }

        @Override
        public long getLongColor() {
            return (long) color;
        }

        public String getName() {
            return name;
        }
    }

    static class NColor32 extends AbstractNColor {
        private final int color;

        public NColor32(int color) {
            this.color = color;
        }

        public NColor32(int r, int g, int b) {
            this.color = (255 << 24) | (r & 255) << 16 | (g & 255) << 8 | (b & 255);
        }

        public NColor32(int r, int g, int b, int a) {
            this.color = (a & 255) << 24 | (r & 255) << 16 | (g & 255) << 8 | (b & 255);
        }

        public Bits getBits() {
            return Bits.BITS_32;
        }

        @Override
        public NColor toColor32() {
            return this;
        }

        @Override
        public NColor withName(String name) {
            name = NStringUtils.trimToNull(name);
            if (name == null) {
                return this;
            }
            return NColor.of32(color, name);
        }

        @Override
        public int getRGB() {
            return color;
        }

        @Override
        public int getIntColor() {
            return (int) color;
        }

        @Override
        public long getLongColor() {
            return (long) color;
        }

        public String getName() {
            return null;
        }
    }

    static class NColor32Named extends AbstractNColor {
        private final int color;
        private final String name;

        public NColor32Named(int color, String name) {
            this.color = color;
            this.name = name;
        }

        public NColor32Named(int r, int g, int b, String name) {
            this.color = (r & 255) << 16 | (g & 255) << 8 | (b & 255) << 0;
            this.name = name;
        }

        public NColor32Named(int r, int g, int b, int a, String name) {
            this.color = (a & 255) << 24 | (r & 255) << 16 | (g & 255) << 8 | (b & 255) << 0;
            this.name = name;
        }

        public Bits getBits() {
            return Bits.BITS_32;
        }

        @Override
        public NColor toColor32() {
            return this;
        }

        @Override
        public NColor withName(String name) {
            name = NStringUtils.trimToNull(name);
            if (name == null) {
                return NColor.of32(color);
            }
            if (name.equals(this.name)) {
                return this;
            }
            return NColor.of32(color, name);
        }

        @Override
        public int getRGB() {
            return color;
        }

        @Override
        public int getIntColor() {
            return (int) color;
        }

        @Override
        public long getLongColor() {
            return (long) color;
        }

        public String getName() {
            return name;
        }
    }

    static class NColor64 extends AbstractNColor {
        private final long color;

        public NColor64(long color) {
            this.color = color;
        }

        public Bits getBits() {
            return Bits.BITS_64;
        }

        @Override
        public NColor withName(String name) {
            name = NStringUtils.trimToNull(name);
            if (name == null) {
                return this;
            }
            return NColor.of64(color, name);
        }

        @Override
        public int getIntColor() {
            return (int) color;
        }

        @Override
        public long getLongColor() {
            return (long) color;
        }

        @Override
        public int getRGB() {
            return
                    ((int) (color >> 56) & 0xFF) << 24 |
                            ((int) (color >> 40) & 0xFF) << 16 |
                            ((int) (color >> 24) & 0xFF) << 8 |
                            ((int) (color >> 8) & 0xFF);
        }

        @Override
        public NColor toColor32() {
            return NColor.of32(getRGB());
        }

        public String getName() {
            return null;
        }
    }

    static class NColor64Named extends AbstractNColor {
        private final long color;
        private final String name;

        public NColor64Named(long color, String name) {
            this.color = color;
            this.name = name;
        }

        public Bits getBits() {
            return Bits.BITS_64;
        }

        @Override
        public NColor withName(String name) {
            name = NStringUtils.trimToNull(name);
            if (name == null) {
                return NColor.of64(color);
            }
            if (name.equals(this.name)) {
                return this;
            }
            return NColor.of64(color, name);
        }

        @Override
        public int getIntColor() {
            return (int) color;
        }

        @Override
        public long getLongColor() {
            return (long) color;
        }

        @Override
        public int getRGB() {
            return
                    ((int) (color >> 56) & 0xFF) << 24 |
                            ((int) (color >> 40) & 0xFF) << 16 |
                            ((int) (color >> 24) & 0xFF) << 8 |
                            ((int) (color >> 8) & 0xFF);
        }

        @Override
        public NColor toColor32() {
            return NColor.of32(getRGB(), name);
        }

        public String getName() {
            return name;
        }
    }

}
