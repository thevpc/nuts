package net.thevpc.nuts.tutorial.lib;

import net.thevpc.nuts.NDefinition;
import net.thevpc.nuts.NInstallStatusFilters;
import net.thevpc.nuts.NSearchCommand;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.Nuts;

public class Hello {

    public static void main(String[] args) {
        NSession session = Nuts.openWorkspace();
        for (NDefinition def : NSearchCommand.of(session)
                //.addId("net.thevpc.nuts:nuts")
                .setInstallStatus(NInstallStatusFilters.of(session).byDeployed(true))
                .getResultDefinitions()) {
            session.out().println(def);
        }

    }
}
