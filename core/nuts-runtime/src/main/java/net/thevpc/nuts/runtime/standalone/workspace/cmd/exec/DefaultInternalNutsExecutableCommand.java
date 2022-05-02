/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.runtime.standalone.app.util.NutsAppUtils;
import net.thevpc.nuts.text.NutsText;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;

/**
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
        NutsTexts txt = NutsTexts.of(getSession());
        NutsText n = txt.parser().parseResource("/net/thevpc/nuts/runtime/command/" + name + ".ntf",
                txt.parser().createLoader(getClass().getClassLoader())
        );
        if (n == null) {
            return "no help found for " + name;
        }
        return n.toString();
    }

    @Override
    public void dryExecute() {
        NutsSession session = getSession();
        if (NutsAppUtils.processHelpOptions(args, getSession())) {
            if (getSession().isPlainOut()) {
                session.out().println("[dry] ==show-help==");
            } else {
                session.out().printlnf("[dry] ==show-help==");
            }
            return;
        }
        NutsTexts text = NutsTexts.of(session);
        if (getSession().isPlainOut()) {
            session.out().printlnf("[dry] %s%n",
                    text.builder()
                            .append("internal", NutsTextStyle.pale())
                            .append(" ")
                            .append(getName(), NutsTextStyle.primary5())
                            .append(" ")
                            .append(NutsCommandLine.of(args))
            );
        } else {
            session.out().printlnf(NutsMessage.cstyle(
                            "[dry] %s",
                            text.builder()
                                    .append("internal", NutsTextStyle.pale())
                                    .append(" ")
                                    .append(getName(), NutsTextStyle.primary5())
                                    .append(" ")
                                    .append(NutsCommandLine.of(args))
                    )
            );
        }
    }

    @Override
    public String toString() {
        return getName() + " " + NutsCommandLine.of(args).toString();
    }

}
