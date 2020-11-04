package net.thevpc.nuts.toolbox.nutsserver.http.commands;

import net.thevpc.common.util.ListValueMap;
import net.thevpc.nuts.NutsDescriptor;
import net.thevpc.nuts.toolbox.nutsserver.AbstractFacadeCommand;
import net.thevpc.nuts.toolbox.nutsserver.FacadeCommandContext;

import java.io.IOException;

public class FetchDescriptorFacadeCommand extends AbstractFacadeCommand {
    public FetchDescriptorFacadeCommand() {
        super("fetch-descriptor");
    }

    @Override
    public void executeImpl(FacadeCommandContext context) throws IOException {
        ListValueMap<String, String> parameters = context.getParameters();
        String id = parameters.getFirst("id");
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
