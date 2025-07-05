package net.thevpc.nuts;

import net.thevpc.nuts.boot.NBootArguments;
import net.thevpc.nuts.boot.reserved.cmdline.NBootCmdLine;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.time.NClock;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NMsg;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class NAppBuilder {
    private NApplicationHandleMode handleMode = NApplicationHandleMode.HANDLE;
    private Object instance;
    private String[] nutsArgs;
    private String[] args;

    public static NAppBuilder of() {
        return new NAppBuilder();
    }

    public static NAppBuilder of(String[] args) {
        return new NAppBuilder().args(args);
    }

    public NAppBuilder handleErrors() {
        this.handleMode = NApplicationHandleMode.HANDLE;
        return this;
    }

    public NAppBuilder propagateErrors() {
        this.handleMode = NApplicationHandleMode.PROPAGATE;
        return this;
    }

    public NAppBuilder fatalErrors() {
        this.handleMode = NApplicationHandleMode.EXIT;
        return this;
    }

    public NAppBuilder ignoreErrors() {
        this.handleMode = NApplicationHandleMode.NOP;
        return this;
    }

    private void runInstance(NApplication applicationInstance) {
        boolean inherited = NWorkspace.of().getBootOptions().getInherited().orElse(false);
        NApp nApp = NApp.of();
        String appClassName = nApp.getAppClass() == null ? null : nApp.getAppClass().getName();
        if (appClassName == null) {
            appClassName = applicationInstance.getClass().getName();
        }
        NId appId = nApp.getId().orNull();
        NLog.of(NApplications.class).with().level(Level.FINE).verb(NLogVerb.START)
                .log(
                        NMsg.ofC(
                                NI18n.of("running application %s: %s (%s) %s"),
                                inherited ? ("(" + NI18n.of("inherited") + ")") : "",
                                appId == null ? ("<" + NI18n.of("unresolved-id") + ">") : appId,
                                appClassName,
                                nApp.getCmdLine()
                        )
                );
        try {
            switch (nApp.getMode()) {
                //both RUN and AUTO_COMPLETE execute the run branch. Later
                //session.isExecMode()
                case RUN:
                case AUTO_COMPLETE: {
                    applicationInstance.run();
                    return;
                }
                case INSTALL: {
                    applicationInstance.onInstallApplication();
                    return;
                }
                case UPDATE: {
                    applicationInstance.onUpdateApplication();
                    return;
                }
                case UNINSTALL: {
                    applicationInstance.onUninstallApplication();
                    return;
                }
            }
        } catch (NExecutionException e) {
            if (e.getExitCode() == NExecutionException.SUCCESS) {
                return;
            }
            throw e;
        }
        throw new NExecutionException(NMsg.ofC(NI18n.of("unsupported execution mode %s"), nApp.getMode()), NExecutionException.ERROR_255);
    }

    public NApplicationHandleMode getHandleMode() {
        return handleMode;
    }

    public NAppBuilder setHandleMode(NApplicationHandleMode mode) {
        this.handleMode = mode;
        return this;
    }

    public Object getInstance() {
        return instance;
    }

    public NAppBuilder instance(Object applicationInstance) {
        this.instance = applicationInstance;
        return this;
    }

    private Object createInstance(Class applicationType) {
        try {
            return applicationType == null ? null : applicationType.getConstructor().newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public NAppBuilder type(Class applicationType) {
        this.instance = applicationType == null ? null : createInstance(applicationType);
        return this;
    }

    public String[] getNutsArgs() {
        return nutsArgs;
    }

    public NAppBuilder setNutsArgs(String... nutsArgs) {
        this.nutsArgs = nutsArgs;
        return this;
    }

    public NAppBuilder setNutsArgsLine(String nutsArgs) {
        this.nutsArgs = NBootCmdLine.parseDefault(nutsArgs);
        return this;
    }

    public NAppBuilder setNutsArgsLine(String nutsArgs, String[] extraArgs) {
        List<String> all = new ArrayList<>();
        all.addAll(Arrays.asList(NBootCmdLine.parseDefault(nutsArgs)));
        if (extraArgs != null) {
            for (String s : all) {
                if (s != null) {
                    all.add(s);
                }
            }
        }
        this.nutsArgs = all.toArray(new String[0]);
        return this;
    }

    public String[] getArgs() {
        return args;
    }

    public NAppBuilder args(String[] args) {
        this.args = args;
        return this;
    }

    public void run() {
        NApplicationHandleMode m = this.getHandleMode() == null ? NApplicationHandleMode.HANDLE : getHandleMode();
        try {
            NClock now = NClock.now();
            String[] args = this.getArgs() == null ? new String[0] : this.getArgs();
            Object applicationInstanceObj = this.getInstance();
            if (applicationInstanceObj == null) {
                StackTraceElement[] stackTrace = new Throwable().getStackTrace();
                if (stackTrace.length >= 1 && Objects.equals(stackTrace[1].getMethodName(), "main")) {
                    Class c = Class.forName(stackTrace[1].getClassName(), true, Thread.currentThread().getContextClassLoader());
                    Method main = c.getDeclaredMethod("main", String[].class);
                    if (Modifier.isStatic(main.getModifiers())) {
                        // i am in good position to say this is the app instance type
                        applicationInstanceObj = createInstance(c);
                    }
                }
            }
            NAssert.requireNonNull(applicationInstanceObj, "applicationInstance");

            NApplication applicationInstance = (applicationInstanceObj instanceof NApplication) ? (NApplication) applicationInstanceObj : NApplications.createApplicationInstanceFromAnnotatedInstance(applicationInstanceObj);
            Class appClass =
                    (applicationInstance instanceof NApplications.AnnotationClassNApplication) ?
                            ((NApplications.AnnotationClassNApplication) applicationInstance).getAppInstance().getClass()
                            : applicationInstance.getClass();
            NWorkspace ws = NWorkspace.get().orNull();
            if (ws == null) {
                ws = Nuts.openWorkspace(NBootArguments.of(this.getNutsArgs()).setAppArgs(args));
            }
            ws.runWith(() -> {
                NApp a = NApp.of();
                a.setArguments(args);
                a.prepare(new NAppInitInfo(args, appClass, now));
                runInstance(applicationInstance);
            });
            switch (m) {
                case EXIT: {
                    System.exit(0);
                    break;
                }
            }
        } catch (Exception e) {
            switch (m) {
                case PROPAGATE: {
                    NExceptionHandler.of(e).propagate();
                    break;
                }
                case EXIT: {
                    NExceptionHandler.of(e).handleFatal();
                    break;
                }
                case HANDLE: {
                    NExceptionHandler.of(e).handle();
                    break;
                }
                default: {
                    if (e instanceof RuntimeException) {
                        throw (RuntimeException) e;
                    }
                    throw new RuntimeException(e);
                }
            }
        }
    }


}
