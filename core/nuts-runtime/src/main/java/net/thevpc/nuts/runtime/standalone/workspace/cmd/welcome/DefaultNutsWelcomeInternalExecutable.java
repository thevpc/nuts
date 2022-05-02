/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.welcome;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.runtime.standalone.app.util.NutsAppUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultInternalNutsExecutableCommand;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author thevpc
 */
public class DefaultNutsWelcomeInternalExecutable extends DefaultInternalNutsExecutableCommand {

    public DefaultNutsWelcomeInternalExecutable(String[] args, NutsSession session) {
        super("welcome", args, session);
    }

    @Override
    public void execute() {
        if (NutsAppUtils.processHelpOptions(args, getSession())) {
            showDefaultHelp();
            return;
        }
        NutsSession session = getSession();
        NutsCommandLine commandLine = NutsCommandLine.of(args);
        while (commandLine.hasNext()) {
            NutsArgument a = commandLine.peek().get(session);
            if (a.isOption()) {
                switch(a.getStringKey().orElse("")) {
                    default: {
                        getSession().configureLast(commandLine);
                    }
                }
            } else {
                getSession().configureLast(commandLine);
            }
        }

        if (session.isPlainOut()) {
            session.out().resetLine().println(NutsWorkspaceExt.of(session.getWorkspace()).getWelcomeText(session));
        } else {
            Map<String, Object> welcome = new LinkedHashMap<>();
            welcome.put("message", "Welcome to nuts. Yeah, it is working...");
            welcome.put("name", NutsTexts.of(session).ofStyled("nuts", NutsTextStyle.primary(1)));
            welcome.put("long-name", "Network Updatable Things Services");
            welcome.put("description", "The Free and Open Source Package Manager for Java (TM) and other Things ...");
            welcome.put("url", NutsPath.of("https://github.com/thevpc/nuts",session));
            welcome.put("author", "thevpc");
            welcome.put("api-id", session.getWorkspace().getApiId().builder().setVersion("").build());
            welcome.put("api-version", session.getWorkspace().getApiVersion());
            welcome.put("runtime-id", session.getWorkspace().getRuntimeId().builder().setVersion("").build());
            welcome.put("runtime-version", session.getWorkspace().getRuntimeId().getVersion());
            welcome.put("workspace", session.locations().getWorkspaceLocation());
            welcome.put("hash-name", NutsPath.of(session.getWorkspace().getHashName(),session));
            if (session.isPlainOut()) {
                session = session.copy().setOutputFormat(NutsContentType.PROPS);
            }
            session.out().resetLine().printlnf(welcome);
        }
    }

}
