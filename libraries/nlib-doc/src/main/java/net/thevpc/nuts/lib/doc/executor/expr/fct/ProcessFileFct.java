package net.thevpc.nuts.lib.doc.executor.expr.fct;

import net.thevpc.nuts.expr.NExprDeclarations;
import net.thevpc.nuts.expr.NExprNodeValue;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.lib.doc.context.NDocContext;
import net.thevpc.nuts.lib.doc.executor.expr.BaseNexprNExprFct;
import net.thevpc.nuts.lib.doc.util.FileProcessorUtils;
import net.thevpc.nuts.lib.doc.util.StringUtils;

import java.util.List;

public class ProcessFileFct extends BaseNexprNExprFct {
    public ProcessFileFct() {
        super("processFile");
    }

    @Override
    public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context) {
        if (args.size() != 1) {
            throw new IllegalStateException(name + " : invalid arguments count");
        }
        NDocContext fcontext = fcontext(context);

        String str = (String) args.get(0).getValue().orNull();
        String path = FileProcessorUtils.toAbsolute(str, fcontext.getWorkingDirRequired());
        NPath opath = NPath.of(path);
        fcontext.getLog().debug("eval", name + "(" + StringUtils.toLiteralString(opath) + ")");
        fcontext.getProcessorManager().processSourceRegularFile(opath, null);
        return "";
    }
}
