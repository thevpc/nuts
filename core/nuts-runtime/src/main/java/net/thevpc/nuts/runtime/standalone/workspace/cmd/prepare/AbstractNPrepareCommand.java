/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.prepare;

import net.thevpc.nuts.NId;
import net.thevpc.nuts.NPrepareCommand;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NWorkspaceCommandBase;
import net.thevpc.nuts.spi.NSupportLevelContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public abstract class AbstractNPrepareCommand extends NWorkspaceCommandBase<NPrepareCommand> implements NPrepareCommand {
    protected String targetServer;
    protected String userName;
    protected String version;
    protected String targetHome;
    protected List<NId> ids = new ArrayList<>();

    public AbstractNPrepareCommand(NSession session) {
        super(session, "prepare");
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public NPrepareCommand setTargetServer(String targetServer) {
        this.targetServer = targetServer;
        return this;
    }

    @Override
    public NPrepareCommand setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    @Override
    public NPrepareCommand setVersion(String version) {
        this.version = version;
        return this;
    }

    @Override
    public NPrepareCommand setIds(List<NId> ids) {
        if (this.ids == null) {
            this.ids = new ArrayList<>();
        } else {
            this.ids.clear();
        }
        if (ids != null) {
            this.ids.addAll(ids.stream().filter(Objects::nonNull).collect(Collectors.toList()));
        }
        return this;
    }

    @Override
    public NPrepareCommand addIds(List<NId> ids) {
        if (this.ids == null) {
            this.ids = new ArrayList<>();
        }
        if (ids != null) {
            this.ids.addAll(ids.stream().filter(Objects::nonNull).collect(Collectors.toList()));
        }
        return this;
    }

    public String getTargetServer() {
        return targetServer;
    }

    public String getUserName() {
        return userName;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public boolean configureFirst(NCommandLine cmdLine) {
        NArg a = cmdLine.peek().get(session);
        if (a == null) {
            return false;
        }
        if (super.configureFirst(cmdLine)) {
            return true;
        } else if (cmdLine.withNextString((v, arg, s) -> {
            setUserName(v);
        }, "--user")) {
            return true;
        } else if (cmdLine.withNextString((v, arg, s) -> {
            setTargetServer(v);
        }, "--target-server")) {
            return true;
        } else if (cmdLine.withNextString((v, arg, s) -> {
            setVersion(v);
        }, "--version")) {
            return true;
        } else if (cmdLine.withNextString((v, arg, s) -> {
            this.targetHome = v;
        }, "--target-home")) {
            return true;
        }
        return false;
    }
}
