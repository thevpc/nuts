package net.thevpc.nuts.runtime.standalone.workspace.cmd.fetch;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.DefaultNutsQueryBaseOptions;

public abstract class AbstractNutsFetchCommand extends DefaultNutsQueryBaseOptions<NutsFetchCommand> implements NutsFetchCommand {

    private NutsId id;
//    protected Boolean installedOrNot;

    public AbstractNutsFetchCommand(NutsWorkspace ws) {
        super(ws, "fetch");
        setFailFast(true);
    }

    @Override
    public NutsFetchCommand setId(String id) {
        checkSession();
        this.id = NutsId.of(id).get(session);
        return this;
    }

    @Override
    public NutsFetchCommand setNutsApi() {
        return setId(ws.getApiId());
    }

    @Override
    public NutsFetchCommand setNutsRuntime() {
        return setId(ws.getRuntimeId());
    }

    @Override
    public NutsFetchCommand setId(NutsId id) {
        if (id == null) {
            checkSession();
            throw new NutsParseException(session, NutsMessage.ofNtf("invalid Id format : null"));
        }
        this.id = id;
        return this;
    }

    @Override
    public NutsId getId() {
        return id;
    }

    @Override
    public NutsFetchCommand setAll(NutsFetchCommand other) {
        super.copyFromDefaultNutsQueryBaseOptions((DefaultNutsQueryBaseOptions) other);
        if (other != null) {
            NutsFetchCommand o = other;
            this.id = o.getId();
//            this.installedOrNot = o.getInstalled();
        }
        return this;
    }
//
//    @Override
//    public NutsFetchCommand addRepositories(Collection<String> value) {
//        if (value != null) {
//            addRepositories(value.toArray(new String[0]));
//        }
//        return this;
//    }

//    @Override
//    public NutsFetchCommand installed() {
//        return setInstalled(true);
//    }
//
//    @Override
//    public Boolean getInstalled() {
//        return installedOrNot;
//    }
//
//    public NutsFetchCommand setInstalled(Boolean enable) {
//        installedOrNot = enable;
//        return this;
//    }
//
//    public NutsFetchCommand installed(Boolean enable) {
//        return setInstalled(enable);
//    }
//
//    @Override
//    public NutsFetchCommand notInstalled() {
//        return setInstalled(false);
//    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek().orNull();
        if (a == null) {
            return false;
        }
        boolean enabled = a.isActive();
        switch(a.key()) {
            case "--not-installed": {
                cmdLine.skip();
                if (enabled) {
                    setRepositoryFilter(
                            NutsRepositoryFilters.of(getSession()).installedRepo().neg()
                                    .and(this.getRepositoryFilter())
                    );
                }
                return true;
            }
            case "-i":
            case "--installed": {
                cmdLine.skip();
                if (enabled) {
                    setRepositoryFilter(
                            NutsRepositoryFilters.of(session).installedRepo()
                                    .and(this.getRepositoryFilter())
                    );
                }
                return true;
            }
            default: {
                if (super.configureFirst(cmdLine)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{"
                + "failFast=" + isFailFast()
                + ", optional=" + getOptional()
                + ", scope=" + getScope()
                + ", content=" + isContent()
                + ", inlineDependencies=" + isInlineDependencies()
                + ", dependencies=" + isDependencies()
                + ", effective=" + isEffective()
                + ", location=" + getLocation()
                + ", repos=" + getRepositoryFilter()
                + ", displayOptions=" + getDisplayOptions()
                + ", id=" + getId()
                + ", session=" + getSession()
                + '}';
    }
}
