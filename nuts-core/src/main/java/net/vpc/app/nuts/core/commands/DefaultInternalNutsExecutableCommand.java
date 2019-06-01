/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.commands;

import net.vpc.app.nuts.NutsExecutableType;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;

/**
 *
 * @author vpc
 */
public abstract class DefaultInternalNutsExecutableCommand extends AbstractNutsExecutableCommand {

    protected String[] args;
    protected NutsWorkspace ws;
    private NutsSession session;

    public DefaultInternalNutsExecutableCommand(String name, String[] args, NutsWorkspace ws, NutsSession session) {
        super(name, name,NutsExecutableType.INTERNAL);
        this.args = args;
        this.ws = ws;
        this.session = session;
    }

    public NutsSession getSession() {
        return session;
    }

    public NutsSession getSession(boolean autoCreate) {
        if (session == null) {
            if (autoCreate) {
                session = ws.createSession();
            }
        }
        return session;
    }

    @Override
    public NutsId getId() {
        return null;
    }

    protected void showDefaultHelp() {
        session.getTerminal().fout().println(getHelpText());
    }

    @Override
    public String getHelpText() {
        return ws.io().getResourceString("/net/vpc/app/nuts/command/" + name + ".help", getClass(), name + ": no help found");
    }

}
