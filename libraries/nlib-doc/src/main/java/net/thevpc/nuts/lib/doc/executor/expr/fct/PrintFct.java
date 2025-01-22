package net.thevpc.nuts.lib.doc.executor.expr.fct;

import net.thevpc.nuts.expr.NExprDeclarations;
import net.thevpc.nuts.expr.NExprNodeValue;
import net.thevpc.nuts.lib.doc.context.NDocContext;
import net.thevpc.nuts.lib.doc.executor.expr.BaseNexprNExprFct;
import net.thevpc.nuts.lib.doc.util.StringUtils;
import net.thevpc.nuts.util.NLiteral;

import java.util.ArrayList;
import java.util.List;

public class PrintFct extends BaseNexprNExprFct {
    public PrintFct() {
        super("print");
    }

    @Override
    public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context) {
        NDocContext fcontext = fcontext(context);

        List<String> all = new ArrayList<>();
        for (NExprNodeValue arg : args) {
            all.add(String.valueOf(arg.getValue().orNull()));
        }
        StringBuilder sb = new StringBuilder();
        if (!all.isEmpty()) {
            if (all.size() == 1) {
                sb.append(all.get(0));
            } else {
                sb.append(String.join(", ", all));
            }
        }
        fcontext.getLog().debug("eval", name + "(" + StringUtils.toLiteralString(sb.toString()) + ")");
        return sb.toString();
    }
}
