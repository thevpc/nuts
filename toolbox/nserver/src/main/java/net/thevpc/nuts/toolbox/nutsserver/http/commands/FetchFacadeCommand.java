package net.thevpc.nuts.toolbox.nutsserver.http.commands;

import net.thevpc.nuts.NDefinition;
import net.thevpc.nuts.NFetchCmd;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.toolbox.nutsserver.AbstractFacadeCommand;
import net.thevpc.nuts.toolbox.nutsserver.FacadeCommandContext;

import java.io.IOException;
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
        NDefinition fetch = null;
        try {
            fetch = NFetchCmd.of(id)
                    .setTransitive(transitive)
                    .getResultDefinition();
        } catch (Exception exc) {
            //
        }
        if (fetch != null && fetch.getContent().map(NPath::exists).orElse(false)) {
            context.sendResponseFile(200, fetch.getContent().orNull());
        } else {
            context.sendError(404, "File Not Found");
        }
    }
}
