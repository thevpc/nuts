package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.reflect.NScorableContext;
import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringReaderExt;
import net.thevpc.nuts.spi.NCodeHighlighter;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.util.NStringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * XML/HTML highlighter.
 * <p>
 * This is a small dedicated lexer rather than a token-stream post processor:
 * the tokenizer merges adjacent operator characters, so a closing tag arrives
 * as {@code "></"} and a token-history heuristic cannot reliably recognize it.
 */
public class XmlCodeHighlighter implements NCodeHighlighter {

    public XmlCodeHighlighter() {
    }

    @Override
    public String id() {
        return "xml";
    }

    @NScore
    public static int getScore(NScorableContext context) {
        String s = context.criteria();
        if (s == null) {
            return NScorable.DEFAULT_SCORE;
        }
        switch (s) {
            case "xml":
            case "xsl":
            case "xslt":
            case "html":
            case "htm":
            case "xhtml":
            case "svg":
            case "sgml":
            case "application/xml":
            case "application/xhtml+xml":
            case "application/xhtml":
            case "text/xml":
            case "text/html":
            case "image/svg+xml": {
                return NScorable.DEFAULT_SCORE;
            }
        }
        return NScorable.UNSUPPORTED_SCORE;
    }

    @Override
    public NText stringToText(String text) {
        List<NText> all = new ArrayList<>();
        StringReaderExt ar = new StringReaderExt(text);
        while (ar.hasNext()) {
            char c = ar.peekChar();
            if (c == '<') {
                readMarkup(ar, all);
            } else if (c == '&') {
                readEntity(ar, all);
            } else if (Character.isWhitespace(c)) {
                all.addAll(Arrays.asList(StringReaderExtUtils.readSpaces(ar)));
            } else {
                // text content
                StringBuilder sb = new StringBuilder();
                while (ar.hasNext() && ar.peekChar() != '<' && ar.peekChar() != '&') {
                    sb.append(ar.readChar());
                }
                all.add(NText.ofPlain(sb.toString()));
            }
        }
        return NText.ofList(all.toArray(new NText[0]));
    }

    private void readMarkup(StringReaderExt ar, List<NText> all) {
        if (startsWith(ar, "<!--")) {
            all.add(readUntil(ar, "-->", NTextStyle.comments()));
            return;
        }
        if (startsWith(ar, "<![CDATA[")) {
            all.add(readUntil(ar, "]]>", NTextStyle.string()));
            return;
        }
        if (startsWith(ar, "<!")) {
            all.add(readUntil(ar, ">", NTextStyle.annotation()));
            return;
        }
        if (startsWith(ar, "<?")) {
            all.add(NText.ofStyled(ar.nextChars(2), NTextStyle.separator()));
            readWhitespace(ar, all);
            if (ar.hasNext()) {
                all.add(NText.ofStyled(readName(ar), NTextStyle.keyword()));
            }
            readTagBody(ar, all, "?>");
            return;
        }
        if (startsWith(ar, "</")) {
            all.add(NText.ofStyled(ar.nextChars(2), NTextStyle.separator()));
            readWhitespace(ar, all);
            if (ar.hasNext()) {
                all.add(NText.ofStyled(readName(ar), NTextStyle.keyword()));
            }
            readTagBody(ar, all, ">");
            return;
        }
        // start tag
        all.add(NText.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
        readWhitespace(ar, all);
        if (ar.hasNext()) {
            all.add(NText.ofStyled(readName(ar), NTextStyle.keyword()));
        }
        readTagBody(ar, all, ">");
    }

    private void readTagBody(StringReaderExt ar, List<NText> all, String close) {
        while (ar.hasNext()) {
            if (startsWith(ar, close)) {
                all.add(NText.ofStyled(ar.nextChars(close.length()), NTextStyle.separator()));
                return;
            }
            char c = ar.peekChar();
            if (Character.isWhitespace(c)) {
                readWhitespace(ar, all);
            } else if (c == '>' || c == '/' || c == '=') {
                all.add(NText.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
            } else if (c == '"' || c == '\'') {
                all.addAll(Arrays.asList(c == '"'
                        ? StringReaderExtUtils.readJSDoubleQuotesString(ar)
                        : StringReaderExtUtils.readJSSimpleQuotes(ar)));
            } else if (c == '&') {
                readEntity(ar, all);
            } else {
                String name = readName(ar);
                if (name.isEmpty()) {
                    all.add(NText.ofPlain(String.valueOf(ar.readChar())));
                } else {
                    all.add(NText.ofStyled(name, NTextStyle.option()));
                }
            }
        }
    }

    private String readName(StringReaderExt ar) {
        StringBuilder sb = new StringBuilder();
        while (ar.hasNext()) {
            char c = ar.peekChar();
            if (Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == ':' || c == '.') {
                sb.append(ar.readChar());
            } else {
                break;
            }
        }
        return sb.toString();
    }

    private void readWhitespace(StringReaderExt ar, List<NText> all) {
        if (ar.hasNext() && Character.isWhitespace(ar.peekChar())) {
            all.addAll(Arrays.asList(StringReaderExtUtils.readSpaces(ar)));
        }
    }

    private void readEntity(StringReaderExt ar, List<NText> all) {
        StringBuilder sb = new StringBuilder();
        sb.append(ar.readChar()); // &
        while (ar.hasNext() && ar.peekChar() != ';' && !Character.isWhitespace(ar.peekChar())
                && ar.peekChar() != '<' && ar.peekChar() != '&') {
            sb.append(ar.readChar());
        }
        if (ar.hasNext() && ar.peekChar() == ';') {
            sb.append(ar.readChar());
        }
        all.add(NText.ofStyled(sb.toString(), NTextStyle.annotation()));
    }

    private NText readUntil(StringReaderExt ar, String end, NTextStyle style) {
        StringBuilder sb = new StringBuilder();
        while (ar.hasNext()) {
            if (startsWith(ar, end)) {
                sb.append(ar.nextChars(end.length()));
                break;
            }
            sb.append(ar.readChar());
        }
        return NText.ofStyled(sb.toString(), style);
    }

    private boolean startsWith(StringReaderExt ar, String s) {
        return ar.peekChars(s.length()).equals(s);
    }

    public NText tokenToText(String text, String nodeType) {
        switch (NStringUtils.strip(nodeType).toLowerCase()) {
            case "name":
            case "attribute":
                return formatNodeName(text);
            case "string":
                return formatNodeString(text);
            case "<":
            case "<?":
            case "</":
            case ">":
            case "&":
            case "=":
            case "separator":
                return formatNodeSeparator(text);
        }
        return NText.ofPlain(text);
    }

    public NText formatNodeName(String text) {
        return NText.ofStyled(text, NTextStyle.keyword());
    }

    public NText formatNodeString(String text) {
        return NText.ofStyled(text, NTextStyle.string());
    }

    public NText formatNodeSeparator(String text) {
        return NText.ofStyled(text, NTextStyle.separator());
    }
}
