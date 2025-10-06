package net.thevpc.nuts.runtime.standalone.executor.embedded;

import net.thevpc.nuts.app.NApp;
import net.thevpc.nuts.app.NAppInitInfo;
import net.thevpc.nuts.app.NApplications;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.command.NExecCmd;
import net.thevpc.nuts.command.NExecutionContext;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NWorkspaceOptionsBuilder;
import net.thevpc.nuts.core.NWorkspaceOptionsConfig;
import net.thevpc.nuts.text.NCmdLineFormat;
import net.thevpc.nuts.platform.NShellFamily;
import net.thevpc.nuts.runtime.standalone.executor.java.JavaExecutorComponent;
import net.thevpc.nuts.runtime.standalone.executor.java.JavaExecutorOptions;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.time.NClock;

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
        Map<String, String> newEnv = NWorkspaceExt.of().getConfigModel().appendEnv(executionContext.getEnv());
        return NWorkspaceExt.of().getConfigModel().currentEnv.callWith(newEnv, () -> {
            NClock now = NClock.now();
            if (cls.getName().equals("net.thevpc.nuts.Nuts")) {
                NWorkspaceOptionsBuilder o = NWorkspaceOptionsBuilder.of().setCmdLine(
                        joptions.getAppArgs().toArray(new String[0])
                );
                List<String> appArgs;
                if (o.getApplicationArguments().get().size() == 0) {
                    if (o.getSkipWelcome().orElse(false)) {
                        return null;
                    }
                    appArgs = Arrays.asList(new String[]{"welcome"});
                } else {
                    appArgs = o.getApplicationArguments().get();
                }
                session.configure(o.build());
                Object oldId = NApplications.getSharedMap().get("nuts.embedded.application.id");
                NApplications.getSharedMap().put("nuts.embedded.application.id", id);
                try {
                    NExecCmd.of()
                            .addCommand(appArgs)
                            .addExecutorOptions(o.getExecutorOptions().orNull())
                            .setExecutionType(o.getExecutionType().orNull())
                            .failFast()
                            .run();
                } finally {
                    NApplications.getSharedMap().put("nuts.embedded.application.id", oldId);
                }
                return null;
            }
            final Method[] mainMethod = {null};
            String nutsAppVersion = null;
            Object nutsApp = null;
            NSession sessionCopy = getSession().copy();
            try {
                nutsAppVersion = CoreNApplications.getNutsAppVersion(cls);
                if (nutsAppVersion != null) {
                    mainMethod[0] = cls.getMethod("run", NSession.class, String[].class);
                    mainMethod[0].setAccessible(true);
                    nutsApp = CoreNApplications.createApplicationInstance(cls, session, joptions.getAppArgs().toArray(new String[0]));
                }
            } catch (Exception rr) {
                //ignore
            }
            String finalNutsAppVersion = nutsAppVersion;
            Object finalNutsApp = nutsApp;
            return sessionCopy.callWith(() -> {
                NApp.of().prepare(new NAppInitInfo(joptions.getAppArgs().toArray(new String[0]), cls, now));
                try {
                    if (finalNutsAppVersion != null && finalNutsApp != null) {
                        //NutsWorkspace
                        NApplications.getSharedMap().put("nuts.embedded.application.id", id);
                        mainMethod[0].invoke(finalNutsApp, sessionCopy, joptions.getAppArgs().toArray(new String[0]));
                    } else {
                        //NutsWorkspace

                        NWorkspaceOptionsBuilder bootOptions = JavaExecutorComponent.createChildOptions(executionContext);
                        System.setProperty("nuts.boot.args",
                                NCmdLineFormat.ofPlain(
                                        bootOptions
                                                .toCmdLine(new NWorkspaceOptionsConfig().setCompact(true))
                                                .add(id.getLongName())
                                ).setShellFamily(NShellFamily.SH).toString()
                        );
                        mainMethod[0] = cls.getMethod("main", String[].class);
                        mainMethod[0].invoke(null, new Object[]{joptions.getAppArgs().toArray(new String[0])});
                    }
                } catch (Exception e) {
                    throw CoreNUtils.toUncheckedException(e);
                }
                return null;
            });
        });
    }

}
