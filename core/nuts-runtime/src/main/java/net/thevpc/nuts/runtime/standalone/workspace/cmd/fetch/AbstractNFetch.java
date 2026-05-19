package net.thevpc.nuts.runtime.standalone.workspace.cmd.fetch;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.command.NFetch;
import net.thevpc.nuts.core.NRepositoryFilters;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.DefaultNQueryBaseOptions;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.expr.NParseException;

@NScore(fixed = NScorable.DEFAULT_SCORE)
public abstract class AbstractNFetch extends DefaultNQueryBaseOptions<NFetch> implements NFetch {

    private NId id;
    private boolean ignoreCurrentEnvironment;
//    protected Boolean installedOrNot;

    public AbstractNFetch() {
        super("fetch");
        failFast(true);
    }

    @Override
    public boolean isIgnoreCurrentEnvironment() {
        return ignoreCurrentEnvironment;
    }

    @Override
    public NFetch ignoreCurrentEnvironment(boolean ignoreCurrentEnvironment) {
        this.ignoreCurrentEnvironment = ignoreCurrentEnvironment;
        return this;
    }

    @Override
    public NFetch id(String id) {
        NId nid = NId.get(id).get();
        return id(nid);
    }

    @Override
    public NFetch id(NId id) {
        if (id == null) {
            throw new NParseException(NMsg.ofNtf("invalid Id format to fetch : null"));
        }
        if (
                id.version().isBlank()
                        || !id.version().isSingleValue()
        ) {
            throw new NParseException(NMsg.ofC("invalid Id format to fetch : %s", id));
        }
        this.id = id;
        return this;
    }

    @Override
    public NId id() {
        return id;
    }

    @Override
    public NFetch copyFrom(NFetch other) {
        super.copyFromDefaultNQueryBaseOptions((DefaultNQueryBaseOptions) other);
        if (other != null) {
            NFetch o = other;
            this.id = o.id();
            this.ignoreCurrentEnvironment = o.isIgnoreCurrentEnvironment();
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
        boolean enabled = a.isUncommented();
        switch (a.key()) {
            case "--not-installed": {
                cmdLine.skip();
                if (enabled) {
                    repositoryFilter(
                            NRepositoryFilters.of().installedRepo().neg()
                                    .and(this.repositoryFilter())
                    );
                }
                return true;
            }
            case "-i":
            case "--installed": {
                cmdLine.skip();
                if (enabled) {
                    repositoryFilter(
                            NRepositoryFilters.of().installedRepo()
                                    .and(this.repositoryFilter())
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
//                + ", content=" + isContent()
                + ", inlineDependencies=" + isInlineDependencies()
//                + ", dependencies=" + isDependencies()
//                + ", effective=" + isEffective()
                + ", repos=" + repositoryFilter()
                + ", displayOptions=" + getDisplayOptions()
                + ", id=" + id()
                + '}';
    }
}
