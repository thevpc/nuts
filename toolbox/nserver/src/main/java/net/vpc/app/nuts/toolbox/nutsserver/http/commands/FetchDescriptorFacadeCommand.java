package net.vpc.app.nuts.toolbox.nutsserver.http.commands;

import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.toolbox.nutsserver.AbstractFacadeCommand;
import net.vpc.app.nuts.toolbox.nutsserver.FacadeCommandContext;
import net.vpc.common.util.ListMap;

import java.io.IOException;

public class FetchDescriptorFacadeCommand extends AbstractFacadeCommand {
    public FetchDescriptorFacadeCommand() {
        super("fetch-descriptor");
    }

    @Override
    public void executeImpl(FacadeCommandContext context) throws IOException {
        ListMap<String, String> parameters = context.getParameters();
        String id = parameters.getOne("id");
        boolean transitive = parameters.containsKey("transitive");
        NutsDescriptor fetch = null;
        try {
            fetch = context.getWorkspace().fetch().setId(id).setSession(context.getSession())
                    .setTransitive(transitive).getResultDescriptor();
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
