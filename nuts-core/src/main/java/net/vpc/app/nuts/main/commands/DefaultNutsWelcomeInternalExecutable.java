/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.main.commands;

import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.core.NutsWorkspaceExt;
import net.vpc.app.nuts.runtime.util.CoreNutsUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author vpc
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
            welcome.put("description", "The Open Source Package Manager for Java (TM) and other Things ...");
            welcome.put("url", "The Open Source Package Manager");
            welcome.put("author", "thevpc");
            welcome.put("api-id", getSession().getWorkspace().config().getApiId().builder().setVersion("").build().toString());
            welcome.put("api-version", getSession().getWorkspace().config().getApiVersion());
            welcome.put("runtime-id", getSession().getWorkspace().config().getRuntimeId().builder().setVersion("").build().toString());
            welcome.put("runtime-version", getSession().getWorkspace().config().getRuntimeId().builder().setVersion("").build().toString());
            getSession().formatObject(welcome).println();
        }
    }

}
