package net.thevpc.nuts.elem;

public interface NElementFormatter extends NElementTransform {
    static NElementFormatter of(NElementFormatterStyle style) {
        return NElements.of().createElementFormatter(style);
    }

    static NElementFormatter ofPretty() {
        return of(NElementFormatterStyle.PRETTY);
    }

    static NElementFormatter ofCompact(boolean compact) {
        return compact ? of(NElementFormatterStyle.COMPACT) : of(NElementFormatterStyle.PRETTY);
    }

    static NElementFormatter ofCompact() {
        return of(NElementFormatterStyle.COMPACT);
    }

    static NElementFormatter ofSafe() {
        return of(NElementFormatterStyle.SAFE);
    }

    NElementFormatterBuilder builder();
}
