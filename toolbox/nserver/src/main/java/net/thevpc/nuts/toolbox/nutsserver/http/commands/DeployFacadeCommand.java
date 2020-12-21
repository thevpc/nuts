package net.thevpc.nuts.toolbox.nutsserver.http.commands;

import net.thevpc.nuts.NutsDescriptor;
import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.toolbox.nutsserver.AbstractFacadeCommand;
import net.thevpc.nuts.toolbox.nutsserver.FacadeCommandContext;
import net.thevpc.nuts.toolbox.nutsserver.util.ItemStreamInfo;
import net.thevpc.nuts.toolbox.nutsserver.util.MultipartStreamHelper;
import net.thevpc.common.strings.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

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
        if (StringUtils.isBlank(boundary)) {
            context.sendError(400, "Invalid JShellCommandNode Arguments : " + getName() + " . Invalid format.");
            return;
        }
        MultipartStreamHelper stream = new MultipartStreamHelper(context.getRequestBody(), boundary);
        NutsDescriptor descriptor = null;
        String receivedContentHash = null;
        InputStream content = null;
        Path contentFile = null;
        for (ItemStreamInfo info : stream) {
            String name = info.resolveVarInHeader("Content-Disposition", "name");
            switch (name) {
                case "descriptor":
                    try {
                        descriptor = context.getWorkspace().descriptor().parser().parse(info.getContent());
                    } finally {
                        info.getContent().close();
                    }
                    break;
                case "content-hash":
                    try {
                        receivedContentHash = context.getWorkspace().io().hash().source(info.getContent()).computeString();
                    } finally {
                        info.getContent().close();
                    }
                    break;
                case "content":
                    contentFile = context.getWorkspace().io().tmp().createTempFile(
                            context.getWorkspace().locations().getDefaultIdFilename(
                                    descriptor.getId().builder().setFaceDescriptor().build()
                            )
                    );
                    context.getWorkspace().io().copy()
                            .setSession(context.getSession())
                            .setSource(info.getContent())
                            .setTarget(contentFile)
                            .run();
                    break;
            }
        }
        if (contentFile == null) {
            context.sendError(400, "Invalid JShellCommandNode Arguments : " + getName() + " : Missing File");
        }
        NutsId id = context.getWorkspace().deploy().setContent(contentFile)
                .setSha1(receivedContentHash)
                .setDescriptor(descriptor)
                .setSession(context.getSession().copy())
                .getResult()[0];
//                NutsId id = workspace.deploy(content, descriptor, null);
        context.sendResponseText(200, id.toString());
    }
}
