package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTextStyleGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import net.thevpc.nuts.text.NutsTextStyles;

public class DefaultNutsTextStyleGenerator implements NutsTextStyleGenerator {
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

    private List<NutsTextStyle> decos;
    private List<Function<Integer, NutsTextStyles>> supps;

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

    private NutsTextStyle fg(int i) {
        switch (resolveColorType()) {
            case 1: {
                if (i < 0) {
                    i = rnd.nextInt(16);
                }
                return NutsTextStyle.primary(
                        i % (16)
                );
            }
            case 2: {
                if (i < 0) {
                    i = rnd.nextInt(256);
                }
                return NutsTextStyle.foregroundColor(
                        i % (256)
                );
            }
            default: {
                if (i < 0) {
                    i = rnd.nextInt(2 << 24);
                }
                return NutsTextStyle.foregroundTrueColor(
                        i % (2 << 24)
                );
            }
        }
    }

    private NutsTextStyle bg(int i) {
        switch (resolveColorType()) {
            case 1: {
                if (i < 0) {
                    i = rnd.nextInt(16);
                }
                return NutsTextStyle.secondary(
                        i % (16)
                );
            }
            case 2: {
                if (i < 0) {
                    i = rnd.nextInt(256);
                }
                return NutsTextStyle.backgroundColor(
                        i % (256)
                );
            }
            default: {
                if (i < 0) {
                    i = rnd.nextInt(2 << 24);
                }
                return NutsTextStyle.backgroundTrueColor(
                        i % (2 << 24)
                );
            }
        }
    }

    private List<Function<Integer, NutsTextStyles>> supps() {
        if (supps == null) {
            boolean includeAny = isIncludeAny();
            supps = new ArrayList<>();
            if (includePlain || includeAny) {
                supps.add((i) -> NutsTextStyles.PLAIN);
            }
            if (includeForeground || includeAny) {
                supps.add((i) -> {
                    NutsTextStyle s = fg(i);
                    if (!decos().isEmpty() && rnd.nextBoolean()) {
                        NutsTextStyle s2 = decos().get(rnd.nextInt(decos().size()));
                        return NutsTextStyles.of(s,s2);
                    }
                    return NutsTextStyles.of(s);
                });
            }
            if (includeBackground || includeAny) {
                supps.add((i) -> {
                    NutsTextStyle s = bg(-1);
                    if (!decos().isEmpty() && rnd.nextBoolean()) {
                        NutsTextStyle s2 = decos().get(rnd.nextInt(decos().size()));
                        return NutsTextStyles.of(s, s2);
                    }
                    return NutsTextStyles.of(s);
                });
            }
            if (!decos().isEmpty() && rnd.nextBoolean()) {
                supps.add((i) -> {
                    NutsTextStyle s2 = decos().get(rnd.nextInt(decos().size()));
                    return NutsTextStyles.of(s2);
                });
            }
        }
        return supps;
    }

    @Override
    public NutsTextStyles hash(Object i) {
        return hash(i == null ? 0 : i.hashCode());
    }

    @Override
    public NutsTextStyles hash(int i) {
        i = Math.abs(i);
        int a = (i & 3) % supps().size();
        i = i >> 2;
        return supps().get(a).apply(i);
    }

    @Override
    public NutsTextStyles random() {
        return supps().get(
                rnd.nextInt(supps().size())
        ).apply(-1);
    }

    @Override
    public boolean isIncludePlain() {
        return includePlain;
    }

    @Override
    public NutsTextStyleGenerator setIncludePlain(boolean includePlain) {
        this.includePlain = includePlain;
        return this;
    }

    @Override
    public boolean isIncludeBold() {
        return includeBold;
    }

    @Override
    public NutsTextStyleGenerator setIncludeBold(boolean includeBold) {
        this.includeBold = includeBold;
        return this;
    }

    @Override
    public boolean isIncludeBlink() {
        return includeBlink;
    }

    @Override
    public NutsTextStyleGenerator setIncludeBlink(boolean includeBlink) {
        this.includeBlink = includeBlink;
        return this;
    }

    @Override
    public boolean isIncludeReversed() {
        return includeReversed;
    }

    @Override
    public NutsTextStyleGenerator setIncludeReversed(boolean includeReversed) {
        this.includeReversed = includeReversed;
        return this;
    }

    @Override
    public boolean isIncludeItalic() {
        return includeItalic;
    }

    @Override
    public NutsTextStyleGenerator setIncludeItalic(boolean includeItalic) {
        this.includeItalic = includeItalic;
        return this;
    }

    @Override
    public boolean isIncludeUnderlined() {
        return includeUnderlined;
    }

    @Override
    public NutsTextStyleGenerator setIncludeUnderlined(boolean includeUnderlined) {
        this.includeUnderlined = includeUnderlined;
        return this;
    }

    @Override
    public boolean isIncludeStriked() {
        return includeStriked;
    }

    @Override
    public NutsTextStyleGenerator setIncludeStriked(boolean includeStriked) {
        this.includeStriked = includeStriked;
        return this;
    }

    private List<NutsTextStyle> decos() {
        boolean includeAny = isIncludeAny();
        if (decos == null) {
            decos = new ArrayList<>();
            if ((includeStriked || includeAny)) {
                decos.add(NutsTextStyle.striked());
            }
            if ((includeBold || includeAny)) {
                decos.add(NutsTextStyle.bold());
            }
            if ((includeBlink || includeAny)) {
                decos.add(NutsTextStyle.blink());
            }
            if ((includeReversed || includeAny)) {
                decos.add(NutsTextStyle.reversed());
            }
            if ((includeItalic || includeAny)) {
                decos.add(NutsTextStyle.italic());
            }
            if ((includeUnderlined || includeAny)) {
                decos.add(NutsTextStyle.underlined());
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
    public NutsTextStyleGenerator setIncludeForeground(boolean includeForeground) {
        this.includeForeground = includeForeground;
        return this;
    }

    @Override
    public boolean isIncludeBackground() {
        return includeBackground;
    }

    @Override
    public NutsTextStyleGenerator setIncludeBackground(boolean includeBackground) {
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
    public NutsTextStyleGenerator setUseThemeColors() {
        this.colors = 4;
        return this;
    }

    @Override
    public NutsTextStyleGenerator setUsePaletteColors() {
        this.colors = 8;
        return this;
    }

    @Override
    public NutsTextStyleGenerator setUseTrueColors() {
        this.colors = 24;
        return this;
    }
}
