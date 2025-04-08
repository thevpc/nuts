package net.thevpc.nuts.runtime.standalone.workspace.cmd.fetch;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.DefaultNQueryBaseOptions;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NMsg;

public abstract class AbstractNFetchCmd extends DefaultNQueryBaseOptions<NFetchCmd> implements NFetchCmd {

    private NId id;
    private boolean filterCurrentEnvironment=true;
//    protected Boolean installedOrNot;

    public AbstractNFetchCmd() {
        super("fetch");
        failFast();
    }

    @Override
    public boolean isFilterCurrentEnvironment() {
        return filterCurrentEnvironment;
    }

    @Override
    public NFetchCmd setFilterCurrentEnvironment(boolean filterCurrentEnvironment) {
        this.filterCurrentEnvironment = filterCurrentEnvironment;
        return this;
    }
    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public NFetchCmd setId(String id) {
        NId nid = NId.get(id).get();
        return setId(nid);
    }

    @Override
    public NFetchCmd setId(NId id) {
        if (id == null) {
            throw new NParseException(NMsg.ofNtf("invalid Id format to fetch : null"));
        }
        if (
                id.getVersion().isBlank()
                        || !id.getVersion().isSingleValue()
        ) {
            throw new NParseException(NMsg.ofC("invalid Id format to fetch : %s", id));
        }
        this.id = id;
        return this;
    }

    @Override
    public NId getId() {
        return id;
    }

    @Override
    public NFetchCmd copyFrom(NFetchCmd other) {
        super.copyFromDefaultNQueryBaseOptions((DefaultNQueryBaseOptions) other);
        if (other != null) {
            NFetchCmd o = other;
            this.id = o.getId();
            this.filterCurrentEnvironment = o.isFilterCurrentEnvironment();
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
                            NRepositoryFilters.of().installedRepo().neg()
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
                            NRepositoryFilters.of().installedRepo()
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
                + ", repos=" + getRepositoryFilter()
                + ", displayOptions=" + getDisplayOptions()
                + ", id=" + getId()
                + '}';
    }
}
