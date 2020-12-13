package net.thevpc.nuts.runtime.format.text.special;

import net.thevpc.nuts.NutsTextNode;
import net.thevpc.nuts.NutsTextNodeFactory;
import net.thevpc.nuts.NutsTextNodeStyle;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.app.NutsCommandLineUtils;
import net.thevpc.nuts.runtime.format.text.parser.SpecialTextFormatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShellSpecialTextFormatter implements SpecialTextFormatter {
    private NutsWorkspace ws;

    public ShellSpecialTextFormatter(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsTextNode toNode(String text) {
        List<NutsTextNode> all = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new StringReader(text));
        String line = null;
        boolean first = true;
        NutsTextNodeFactory factory = ws.formats().text().factory();
        while (true) {
            try {
                if ((line = reader.readLine()) == null) break;
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            if (first) {
                first = false;
            } else {
                all.add(factory.plain("\n"));
            }
            all.add(commandToNode(line));
        }
        return factory.list(all);
    }


    public NutsTextNode next(AheadReader reader, boolean exitOnClosedCurlBrace, boolean exitOnClosedPar, boolean exitOnDblQuote, boolean exitOnAntiQuote) {
        boolean lineStart = true;
        List<NutsTextNode> all = new ArrayList<>();
        NutsTextNodeFactory factory = ws.formats().text().factory();
        boolean exit = false;
        while (!exit && reader.hasNext()) {
            switch (reader.peekChar()) {
                case '}': {
                    lineStart = false;
                    if (exitOnClosedCurlBrace) {
                        exit = true;
                    } else {
                        all.add(factory.styled(
                                reader.readChars(1), NutsTextNodeStyle.SEPARATOR1
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
                                reader.readChars(1), NutsTextNodeStyle.SEPARATOR1
                        ));
                    }
                    break;
                }
                case '>': {
                    lineStart = false;
                    if (reader.isAvailable(2) && reader.peekChar() == '>') {
                        all.add(factory.styled(
                                reader.readChars(2), NutsTextNodeStyle.SEPARATOR1
                        ));
                    } else {
                        all.add(factory.styled(
                                reader.readChars(1), NutsTextNodeStyle.SEPARATOR1
                        ));
                    }
                    break;
                }
                case '&': {
                    lineStart = false;
                    if (reader.isAvailable(2) && reader.peekChar() == '&') {
                        all.add(factory.styled(
                                reader.readChars(2), NutsTextNodeStyle.SEPARATOR1
                        ));
                    } else if (reader.isAvailable(2) && reader.peekChar() == '>') {
                        all.add(factory.styled(
                                reader.readChars(2), NutsTextNodeStyle.SEPARATOR1
                        ));
                    } else if (reader.isAvailable(2) && reader.peekChar() == '<') {
                        all.add(factory.styled(
                                reader.readChars(2), NutsTextNodeStyle.SEPARATOR1
                        ));
                    } else {
                        all.add(factory.styled(
                                reader.readChars(1), NutsTextNodeStyle.SEPARATOR1
                        ));
                    }
                    break;
                }
                case '|': {
                    lineStart = false;
                    if (reader.isAvailable(2) && reader.peekChar() == '|') {
                        all.add(factory.styled(
                                reader.readChars(2), NutsTextNodeStyle.SEPARATOR1
                        ));
                    } else {
                        all.add(factory.styled(
                                reader.readChars(1), NutsTextNodeStyle.SEPARATOR1
                        ));
                    }
                    break;
                }
                case ';': {
                    all.add(factory.styled(
                            reader.readChars(1), NutsTextNodeStyle.SEPARATOR1
                    ));
                    lineStart = true;
                    break;
                }
                case '\n': {
                    if (reader.isAvailable(2) && reader.peekChar() == '\r') {
                        all.add(factory.styled(
                                reader.readChars(2), NutsTextNodeStyle.SEPARATOR1
                        ));
                    } else {
                        all.add(factory.styled(
                                reader.readChars(1), NutsTextNodeStyle.SEPARATOR1
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
                            reader.readChars(sb.length());
                            all.add(factory.styled(
                                    sb.toString(), NutsTextNodeStyle.USER_INPUT1
                            ));
                            break;
                        } else {
                            all.add(factory.styled(
                                    reader.readChars(1), NutsTextNodeStyle.SEPARATOR1
                            ));
                        }
                    } else if (reader.isAvailable(2) && reader.peekChar() == '<') {
                        all.add(factory.styled(
                                reader.readChars(2), NutsTextNodeStyle.SEPARATOR1
                        ));
                    } else {
                        all.add(factory.styled(
                                reader.readChars(1), NutsTextNodeStyle.SEPARATOR1
                        ));
                    }
                    break;
                }
                case '\\': {
                    lineStart = false;
                    all.add(factory.styled(
                            reader.readChars(2), NutsTextNodeStyle.SEPARATOR2
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
                        a.add(factory.styled(reader.readChars(1), NutsTextNodeStyle.STRING1));
                        a.add(next(reader, false, false, false, true));
                        if (reader.hasNext() && reader.peekChar() == '`') {
                            a.add(factory.styled(reader.readChars(1), NutsTextNodeStyle.STRING1));
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
                    sb.append(reader.readChar());
                    boolean end = false;
                    while (!end && reader.hasNext()) {
                        switch (reader.peekChar()) {
                            case '\\': {
                                sb.append(reader.readChars(2));
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
                    all.add(factory.styled(sb.toString(), NutsTextNodeStyle.STRING1));
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
                                all.add(factory.styled(reader.readChars(2), NutsTextNodeStyle.STRING1));
                                break;
                            }
                            default: {
                                if (Character.isAlphabetic(reader.peekChar(1))) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(reader.readChar());
                                    while (reader.hasNext() && (Character.isAlphabetic(reader.peekChar()) || reader.peekChar() == '_')) {
                                        sb.append(reader.hasNext());
                                    }
                                    all.add(factory.styled(sb.toString(), NutsTextNodeStyle.VAR1));
                                } else {
                                    all.add(factory.styled(reader.readChars(1), NutsTextNodeStyle.SEPARATOR1));
                                }
                            }
                        }
                    } else {
                        all.add(factory.styled(reader.readChars(1), NutsTextNodeStyle.STRING1));
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
                    all.add(factory.plain(whites.toString()));
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
                        NutsTextNodeStyle keyword1 = NutsTextNodeStyle.KEYWORD2;
                        switch (sb.toString()) {
                            case "if":
                            case "while":
                            case "do":
                            case "fi":
                            case "elif":
                            case "then":
                            case "else": {
                                keyword1 = NutsTextNodeStyle.KEYWORD1;
                                break;
                            }
                            case "cp":
                            case "ls":
                            case "ll":
                            case "rm":
                            case "pwd":
                            case "echo": {
                                keyword1 = NutsTextNodeStyle.KEYWORD3;
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

    private NutsTextNode nextDollar(AheadReader reader) {
        NutsTextNodeFactory factory = ws.formats().text().factory();
        if (reader.isAvailable(2)) {
            char c = reader.peekChar(1);
            switch (c) {
                case '(': {
                    List<NutsTextNode> a = new ArrayList<>();
                    a.add(factory.styled(reader.readChars(1), NutsTextNodeStyle.SEPARATOR1));
                    a.add(next(reader, false, true, false, false));
                    if (reader.hasNext() && reader.peekChar() == ')') {
                        a.add(factory.styled(reader.readChars(1), NutsTextNodeStyle.SEPARATOR1));
                    }
                    return factory.list(a);
                }
                case '{': {
                    List<NutsTextNode> a = new ArrayList<>();
                    a.add(factory.styled(reader.readChars(1), NutsTextNodeStyle.SEPARATOR1));
                    a.add(next(reader, true, false, false, false));
                    if (reader.hasNext() && reader.peekChar() == ')') {
                        a.add(factory.styled(reader.readChars(1), NutsTextNodeStyle.SEPARATOR1));
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
                    return factory.styled(reader.readChars(2), NutsTextNodeStyle.STRING1);
                }
                default: {
                    if (Character.isAlphabetic(reader.peekChar(1))) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(reader.readChar());
                        while (reader.hasNext() && (Character.isAlphabetic(reader.peekChar()) || reader.peekChar() == '_')) {
                            sb.append(reader.hasNext());
                        }
                        return factory.styled(sb.toString(), NutsTextNodeStyle.VAR1);
                    } else {
                        return factory.styled(reader.readChars(1), NutsTextNodeStyle.SEPARATOR1);
                    }
                }
            }
        } else {
            return factory.styled(reader.readChars(1), NutsTextNodeStyle.STRING1);
        }
    }

    public NutsTextNode nextDoubleQuotes(AheadReader reader) {
        List<NutsTextNode> all = new ArrayList<>();
        NutsTextNodeFactory factory = ws.formats().text().factory();
        boolean exit = false;
        StringBuilder sb = new StringBuilder();
        sb.append(reader.hasNext());
        while (!exit && reader.hasNext()) {
            switch (reader.peekChar()) {
                case '\\': {
                    sb.append(reader.readChars(2));
                    break;
                }
                case '\"': {
                    sb.append(reader.readChars(1));
                    exit = true;
                    break;
                }
                case '$': {
                    if (sb.length() > 0) {
                        all.add(factory.styled(sb.toString(), NutsTextNodeStyle.STRING1));
                        sb.setLength(0);
                    }
                    all.add(nextDollar(reader));
                }
                case '`': {
                    if (sb.length() > 0) {
                        all.add(factory.styled(sb.toString(), NutsTextNodeStyle.STRING1));
                        sb.setLength(0);
                    }
                    List<NutsTextNode> a = new ArrayList<>();
                    a.add(factory.styled(reader.readChars(1), NutsTextNodeStyle.STRING1));
                    a.add(next(reader, false, false, false, true));
                    if (reader.hasNext() && reader.peekChar() == '`') {
                        a.add(factory.styled(reader.readChars(1), NutsTextNodeStyle.STRING1));
                    } else {
                        exit = true;
                    }
                    all.add(factory.list(a));
                    break;
                }
                default: {
                    sb.append(reader.readChars(1));
                }
            }
        }
        if (sb.length() > 0) {
            all.add(factory.styled(sb.toString(), NutsTextNodeStyle.STRING1));
            sb.setLength(0);
        }
        return factory.list(all);
    }

    public NutsTextNode commandToNode(String text) {
        String[] u = NutsCommandLineUtils.parseCommandLine(null, text);
        List<NutsTextNode> all = new ArrayList<>();
        boolean cmdName = true;
        NutsTextNodeFactory factory = ws.formats().text().factory();
        for (int i = 0; i < u.length; i++) {
            if (!all.isEmpty()) {
                all.add(factory.plain(" "));
            }
            if (i == 0 & !u[i].startsWith("-") && !u[i].startsWith("+")) {
                all.add(factory.styled(u[i], NutsTextNodeStyle.KEYWORD1));
            } else {
                all.addAll(Arrays.asList(argToNodes(u[i])));
            }
        }
        return factory.list(all);
    }

    private boolean isSynopsysOption(String s2) {
        return (
                (s2.startsWith("--") && isSynopsysWord(s2.substring(2)))
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
                        || (s2.startsWith("+~") && isSynopsysWord(s2.substring(2)))
        );
    }

    private boolean isSynopsysWord(String s) {
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

    private NutsTextNode[] arrNotNull(NutsTextNode... all) {
        return Arrays.stream(all).filter(x -> x != null).toArray(NutsTextNode[]::new);
    }

    private NutsTextNode[] argToNodes(String s) {
        NutsTextNodeFactory factory = ws.formats().text().factory();
        NutsTextNode threePoints = null;
        if (s.endsWith("...")) {
            threePoints = factory.styled("...", NutsTextNodeStyle.SEPARATOR2);
            s = s.substring(0, s.length() - 3);
        }
        if (s.startsWith("[<") && s.endsWith(">]")
        ) {
            String start=s.substring(0,2);
            String end=s.substring(s.length()-2);
            String s2 = s.substring(2, s.length() - 2);
            if (isSynopsysOption(s2)) {
                return arrNotNull(
                        factory.styled(start, NutsTextNodeStyle.PALE1),
                        factory.styled(s2, NutsTextNodeStyle.OPTION1),
                        factory.styled(end, NutsTextNodeStyle.PALE1),
                        threePoints
                );
            } else if (isSynopsysWord(s2)) {
                return arrNotNull(
                        factory.styled(start, NutsTextNodeStyle.PALE1),
                        factory.plain(s2),
                        factory.styled(end, NutsTextNodeStyle.PALE1)
                        , threePoints);
            }
        }
        if (s.startsWith("[") && s.endsWith("]")
                || (s.startsWith("<") && s.endsWith(">"))
                || (s.startsWith("(") && s.endsWith(")"))
        ) {
            String start=String.valueOf(s.charAt(0));
            String end=String.valueOf(s.charAt(s.length()-1));
            String s2 = s.substring(1, s.length() - 1);
            if (isSynopsysOption(s2)) {
                return arrNotNull(
                        factory.styled(start, NutsTextNodeStyle.PALE1),
                        factory.styled(String.valueOf(s2), NutsTextNodeStyle.OPTION1),
                        factory.styled(end, NutsTextNodeStyle.PALE1),
                        threePoints
                );
            } else if (isSynopsysWord(s2)) {
                return arrNotNull(
                        factory.styled(start, NutsTextNodeStyle.PALE1),
                        factory.plain(s2),
                        factory.styled(end, NutsTextNodeStyle.PALE1)
                        , threePoints);
            }
        }
        boolean option = false;
        if (s.startsWith("-") || s.startsWith("+")) {
            option = true;
        }
        int x = s.indexOf('=');
        int eq = -1;
        if (x >= 0 && isCommonName(s.substring(0, x))) {
            eq = x;
        }

        if (eq != -1) {
            if (option) {
                return arrNotNull(
                        factory.styled(s.substring(0, x), NutsTextNodeStyle.OPTION1),
                        factory.styled(String.valueOf(s.charAt(eq)), NutsTextNodeStyle.SEPARATOR1),
                        factory.plain(s.substring(x + 1)),
                        threePoints);
            }
            return arrNotNull(
                    factory.plain(s.substring(0, x)),
                    factory.styled(String.valueOf(s.charAt(eq)), NutsTextNodeStyle.SEPARATOR1),
                    factory.plain(s.substring(x + 1)),
                    threePoints);
        } else {
            if (option) {
                return arrNotNull(
                        factory.styled(s, NutsTextNodeStyle.OPTION1)
                        , threePoints);
            }
            return arrNotNull(
                    factory.plain(s),
                    threePoints);
        }
    }

    private boolean isSubCommand(String substring) {
        if (!Character.isAlphabetic(substring.charAt(0))) {
            return false;
        }
        for (char c : substring.toCharArray()) {
            if (!(Character.isAlphabetic(c) || c == '-' || c == '+' || c == '_')) {
                return false;
            }
        }
        return true;
    }

    private boolean isCommonName(String substring) {
        for (char c : substring.toCharArray()) {
            if (!(Character.isAlphabetic(c) || Character.isDigit(c) || c == '-' || c == '+' || c == '_')) {
                return false;
            }
        }
        return true;
    }

    private static class AheadReader {
        String content;
        int pos = 0;

        public AheadReader(String content) {
            this.content = content;
        }

        public char peekChar() {
            return content.charAt(pos);
        }

        public char peekChar(int i) {
            return content.charAt(pos + i);
        }

        public boolean isAvailable(int count) {
            return pos + count < content.length();
        }

        public char readChar() {
            char c = content.charAt(pos);
            pos++;
            return c;
        }

        public String readChars(int max) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < max; i++) {
                if (hasNext()) {
                    sb.append(readChar());
                } else {
                    break;
                }
            }
            return sb.toString();
        }

        public boolean hasNext() {
            return content.length() - pos == 0;
        }
    }

}
