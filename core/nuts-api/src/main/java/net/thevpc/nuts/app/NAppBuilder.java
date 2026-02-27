package net.thevpc.nuts.app;

import net.thevpc.nuts.*;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.boot.NBootArguments;
import net.thevpc.nuts.boot.internal.cmdline.NBootCmdLine;
import net.thevpc.nuts.command.NExecutionException;
import net.thevpc.nuts.text.NI18n;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.time.NClock;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NExceptions;
import net.thevpc.nuts.util.NUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Builder used to configure and execute a Nuts {@link NApplication}.
 *
 * <p>This class supports:
 * <ul>
 *     <li>Automatic discovery of the application class (from {@code main()} when applicable)</li>
 *     <li>Explicit instance injection via {@link #instance(Object)}</li>
 *     <li>Reflective construction via {@link #type(Class)}</li>
 *     <li>Control of error-handling mode (handle, propagate, exit, ignore)</li>
 *     <li>Providing both Nuts arguments and application arguments</li>
 * </ul>
 *
 * <h2>Basic Usage</h2>
 * <p>
 * A typical JVM application may delegate to Nuts as follows:
 *
 * <pre>{@code
 * public static void main(String[] args) {
 *     NApp.builder(args).run();
 * }
 * }</pre>
 *
 * <h2>Advanced Custom Instance Construction</h2>
 * <p>
 * If you want the builder to instantiate your application class reflectively:
 *
 * <pre>{@code
 * NApp.builder(args)
 *     .type(MyApplication.class)
 *     .handleErrors()
 *     .run();
 * }</pre>
 *
 * <h2>Error Handling</h2>
 * <p>
 * The handling strategy is selected via:
 * <ul>
 *   <li>{@link #handleErrors()}      – default Nuts behavior</li>
 *   <li>{@link #propagateErrors()}   – rethrow exceptions to caller</li>
 *   <li>{@link #fatalErrors()}       – exit the JVM on error</li>
 *   <li>{@link #ignoreErrors()}      – ignore exceptions</li>
 * </ul>
 * <p>
 * This builder orchestrates argument parsing, instance preparation, execution mode
 * dispatching, and high-level runtime behavior for Nuts applications.
 */
public class NAppBuilder {

    /**
     * Defines how runtime errors should be handled.
     */
    private NApplicationHandleMode handleMode = NApplicationHandleMode.HANDLE;

    /**
     * The application instance to run. May be created reflectively.
     */
    private Object instance;

    /**
     * Arguments passed to the Nuts workspace / bootstrap.
     */
    private String[] nutsArgs;

    /**
     * Arguments passed to the application itself.
     */
    private String[] args;

    private NWorkspace preparedWorkspace;

    /**
     * Creates a new empty builder.
     */
    public static NAppBuilder of() {
        return new NAppBuilder();
    }

    /**
     * Creates a new builder and sets plain arguments.
     */
    public static NAppBuilder of(String[] args) {
        return new NAppBuilder().args(args);
    }

    /**
     * Errors are handled by Nuts (default behavior).
     */
    public NAppBuilder handleErrors() {
        this.handleMode = NApplicationHandleMode.HANDLE;
        return this;
    }

    /**
     * Errors are propagated to the caller.
     */
    public NAppBuilder propagateErrors() {
        this.handleMode = NApplicationHandleMode.PROPAGATE;
        return this;
    }

    /**
     * Errors are considered fatal and cause process exit.
     */
    public NAppBuilder fatalErrors() {
        this.handleMode = NApplicationHandleMode.EXIT;
        return this;
    }

    /**
     * Errors are ignored (no operation).
     */
    public NAppBuilder ignoreErrors() {
        this.handleMode = NApplicationHandleMode.NOP;
        return this;
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

    /**
     * Sets the application instance explicitly.
     */
    public NAppBuilder instance(Object applicationInstance) {
        this.instance = applicationInstance;
        return this;
    }

    /**
     * Creates an application instance by calling a no-argument constructor.
     * Errors are wrapped in RuntimeExceptions for simplicity.
     */
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

    /**
     * Creates and stores an instance from a class type.
     */
    public NAppBuilder type(Class applicationType) {
        this.instance = applicationType == null ? null : createInstance(applicationType);
        return this;
    }

    public String[] getNutsArgs() {
        return nutsArgs;
    }

    /**
     * Sets Nuts bootstrap/WS args explicitly.
     */
    public NAppBuilder setNutsArgs(String... nutsArgs) {
        this.nutsArgs = nutsArgs;
        return this;
    }

    /**
     * Parses a Nuts argument line into structured args.
     */
    public NAppBuilder setNutsArgsLine(String nutsArgs) {
        this.nutsArgs = NBootCmdLine.parseDefault(nutsArgs);
        return this;
    }

    /**
     * Parses a Nuts argument line and merges it with additional arguments.
     * Note: Logic seems incorrect—currently re-adds the parsed items instead
     * of merging `extraArgs`. Might be a bug.
     */
    public NAppBuilder setNutsArgsLine(String nutsArgs, String[] extraArgs) {
        List<String> all = new ArrayList<>();
        all.addAll(Arrays.asList(NBootCmdLine.parseDefault(nutsArgs)));
        if (extraArgs != null) {
            for (String s : extraArgs) {
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

    /**
     * Sets plain application arguments.
     */
    public NAppBuilder args(String[] args) {
        this.args = args;
        return this;
    }

    public NAppBuilder prepare() {
        if (this.preparedWorkspace == null) {
            try {
                NClock now = NClock.now();
                NWorkspace ws = NWorkspace.get().orNull();
                if (ws == null) {
                    ws = Nuts.openWorkspace(NBootArguments.of(this.getNutsArgs()).setAppArgs(args));
                }
                ws.runWith(() -> {
                    NApp a = NApp.of();
                    a.prepare(new NAppInitInfo(args, null, null, null, null, now));
                });
                this.preparedWorkspace = ws;
            } catch (Exception e) {
                throw NExceptions.ofUncheckedException(e);
            }
        }
        return this;
    }

    /**
     * Entry point that resolves the app instance, initializes the application,
     * applies error-handling strategy, and executes the application lifecycle.
     */
    public void run() {
        NApplicationHandleMode.runHandled(this::prepare, getHandleMode());
        if(preparedWorkspace!=null) {
            preparedWorkspace.runApplication(this.getHandleMode());
        }
    }


}
