/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.prepare;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.command.NPrepare;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NWorkspaceCmdBase;
import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.util.NBlankable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
@NScore(fixed = NScorable.DEFAULT_SCORE)
public abstract class AbstractNPrepare extends NWorkspaceCmdBase<NPrepare> implements NPrepare {
    protected NConnectionString connectionString;
    protected NVersion version;
    protected String java;
    protected String workspace;
    protected List<NId> ids = new ArrayList<>();

    public AbstractNPrepare() {
        super("prepare");
    }

    @Override
    public NPrepare at(NConnectionString connectionString) {
        this.connectionString = connectionString;
        return this;
    }

    @Override
    public NPrepare connectionString(NConnectionString connectionString) {
        this.connectionString = connectionString;
        return this;
    }

    public NConnectionString connectionString() {
        return connectionString;
    }

    @Override
    public String workspace() {
        return workspace;
    }

    @Override
    public NPrepare workspace(String workspace) {
        this.workspace = workspace;
        return this;
    }

    @Override
    public NPrepare version(NVersion version) {
        this.version = version;
        return this;
    }

    @Override
    public NVersion version() {
        return version;
    }

    public String java() {
        return java;
    }

    @Override
    public NPrepare java(String targetHome) {
        this.java = targetHome;
        return this;
    }

    @Override
    public NPrepare clearIds(NId... ids) {
        if (this.ids != null) {
            this.ids.clear();
        }
        return this;
    }

    @Override
    public NPrepare ids(NId... ids) {
        if (ids != null) {
            for (NId id : ids) {
                if (id != null) {
                    if (this.ids == null) {
                        this.ids = new ArrayList<>();
                    }
                    this.ids.add(id);
                }
            }
        }
        return this;
    }

    @Override
    public NPrepare ids(List<NId> ids) {
        if (this.ids == null) {
            this.ids = new ArrayList<>();
        }
        if (ids != null) {
            this.ids.addAll(ids.stream().filter(Objects::nonNull).collect(Collectors.toList()));
        }
        return this;
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg a = cmdLine.peek().get();
        if (a == null) {
            return false;
        }
        return cmdLine.matcher()
                .with((c) -> super.configureFirst(cmdLine))
                .when("--at").asEntry((v) -> at(NBlankable.isBlank(v.stringValue()) ? null : NConnectionString.of(v.stringValue())))
                .when("--version").asEntry((v) -> version(NBlankable.isBlank(v.stringValue()) ? null : NVersion.of(v.stringValue())))
                .when("--workspace").asEntry((v) -> workspace(v.stringValue()))
                .when("--java").asEntry((v) -> java(v.stringValue()))
                .anyMatch();
    }
}
