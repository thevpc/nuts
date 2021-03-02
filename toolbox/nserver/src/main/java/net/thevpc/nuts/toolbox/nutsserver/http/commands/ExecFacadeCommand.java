package net.thevpc.nuts.toolbox.nutsserver.http.commands;

import net.thevpc.common.collections.ListValueMap;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsTerminalMode;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.toolbox.nutsserver.AbstractFacadeCommand;
import net.thevpc.nuts.toolbox.nutsserver.FacadeCommandContext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class ExecFacadeCommand extends AbstractFacadeCommand {
    public ExecFacadeCommand() {
        super("exec");
    }

    @Override
    public void executeImpl(FacadeCommandContext context) throws IOException {

//                String boundary = context.getRequestHeaderFirstValue("Content-type");
//                if (StringUtils.isEmpty(boundary)) {
//                    context.sendError(400, "Invalid JShellCommandNode Arguments : " + getName() + " . Invalid format.");
//                    return;
//                }
//                MultipartStreamHelper stream = new MultipartStreamHelper(context.getRequestBody(), boundary);
//                NutsDescriptor descriptor = null;
//                String receivedContentHash = null;
//                InputStream content = null;
//                File contentFile = null;
//                for (ItemStreamInfo info : stream) {
//                    String name = info.resolveVarInHeader("Content-Disposition", "name");
//                    switch (name) {
//                        case "descriptor":
//                            descriptor = CoreNutsUtils.parseNutsDescriptor(info.getContent(), true);
//                            break;
//                        case "content-hash":
//                            receivedContentHash = CoreSecurityUtils.evalSHA1(info.getContent(), true);
//                            break;
//                        case "content":
//                            contentFile = CoreIOUtils.createTempFile(descriptor, false);
//                            CoreIOUtils.copy(info.getContent(), contentFile, true, true);
//                            break;
//                    }
//                }
//                if (contentFile == null) {
//                    context.sendError(400, "Invalid JShellCommandNode Arguments : " + getName() + " : Missing File");
//                }
        ListValueMap<String, String> parameters = context.getParameters();
        List<String> cmd = parameters.getValues("cmd");
        NutsWorkspace ws = context.getWorkspace();

        NutsSession session = ws.createSession();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        session.getTerminal().setOut(ws.io().createPrintStream(out, NutsTerminalMode.FILTERED, session));
        session.getTerminal().setIn(new ByteArrayInputStream(new byte[0]));

        int result = ws.exec()
                .addCommand(cmd)
                .setSession(session)
                .getResult();

        context.sendResponseText(200, String.valueOf(result) + "\n" + new String(out.toByteArray()));
    }
}
