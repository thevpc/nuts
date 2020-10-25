package net.vpc.app.nuts.toolbox.nutsserver.http.commands;

import net.vpc.app.nuts.toolbox.nutsserver.AbstractFacadeCommand;
import net.vpc.app.nuts.toolbox.nutsserver.FacadeCommandContext;

import java.io.IOException;

public class VersionFacadeCommand extends AbstractFacadeCommand {
    public VersionFacadeCommand() {
        super("version");
    }

    @Override
    public void executeImpl(FacadeCommandContext context) throws IOException {
        context.sendResponseText(200,
                context.getWorkspace()
                        .id().builder()
                        .setNamespace(context.getServerId())
                        .setGroupId("net.vpc.app.nuts")
                        .setArtifactId("nuts-server")
                        .setVersion(context.getWorkspace().getRuntimeId().getVersion().toString())
                        .build().toString()
        );
    }
}
