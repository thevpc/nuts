/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.welcome;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;


import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.app.util.NAppUtils;
import net.thevpc.nuts.runtime.standalone.util.ExtraApiUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.internal.DefaultInternalNExecutableCommand;
import net.thevpc.nuts.text.NText;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author thevpc
 */
public class DefaultNWelcomeInternalExecutable extends DefaultInternalNExecutableCommand {

    public DefaultNWelcomeInternalExecutable(NWorkspace workspace,String[] args, NExecCmd execCommand) {
        super(workspace,"welcome", args, execCommand);
    }

    @Override
    public int execute() {
        NSession session = workspace.currentSession();
        boolean dry = ExtraApiUtils.asBoolean(getExecCommand().getDry());
        if(dry){
            dryExecute();
            return NExecutionException.SUCCESS;
        }
        if (NAppUtils.processHelpOptions(args)) {
            showDefaultHelp();
            return NExecutionException.SUCCESS;
        }
        NCmdLine cmdLine = NCmdLine.of(args);
        while (cmdLine.hasNext()) {
            NArg a = cmdLine.peek().get();
            if (a.isOption()) {
                switch(a.key()) {
                    default: {
                        session.configureLast(cmdLine);
                    }
                }
            } else {
                session.configureLast(cmdLine);
            }
        }

        if (session.isPlainOut()) {
            session.out().resetLine().println(NWorkspaceExt.of().getWelcomeText());
        } else {
            Map<String, Object> welcome = new LinkedHashMap<>();
            welcome.put("message", "Welcome to nuts. Yeah, it is working...");
            welcome.put("name", NText.ofStyledPrimary1("nuts"));
            welcome.put("long-name", "Network Updatable Things Services");
            welcome.put("description", "The Free and Open Source Package Manager for Java (TM) and other Things ...");
            welcome.put("url", NPath.of("https://github.com/thevpc/nuts"));
            welcome.put("author", "thevpc");
            welcome.put("api-id", session.getWorkspace().getApiId().builder().setVersion("").build());
            welcome.put("api-version", session.getWorkspace().getApiVersion());
            welcome.put("runtime-id", session.getWorkspace().getRuntimeId().builder().setVersion("").build());
            welcome.put("runtime-version", session.getWorkspace().getRuntimeId().getVersion());
            welcome.put("workspace", NWorkspace.of().getWorkspaceLocation());
            welcome.put("hash-name", NPath.of(session.getWorkspace().getDigestName()));
            if (session.isPlainOut()) {
                session = session.copy().setOutputFormat(NContentType.PROPS);
            }
            session.out().resetLine().println(welcome);
        }
        return NExecutionException.SUCCESS;
    }

}
