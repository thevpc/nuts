package net.thevpc.nuts.runtime.standalone.executor.embedded;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.executor.embedded.ClassloaderAwareRunnable;
import net.thevpc.nuts.runtime.standalone.executor.java.JavaExecutorOptions;

import java.lang.reflect.Method;

public class ClassloaderAwareRunnableImpl extends ClassloaderAwareRunnable {

    private final Class<?> cls;
    private final JavaExecutorOptions joptions;
    private final NutsId id;

    public ClassloaderAwareRunnableImpl(NutsId id, ClassLoader classLoader, Class<?> cls, NutsSession session, JavaExecutorOptions joptions) {
        super(session.copy(), classLoader);
        this.id = id;
        this.cls = cls;
        this.joptions = joptions;
    }

    @Override
    public Object runWithContext() throws Throwable {
        if (cls.getName().equals("net.thevpc.nuts.Nuts")) {
            NutsWorkspaceOptionsBuilder o = NutsWorkspaceOptionsBuilder.of(session).parseArguments(
                    joptions.getAppArgs().toArray(new String[0])
            );
            String[] appArgs;
            if (o.getApplicationArguments().length == 0) {
                if (o.isSkipWelcome()) {
                    return null;
                }
                appArgs = new String[]{"welcome"};
            } else {
                appArgs = o.getApplicationArguments();
            }
            session.configure(o.build());
            Object oldId = NutsApplications.getSharedMap().get("nuts.embedded.application.id");
            NutsApplications.getSharedMap().put("nuts.embedded.application.id", id);
            try {
                session.exec()
                        .addCommand(appArgs)
                        .addExecutorOptions(o.getExecutorOptions())
                        .setExecutionType(o.getExecutionType())
                        .setFailFast(true)
                        .setDry(session.isDry())
                        .run();
            } finally {
                NutsApplications.getSharedMap().put("nuts.embedded.application.id", oldId);
            }
            return null;
        }
        Method mainMethod = null;
        boolean isNutsApp = false;
        Object nutsApp = null;
        try {
            mainMethod = cls.getMethod("run", NutsSession.class, String[].class);
            mainMethod.setAccessible(true);
            for (Class<?> pi : cls.getInterfaces()) {
                //this is the old nuts apps (version >= 0.8.1)
                if (pi.getName().equals("net.thevpc.nuts.NutsApplication")) {
                    isNutsApp = true;
                    break;
                }
            }
            Class p = cls.getSuperclass();
            while (!isNutsApp && p != null) {
                if (p.getName().equals("net.thevpc.nuts.NutsApplication")
                        //this is the old nuts apps (version < 0.8.0)
                        || p.getName().equals("net.vpc.app.nuts.NutsApplication")) {
                    isNutsApp = true;
                    break;
                }
                for (Class<?> pi : cls.getInterfaces()) {
                    //this is the old nuts apps (version >= 0.8.1)
                    if (pi.getName().equals("net.thevpc.nuts.NutsApplication")) {
                        isNutsApp = true;
                        break;
                    }
                }
                p = p.getSuperclass();
            }
            if (isNutsApp) {
                isNutsApp = false;
                nutsApp = cls.getConstructor().newInstance();
                isNutsApp = true;
            }
        } catch (Exception rr) {
            //ignore

        }
        if (isNutsApp) {
            //NutsWorkspace
            NutsApplications.getSharedMap().put("nuts.embedded.application.id", id);
            mainMethod.invoke(nutsApp, getSession().copy(), joptions.getAppArgs().toArray(new String[0]));
        } else {
            //NutsWorkspace
            System.setProperty("nuts.boot.args",
                    getSession().boot().getBootOptions()
                            .formatter().setExported(true).setCompact(true).getBootCommandLine()
                            .formatter().setShellFamily(NutsShellFamily.SH).toString()
            );
            mainMethod = cls.getMethod("main", String[].class);
//                List<String> nargs = new ArrayList<>();
//                nargs.addAll(joptions.getApp());
            mainMethod.invoke(null, new Object[]{joptions.getAppArgs().toArray(new String[0])});
        }
        return null;
    }

}
