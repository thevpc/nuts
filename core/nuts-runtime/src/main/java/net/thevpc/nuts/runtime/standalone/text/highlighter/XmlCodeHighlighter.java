package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.io.NStreamTokenizer;
import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.reflect.NScorableContext;
import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.runtime.standalone.collections.NEvictingQueueImpl;
import net.thevpc.nuts.spi.NCodeHighlighter;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.expr.NToken;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

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
        if(s==null){
            return NScorable.DEFAULT_SCORE;
        }
        switch (s){
            case "xml":
            case "xsl":
            case "application/xml":
            case "text/xml":
            case "text/html":
            case "html":
            case "sgml":
            {
                return NScorable.DEFAULT_SCORE;
            }
        }
        return NScorable.UNSUPPORTED_SCORE;
    }

    @Override
    public NText stringToText(String text) {
        NStreamTokenizer st = new NStreamTokenizer(new StringReader(text));
        st.xmlComments(true);
        st.parseNumbers(false);
        st.wordChars('0', '9');
        st.wordChars('.', '.');
        st.wordChars('-', '-');

        List<NText> nodes = new ArrayList<>();
        int s;
        NEvictingQueueImpl<String> last = new NEvictingQueueImpl<>(3);
        while ((s = st.nextToken()) != NToken.TT_EOF) {
            switch (s) {
                case NToken.TT_SPACE: {
                    nodes.add(NText.ofPlain(st.image));
                    break;
                }
                case NToken.TT_COMMENTS: {
                    nodes.add(NText.ofStyled(st.image, NTextStyle.comments()));
                    break;
                }
                case NToken.TT_INT:
                case NToken.TT_LONG:
                case NToken.TT_BIG_INT:
                case NToken.TT_FLOAT:
                case NToken.TT_DOUBLE:
                case NToken.TT_BIG_DECIMAL:{
                    nodes.add(NText.ofStyled(st.image, NTextStyle.number()));
                    break;
                }
                case NToken.TT_WORD: {
                    if (last.size() > 0 && last.get(last.size() - 1).equals("<")) {
                        nodes.add(formatNodeName(st.image));
                    } else if (last.size() > 1 && last.get(last.size() - 2).equals("<") && last.get(last.size() - 1).equals("/")) {
                        nodes.add(formatNodeName(st.image));
                    } else if (last.size() > 1 && last.get(last.size() - 2).equals("<") && last.get(last.size() - 1).equals("?")) {
                        nodes.add(formatNodeName(st.image));
                    } else {
                        if (st.image.equals("true") || st.image.equals("false")) {
                            nodes.add(formatNodeName(st.image));
                        } else {
                            nodes.add(NText.ofPlain(st.image));
                        }
                    }
                    break;
                }
                case '\'': {
                    nodes.add(formatNodeString(st.image));
                    break;
                }
                case '\"': {
                    nodes.add(formatNodeString(st.image));
                    break;
                }
                case '<':
                case '>':
                case '&':
                case '=': {
                    nodes.add(NText.ofStyled(st.image, NTextStyle.separator()));
                    break;
                }
                default: {
                    nodes.add(NText.ofStyled(st.image, NTextStyle.separator()));
                }
            }
            last.add(st.image == null ? "" : st.image);
        }
        return NText.ofList(nodes).simplify();
    }

    public NText tokenToText(String text, String nodeType) {
        switch (NStringUtils.strip(nodeType).toLowerCase()) {
            case "name":
                return formatNodeName(text);
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
