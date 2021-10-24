package net.thevpc.nuts.runtime.core.format.text.bloc;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.parsers.StringReaderExt;
import net.thevpc.nuts.runtime.core.format.text.parser.DefaultNutsTextPlain;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsComponent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FishBlocTextFormatter implements NutsCodeFormat {

    private NutsWorkspace ws;
    private NutsTextManager factory;

    public FishBlocTextFormatter(NutsWorkspace ws) {
        this.ws = ws;
        factory = NutsWorkspaceUtils.defaultSession(ws).text();
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<String> context) {
        String s = context.getConstraints();
        switch (s) {
            case "fish": {
                return NutsComponent.DEFAULT_SUPPORT;
            }
            case "system": {
                switch (NutsShellFamily.getCurrent()) {
                    case FISH: {
                        return NutsComponent.DEFAULT_SUPPORT;
                    }
                }
            }
        }
        return NutsComponent.NO_SUPPORT;
    }

    @Override
    public NutsText tokenToText(String text, String nodeType, NutsSession session) {
        factory.setSession(session);
        return factory.ofPlain(text);
    }

    private NutsText[] parseCommandLine_readSimpleQuotes(StringReaderExt ar, NutsSession session) {
        StringBuilder sb = new StringBuilder();
        sb.append(ar.nextChar()); //quote!
        List<NutsText> ret = new ArrayList<>();
        while (ar.hasNext()) {
            char c = ar.peekChar();
            if (c == '\\') {
                StringBuilder sb2 = new StringBuilder();
                sb2.append(ar.nextChar());
                if (sb.length() > 0) {
                    ret.add(factory.ofStyled(sb.toString(), NutsTextStyle.string(2)));
                    sb.setLength(0);
                }
                if (ar.hasNext()) {
                    sb2.append(ar.nextChar());
                }
                ret.add(factory.ofStyled(sb2.toString(), NutsTextStyle.separator()));
                break;
            } else if (c == '\'') {
                sb.append(ar.nextChar());
                break;
            } else {
                sb.append(ar.nextChar());
            }
        }
        if (sb.length() > 0) {
            ret.add(factory.ofStyled(sb.toString(), NutsTextStyle.string(2)));
            sb.setLength(0);
        }
        return ret.toArray(new NutsText[0]);
    }

    private NutsText[] parseCommandLine_readWord(StringReaderExt ar, NutsSession session) {
        StringBuilder sb = new StringBuilder();
        List<NutsText> ret = new ArrayList<>();
        boolean inLoop = true;
        boolean endsWithSep = false;
        while (inLoop && ar.hasNext()) {
            char c = ar.peekChar();
            switch (c) {
                case '\\': {
                    if (sb.length() > 0) {
                        ret.add(factory.ofPlain(sb.toString()));
                        sb.setLength(0);
                    }
                    ret.addAll(Arrays.asList(parseCommandLine_readAntiSlash(ar, session)));
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
                case '!': {
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
            ret.add(factory.ofPlain(sb.toString()));
            sb.setLength(0);
        }
        if (ret.isEmpty()) {
            throw new IllegalArgumentException("was not expecting " + ar.peekChar() + " as part of word");
        }
        if (ret.get(0).getType() == NutsTextType.PLAIN && isOption(((NutsTextPlain) ret.get(0)).getText())) {
            ret.set(0, factory.ofStyled(ret.get(0), NutsTextStyle.option()));
        }
        return ret.toArray(new NutsText[0]);
    }

    private static NutsText[] parseCommandLine_readAntiSlash(StringReaderExt ar, NutsSession session) {
        StringBuilder sb2 = new StringBuilder();
        sb2.append(ar.nextChar());
        if (ar.hasNext()) {
            sb2.append(ar.nextChar());
        }
        NutsTextManager factory = session.text();
        return new NutsText[]{factory.ofStyled(sb2.toString(), NutsTextStyle.separator())};
    }

    private NutsText[] parseCommandLine_readDollar(StringReaderExt ar, NutsSession session) {
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
                    return new NutsText[]{factory.ofStyled(sb2.toString(), NutsTextStyle.separator())};
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
            return new NutsText[]{
                    factory.ofStyled("$", NutsTextStyle.separator()),
                    factory.ofStyled(sb2.toString(), NutsTextStyle.keyword(4)),};
        }
        return new NutsText[]{
                factory.ofStyled("$", NutsTextStyle.separator()),};
    }

    private NutsText[] parseCommandLine_readDoubleQuotes(StringReaderExt ar, NutsSession session) {
        List<NutsText> ret = new ArrayList<>();
        factory.setSession(session);
        StringBuilder sb = new StringBuilder();

        ret.add(factory.ofStyled(String.valueOf(ar.nextChar()), NutsTextStyle.string()));
        while (ar.hasNext()) {
            char c = ar.peekChar();
            if (c == '\\') {
                if (sb.length() > 0) {
                    ret.add(factory.ofStyled(sb.toString(), NutsTextStyle.string()));
                    sb.setLength(0);
                }
                ret.addAll(Arrays.asList(parseCommandLine_readAntiSlash(ar, session)));
            } else if (c == '$') {
                if (sb.length() > 0) {
                    ret.add(factory.ofStyled(sb.toString(), NutsTextStyle.string()));
                    sb.setLength(0);
                }
                ret.addAll(Arrays.asList(parseCommandLine_readDollar(ar, session)));
            } else if (c == '\"') {
                if (sb.length() > 0) {
                    ret.add(factory.ofStyled(sb.toString(), NutsTextStyle.string()));
                    sb.setLength(0);
                }
                ret.add(factory.ofStyled(String.valueOf(ar.nextChar()), NutsTextStyle.string()));
                break;
            } else {
                sb.append(ar.nextChar());
            }
        }
        if (sb.length() > 0) {
            ret.add(factory.ofStyled(sb.toString(), NutsTextStyle.string()));
            sb.setLength(0);
        }
        return ret.toArray(new NutsText[0]);
    }



    private enum TokenType{
        ENV,WORD,SPACE,QUOTES,SEPARATORS,OTHER,EMPTY
    }

    private static TokenType resolveTokenType(NutsText n) {
        if (n instanceof DefaultNutsTextPlain) {
            String text = ((DefaultNutsTextPlain) n).getText();
            if (text.length() > 0) {
                char c = text.charAt(0);
                switch (c) {
                    case '\"':
                    case '\'':
                    case '`': {
                        return TokenType.QUOTES;
                    }
                    case ';':
                    case '&':
                    case '|':
                    case '(':
                    case ')':
                    case '[':
                    case ']':
                    case ',':
                        return TokenType.SEPARATORS;
                    default: {
                        if (Character.isWhitespace(c)) {
                            return TokenType.SPACE;
                        }
                        return TokenType.WORD;
                    }
                }
            }else{
                return TokenType.EMPTY;
            }
        }
        return TokenType.OTHER;
    }

    private static boolean isWhites(NutsText n) {
        if (n instanceof DefaultNutsTextPlain) {
            if (Character.isWhitespace(((DefaultNutsTextPlain) n).getText().charAt(0))) {
                return true;
            }
        }
        return false;
    }

    private static int indexOfFirstWord(List<NutsText> all, int from) {
        for (int i = from; i < all.size(); i++) {
            NutsText n = all.get(i);
            switch (resolveTokenType(n)){
                case SPACE:
                case SEPARATORS:
                case ENV:{
                    break;
                }
                case WORD:{
                    if (i == all.size() - 1) {
                        return i;
                    }
                    NutsText p = all.get(i + 1);
                    switch (resolveTokenType(n)) {
                        case SPACE:
                        case SEPARATORS:{
                            return i;
                        }
                    }
                    break;
                }
            }
        }
        return -1;
    }

    private NutsText[] parseCommandLine_readAntiQuotes(StringReaderExt ar, NutsSession session) {
        List<NutsText> all = new ArrayList<>();
        factory.setSession(session);
        all.add(factory.ofStyled(String.valueOf(ar.nextChar()), NutsTextStyle.separator()));
        boolean inLoop = true;
        boolean wasSpace = true;
        while (inLoop && ar.hasNext()) {
            char c = ar.peekChar();
            switch (c) {
                case '`': {
                    wasSpace = false;
                    all.add(factory.ofStyled(String.valueOf(ar.nextChar()), NutsTextStyle.separator()));
                    inLoop = false;
                    break;
                }
                default: {
                    wasSpace = parseCommandLineStep(ar, all, 1, wasSpace, session);
                }
            }
        }
        return all.toArray(new NutsText[0]);
    }

    private NutsText[] parseCommandLine_readDollarPar(NutsWorkspace ws, StringReaderExt ar, NutsSession session) {
        List<NutsText> all = new ArrayList<>();
        factory.setSession(session);
        all.add(factory.ofStyled(String.valueOf(ar.nextChar()) + ar.nextChar(), NutsTextStyle.separator()));
        boolean inLoop = true;
        boolean wasSpace = false;
        while (inLoop && ar.hasNext()) {
            char c = ar.peekChar();
            switch (c) {
                case ')': {
                    all.add(factory.ofStyled(String.valueOf(ar.nextChar()), NutsTextStyle.separator()));
                    inLoop = false;
                    break;
                }
                default: {
                    wasSpace = parseCommandLineStep(ar, all, 2, wasSpace, session);
                }
            }
        }
        return all.toArray(new NutsText[0]);
    }

    private NutsText[] parseCommandLine_readDollarPar2(StringReaderExt ar, NutsSession session) {
        List<NutsText> all = new ArrayList<>();
        factory.setSession(session);
        all.add(factory.ofStyled(String.valueOf(ar.nextChar()) + ar.nextChar() + ar.nextChar(), NutsTextStyle.separator()));
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
                    all.add(factory.ofStyled(String.valueOf(ar.nextChars(2)), NutsTextStyle.operator()));
                    break;
                }
                case ')': {
                    if (ar.peekChars(2).equals("))")) {
                        wasSpace = false;
                        all.add(factory.ofStyled(String.valueOf(ar.nextChars(2)), NutsTextStyle.separator()));
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
        return all.toArray(new NutsText[0]);
    }

    private NutsText[] parseCommandLine_readDollarCurlyBrackets(StringReaderExt ar, NutsSession session) {
        List<NutsText> all = new ArrayList<>();
        factory.setSession(session);
        all.add(factory.ofStyled(String.valueOf(ar.nextChar()) + ar.nextChar(), NutsTextStyle.separator()));
        boolean inLoop = true;
        int startIndex = 0;
        boolean expectedName = true;
        boolean wasSpace = true;
        while (inLoop && ar.hasNext()) {
            char c = ar.peekChar();
            switch (c) {
                case '}': {
                    all.add(factory.ofStyled(String.valueOf(ar.nextChar()), NutsTextStyle.separator()));
                    inLoop = false;
                    break;
                }
                default: {
                    startIndex = all.size();
                    wasSpace = parseCommandLineStep(ar, all, -1, wasSpace, session);
                    if (expectedName) {
                        expectedName = false;
                        if (all.size() > startIndex) {
                            TokenType t = resolveTokenType(all.get(startIndex));
                            if (t== TokenType.ENV || t== TokenType.WORD) {
                                all.set(startIndex, factory.ofStyled(all.get(startIndex), NutsTextStyle.keyword(4)));
                                wasSpace = false;
                            }
                        }
                    }
                }
            }
        }
        return all.toArray(new NutsText[0]);
    }

    private NutsText[] parseCommandLine_readPar2(StringReaderExt ar, NutsSession session) {
        List<NutsText> all = new ArrayList<>();
        factory.setSession(session);
        all.add(factory.ofStyled(String.valueOf(ar.nextChar()) + ar.nextChar(), NutsTextStyle.separator()));
        boolean inLoop = true;
        boolean wasSpace = true;
        while (inLoop && ar.hasNext()) {
            char c = ar.peekChar();
            switch (c) {
                case ')': {
                    if (ar.peekChars(2).equals("))")) {
                        all.add(factory.ofStyled(String.valueOf(ar.nextChars(2)), NutsTextStyle.separator()));
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
        return all.toArray(new NutsText[0]);
    }

    /**
     * return is space
     *
     * @param ar         ar
     * @param all        all
     * @param startIndex startIndex
     * @param wasSpace   wasSpace
     * @return is space
     */
    private boolean parseCommandLineStep(StringReaderExt ar, List<NutsText> all, int startIndex, boolean wasSpace, NutsSession session) {
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
                all.add(factory.ofStyled(String.valueOf(ar.nextChar()), NutsTextStyle.separator()));
                break;
            }
            case ':': {
                all.add(factory.ofStyled(String.valueOf(ar.nextChar()), NutsTextStyle.separator(2)));
                break;
            }
            case '|': {
                if (ar.peekChars(2).equals("||")) {
                    all.add(factory.ofStyled(ar.nextChars(2), NutsTextStyle.separator()));
                } else {
                    all.add(factory.ofStyled(String.valueOf(ar.nextChar()), NutsTextStyle.separator()));
                }
                break;
            }
            case '&': {
                if (ar.peekChars(2).equals("&&")) {
                    all.add(factory.ofStyled(ar.nextChars(2), NutsTextStyle.separator()));
                } else if (ar.peekChars(3).equals("&>>")) {
                    all.add(factory.ofStyled(ar.nextChars(3), NutsTextStyle.separator()));
                } else if (ar.peekChars(2).equals("&>")) {
                    all.add(factory.ofStyled(ar.nextChars(2), NutsTextStyle.separator()));
                } else {
                    all.add(factory.ofStyled(String.valueOf(ar.nextChar()), NutsTextStyle.separator()));
                }
                break;
            }
            case '>': {
                if (ar.peekChars(2).equals(">>")) {
                    all.add(factory.ofStyled(ar.nextChars(2), NutsTextStyle.separator()));
                } else if (ar.peekChars(2).equals(">&")) {
                    all.add(factory.ofStyled(ar.nextChars(2), NutsTextStyle.separator()));
                } else {
                    all.add(factory.ofStyled(String.valueOf(ar.nextChar()), NutsTextStyle.separator()));
                }
                break;
            }
            case '<': {
                if (ar.peekChars(2).equals("<<")) {
                    all.add(factory.ofStyled(ar.nextChars(2), NutsTextStyle.separator()));
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
                            all.add(factory.ofStyled("<", NutsTextStyle.input()));
                            all.add(factory.ofStyled(s0, NutsTextStyle.option()));
                            all.add(factory.ofStyled(">", NutsTextStyle.input()));
                        } else if (isSynopsysWord(s0)) {
                            all.add(factory.ofStyled("<", NutsTextStyle.input()));
                            all.add(factory.ofStyled(s0, NutsTextStyle.input()));
                            all.add(factory.ofStyled(">", NutsTextStyle.input()));
                        } else {
                            all.add(factory.ofStyled(String.valueOf(ar.nextChar()), NutsTextStyle.separator()));
                        }
                    } else {
                        all.add(factory.ofStyled(String.valueOf(ar.nextChar()), NutsTextStyle.separator()));
                    }
                }
                break;
            }
            case '(': {
                if (ar.peekChars("((")) {
                    all.addAll(Arrays.asList(parseCommandLine_readPar2(ar, session)));
                } else {
                    all.add(factory.ofStyled(String.valueOf(ar.nextChar()), NutsTextStyle.separator()));
                }
            }
            case ')':
            case '{':
            case '}':
            case '~':
            case '!': {
                all.add(factory.ofStyled(String.valueOf(ar.nextChar()), NutsTextStyle.separator()));
                break;
            }
            case '*':
            case '?':
            case '[':
            case ']':
            case '=': {
                all.add(factory.ofStyled(String.valueOf(ar.nextChar()), NutsTextStyle.separator()));
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
                    all.add(factory.ofStyled(sb.toString(), NutsTextStyle.comments()));
                } else {
                    all.add(factory.ofStyled(String.valueOf(ar.nextChar()), NutsTextStyle.separator()));
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
                            all.set(i, factory.ofStyled(all.get(i), NutsTextStyle.keyword()));
                        }
                    }
                } else {
                    all.addAll(Arrays.asList(parseCommandLine_readWord(ar, session)));
                }
            }
        }
        return false;
    }

    private NutsText[] parseCommandLine(String commandLineString, NutsSession session) {
        StringReaderExt ar = new StringReaderExt(commandLineString);
        List<NutsText> all = new ArrayList<>();
        boolean wasSpace = true;
        while (ar.hasNext()) {
            wasSpace = parseCommandLineStep(ar, all, 0, wasSpace, session);
        }
        return all.toArray(new NutsText[0]);
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
    public NutsText stringToText(String text, NutsSession session) {
        factory.setSession(session);
        List<NutsText> all = new ArrayList<>();
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
                all.add(factory.ofPlain("\n"));
            }
            all.add(commandToNode(line, session));
        }
        return factory.ofList(all).simplify();
    }

    public NutsText next(StringReaderExt reader, boolean exitOnClosedCurlBrace, boolean exitOnClosedPar, boolean exitOnDblQuote, boolean exitOnAntiQuote, NutsSession session) {
        boolean lineStart = true;
        List<NutsText> all = new ArrayList<>();
        NutsTextManager factory = session.text();
        boolean exit = false;
        while (!exit && reader.hasNext()) {
            switch (reader.peekChar()) {
                case '}': {
                    lineStart = false;
                    if (exitOnClosedCurlBrace) {
                        exit = true;
                    } else {
                        all.add(factory.ofStyled(
                                reader.nextChars(1), NutsTextStyle.separator()
                        ));
                    }
                    break;
                }
                case ')': {
                    lineStart = false;
                    if (exitOnClosedPar) {
                        exit = true;
                    } else {
                        all.add(factory.ofStyled(
                                reader.nextChars(1), NutsTextStyle.separator()
                        ));
                    }
                    break;
                }
                case '>': {
                    lineStart = false;
                    if (reader.isAvailable(2) && reader.peekChar() == '>') {
                        all.add(factory.ofStyled(
                                reader.nextChars(2), NutsTextStyle.separator()
                        ));
                    } else {
                        all.add(factory.ofStyled(
                                reader.nextChars(1), NutsTextStyle.separator()
                        ));
                    }
                    break;
                }
                case '&': {
                    lineStart = false;
                    if (reader.isAvailable(2) && reader.peekChar() == '&') {
                        all.add(factory.ofStyled(
                                reader.nextChars(2), NutsTextStyle.separator()
                        ));
                    } else if (reader.isAvailable(2) && reader.peekChar() == '>') {
                        all.add(factory.ofStyled(
                                reader.nextChars(2), NutsTextStyle.separator()
                        ));
                    } else if (reader.isAvailable(2) && reader.peekChar() == '<') {
                        all.add(factory.ofStyled(
                                reader.nextChars(2), NutsTextStyle.separator()
                        ));
                    } else {
                        all.add(factory.ofStyled(
                                reader.nextChars(1), NutsTextStyle.separator()
                        ));
                    }
                    break;
                }
                case '|': {
                    lineStart = false;
                    if (reader.isAvailable(2) && reader.peekChar() == '|') {
                        all.add(factory.ofStyled(
                                reader.nextChars(2), NutsTextStyle.separator()
                        ));
                    } else {
                        all.add(factory.ofStyled(
                                reader.nextChars(1), NutsTextStyle.separator()
                        ));
                    }
                    break;
                }
                case ';': {
                    all.add(factory.ofStyled(
                            reader.nextChars(1), NutsTextStyle.separator()
                    ));
                    lineStart = true;
                    break;
                }
                case '\n': {
                    if (reader.isAvailable(2) && reader.peekChar() == '\r') {
                        all.add(factory.ofStyled(
                                reader.nextChars(2), NutsTextStyle.separator()
                        ));
                    } else {
                        all.add(factory.ofStyled(
                                reader.nextChars(1), NutsTextStyle.separator()
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
                            all.add(factory.ofStyled(
                                    sb.toString(), NutsTextStyle.input()
                            ));
                            break;
                        } else {
                            all.add(factory.ofStyled(
                                    reader.nextChars(1), NutsTextStyle.separator()
                            ));
                        }
                    } else if (reader.isAvailable(2) && reader.peekChar() == '<') {
                        all.add(factory.ofStyled(
                                reader.nextChars(2), NutsTextStyle.separator()
                        ));
                    } else {
                        all.add(factory.ofStyled(
                                reader.nextChars(1), NutsTextStyle.separator()
                        ));
                    }
                    break;
                }
                case '\\': {
                    lineStart = false;
                    all.add(factory.ofStyled(
                            reader.nextChars(2), NutsTextStyle.separator(2)
                    ));
                    break;
                }
                case '\"': {
                    lineStart = false;
                    all.add(nextDoubleQuotes(reader, session));
                    break;
                }
                case '`': {
                    lineStart = false;
                    if (exitOnAntiQuote) {
                        exit = true;
                    } else {
                        List<NutsText> a = new ArrayList<>();
                        a.add(factory.ofStyled(reader.nextChars(1), NutsTextStyle.string()));
                        a.add(next(reader, false, false, false, true, session));
                        if (reader.hasNext() && reader.peekChar() == '`') {
                            a.add(factory.ofStyled(reader.nextChars(1), NutsTextStyle.string()));
                        } else {
                            exit = true;
                        }
                        all.add(factory.ofList(a).simplify());
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
                    all.add(factory.ofStyled(sb.toString(), NutsTextStyle.string()));
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
                                all.add(factory.ofStyled(reader.nextChars(2), NutsTextStyle.string()));
                                break;
                            }
                            default: {
                                if (Character.isAlphabetic(reader.peekChar(1))) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(reader.nextChar());
                                    while (reader.hasNext() && (Character.isAlphabetic(reader.peekChar()) || reader.peekChar() == '_')) {
                                        sb.append(reader.nextChar());
                                    }
                                    all.add(factory.ofStyled(sb.toString(), NutsTextStyle.variable()));
                                } else {
                                    all.add(factory.ofStyled(reader.nextChars(1), NutsTextStyle.separator()));
                                }
                            }
                        }
                    } else {
                        all.add(factory.ofStyled(reader.nextChars(1), NutsTextStyle.string()));
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
                    all.add(factory.ofPlain(whites.toString()));
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
                        NutsTextStyle keyword1 = NutsTextStyle.keyword(2);
                        switch (sb.toString()) {
                            case "if":
                            case "while":
                            case "do":
                            case "fi":
                            case "elif":
                            case "then":
                            case "else": {
                                keyword1 = NutsTextStyle.keyword();
                                break;
                            }
                            case "cp":
                            case "ls":
                            case "ll":
                            case "rm":
                            case "pwd":
                            case "echo": {
                                keyword1 = NutsTextStyle.keyword(3);
                                break;
                            }
                        }
                        all.add(factory.ofStyled(sb.toString(), keyword1));
                    } else {
                        all.add(factory.ofPlain(sb.toString()));
                    }
                    lineStart = false;
                    break;
                }
            }
        }
        return factory.ofList(all).simplify();
    }

    private NutsText nextDollar(StringReaderExt reader, NutsSession session) {
        NutsTextManager factory = session.text();
        if (reader.isAvailable(2)) {
            char c = reader.peekChar(1);
            switch (c) {
                case '(': {
                    List<NutsText> a = new ArrayList<>();
                    a.add(factory.ofStyled(reader.nextChars(1), NutsTextStyle.separator()));
                    a.add(next(reader, false, true, false, false, session));
                    if (reader.hasNext() && reader.peekChar() == ')') {
                        a.add(factory.ofStyled(reader.nextChars(1), NutsTextStyle.separator()));
                    }
                    return factory.ofList(a).simplify();
                }
                case '{': {
                    List<NutsText> a = new ArrayList<>();
                    a.add(factory.ofStyled(reader.nextChars(1), NutsTextStyle.separator()));
                    a.add(next(reader, true, false, false, false, session));
                    if (reader.hasNext() && reader.peekChar() == ')') {
                        a.add(factory.ofStyled(reader.nextChars(1), NutsTextStyle.separator()));
                    }
                    return factory.ofList(a).simplify();
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
                    return factory.ofStyled(reader.nextChars(2), NutsTextStyle.string());
                }
                default: {
                    if (Character.isAlphabetic(reader.peekChar(1))) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(reader.nextChar());
                        while (reader.hasNext() && (Character.isAlphabetic(reader.peekChar()) || reader.peekChar() == '_')) {
                            sb.append(reader.nextChar());
                        }
                        return factory.ofStyled(sb.toString(), NutsTextStyle.variable());
                    } else {
                        return factory.ofStyled(reader.nextChars(1), NutsTextStyle.separator());
                    }
                }
            }
        } else {
            return factory.ofStyled(reader.nextChars(1), NutsTextStyle.string());
        }
    }

    public NutsText nextDoubleQuotes(StringReaderExt reader, NutsSession session) {
        List<NutsText> all = new ArrayList<>();
        NutsTextManager factory = session.text();
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
                        all.add(factory.ofStyled(sb.toString(), NutsTextStyle.string()));
                        sb.setLength(0);
                    }
                    all.add(nextDollar(reader, session));
                }
                case '`': {
                    if (sb.length() > 0) {
                        all.add(factory.ofStyled(sb.toString(), NutsTextStyle.string()));
                        sb.setLength(0);
                    }
                    List<NutsText> a = new ArrayList<>();
                    a.add(factory.ofStyled(reader.nextChars(1), NutsTextStyle.string()));
                    a.add(next(reader, false, false, false, true, session));
                    if (reader.hasNext() && reader.peekChar() == '`') {
                        a.add(factory.ofStyled(reader.nextChars(1), NutsTextStyle.string()));
                    } else {
                        exit = true;
                    }
                    all.add(factory.ofList(a).simplify());
                    break;
                }
                default: {
                    sb.append(reader.nextChars(1));
                }
            }
        }
        if (sb.length() > 0) {
            all.add(factory.ofStyled(sb.toString(), NutsTextStyle.string()));
            sb.setLength(0);
        }
        return factory.ofList(all).simplify();
    }

    public NutsText commandToNode(String text, NutsSession session) {
        factory.setSession(session);
        return factory.ofList(parseCommandLine(text, session));
    }

}
