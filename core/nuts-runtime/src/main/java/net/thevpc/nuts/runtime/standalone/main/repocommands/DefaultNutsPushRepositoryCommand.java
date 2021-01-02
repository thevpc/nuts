/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.main.repocommands;

import java.util.logging.Level;

import net.thevpc.nuts.NutsConstants;
import net.thevpc.nuts.NutsLogger;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.spi.NutsPushRepositoryCommand;
import net.thevpc.nuts.NutsRepository;
import net.thevpc.nuts.runtime.core.repos.NutsRepositoryExt;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.NutsLogVerb;
import net.thevpc.nuts.runtime.standalone.repocommands.AbstractNutsPushRepositoryCommand;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

/**
 *
 * @author thevpc
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
        NutsSession session = getValidWorkspaceSession();
        NutsWorkspaceUtils.of(getRepo().getWorkspace()).checkSession(session);
        getRepo().security().checkAllowed(NutsConstants.Permissions.PUSH, "push", session);
        try {
            NutsRepositoryExt.of(getRepo()).pushImpl(this);
            if (LOG.isLoggable(Level.FINEST)) {

                LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.SUCCESS).log( "{0} Push {1}", CoreStringUtils.alignLeft(getRepo().getName(), 20), getId());
            }
        } catch (RuntimeException ex) {

            if (LOG.isLoggable(Level.FINEST)) {
                LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.FAIL).log( "{0} Push {1}", CoreStringUtils.alignLeft(getRepo().getName(), 20), getId());
            }
        }
        return this;
    }
}
