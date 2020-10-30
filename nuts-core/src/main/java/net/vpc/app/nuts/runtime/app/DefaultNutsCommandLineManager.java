package net.vpc.app.nuts.runtime.app;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;

import java.util.List;

public class DefaultNutsCommandLineManager implements NutsCommandLineManager {
    private NutsWorkspace ws;

    public DefaultNutsCommandLineManager(NutsWorkspace ws) {
        this.ws=ws;
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
        return new DefaultNutsCommandLineFormat(getWorkspace());
    }

    @Override
    public NutsCommandLine parse(String line) {
        return new DefaultNutsCommandLine(getWorkspace(), NutsCommandLineUtils.parseCommandLine(getWorkspace(), line));
    }

    @Override
    public NutsCommandLine create(String... args) {
        return new DefaultNutsCommandLine(getWorkspace(), args);
    }

    @Override
    public NutsCommandLine create(List<String> args) {

        return new DefaultNutsCommandLine(getWorkspace(), args, null);
    }

    @Override
    public NutsArgumentCandidateBuilder createCandidate() {
        return new DefaultNutsArgumentCandidateBuilder();
    }

    @Override
    public NutsArgument createArgument(String argument) {
        return Factory.createArgument0(getWorkspace(), argument, '=');
    }


    @Override
    public NutsArgumentName createName(String type, String label) {
        return Factory.createName0(ws.createSession(), type, label);
    }

    @Override
    public NutsArgumentName createName(String type) {
        return createName(type, type);
    }

    public static class Factory {
        public static NutsArgument createArgument0(NutsWorkspace ws, String argument, char eq) {
            return new DefaultNutsArgument(argument, eq);
        }

        public static NutsArgumentCandidate createCandidate0(NutsWorkspace ws, String value, String label) {
            return new NutsDefaultArgumentCandidate(value, CoreStringUtils.isBlank(label) ? value : label);
        }

        public static NutsArgumentName createName0(NutsSession ws, String type, String label) {
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
                    return new ExtensionNonOption(type, null);
                }
                case "file": {
                    return new FileNonOption(type);
                }
                case "boolean": {
                    return new ValueNonOption( type, "true", "false");
                }
                case "repository": {
                    return new RepositoryNonOption( label);
                }
                case "repository-type": {
                    return new RepositoryTypeNonOption(label);
                }
                case "right": {
                    return new PermissionNonOption(label,  null, false);
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
}
