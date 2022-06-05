package net.thevpc.nuts.text;

public interface NutsStringWriter {
    void append(Object any);

    void append(Object any, NutsTextStyle style);

    void append(Object any, NutsTextStyles styles);
}
