package net.thevpc.nuts.runtime.standalone.text.parser.v1;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.text.DefaultNTexts;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NStringUtils;


public class NewLineParserStep extends ParserStep {

    StringBuilder start = new StringBuilder();
    private NSession session;
    public NewLineParserStep(char c, NSession session) {
        start.append(c);
        this.session=session;
    }

    @Override
    public void consume(char c, DefaultNTextNodeParser.State state, boolean wasNewLine) {
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
        throw new NUnsupportedOperationException(session);
    }

    @Override
    public NText toText() {
        DefaultNTexts factory0 = (DefaultNTexts) NTexts.of(session);
        return factory0.ofPlain(start.toString());
    }

    @Override
    public void end(DefaultNTextNodeParser.State p) {
        p.applyPop(this);
    }

    public boolean isComplete() {
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("NewLine(" + NStringUtils.formatStringLiteral(start.toString(), NStringUtils.QuoteType.DOUBLE));
        return sb.append(")").toString();
    }

}
