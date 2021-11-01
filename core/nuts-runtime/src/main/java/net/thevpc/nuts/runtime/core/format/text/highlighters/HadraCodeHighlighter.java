package net.thevpc.nuts.runtime.core.format.text.highlighters;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.parsers.StringReaderExt;

import java.util.*;

import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsComponent;
import net.thevpc.nuts.NutsCodeHighlighter;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

public class HadraCodeHighlighter implements NutsCodeHighlighter {

    private static Set<String> reservedWords = new LinkedHashSet<>(
            Arrays.asList(
                    "abstract", "assert", "boolean", "break", "byte", "case",
                    "catch", "char", "class", "const", "continue", "default",
                    "double", "do", "else", "enum", "extends", "false",
                    "final", "finally", "float", "for", "goto", "if",
                    "implements", "import", "instanceof", "int", "interface", "long",
                    "native", "new", "null", "package", "private", "protected",
                    "public", "return", "short", "static", "strictfp", "super",
                    "switch", "synchronized", "this", "throw", "throws", "transient",
                    "true", "try", "void", "volatile", "while",
                    //
                    "is", "bigint", "bigdecimal", "string", "date", "time", "datetime", "timestamp",
                    "constructor", "operator",
                    //other reserved...
                    "yield", "_", "it", "record", "fun", "implicit", "def",
                    "bool", "decimal", "bigint", "bigdecimal", "string", "object",
                    "date", "time", "datetime",
                    "int8", "int16", "int32", "int64", "int128",
                    "uint8", "uint16", "uint32", "uint64", "uint128",
                    "uint", "ulong", "ref", "ptr", "unsafe", "init"
            )
    );
    private NutsWorkspace ws;
    NutsTexts factory;

    @Override
    public String getId() {
        return "handra";
    }

    public HadraCodeHighlighter(NutsWorkspace ws) {
        this.ws = ws;
        factory = NutsTexts.of(NutsWorkspaceUtils.defaultSession(ws));
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<String> context) {
        String s = context.getConstraints();
        return "java".equals(s) ? NutsComponent.DEFAULT_SUPPORT : NutsComponent.NO_SUPPORT;
    }

    @Override
    public NutsText tokenToText(String text, String nodeType,NutsSession session) {
        String str = String.valueOf(text);
        switch (nodeType.toLowerCase()) {
            case "separator": {
                return factory.setSession(session).ofStyled(str, NutsTextStyle.separator());
            }
            case "keyword": {
                return factory.setSession(session).ofStyled(str, NutsTextStyle.separator());
            }
        }
        return factory.ofPlain(str);
    }

    @Override
    public NutsText stringToText(String text, NutsSession session) {
        factory.setSession(session);
        List<NutsText> all = new ArrayList<>();
        StringReaderExt ar = new StringReaderExt(text);
        while (ar.hasNext()) {
            switch (ar.peekChar()) {
                case '{':
                case '}':
                case '(':
                case ')':
                case '[':
                case ']':
                case '@':
                case '=':
                case '+':
                case '*':
                case '%':
                case ':':
                case '?':
                case '<':
                case '>':
                case '!':
                case ';': {
                    all.add(factory.ofStyled(String.valueOf(ar.nextChar()), NutsTextStyle.separator()));
                    break;
                }
                case '\'': {
                    all.addAll(Arrays.asList(StringReaderExtUtils.readJSSimpleQuotes(session, ar)));
                    break;
                }
                case '"': {
                    all.addAll(Arrays.asList(StringReaderExtUtils.readJSDoubleQuotesString(session, ar)));
                    break;
                }
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
                    all.addAll(Arrays.asList(StringReaderExtUtils.readNumber(session, ar)));
                    break;
                }
                case '.':
                case '-': {
                    NutsText[] d = StringReaderExtUtils.readNumber(session, ar);
                    if (d != null) {
                        all.addAll(Arrays.asList(d));
                    } else {
                        all.add(factory.ofStyled(String.valueOf(ar.nextChar()), NutsTextStyle.separator()));
                    }
                    break;
                }
                case '/': {
                    if (ar.peekChars("//")) {
                        all.addAll(Arrays.asList(StringReaderExtUtils.readSlashSlashComments(session, ar)));
                    } else if (ar.peekChars("/*")) {
                        all.addAll(Arrays.asList(StringReaderExtUtils.readSlashStarComments(session, ar)));
                    } else {
                        all.add(factory.ofStyled(String.valueOf(ar.nextChar()), NutsTextStyle.separator()));
                    }
                    break;
                }
                default: {
                    if (Character.isWhitespace(ar.peekChar())) {
                        all.addAll(Arrays.asList(StringReaderExtUtils.readSpaces(session, ar)));
                    } else {
                        NutsText[] d = StringReaderExtUtils.readJSIdentifier(session, ar);
                        if (d != null) {
                            if (d.length == 1 && d[0].getType() == NutsTextType.PLAIN) {
                                String txt = ((NutsTextPlain) d[0]).getText();
                                if (reservedWords.contains(txt)) {
                                    d[0] = factory.ofStyled(d[0], NutsTextStyle.keyword());
                                }
                            }
                            all.addAll(Arrays.asList(d));
                        } else {
                            all.add(factory.ofStyled(String.valueOf(ar.nextChar()), NutsTextStyle.separator()));
                        }
                    }
                    break;
                }
            }
        }
        return factory.ofList(all.toArray(new NutsText[0]));
    }
}
