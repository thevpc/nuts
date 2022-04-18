/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repository.cmd.push;

import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.boot.PrivateNutsUtilCollections;
import net.thevpc.nuts.runtime.standalone.repository.cmd.NutsRepositoryCommandBase;
import net.thevpc.nuts.spi.NutsPushRepositoryCommand;
import net.thevpc.nuts.NutsRepository;

import java.util.Arrays;
import java.util.List;

/**
 * @author thevpc
 * %category SPI Base
 */
public abstract class AbstractNutsPushRepositoryCommand extends NutsRepositoryCommandBase<NutsPushRepositoryCommand> implements NutsPushRepositoryCommand {

    protected NutsId id;
    protected boolean offline;
    protected List<String> args;
    protected String repository;

    public AbstractNutsPushRepositoryCommand(NutsRepository repo) {
        super(repo, "push");
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmd) {
        if (super.configureFirst(cmd)) {
            return true;
        }
        return false;
    }

    @Override
    public String getRepository() {
        return repository;
    }

    @Override
    public NutsPushRepositoryCommand setRepository(String repository) {
        this.repository = repository;
        return this;
    }

    @Override
    public boolean isOffline() {
        return offline;
    }

    @Override
    public NutsPushRepositoryCommand setOffline(boolean offline) {
        this.offline = offline;
        return this;
    }

    @Override
    public NutsPushRepositoryCommand setId(NutsId id) {
        this.id = id;
        return this;
    }

//    @Override
//    public NutsPushRepositoryCommand id(NutsId id) {
//        return setId(id);
//    }

    @Override
    public NutsId getId() {
        return id;
    }

    @Override
    public List<String> getArgs() {
        return PrivateNutsUtilCollections.unmodifiableList(args);
    }

//    @Override
//    public NutsPushRepositoryCommand clearArgs() {
//        this.args = null;
//        return this;
//    }


    @Override
    public NutsPushRepositoryCommand setArgs(List<String> args) {
        this.args.clear();
        this.args.addAll(PrivateNutsUtilCollections.nonNullList(args));
        return this;
    }

    @Override
    public NutsPushRepositoryCommand setArgs(String[] args) {
        this.args.clear();
        if (args != null) {
            this.args.addAll(Arrays.asList(args));
        }
        return this;
    }

//    @Override
//    public NutsPushRepositoryCommand addArg(String arg) {
//        if (this.args == null) {
//            this.args = new ArrayList<>();
//        }
//        if (arg == null) {
//            throw new NullPointerException();
//        }
//        this.args.add(arg);
//        return this;
//    }

//    @Override
//    public NutsPushRepositoryCommand addArgs(String... args) {
//        return addArgs(args == null ? null : Arrays.asList(args));
//    }

//    @Override
//    public NutsPushRepositoryCommand addArgs(Collection<String> args) {
//        if (this.args == null) {
//            this.args = new ArrayList<>();
//        }
//        if (args != null) {
//            for (String arg : args) {
//                if (arg == null) {
//                    throw new NullPointerException();
//                }
//                this.args.add(arg);
//            }
//        }
//        return this;
//    }
}
