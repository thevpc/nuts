package net.thevpc.nuts.toolbox.nutsserver.http.commands;

import net.thevpc.common.collections.ListValueMap;
import net.thevpc.nuts.NutsDefinition;
import net.thevpc.nuts.toolbox.nutsserver.AbstractFacadeCommand;
import net.thevpc.nuts.toolbox.nutsserver.FacadeCommandContext;

import java.io.IOException;
import java.nio.file.Files;

public class FetchFacadeCommand extends AbstractFacadeCommand {
    public FetchFacadeCommand() {
        super("fetch");
    }

    @Override
    public void executeImpl(FacadeCommandContext context) throws IOException {
        ListValueMap<String, String> parameters = context.getParameters();
        String id = parameters.getFirst("id");
        boolean transitive = parameters.containsKey("transitive");
        NutsDefinition fetch = null;
        try {
            fetch = context.getWorkspace().fetch().setId(id).setSession(context.getSession()).setTransitive(transitive)
                    .getResultDefinition();
        } catch (Exception exc) {
            //
        }
        if (fetch != null && fetch.getPath() != null && Files.exists(fetch.getPath())) {
            context.sendResponseFile(200, fetch.getPath());
        } else {
            context.sendError(404, "File Not Found");
        }
    }
}
