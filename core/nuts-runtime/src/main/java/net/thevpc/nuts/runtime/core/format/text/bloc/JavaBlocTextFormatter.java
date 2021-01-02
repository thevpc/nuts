package net.thevpc.nuts.runtime.core.format.text.bloc;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.format.text.parser.BlocTextFormatter;
import net.thevpc.nuts.runtime.standalone.util.common.StringReaderExt;

import java.util.*;

public class JavaBlocTextFormatter implements BlocTextFormatter {
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

    public JavaBlocTextFormatter(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsTextNode toNode(String text) {
        NutsTextNodeFactory factory = ws.formats().text().factory();
        List<NutsTextNode> all = new ArrayList<>();
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
                case ';':
                    {
                    all.add(factory.styled(String.valueOf(ar.nextChar()), NutsTextNodeStyle.separator()));
                    break;
                }
                case '\'': {
                    all.addAll(Arrays.asList(StringReaderExtUtils.readJSSimpleQuotes(ws, ar)));
                    break;
                }
                case '"': {
                    all.addAll(Arrays.asList(StringReaderExtUtils.readJSDoubleQuotesString(ws, ar)));
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
                    all.addAll(Arrays.asList(StringReaderExtUtils.readNumber(ws, ar)));
                    break;
                }
                case '.':
                case '-':{
                    NutsTextNode[] d = StringReaderExtUtils.readNumber(ws, ar);
                    if(d!=null) {
                        all.addAll(Arrays.asList(d));
                    }else{
                        all.add(factory.styled(String.valueOf(ar.nextChar()), NutsTextNodeStyle.separator()));
                    }
                    break;
                }
                case '/':{
                    if(ar.peekChars("//")) {
                        all.addAll(Arrays.asList(StringReaderExtUtils.readSlashSlashComments(ws,ar)));
                    }else if(ar.peekChars("/*")){
                        all.addAll(Arrays.asList(StringReaderExtUtils.readSlashStarComments(ws,ar)));
                    }else{
                        all.add(factory.styled(String.valueOf(ar.nextChar()), NutsTextNodeStyle.separator()));
                    }
                    break;
                }
                default: {
                    if(Character.isWhitespace(ar.peekChar())){
                        all.addAll(Arrays.asList(StringReaderExtUtils.readSpaces(ws,ar)));
                    }else {
                        NutsTextNode[] d = StringReaderExtUtils.readJSIdentifier(ws, ar);
                        if (d != null) {
                            if (d.length == 1 && d[0].getType() == NutsTextNodeType.PLAIN) {
                                String txt = ((NutsTextNodePlain) d[0]).getText();
                                if(reservedWords.contains(txt)){
                                    d[0] = factory.styled(d[0], NutsTextNodeStyle.keyword());
                                }
                            }
                            all.addAll(Arrays.asList(d));
                        } else {
                            all.add(factory.styled(String.valueOf(ar.nextChar()), NutsTextNodeStyle.separator()));
                        }
                    }
                    break;
                }
            }
        }
        return factory.list(all.toArray(new NutsTextNode[0]));
    }
}
