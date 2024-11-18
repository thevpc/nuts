package net.thevpc.nuts.toolbox.nutsserver.http.commands;

import net.thevpc.nuts.NFetchCmd;
import net.thevpc.nuts.NId;
import net.thevpc.nuts.toolbox.nutsserver.AbstractFacadeCommand;
import net.thevpc.nuts.toolbox.nutsserver.FacadeCommandContext;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ResolveIdFacadeCommand extends AbstractFacadeCommand {
    public ResolveIdFacadeCommand() {
        super("resolve-id");
    }

    @Override
    public void executeImpl(FacadeCommandContext context) throws IOException {
        Map<String, List<String>> parameters = context.getParameters();
        List<String> idList = parameters.get("id");
        String id = (idList==null || idList.isEmpty())?null: idList.get(0);
        boolean transitive = parameters.containsKey("transitive");
        NId fetch = null;
        try {
            fetch = NFetchCmd.of(id)
                    .setTransitive(transitive)
                    .getResultId();
        } catch (Exception exc) {
            //
        }
        if (fetch != null) {
            context.sendResponseText(200, fetch.toString());
        } else {
            context.sendError(404, "Nuts not Found");
        }
    }
}
