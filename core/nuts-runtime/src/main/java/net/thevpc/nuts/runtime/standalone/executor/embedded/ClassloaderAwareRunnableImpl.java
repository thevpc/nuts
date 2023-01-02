package net.thevpc.nuts.runtime.standalone.executor.embedded;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.executor.java.JavaExecutorComponent;
import net.thevpc.nuts.runtime.standalone.executor.java.JavaExecutorOptions;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

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
    public Object runWithContext() throws Throwable {
        if (cls.getName().equals("net.thevpc.nuts.Nuts")) {
            NWorkspaceOptionsBuilder o = NWorkspaceOptionsBuilder.of().setCommandLine(
                    joptions.getAppArgs().toArray(new String[0]),session
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
                NExecCommand.of(session)
                        .addCommand(appArgs)
                        .addExecutorOptions(o.getExecutorOptions().orNull())
                        .setExecutionType(o.getExecutionType().orNull())
                        .setFailFast(true)
                        .run();
            } finally {
                NApplications.getSharedMap().put("nuts.embedded.application.id", oldId);
            }
            return null;
        }
        Method mainMethod = null;
        String nutsAppVersion = null;
        Object nutsApp = null;
        NSession sessionCopy = getSession().copy();
        try {
            nutsAppVersion = CoreNApplications.getNutsAppVersion(cls);
            if (nutsAppVersion != null) {
                mainMethod = cls.getMethod("run", NSession.class, String[].class);
                mainMethod.setAccessible(true);
                nutsApp= CoreNApplications.createApplicationInstance(cls,session,joptions.getAppArgs().toArray(new String[0]));
            }
        } catch (Exception rr) {
            //ignore
        }
        if (nutsAppVersion != null && nutsApp!=null) {
            //NutsWorkspace
            NApplications.getSharedMap().put("nuts.embedded.application.id", id);
            mainMethod.invoke(nutsApp, sessionCopy, joptions.getAppArgs().toArray(new String[0]));
        } else {
            //NutsWorkspace

            NWorkspaceOptionsBuilder bootOptions = JavaExecutorComponent.createChildOptions(executionContext);
            System.setProperty("nuts.boot.args",
                    bootOptions
                            .toCommandLine(new NWorkspaceOptionsConfig().setCompact(true))
                            .add(id.getLongName())
                            .formatter(session).setShellFamily(NShellFamily.SH).toString()
            );
            mainMethod = cls.getMethod("main", String[].class);
            mainMethod.invoke(null, new Object[]{joptions.getAppArgs().toArray(new String[0])});
        }
        return null;
    }

}
