/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.main.repocommands;

import java.util.logging.Level;

import net.thevpc.nuts.NutsConstants;
import net.thevpc.nuts.NutsLogger;
import net.thevpc.nuts.NutsPushRepositoryCommand;
import net.thevpc.nuts.NutsRepository;
import net.thevpc.nuts.runtime.core.repos.NutsRepositoryExt;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.log.NutsLogVerb;
import net.thevpc.nuts.runtime.repocommands.AbstractNutsPushRepositoryCommand;
import net.thevpc.nuts.runtime.util.NutsWorkspaceUtils;

/**
 *
 * @author vpc
 * %category SPI Base
 */
public class DefaultNutsPushRepositoryCommand extends AbstractNutsPushRepositoryCommand {
    private final NutsLogger LOG;

    public DefaultNutsPushRepositoryCommand(NutsRepository repo) {
        super(repo);
        LOG=repo.getWorkspace().log().of(DefaultNutsPushRepositoryCommand.class);
    }

    @Override
    public NutsPushRepositoryCommand run() {
        NutsWorkspaceUtils.of(getRepo().getWorkspace()).checkSession(getSession());
        getRepo().security().checkAllowed(NutsConstants.Permissions.PUSH, "push");
        try {
            NutsRepositoryExt.of(getRepo()).pushImpl(this);
            if (LOG.isLoggable(Level.FINEST)) {

                LOG.with().level(Level.FINEST).verb(NutsLogVerb.SUCCESS).log( "{0} Push {1}", CoreStringUtils.alignLeft(getRepo().getName(), 20), getId());
            }
        } catch (RuntimeException ex) {

            if (LOG.isLoggable(Level.FINEST)) {
                LOG.with().level(Level.FINEST).verb(NutsLogVerb.FAIL).log( "{0} Push {1}", CoreStringUtils.alignLeft(getRepo().getName(), 20), getId());
            }
        }
        return this;
    }
}
