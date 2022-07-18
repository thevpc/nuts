package net.thevpc.nuts.toolbox.nutsserver.http.commands;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NutsCp;
import net.thevpc.nuts.io.NutsDigest;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.spi.NutsPaths;
import net.thevpc.nuts.toolbox.nutsserver.AbstractFacadeCommand;
import net.thevpc.nuts.toolbox.nutsserver.FacadeCommandContext;
import net.thevpc.nuts.toolbox.nutsserver.util.ItemStreamInfo;
import net.thevpc.nuts.toolbox.nutsserver.util.MultipartStreamHelper;

import java.io.IOException;
import java.io.InputStream;

public class DeployFacadeCommand extends AbstractFacadeCommand {
    public DeployFacadeCommand() {
        super("deploy");
    }
//            @Override
//            public void execute(FacadeCommandContext context) throws IOException {
//                executeImpl(context);
//            }

    @Override
    public void executeImpl(FacadeCommandContext context) throws IOException {
        String boundary = context.getRequestHeaderFirstValue("Content-type");
        if (NutsBlankable.isBlank(boundary)) {
            context.sendError(400, "invalid JShellCommandNode arguments : " + getName() + " . invalid format.");
            return;
        }
        NutsSession session = context.getSession();
        MultipartStreamHelper stream = new MultipartStreamHelper(context.getRequestBody(), boundary, session);
        NutsDescriptor descriptor = null;
        String receivedContentHash = null;
        InputStream content = null;
        String contentFile = null;
        for (ItemStreamInfo info : stream) {
            String name = info.resolveVarInHeader("Content-Disposition", "name");
            switch (name) {
                case "descriptor":
                    try {
                        descriptor = NutsDescriptorParser.of(session)
                                .setSession(session).parse(info.getContent()).get(session);
                    } finally {
                        info.getContent().close();
                    }
                    break;
                case "content-hash":
                    try {
                        receivedContentHash = NutsDigest.of(session).setSource(info.getContent()).computeString();
                    } finally {
                        info.getContent().close();
                    }
                    break;
                case "content":
                    contentFile = NutsPaths.of(session)
                            .createTempFile(
                                    session.locations().getDefaultIdFilename(
                                            descriptor.getId().builder().setFaceDescriptor().build()
                                    )).toString();
                    NutsCp.of(session)
                            .setSession(session)
                            .setSource(info.getContent())
                            .setTarget(NutsPath.of(contentFile,session))
                            .run();
                    break;
            }
        }
        if (contentFile == null) {
            context.sendError(400, "invalid JShellCommandNode arguments : " + getName() + " : missing file");
        }
        NutsId id = session.deploy().setContent(NutsPath.of(contentFile,session))
                .setSha1(receivedContentHash)
                .setDescriptor(descriptor)
                .setSession(session.copy())
                .getResult().get(0);
//                NutsId id = workspace.deploy(content, descriptor, null);
        context.sendResponseText(200, id.toString());
    }
}
