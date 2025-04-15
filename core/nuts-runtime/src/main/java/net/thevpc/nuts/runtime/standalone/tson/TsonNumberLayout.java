package net.thevpc.nuts.runtime.standalone.tson;

public enum TsonNumberLayout {
    BINARY,
    DECIMAL,
    OCTAL,
    HEXADECIMAL;

    public int radix() {
        switch (this) {
            case BINARY:
                return 2;
            case OCTAL:
                return 8;
            case DECIMAL:
                return 10;
            case HEXADECIMAL:
                return 16;
        }
        throw new IllegalArgumentException("unexpected TsonNumberLayout");
    }
}
