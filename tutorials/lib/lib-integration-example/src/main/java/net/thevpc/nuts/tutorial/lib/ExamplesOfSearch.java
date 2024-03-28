package net.thevpc.nuts.tutorial.lib;

import net.thevpc.nuts.*;

public class ExamplesOfSearch {

    public void executeAll(NSession session) {
        executeSearch(session);
    }

    public void executeSearch(NSession session) {
        session.out().println("Example of ## Search ##");
        for (NDefinition def : NSearchCmd.of(session)
                .addId("net.thevpc.nuts:nuts")
                .setInstallStatus(NInstallStatusFilters.of(session).byDeployed(true))
                .getResultDefinitions()) {
            session.out().println(def);
        }
    }

}
