/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands.welcome;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.wscommands.exec.DefaultInternalNutsExecutableCommand;

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
        if (CoreNutsUtils.isIncludesHelpOption(args)) {
            showDefaultHelp();
            return;
        }
        NutsSession session = getSession();
        NutsWorkspace ws = session.getWorkspace();
        NutsCommandLine commandLine = ws.commandLine().create(args);
        while (commandLine.hasNext()) {
            NutsArgument a = commandLine.peek();
            if (a.isOption()) {
                switch (a.getKey().getString()) {
                    case "--help": {
                        commandLine.skipAll();
                        showDefaultHelp();
                        return;
                    }
                    default: {
                        getSession().configureLast(commandLine);
                    }
                }
            } else {
                getSession().configureLast(commandLine);
            }
        }

        if (!session.isBot() && session.isPlainOut()) {
            session.out().resetLine().println(NutsWorkspaceExt.of(ws).getWelcomeText(session));
        } else {
            Map<String, Object> welcome = new LinkedHashMap<>();
            welcome.put("message", "Welcome to nuts. Yeah, it is working...");
            welcome.put("name", ws.text().forStyled("nuts", NutsTextStyle.primary(1)));
            welcome.put("long-name", "Network Updatable Things Services");
            welcome.put("description", "The Free and Open Source Package Manager for Java (TM) and other Things ...");
            welcome.put("url", ws.io().path("http://github.com/thevpc/nuts"));
            welcome.put("author", "thevpc");
            welcome.put("api-id", ws.getApiId().builder().setVersion("").build());
            welcome.put("api-version", ws.getApiVersion());
            welcome.put("runtime-id", ws.getRuntimeId().builder().setVersion("").build());
            welcome.put("runtime-version", ws.getRuntimeId().getVersion());
            welcome.put("workspace", ws.io().path(ws.locations().getWorkspaceLocation()));
            welcome.put("hash-name", ws.io().path(ws.getHashName()));
            if (session.isPlainOut()) {
                session = session.copy().setOutputFormat(NutsContentType.PROPS);
            }
            session.out().resetLine();
            session.getWorkspace().formats().object(welcome).println();
        }
    }

}
