package net.thevpc.nuts.runtime.standalone.xtra.expr.template;

import net.thevpc.nuts.util.NLiteral;

import java.io.IOException;

class PlainTagNode extends TagNode {
    private String value;

    public PlainTagNode(String value) {
        this.value = value;
    }

    public void run(ProcessStreamContext ctx) throws IOException {
        ctx.out.write(value);
    }

    @Override
    public String toString() {
        String a=value;
        if(a.length()>20){
            a=a.substring(0,20)+"...";
        }
        return "Plain(" + NLiteral.of(a).toStringLiteral() + ')';
    }
}
