package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.NAffixType;
import net.thevpc.nuts.elem.NElementNewLine;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NNewLineMode;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NIllegalArgumentException;

import java.util.Objects;

public class DefaultNElementNewLine implements NElementNewLine {
    public static DefaultNElementNewLine LF = new DefaultNElementNewLine("\n");
    public static DefaultNElementNewLine CR = new DefaultNElementNewLine("\r");
    public static DefaultNElementNewLine CRLF = new DefaultNElementNewLine("\r\n");
    private NNewLineMode value;

    public static DefaultNElementNewLine of(NNewLineMode newlineToken) {
        NAssert.requireNamedNonNull(newlineToken, "newlineToken");
        switch (newlineToken) {
            case LF:
                return LF;
            case CR:
                return CR;
            case CRLF:
                return CRLF;
            case AUTO:
                return of(NNewLineMode.AUTO.normalize());
        }
        throw new NIllegalArgumentException(NMsg.ofC("expected a single newline"));
    }

    public static DefaultNElementNewLine of(String newlineToken) {
        NAssert.requireNamedNonNull(newlineToken, "newlineToken");
        switch (newlineToken) {
            case "\n":
                return LF;
            case "\r":
                return CR;
            case "\r\n":
                return CRLF;
        }
        throw new NIllegalArgumentException(NMsg.ofC("expected a single newline"));
    }

    private DefaultNElementNewLine(String newlineToken) {
        NAssert.requireNamedNonNull(newlineToken, "newlineToken");
        switch (newlineToken) {
            case "\n": {
                this.value = NNewLineMode.LF;
                break;
            }
            case "\r": {
                this.value = NNewLineMode.CR;
                break;
            }
            case "\r\n": {
                this.value = NNewLineMode.CRLF;
                break;
            }
            default: {
                throw new NIllegalArgumentException(NMsg.ofC("expected a single newline"));
            }
        }
    }

    private DefaultNElementNewLine(NNewLineMode newlineToken) {
        NAssert.requireNamedNonNull(newlineToken, "newlineToken");
        this.value = newlineToken;
    }

    @Override
    public NNewLineMode value() {
        return value;
    }

    @Override
    public NAffixType type() {
        return NAffixType.NEWLINE;
    }

    @Override
    public boolean isBlank() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNElementNewLine that = (DefaultNElementNewLine) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return value.value();
    }
}
