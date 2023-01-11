package net.thevpc.nuts.runtime.standalone.executor.java;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NOutputStream;
import net.thevpc.nuts.runtime.standalone.executor.AbstractSyncIProcessExecHelper;
import net.thevpc.nuts.runtime.standalone.executor.system.ProcessExecHelper;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.recom.NRecommendationPhase;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.recom.RequestQueryInfo;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;

class JavaProcessExecHelper extends AbstractSyncIProcessExecHelper {

    private final NSession execSession;
    private final List<NString> xargs;
    private final JavaExecutorOptions joptions;
    private final NSession ws;
    private final NExecutionContext executionContext;
    private final NDefinition def;
    private final List<String> args;
    private final HashMap<String, String> osEnv;

    public JavaProcessExecHelper(NSession ns, NSession execSession, List<NString> xargs, JavaExecutorOptions joptions, NSession ws, NExecutionContext executionContext, NDefinition def, List<String> args, HashMap<String, String> osEnv) {
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
    public int exec() {
        if (execSession.isDry()) {
            NOutputStream out = execSession.out();
            out.println("[dry] ==[nuts-exec]== ");
            for (int i = 0; i < xargs.size(); i++) {
                NString xarg = xargs.get(i);
//                if (i > 0 && xargs.get(i - 1).equals("--nuts-path")) {
//                    for (String s : xarg.split(";")) {
//                        out.println("\t\t\t " + s);
//                    }
//                } else {
                out.println("\t\t " + xarg);
//                }
            }
            String directory = NBlankable.isBlank(joptions.getDir()) ? null : NPath.of(joptions.getDir(), ws)
                    .toAbsolute().toString();
            ProcessExecHelper.ofDefinition(def,
                    args.toArray(new String[0]), osEnv, directory, executionContext.getExecutorProperties(), joptions.isShowCommand(), true, executionContext.getSleepMillis(), executionContext.isInheritSystemIO(), false,
                    executionContext.getRedirectOutputFile(),
                    executionContext.getRedirectInputFile(),
                    executionContext.getRunAs(), executionContext.getSession(),
                    execSession
            ).exec();
            return 0;
        }
        return preExec().exec();
    }

    private ProcessExecHelper preExec() {
        if (joptions.isShowCommand() || CoreNUtils.isShowCommand(getSession())) {
            NOutputStream out = execSession.out();
            out.println(NMsg.ofC("%s ", NTexts.of(ws).ofStyled("nuts-exec", NTextStyle.primary1())));
            for (int i = 0; i < xargs.size(); i++) {
                NString xarg = xargs.get(i);
                out.println(NMsg.ofC("\t\t %s", xarg));
            }
        }
        String directory = NBlankable.isBlank(joptions.getDir()) ? null : NPath.of(joptions.getDir(), ws)
                .toAbsolute().toString();
        NWorkspaceExt.of(getSession()).getModel().recomm.getRecommendations(new RequestQueryInfo(def.getId().toString(), ""), NRecommendationPhase.EXEC, false, getSession());
        return ProcessExecHelper.ofDefinition(def,
                args.toArray(new String[0]), osEnv, directory, executionContext.getExecutorProperties(), joptions.isShowCommand(), true, executionContext.getSleepMillis(), executionContext.isInheritSystemIO(), false,
                executionContext.getRedirectOutputFile(),
                executionContext.getRedirectInputFile(),
                executionContext.getRunAs(), executionContext.getSession(),
                execSession
        );
    }

    @Override
    public Future<Integer> execAsync() {
        return preExec().execAsync();
    }
}
