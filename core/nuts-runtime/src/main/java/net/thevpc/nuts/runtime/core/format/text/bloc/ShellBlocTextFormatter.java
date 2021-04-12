package net.thevpc.nuts.runtime.core.format.text.bloc;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.parsers.StringReaderExt;
import net.thevpc.nuts.runtime.core.format.text.parser.DefaultNutsTextNodePlain;
import net.thevpc.nuts.runtime.core.format.text.parser.DefaultNutsTextNodeStyled;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.thevpc.nuts.spi.NutsComponent;
import net.thevpc.nuts.NutsCodeFormat;

public class ShellBlocTextFormatter implements NutsCodeFormat {

    private NutsWorkspace ws;
    private NutsTextManager factory;

    public ShellBlocTextFormatter(NutsWorkspace ws) {
        this.ws = ws;
        factory = ws.formats().text();
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<String> criteria) {
        String s = criteria.getConstraints();
        return "sh".equals(s) ? NutsComponent.DEFAULT_SUPPORT : NutsComponent.NO_SUPPORT;
    }

    @Override
    public NutsTextNode tokenToNode(String text, String nodeType,NutsSession session) {
        factory.setSession(session);
        return factory.plain(text);
    }

    private NutsTextNode[] parseCommandLine_readSimpleQuotes(StringReaderExt ar, NutsSession session) {
        StringBuilder sb = new StringBuilder();
        sb.append(ar.nextChar()); //quote!
        List<NutsTextNode> ret = new ArrayList<>();
        while (ar.hasNext()) {
            char c = ar.peekChar();
            if (c == '\\') {
                StringBuilder sb2 = new StringBuilder();
                sb2.append(ar.nextChar());
                if (sb.length() > 0) {
                    ret.add(factory.styled(sb.toString(), NutsTextNodeStyle.string(2)));
                    sb.setLength(0);
                }
                if (ar.hasNext()) {
                    sb2.append(ar.nextChar());
                }
                ret.add(factory.styled(sb2.toString(), NutsTextNodeStyle.separator()));
                break;
            } else if (c == '\'') {
                sb.append(ar.nextChar());
                break;
            } else {
                sb.append(ar.nextChar());
            }
        }
        if (sb.length() > 0) {
            ret.add(factory.styled(sb.toString(), NutsTextNodeStyle.string(2)));
            sb.setLength(0);
        }
        return ret.toArray(new NutsTextNode[0]);
    }

    private NutsTextNode[] parseCommandLine_readWord(StringReaderExt ar, NutsSession session) {
        StringBuilder sb = new StringBuilder();
        List<NutsTextNode> ret = new ArrayList<>();
        boolean inLoop = true;
        boolean endsWithSep = false;
        while (inLoop && ar.hasNext()) {
            char c = ar.peekChar();
            switch (c) {
                case '\\': {
                    if (sb.length() > 0) {
                        ret.add(factory.plain(sb.toString()));
                        sb.setLength(0);
                    }
                    ret.addAll(Arrays.asList(parseCommandLine_readAntiSlash(ws, ar, session)));
                    break;
                }
                case ';': {
                    endsWithSep = true;
                    inLoop = false;
                    break;
                }
                case ':': {
                    endsWithSep = true;
                    inLoop = false;
                    break;
                }
                case '$':
                case '`':
                case '\"':
                case '\'':
                case '(':
                case ')':
                case '[':
                case ']':
                case '{':
                case '}':
                case '<':
                case '>':
                case '&':
                case '|':
                case '*':
                case '?':
                case '#':
                case '=':
                case '~': 
                case '!': 
                {
                    inLoop = false;
                    break;
                }
                default: {
                    if (c <= 32) {
                        endsWithSep = true;
                        inLoop = false;
                    } else {
                        sb.append(ar.nextChar());
                    }
                }
            }
        }
        if (sb.length() > 0) {
            ret.add(factory.plain(sb.toString()));
            sb.setLength(0);
        }
        if (ret.isEmpty()) {
            throw new IllegalArgumentException("was not expecting " + ar.peekChar() + " as part of word");
        }
        if (ret.get(0).getType() == NutsTextNodeType.PLAIN && isOption(((NutsTextNodePlain) ret.get(0)).getText())) {
            ret.set(0, factory.styled(ret.get(0), NutsTextNodeStyle.option()));
        }
        return ret.toArray(new NutsTextNode[0]);
    }

    private static NutsTextNode[] parseCommandLine_readAntiSlash(NutsWorkspace ws, StringReaderExt ar, NutsSession session) {
        StringBuilder sb2 = new StringBuilder();
        sb2.append(ar.nextChar());
        if (ar.hasNext()) {
            sb2.append(ar.nextChar());
        }
        NutsTextManager factory = ws.formats().text();
        return new NutsTextNode[]{factory.styled(sb2.toString(), NutsTextNodeStyle.separator())};
    }

    private NutsTextNode[] parseCommandLine_readDollar(StringReaderExt ar, NutsSession session) {
        if (ar.peekChars("$((")) {
            return parseCommandLine_readDollarPar2(ar, session);
        }
        StringBuilder sb2 = new StringBuilder();
        if (ar.hasNext(1)) {
            switch (ar.peekChar(1)) {
                case '(': {
                    return parseCommandLine_readDollarPar2(ar, session);
                }
                case '{': {
                    return parseCommandLine_readDollarCurlyBrackets(ar, session);
                }
                case '*':
                case '?':
                case '@':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9': {
                    sb2.append(ar.nextChar());
                    sb2.append(ar.nextChar());
                    return new NutsTextNode[]{factory.styled(sb2.toString(), NutsTextNodeStyle.separator())};
                }
            }
        }
        ar.nextChar();
        while (ar.hasNext()) {
            char c = ar.peekChar();
            if (Character.isAlphabetic(c) || Character.isDigit(c) || c == '_') {
                sb2.append(ar.nextChar());
            } else {
                break;
            }
        }
        if (sb2.length() > 0) {
            return new NutsTextNode[]{
                factory.styled("$", NutsTextNodeStyle.separator()),
                factory.styled(sb2.toString(), NutsTextNodeStyle.keyword(4)),};
        }
        return new NutsTextNode[]{
            factory.styled("$", NutsTextNodeStyle.separator()),};
    }

    private NutsTextNode[] parseCommandLine_readDoubleQuotes(StringReaderExt ar, NutsSession session) {
        List<NutsTextNode> ret = new ArrayList<>();
        factory.setSession(session);
        StringBuilder sb = new StringBuilder();

        ret.add(factory.styled(String.valueOf(ar.nextChar()), NutsTextNodeStyle.string()));
        while (ar.hasNext()) {
            char c = ar.peekChar();
            if (c == '\\') {
                if (sb.length() > 0) {
                    ret.add(factory.styled(sb.toString(), NutsTextNodeStyle.string()));
                    sb.setLength(0);
                }
                ret.addAll(Arrays.asList(parseCommandLine_readAntiSlash(ws, ar, session)));
            } else if (c == '$') {
                if (sb.length() > 0) {
                    ret.add(factory.styled(sb.toString(), NutsTextNodeStyle.string()));
                    sb.setLength(0);
                }
                ret.addAll(Arrays.asList(parseCommandLine_readDollar(ar, session)));
            } else if (c == '\"') {
                if (sb.length() > 0) {
                    ret.add(factory.styled(sb.toString(), NutsTextNodeStyle.string()));
                    sb.setLength(0);
                }
                ret.add(factory.styled(String.valueOf(ar.nextChar()), NutsTextNodeStyle.string()));
                break;
            } else {
                sb.append(ar.nextChar());
            }
        }
        if (sb.length() > 0) {
            ret.add(factory.styled(sb.toString(), NutsTextNodeStyle.string()));
            sb.setLength(0);
        }
        return ret.toArray(new NutsTextNode[0]);
    }

    private static boolean isWord(NutsTextNode n) {
        if (n instanceof DefaultNutsTextNodePlain) {
            if (Character.isAlphabetic(((DefaultNutsTextNodePlain) n).getText().charAt(0))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSeparator(NutsTextNode n) {
        if (n instanceof DefaultNutsTextNodeStyled) {
            NutsTextNode v = ((DefaultNutsTextNodeStyled) n).getChild();
            if (v instanceof DefaultNutsTextNodePlain) {
                String t = ((DefaultNutsTextNodePlain) v).getText();
                switch (t.charAt(0)) {
                    case ';':
                    case '&':
                    case '|':
                        return true;
                }
            }
        }
        return false;
    }

    private static boolean isWhites(NutsTextNode n) {
        if (n instanceof DefaultNutsTextNodePlain) {
            if (Character.isWhitespace(((DefaultNutsTextNodePlain) n).getText().charAt(0))) {
                return true;
            }
        }
        return false;
    }

    private static int indexOfFirstWord(List<NutsTextNode> all, int from) {
        for (int i = from; i < all.size(); i++) {
            NutsTextNode n = all.get(i);
            if (isWord(n)) {
                if (i == all.size() - 1 || isWhites(all.get(i + 1)) || isSeparator(all.get(i + 1))) {
                    return i;
                }
            }
        }
        return -1;
    }

    private NutsTextNode[] parseCommandLine_readAntiQuotes(StringReaderExt ar, NutsSession session) {
        List<NutsTextNode> all = new ArrayList<>();
        factory.setSession(session);
        all.add(factory.styled(String.valueOf(ar.nextChar()), NutsTextNodeStyle.separator()));
        boolean inLoop = true;
        boolean wasSpace = true;
        while (inLoop && ar.hasNext()) {
            char c = ar.peekChar();
            switch (c) {
                case '`': {
                    wasSpace = false;
                    all.add(factory.styled(String.valueOf(ar.nextChar()), NutsTextNodeStyle.separator()));
                    inLoop = false;
                    break;
                }
                default: {
                    wasSpace = parseCommandLineStep(ar, all, 1, wasSpace, session);
                }
            }
        }
        return all.toArray(new NutsTextNode[0]);
    }

    private NutsTextNode[] parseCommandLine_readDollarPar(NutsWorkspace ws, StringReaderExt ar, NutsSession session) {
        List<NutsTextNode> all = new ArrayList<>();
        factory.setSession(session);
        all.add(factory.styled(String.valueOf(ar.nextChar()) + ar.nextChar(), NutsTextNodeStyle.separator()));
        boolean inLoop = true;
        boolean wasSpace = false;
        while (inLoop && ar.hasNext()) {
            char c = ar.peekChar();
            switch (c) {
                case ')': {
                    all.add(factory.styled(String.valueOf(ar.nextChar()), NutsTextNodeStyle.separator()));
                    inLoop = false;
                    break;
                }
                default: {
                    wasSpace = parseCommandLineStep(ar, all, 2, wasSpace, session);
                }
            }
        }
        return all.toArray(new NutsTextNode[0]);
    }

    private NutsTextNode[] parseCommandLine_readDollarPar2(StringReaderExt ar, NutsSession session) {
        List<NutsTextNode> all = new ArrayList<>();
        factory.setSession(session);
        all.add(factory.styled(String.valueOf(ar.nextChar()) + ar.nextChar() + ar.nextChar(), NutsTextNodeStyle.separator()));
        boolean inLoop = true;
        boolean wasSpace = true;
        while (inLoop && ar.hasNext()) {
            char c = ar.peekChar();
            switch (c) {
                case '+':
                case '-':
                case '*':
                case '/':
                case '%': {
                    wasSpace = false;
                    all.add(factory.styled(String.valueOf(ar.nextChars(2)), NutsTextNodeStyle.operator()));
                    break;
                }
                case ')': {
                    if (ar.peekChars(2).equals("))")) {
                        wasSpace = false;
                        all.add(factory.styled(String.valueOf(ar.nextChars(2)), NutsTextNodeStyle.separator()));
                        inLoop = false;
                    } else {
                        wasSpace = parseCommandLineStep(ar, all, 2, wasSpace, session);
                    }
                    break;
                }
                default: {
                    wasSpace = parseCommandLineStep(ar, all, 2, wasSpace, session);
                }
            }
        }
        return all.toArray(new NutsTextNode[0]);
    }

    private NutsTextNode[] parseCommandLine_readDollarCurlyBrackets(StringReaderExt ar, NutsSession session) {
        List<NutsTextNode> all = new ArrayList<>();
        factory.setSession(session);
        all.add(factory.styled(String.valueOf(ar.nextChar()) + ar.nextChar(), NutsTextNodeStyle.separator()));
        boolean inLoop = true;
        int startIndex = 0;
        boolean expectedName = true;
        boolean wasSpace = true;
        while (inLoop && ar.hasNext()) {
            char c = ar.peekChar();
            switch (c) {
                case '}': {
                    all.add(factory.styled(String.valueOf(ar.nextChar()), NutsTextNodeStyle.separator()));
                    inLoop = false;
                    break;
                }
                default: {
                    startIndex = all.size();
                    wasSpace = parseCommandLineStep(ar, all, -1, wasSpace, session);
                    if (expectedName) {
                        expectedName = false;
                        if (all.size() > startIndex) {
                            if (isWord(all.get(startIndex))) {
                                all.set(startIndex, factory.styled(all.get(startIndex), NutsTextNodeStyle.keyword(4)));
                                wasSpace = false;
                            }
                        }
                    }
                }
            }
        }
        return all.toArray(new NutsTextNode[0]);
    }

    private NutsTextNode[] parseCommandLine_readPar2(StringReaderExt ar, NutsSession session) {
        List<NutsTextNode> all = new ArrayList<>();
        factory.setSession(session);
        all.add(factory.styled(String.valueOf(ar.nextChar()) + ar.nextChar(), NutsTextNodeStyle.separator()));
        boolean inLoop = true;
        boolean wasSpace = true;
        while (inLoop && ar.hasNext()) {
            char c = ar.peekChar();
            switch (c) {
                case ')': {
                    if (ar.peekChars(2).equals("))")) {
                        all.add(factory.styled(String.valueOf(ar.nextChars(2)), NutsTextNodeStyle.separator()));
                        inLoop = false;
                    } else {
                        wasSpace = parseCommandLineStep(ar, all, 2, wasSpace, session);
                    }
                    break;
                }
                default: {
                    wasSpace = parseCommandLineStep(ar, all, 2, wasSpace, session);
                }
            }
        }
        return all.toArray(new NutsTextNode[0]);
    }

    /**
     * return is space
     *
     * @param ar ar
     * @param all all
     * @param startIndex startIndex
     * @param wasSpace wasSpace
     * @return is space
     */
    private boolean parseCommandLineStep(StringReaderExt ar, List<NutsTextNode> all, int startIndex, boolean wasSpace, NutsSession session) {
        char c = ar.peekChar();
        if (c <= 32) {
            all.addAll(Arrays.asList(StringReaderExtUtils.readSpaces(session, ar)));
            return true;
        }
        switch (c) {
            case '\'': {
                all.addAll(Arrays.asList(parseCommandLine_readSimpleQuotes(ar, session)));
                break;
            }
            case '`': {
                all.addAll(Arrays.asList(parseCommandLine_readAntiQuotes(ar, session)));
                break;
            }
            case '"': {
                all.addAll(Arrays.asList(parseCommandLine_readDoubleQuotes(ar, session)));
                break;
            }
            case '$': {
                all.addAll(Arrays.asList(parseCommandLine_readDollar(ar, session)));
                break;
            }
            case ';': {
                all.add(factory.styled(String.valueOf(ar.nextChar()), NutsTextNodeStyle.separator()));
                break;
            }
            case ':': {
                all.add(factory.styled(String.valueOf(ar.nextChar()), NutsTextNodeStyle.separator(2)));
                break;
            }
            case '|': {
                if (ar.peekChars(2).equals("||")) {
                    all.add(factory.styled(ar.nextChars(2), NutsTextNodeStyle.separator()));
                } else {
                    all.add(factory.styled(String.valueOf(ar.nextChar()), NutsTextNodeStyle.separator()));
                }
                break;
            }
            case '&': {
                if (ar.peekChars(2).equals("&&")) {
                    all.add(factory.styled(ar.nextChars(2), NutsTextNodeStyle.separator()));
                } else if (ar.peekChars(3).equals("&>>")) {
                    all.add(factory.styled(ar.nextChars(3), NutsTextNodeStyle.separator()));
                } else if (ar.peekChars(2).equals("&>")) {
                    all.add(factory.styled(ar.nextChars(2), NutsTextNodeStyle.separator()));
                } else {
                    all.add(factory.styled(String.valueOf(ar.nextChar()), NutsTextNodeStyle.separator()));
                }
                break;
            }
            case '>': {
                if (ar.peekChars(2).equals(">>")) {
                    all.add(factory.styled(ar.nextChars(2), NutsTextNodeStyle.separator()));
                } else if (ar.peekChars(2).equals(">&")) {
                    all.add(factory.styled(ar.nextChars(2), NutsTextNodeStyle.separator()));
                } else {
                    all.add(factory.styled(String.valueOf(ar.nextChar()), NutsTextNodeStyle.separator()));
                }
                break;
            }
            case '<': {
                if (ar.peekChars(2).equals("<<")) {
                    all.add(factory.styled(ar.nextChars(2), NutsTextNodeStyle.separator()));
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append(ar.peekChar(0));
                    boolean ok = true;
                    int i = 1;
                    while (ok && ar.isAvailable(i)) {
                        char c1 = ar.peekChar(i);
                        if (c1 == '>') {
                            sb.append(c1);
                            break;
                        } else if (c1 == '-' || c1 == '+' || Character.isAlphabetic(c1)) {
                            sb.append(c1);
                        } else {
                            ok = false;
                        }
                        i++;
                    }
                    if (sb.charAt(sb.length() - 1) != '>') {
                        ok = false;
                    }
                    if (ok) {
                        String s = ar.nextChars(sb.length());
                        String s0 = s.substring(1, s.length() - 1);
                        if (isSynopsysOption(s0)) {
                            all.add(factory.styled("<", NutsTextNodeStyle.input()));
                            all.add(factory.styled(s0, NutsTextNodeStyle.option()));
                            all.add(factory.styled(">", NutsTextNodeStyle.input()));
                        } else if (isSynopsysWord(s0)) {
                            all.add(factory.styled("<", NutsTextNodeStyle.input()));
                            all.add(factory.styled(s0, NutsTextNodeStyle.input()));
                            all.add(factory.styled(">", NutsTextNodeStyle.input()));
                        } else {
                            all.add(factory.styled(String.valueOf(ar.nextChar()), NutsTextNodeStyle.separator()));
                        }
                    } else {
                        all.add(factory.styled(String.valueOf(ar.nextChar()), NutsTextNodeStyle.separator()));
                    }
                }
                break;
            }
            case '(': {
                if (ar.peekChars("((")) {
                    all.addAll(Arrays.asList(parseCommandLine_readPar2(ar, session)));
                } else {
                    all.add(factory.styled(String.valueOf(ar.nextChar()), NutsTextNodeStyle.separator()));
                }
            }
            case ')':
            case '{':
            case '}':
            case '~':
            case '!': 
            {
                all.add(factory.styled(String.valueOf(ar.nextChar()), NutsTextNodeStyle.separator()));
                break;
            }
            case '*':
            case '?':
            case '[':
            case ']':
            case '=': {
                all.add(factory.styled(String.valueOf(ar.nextChar()), NutsTextNodeStyle.separator()));
                break;
            }
            case '#': {
                if (wasSpace) {
                    StringBuilder sb = new StringBuilder();
                    while (ar.hasNext()) {
                        c = ar.peekChar();
                        if (c == '\n') {
                            break;
                        } else if (c == '\r') {
                            break;
                        } else {
                            sb.append(ar.nextChar());
                        }
                    }
                    all.add(factory.styled(sb.toString(), NutsTextNodeStyle.comments()));
                } else {
                    all.add(factory.styled(String.valueOf(ar.nextChar()), NutsTextNodeStyle.separator()));
                }
                break;
            }
            default: {
                if (startIndex >= 0) {
                    boolean first = all.size() == startIndex;
                    all.addAll(Arrays.asList(parseCommandLine_readWord(ar, session)));
                    if (first) {
                        int i = indexOfFirstWord(all, startIndex);
                        if (i >= 0) {
                            all.set(i, factory.styled(all.get(i), NutsTextNodeStyle.keyword()));
                        }
                    }
                } else {
                    all.addAll(Arrays.asList(parseCommandLine_readWord(ar, session)));
                }
            }
        }
        return false;
    }

    private NutsTextNode[] parseCommandLine(String commandLineString, NutsSession session) {
        StringReaderExt ar = new StringReaderExt(commandLineString);
        List<NutsTextNode> all = new ArrayList<>();
        boolean wasSpace = true;
        while (ar.hasNext()) {
            wasSpace = parseCommandLineStep(ar, all, 0, wasSpace, session);
        }
        return all.toArray(new NutsTextNode[0]);
    }

    private static boolean isSynopsysOption(String s2) {
        return ((s2.startsWith("--") && isSynopsysWord(s2.substring(2)))
                || (s2.startsWith("++") && isSynopsysWord(s2.substring(2)))
                || (s2.startsWith("-") && isSynopsysWord(s2.substring(1)))
                || (s2.startsWith("+") && isSynopsysWord(s2.substring(1)))
                || (s2.startsWith("--!") && isSynopsysWord(s2.substring(3)))
                || (s2.startsWith("++!") && isSynopsysWord(s2.substring(3)))
                || (s2.startsWith("-!") && isSynopsysWord(s2.substring(2)))
                || (s2.startsWith("+!") && isSynopsysWord(s2.substring(2)))
                || (s2.startsWith("--~") && isSynopsysWord(s2.substring(3)))
                || (s2.startsWith("++~") && isSynopsysWord(s2.substring(3)))
                || (s2.startsWith("-~") && isSynopsysWord(s2.substring(2)))
                || (s2.startsWith("+~") && isSynopsysWord(s2.substring(2))));
    }

    private static boolean isOption(String s2) {
        return ((s2.startsWith("-"))
                || (s2.startsWith("+")));
    }

    private static boolean isSynopsysWord(String s) {
        if (s.length() > 0) {
            if (!Character.isAlphabetic(s.charAt(0))) {
                return false;
            }
            if (!Character.isAlphabetic(s.charAt(0))) {
                return false;
            }
            for (int i = 0; i < s.length(); i++) {
                if (Character.isAlphabetic(s.charAt(i))) {
                    //ok
                } else if (s.charAt(i) == '-') {
                    if (s.charAt(i - 1) == '-') {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            return true;
        }
        return true;
    }

    @Override
    public NutsTextNode textToNode(String text, NutsSession session) {
        factory.setSession(session);
        List<NutsTextNode> all = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new StringReader(text));
        String line = null;
        boolean first = true;
        while (true) {
            try {
                if ((line = reader.readLine()) == null) {
                    break;
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            if (first) {
                first = false;
            } else {
                all.add(factory.plain("\n"));
            }
            all.add(commandToNode(line, session));
        }
        return factory.list(all);
    }

    public NutsTextNode next(StringReaderExt reader, boolean exitOnClosedCurlBrace, boolean exitOnClosedPar, boolean exitOnDblQuote, boolean exitOnAntiQuote) {
        boolean lineStart = true;
        List<NutsTextNode> all = new ArrayList<>();
        NutsTextManager factory = ws.formats().text();
        boolean exit = false;
        while (!exit && reader.hasNext()) {
            switch (reader.peekChar()) {
                case '}': {
                    lineStart = false;
                    if (exitOnClosedCurlBrace) {
                        exit = true;
                    } else {
                        all.add(factory.styled(
                                reader.nextChars(1), NutsTextNodeStyle.separator()
                        ));
                    }
                    break;
                }
                case ')': {
                    lineStart = false;
                    if (exitOnClosedPar) {
                        exit = true;
                    } else {
                        all.add(factory.styled(
                                reader.nextChars(1), NutsTextNodeStyle.separator()
                        ));
                    }
                    break;
                }
                case '>': {
                    lineStart = false;
                    if (reader.isAvailable(2) && reader.peekChar() == '>') {
                        all.add(factory.styled(
                                reader.nextChars(2), NutsTextNodeStyle.separator()
                        ));
                    } else {
                        all.add(factory.styled(
                                reader.nextChars(1), NutsTextNodeStyle.separator()
                        ));
                    }
                    break;
                }
                case '&': {
                    lineStart = false;
                    if (reader.isAvailable(2) && reader.peekChar() == '&') {
                        all.add(factory.styled(
                                reader.nextChars(2), NutsTextNodeStyle.separator()
                        ));
                    } else if (reader.isAvailable(2) && reader.peekChar() == '>') {
                        all.add(factory.styled(
                                reader.nextChars(2), NutsTextNodeStyle.separator()
                        ));
                    } else if (reader.isAvailable(2) && reader.peekChar() == '<') {
                        all.add(factory.styled(
                                reader.nextChars(2), NutsTextNodeStyle.separator()
                        ));
                    } else {
                        all.add(factory.styled(
                                reader.nextChars(1), NutsTextNodeStyle.separator()
                        ));
                    }
                    break;
                }
                case '|': {
                    lineStart = false;
                    if (reader.isAvailable(2) && reader.peekChar() == '|') {
                        all.add(factory.styled(
                                reader.nextChars(2), NutsTextNodeStyle.separator()
                        ));
                    } else {
                        all.add(factory.styled(
                                reader.nextChars(1), NutsTextNodeStyle.separator()
                        ));
                    }
                    break;
                }
                case ';': {
                    all.add(factory.styled(
                            reader.nextChars(1), NutsTextNodeStyle.separator()
                    ));
                    lineStart = true;
                    break;
                }
                case '\n': {
                    if (reader.isAvailable(2) && reader.peekChar() == '\r') {
                        all.add(factory.styled(
                                reader.nextChars(2), NutsTextNodeStyle.separator()
                        ));
                    } else {
                        all.add(factory.styled(
                                reader.nextChars(1), NutsTextNodeStyle.separator()
                        ));
                    }
                    lineStart = true;
                    break;
                }
                case '<': {
                    lineStart = false;
                    StringBuilder sb = new StringBuilder();
                    if (reader.isAvailable(3)) {
                        int index = 0;
                        sb.append(reader.peekChar(index));
                        index++;
                        boolean ok = false;
                        while (reader.isAvailable(index)) {
                            char c = reader.peekChar(index);
                            if (c == '>') {
                                sb.append(c);
                                ok = true;
                                break;
                            } else if (Character.isAlphabetic(c) || c == '-') {
                                sb.append(c);
                            } else {
                                break;
                            }
                            index++;
                        }
                        if (ok) {
                            reader.nextChars(sb.length());
                            all.add(factory.styled(
                                    sb.toString(), NutsTextNodeStyle.input()
                            ));
                            break;
                        } else {
                            all.add(factory.styled(
                                    reader.nextChars(1), NutsTextNodeStyle.separator()
                            ));
                        }
                    } else if (reader.isAvailable(2) && reader.peekChar() == '<') {
                        all.add(factory.styled(
                                reader.nextChars(2), NutsTextNodeStyle.separator()
                        ));
                    } else {
                        all.add(factory.styled(
                                reader.nextChars(1), NutsTextNodeStyle.separator()
                        ));
                    }
                    break;
                }
                case '\\': {
                    lineStart = false;
                    all.add(factory.styled(
                            reader.nextChars(2), NutsTextNodeStyle.separator(2)
                    ));
                    break;
                }
                case '\"': {
                    lineStart = false;
                    all.add(nextDoubleQuotes(reader));
                    break;
                }
                case '`': {
                    lineStart = false;
                    if (exitOnAntiQuote) {
                        exit = true;
                    } else {
                        List<NutsTextNode> a = new ArrayList<>();
                        a.add(factory.styled(reader.nextChars(1), NutsTextNodeStyle.string()));
                        a.add(next(reader, false, false, false, true));
                        if (reader.hasNext() && reader.peekChar() == '`') {
                            a.add(factory.styled(reader.nextChars(1), NutsTextNodeStyle.string()));
                        } else {
                            exit = true;
                        }
                        all.add(factory.list(a));
                    }
                    break;
                }
                case '\'': {
                    lineStart = false;
                    StringBuilder sb = new StringBuilder();
                    sb.append(reader.nextChar());
                    boolean end = false;
                    while (!end && reader.hasNext()) {
                        switch (reader.peekChar()) {
                            case '\\': {
                                sb.append(reader.nextChars(2));
                                break;
                            }
                            case '\'': {
                                sb.append(reader.nextChar());
                                end = true;
                                break;
                            }
                            default: {
                                sb.append(reader.nextChar());
                                break;
                            }
                        }
                    }
                    all.add(factory.styled(sb.toString(), NutsTextNodeStyle.string()));
                    break;
                }
                case '$': {
                    lineStart = false;
                    if (reader.isAvailable(2)) {
                        char c = reader.peekChar(1);
                        switch (c) {
                            case '(': {
                                break;
                            }
                            case '{': {
                                break;
                            }
                            case '$':
                            case '*':
                            case '@':
                            case '-':
                            case '?':
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9': {
                                all.add(factory.styled(reader.nextChars(2), NutsTextNodeStyle.string()));
                                break;
                            }
                            default: {
                                if (Character.isAlphabetic(reader.peekChar(1))) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(reader.nextChar());
                                    while (reader.hasNext() && (Character.isAlphabetic(reader.peekChar()) || reader.peekChar() == '_')) {
                                        sb.append(reader.nextChar());
                                    }
                                    all.add(factory.styled(sb.toString(), NutsTextNodeStyle.variable()));
                                } else {
                                    all.add(factory.styled(reader.nextChars(1), NutsTextNodeStyle.separator()));
                                }
                            }
                        }
                    } else {
                        all.add(factory.styled(reader.nextChars(1), NutsTextNodeStyle.string()));
                    }
                    break;
                }
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                case 11:
                case 12:
                case 13:
                case 14:
                case 15:
                case 16:
                case 17:
                case 18:
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                case 24:
                case 25:
                case 26:
                case 27:
                case 28:
                case 29:
                case 30:
                case 31:
                case 33: {
                    StringBuilder whites = new StringBuilder();
                    while (reader.hasNext() && Character.isWhitespace(reader.peekChar())) {
                        whites.append(reader.nextChar());
                    }
                    all.add(factory.plain(whites.toString()));
                    break;
                }
                default: {
                    StringBuilder sb = new StringBuilder();
                    sb.append(reader.nextChar());
                    while (reader.hasNext()) {
                        char c2 = reader.peekChar();
                        boolean accept = true;
                        switch (c2) {
                            case '$':
                            case '<':
                            case '>':
                            case '&':
                            case '|':
                            case '{':
                            case '}':
                            case '(':
                            case ')':
                            case '[':
                            case ']':
                            case '*':
                            case '+':
                            case '?':
                            case '\\': {
                                accept = false;
                                break;
                            }
                            default: {
                                if (c2 <= 32) {
                                    accept = false;
                                } else {
                                    sb.append(reader.nextChar());
                                }
                            }
                        }
                        if (!accept) {
                            break;
                        }
                    }
                    if (lineStart && !reader.hasNext() || Character.isWhitespace(reader.peekChar())) {
                        //command name
                        NutsTextNodeStyle keyword1 = NutsTextNodeStyle.keyword(2);
                        switch (sb.toString()) {
                            case "if":
                            case "while":
                            case "do":
                            case "fi":
                            case "elif":
                            case "then":
                            case "else": {
                                keyword1 = NutsTextNodeStyle.keyword();
                                break;
                            }
                            case "cp":
                            case "ls":
                            case "ll":
                            case "rm":
                            case "pwd":
                            case "echo": {
                                keyword1 = NutsTextNodeStyle.keyword(3);
                                break;
                            }
                        }
                        all.add(factory.styled(sb.toString(), keyword1));
                    } else {
                        all.add(factory.plain(sb.toString()));
                    }
                    lineStart = false;
                    break;
                }
            }
        }
        return factory.list(all);
    }

    private NutsTextNode nextDollar(StringReaderExt reader) {
        NutsTextManager factory = ws.formats().text();
        if (reader.isAvailable(2)) {
            char c = reader.peekChar(1);
            switch (c) {
                case '(': {
                    List<NutsTextNode> a = new ArrayList<>();
                    a.add(factory.styled(reader.nextChars(1), NutsTextNodeStyle.separator()));
                    a.add(next(reader, false, true, false, false));
                    if (reader.hasNext() && reader.peekChar() == ')') {
                        a.add(factory.styled(reader.nextChars(1), NutsTextNodeStyle.separator()));
                    }
                    return factory.list(a);
                }
                case '{': {
                    List<NutsTextNode> a = new ArrayList<>();
                    a.add(factory.styled(reader.nextChars(1), NutsTextNodeStyle.separator()));
                    a.add(next(reader, true, false, false, false));
                    if (reader.hasNext() && reader.peekChar() == ')') {
                        a.add(factory.styled(reader.nextChars(1), NutsTextNodeStyle.separator()));
                    }
                    return factory.list(a);
                }
                case '$':
                case '*':
                case '@':
                case '-':
                case '?':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9': {
                    return factory.styled(reader.nextChars(2), NutsTextNodeStyle.string());
                }
                default: {
                    if (Character.isAlphabetic(reader.peekChar(1))) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(reader.nextChar());
                        while (reader.hasNext() && (Character.isAlphabetic(reader.peekChar()) || reader.peekChar() == '_')) {
                            sb.append(reader.nextChar());
                        }
                        return factory.styled(sb.toString(), NutsTextNodeStyle.variable());
                    } else {
                        return factory.styled(reader.nextChars(1), NutsTextNodeStyle.separator());
                    }
                }
            }
        } else {
            return factory.styled(reader.nextChars(1), NutsTextNodeStyle.string());
        }
    }

    public NutsTextNode nextDoubleQuotes(StringReaderExt reader) {
        List<NutsTextNode> all = new ArrayList<>();
        NutsTextManager factory = ws.formats().text();
        boolean exit = false;
        StringBuilder sb = new StringBuilder();
        sb.append(reader.nextChar());
        while (!exit && reader.hasNext()) {
            switch (reader.peekChar()) {
                case '\\': {
                    sb.append(reader.nextChars(2));
                    break;
                }
                case '\"': {
                    sb.append(reader.nextChars(1));
                    exit = true;
                    break;
                }
                case '$': {
                    if (sb.length() > 0) {
                        all.add(factory.styled(sb.toString(), NutsTextNodeStyle.string()));
                        sb.setLength(0);
                    }
                    all.add(nextDollar(reader));
                }
                case '`': {
                    if (sb.length() > 0) {
                        all.add(factory.styled(sb.toString(), NutsTextNodeStyle.string()));
                        sb.setLength(0);
                    }
                    List<NutsTextNode> a = new ArrayList<>();
                    a.add(factory.styled(reader.nextChars(1), NutsTextNodeStyle.string()));
                    a.add(next(reader, false, false, false, true));
                    if (reader.hasNext() && reader.peekChar() == '`') {
                        a.add(factory.styled(reader.nextChars(1), NutsTextNodeStyle.string()));
                    } else {
                        exit = true;
                    }
                    all.add(factory.list(a));
                    break;
                }
                default: {
                    sb.append(reader.nextChars(1));
                }
            }
        }
        if (sb.length() > 0) {
            all.add(factory.styled(sb.toString(), NutsTextNodeStyle.string()));
            sb.setLength(0);
        }
        return factory.list(all);
    }

    public NutsTextNode commandToNode(String text, NutsSession session) {
        factory.setSession(session);
        return factory.list(parseCommandLine(text, session));
    }

}
