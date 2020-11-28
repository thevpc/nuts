package net.thevpc.nuts.runtime.format.text.special;

import net.thevpc.nuts.NutsTextNode;
import net.thevpc.nuts.NutsTextNodeFactory;
import net.thevpc.nuts.NutsTextNodeStyle;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.util.common.StreamTokenizerExt;
import net.thevpc.nuts.runtime.format.text.parser.SpecialTextFormatter;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class XmlSpecialTextFormatter implements SpecialTextFormatter {
    private NutsWorkspace ws;

    public XmlSpecialTextFormatter(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsTextNode toNode(String text) {
        StreamTokenizerExt st=new StreamTokenizerExt(new StringReader(text));
        st.xmlComments(true);
        List<NutsTextNode> nodes=new ArrayList<>();
        NutsTextNodeFactory factory = ws.formats().text().factory();
        int s;
        while((s=st.nextToken())!=StreamTokenizerExt.TT_EOF){
            switch (s){
                case StreamTokenizerExt.TT_SPACES:{
                    nodes.add(factory.plain(st.image));
                    break;
                }
                case StreamTokenizerExt.TT_COMMENTS:{
                    nodes.add(factory.styled(factory.plain(st.image), NutsTextNodeStyle.COMMENTS1));
                    break;
                }
                case StreamTokenizerExt.TT_INTEGER:
                case StreamTokenizerExt.TT_DOUBLE:{
                    nodes.add(factory.styled(factory.plain(st.image),NutsTextNodeStyle.NUMBER1));
                    break;
                }
                case StreamTokenizerExt.TT_WORD:{
                    if(st.image.equals("true") || st.image.equals("false")) {
                        nodes.add(factory.styled(factory.plain(st.image),NutsTextNodeStyle.KEYWORD1));
                    }else{
                        nodes.add(factory.styled(factory.plain(st.image)));
                    }
                    break;
                }
                case '\'':{
                    nodes.add(factory.styled(factory.plain(st.image),NutsTextNodeStyle.STRING1));
                    break;
                }
                case '\"':{
                    nodes.add(factory.styled(factory.plain(st.image),NutsTextNodeStyle.STRING1));
                    break;
                }
                case '<':
                case '>':
                case '&':
                case '=':
                {
                    nodes.add(factory.styled(factory.plain(st.image),NutsTextNodeStyle.SEPARATOR1));
                    break;
                }
                default:{
                    nodes.add(factory.styled(factory.plain(st.image),NutsTextNodeStyle.SEPARATOR1));
                }
            }
        }
        return factory.list(nodes);
    }
}
