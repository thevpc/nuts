/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.internal;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.command.NExecutableType;
import net.thevpc.nuts.io.NOut;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.app.util.NAppUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.AbstractNExecutableInformationExt;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTextTransformConfig;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.text.NMsg;

/**
 * @author thevpc
 */
public class DefaultInternalNExecutableCommand extends AbstractNExecutableInformationExt {

    protected String[] args;
    protected NInternalCommand impl;
    public DefaultInternalNExecutableCommand(String name, String[] args, NExec execCommand) {
        super(name, name, NExecutableType.INTERNAL,execCommand);
        this.args = args;
    }
    public DefaultInternalNExecutableCommand(NInternalCommand impl, String[] args, NExec execCommand) {
        super(impl.getName(), impl.getName(), NExecutableType.INTERNAL,execCommand);
        this.args = args;
        this.impl = impl;
    }

    @Override
    public int execute() {
        if(impl==null){
            throw new NIllegalArgumentException(NMsg.ofC("impl is null"));
        }
        return impl.execute(args, getExecCommand());
    }

    @Override
    public NId id() {
        return null;
    }

    protected void showDefaultHelp() {
        NOut.println(helpText());
    }


    @Override
    public NText helpText() {
        NTexts txt = NTexts.of();
        NPath path = NPath.of("classpath://net/thevpc/nuts/runtime/command/" + name + ".ntf", getClass().getClassLoader());
        NText n = txt.parser().parse(path);
        if (n == null) {
            return super.helpText();
        }
        return txt.transform(n,
                new NTextTransformConfig()
                        .processAll(true)
                        .rootLevel(1)
                        .importClassLoader(getClass().getClassLoader())
                        .currentDir(path.parent())
        );
    }


    public void dryExecute() {
        if (NAppUtils.processHelpOptions(args)) {
            NOut.println("[dry] ==show-help==");
            return;
        }
        NTexts text = NTexts.of();
        if (NOut.isPlain()) {
            NOut.println(NMsg.ofC("[dry] %s%n",
                    text.ofBuilder()
                            .append("internal", NTextStyle.pale())
                            .append(" ")
                            .append(name(), NTextStyle.primary5())
                            .append(" ")
                            .append(NCmdLine.of(args))
            ));
        } else {
            NOut.println(NMsg.ofC(
                            "[dry] %s",
                            text.ofBuilder()
                                    .append("internal", NTextStyle.pale())
                                    .append(" ")
                                    .append(name(), NTextStyle.primary5())
                                    .append(" ")
                                    .append(NCmdLine.of(args))
                    )
            );
        }
    }

    @Override
    public String toString() {
        return name() + " " + NCmdLine.of(args).toString();
    }

}
