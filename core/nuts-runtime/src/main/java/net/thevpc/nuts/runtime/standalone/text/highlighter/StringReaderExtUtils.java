package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringReaderExt;

import java.util.ArrayList;
import java.util.List;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.text.NText;

public class StringReaderExtUtils {

    public static NText[] readSpaces(NSession session, StringReaderExt ar) {
        NTexts factory = NTexts.of(session);
        StringBuilder sb = new StringBuilder();
        while (ar.hasNext() && ar.peekChar() <= 32) {
            sb.append(ar.nextChar());
        }
        return new NText[]{
            factory.ofPlain(sb.toString())
        };
    }

    public static NText[] readSlashSlashComments(NSession session, StringReaderExt ar) {
        NTexts factory = NTexts.of(session);
        StringBuilder sb = new StringBuilder();
        if (!ar.peekChars("//")) {
            return null;
        }
        sb.append(ar.nextChars(2));
        boolean inLoop = true;
        while (inLoop && ar.hasNext()) {
            switch (ar.peekChar()) {
                case '\n':
                case '\r': {
                    sb.append(ar.nextChar());
                    if (ar.hasNext() && ar.peekChar() == '\n') {
                        sb.append(ar.nextChar());
                    }
                    inLoop = false;
                    break;
                }
                default: {
                    sb.append(ar.nextChar());
                }
            }
        }
        return new NText[]{
            factory.ofStyled(sb.toString(), NTextStyle.comments())
        };
    }

    public static NText[] readSlashStarComments(NSession session, StringReaderExt ar) {
        NTexts factory = NTexts.of(session);
        StringBuilder sb = new StringBuilder();
        if (!ar.peekChars("/*")) {
            return null;
        }
        sb.append(ar.nextChars(2));
        boolean inLoop = true;
        while (inLoop && ar.hasNext()) {
            switch (ar.peekChar()) {
                case '*': {
                    if (ar.peekChars("*/")) {
                        sb.append(ar.nextChars(2));
                        inLoop = false;
                    } else {
                        sb.append(ar.nextChar());
                    }
                    break;
                }
                default: {
                    sb.append(ar.nextChar());
                }
            }
        }
        return new NText[]{
            factory.ofStyled(sb.toString(), NTextStyle.comments(2))
        };
    }

    public static NText[] readJSDoubleQuotesString(NSession session, StringReaderExt ar) {
        NTexts factory = NTexts.of(session);
        List<NText> all = new ArrayList<>();
        boolean inLoop = true;
        StringBuilder sb = new StringBuilder();
        if (ar.hasNext() && ar.peekChars("\"")) {
            sb.append(ar.nextChar());
            while (inLoop && ar.hasNext()) {
                switch (ar.peekChar()) {
                    case '\\': {
                        if (sb.length() > 0) {
                            all.add(factory.ofStyled(sb.toString(), NTextStyle.string()));
                            sb.setLength(0);
                        }
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append(ar.nextChar());
                        if (ar.hasNext()) {
                            sb2.append(ar.nextChar());
                            if (ar.peekChar() == 'u') {
                                for (int i = 0; i < 4; i++) {
                                    char c2 = ar.peekChar();
                                    if (Character.isDigit(c2) || (Character.toUpperCase(c2) >= 'A' && Character.toUpperCase(c2) <= 'F')) {
                                        sb2.append(ar.nextChar());
                                    }
                                }
                            }
                        }
                        all.add(factory.ofStyled(sb2.toString(), NTextStyle.separator()));
                        break;
                    }
                    case '\"': {
                        sb.append(ar.nextChar());
                        inLoop = false;
                        break;
                    }
                    default: {
                        sb.append(ar.nextChar());
                    }
                }
            }
            if (sb.length() > 0) {
                all.add(factory.ofStyled(sb.toString(), NTextStyle.string()));
                sb.setLength(0);
            }
            return all.toArray(new NText[0]);
        } else {
            return null;
        }
    }

    public static NText[] readJSSimpleQuotes(NSession session, StringReaderExt ar) {
        NTexts factory = NTexts.of(session);
        List<NText> all = new ArrayList<>();
        boolean inLoop = true;
        StringBuilder sb = new StringBuilder();
        if (ar.hasNext() && ar.peekChars("\'")) {
            sb.append(ar.nextChar());
            while (inLoop && ar.hasNext()) {
                switch (ar.peekChar()) {
                    case '\\': {
                        if (sb.length() > 0) {
                            all.add(factory.ofStyled(sb.toString(), NTextStyle.string()));
                            sb.setLength(0);
                        }
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append(ar.nextChar());
                        if (ar.hasNext()) {
                            sb2.append(ar.nextChar());
                            if (ar.peekChar() == 'u') {
                                for (int i = 0; i < 4; i++) {
                                    char c2 = ar.peekChar();
                                    if (Character.isDigit(c2) || (Character.toUpperCase(c2) >= 'A' && Character.toUpperCase(c2) <= 'F')) {
                                        sb2.append(ar.nextChar());
                                    }
                                }
                            }
                        }
                        all.add(factory.ofStyled(sb2.toString(), NTextStyle.separator()));
                        break;
                    }
                    case '\'': {
                        sb.append(ar.nextChar());
                        inLoop = false;
                        break;
                    }
                    default: {
                        sb.append(ar.nextChar());
                    }
                }
            }
            if (sb.length() > 0) {
                all.add(factory.ofStyled(sb.toString(), NTextStyle.string(2)));
                sb.setLength(0);
            }
            return all.toArray(new NText[0]);
        } else {
            return null;
        }
    }

    public static NText[] readJSIdentifier(NSession session, StringReaderExt ar) {
        NTexts factory = NTexts.of(session);
        List<NText> all = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        if (!ar.hasNext() || !Character.isJavaIdentifierStart(ar.peekChar())) {
            return null;
        }
        sb.append(ar.nextChar());
        while (ar.hasNext()) {
            if (Character.isJavaIdentifierPart(ar.peekChar())) {
                sb.append(ar.nextChar());
            } else {
                break;
            }
        }
        all.add(factory.ofPlain(sb.toString()));
        return all.toArray(new NText[0]);
    }


    public static NText[] readNumber(NSession session, StringReaderExt ar) {
        NTexts factory = NTexts.of(session);
        boolean nbrVisited = false;
        boolean minusVisited = false;
        boolean EminusVisited = false;
        boolean dotVisited = false;
        boolean EVisited = false;
        boolean Enbr = false;
        int index = 0;
        int lastOk = -1;
        boolean inLoop = true;
        while (inLoop && ar.hasNext(index)) {
            char c = ar.peekChar(index);
            switch (c) {
                case 'E': {
                    if (EVisited || !nbrVisited) {
                        inLoop = false;
                    } else {
                        EVisited = true;
                    }
                    break;
                }
                case '.': {
                    if (dotVisited) {
                        inLoop = false;
                    } else {
                        lastOk = index;
                        dotVisited = true;
                    }
                    break;
                }
                case '-': {
                    if (EVisited) {
                        if (EminusVisited || Enbr) {
                            inLoop = false;
                        } else {
                            EminusVisited = true;
                        }
                    } else {
                        nbrVisited = true;
                    }
                    break;
                }
                default: {
                    if (Character.isDigit(c)) {
                        if (EVisited) {
                            Enbr = true;
                        } else {
                            nbrVisited = true;
                        }
                        lastOk = index;
                    } else {
                        inLoop = false;
                    }
                }
            }
            index++;
        }
        if (lastOk >= 0) {
            return new NText[]{
                factory.ofStyled(ar.nextChars(lastOk + 1), NTextStyle.number())
            };
        }
        return null;
    }
}
