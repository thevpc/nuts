package net.thevpc.nuts.toolbox.nutsserver.http.commands;

import net.thevpc.nuts.NExecCmd;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.io.NSessionTerminal;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.toolbox.nutsserver.AbstractFacadeCommand;
import net.thevpc.nuts.toolbox.nutsserver.FacadeCommandContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExecFacadeCommand extends AbstractFacadeCommand {
    public ExecFacadeCommand() {
        super("exec");
    }

    @Override
    public void executeImpl(FacadeCommandContext context) throws IOException {

//                String boundary = context.getRequestHeaderFirstValue("Content-type");
//                if (StringUtils.isEmpty(boundary)) {
//                    context.sendError(400, "Invalid NShellCommandNode Arguments : " + getName() + " . Invalid format.");
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
//                    context.sendError(400, "Invalid NShellCommandNode Arguments : " + getName() + " : Missing File");
//                }
        Map<String, List<String>> parameters = context.getParameters();
        List<String> cmd = parameters.get("cmd");
        if (cmd == null) {
            cmd = new ArrayList<>();
        }
        NSession session = context.getSession().copy();
        session.setTerminal(NSessionTerminal.of(
                new ByteArrayInputStream(new byte[0]),
                NPrintStream.ofMem().setTerminalMode(NTerminalMode.FILTERED),
                NPrintStream.ofMem()
        ));
        int result = NExecCmd.of()
                .addCommand(cmd)
                .getResultCode();
        context.sendResponseText(200, result + "\n" + session.out().toString());
    }
}
