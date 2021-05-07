package net.thevpc.nuts.runtime.core.app;

import net.thevpc.nuts.NutsDefaultArgumentCandidate;
import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;

import java.util.List;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

public class DefaultNutsCommandLineManager implements NutsCommandLineManager {

    private NutsWorkspace ws;
    private NutsSession session;

    public DefaultNutsCommandLineManager(NutsWorkspace ws) {
        this.ws = ws;
    }

    public NutsSession getSession() {
        return session;
    }

    public NutsCommandLineManager setSession(NutsSession session) {
        this.session = session;
        return this;
    }
    
    

    public NutsWorkspace getWorkspace() {
        return ws;
    }

    @Override
    public NutsCommandLineFormat formatter(NutsCommandLine commandLine) {
        return formatter().setValue(commandLine);
    }

    @Override
    public NutsCommandLineFormat formatter() {
        return new DefaultNutsCommandLineFormat(getWorkspace()).setSession(session);
    }

    @Override
    public NutsCommandLine parse(String line) {
        checkSession();
        return new DefaultNutsCommandLine(getSession(), NutsCommandLineUtils.parseCommandLine(getSession(), line));
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
    public NutsArgumentCandidateBuilder createCandidate() {
        checkSession();
        return new DefaultNutsArgumentCandidateBuilder();
    }

    @Override
    public NutsArgument createArgument(String argument) {
        checkSession();
        return Factory.createArgument0(getWorkspace(), argument, '=');
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

        public static NutsArgument createArgument0(NutsWorkspace ws, String argument, char eq) {
            return new DefaultNutsArgument(argument, eq);
        }

        public static NutsArgumentCandidate createCandidate0(NutsWorkspace ws, String value, String label) {
            return new NutsDefaultArgumentCandidate(value, CoreStringUtils.isBlank(label) ? value : label);
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
    public NutsCommandHistoryBuilder createHistory() {
        return new NutsCommandHistoryBuilderImpl(ws);
    }

}
