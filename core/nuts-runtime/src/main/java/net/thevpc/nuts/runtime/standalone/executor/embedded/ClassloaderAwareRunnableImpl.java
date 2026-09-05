package net.thevpc.nuts.runtime.standalone.executor.embedded;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.command.NExecutionContext;
import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NWorkspaceOptionsBuilder;
import net.thevpc.nuts.core.NWorkspaceOptionsConfig;
import net.thevpc.nuts.platform.NEnv;
import net.thevpc.nuts.runtime.standalone.app.NApplicationImpl;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExtNewContext;
import net.thevpc.nuts.text.NCmdLineWriter;
import net.thevpc.nuts.platform.NShellFamily;
import net.thevpc.nuts.runtime.standalone.executor.java.JavaExecutorComponent;
import net.thevpc.nuts.runtime.standalone.executor.java.JavaExecutorOptions;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.time.NClock;
import net.thevpc.nuts.util.NException;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ClassloaderAwareRunnableImpl extends ClassloaderAwareRunnable {

    private final Class<?> cls;
    private final JavaExecutorOptions joptions;
    private final NId id;
    private final NExecutionContext executionContext;

    public ClassloaderAwareRunnableImpl(NId id, ClassLoader classLoader, Class<?> cls, NSession session, JavaExecutorOptions joptions, NExecutionContext executionContext) {
        super(session.copy(), classLoader);
        this.id = id;
        this.cls = cls;
        this.joptions = joptions;
        this.executionContext = executionContext;
    }

    @Override
    public Object runWithContext() {
        NWorkspaceExt ows = NWorkspaceExt.of();
        Map<String, String> newEnv = ows.getModel().appendEnv(executionContext.env());
        NApplicationImpl newApp = new NApplicationImpl();
        NWorkspaceExtNewContext wsc = new NWorkspaceExtNewContext(ows, newEnv, newApp);
        return wsc.callWith(() -> {
            NClock now = NClock.now();
            if (cls.getName().equals("net.thevpc.nuts.Nuts")) {
                NWorkspaceOptionsBuilder o = NWorkspaceOptionsBuilder.of().setCmdLine(
                        joptions.getAppArgs().toArray(new String[0])
                );
                List<String> appArgs;
                if (o.applicationArguments().get().isEmpty()) {
                    if (o.skipWelcome().orElse(false)) {
                        return null;
                    }
                    appArgs = Arrays.asList("welcome");
                } else {
                    appArgs = o.applicationArguments().get();
                }
                session.configure(o.build());
                NExec.of()
                        .command(appArgs)
                        .executorOptions(o.executorOptions().orNull())
                        .executionType(o.executionType().orNull())
                        .failFast(true)
                        .run();
                return null;
            }
            final Method[] mainMethod = {null};
            NSession sessionCopy = NSession.of().copyFrom(getSession());
            return sessionCopy.callWith(() -> {
                try {
                    NWorkspaceOptionsBuilder bootOptions = JavaExecutorComponent.createChildOptions(executionContext);
                    NEnv.of().env().put(NConstants.Env.NUTS_BOOT_ARGS,
                            NCmdLineWriter.of().shellFamily(NShellFamily.SH).formatPlain(bootOptions
                                    .toCmdLine(new NWorkspaceOptionsConfig().compact(true))
                                    .add(id.longName()))
                    );
                    NEnv.of().env().put(NConstants.Env.NUTS_BOOT_ID, id.longName());
                    mainMethod[0] = cls.getMethod("main", String[].class);
                    mainMethod[0].invoke(null, new Object[]{joptions.getAppArgs().toArray(new String[0])});
//                    }
                } catch (Exception e) {
                    throw NException.ofUncheckedException(e);
                }
                return null;
            });
        });
    }

}
