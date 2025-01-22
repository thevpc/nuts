package net.thevpc.nuts.lib.doc.executor.expr.fct;

import net.thevpc.nuts.expr.NExprDeclarations;
import net.thevpc.nuts.expr.NExprNodeValue;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.lib.doc.context.NDocContext;
import net.thevpc.nuts.lib.doc.executor.expr.BaseNexprNExprFct;
import net.thevpc.nuts.lib.doc.util.FileProcessorUtils;
import net.thevpc.nuts.lib.doc.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;

public class IncludeFct extends BaseNexprNExprFct {
    public IncludeFct() {
        super("include");
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
        try (InputStream in = opath.getInputStream()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            fcontext.getProcessorManager().processStream(in, out, fcontext.getMimeTypeResolver().resolveMimetype(opath.toString()));
            return out.toString();
        } catch (IOException io) {
            throw new UncheckedIOException(io);
        }
    }
}
