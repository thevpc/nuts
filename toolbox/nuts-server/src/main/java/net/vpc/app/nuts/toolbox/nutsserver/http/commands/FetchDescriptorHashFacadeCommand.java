package net.vpc.app.nuts.toolbox.nutsserver.http.commands;

import net.vpc.app.nuts.toolbox.nutsserver.AbstractFacadeCommand;
import net.vpc.app.nuts.toolbox.nutsserver.FacadeCommandContext;
import net.vpc.common.util.ListMap;

import java.io.IOException;

public class FetchDescriptorHashFacadeCommand extends AbstractFacadeCommand {
    public FetchDescriptorHashFacadeCommand() {
        super("fetch-descriptor-hash");
    }

    @Override
    public void executeImpl(FacadeCommandContext context) throws IOException {
        ListMap<String, String> parameters = context.getParameters();
        String id = parameters.getOne("id");
        boolean transitive = parameters.containsKey("transitive");
        String hash = null;
        try {
            hash = context.getWorkspace().fetch().id(id)
                    .session(context.getSession()).transitive(transitive)
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
