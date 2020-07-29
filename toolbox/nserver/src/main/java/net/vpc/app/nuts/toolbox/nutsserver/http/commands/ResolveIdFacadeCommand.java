package net.vpc.app.nuts.toolbox.nutsserver.http.commands;

import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.toolbox.nutsserver.AbstractFacadeCommand;
import net.vpc.app.nuts.toolbox.nutsserver.FacadeCommandContext;
import net.vpc.common.util.ListMap;

import java.io.IOException;

public class ResolveIdFacadeCommand extends AbstractFacadeCommand {
    public ResolveIdFacadeCommand() {
        super("resolve-id");
    }

    @Override
    public void executeImpl(FacadeCommandContext context) throws IOException {
        ListMap<String, String> parameters = context.getParameters();
        String id = parameters.getOne("id");
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
