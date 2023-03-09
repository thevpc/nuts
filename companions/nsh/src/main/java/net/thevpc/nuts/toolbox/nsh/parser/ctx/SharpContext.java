package net.thevpc.nuts.toolbox.nsh.parser.ctx;

import net.thevpc.nuts.toolbox.nsh.parser.AbstractContext;
import net.thevpc.nuts.toolbox.nsh.parser.NShellParser;
import net.thevpc.nuts.toolbox.nsh.parser.StrReader;
import net.thevpc.nuts.toolbox.nsh.parser.Token;

public class SharpContext extends AbstractContext {
    public SharpContext(NShellParser jshp) {
        super(jshp);
    }

    @Override
    public Token nextToken() {
        StrReader reader = this.reader.strReader();
        StringBuilder sb=new StringBuilder();
        int t = reader.peekChar();
        if(t!='#'){
            return null;
        }
        reader.read();
        while(true){
            int r = reader.peekChar();
            if (r < 0) {
                break;
            }
            char rc=(char)r;
            if (rc == '\n') {
                break;
            }else{
                reader.read();
                sb.append(rc);
            }
        }
        return new Token("#",sb.toString(),"#"+sb.toString());
    }
}
