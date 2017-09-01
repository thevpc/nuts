package net.vpc.app.nuts.extensions.cmd;

import net.vpc.app.nuts.NutsCommand;
import net.vpc.app.nuts.NutsCommandContext;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.util.StringUtils;
import net.vpc.apps.javashell.JavaShell;
import net.vpc.apps.javashell.cmds.JavaShellInternalCmd;
import net.vpc.apps.javashell.parser.Env;
import net.vpc.apps.javashell.parser.JavaShellEvalContext;
import net.vpc.apps.javashell.parser.nodes.Node;

import java.io.File;

public class NutsJavaShell extends JavaShell {
    private DefaultNutsCommandLineConsoleComponent component;
    private NutsWorkspace workspace;

    public NutsJavaShell(DefaultNutsCommandLineConsoleComponent component, NutsWorkspace workspace) {
        this.component = component;
        this.workspace = workspace;
        super.setCwd(workspace.getCwd().getPath());
    }

    @Override
    public JavaShellInternalCmd getInternalCommand(String n) {
        NutsCommand ncommand = null;
        try {
            ncommand = component.getCommand(n);
        } catch (Exception ex) {
            return null;
        }
        return new NutsShellInternalCmd(ncommand, component);
    }

    @Override
    public int execExternalCommand(String[] command, JavaShellEvalContext context) {
        try {
            JavaShellInternalCmd exec = getInternalCommand("exec");
            return exec.exec(command, context);
        } catch (Exception ex) {
            return onResult(1, ex);
        }
    }

    @Override
    public JavaShellEvalContext createContext(Node root, Node parent, Env env, String[] args) {
        return createContext(component.getContext(), root, parent, env, args);
    }

    @Override
    public JavaShellEvalContext createContext(JavaShellEvalContext parentContext) {
        return new NutsJavaShellEvalContext((NutsJavaShellEvalContext)parentContext);
    }

    public JavaShellEvalContext createContext(NutsCommandContext commandContext, Node root, Node parent, Env env, String[] args) {
        return new NutsJavaShellEvalContext(this, args, root, parent, commandContext, workspace, env);
    }

    @Override
    public String errorToMessage(Throwable th) {
        return StringUtils.exceptionToString(th);
    }

    @Override
    public void onErrorImpl(String message, Throwable th) {
        component.getContext().getTerminal().getErr().println(message);
    }

    @Override
    public String which(String path0, JavaShellEvalContext context) {
        if (!path0.startsWith("/")) {
            return getCwd() + "/" + path0;
        }
        return path0;
    }

    @Override
    public void setCwd(String cwd) {
        super.setCwd(cwd);
        workspace.setCwd(new File(cwd));
    }
}
