package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

public interface CharStreamCodeSupport {
    void reset();

    String getErrorMessage();

    boolean isValid();

    void next(char cbuf[]);

    void next(char cbuf[], int off, int len);

    void next(char c);
}
