/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repocommands;

import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsRepository;
import net.thevpc.nuts.spi.NutsRepositoryUndeployCommand;

/**
 *
 * @author thevpc
 */
public abstract class AbstractNutsRepositoryUndeployCommand extends NutsRepositoryCommandBase<NutsRepositoryUndeployCommand> implements NutsRepositoryUndeployCommand {

    private NutsId id;
    private String repository;
    private boolean offline = false;
    private boolean transitive = true;

    public AbstractNutsRepositoryUndeployCommand(NutsRepository repo) {
        super(repo, "undeploy");
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmd) {
        if (super.configureFirst(cmd)) {
            return true;
        }
        return false;
    }

    @Override
    public NutsId getId() {
        return id;
    }

    @Override
    public NutsRepositoryUndeployCommand setId(NutsId id) {
        this.id = id;
        return this;
    }

    @Override
    public String getRepository() {
        return repository;
    }

    @Override
    public NutsRepositoryUndeployCommand setRepository(String repository) {
        this.repository = repository;
        return this;
    }

//    @Override
//    public NutsRepositoryUndeployCommand transitive() {
//        return transitive(true);
//    }

    @Override
    public NutsRepositoryUndeployCommand setOffline(boolean offline) {
        this.offline = offline;
        return this;
    }

    @Override
    public boolean isTransitive() {
        return transitive;
    }

    @Override
    public NutsRepositoryUndeployCommand setTransitive(boolean transitive) {
        this.transitive = transitive;
        return this;
    }

    @Override
    public boolean isOffline() {
        return offline;
    }

//    @Override
//    public NutsRepositoryUndeployCommand id(NutsId id) {
//        return setId(id);
//    }
//
//    @Override
//    public NutsRepositoryUndeployCommand repository(String repository) {
//        return setRepository(repository);
//    }
//
//    @Override
//    public NutsRepositoryUndeployCommand offline() {
//        return offline(true);
//    }
//
//    @Override
//    public NutsRepositoryUndeployCommand offline(boolean offline) {
//        return setOffline(offline);
//    }
//
//    @Override
//    public NutsRepositoryUndeployCommand transitive(boolean transitive) {
//        return setTransitive(transitive);
//    }

//    @Override
//    public NutsRepositoryUndeploymentOptions copy() {
//        return new DefaultNutsRepositoryUndeploymentOptions()
//                .setId(id)
//                .setOffline(offline)
//                .setRepository(repository)
//                .setTransitive(transitive);
//
//    }


}
