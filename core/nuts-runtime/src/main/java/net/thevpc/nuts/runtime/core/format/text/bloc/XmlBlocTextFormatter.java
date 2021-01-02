package net.thevpc.nuts.runtime.core.format.text.bloc;

import net.thevpc.nuts.NutsTextNode;
import net.thevpc.nuts.NutsTextNodeFactory;
import net.thevpc.nuts.NutsTextNodeStyle;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.standalone.util.common.StreamTokenizerExt;
import net.thevpc.nuts.runtime.core.format.text.parser.BlocTextFormatter;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class XmlBlocTextFormatter implements BlocTextFormatter {
    private NutsWorkspace ws;

    public XmlBlocTextFormatter(NutsWorkspace ws) {
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
                    nodes.add(factory.styled(factory.plain(st.image), NutsTextNodeStyle.comments()));
                    break;
                }
                case StreamTokenizerExt.TT_INTEGER:
                case StreamTokenizerExt.TT_DOUBLE:{
                    nodes.add(factory.styled(factory.plain(st.image),NutsTextNodeStyle.number()));
                    break;
                }
                case StreamTokenizerExt.TT_WORD:{
                    if(st.image.equals("true") || st.image.equals("false")) {
                        nodes.add(factory.styled(factory.plain(st.image),NutsTextNodeStyle.keyword()));
                    }else{
                        nodes.add(factory.styled(factory.plain(st.image)));
                    }
                    break;
                }
                case '\'':{
                    nodes.add(factory.styled(factory.plain(st.image),NutsTextNodeStyle.string()));
                    break;
                }
                case '\"':{
                    nodes.add(factory.styled(factory.plain(st.image),NutsTextNodeStyle.string()));
                    break;
                }
                case '<':
                case '>':
                case '&':
                case '=':
                {
                    nodes.add(factory.styled(factory.plain(st.image),NutsTextNodeStyle.separator()));
                    break;
                }
                default:{
                    nodes.add(factory.styled(factory.plain(st.image),NutsTextNodeStyle.separator()));
                }
            }
        }
        return factory.list(nodes);
    }
}
