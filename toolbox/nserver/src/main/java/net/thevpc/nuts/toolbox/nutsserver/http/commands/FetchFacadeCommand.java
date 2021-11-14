package net.thevpc.nuts.toolbox.nutsserver.http.commands;

import net.thevpc.nuts.NutsDefinition;
import net.thevpc.nuts.toolbox.nutsserver.AbstractFacadeCommand;
import net.thevpc.nuts.toolbox.nutsserver.FacadeCommandContext;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

public class FetchFacadeCommand extends AbstractFacadeCommand {
    public FetchFacadeCommand() {
        super("fetch");
    }

    @Override
    public void executeImpl(FacadeCommandContext context) throws IOException {
        Map<String, List<String>> parameters = context.getParameters();
        List<String> idList = parameters.get("id");
        String id = (idList==null || idList.isEmpty())?null: idList.get(0);
        boolean transitive = parameters.containsKey("transitive");
        NutsDefinition fetch = null;
        try {
            fetch = context.getSession().fetch().setId(id).setSession(context.getSession().copy().setTransitive(transitive))
                    .getResultDefinition();
        } catch (Exception exc) {
            //
        }
        if (fetch != null && fetch.getFile() != null && Files.exists(fetch.getFile())) {
            context.sendResponseFile(200, fetch.getFile());
        } else {
            context.sendError(404, "File Not Found");
        }
    }
}
