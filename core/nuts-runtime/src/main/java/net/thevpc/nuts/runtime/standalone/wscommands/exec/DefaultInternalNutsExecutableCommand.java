/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands.exec;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.wscommands.exec.AbstractNutsExecutableCommand;

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
        NutsTextManager txt = getSession().text();
        NutsText n = txt.parser().parseResource("/net/thevpc/nuts/runtime/command/" + name + ".ntf",
                txt.parser().createLoader(getClass().getClassLoader())
        );
        if(n==null){
            return "no help found for " + name;
        }
        return n.toString();
    }

    @Override
    public void dryExecute() {
        if (CoreNutsUtils.isIncludesHelpOption(args)) {
            getSession().out().println("[dry] ==show-help==");
            return;
        }
        NutsTextManager text = getSession().text();
        getSession().out().printf("[dry] %s%n",
                text.builder()
                        .append("internal", NutsTextStyle.pale())
                        .append(" ")
                        .append(getName(),NutsTextStyle.primary5())
                        .append(" ")
                        .append(getSession().commandLine().create(args))
                );
    }

    @Override
    public String toString() {
        return getName()+" "+ NutsCommandLine.of(args,getSession()).toString();
    }

}
