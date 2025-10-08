package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.runtime.standalone.xtra.expr.StringReaderExt;

import java.util.*;

import net.thevpc.nuts.spi.NCodeHighlighter;
import net.thevpc.nuts.spi.NScorableContext;
import net.thevpc.nuts.text.*;

public class JavaCodeHighlighter implements NCodeHighlighter {

    private Set<String> reservedWords = new HashSet<>();
    public JavaCodeHighlighter() {
        reservedWords.addAll(NCodeHighlighterHelper.loadNames("java.kw1",getClass()));
    }

    @Override
    public String getId() {
        return "java";
    }

    @Override
    public NText tokenToText(String text, String nodeType, NTexts txt) {
        return txt.ofPlain(text);
    }
    

    @Override
    public int getScore(NScorableContext context) {
        String s = context.getCriteria();
        if(s==null){
            return DEFAULT_SCORE;
        }
        switch (s){
            case "java":
            case "jav":
            case "text/x-java":
            {
                return DEFAULT_SCORE;
            }
        }
        return UNSUPPORTED_SCORE;
    }

    @Override
    public NText stringToText(String text, NTexts txt) {
        List<NText> all = new ArrayList<>();
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
                    all.add(txt.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
                    break;
                }
                case '\'': {
                    all.addAll(Arrays.asList(StringReaderExtUtils.readJSSimpleQuotes(ar)));
                    break;
                }
                case '"': {
                    all.addAll(Arrays.asList(StringReaderExtUtils.readJSDoubleQuotesString(ar)));
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
                    all.addAll(Arrays.asList(StringReaderExtUtils.readNumber(ar)));
                    break;
                }
                case '.':
                case '-': {
                    NText[] d = StringReaderExtUtils.readNumber(ar);
                    if (d != null) {
                        all.addAll(Arrays.asList(d));
                    } else {
                        all.add(txt.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
                    }
                    break;
                }
                case '/': {
                    if (ar.peekChars("//")) {
                        all.addAll(Arrays.asList(StringReaderExtUtils.readSlashSlashComments(ar)));
                    } else if (ar.peekChars("/*")) {
                        all.addAll(Arrays.asList(StringReaderExtUtils.readSlashStarComments(ar)));
                    } else {
                        all.add(txt.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
                    }
                    break;
                }
                default: {
                    if (Character.isWhitespace(ar.peekChar())) {
                        all.addAll(Arrays.asList(StringReaderExtUtils.readSpaces(ar)));
                    } else {
                        NText[] d = StringReaderExtUtils.readJSIdentifier(ar);
                        if (d != null) {
                            if (d.length == 1 && d[0].type() == NTextType.PLAIN) {
                                String txt2 = ((NTextPlain) d[0]).getValue();
                                if (reservedWords.contains(txt2)) {
                                    d[0] = txt.ofStyled(d[0], NTextStyle.keyword());
                                }
                            }
                            all.addAll(Arrays.asList(d));
                        } else {
                            all.add(txt.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
                        }
                    }
                    break;
                }
            }
        }
        return txt.ofList(all.toArray(new NText[0]));
    }
}
