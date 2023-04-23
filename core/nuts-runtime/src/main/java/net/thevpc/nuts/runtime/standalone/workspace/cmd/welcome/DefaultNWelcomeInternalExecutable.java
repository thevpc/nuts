/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.welcome;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.app.util.NAppUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultInternalNExecutableCommand;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author thevpc
 */
public class DefaultNWelcomeInternalExecutable extends DefaultInternalNExecutableCommand {

    public DefaultNWelcomeInternalExecutable(String[] args, NExecCommand execCommand) {
        super("welcome", args, execCommand);
    }

    @Override
    public int execute() {
        if(getSession().isDry()){
            dryExecute();
            return NExecutionException.SUCCESS;
        }
        if (NAppUtils.processHelpOptions(args, getSession())) {
            showDefaultHelp();
            return NExecutionException.SUCCESS;
        }
        NSession session = getSession();
        NCmdLine cmdLine = NCmdLine.of(args);
        while (cmdLine.hasNext()) {
            NArg a = cmdLine.peek().get(session);
            if (a.isOption()) {
                switch(a.key()) {
                    default: {
                        getSession().configureLast(cmdLine);
                    }
                }
            } else {
                getSession().configureLast(cmdLine);
            }
        }

        if (session.isPlainOut()) {
            session.out().resetLine().println(NWorkspaceExt.of(session.getWorkspace()).getWelcomeText(session));
        } else {
            Map<String, Object> welcome = new LinkedHashMap<>();
            welcome.put("message", "Welcome to nuts. Yeah, it is working...");
            welcome.put("name", NTexts.of(session).ofStyled("nuts", NTextStyle.primary(1)));
            welcome.put("long-name", "Network Updatable Things Services");
            welcome.put("description", "The Free and Open Source Package Manager for Java (TM) and other Things ...");
            welcome.put("url", NPath.of("https://github.com/thevpc/nuts",session));
            welcome.put("author", "thevpc");
            welcome.put("api-id", session.getWorkspace().getApiId().builder().setVersion("").build());
            welcome.put("api-version", session.getWorkspace().getApiVersion());
            welcome.put("runtime-id", session.getWorkspace().getRuntimeId().builder().setVersion("").build());
            welcome.put("runtime-version", session.getWorkspace().getRuntimeId().getVersion());
            welcome.put("workspace", NLocations.of(session).getWorkspaceLocation());
            welcome.put("hash-name", NPath.of(session.getWorkspace().getHashName(),session));
            if (session.isPlainOut()) {
                session = session.copy().setOutputFormat(NContentType.PROPS);
            }
            session.out().resetLine().println(welcome);
        }
        return NExecutionException.SUCCESS;
    }

}
