package net.thevpc.nuts.toolbox.nutsserver.http.commands;

import net.thevpc.nuts.NutsConstants;
import net.thevpc.nuts.NutsDefinition;
import net.thevpc.nuts.toolbox.nutsserver.AbstractFacadeCommand;
import net.thevpc.nuts.toolbox.nutsserver.FacadeCommandContext;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class GetBootFacadeCommand extends AbstractFacadeCommand {
    public GetBootFacadeCommand() {
        super("boot");
    }
//            @Override
//            public void execute(FacadeCommandContext context) throws IOException {
//                executeImpl(context);
//            }

    @Override
    public void executeImpl(FacadeCommandContext context) throws IOException {
        String version = null;
        for (Map.Entry<String, List<String>> e : context.getParameters().entrySet()) {
            if (e.getKey().equals("version")) {
                version = e.getValue().toString();
            } else {
                version = e.getKey();
            }
        }
        if (version == null) {
            NutsDefinition def = context.getSession().search().addId(NutsConstants.Ids.NUTS_API).setLatest(true).setContent(true).getResultDefinitions().first();
            if (def != null) {
                context.addResponseHeader("content-disposition", "attachment; filename=\"nuts-" + def.getId().getVersion().toString() + ".jar\"");
                context.sendResponseFile(200, def.getPath());
            } else {
                context.sendError(404, "File Note Found");
            }
        } else {
            NutsDefinition def = context.getSession().fetch().setId(NutsConstants.Ids.NUTS_API + "#" + version).setContent(true).getResultDefinition();
            if (def != null) {
                context.addResponseHeader("content-disposition", "attachment; filename=\"nuts-" + def.getId().getVersion().toString() + ".jar\"");
                context.sendResponseFile(200, def.getPath());
            } else {
                context.sendError(404, "File Note Found");
            }
        }
    }
}
