package net.thevpc.nuts.toolbox.nutsserver.http.commands;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NDigest;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.toolbox.nutsserver.AbstractFacadeCommand;
import net.thevpc.nuts.toolbox.nutsserver.FacadeCommandContext;
import net.thevpc.nuts.toolbox.nutsserver.util.ItemStreamInfo;
import net.thevpc.nuts.toolbox.nutsserver.util.MultipartStreamHelper;
import net.thevpc.nuts.util.NBlankable;

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
        if (NBlankable.isBlank(boundary)) {
            context.sendError(400, "invalid NShellCommandNode arguments : " + getName() + " . invalid format.");
            return;
        }
        NSession session = context.getSession();
        MultipartStreamHelper stream = new MultipartStreamHelper(context.getRequestBody(), boundary, session);
        NDescriptor descriptor = null;
        String receivedContentHash = null;
        InputStream content = null;
        String contentFile = null;
        for (ItemStreamInfo info : stream) {
            String name = info.resolveVarInHeader("Content-Disposition", "name");
            switch (name) {
                case "descriptor":
                    try {
                        descriptor = NDescriptorParser.of(session)
                                .setSession(session).parse(info.getContent()).get(session);
                    } finally {
                        info.getContent().close();
                    }
                    break;
                case "content-hash":
                    try {
                        receivedContentHash = NDigest.of(session).setSource(info.getContent()).computeString();
                    } finally {
                        info.getContent().close();
                    }
                    break;
                case "content":
                    contentFile = NPath
                            .ofTempFile(
                                    NLocations.of(session).getDefaultIdFilename(
                                            descriptor.getId().builder().setFaceDescriptor().build()
                                    ),session).toString();
                    NCp.of(session)
                            .setSession(session)
                            .setSource(info.getContent())
                            .setTarget(NPath.of(contentFile,session))
                            .run();
                    break;
            }
        }
        if (contentFile == null) {
            context.sendError(400, "invalid NShellCommandNode arguments : " + getName() + " : missing file");
        }
        NId id = NDeployCmd.of(session.copy()).setContent(NPath.of(contentFile,session))
                .setSha1(receivedContentHash)
                .setDescriptor(descriptor)
                .getResult().get(0);
//                NutsId id = workspace.deploy(content, descriptor, null);
        context.sendResponseText(200, id.toString());
    }
}
