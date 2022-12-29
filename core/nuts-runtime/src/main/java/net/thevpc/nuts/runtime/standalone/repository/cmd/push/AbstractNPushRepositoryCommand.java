/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repository.cmd.push;

import net.thevpc.nuts.NId;
import net.thevpc.nuts.NRepository;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.runtime.standalone.repository.cmd.NRepositoryCommandBase;
import net.thevpc.nuts.runtime.standalone.util.collections.CoreCollectionUtils;
import net.thevpc.nuts.spi.NPushRepositoryCommand;

import java.util.Arrays;
import java.util.List;

/**
 * @author thevpc
 * %category SPI Base
 */
public abstract class AbstractNPushRepositoryCommand extends NRepositoryCommandBase<NPushRepositoryCommand> implements NPushRepositoryCommand {

    protected NId id;
    protected boolean offline;
    protected List<String> args;
    protected String repository;

    public AbstractNPushRepositoryCommand(NRepository repo) {
        super(repo, "push");
    }

    @Override
    public boolean configureFirst(NCommandLine cmd) {
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
    public NPushRepositoryCommand setRepository(String repository) {
        this.repository = repository;
        return this;
    }

    @Override
    public boolean isOffline() {
        return offline;
    }

    @Override
    public NPushRepositoryCommand setOffline(boolean offline) {
        this.offline = offline;
        return this;
    }

    @Override
    public NPushRepositoryCommand setId(NId id) {
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
        return CoreCollectionUtils.unmodifiableList(args);
    }

//    @Override
//    public NutsPushRepositoryCommand clearArgs() {
//        this.args = null;
//        return this;
//    }


    @Override
    public NPushRepositoryCommand setArgs(List<String> args) {
        this.args.clear();
        this.args.addAll(CoreCollectionUtils.nonNullList(args));
        return this;
    }

    @Override
    public NPushRepositoryCommand setArgs(String[] args) {
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
