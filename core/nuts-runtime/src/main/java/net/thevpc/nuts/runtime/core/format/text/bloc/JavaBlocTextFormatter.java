package net.thevpc.nuts.runtime.core.format.text.bloc;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.parsers.StringReaderExt;

import java.util.*;
import net.thevpc.nuts.spi.NutsComponent;
import net.thevpc.nuts.NutsCodeFormat;

public class JavaBlocTextFormatter implements NutsCodeFormat {

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
                    "true", "try", "void", "volatile", "while"
            )
    );
    private NutsWorkspace ws;
    private NutsTextManager factory;

    public JavaBlocTextFormatter(NutsWorkspace ws) {
        this.ws = ws;
        factory = ws.formats().text();
    }

    @Override
    public NutsText tokenToNode(String text, String nodeType,NutsSession session) {
        return factory.setSession(session).forPlain(text);
    }
    

    @Override
    public int getSupportLevel(NutsSupportLevelContext<String> criteria) {
        String s = criteria.getConstraints();
        return "java".equals(s) ? NutsComponent.DEFAULT_SUPPORT : NutsComponent.NO_SUPPORT;
    }

    @Override
    public NutsText textToNode(String text, NutsSession session) {
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
                    all.add(factory.forStyled(String.valueOf(ar.nextChar()), NutsTextNodeStyle.separator()));
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
                        all.add(factory.forStyled(String.valueOf(ar.nextChar()), NutsTextNodeStyle.separator()));
                    }
                    break;
                }
                case '/': {
                    if (ar.peekChars("//")) {
                        all.addAll(Arrays.asList(StringReaderExtUtils.readSlashSlashComments(session, ar)));
                    } else if (ar.peekChars("/*")) {
                        all.addAll(Arrays.asList(StringReaderExtUtils.readSlashStarComments(session, ar)));
                    } else {
                        all.add(factory.forStyled(String.valueOf(ar.nextChar()), NutsTextNodeStyle.separator()));
                    }
                    break;
                }
                default: {
                    if (Character.isWhitespace(ar.peekChar())) {
                        all.addAll(Arrays.asList(StringReaderExtUtils.readSpaces(session, ar)));
                    } else {
                        NutsText[] d = StringReaderExtUtils.readJSIdentifier(session, ar);
                        if (d != null) {
                            if (d.length == 1 && d[0].getType() == NutsTextNodeType.PLAIN) {
                                String txt = ((NutsTextPlain) d[0]).getText();
                                if (reservedWords.contains(txt)) {
                                    d[0] = factory.forStyled(d[0], NutsTextNodeStyle.keyword());
                                }
                            }
                            all.addAll(Arrays.asList(d));
                        } else {
                            all.add(factory.forStyled(String.valueOf(ar.nextChar()), NutsTextNodeStyle.separator()));
                        }
                    }
                    break;
                }
            }
        }
        return factory.forList(all.toArray(new NutsText[0]));
    }
}
