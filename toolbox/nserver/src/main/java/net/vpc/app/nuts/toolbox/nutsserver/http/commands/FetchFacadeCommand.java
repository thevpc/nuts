package net.vpc.app.nuts.toolbox.nutsserver.http.commands;

import net.vpc.app.nuts.NutsDefinition;
import net.vpc.app.nuts.toolbox.nutsserver.AbstractFacadeCommand;
import net.vpc.app.nuts.toolbox.nutsserver.FacadeCommandContext;
import net.vpc.common.util.ListMap;

import java.io.IOException;
import java.nio.file.Files;

public class FetchFacadeCommand extends AbstractFacadeCommand {
    public FetchFacadeCommand() {
        super("fetch");
    }

    @Override
    public void executeImpl(FacadeCommandContext context) throws IOException {
        ListMap<String, String> parameters = context.getParameters();
        String id = parameters.getOne("id");
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
