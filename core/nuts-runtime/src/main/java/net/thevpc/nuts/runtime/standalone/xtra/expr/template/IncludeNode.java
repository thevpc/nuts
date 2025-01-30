package net.thevpc.nuts.runtime.standalone.xtra.expr.template;

import net.thevpc.nuts.expr.NExprNode;
import net.thevpc.nuts.io.NPath;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

class IncludeNode extends TagNode {
    private NExprNode expr;

    public IncludeNode(NExprNode expr) {
        this.expr = expr;
    }

    public void run(ProcessStreamContext ctx) throws IOException {
        Object eval = ctx.eval(expr);
        try (InputStream in = NPath.of((String) eval).getInputStream()) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ctx.getStreamProcessor().processStream(in, bos, ctx.context);
            String s = new String(bos.toByteArray());
            ctx.out.write(s);

//            WriterOutputStream wout = new WriterOutputStream(ctx.out);
//            ctx.getStreamProcessor().processStream(in, wout,ctx.context);
//            wout.flush();
        }
    }

    @Override
    public String toString() {
        return "Include(" + expr + ")";
    }
}
