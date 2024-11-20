/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.internal;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.app.util.NAppUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.AbstractNExecutableInformationExt;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTextTransformConfig;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NMsg;

/**
 * @author thevpc
 */
public abstract class DefaultInternalNExecutableCommand extends AbstractNExecutableInformationExt {

    protected String[] args;
    public DefaultInternalNExecutableCommand(NWorkspace workspace,String name, String[] args, NExecCmd execCommand) {
        super(workspace,name, name, NExecutableType.INTERNAL,execCommand);
        this.args = args;
    }

    @Override
    public NId getId() {
        return null;
    }

    protected void showDefaultHelp() {
        NSession session = workspace.currentSession();
        session.out().println(getHelpText());
    }


    @Override
    public NText getHelpText() {
        NSession session = workspace.currentSession();
        NTexts txt = NTexts.of();
        NPath path = NPath.of("classpath://net/thevpc/nuts/runtime/command/" + name + ".ntf", getClass().getClassLoader());
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
        NSession session = workspace.currentSession();
        if (NAppUtils.processHelpOptions(args, session)) {
            session.out().println("[dry] ==show-help==");
            return;
        }
        NTexts text = NTexts.of();
        if (session.isPlainOut()) {
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
