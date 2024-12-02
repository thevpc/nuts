package net.thevpc.nuts.tutorial.lib;

import net.thevpc.nuts.*;

public class ExamplesOfSearch {

    public void executeAll() {
        executeSearch();
    }

    public void executeSearch() {
        NSession session = NSession.of();
        session.out().println("Example of ## Search ##");
        for (NDefinition def : NSearchCmd.of()
                .addId(NConstants.Ids.NUTS_API)
                .setInstallStatus(NInstallStatusFilters.of().byDeployed(true))
                .getResultDefinitions()) {
            session.out().println(def);
        }
    }

}
