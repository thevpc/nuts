package net.thevpc.nuts.runtime.core.format.text.bloc;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.collections.EvictingQueue;
import net.thevpc.nuts.runtime.bundles.parsers.StreamTokenizerExt;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import net.thevpc.nuts.spi.NutsComponent;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;

public class XmlCodeFormatter implements NutsCodeFormat {

    private NutsWorkspace ws;
    NutsTextManager factory;

    public XmlCodeFormatter(NutsWorkspace ws) {
        this.ws = ws;
        factory = ws.text();
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<String> context) {
        String s = context.getConstraints();
        return "xml".equals(s) ? NutsComponent.DEFAULT_SUPPORT : NutsComponent.NO_SUPPORT;
    }

    public NutsText tokenToText(String text, String nodeType,NutsSession session) {
        factory.setSession(session);
        switch (NutsUtilStrings.trim(nodeType).toLowerCase()) {
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
        return factory.forPlain(text);
    }

    public NutsText formatNodeName(String text) {
        return factory.forStyled(factory.forPlain(text), NutsTextStyle.keyword());
    }

    public NutsText formatNodeString(String text) {
        return factory.forStyled(factory.forPlain(text), NutsTextStyle.string());
    }

    public NutsText formatNodeSeparator(String text) {
        return factory.forStyled(factory.forPlain(text), NutsTextStyle.separator());
    }

    @Override
    public NutsText stringToText(String text, NutsSession session) {
        factory.setSession(session);
        StreamTokenizerExt st = new StreamTokenizerExt(new StringReader(text));
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
                    nodes.add(factory.forPlain(st.image));
                    break;
                }
                case StreamTokenizerExt.TT_COMMENTS: {
                    nodes.add(factory.forStyled(factory.forPlain(st.image), NutsTextStyle.comments()));
                    break;
                }
                case StreamTokenizerExt.TT_INTEGER:
                case StreamTokenizerExt.TT_DOUBLE: {
                    nodes.add(factory.forStyled(factory.forPlain(st.image), NutsTextStyle.number()));
                    break;
                }
                case StreamTokenizerExt.TT_WORD: {
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
                            nodes.add(factory.forPlain(st.image));
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
                    nodes.add(factory.forStyled(factory.forPlain(st.image), NutsTextStyle.separator()));
                    break;
                }
                default: {
                    nodes.add(factory.forStyled(factory.forPlain(st.image), NutsTextStyle.separator()));
                }
            }
            last.add(st.image == null ? "" : st.image);
        }
        return factory.forList(nodes).simplify();
    }
}
