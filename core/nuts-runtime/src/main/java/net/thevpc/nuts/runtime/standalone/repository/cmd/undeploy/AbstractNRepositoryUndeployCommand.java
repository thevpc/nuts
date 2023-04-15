/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repository.cmd.undeploy;

import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.NId;
import net.thevpc.nuts.NRepository;
import net.thevpc.nuts.runtime.standalone.repository.cmd.NRepositoryCommandBase;
import net.thevpc.nuts.spi.NRepositoryUndeployCommand;

/**
 *
 * @author thevpc
 */
public abstract class AbstractNRepositoryUndeployCommand extends NRepositoryCommandBase<NRepositoryUndeployCommand> implements NRepositoryUndeployCommand {

    private NId id;
    private String repository;
    private boolean offline = false;
    private boolean transitive = true;

    public AbstractNRepositoryUndeployCommand(NRepository repo) {
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
    public NRepositoryUndeployCommand setId(NId id) {
        this.id = id;
        return this;
    }

    @Override
    public String getRepository() {
        return repository;
    }

    @Override
    public NRepositoryUndeployCommand setRepository(String repository) {
        this.repository = repository;
        return this;
    }

//    @Override
//    public NutsRepositoryUndeployCommand transitive() {
//        return transitive(true);
//    }

    @Override
    public NRepositoryUndeployCommand setOffline(boolean offline) {
        this.offline = offline;
        return this;
    }

    @Override
    public boolean isTransitive() {
        return transitive;
    }

    @Override
    public NRepositoryUndeployCommand setTransitive(boolean transitive) {
        this.transitive = transitive;
        return this;
    }

    @Override
    public boolean isOffline() {
        return offline;
    }

}
