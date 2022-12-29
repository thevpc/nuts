package net.thevpc.nuts.runtime.standalone.workspace.cmd.fetch;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArgument;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.DefaultNQueryBaseOptions;
import net.thevpc.nuts.spi.NSupportLevelContext;

public abstract class AbstractNFetchCommand extends DefaultNQueryBaseOptions<NFetchCommand> implements NFetchCommand {

    private NId id;
//    protected Boolean installedOrNot;

    public AbstractNFetchCommand(NSession ws) {
        super(ws, "fetch");
        setFailFast(true);
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public NFetchCommand setId(String id) {
        checkSession();
        this.id = NId.of(id).get(session);
        return this;
    }

    @Override
    public NFetchCommand setNutsApi() {
        return setId(ws.getApiId());
    }

    @Override
    public NFetchCommand setNutsRuntime() {
        return setId(ws.getRuntimeId());
    }

    @Override
    public NFetchCommand setId(NId id) {
        if (id == null) {
            checkSession();
            throw new NParseException(session, NMsg.ofNtf("invalid Id format : null"));
        }
        this.id = id;
        return this;
    }

    @Override
    public NId getId() {
        return id;
    }

    @Override
    public NFetchCommand setAll(NFetchCommand other) {
        super.copyFromDefaultNQueryBaseOptions((DefaultNQueryBaseOptions) other);
        if (other != null) {
            NFetchCommand o = other;
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
    public boolean configureFirst(NCommandLine cmdLine) {
        NArgument a = cmdLine.peek().orNull();
        if (a == null) {
            return false;
        }
        boolean enabled = a.isActive();
        switch (a.key()) {
            case "--not-installed": {
                cmdLine.skip();
                if (enabled) {
                    setRepositoryFilter(
                            NRepositoryFilters.of(getSession()).installedRepo().neg()
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
                            NRepositoryFilters.of(session).installedRepo()
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
