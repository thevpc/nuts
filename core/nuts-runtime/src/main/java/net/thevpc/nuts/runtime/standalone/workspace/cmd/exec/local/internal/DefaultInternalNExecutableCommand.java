/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.internal;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.boot.NBootCompleteRequest;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.command.NExecutableType;
import net.thevpc.nuts.command.NExecutionException;
import net.thevpc.nuts.internal.rpi.NTextRPI;
import net.thevpc.nuts.io.NOut;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.app.util.NAppUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.AbstractNExecutableInformationExt;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NIllegalArgumentException;

import java.util.List;

/**
 * @author thevpc
 */
public class DefaultInternalNExecutableCommand extends AbstractNExecutableInformationExt {

    protected String[] args;
    protected NInternalCommand impl;
    public DefaultInternalNExecutableCommand(String name, String[] args, NExec execCommand, List<String> executorOptions) {
        super(name, name, NExecutableType.INTERNAL,execCommand);
        this.args = args;
        this.executorOptions = executorOptions;
        NCmdLine.of(this.executorOptions).matcher()
                .with("--show-command").matchFlag(a->this.showCommand = (a.booleanValue()))
                .with("--nuts-exec-mode").matchFlag(a->this.completeRequest = NBootCompleteRequest.parseOrNull(a.stringValue()))
                .withAny().skip()
                .requireAll();
    }
    public DefaultInternalNExecutableCommand(NInternalCommand impl, String[] args, NExec execCommand, List<String> executorOptions) {
        super(impl.getName(), impl.getName(), NExecutableType.INTERNAL,execCommand);
        this.args = args;
        this.impl = impl;
        this.executorOptions = executorOptions;
        NCmdLine.of(this.executorOptions).matcher()
                .with("--show-command").matchFlag(a->this.showCommand = (a.booleanValue()))
                .with("--nuts-exec-mode").matchFlag(a->this.completeRequest = NBootCompleteRequest.parseOrNull(a.stringValue()))
                .withAny().skip()
                .requireAll();
    }

    @Override
    public int execute() {
        if(completeRequest!=null){
            return NExecutionException.SUCCESS;
        }
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
        NPath path = NPath.of("classpath://net/thevpc/nuts/runtime/command/" + name + ".ntf", getClass().getClassLoader());
        NText n = NTextParser.of().parse(path);
        if (n == null) {
            return super.helpText();
        }
        return NText.transform(n,
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
        if (NOut.isPlain()) {
            NOut.println(NMsg.ofC("[dry] %s%n",
                    NTextBuilder.of()
                            .append("internal", NTextStyle.pale())
                            .append(" ")
                            .append(name(), NTextStyle.primary5())
                            .append(" ")
                            .append(NCmdLine.of(args))
            ));
        } else {
            NOut.println(NMsg.ofC(
                            "[dry] %s",
                            NTextBuilder.of()
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
