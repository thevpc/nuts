/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;

/**
 *
 * @author thevpc
 */
public abstract class DefaultInternalNutsExecutableCommand extends AbstractNutsExecutableCommand {

    protected String[] args;
    private NutsSession session;

    public DefaultInternalNutsExecutableCommand(String name, String[] args, NutsSession session) {
        super(name, name, NutsExecutableType.INTERNAL);
        this.args = args;
        this.session = session;
    }

    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsId getId() {
        return null;
    }

    protected void showDefaultHelp() {
        session.out().println(getHelpText());
    }

    @Override
    public String getHelpText() {
        return getSession().getWorkspace().formats().text().loadFormattedString(
                "/net/thevpc/nuts/runtime/command/" + name + ".ntf",
                getClass().getClassLoader(), "no help found for " + name);
    }

    @Override
    public void dryExecute() {
        if (CoreNutsUtils.isIncludesHelpOption(args)) {
            getSession().out().println("[dry] ==show-help==");
            return;
        }
        NutsTextFormatManager text = getSession().getWorkspace().formats().text();
        getSession().out().printf("[dry] %s%n",
                text.builder()
                        .append("internal", NutsTextNodeStyle.pale())
                        .append(" ")
                        .append(getName(),NutsTextNodeStyle.primary(5))
                        .append(" ")
                        .append(getSession().getWorkspace().commandLine().create(args))
                );
    }

}
