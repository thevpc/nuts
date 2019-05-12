/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsPushRepositoryCommand;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.core.spi.NutsRepositoryExt;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsPushRepositoryCommand extends NutsRepositoryCommandBase<NutsPushRepositoryCommand> implements NutsPushRepositoryCommand {

    private static final Logger LOG = Logger.getLogger(DefaultNutsPushRepositoryCommand.class.getName());
    private NutsId id;
    private boolean offline;
    private List<String> args;
    private String repository;

    public DefaultNutsPushRepositoryCommand(NutsRepository repo) {
        super(repo);
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
    public NutsPushRepositoryCommand repository(String repository) {
        return setRepository(repository);
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
    public NutsPushRepositoryCommand offline() {
        return offline(true);
    }

    @Override
    public NutsPushRepositoryCommand offline(boolean enable) {
        return setOffline(enable);
    }

    @Override
    public NutsPushRepositoryCommand setId(NutsId id) {
        this.id = id;
        return this;
    }

    @Override
    public NutsPushRepositoryCommand id(NutsId id) {
        return setId(id);
    }

    @Override
    public NutsId getId() {
        return id;
    }

    @Override
    public String[] getArgs() {
        return args == null ? new String[0] : args.toArray(new String[0]);
    }

    @Override
    public NutsPushRepositoryCommand clearArgs() {
        this.args = null;
        return this;
    }

    @Override
    public NutsPushRepositoryCommand addArg(String arg) {
        if (this.args == null) {
            this.args = new ArrayList<>();
        }
        if (arg == null) {
            throw new NullPointerException();
        }
        this.args.add(arg);
        return this;
    }

    @Override
    public NutsPushRepositoryCommand addArgs(String... args) {
        return addArgs(args == null ? null : Arrays.asList(args));
    }

    @Override
    public NutsPushRepositoryCommand addArgs(Collection<String> args) {
        if (this.args == null) {
            this.args = new ArrayList<>();
        }
        if (args != null) {
            for (String arg : args) {
                if (arg == null) {
                    throw new NullPointerException();
                }
                this.args.add(arg);
            }
        }
        return this;
    }

    @Override
    public NutsPushRepositoryCommand run() {
        CoreNutsUtils.checkSession(getSession());
        getRepo().security().checkAllowed(NutsConstants.Rights.PUSH, "push");
        try {
            NutsRepositoryExt.of(getRepo()).pushImpl(this);
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "[SUCCESS] {0} Push {1}", new Object[]{CoreStringUtils.alignLeft(getRepo().config().getName(), 20), id});
            }
        } catch (RuntimeException ex) {

            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "[ERROR  ] {0} Push {1}", new Object[]{CoreStringUtils.alignLeft(getRepo().config().getName(), 20), id});
            }
        }
        return this;
    }
}
