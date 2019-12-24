/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.main.repocommands;

import java.util.logging.Level;

import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsLogger;
import net.vpc.app.nuts.NutsPushRepositoryCommand;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.runtime.log.NutsLogVerb;
import net.vpc.app.nuts.runtime.repocommands.AbstractNutsPushRepositoryCommand;
import net.vpc.app.nuts.core.repos.NutsRepositoryExt;
import net.vpc.app.nuts.runtime.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsPushRepositoryCommand extends AbstractNutsPushRepositoryCommand {
    private final NutsLogger LOG;

    public DefaultNutsPushRepositoryCommand(NutsRepository repo) {
        super(repo);
        LOG=repo.workspace().log().of(DefaultNutsPushRepositoryCommand.class);
    }

    @Override
    public NutsPushRepositoryCommand run() {
        NutsWorkspaceUtils.of(getRepo().getWorkspace()).checkSession(getSession());
        getRepo().security().checkAllowed(NutsConstants.Permissions.PUSH, "push");
        try {
            NutsRepositoryExt.of(getRepo()).pushImpl(this);
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, NutsLogVerb.SUCCESS, "{0} Push {1}", CoreStringUtils.alignLeft(getRepo().config().getName(), 20), getId());
            }
        } catch (RuntimeException ex) {

            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, NutsLogVerb.FAIL, "{0} Push {1}", CoreStringUtils.alignLeft(getRepo().config().getName(), 20), getId());
            }
        }
        return this;
    }
}
