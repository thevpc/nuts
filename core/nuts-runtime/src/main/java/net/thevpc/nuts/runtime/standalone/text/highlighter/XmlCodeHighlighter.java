package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.lib.common.collections.EvictingQueue;
import net.thevpc.nuts.lib.common.str.NStreamTokenizer;
import net.thevpc.nuts.expr.NToken;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NStringUtils;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class XmlCodeHighlighter implements NCodeHighlighter {

    private final NWorkspace ws;

    public XmlCodeHighlighter(NWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public String getId() {
        return "xml";
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        String s = context.getConstraints();
        if(s==null){
            return NConstants.Support.DEFAULT_SUPPORT;
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
                return NConstants.Support.DEFAULT_SUPPORT;
            }
        }
        return NConstants.Support.NO_SUPPORT;
    }

    @Override
    public NText stringToText(String text, NTexts txt, NSession session) {
        NStreamTokenizer st = new NStreamTokenizer(new StringReader(text),session);
        st.xmlComments(true);
        st.parseNumbers(false);
        st.wordChars('0', '9');
        st.wordChars('.', '.');
        st.wordChars('-', '-');

        List<NText> nodes = new ArrayList<>();
        int s;
        EvictingQueue<String> last = new EvictingQueue<>(3);
        while ((s = st.nextToken()) != NStreamTokenizer.TT_EOF) {
            switch (s) {
                case NStreamTokenizer.TT_SPACES: {
                    nodes.add(txt.ofPlain(st.image));
                    break;
                }
                case NStreamTokenizer.TT_COMMENTS: {
                    nodes.add(txt.ofStyled(st.image, NTextStyle.comments()));
                    break;
                }
                case NToken.TT_INT:
                case NToken.TT_LONG:
                case NToken.TT_BIG_INT:
                case NToken.TT_FLOAT:
                case NToken.TT_DOUBLE:
                case NToken.TT_BIG_DECIMAL:{
                    nodes.add(txt.ofStyled(st.image, NTextStyle.number()));
                    break;
                }
                case NStreamTokenizer.TT_WORD: {
                    if (last.size() > 0 && last.get(last.size() - 1).equals("<")) {
                        nodes.add(formatNodeName(st.image, txt));
                    } else if (last.size() > 1 && last.get(last.size() - 2).equals("<") && last.get(last.size() - 1).equals("/")) {
                        nodes.add(formatNodeName(st.image, txt));
                    } else if (last.size() > 1 && last.get(last.size() - 2).equals("<") && last.get(last.size() - 1).equals("?")) {
                        nodes.add(formatNodeName(st.image, txt));
                    } else {
                        if (st.image.equals("true") || st.image.equals("false")) {
                            nodes.add(formatNodeName(st.image, txt));
                        } else {
                            nodes.add(txt.ofPlain(st.image));
                        }
                    }
                    break;
                }
                case '\'': {
                    nodes.add(formatNodeString(st.image, txt));
                    break;
                }
                case '\"': {
                    nodes.add(formatNodeString(st.image, txt));
                    break;
                }
                case '<':
                case '>':
                case '&':
                case '=': {
                    nodes.add(txt.ofStyled(st.image, NTextStyle.separator()));
                    break;
                }
                default: {
                    nodes.add(txt.ofStyled(st.image, NTextStyle.separator()));
                }
            }
            last.add(st.image == null ? "" : st.image);
        }
        return txt.ofList(nodes).simplify();
    }

    public NText tokenToText(String text, String nodeType, NTexts txt, NSession session) {
        switch (NStringUtils.trim(nodeType).toLowerCase()) {
            case "name":
                return formatNodeName(text, txt);
            case "attribute":
                return formatNodeName(text, txt);
            case "string":
                return formatNodeString(text, txt);
            case "<":
            case "<?":
            case "</":
            case ">":
            case "&":
            case "=":
            case "separator":
                return formatNodeSeparator(text, txt);
        }
        return txt.ofPlain(text);
    }

    public NText formatNodeName(String text, NTexts txt) {
        return txt.ofStyled(text, NTextStyle.keyword());
    }

    public NText formatNodeString(String text, NTexts txt) {
        return txt.ofStyled(text, NTextStyle.string());
    }

    public NText formatNodeSeparator(String text, NTexts txt) {
        return txt.ofStyled(text, NTextStyle.separator());
    }
}
