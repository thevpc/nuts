package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.expr.NExprContext;
import net.thevpc.nuts.expr.NExprVar;
import net.thevpc.nuts.expr.NExprVarReader;
import net.thevpc.nuts.expr.NExprVarWriter;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NIllegalArgumentException;

import java.util.function.Function;

public class NExprVarFromFunction implements NExprVar {
    private final NExprVarReader r;
    private final NExprVarWriter w;
    private final String name;

    public NExprVarFromFunction(String name, NExprVarReader reader, NExprVarWriter writer) {
        NAssert.requireNamedNonBlank(name, "name");
        this.r = reader;
        this.w = writer;
        if (writer == null && reader == null) {
            NAssert.requireNamedNonNull(reader, "reader");
        }
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public Object get(NExprContext context) {
        if (r == null) {
            throw new NIllegalArgumentException(NMsg.ofC("write only variable %s", name));
        }
        return r.get(context);
    }

    @Override
    public void set(Object value, NExprContext context) {
        if (w == null) {
            throw new NIllegalArgumentException(NMsg.ofC("readonly only variable %s", name));
        }
        w.set(value, context);
    }
}
