package net.thevpc.nuts.runtime.core.app;

import net.thevpc.nuts.*;

import java.util.List;

import net.thevpc.nuts.runtime.core.shell.NutsShellHelper;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

public class DefaultNutsCommandLines implements NutsCommandLines {

    private NutsWorkspace ws;
    private NutsSession session;
    private NutsShellFamily family = NutsShellFamily.getCurrent();

    public DefaultNutsCommandLines(NutsSession session) {
        this.session = session;
        this.ws = session.getWorkspace();
    }

    public NutsSession getSession() {
        return session;
    }

    public NutsCommandLines setSession(NutsSession session) {
       this.session = NutsWorkspaceUtils.bindSession(ws, session);
        return this;
    }

    public NutsShellFamily getShellFamily() {
        return family;
    }

    public NutsCommandLines setShellFamily(NutsShellFamily family) {
        this.family = family == null ? NutsShellFamily.getCurrent() : family;
        return this;
    }

    public NutsWorkspace getWorkspace() {
        return ws;
    }

    @Override
    public NutsCommandLine parse(String line) {
        checkSession();
        return new DefaultNutsCommandLine(getSession(), parseCommandLineArr(line));
    }

    private String[] parseCommandLineArr(String line) {
        NutsShellFamily f = getShellFamily();
        if (f == null) {
            f = NutsShellFamily.getCurrent();
        }
        return NutsShellHelper.of(f).parseCommandLineArr(line,session);
    }

    protected void checkSession() {
        NutsWorkspaceUtils.checkSession(ws, session);
    }

    @Override
    public NutsCommandLine create(String... args) {
        checkSession();
        return new DefaultNutsCommandLine(getSession(), args);
    }

    @Override
    public NutsCommandLine create(List<String> args) {
        checkSession();
        return new DefaultNutsCommandLine(getSession(), args, null);
    }

    @Override
    public NutsArgument createArgument(String argument) {
        checkSession();
        return Factory.createArgument0(getSession(), argument, '=');
    }

    @Override
    public NutsArgumentName createName(String type, String label) {
        checkSession();
        return Factory.createName0(getSession(), type, label);
    }

    @Override
    public NutsArgumentName createName(String type) {
        checkSession();
        return createName(type, type);
    }

    public static class Factory {

        public static NutsArgument createArgument0(NutsSession ws, String argument, char eq) {
            return new DefaultNutsArgument(argument, eq);
        }

        public static NutsArgumentName createName0(NutsSession session, String type, String label) {
            if (type == null) {
                type = "";
            }
            if (label == null) {
                label = type;
            }
            switch (type) {
                case "arch": {
                    return new ArchitectureNonOption(label);
                }
                case "packaging": {
                    return new PackagingNonOption(label);
                }
                case "extension": {
                    return new ExtensionNonOption(type, session);
                }
                case "file": {
                    return new FileNonOption(type);
                }
                case "boolean": {
                    return new ValueNonOption(type, "true", "false");
                }
                case "repository": {
                    return new RepositoryNonOption(label);
                }
                case "repository-type": {
                    return new RepositoryTypeNonOption(label);
                }
                case "right": {
                    return new PermissionNonOption(label, null, false);
                }
                case "user": {
                    return new UserNonOption(label);
                }
                case "group": {
                    return new GroupNonOption(label);
                }
                default: {
                    return new DefaultNonOption(label);
                }
            }
        }
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<Object> context) {
        return DEFAULT_SUPPORT;
    }
}
