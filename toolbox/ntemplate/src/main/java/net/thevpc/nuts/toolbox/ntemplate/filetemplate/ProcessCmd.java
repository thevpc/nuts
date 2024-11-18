package net.thevpc.nuts.toolbox.ntemplate.filetemplate;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltinDefault;
import net.thevpc.nuts.toolbox.nsh.eval.NShellExecutionContext;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.util.StringUtils;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMsg;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ProcessCmd extends NShellBuiltinDefault {

    private final FileTemplater fileTemplater;

    public ProcessCmd(FileTemplater fileTemplater) {
        super("process", 10, Options.class);
        this.fileTemplater = fileTemplater;
    }

    @Override
    protected boolean nextOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        Options o = context.getOptions();
        NSession session = context.getSession();
        if (cmdLine.isNonOption(0)) {
            o.args.add(cmdLine.next().flatMap(NLiteral::asString).get());
            while (cmdLine.hasNext()) {
                o.args.add(cmdLine.next().flatMap(NLiteral::asString).get());
            }
            return true;
        }
        return false;
    }

    @Override
    protected void main(NCmdLine cmdLine, NShellExecutionContext context) {
        Options o = context.getOptions();
        if (o.args.size() == 0) {
            throw new NExecutionException(NMsg.ofC("%s : invalid arguments count", getName()), NExecutionException.ERROR_1);
        }
        for (String pathString : o.args) {
            fileTemplater.getLog().debug("eval", getName() + "(" + StringUtils.toLiteralString(pathString) + ")");
            fileTemplater.executeRegularFile(Paths.get(pathString), null);
        }
    }

    private static class Options {
        List<String> args = new ArrayList<>();
    }

    @Override
    protected boolean nextNonOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        return nextOption(arg, cmdLine, context);
    }
}
