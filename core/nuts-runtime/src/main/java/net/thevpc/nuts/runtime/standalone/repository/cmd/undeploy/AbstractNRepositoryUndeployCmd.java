/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repository.cmd.undeploy;

import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.NId;
import net.thevpc.nuts.NRepository;
import net.thevpc.nuts.runtime.standalone.repository.cmd.NRepositoryCmdBase;
import net.thevpc.nuts.spi.NRepositoryUndeployCmd;

/**
 *
 * @author thevpc
 */
public abstract class AbstractNRepositoryUndeployCmd extends NRepositoryCmdBase<NRepositoryUndeployCmd> implements NRepositoryUndeployCmd {

    private NId id;
    private String repository;
    private boolean offline = false;
    private boolean transitive = true;

    public AbstractNRepositoryUndeployCmd(NRepository repo) {
        super(repo, "undeploy");
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        if (super.configureFirst(cmdLine)) {
            return true;
        }
        return false;
    }

    @Override
    public NId getId() {
        return id;
    }

    @Override
    public NRepositoryUndeployCmd setId(NId id) {
        this.id = id;
        return this;
    }

    @Override
    public String getRepository() {
        return repository;
    }

    @Override
    public NRepositoryUndeployCmd setRepository(String repository) {
        this.repository = repository;
        return this;
    }

//    @Override
//    public NutsRepositoryUndeployCommand transitive() {
//        return transitive(true);
//    }

    @Override
    public NRepositoryUndeployCmd setOffline(boolean offline) {
        this.offline = offline;
        return this;
    }

    @Override
    public boolean isTransitive() {
        return transitive;
    }

    @Override
    public NRepositoryUndeployCmd setTransitive(boolean transitive) {
        this.transitive = transitive;
        return this;
    }

    @Override
    public boolean isOffline() {
        return offline;
    }

}
