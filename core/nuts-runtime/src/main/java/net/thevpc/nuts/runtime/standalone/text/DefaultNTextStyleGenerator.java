package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTextStyleGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import net.thevpc.nuts.text.NTextStyles;

public class DefaultNTextStyleGenerator implements NTextStyleGenerator {
    private boolean includeForeground;
    private boolean includeBackground;
    /**
     * 0-1,4 : 0-16
     * 2,8 : 8bit colors, colors
     * 2,24 : 24bits colors
     * any other value : 24bits colors
     */
    private int colors=8;
    private boolean includePlain;
    private boolean includeBold;
    private boolean includeBlink;
    private boolean includeReversed;
    private boolean includeItalic;
    private boolean includeUnderlined;
    private boolean includeStriked;
    private Random rnd = new Random();

    private List<NTextStyle> decos;
    private List<Function<Integer, NTextStyles>> supps;

    private int resolveColorType() {
        switch (colors) {
            case 0:
            case 4:
                return 1;
            case 2:
            case 8:
                return 2;
            case 3:
            case 24:
                return 3;
        }
        return 3;
    }

    private NTextStyle fg(int i) {
        switch (resolveColorType()) {
            case 1: {
                if (i < 0) {
                    i = rnd.nextInt(16);
                }
                return NTextStyle.primary(
                        i % (16)
                );
            }
            case 2: {
                if (i < 0) {
                    i = rnd.nextInt(256);
                }
                return NTextStyle.foregroundColor(
                        i % (256)
                );
            }
            default: {
                if (i < 0) {
                    i = rnd.nextInt(2 << 24);
                }
                return NTextStyle.foregroundTrueColor(
                        i % (2 << 24)
                );
            }
        }
    }

    private NTextStyle bg(int i) {
        switch (resolveColorType()) {
            case 1: {
                if (i < 0) {
                    i = rnd.nextInt(16);
                }
                return NTextStyle.secondary(
                        i % (16)
                );
            }
            case 2: {
                if (i < 0) {
                    i = rnd.nextInt(256);
                }
                return NTextStyle.backgroundColor(
                        i % (256)
                );
            }
            default: {
                if (i < 0) {
                    i = rnd.nextInt(2 << 24);
                }
                return NTextStyle.backgroundTrueColor(
                        i % (2 << 24)
                );
            }
        }
    }

    private List<Function<Integer, NTextStyles>> supps() {
        if (supps == null) {
            boolean includeAny = isIncludeAny();
            supps = new ArrayList<>();
            if (includePlain || includeAny) {
                supps.add((i) -> NTextStyles.PLAIN);
            }
            if (includeForeground || includeAny) {
                supps.add((i) -> {
                    NTextStyle s = fg(i);
                    if (!decos().isEmpty() && rnd.nextBoolean()) {
                        NTextStyle s2 = decos().get(rnd.nextInt(decos().size()));
                        return NTextStyles.of(s,s2);
                    }
                    return NTextStyles.of(s);
                });
            }
            if (includeBackground || includeAny) {
                supps.add((i) -> {
                    NTextStyle s = bg(-1);
                    if (!decos().isEmpty() && rnd.nextBoolean()) {
                        NTextStyle s2 = decos().get(rnd.nextInt(decos().size()));
                        return NTextStyles.of(s, s2);
                    }
                    return NTextStyles.of(s);
                });
            }
            if (!decos().isEmpty() && rnd.nextBoolean()) {
                supps.add((i) -> {
                    NTextStyle s2 = decos().get(rnd.nextInt(decos().size()));
                    return NTextStyles.of(s2);
                });
            }
        }
        return supps;
    }

    @Override
    public NTextStyles hash(Object i) {
        return hash(i == null ? 0 : i.hashCode());
    }

    @Override
    public NTextStyles hash(int i) {
        i = Math.abs(i);
        int a = (i & 3) % supps().size();
        i = i >> 2;
        return supps().get(a).apply(i);
    }

    @Override
    public NTextStyles random() {
        return supps().get(
                rnd.nextInt(supps().size())
        ).apply(-1);
    }

    @Override
    public boolean isIncludePlain() {
        return includePlain;
    }

    @Override
    public NTextStyleGenerator setIncludePlain(boolean includePlain) {
        this.includePlain = includePlain;
        return this;
    }

    @Override
    public boolean isIncludeBold() {
        return includeBold;
    }

    @Override
    public NTextStyleGenerator setIncludeBold(boolean includeBold) {
        this.includeBold = includeBold;
        return this;
    }

    @Override
    public boolean isIncludeBlink() {
        return includeBlink;
    }

    @Override
    public NTextStyleGenerator setIncludeBlink(boolean includeBlink) {
        this.includeBlink = includeBlink;
        return this;
    }

    @Override
    public boolean isIncludeReversed() {
        return includeReversed;
    }

    @Override
    public NTextStyleGenerator setIncludeReversed(boolean includeReversed) {
        this.includeReversed = includeReversed;
        return this;
    }

    @Override
    public boolean isIncludeItalic() {
        return includeItalic;
    }

    @Override
    public NTextStyleGenerator setIncludeItalic(boolean includeItalic) {
        this.includeItalic = includeItalic;
        return this;
    }

    @Override
    public boolean isIncludeUnderlined() {
        return includeUnderlined;
    }

    @Override
    public NTextStyleGenerator setIncludeUnderlined(boolean includeUnderlined) {
        this.includeUnderlined = includeUnderlined;
        return this;
    }

    @Override
    public boolean isIncludeStriked() {
        return includeStriked;
    }

    @Override
    public NTextStyleGenerator setIncludeStriked(boolean includeStriked) {
        this.includeStriked = includeStriked;
        return this;
    }

    private List<NTextStyle> decos() {
        boolean includeAny = isIncludeAny();
        if (decos == null) {
            decos = new ArrayList<>();
            if ((includeStriked || includeAny)) {
                decos.add(NTextStyle.striked());
            }
            if ((includeBold || includeAny)) {
                decos.add(NTextStyle.bold());
            }
            if ((includeBlink || includeAny)) {
                decos.add(NTextStyle.blink());
            }
            if ((includeReversed || includeAny)) {
                decos.add(NTextStyle.reversed());
            }
            if ((includeItalic || includeAny)) {
                decos.add(NTextStyle.italic());
            }
            if ((includeUnderlined || includeAny)) {
                decos.add(NTextStyle.underlined());
            }
        }
        return decos;
    }

    public boolean isIncludeAny() {
        return !includeForeground && !includeBackground
                && !includeBold
                && !includeBlink
                && !includeUnderlined
                && !includeReversed
                && !includeStriked
                && !includeItalic
                && !includePlain
                ;
    }

    @Override
    public boolean isIncludeForeground() {
        return includeForeground;
    }

    @Override
    public NTextStyleGenerator setIncludeForeground(boolean includeForeground) {
        this.includeForeground = includeForeground;
        return this;
    }

    @Override
    public boolean isIncludeBackground() {
        return includeBackground;
    }

    @Override
    public NTextStyleGenerator setIncludeBackground(boolean includeBackground) {
        this.includeBackground = includeBackground;
        return this;
    }

    @Override
    public boolean isUseThemeColors() {
        return this.colors == 4;
    }
    @Override
    public boolean isUsePaletteColors() {
        return this.colors == 8;
    }
    @Override
    public boolean isUseTrueColors() {
        return this.colors == 24;
    }
    @Override
    public NTextStyleGenerator setUseThemeColors() {
        this.colors = 4;
        return this;
    }

    @Override
    public NTextStyleGenerator setUsePaletteColors() {
        this.colors = 8;
        return this;
    }

    @Override
    public NTextStyleGenerator setUseTrueColors() {
        this.colors = 24;
        return this;
    }
}
