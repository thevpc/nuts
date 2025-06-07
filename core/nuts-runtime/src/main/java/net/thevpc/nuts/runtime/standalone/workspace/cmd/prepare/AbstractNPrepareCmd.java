/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.prepare;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NWorkspaceCmdBase;
import net.thevpc.nuts.spi.NSupportLevelContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public abstract class AbstractNPrepareCmd extends NWorkspaceCmdBase<NPrepareCmd> implements NPrepareCmd {
    protected String targetServer;
    protected String userName;
    protected String version;
    protected String targetHome;
    protected List<NId> ids = new ArrayList<>();

    public AbstractNPrepareCmd(NWorkspace workspace) {
        super("prepare");
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public NPrepareCmd setTargetServer(String targetServer) {
        this.targetServer = targetServer;
        return this;
    }

    @Override
    public NPrepareCmd setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    @Override
    public NPrepareCmd setVersion(String version) {
        this.version = version;
        return this;
    }

    @Override
    public NPrepareCmd setIds(List<NId> ids) {
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
    public NPrepareCmd addIds(List<NId> ids) {
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
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg a = cmdLine.peek().get();
        if (a == null) {
            return false;
        }
        if (super.configureFirst(cmdLine)) {
            return true;
        } else if (cmdLine.withNextEntry((v) -> {
            setUserName(v.stringValue());
        }, "--user")) {
            return true;
        } else if (cmdLine.withNextEntry((v) -> {
            setTargetServer(v.stringValue());
        }, "--target-server")) {
            return true;
        } else if (cmdLine.withNextEntry((v) -> {
            setVersion(v.stringValue());
        }, "--version")) {
            return true;
        } else if (cmdLine.withNextEntry((v) -> {
            this.targetHome = v.stringValue();
        }, "--target-home")) {
            return true;
        }
        return false;
    }
}
