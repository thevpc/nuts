package net.thevpc.nuts.runtime.standalone.workspace.cmd.undeploy;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.command.NUndeploy;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.text.NContentType;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NWorkspaceCmdBase;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.text.NMsg;

import java.util.ArrayList;
import java.util.List;

@NScore(fixed = NScorable.DEFAULT_SCORE)
public abstract class AbstractNUndeploy extends NWorkspaceCmdBase<NUndeploy> implements NUndeploy {

    protected List<NId> result;
    protected final List<NId> ids = new ArrayList<>();
    protected String repository;
    protected boolean offline = true;

    public AbstractNUndeploy() {
        super("undeploy");
    }

    @Override
    public List<NId> ids() {
        return ids;
    }

    @Override
    public NUndeploy ids(List<NId> value) {
        ids.clear();
        addIds(value);
        return this;
    }

    @Override
    public NUndeploy addId(NId id) {
        if (id != null) {
            ids.add(id);
        }
        invalidateResult();
        return this;
    }

    @Override
    public NUndeploy addId(String id) {
        return addId(NBlankable.isBlank(id) ? null : NId.get(id).get());
    }

    @Override
    public NUndeploy addIds(String... values) {
        if (values != null) {
            for (String s : values) {
                if (!NBlankable.isBlank(s)) {
                    ids.add(NId.get(s).get());
                }
            }
        }
        return this;
    }

    @Override
    public NUndeploy addIds(NId... value) {
        if (value != null) {
            for (NId s : value) {
                if (s != null) {
                    ids.add(s);
                }
            }
        }
        return this;
    }

    @Override
    public NUndeploy addIds(List<NId> value) {
        if (value != null) {
            for (NId s : value) {
                if (s != null) {
                    ids.add(s);
                }
            }
        }
        return this;
    }

    @Override
    public NUndeploy clearIds() {
        ids.clear();
        return this;
    }

    @Override
    public String repository() {
        return repository;
    }

    @Override
    public NUndeploy repository(String repository) {
        this.repository = repository;
        invalidateResult();
        return this;
    }

    protected void addResult(NId id) {
        if (result == null) {
            result = new ArrayList<>();
        }
        result.add(id);
        NSession session = NSession.of();
        if (session.isTrace()) {
            if (session.outputFormat().orNull() == null || session.outputFormat().orDefault() == NContentType.PLAIN) {
                if (session.outputFormat().orNull() == null || session.outputFormat().orDefault() == NContentType.PLAIN) {
                    session.terminal().out().println(NMsg.ofC("Nuts %s undeployed successfully", id));
                }
            }
        }
    }

    @Override
    public boolean isOffline() {
        return offline;
    }

    @Override
    public NUndeploy offline(boolean offline) {
        this.offline = offline;
        invalidateResult();
        return this;
    }

    @Override
    protected void invalidateResult() {
        result = null;
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg aa = cmdLine.peek().orNull();
        if (aa == null) {
            return false;
        }
        switch (aa.key()) {
            case "--offline": {
                return cmdLine.matcher().matchFlag((v) -> offline(v.booleanValue())).anyMatch();
            }
            case "-r":
            case "-repository":
            case "--from": {
                return cmdLine.matcher().matchEntry((v) -> repository(v.stringValue())).anyMatch();
            }

            default: {
                if (super.configureFirst(cmdLine)) {
                    return true;
                }
                if (aa.isOption()) {
                    return false;
                } else {
                    cmdLine.skip();
                    addId(aa.asString().get());
                    return true;
                }
            }
        }
    }

}
