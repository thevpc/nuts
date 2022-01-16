package net.thevpc.nuts.runtime.standalone.executor.java;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.executor.AbstractSyncIProcessExecHelper;
import net.thevpc.nuts.runtime.standalone.executor.system.ProcessExecHelper;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;

class JavaProcessExecHelper extends AbstractSyncIProcessExecHelper {

    private final NutsSession execSession;
    private final List<NutsString> xargs;
    private final JavaExecutorOptions joptions;
    private final NutsSession ws;
    private final NutsExecutionContext executionContext;
    private final NutsDefinition def;
    private final List<String> args;
    private final HashMap<String, String> osEnv;

    public JavaProcessExecHelper(NutsSession ns, NutsSession execSession, List<NutsString> xargs, JavaExecutorOptions joptions, NutsSession ws, NutsExecutionContext executionContext, NutsDefinition def, List<String> args, HashMap<String, String> osEnv) {
        super(ns);
        this.execSession = execSession;
        this.xargs = xargs;
        this.joptions = joptions;
        this.ws = ws;
        this.executionContext = executionContext;
        this.def = def;
        this.args = args;
        this.osEnv = osEnv;
    }

    @Override
    public void dryExec() {
        NutsPrintStream out = execSession.out();
        out.println("[dry] ==[nuts-exec]== ");
        for (int i = 0; i < xargs.size(); i++) {
            NutsString xarg = xargs.get(i);
//                if (i > 0 && xargs.get(i - 1).equals("--nuts-path")) {
//                    for (String s : xarg.split(";")) {
//                        out.println("\t\t\t " + s);
//                    }
//                } else {
            out.println("\t\t " + xarg);
//                }
        }
        String directory = NutsBlankable.isBlank(joptions.getDir()) ? null : NutsPath.of(joptions.getDir(), ws)
                .toAbsolute().toString();
        ProcessExecHelper.ofDefinition(def,
                args.toArray(new String[0]), osEnv, directory, executionContext.getExecutorProperties(), joptions.isShowCommand(), true, executionContext.getSleepMillis(), executionContext.isInheritSystemIO(), false, NutsBlankable.isBlank(executionContext.getRedirectOutputFile()) ? null : new File(executionContext.getRedirectOutputFile()), NutsBlankable.isBlank(executionContext.getRedirectInputFile()) ? null : new File(executionContext.getRedirectInputFile()), executionContext.getRunAs(), executionContext.getSession(),
                execSession
        ).dryExec();
    }

    @Override
    public int exec() {
        return preExec().exec();
    }

    private ProcessExecHelper preExec() {
        if (joptions.isShowCommand() || getSession().boot().getBootCustomBoolArgument(false, false, false, "---show-command")) {
            NutsPrintStream out = execSession.out();
            out.printf("%s %n", NutsTexts.of(ws).ofStyled("nuts-exec", NutsTextStyle.primary1()));
            for (int i = 0; i < xargs.size(); i++) {
                NutsString xarg = xargs.get(i);
//                    if (i > 0 && xargs.get(i - 1).equals("--nuts-path")) {
//                        for (String s : xarg.split(";")) {
//                            out.println("\t\t\t " + s);
//                        }
//                    } else {
                out.println("\t\t " + xarg);
//                    }
            }
        }
        String directory = NutsBlankable.isBlank(joptions.getDir()) ? null : NutsPath.of(joptions.getDir(), ws)
                .toAbsolute().toString();
        return ProcessExecHelper.ofDefinition(def,
                args.toArray(new String[0]), osEnv, directory, executionContext.getExecutorProperties(), joptions.isShowCommand(), true, executionContext.getSleepMillis(), executionContext.isInheritSystemIO(), false, NutsBlankable.isBlank(executionContext.getRedirectOutputFile()) ? null : new File(executionContext.getRedirectOutputFile()), NutsBlankable.isBlank(executionContext.getRedirectInputFile()) ? null : new File(executionContext.getRedirectInputFile()), executionContext.getRunAs(), executionContext.getSession(),
                execSession
        );
    }

    @Override
    public Future<Integer> execAsync() {
        return preExec().execAsync();
    }
}
