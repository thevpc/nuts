package net.thevpc.nuts.runtime.standalone.text.parser.v1;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.text.DefaultNutsTexts;


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
            state.applyPopReplay(this, c);
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
        DefaultNutsTexts factory0 = (DefaultNutsTexts) NutsTexts.of(session);
        return factory0.ofPlain(start.toString());
    }

    @Override
    public void end(DefaultNutsTextNodeParser.State p) {
        p.applyPop(this);
    }

    public boolean isComplete() {
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("NewLine(" + NutsUtilStrings.formatStringLiteral(start.toString(), NutsUtilStrings.QuoteType.DOUBLE));
        return sb.append(")").toString();
    }

}
