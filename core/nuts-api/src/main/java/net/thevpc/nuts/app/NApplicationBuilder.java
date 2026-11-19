package net.thevpc.nuts.app;

import net.thevpc.nuts.*;
import net.thevpc.nuts.boot.NBootArguments;
import net.thevpc.nuts.boot.internal.cmdline.NBootCmdLine;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.time.NClock;
import net.thevpc.nuts.util.NException;
import net.thevpc.nuts.util.NToStringBuilder;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Configures and launches a Nuts application in a workspace.
 *
 * <p>The builder separates Nuts bootstrap arguments from application arguments and
 * selects how launch failures are handled. {@link #prepare()} opens or reuses a
 * workspace and initializes its {@link NApplication}; {@link #run()} then dispatches
 * the application lifecycle in that workspace. Repeated calls to {@code prepare()}
 * reuse the workspace prepared by this builder.</p>
 */
public class NApplicationBuilder {

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
    public static NApplicationBuilder of() {
        return new NApplicationBuilder();
    }

    /**
     * Creates a new builder and sets plain arguments.
     */
    public static NApplicationBuilder of(String[] args) {
        return new NApplicationBuilder().args(args);
    }

    /**
     * Errors are handled by Nuts (default behavior).
     */
    public NApplicationBuilder handleErrors() {
        this.handleMode = NApplicationHandleMode.HANDLE;
        return this;
    }

    /**
     * Errors are propagated to the caller.
     */
    public NApplicationBuilder propagateErrors() {
        this.handleMode = NApplicationHandleMode.PROPAGATE;
        return this;
    }

    /**
     * Errors are considered fatal and cause process exit.
     */
    public NApplicationBuilder fatalErrors() {
        this.handleMode = NApplicationHandleMode.EXIT;
        return this;
    }

    /**
     * Errors are ignored (no operation).
     */
    public NApplicationBuilder ignoreErrors() {
        this.handleMode = NApplicationHandleMode.NOP;
        return this;
    }

    /**
     * Handle mode.
     *
     * @return handle mode result
     */
    public NApplicationHandleMode handleMode() {
        return handleMode;
    }

    /**
     * Handle mode.
     *
     * @param mode mode
     * @return handle mode result
     */
    public NApplicationBuilder handleMode(NApplicationHandleMode mode) {
        this.handleMode = mode;
        return this;
    }

    /**
     * Instance.
     *
     * @return instance result
     */
    public Object instance() {
        return instance;
    }

    /**
     * Sets the application instance explicitly.
     */
    public NApplicationBuilder instance(Object applicationInstance) {
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
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw NException.ofUncheckedException(e);
        }
    }

    /**
     * Creates and stores an instance from a class type.
     */
    public NApplicationBuilder type(Class applicationType) {
        this.instance = applicationType == null ? null : createInstance(applicationType);
        return this;
    }

    /**
     * Nuts args.
     *
     * @return nuts args result
     */
    public String[] nutsArgs() {
        return nutsArgs;
    }

    /**
     * Sets Nuts bootstrap/WS args explicitly.
     */
    public NApplicationBuilder nutsArgs(String... nutsArgs) {
        this.nutsArgs = nutsArgs;
        return this;
    }

    /**
     * Parses a Nuts argument line into structured args.
     */
    public NApplicationBuilder nutsArgsLine(String nutsArgs) {
        this.nutsArgs = NBootCmdLine.parseDefault(nutsArgs);
        return this;
    }

    /**
     * Parses a Nuts argument line and merges it with additional arguments.
     * Note: Logic seems incorrect—currently re-adds the parsed items instead
     * of merging `extraArgs`. Might be a bug.
     */
    public NApplicationBuilder nutsArgsLine(String nutsArgs, String[] extraArgs) {
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

    /**
     * Args.
     *
     * @return args result
     */
    public String[] args() {
        return args;
    }

    /**
     * Sets plain application arguments.
     */
    public NApplicationBuilder args(String[] args) {
        this.args = args;
        return this;
    }

    /**
     * Prepare.
     *
     * @return prepare result
     */
    public NApplicationBuilder prepare() {
        if (this.preparedWorkspace == null) {
            try {
                NClock now = NClock.now();
                NWorkspace ws = NWorkspace.get().orNull();
                if (ws == null) {
                    ws = Nuts.openWorkspace(NBootArguments.of(this.nutsArgs()).appArgs(args));
                }
                ws.runWith(() -> {
                    NApplication a = NApplication.of();
                    a.prepare(new NAppInitInfo(args, null, null, null, null, now));
                });
                this.preparedWorkspace = ws;
            } catch (Exception e) {
                throw NException.ofUncheckedException(e);
            }
        }
        return this;
    }

    /**
     * Entry point that resolves the app instance, initializes the application,
     * applies error-handling strategy, and executes the application lifecycle.
     */
    public void run() {
        NApplicationHandleMode mode = handleMode() == null ? NApplicationHandleMode.HANDLE : handleMode();
        if (mode != NApplicationHandleMode.HANDLE) {
            NApplicationHandleMode.runHandled(this::prepare, mode);
            if (preparedWorkspace != null) {
                preparedWorkspace.runApplication(mode);
            }
            return;
        }
        // HANDLE (default) : show the error but keep its exit code, which used to be lost
        // (the error was printed and run() returned normally, so every app exited with 0).
        // When no workspace is in scope, this application owns the JVM : exit with the code.
        // Otherwise it runs in-process inside a host workspace (embedded execution, which
        // calls main() by reflection) : rethrow so that the host executor gets the code
        // (exiting there would kill the host).
        boolean ownsJvm = !NWorkspace.get().isPresent();
        try {
            prepare();
            if (preparedWorkspace != null) {
                preparedWorkspace.runApplication(NApplicationHandleMode.NOP);
            }
        } catch (RuntimeException e) {
            int code = net.thevpc.nuts.util.NExceptionHandler.of(e).handle().code();
            if (code == 0) {
                return;
            }
            if (ownsJvm) {
                System.exit(code);
            }
            if (NException.resolveWithExitCodeExceptionBase(e).isPresent()) {
                throw e;
            }
            throw new net.thevpc.nuts.command.NExecutionException(net.thevpc.nuts.text.NMsg.ofC("%s", e), e, code);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NApplicationBuilder that = (NApplicationBuilder) o;
        return handleMode == that.handleMode && Objects.equals(instance, that.instance) && Objects.deepEquals(nutsArgs, that.nutsArgs) && Objects.deepEquals(args, that.args) && Objects.equals(preparedWorkspace, that.preparedWorkspace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(handleMode, instance, Arrays.hashCode(nutsArgs), Arrays.hashCode(args), preparedWorkspace);
    }

    @Override
    public String toString() {
        return NToStringBuilder.of(this).omitBlanks(true)
                .add("handleMode", handleMode)
                .add("instance", instance)
                .add("nutsArgs", nutsArgs)
                .add("args", args)
                .add("preparedWorkspace", preparedWorkspace)
                .build();
    }
}
