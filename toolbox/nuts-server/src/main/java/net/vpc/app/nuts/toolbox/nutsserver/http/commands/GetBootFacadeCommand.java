package net.vpc.app.nuts.toolbox.nutsserver.http.commands;

import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsDefinition;
import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.toolbox.nutsserver.AbstractFacadeCommand;
import net.vpc.app.nuts.toolbox.nutsserver.FacadeCommandContext;
import net.vpc.app.nuts.toolbox.nutsserver.util.ItemStreamInfo;
import net.vpc.app.nuts.toolbox.nutsserver.util.MultipartStreamHelper;
import net.vpc.common.strings.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
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
            NutsDefinition def = context.getWorkspace().search().id(NutsConstants.Ids.NUTS_API).latest().content().getResultDefinitions().first();
            if (def != null) {
                context.addResponseHeader("content-disposition", "attachment; filename=\"nuts-" + def.getId().getVersion().toString() + ".jar\"");
                context.sendResponseFile(200, def.getPath());
            } else {
                context.sendError(404, "File Note Found");
            }
        } else {
            NutsDefinition def = context.getWorkspace().fetch().id(NutsConstants.Ids.NUTS_API + "#" + version).content().getResultDefinition();
            if (def != null) {
                context.addResponseHeader("content-disposition", "attachment; filename=\"nuts-" + def.getId().getVersion().toString() + ".jar\"");
                context.sendResponseFile(200, def.getPath());
            } else {
                context.sendError(404, "File Note Found");
            }
        }
    }
}
