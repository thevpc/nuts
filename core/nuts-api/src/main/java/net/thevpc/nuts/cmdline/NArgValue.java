package net.thevpc.nuts.cmdline;

import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NOptional;

public interface NArgValue<T> {
    String key();
    T value();
    boolean booleanValue();
    String stringValue();
    NOptional<T> optionalValue();
    NLiteral literal();
}
