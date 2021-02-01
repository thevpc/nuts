package net.thevpc.nuts.runtime.core.format.text.bloc;

import net.thevpc.nuts.NutsTextNode;
import net.thevpc.nuts.NutsTextNodeFactory;
import net.thevpc.nuts.NutsTextNodeStyle;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.bundles.datastr.EvictingQueue;
import net.thevpc.nuts.runtime.bundles.parsers.StreamTokenizerExt;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import net.thevpc.nuts.NutsSupportLevelContext;
import net.thevpc.nuts.spi.NutsComponent;
import net.thevpc.nuts.NutsCodeFormat;

public class XmlBlocTextFormatter implements NutsCodeFormat {
    private NutsWorkspace ws;

    public XmlBlocTextFormatter(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<String> criteria) {
        String s = criteria.getConstraints();
        return "xml".equals(s) ? NutsComponent.DEFAULT_SUPPORT : NutsComponent.NO_SUPPORT;
    }

    @Override
    public NutsTextNode toNode(String text) {
        StreamTokenizerExt st=new StreamTokenizerExt(new StringReader(text));
        st.xmlComments(true);
        st.doNotParseNumbers();
        st.wordChars('0','9');
        st.wordChars('.','.');
        st.wordChars('-','-');

        List<NutsTextNode> nodes=new ArrayList<>();
        NutsTextNodeFactory factory = ws.formats().text().factory();
        int s;
        EvictingQueue<String> last=new EvictingQueue<>(3);
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
                    if(last.size()>0 && last.get(last.size()-1).equals("<")) {
                        nodes.add(factory.styled(factory.plain(st.image), NutsTextNodeStyle.keyword()));
                    }else if(last.size()>1 && last.get(last.size()-2).equals("<") && last.get(last.size()-1).equals("/")){
                        nodes.add(factory.styled(factory.plain(st.image), NutsTextNodeStyle.keyword()));
                    }else if(last.size()>1 && last.get(last.size()-2).equals("<") && last.get(last.size()-1).equals("?")){
                        nodes.add(factory.styled(factory.plain(st.image), NutsTextNodeStyle.keyword()));
                    }else {
                        if (st.image.equals("true") || st.image.equals("false")) {
                            nodes.add(factory.styled(factory.plain(st.image), NutsTextNodeStyle.keyword()));
                        } else {
                            nodes.add(factory.styled(factory.plain(st.image)));
                        }
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
            last.add(st.image==null?"":st.image);
        }
        return factory.list(nodes);
    }
}
