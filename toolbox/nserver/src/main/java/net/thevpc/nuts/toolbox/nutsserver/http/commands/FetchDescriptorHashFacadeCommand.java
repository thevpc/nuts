package net.thevpc.nuts.toolbox.nutsserver.http.commands;

import net.thevpc.common.util.ListValueMap;
import net.thevpc.nuts.toolbox.nutsserver.AbstractFacadeCommand;
import net.thevpc.nuts.toolbox.nutsserver.FacadeCommandContext;

import java.io.IOException;

public class FetchDescriptorHashFacadeCommand extends AbstractFacadeCommand {
    public FetchDescriptorHashFacadeCommand() {
        super("fetch-descriptor-hash");
    }

    @Override
    public void executeImpl(FacadeCommandContext context) throws IOException {
        ListValueMap<String, String> parameters = context.getParameters();
        String id = parameters.getFirst("id");
        boolean transitive = parameters.containsKey("transitive");
        String hash = null;
        try {
            hash = context.getWorkspace().fetch().setId(id)
                    .setSession(context.getSession()).setTransitive(transitive)
                    .getResultDescriptorHash();
        } catch (Exception exc) {
            //
        }
        if (hash != null) {
            context.sendResponseText(200, hash);
        } else {
            context.sendError(404, "Nuts not Found");
        }
    }
}
