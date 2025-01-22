package net.thevpc.nuts.lib.doc.executor.expr.fct;

import net.thevpc.nuts.expr.NExprDeclarations;
import net.thevpc.nuts.expr.NExprNodeValue;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.lib.doc.context.NDocContext;
import net.thevpc.nuts.lib.doc.executor.expr.BaseNexprNExprFct;
import net.thevpc.nuts.util.NMemorySize;
import net.thevpc.nuts.util.NMemorySizeFormat;

import java.util.List;

public class FileContentLengthString extends BaseNexprNExprFct {
    public FileContentLengthString() {
        super("fileContentLengthString");
    }

    @Override
    public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context) {
        if (args.size() != 1) {
            throw new IllegalStateException(name + " : invalid arguments count");
        }
        NDocContext fcontext = fcontext(context);
        String str = (String) args.get(0).getValue().orNull();
        long contentLength = NPath.of(str).getContentLength();
        if(contentLength<0){
            return "NOT_FOUND";
        }
        NMemorySize m = NMemorySize.ofBits(contentLength)
                .reduceToLargestUnit();
        //m.getLargestUnit()
        return m.toString();
    }
}
