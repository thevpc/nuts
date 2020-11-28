package net.thevpc.nuts.runtime.format.text.special;

import net.thevpc.nuts.NutsTextNodeFactory;
import net.thevpc.nuts.NutsTextNodeStyle;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.util.common.StreamTokenizerExt;
import net.thevpc.nuts.runtime.format.text.parser.SpecialTextFormatter;
import net.thevpc.nuts.NutsTextNode;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class JsonSpecialTextFormatter implements SpecialTextFormatter {
    private NutsWorkspace ws;

    public JsonSpecialTextFormatter(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsTextNode toNode(String text) {
        StreamTokenizerExt st=new StreamTokenizerExt(new StringReader(text));
        List<NutsTextNode> nodes=new ArrayList<>();
        int s;
        NutsTextNodeFactory factory = ws.formats().text().factory();
        while((s=st.nextToken())!=StreamTokenizerExt.TT_EOF){
            switch (s){
                case StreamTokenizerExt.TT_INTEGER:
                case StreamTokenizerExt.TT_DOUBLE:{
                    nodes.add(factory.styled(st.image, NutsTextNodeStyle.NUMBER1));
                    break;
                }
                case StreamTokenizerExt.TT_WORD:{
                    if(st.image.equals("true") || st.image.equals("false")) {
                        nodes.add(factory.styled(st.image,NutsTextNodeStyle.KEYWORD1));
                    }else{
                        nodes.add(factory.plain(st.image));
                    }
                    break;
                }
                case '\'':{
                    nodes.add(factory.styled(st.image,NutsTextNodeStyle.STRING1));
                    break;
                }
                case '\"':{
                    nodes.add(factory.styled(st.image,NutsTextNodeStyle.STRING1));
                    break;
                }
                case '{':
                case '}':
                case '[':
                case ']':
                    {
                    nodes.add(factory.styled(st.image,NutsTextNodeStyle.SEPARATOR1));
                    break;
                }
            }
        }
        return factory.list(nodes);
    }
}
