package net.thevpc.nuts.toolbox.nutsserver.http.commands;

import net.thevpc.common.collections.ListValueMap;
import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.toolbox.nutsserver.AbstractFacadeCommand;
import net.thevpc.nuts.toolbox.nutsserver.FacadeCommandContext;

import java.io.IOException;

public class ResolveIdFacadeCommand extends AbstractFacadeCommand {
    public ResolveIdFacadeCommand() {
        super("resolve-id");
    }

    @Override
    public void executeImpl(FacadeCommandContext context) throws IOException {
        ListValueMap<String, String> parameters = context.getParameters();
        String id = parameters.getFirst("id");
        boolean transitive = parameters.containsKey("transitive");
        NutsId fetch = null;
        try {
            fetch = context.getWorkspace().fetch().setId(id)
                    .setSession(context.getSession().copy())
                    .setTransitive(transitive).getResultId();
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
