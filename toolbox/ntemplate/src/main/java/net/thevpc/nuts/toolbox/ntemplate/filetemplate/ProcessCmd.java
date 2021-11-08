package net.thevpc.nuts.toolbox.ntemplate.filetemplate;

import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.NutsExecutionException;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.toolbox.nsh.SimpleJShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellExecutionContext;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.util.StringUtils;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ProcessCmd extends SimpleJShellBuiltin {

    private final FileTemplater fileTemplater;

    public ProcessCmd(FileTemplater fileTemplater) {
        super("process", 10, Options.class);
        this.fileTemplater = fileTemplater;
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, JShellExecutionContext context) {
        Options o = context.getOptions();
        if (commandLine.isNonOption(0)) {
            o.args.add(commandLine.next().getString());
            while (commandLine.hasNext()) {
                o.args.add(commandLine.next().getString());
            }
            return true;
        }
        return false;
    }

    @Override
    protected void execBuiltin(NutsCommandLine commandLine, JShellExecutionContext context) {
        Options o = context.getOptions();
        if (o.args.size() == 0) {
            throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("%s : invalid arguments count", getName()), 1);
        }
        for (String pathString : o.args) {
            fileTemplater.getLog().debug("eval", getName() + "(" + StringUtils.toLiteralString(pathString) + ")");
            fileTemplater.executeRegularFile(Paths.get(pathString), null);
        }
    }

    private static class Options {
        List<String> args = new ArrayList<>();
    }
}
