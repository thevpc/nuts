package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.NAffixType;
import net.thevpc.nuts.elem.NElementSpace;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NIllegalArgumentException;

import java.util.Objects;

public class DefaultNElementSpace implements NElementSpace {
    private static DefaultNElementSpace S1 = new DefaultNElementSpace(" ");
    private static DefaultNElementSpace S2 = new DefaultNElementSpace("  ");
    private static DefaultNElementSpace S3 = new DefaultNElementSpace("   ");
    private static DefaultNElementSpace S4 = new DefaultNElementSpace("    ");
    private static DefaultNElementSpace T1 = new DefaultNElementSpace("\t");
    private static DefaultNElementSpace T2 = new DefaultNElementSpace("\t\t");
    private static DefaultNElementSpace T3 = new DefaultNElementSpace("\t\t\t");
    private static DefaultNElementSpace T4 = new DefaultNElementSpace("\t\t\t\t");

    private String str;

    public static DefaultNElementSpace of(String spacesToken) {
        NAssert.requireNonNull(spacesToken, "spacesToken");
        char[] c = spacesToken.toCharArray();
        NAssert.requireTrue(c.length > 0, () -> NMsg.ofC("empty space"));
        boolean repeatable = true;
        char c0 = c[0];
        checkSpaceChar(c0);
        for (int i = 1; i < c.length; i++) {
            if (c[i] != c0) {
                checkSpaceChar(c[i]);
                repeatable = false;
                break;
            }
        }
        if (repeatable) {
            switch (c0) {
                case ' ': {
                    switch (c.length) {
                        case 1:
                            return S1;
                        case 2:
                            return S2;
                        case 3:
                            return S3;
                        case 4:
                            return S4;
                    }
                    break;
                }
                case '\t': {
                    switch (c.length) {
                        case 1:
                            return T1;
                        case 2:
                            return T2;
                        case 3:
                            return T3;
                        case 4:
                            return T4;
                    }
                    break;
                }
            }
        }
        return new DefaultNElementSpace(spacesToken);
    }

    private static void checkSpaceChar(char c) {
        switch (c) {
            case ' ':
            case '\t': {
                //okkay
                break;
            }
            case '\n':
            case '\r': {
                throw new NIllegalArgumentException(NMsg.ofC("newlines are not allowed ins spaces"));
            }
            default: {
                if (!Character.isWhitespace(c)) {
                    throw new NIllegalArgumentException(NMsg.ofC("non whitespaces are not allowed ins spaces"));
                }
            }
        }
    }

    private DefaultNElementSpace(String str) {
        this.str = str;
    }

    @Override
    public String value() {
        return str;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNElementSpace that = (DefaultNElementSpace) o;
        return Objects.equals(str, that.str);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(str);
    }

    @Override
    public NAffixType type() {
        return NAffixType.SPACE;
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
