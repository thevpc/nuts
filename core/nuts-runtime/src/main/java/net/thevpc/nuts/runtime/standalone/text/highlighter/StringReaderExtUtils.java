package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.NutsTextStyle;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringReaderExt;

import java.util.ArrayList;
import java.util.List;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsTexts;
import net.thevpc.nuts.NutsText;

public class StringReaderExtUtils {

    public static NutsText[] readSpaces(NutsSession session, StringReaderExt ar) {
        NutsTexts factory = NutsTexts.of(session);
        StringBuilder sb = new StringBuilder();
        while (ar.hasNext() && ar.peekChar() <= 32) {
            sb.append(ar.nextChar());
        }
        return new NutsText[]{
            factory.ofPlain(sb.toString())
        };
    }

    public static NutsText[] readSlashSlashComments(NutsSession session, StringReaderExt ar) {
        NutsTexts factory = NutsTexts.of(session);
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
        return new NutsText[]{
            factory.ofStyled(sb.toString(), NutsTextStyle.comments())
        };
    }

    public static NutsText[] readSlashStarComments(NutsSession session, StringReaderExt ar) {
        NutsTexts factory = NutsTexts.of(session);
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
        return new NutsText[]{
            factory.ofStyled(sb.toString(), NutsTextStyle.comments(2))
        };
    }

    public static NutsText[] readJSDoubleQuotesString(NutsSession session, StringReaderExt ar) {
        NutsTexts factory = NutsTexts.of(session);
        List<NutsText> all = new ArrayList<>();
        boolean inLoop = true;
        StringBuilder sb = new StringBuilder();
        if (ar.hasNext() && ar.peekChars("\"")) {
            sb.append(ar.nextChar());
            while (inLoop && ar.hasNext()) {
                switch (ar.peekChar()) {
                    case '\\': {
                        if (sb.length() > 0) {
                            all.add(factory.ofStyled(sb.toString(), NutsTextStyle.string()));
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
                        all.add(factory.ofStyled(sb2.toString(), NutsTextStyle.separator()));
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
                all.add(factory.ofStyled(sb.toString(), NutsTextStyle.string()));
                sb.setLength(0);
            }
            return all.toArray(new NutsText[0]);
        } else {
            return null;
        }
    }

    public static NutsText[] readJSSimpleQuotes(NutsSession session, StringReaderExt ar) {
        NutsTexts factory = NutsTexts.of(session);
        List<NutsText> all = new ArrayList<>();
        boolean inLoop = true;
        StringBuilder sb = new StringBuilder();
        if (ar.hasNext() && ar.peekChars("\'")) {
            sb.append(ar.nextChar());
            while (inLoop && ar.hasNext()) {
                switch (ar.peekChar()) {
                    case '\\': {
                        if (sb.length() > 0) {
                            all.add(factory.ofStyled(sb.toString(), NutsTextStyle.string()));
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
                        all.add(factory.ofStyled(sb2.toString(), NutsTextStyle.separator()));
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
                all.add(factory.ofStyled(sb.toString(), NutsTextStyle.string(2)));
                sb.setLength(0);
            }
            return all.toArray(new NutsText[0]);
        } else {
            return null;
        }
    }

    public static NutsText[] readJSIdentifier(NutsSession session, StringReaderExt ar) {
        NutsTexts factory = NutsTexts.of(session);
        List<NutsText> all = new ArrayList<>();
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
        return all.toArray(new NutsText[0]);

    }

    public static NutsText[] readNumber(NutsSession session, StringReaderExt ar) {
        NutsTexts factory = NutsTexts.of(session);
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
            return new NutsText[]{
                factory.ofStyled(ar.nextChars(lastOk + 1), NutsTextStyle.number())
            };
        }
        return null;
    }
}
