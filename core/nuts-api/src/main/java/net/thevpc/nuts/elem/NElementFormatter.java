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

    static NElementFormatter ofStable() {
        return of(NElementFormatterStyle.STABLE);
    }

    static NElementFormatter ofVerbatim() {
        return of(NElementFormatterStyle.VERBATIM);
    }

    static NElementFormatter ofSimple() {
        return of(NElementFormatterStyle.SIMPLE);
    }

    NElementFormatterBuilder builder();
}
