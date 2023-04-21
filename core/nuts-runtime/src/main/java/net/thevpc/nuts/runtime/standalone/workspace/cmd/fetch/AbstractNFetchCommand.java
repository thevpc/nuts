package net.thevpc.nuts.runtime.standalone.workspace.cmd.fetch;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.DefaultNQueryBaseOptions;
import net.thevpc.nuts.spi.NSupportLevelContext;

public abstract class AbstractNFetchCommand extends DefaultNQueryBaseOptions<NFetchCommand> implements NFetchCommand {

    private NId id;
//    protected Boolean installedOrNot;

    public AbstractNFetchCommand(NSession session) {
        super(session, "fetch");
        setFailFast(true);
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public NFetchCommand setId(String id) {
        checkSession();
        NId nid = NId.of(id).get(session);
        return setId(nid);
    }

    @Override
    public NFetchCommand setId(NId id) {
        if (id == null) {
            checkSession();
            throw new NParseException(session, NMsg.ofNtf("invalid Id format : null"));
        }
        if (
                id.getVersion().isBlank()
                        || !id.getVersion().isSingleValue()
        ) {
            checkSession();
            throw new NParseException(session, NMsg.ofC("invalid Id format : %s", id));
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

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg a = cmdLine.peek().orNull();
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
