package net.thevpc.nuts.toolbox.nsh.parser.ctx;

import net.thevpc.nuts.toolbox.nsh.parser.AbstractContext;
import net.thevpc.nuts.toolbox.nsh.parser.NShellParser;
import net.thevpc.nuts.toolbox.nsh.parser.StrReader;
import net.thevpc.nuts.toolbox.nsh.parser.Token;

public class SimpleQuotedContext extends AbstractContext {
    boolean processed=false;
    public SimpleQuotedContext(NShellParser jshp) {
        super(jshp);
    }

    @Override
    public Token nextToken() {
        if(processed){
            return null;
        }
        StrReader reader = this.reader.strReader();
        StringBuilder sb=new StringBuilder();
        while(true){
            int r = reader.read();
            if (r < 0) {
                if(sb.length()==0){
                    return null;
                }
                break;
            }
            char rc=(char)r;
            if (rc == '\'') {
                break;
            }else{
                sb.append(rc);
            }
        }
        processed=true;
        return new Token("'",sb.toString(),"'"+sb.toString()+"'");
    }
}
