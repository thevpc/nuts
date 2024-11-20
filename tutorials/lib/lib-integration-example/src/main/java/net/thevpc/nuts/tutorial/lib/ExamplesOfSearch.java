package net.thevpc.nuts.tutorial.lib;

import net.thevpc.nuts.*;

public class ExamplesOfSearch {

    public void executeAll(NSession session) {
        executeSearch(session);
    }

    public void executeSearch(NSession session) {
        session.out().println("Example of ## Search ##");
        for (NDefinition def : NSearchCmd.of()
                .addId(NConstants.Ids.NUTS_API)
                .setInstallStatus(NInstallStatusFilters.of().byDeployed(true))
                .getResultDefinitions()) {
            session.out().println(def);
        }
    }

}
