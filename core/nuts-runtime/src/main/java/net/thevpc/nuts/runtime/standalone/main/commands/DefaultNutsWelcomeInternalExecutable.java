/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.main.commands;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;

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
        if (getSession().isPlainOut()) {
            getSession().out().println(NutsWorkspaceExt.of(getSession().getWorkspace()).getWelcomeText());
        } else {
            Map<String, String> welcome = new LinkedHashMap<>();
            welcome.put("message", "Welcome to nuts. Yeah, it is working...");
            welcome.put("name", "nuts");
            welcome.put("long-name", "Network Updatable Things Services");
            welcome.put("description", "The Freen and Open Source Package Manager for Java (TM) and other Things ...");
            welcome.put("url", "http://github.com/thevpc/nuts");
            welcome.put("author", "thevpc");
            welcome.put("api-id", getSession().getWorkspace().getApiId().builder().setVersion("").build().toString());
            welcome.put("api-version", getSession().getWorkspace().getApiVersion());
            welcome.put("runtime-id", getSession().getWorkspace().getRuntimeId().builder().setVersion("").build().toString());
            welcome.put("runtime-version", getSession().getWorkspace().getRuntimeId().builder().setVersion("").build().toString());
            welcome.put("workspace", getSession().getWorkspace().locations().getWorkspaceLocation().toString());
            getSession().formatObject(welcome).println();
        }
    }

}
