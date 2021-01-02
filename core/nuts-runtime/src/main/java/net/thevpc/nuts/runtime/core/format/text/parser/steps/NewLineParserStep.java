package net.thevpc.nuts.runtime.core.format.text.parser.steps;

import net.thevpc.nuts.NutsIllegalArgumentException;
import net.thevpc.nuts.NutsTextNode;
import net.thevpc.nuts.NutsTextNodeStyle;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.format.text.DefaultNutsTextNodeFactory;
import net.thevpc.nuts.runtime.core.format.text.parser.DefaultNutsTextNodeParser;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;

import java.util.ArrayList;
import java.util.List;

public class NewLineParserStep extends ParserStep {

    StringBuilder start = new StringBuilder();
    private NutsWorkspace ws;
    public NewLineParserStep(char c, NutsWorkspace ws) {
        start.append(c);
        this.ws=ws;
    }

    @Override
    public void consume(char c, DefaultNutsTextNodeParser.State state) {
        if(c=='\n') {
            start.append(c);
        }else if(c=='#'){
            state.applyAppendSibling(new PlainParserStep(start.toString(),state.isSpreadLine(),false,ws,state,null));
            state.applyDropReplace(new StyledParserStep(c,state.isSpreadLine(),true,ws));
        }else{
            state.applyAppendSibling(new PlainParserStep('\n',state.isSpreadLine(),false,ws,state,null));
            state.applyDrop();
            state.applyStart(c,state.isSpreadLine(),true);
        }
    }

    @Override
    public void appendChild(ParserStep tt) {
        throw new NutsIllegalArgumentException(ws,"unsupported");
    }

    @Override
    public NutsTextNode toNode() {
        DefaultNutsTextNodeFactory factory0 = (DefaultNutsTextNodeFactory) ws.formats().text().factory();
        return factory0.plain(start.toString());
    }

    @Override
    public void end(DefaultNutsTextNodeParser.State p) {
        p.applyPop();
    }

    public boolean isComplete() {
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("NewLine(" + CoreStringUtils.dblQuote(start.toString()));
        return sb.append(")").toString();
    }

}
