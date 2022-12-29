package net.thevpc.nuts.toolbox.nutsserver.http.commands;

import net.thevpc.nuts.NIdBuilder;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.toolbox.nutsserver.AbstractFacadeCommand;
import net.thevpc.nuts.toolbox.nutsserver.FacadeCommandContext;

import java.io.IOException;

public class VersionFacadeCommand extends AbstractFacadeCommand {
    public VersionFacadeCommand() {
        super("version");
    }

    @Override
    public void executeImpl(FacadeCommandContext context) throws IOException {
        NSession session = context.getSession();
        context.sendResponseText(200,
                NIdBuilder.of()
                        .setRepository(context.getServerId())
                        .setGroupId("net.thevpc.nuts")
                        .setArtifactId("nuts-server")
                        .setVersion(session.getWorkspace().getRuntimeId().getVersion().toString())
                        .build().toString()
        );
    }
}
