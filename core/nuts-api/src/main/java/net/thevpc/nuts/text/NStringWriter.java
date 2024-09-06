package net.thevpc.nuts.text;

public interface NStringWriter {
    void append(Object any);

    void append(Object any, NTextStyle style);

    void append(Object any, NTextStyles styles);
}
