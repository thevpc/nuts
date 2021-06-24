package net.thevpc.nuts.toolbox.nutsserver.http.commands;

import net.thevpc.nuts.NutsDescriptor;
import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.toolbox.nutsserver.AbstractFacadeCommand;
import net.thevpc.nuts.toolbox.nutsserver.FacadeCommandContext;
import net.thevpc.nuts.toolbox.nutsserver.bundled._StringUtils;
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
        if (_StringUtils.isBlank(boundary)) {
            context.sendError(400, "invalid JShellCommandNode arguments : " + getName() + " . invalid format.");
            return;
        }
        MultipartStreamHelper stream = new MultipartStreamHelper(context.getRequestBody(), boundary,context.getSession());
        NutsDescriptor descriptor = null;
        String receivedContentHash = null;
        InputStream content = null;
        String contentFile = null;
        for (ItemStreamInfo info : stream) {
            String name = info.resolveVarInHeader("Content-Disposition", "name");
            switch (name) {
                case "descriptor":
                    try {
                        descriptor = context.getWorkspace().descriptor().parser()
                                .setSession(context.getSession()).parse(info.getContent());
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
                    contentFile = context.getWorkspace().io().tmp()
                            .setSession(context.getSession())
                            .createTempFile(
                            context.getWorkspace().locations().getDefaultIdFilename(
                                    descriptor.getId().builder().setFaceDescriptor().build()
                            ));
                    context.getWorkspace().io().copy()
                            .setSession(context.getSession())
                            .setSource(info.getContent())
                            .setTarget(contentFile)
                            .run();
                    break;
            }
        }
        if (contentFile == null) {
            context.sendError(400, "invalid JShellCommandNode arguments : " + getName() + " : missing file");
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
