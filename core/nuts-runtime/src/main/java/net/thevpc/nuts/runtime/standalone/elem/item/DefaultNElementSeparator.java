package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.NAffixType;
import net.thevpc.nuts.elem.NElementSeparator;
import net.thevpc.nuts.elem.NElementSpace;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NIllegalArgumentException;

import java.util.Objects;

public class DefaultNElementSeparator implements NElementSeparator {
    private static DefaultNElementSeparator COMMA = new DefaultNElementSeparator(",");
    private static DefaultNElementSeparator SEMI_COLON = new DefaultNElementSeparator(";");

    private String str;

    public static DefaultNElementSeparator of(String separator) {
        NAssert.requireNonNull(separator, "separator");
        switch (separator) {
            case ",":
                return COMMA;
            case ";":
                return SEMI_COLON;
        }
        throw new NIllegalArgumentException(NMsg.ofC("invalid separator %s", separator));
    }

    public static DefaultNElementSeparator of(char separator) {
        switch (separator) {
            case ',':
                return COMMA;
            case ';':
                return SEMI_COLON;
        }
        throw new NIllegalArgumentException(NMsg.ofC("invalid separator %s", separator));
    }


    private DefaultNElementSeparator(String str) {
        this.str = str;
    }

    @Override
    public String value() {
        return str;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNElementSeparator that = (DefaultNElementSeparator) o;
        return Objects.equals(str, that.str);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(str);
    }

    @Override
    public NAffixType type() {
        return NAffixType.SEPARATOR;
    }

    @Override
    public boolean isBlank() {
        return true;
    }

    @Override
    public String toString() {
        return value();
    }
}
