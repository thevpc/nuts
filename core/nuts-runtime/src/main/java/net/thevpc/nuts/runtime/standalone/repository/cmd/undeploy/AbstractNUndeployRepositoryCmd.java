/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repository.cmd.undeploy;

import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.runtime.standalone.repository.cmd.NRepositoryCmdBase;
import net.thevpc.nuts.spi.NUndeployRepositoryCmd;

/**
 *
 * @author thevpc
 */
public abstract class AbstractNUndeployRepositoryCmd extends NRepositoryCmdBase<NUndeployRepositoryCmd> implements NUndeployRepositoryCmd {

    private NId id;
    private String repository;
    private boolean offline = false;
    private boolean transitive = true;

    public AbstractNUndeployRepositoryCmd(NRepository repo) {
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
    public NId id() {
        return id;
    }

    @Override
    public NUndeployRepositoryCmd id(NId id) {
        this.id = id;
        return this;
    }

    @Override
    public String repository() {
        return repository;
    }

    @Override
    public NUndeployRepositoryCmd repository(String repository) {
        this.repository = repository;
        return this;
    }

//    @Override
//    public NutsRepositoryUndeployCommand transitive() {
//        return transitive(true);
//    }

    @Override
    public NUndeployRepositoryCmd offline(boolean offline) {
        this.offline = offline;
        return this;
    }

    @Override
    public boolean isTransitive() {
        return transitive;
    }

    @Override
    public NUndeployRepositoryCmd transitive(boolean transitive) {
        this.transitive = transitive;
        return this;
    }

    @Override
    public boolean isOffline() {
        return offline;
    }

}
