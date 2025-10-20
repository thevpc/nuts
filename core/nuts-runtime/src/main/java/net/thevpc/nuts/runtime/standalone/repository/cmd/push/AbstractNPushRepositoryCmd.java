/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repository.cmd.push;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.runtime.standalone.repository.cmd.NRepositoryCmdBase;
import net.thevpc.nuts.util.NCollections;
import net.thevpc.nuts.spi.NPushRepositoryCmd;

import java.util.Arrays;
import java.util.List;

/**
 * @author thevpc
 * %category SPI Base
 */
public abstract class AbstractNPushRepositoryCmd extends NRepositoryCmdBase<NPushRepositoryCmd> implements NPushRepositoryCmd {

    protected NId id;
    protected boolean offline;
    protected List<String> args;
    protected String repository;

    public AbstractNPushRepositoryCmd(NRepository repo) {
        super(repo, "push");
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        if (super.configureFirst(cmdLine)) {
            return true;
        }
        return false;
    }

    @Override
    public String getRepository() {
        return repository;
    }

    @Override
    public NPushRepositoryCmd setRepository(String repository) {
        this.repository = repository;
        return this;
    }

    @Override
    public boolean isOffline() {
        return offline;
    }

    @Override
    public NPushRepositoryCmd setOffline(boolean offline) {
        this.offline = offline;
        return this;
    }

    @Override
    public NPushRepositoryCmd setId(NId id) {
        this.id = id;
        return this;
    }

//    @Override
//    public NutsPushRepositoryCommand id(NutsId id) {
//        return setId(id);
//    }

    @Override
    public NId getId() {
        return id;
    }

    @Override
    public List<String> getArgs() {
        return NCollections.unmodifiableList(args);
    }

//    @Override
//    public NutsPushRepositoryCommand clearArgs() {
//        this.args = null;
//        return this;
//    }


    @Override
    public NPushRepositoryCmd setArgs(List<String> args) {
        this.args.clear();
        this.args.addAll(NCollections.nonNullList(args));
        return this;
    }

    @Override
    public NPushRepositoryCmd setArgs(String[] args) {
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
