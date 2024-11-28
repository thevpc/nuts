package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringReaderExt;
import net.thevpc.nuts.runtime.standalone.text.parser.DefaultNTextPlain;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FishCodeHighlighter implements NCodeHighlighter {

    private NWorkspace workspace;
    public FishCodeHighlighter(NWorkspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public String getId() {
        return "fish";
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        String s = context.getConstraints();
        if(s==null){
            return NConstants.Support.DEFAULT_SUPPORT;
        }
        switch (s) {
            case "fish": {
                return NConstants.Support.DEFAULT_SUPPORT;
            }
            case "system": {
                switch (NShellFamily.getCurrent()) {
                    case FISH: {
                        return NConstants.Support.DEFAULT_SUPPORT;
                    }
                }
            }
        }
        return NConstants.Support.NO_SUPPORT;
    }

    @Override
    public NText tokenToText(String text, String nodeType, NTexts txt) {
        return txt.ofPlain(text);
    }

    private NText[] parseCmdLine_readSimpleQuotes(StringReaderExt ar, NTexts txt) {
        StringBuilder sb = new StringBuilder();
        sb.append(ar.readChar()); //quote!
        List<NText> ret = new ArrayList<>();
        while (ar.hasNext()) {
            char c = ar.peekChar();
            if (c == '\\') {
                StringBuilder sb2 = new StringBuilder();
                sb2.append(ar.readChar());
                if (sb.length() > 0) {
                    ret.add(txt.ofStyled(sb.toString(), NTextStyle.string(2)));
                    sb.setLength(0);
                }
                if (ar.hasNext()) {
                    sb2.append(ar.readChar());
                }
                ret.add(txt.ofStyled(sb2.toString(), NTextStyle.separator()));
                break;
            } else if (c == '\'') {
                sb.append(ar.readChar());
                break;
            } else {
                sb.append(ar.readChar());
            }
        }
        if (sb.length() > 0) {
            ret.add(txt.ofStyled(sb.toString(), NTextStyle.string(2)));
            sb.setLength(0);
        }
        return ret.toArray(new NText[0]);
    }

    private NText[] parseCmdLine_readWord(StringReaderExt ar, NTexts txt) {
        StringBuilder sb = new StringBuilder();
        List<NText> ret = new ArrayList<>();
        boolean inLoop = true;
        boolean endsWithSep = false;
        while (inLoop && ar.hasNext()) {
            char c = ar.peekChar();
            switch (c) {
                case '\\': {
                    if (sb.length() > 0) {
                        ret.add(txt.ofPlain(sb.toString()));
                        sb.setLength(0);
                    }
                    ret.addAll(Arrays.asList(parseCmdLine_readAntiSlash(ar)));
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
                        sb.append(ar.readChar());
                    }
                }
            }
        }
        if (sb.length() > 0) {
            ret.add(txt.ofPlain(sb.toString()));
            sb.setLength(0);
        }
        if (ret.isEmpty()) {
            throw new IllegalArgumentException("was not expecting " + ar.peekChar() + " as part of word");
        }
        if (ret.get(0).getType() == NTextType.PLAIN && isOption(((NTextPlain) ret.get(0)).getText())) {
            ret.set(0, txt.ofStyled(ret.get(0), NTextStyle.option()));
        }
        return ret.toArray(new NText[0]);
    }

    private NText[] parseCmdLine_readAntiSlash(StringReaderExt ar) {
        StringBuilder sb2 = new StringBuilder();
        sb2.append(ar.readChar());
        if (ar.hasNext()) {
            sb2.append(ar.readChar());
        }
        NSession session = workspace.currentSession();
        NTexts txt = NTexts.of();
        return new NText[]{txt.ofStyled(sb2.toString(), NTextStyle.separator())};
    }

    private NText[] parseCmdLine_readDollar(StringReaderExt ar, NTexts txt) {
        if (ar.peekChars("$((")) {
            return parseCmdLine_readDollarPar2(ar, txt);
        }
        StringBuilder sb2 = new StringBuilder();
        if (ar.hasNext(1)) {
            switch (ar.peekChar(1)) {
                case '(': {
                    return parseCmdLine_readDollarPar2(ar, txt);
                }
                case '{': {
                    return parseCmdLine_readDollarCurlyBrackets(ar, txt);
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
                    sb2.append(ar.readChar());
                    sb2.append(ar.readChar());
                    return new NText[]{txt.ofStyled(sb2.toString(), NTextStyle.separator())};
                }
            }
        }
        ar.readChar();
        while (ar.hasNext()) {
            char c = ar.peekChar();
            if (Character.isAlphabetic(c) || Character.isDigit(c) || c == '_') {
                sb2.append(ar.readChar());
            } else {
                break;
            }
        }
        if (sb2.length() > 0) {
            return new NText[]{
                    txt.ofStyled("$", NTextStyle.separator()),
                    txt.ofStyled(sb2.toString(), NTextStyle.keyword(4)),};
        }
        return new NText[]{
                txt.ofStyled("$", NTextStyle.separator()),};
    }

    private NText[] parseCmdLine_readDoubleQuotes(StringReaderExt ar, NTexts txt) {
        List<NText> ret = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        ret.add(txt.ofStyled(String.valueOf(ar.readChar()), NTextStyle.string()));
        while (ar.hasNext()) {
            char c = ar.peekChar();
            if (c == '\\') {
                if (sb.length() > 0) {
                    ret.add(txt.ofStyled(sb.toString(), NTextStyle.string()));
                    sb.setLength(0);
                }
                ret.addAll(Arrays.asList(parseCmdLine_readAntiSlash(ar)));
            } else if (c == '$') {
                if (sb.length() > 0) {
                    ret.add(txt.ofStyled(sb.toString(), NTextStyle.string()));
                    sb.setLength(0);
                }
                ret.addAll(Arrays.asList(parseCmdLine_readDollar(ar, txt)));
            } else if (c == '\"') {
                if (sb.length() > 0) {
                    ret.add(txt.ofStyled(sb.toString(), NTextStyle.string()));
                    sb.setLength(0);
                }
                ret.add(txt.ofStyled(String.valueOf(ar.readChar()), NTextStyle.string()));
                break;
            } else {
                sb.append(ar.readChar());
            }
        }
        if (sb.length() > 0) {
            ret.add(txt.ofStyled(sb.toString(), NTextStyle.string()));
            sb.setLength(0);
        }
        return ret.toArray(new NText[0]);
    }



    private enum TokenType{
        ENV,WORD,SPACE,QUOTES,SEPARATORS,OTHER,EMPTY
    }

    private static TokenType resolveTokenType(NText n) {
        if (n instanceof DefaultNTextPlain) {
            String text = ((DefaultNTextPlain) n).getText();
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

    private static boolean isWhites(NText n) {
        if (n instanceof DefaultNTextPlain) {
            if (Character.isWhitespace(((DefaultNTextPlain) n).getText().charAt(0))) {
                return true;
            }
        }
        return false;
    }

    private static int indexOfFirstWord(List<NText> all, int from) {
        for (int i = from; i < all.size(); i++) {
            NText n = all.get(i);
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
                    NText p = all.get(i + 1);
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

    private NText[] parseCmdLine_readAntiQuotes(StringReaderExt ar, NTexts txt) {
        List<NText> all = new ArrayList<>();
        all.add(txt.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
        boolean inLoop = true;
        boolean wasSpace = true;
        while (inLoop && ar.hasNext()) {
            char c = ar.peekChar();
            switch (c) {
                case '`': {
                    wasSpace = false;
                    all.add(txt.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
                    inLoop = false;
                    break;
                }
                default: {
                    wasSpace = parseCmdLineStep(ar, all, 1, wasSpace, txt);
                }
            }
        }
        return all.toArray(new NText[0]);
    }

    private NText[] parseCmdLine_readDollarPar(NWorkspace ws, StringReaderExt ar, NTexts txt) {
        List<NText> all = new ArrayList<>();
        all.add(txt.ofStyled(String.valueOf(ar.readChar()) + ar.readChar(), NTextStyle.separator()));
        boolean inLoop = true;
        boolean wasSpace = false;
        while (inLoop && ar.hasNext()) {
            char c = ar.peekChar();
            switch (c) {
                case ')': {
                    all.add(txt.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
                    inLoop = false;
                    break;
                }
                default: {
                    wasSpace = parseCmdLineStep(ar, all, 2, wasSpace, txt);
                }
            }
        }
        return all.toArray(new NText[0]);
    }

    private NText[] parseCmdLine_readDollarPar2(StringReaderExt ar, NTexts txt) {
        List<NText> all = new ArrayList<>();
        all.add(txt.ofStyled(String.valueOf(ar.readChar()) + ar.readChar() + ar.readChar(), NTextStyle.separator()));
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
                    all.add(txt.ofStyled(String.valueOf(ar.nextChars(2)), NTextStyle.operator()));
                    break;
                }
                case ')': {
                    if (ar.peekChars(2).equals("))")) {
                        wasSpace = false;
                        all.add(txt.ofStyled(String.valueOf(ar.nextChars(2)), NTextStyle.separator()));
                        inLoop = false;
                    } else {
                        wasSpace = parseCmdLineStep(ar, all, 2, wasSpace, txt);
                    }
                    break;
                }
                default: {
                    wasSpace = parseCmdLineStep(ar, all, 2, wasSpace, txt);
                }
            }
        }
        return all.toArray(new NText[0]);
    }

    private NText[] parseCmdLine_readDollarCurlyBrackets(StringReaderExt ar, NTexts txt) {
        List<NText> all = new ArrayList<>();
        all.add(txt.ofStyled(String.valueOf(ar.readChar()) + ar.readChar(), NTextStyle.separator()));
        boolean inLoop = true;
        int startIndex = 0;
        boolean expectedName = true;
        boolean wasSpace = true;
        while (inLoop && ar.hasNext()) {
            char c = ar.peekChar();
            switch (c) {
                case '}': {
                    all.add(txt.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
                    inLoop = false;
                    break;
                }
                default: {
                    startIndex = all.size();
                    wasSpace = parseCmdLineStep(ar, all, -1, wasSpace, txt);
                    if (expectedName) {
                        expectedName = false;
                        if (all.size() > startIndex) {
                            TokenType t = resolveTokenType(all.get(startIndex));
                            if (t== TokenType.ENV || t== TokenType.WORD) {
                                all.set(startIndex, txt.ofStyled(all.get(startIndex), NTextStyle.keyword(4)));
                                wasSpace = false;
                            }
                        }
                    }
                }
            }
        }
        return all.toArray(new NText[0]);
    }

    private NText[] parseCmdLine_readPar2(StringReaderExt ar, NTexts txt) {
        List<NText> all = new ArrayList<>();
        all.add(txt.ofStyled(String.valueOf(ar.readChar()) + ar.readChar(), NTextStyle.separator()));
        boolean inLoop = true;
        boolean wasSpace = true;
        while (inLoop && ar.hasNext()) {
            char c = ar.peekChar();
            switch (c) {
                case ')': {
                    if (ar.peekChars(2).equals("))")) {
                        all.add(txt.ofStyled(String.valueOf(ar.nextChars(2)), NTextStyle.separator()));
                        inLoop = false;
                    } else {
                        wasSpace = parseCmdLineStep(ar, all, 2, wasSpace, txt);
                    }
                    break;
                }
                default: {
                    wasSpace = parseCmdLineStep(ar, all, 2, wasSpace, txt);
                }
            }
        }
        return all.toArray(new NText[0]);
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
    private boolean parseCmdLineStep(StringReaderExt ar, List<NText> all, int startIndex, boolean wasSpace, NTexts txt) {
        NSession session = workspace.currentSession();
        char c = ar.peekChar();
        if (c <= 32) {
            all.addAll(Arrays.asList(StringReaderExtUtils.readSpaces(session, ar)));
            return true;
        }
        switch (c) {
            case '\'': {
                all.addAll(Arrays.asList(parseCmdLine_readSimpleQuotes(ar, txt)));
                break;
            }
            case '`': {
                all.addAll(Arrays.asList(parseCmdLine_readAntiQuotes(ar, txt)));
                break;
            }
            case '"': {
                all.addAll(Arrays.asList(parseCmdLine_readDoubleQuotes(ar, txt)));
                break;
            }
            case '$': {
                all.addAll(Arrays.asList(parseCmdLine_readDollar(ar, txt)));
                break;
            }
            case ';': {
                all.add(txt.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
                break;
            }
            case ':': {
                all.add(txt.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator(2)));
                break;
            }
            case '|': {
                if (ar.peekChars(2).equals("||")) {
                    all.add(txt.ofStyled(ar.nextChars(2), NTextStyle.separator()));
                } else {
                    all.add(txt.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
                }
                break;
            }
            case '&': {
                if (ar.peekChars(2).equals("&&")) {
                    all.add(txt.ofStyled(ar.nextChars(2), NTextStyle.separator()));
                } else if (ar.peekChars(3).equals("&>>")) {
                    all.add(txt.ofStyled(ar.nextChars(3), NTextStyle.separator()));
                } else if (ar.peekChars(2).equals("&>")) {
                    all.add(txt.ofStyled(ar.nextChars(2), NTextStyle.separator()));
                } else {
                    all.add(txt.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
                }
                break;
            }
            case '>': {
                if (ar.peekChars(2).equals(">>")) {
                    all.add(txt.ofStyled(ar.nextChars(2), NTextStyle.separator()));
                } else if (ar.peekChars(2).equals(">&")) {
                    all.add(txt.ofStyled(ar.nextChars(2), NTextStyle.separator()));
                } else {
                    all.add(txt.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
                }
                break;
            }
            case '<': {
                if (ar.peekChars(2).equals("<<")) {
                    all.add(txt.ofStyled(ar.nextChars(2), NTextStyle.separator()));
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
                        if (isSynopsisOption(s0)) {
                            all.add(txt.ofStyled("<", NTextStyle.input()));
                            all.add(txt.ofStyled(s0, NTextStyle.option()));
                            all.add(txt.ofStyled(">", NTextStyle.input()));
                        } else if (isSynopsisWord(s0)) {
                            all.add(txt.ofStyled("<", NTextStyle.input()));
                            all.add(txt.ofStyled(s0, NTextStyle.input()));
                            all.add(txt.ofStyled(">", NTextStyle.input()));
                        } else {
                            all.add(txt.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
                        }
                    } else {
                        all.add(txt.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
                    }
                }
                break;
            }
            case '(': {
                if (ar.peekChars("((")) {
                    all.addAll(Arrays.asList(parseCmdLine_readPar2(ar, txt)));
                } else {
                    all.add(txt.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
                }
            }
            case ')':
            case '{':
            case '}':
            case '~':
            case '!': {
                all.add(txt.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
                break;
            }
            case '*':
            case '?':
            case '[':
            case ']':
            case '=': {
                all.add(txt.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
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
                            sb.append(ar.readChar());
                        }
                    }
                    all.add(txt.ofStyled(sb.toString(), NTextStyle.comments()));
                } else {
                    all.add(txt.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
                }
                break;
            }
            default: {
                if (startIndex >= 0) {
                    boolean first = all.size() == startIndex;
                    all.addAll(Arrays.asList(parseCmdLine_readWord(ar, txt)));
                    if (first) {
                        int i = indexOfFirstWord(all, startIndex);
                        if (i >= 0) {
                            all.set(i, txt.ofStyled(all.get(i), NTextStyle.keyword()));
                        }
                    }
                } else {
                    all.addAll(Arrays.asList(parseCmdLine_readWord(ar, txt)));
                }
            }
        }
        return false;
    }

    private NText[] parseCmdLine(String commandLineString, NTexts txt) {
        StringReaderExt ar = new StringReaderExt(commandLineString);
        List<NText> all = new ArrayList<>();
        boolean wasSpace = true;
        while (ar.hasNext()) {
            wasSpace = parseCmdLineStep(ar, all, 0, wasSpace, txt);
        }
        return all.toArray(new NText[0]);
    }

    private static boolean isSynopsisOption(String s2) {
        return ((s2.startsWith("--") && isSynopsisWord(s2.substring(2)))
                || (s2.startsWith("++") && isSynopsisWord(s2.substring(2)))
                || (s2.startsWith("-") && isSynopsisWord(s2.substring(1)))
                || (s2.startsWith("+") && isSynopsisWord(s2.substring(1)))
                || (s2.startsWith("--!") && isSynopsisWord(s2.substring(3)))
                || (s2.startsWith("++!") && isSynopsisWord(s2.substring(3)))
                || (s2.startsWith("-!") && isSynopsisWord(s2.substring(2)))
                || (s2.startsWith("+!") && isSynopsisWord(s2.substring(2)))
                || (s2.startsWith("--~") && isSynopsisWord(s2.substring(3)))
                || (s2.startsWith("++~") && isSynopsisWord(s2.substring(3)))
                || (s2.startsWith("-~") && isSynopsisWord(s2.substring(2)))
                || (s2.startsWith("+~") && isSynopsisWord(s2.substring(2))));
    }

    private static boolean isOption(String s2) {
        return ((s2.startsWith("-"))
                || (s2.startsWith("+")));
    }

    private static boolean isSynopsisWord(String s) {
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
    public NText stringToText(String text, NTexts txt) {
        List<NText> all = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new StringReader(text));
        String line = null;
        boolean first = true;
        NSession session = workspace.currentSession();
        while (true) {
            try {
                if ((line = reader.readLine()) == null) {
                    break;
                }
            } catch (IOException ex) {
                throw new NIOException(ex);
            }
            if (first) {
                first = false;
            } else {
                all.add(txt.ofPlain("\n"));
            }
            all.add(commandToNode(line, txt));
        }
        return txt.ofList(all).simplify();
    }

    public NText next(StringReaderExt reader, boolean exitOnClosedCurlBrace, boolean exitOnClosedPar, boolean exitOnDblQuote, boolean exitOnAntiQuote) {
        boolean lineStart = true;
        List<NText> all = new ArrayList<>();
        NSession session = workspace.currentSession();
        NTexts txt = NTexts.of();
        boolean exit = false;
        while (!exit && reader.hasNext()) {
            switch (reader.peekChar()) {
                case '}': {
                    lineStart = false;
                    if (exitOnClosedCurlBrace) {
                        exit = true;
                    } else {
                        all.add(txt.ofStyled(
                                reader.nextChars(1), NTextStyle.separator()
                        ));
                    }
                    break;
                }
                case ')': {
                    lineStart = false;
                    if (exitOnClosedPar) {
                        exit = true;
                    } else {
                        all.add(txt.ofStyled(
                                reader.nextChars(1), NTextStyle.separator()
                        ));
                    }
                    break;
                }
                case '>': {
                    lineStart = false;
                    if (reader.isAvailable(2) && reader.peekChar() == '>') {
                        all.add(txt.ofStyled(
                                reader.nextChars(2), NTextStyle.separator()
                        ));
                    } else {
                        all.add(txt.ofStyled(
                                reader.nextChars(1), NTextStyle.separator()
                        ));
                    }
                    break;
                }
                case '&': {
                    lineStart = false;
                    if (reader.isAvailable(2) && reader.peekChar() == '&') {
                        all.add(txt.ofStyled(
                                reader.nextChars(2), NTextStyle.separator()
                        ));
                    } else if (reader.isAvailable(2) && reader.peekChar() == '>') {
                        all.add(txt.ofStyled(
                                reader.nextChars(2), NTextStyle.separator()
                        ));
                    } else if (reader.isAvailable(2) && reader.peekChar() == '<') {
                        all.add(txt.ofStyled(
                                reader.nextChars(2), NTextStyle.separator()
                        ));
                    } else {
                        all.add(txt.ofStyled(
                                reader.nextChars(1), NTextStyle.separator()
                        ));
                    }
                    break;
                }
                case '|': {
                    lineStart = false;
                    if (reader.isAvailable(2) && reader.peekChar() == '|') {
                        all.add(txt.ofStyled(
                                reader.nextChars(2), NTextStyle.separator()
                        ));
                    } else {
                        all.add(txt.ofStyled(
                                reader.nextChars(1), NTextStyle.separator()
                        ));
                    }
                    break;
                }
                case ';': {
                    all.add(txt.ofStyled(
                            reader.nextChars(1), NTextStyle.separator()
                    ));
                    lineStart = true;
                    break;
                }
                case '\n': {
                    if (reader.isAvailable(2) && reader.peekChar() == '\r') {
                        all.add(txt.ofStyled(
                                reader.nextChars(2), NTextStyle.separator()
                        ));
                    } else {
                        all.add(txt.ofStyled(
                                reader.nextChars(1), NTextStyle.separator()
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
                            all.add(txt.ofStyled(
                                    sb.toString(), NTextStyle.input()
                            ));
                            break;
                        } else {
                            all.add(txt.ofStyled(
                                    reader.nextChars(1), NTextStyle.separator()
                            ));
                        }
                    } else if (reader.isAvailable(2) && reader.peekChar() == '<') {
                        all.add(txt.ofStyled(
                                reader.nextChars(2), NTextStyle.separator()
                        ));
                    } else {
                        all.add(txt.ofStyled(
                                reader.nextChars(1), NTextStyle.separator()
                        ));
                    }
                    break;
                }
                case '\\': {
                    lineStart = false;
                    all.add(txt.ofStyled(
                            reader.nextChars(2), NTextStyle.separator(2)
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
                        List<NText> a = new ArrayList<>();
                        a.add(txt.ofStyled(reader.nextChars(1), NTextStyle.string()));
                        a.add(next(reader, false, false, false, true));
                        if (reader.hasNext() && reader.peekChar() == '`') {
                            a.add(txt.ofStyled(reader.nextChars(1), NTextStyle.string()));
                        } else {
                            exit = true;
                        }
                        all.add(txt.ofList(a).simplify());
                    }
                    break;
                }
                case '\'': {
                    lineStart = false;
                    StringBuilder sb = new StringBuilder();
                    sb.append(reader.readChar());
                    boolean end = false;
                    while (!end && reader.hasNext()) {
                        switch (reader.peekChar()) {
                            case '\\': {
                                sb.append(reader.nextChars(2));
                                break;
                            }
                            case '\'': {
                                sb.append(reader.readChar());
                                end = true;
                                break;
                            }
                            default: {
                                sb.append(reader.readChar());
                                break;
                            }
                        }
                    }
                    all.add(txt.ofStyled(sb.toString(), NTextStyle.string()));
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
                                all.add(txt.ofStyled(reader.nextChars(2), NTextStyle.string()));
                                break;
                            }
                            default: {
                                if (Character.isAlphabetic(reader.peekChar(1))) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(reader.readChar());
                                    while (reader.hasNext() && (Character.isAlphabetic(reader.peekChar()) || reader.peekChar() == '_')) {
                                        sb.append(reader.readChar());
                                    }
                                    all.add(txt.ofStyled(sb.toString(), NTextStyle.variable()));
                                } else {
                                    all.add(txt.ofStyled(reader.nextChars(1), NTextStyle.separator()));
                                }
                            }
                        }
                    } else {
                        all.add(txt.ofStyled(reader.nextChars(1), NTextStyle.string()));
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
                        whites.append(reader.readChar());
                    }
                    all.add(txt.ofPlain(whites.toString()));
                    break;
                }
                default: {
                    StringBuilder sb = new StringBuilder();
                    sb.append(reader.readChar());
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
                                    sb.append(reader.readChar());
                                }
                            }
                        }
                        if (!accept) {
                            break;
                        }
                    }
                    if (lineStart && !reader.hasNext() || Character.isWhitespace(reader.peekChar())) {
                        //command name
                        NTextStyle keyword1 = NTextStyle.keyword(2);
                        switch (sb.toString()) {
                            case "if":
                            case "while":
                            case "do":
                            case "fi":
                            case "elif":
                            case "then":
                            case "else": {
                                keyword1 = NTextStyle.keyword();
                                break;
                            }
                            case "cp":
                            case "ls":
                            case "ll":
                            case "rm":
                            case "pwd":
                            case "echo": {
                                keyword1 = NTextStyle.keyword(3);
                                break;
                            }
                        }
                        all.add(txt.ofStyled(sb.toString(), keyword1));
                    } else {
                        all.add(txt.ofPlain(sb.toString()));
                    }
                    lineStart = false;
                    break;
                }
            }
        }
        return txt.ofList(all).simplify();
    }

    private NText nextDollar(StringReaderExt reader) {
        NSession session = workspace.currentSession();
        NTexts txt = NTexts.of();
        if (reader.isAvailable(2)) {
            char c = reader.peekChar(1);
            switch (c) {
                case '(': {
                    List<NText> a = new ArrayList<>();
                    a.add(txt.ofStyled(reader.nextChars(1), NTextStyle.separator()));
                    a.add(next(reader, false, true, false, false));
                    if (reader.hasNext() && reader.peekChar() == ')') {
                        a.add(txt.ofStyled(reader.nextChars(1), NTextStyle.separator()));
                    }
                    return txt.ofList(a).simplify();
                }
                case '{': {
                    List<NText> a = new ArrayList<>();
                    a.add(txt.ofStyled(reader.nextChars(1), NTextStyle.separator()));
                    a.add(next(reader, true, false, false, false));
                    if (reader.hasNext() && reader.peekChar() == ')') {
                        a.add(txt.ofStyled(reader.nextChars(1), NTextStyle.separator()));
                    }
                    return txt.ofList(a).simplify();
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
                    return txt.ofStyled(reader.nextChars(2), NTextStyle.string());
                }
                default: {
                    if (Character.isAlphabetic(reader.peekChar(1))) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(reader.readChar());
                        while (reader.hasNext() && (Character.isAlphabetic(reader.peekChar()) || reader.peekChar() == '_')) {
                            sb.append(reader.readChar());
                        }
                        return txt.ofStyled(sb.toString(), NTextStyle.variable());
                    } else {
                        return txt.ofStyled(reader.nextChars(1), NTextStyle.separator());
                    }
                }
            }
        } else {
            return txt.ofStyled(reader.nextChars(1), NTextStyle.string());
        }
    }

    public NText nextDoubleQuotes(StringReaderExt reader) {
        List<NText> all = new ArrayList<>();
        NSession session = workspace.currentSession();
        NTexts txt = NTexts.of();
        boolean exit = false;
        StringBuilder sb = new StringBuilder();
        sb.append(reader.readChar());
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
                        all.add(txt.ofStyled(sb.toString(), NTextStyle.string()));
                        sb.setLength(0);
                    }
                    all.add(nextDollar(reader));
                }
                case '`': {
                    if (sb.length() > 0) {
                        all.add(txt.ofStyled(sb.toString(), NTextStyle.string()));
                        sb.setLength(0);
                    }
                    List<NText> a = new ArrayList<>();
                    a.add(txt.ofStyled(reader.nextChars(1), NTextStyle.string()));
                    a.add(next(reader, false, false, false, true));
                    if (reader.hasNext() && reader.peekChar() == '`') {
                        a.add(txt.ofStyled(reader.nextChars(1), NTextStyle.string()));
                    } else {
                        exit = true;
                    }
                    all.add(txt.ofList(a).simplify());
                    break;
                }
                default: {
                    sb.append(reader.nextChars(1));
                }
            }
        }
        if (sb.length() > 0) {
            all.add(txt.ofStyled(sb.toString(), NTextStyle.string()));
            sb.setLength(0);
        }
        return txt.ofList(all).simplify();
    }

    public NText commandToNode(String text, NTexts txt) {
        return txt.ofList(parseCmdLine(text, txt));
    }

}
