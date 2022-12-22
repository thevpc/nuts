package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.NutsCodeHighlighter;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringReaderExt;
import net.thevpc.nuts.spi.NutsComponent;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.text.*;

import java.util.*;

public class SqlCodeHighlighter implements NutsCodeHighlighter {

    private static Set<String> reservedWords = new LinkedHashSet<>(
            Arrays.asList(
                    "add", "constraint", "all", "alter", "column", "table",
                    "and", "any", "as", "asc", "backup", "database",
                    "between", "case", "check", "column", "create", "index",
                    "replace", "procedure", "view", "unique", "default", "delete",
                    "desc", "distinct", "drop", "exec", "exists", "foreign",
                    "key", "full", "outer", "join", "left", "right",
                    "group", "by", "having", "in", "inner", "into",
                    "insert", "select", "null", "not", "like", "limit",
                    "or", "order", "by", "primary", "rownum", "top", "set",
                    "truncate", "union", "all", "update", "values", "where","from"
            )
    );
    private NutsWorkspace ws;

    public SqlCodeHighlighter(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public String getId() {
        return "sql";
    }

    @Override
    public NutsText tokenToText(String text, String nodeType, NutsTexts txt, NutsSession session) {
        return txt.setSession(session).ofPlain(text);
    }
    

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        String s = context.getConstraints();
        if(s==null){
            return DEFAULT_SUPPORT;
        }
        switch (s){
            case "sql":
            case "text/sql":
            {
                return NutsComponent.DEFAULT_SUPPORT;
            }
        }
        return NutsComponent.NO_SUPPORT;
    }

    @Override
    public NutsText stringToText(String text, NutsTexts txt, NutsSession session) {
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
                    all.add(txt.ofStyled(String.valueOf(ar.nextChar()), NutsTextStyle.separator()));
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
                        all.add(txt.ofStyled(String.valueOf(ar.nextChar()), NutsTextStyle.separator()));
                    }
                    break;
                }
                case '/': {
                    if (ar.peekChars("//")) {
                        all.addAll(Arrays.asList(StringReaderExtUtils.readSlashSlashComments(session, ar)));
                    } else if (ar.peekChars("/*")) {
                        all.addAll(Arrays.asList(StringReaderExtUtils.readSlashStarComments(session, ar)));
                    } else {
                        all.add(txt.ofStyled(String.valueOf(ar.nextChar()), NutsTextStyle.separator()));
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
                                String txt2 = ((NutsTextPlain) d[0]).getText();
                                if (reservedWords.contains(txt2.toLowerCase())) {
                                    d[0] = txt.ofStyled(d[0], NutsTextStyle.keyword());
                                }
                            }
                            all.addAll(Arrays.asList(d));
                        } else {
                            all.add(txt.ofStyled(String.valueOf(ar.nextChar()), NutsTextStyle.separator()));
                        }
                    }
                    break;
                }
            }
        }
        return txt.ofList(all.toArray(new NutsText[0]));
    }
}
