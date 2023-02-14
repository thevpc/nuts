/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.app.util.NAppUtils;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTextTransformConfig;
import net.thevpc.nuts.text.NTexts;

/**
 * @author thevpc
 */
public abstract class DefaultInternalNExecutableCommand extends AbstractNExecutableCommand {

    protected String[] args;
    private NSession session;

    public DefaultInternalNExecutableCommand(String name, String[] args, NSession session) {
        super(name, name, NExecutableType.INTERNAL);
        this.args = args;
        this.session = session;
    }

    public NSession getSession() {
        return session;
    }

    @Override
    public NId getId() {
        return null;
    }

    protected void showDefaultHelp() {
        session.out().println(getHelpText());
    }


    @Override
    public NText getHelpText() {
        NTexts txt = NTexts.of(getSession());
        NPath path = NPath.of("classpath://net/thevpc/nuts/runtime/command/" + name + ".ntf", getClass().getClassLoader(), session);
        NText n = txt.parser().parse(path);
        if (n == null) {
            return super.getHelpText();
        }
        return txt.transform(n,
                new NTextTransformConfig()
                        .setProcessAll(true)
                        .setRootLevel(1)
                        .setImportClassLoader(getClass().getClassLoader())
                        .setCurrentDir(path.getParent())
        );
    }


    public void dryExecute() {
        NSession session = getSession();
        if (NAppUtils.processHelpOptions(args, getSession())) {
            session.out().println("[dry] ==show-help==");
            return;
        }
        NTexts text = NTexts.of(session);
        if (getSession().isPlainOut()) {
            session.out().println(NMsg.ofC("[dry] %s%n",
                    text.ofBuilder()
                            .append("internal", NTextStyle.pale())
                            .append(" ")
                            .append(getName(), NTextStyle.primary5())
                            .append(" ")
                            .append(NCmdLine.of(args))
            ));
        } else {
            session.out().println(NMsg.ofC(
                            "[dry] %s",
                            text.ofBuilder()
                                    .append("internal", NTextStyle.pale())
                                    .append(" ")
                                    .append(getName(), NTextStyle.primary5())
                                    .append(" ")
                                    .append(NCmdLine.of(args))
                    )
            );
        }
    }

    @Override
    public String toString() {
        return getName() + " " + NCmdLine.of(args).toString();
    }

}
