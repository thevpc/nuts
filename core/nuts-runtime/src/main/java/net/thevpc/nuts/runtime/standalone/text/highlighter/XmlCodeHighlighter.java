package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.collections.EvictingQueue;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StreamTokenizerExt;
import net.thevpc.nuts.runtime.standalone.xtra.expr.NutsToken;
import net.thevpc.nuts.spi.NutsComponent;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class XmlCodeHighlighter implements NutsCodeHighlighter {

    private final NutsWorkspace ws;

    public XmlCodeHighlighter(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public String getId() {
        return "xml";
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        String s = context.getConstraints();
        if(s==null){
            return DEFAULT_SUPPORT;
        }
        switch (s){
            case "xml":
            case "xsl":
            case "text/xml":
            case "text/html":
            case "html":
            case "sgml":
            {
                return NutsComponent.DEFAULT_SUPPORT;
            }
        }
        return NutsComponent.NO_SUPPORT;    }

    @Override
    public NutsText stringToText(String text, NutsTexts txt, NutsSession session) {
        StreamTokenizerExt st = new StreamTokenizerExt(new StringReader(text),session);
        st.xmlComments(true);
        st.doNotParseNumbers();
        st.wordChars('0', '9');
        st.wordChars('.', '.');
        st.wordChars('-', '-');

        List<NutsText> nodes = new ArrayList<>();
        int s;
        EvictingQueue<String> last = new EvictingQueue<>(3);
        while ((s = st.nextToken()) != StreamTokenizerExt.TT_EOF) {
            switch (s) {
                case StreamTokenizerExt.TT_SPACES: {
                    nodes.add(txt.ofPlain(st.image));
                    break;
                }
                case StreamTokenizerExt.TT_COMMENTS: {
                    nodes.add(txt.applyStyles(txt.ofPlain(st.image), NutsTextStyle.comments()));
                    break;
                }
                case NutsToken.TT_INT:
                case NutsToken.TT_LONG:
                case NutsToken.TT_BIG_INT:
                case NutsToken.TT_FLOAT:
                case NutsToken.TT_DOUBLE:
                case NutsToken.TT_BIG_DECIMAL:{
                    nodes.add(txt.applyStyles(txt.ofPlain(st.image), NutsTextStyle.number()));
                    break;
                }
                case StreamTokenizerExt.TT_WORD: {
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
                    nodes.add(txt.applyStyles(txt.ofPlain(st.image), NutsTextStyle.separator()));
                    break;
                }
                default: {
                    nodes.add(txt.applyStyles(txt.ofPlain(st.image), NutsTextStyle.separator()));
                }
            }
            last.add(st.image == null ? "" : st.image);
        }
        return txt.ofList(nodes).simplify();
    }

    public NutsText tokenToText(String text, String nodeType, NutsTexts txt, NutsSession session) {
        switch (NutsUtilStrings.trim(nodeType).toLowerCase()) {
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

    public NutsText formatNodeName(String text, NutsTexts txt) {
        return txt.applyStyles(txt.ofPlain(text), NutsTextStyle.keyword());
    }

    public NutsText formatNodeString(String text, NutsTexts txt) {
        return txt.applyStyles(txt.ofPlain(text), NutsTextStyle.string());
    }

    public NutsText formatNodeSeparator(String text, NutsTexts txt) {
        return txt.applyStyles(txt.ofPlain(text), NutsTextStyle.separator());
    }
}
