package net.thevpc.nuts.runtime.core.format.text.parser.steps;

import net.thevpc.nuts.NutsIllegalArgumentException;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.core.format.text.DefaultNutsTextManager;
import net.thevpc.nuts.runtime.core.format.text.parser.DefaultNutsTextNodeParser;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.NutsText;


public class NewLineParserStep extends ParserStep {

    StringBuilder start = new StringBuilder();
    private NutsSession session;
    public NewLineParserStep(char c, NutsSession session) {
        start.append(c);
        this.session=session;
    }

    @Override
    public void consume(char c, DefaultNutsTextNodeParser.State state, boolean wasNewLine) {
        if(c=='\n') {
            start.append(c);
        }else /*if(c=='#')*/{
            state.applyPopReplay(c);
//        }else if(c=='#'){
//            state.applyAppendSibling(new PlainParserStep(start.toString(),state.isSpreadLine(),false,session.getWorkspace(),state,null));
//            state.applyDropReplace(new StyledParserStep(c,true,session.getWorkspace(),state));
//        }else{
//            state.applyAppendSibling(new PlainParserStep(start.toString(),false,true,session.getWorkspace(),state,null));
//            state.applyDrop();
//            state.applyStart(c,state.isSpreadLine(),true);
        }
    }

    @Override
    public void appendChild(ParserStep tt) {
        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unsupported"));
    }

    @Override
    public NutsText toText() {
        DefaultNutsTextManager factory0 = (DefaultNutsTextManager) session.text();
        return factory0.ofPlain(start.toString());
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
