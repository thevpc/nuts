package net.thevpc.nuts.app;

import net.thevpc.nuts.boot.internal.cmdline.NBootCmdLine;
import net.thevpc.nuts.util.NToStringBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * NMainArgs class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NMainArgs {
    private NApplicationHandleMode handleMode = NApplicationHandleMode.HANDLE;
    private NApplicationHandler applicationInstance;
    private String[] nutsArgs;
    private String[] args;

    /**
     * Creates a new instance of of.
     *
     * @param args args
     * @return of result
     */
    public static NMainArgs of(String[] args) {
        return new NMainArgs().args(args);
    }

    /**
     * Creates a new instance of of handled.
     *
     * @param args args
     * @return of handled result
     */
    public static NMainArgs ofHandled(String[] args) {
        return new NMainArgs().args(args).handleMode(NApplicationHandleMode.HANDLE);
    }

    /**
     * Creates a new instance of of propagated.
     *
     * @param args args
     * @return of propagated result
     */
    public static NMainArgs ofPropagated(String[] args) {
        return new NMainArgs().args(args).handleMode(NApplicationHandleMode.PROPAGATE);
    }

    /**
     * Creates a new instance of of exit.
     *
     * @param args args
     * @return of exit result
     */
    public static NMainArgs ofExit(String[] args) {
        return new NMainArgs().args(args).handleMode(NApplicationHandleMode.EXIT);
    }

    /**
     * Creates a new instance of of.
     *
     * @param application application
     * @param args        args
     * @return of result
     */
    public static NMainArgs of(NApplicationHandler application, String[] args) {
        return new NMainArgs().applicationInstance(application).nutsArgs(args);
    }

    /**
     * Creates a new instance of of handled.
     *
     * @param application application
     * @param args        args
     * @return of handled result
     */
    public static NMainArgs ofHandled(NApplicationHandler application, String[] args) {
        return new NMainArgs().applicationInstance(application).args(args).handleMode(NApplicationHandleMode.HANDLE);
    }

    /**
     * Creates a new instance of of propagated.
     *
     * @param application application
     * @param args        args
     * @return of propagated result
     */
    public static NMainArgs ofPropagated(NApplicationHandler application, String[] args) {
        return new NMainArgs().applicationInstance(application).args(args).handleMode(NApplicationHandleMode.PROPAGATE);
    }

    /**
     * Creates a new instance of of exit.
     *
     * @param application application
     * @param args        args
     * @return of exit result
     */
    public static NMainArgs ofExit(NApplicationHandler application, String[] args) {
        return new NMainArgs().args(args).applicationInstance(application).handleMode(NApplicationHandleMode.EXIT);
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
    public NMainArgs handleMode(NApplicationHandleMode mode) {
        this.handleMode = mode;
        return this;
    }

    /**
     * Application instance.
     *
     * @return application instance result
     */
    public NApplicationHandler applicationInstance() {
        return applicationInstance;
    }

    /**
     * Application instance.
     *
     * @param applicationInstance application instance
     * @return application instance result
     */
    public NMainArgs applicationInstance(NApplicationHandler applicationInstance) {
        this.applicationInstance = applicationInstance;
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
     * Nuts args.
     *
     * @param nutsArgs nuts args
     * @return nuts args result
     */
    public NMainArgs nutsArgs(String[] nutsArgs) {
        this.nutsArgs = nutsArgs;
        return this;
    }

    /**
     * Nuts args line.
     *
     * @param nutsArgs nuts args
     * @return nuts args line result
     */
    public NMainArgs nutsArgsLine(String nutsArgs) {
        this.nutsArgs = NBootCmdLine.parseDefault(nutsArgs);
        return this;
    }

    /**
     * Nuts args line.
     *
     * @param nutsArgs  nuts args
     * @param extraArgs extra args
     * @return nuts args line result
     */
    public NMainArgs nutsArgsLine(String nutsArgs, String[] extraArgs) {
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

    /**
     * Args.
     *
     * @return args result
     */
    public String[] args() {
        return args;
    }

    /**
     * Args.
     *
     * @param args args
     * @return args result
     */
    public NMainArgs args(String[] args) {
        this.args = args;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NMainArgs nMainArgs = (NMainArgs) o;
        return handleMode == nMainArgs.handleMode && Objects.equals(applicationInstance, nMainArgs.applicationInstance) && Objects.deepEquals(nutsArgs, nMainArgs.nutsArgs) && Objects.deepEquals(args, nMainArgs.args);
    }

    @Override
    public int hashCode() {
        return Objects.hash(handleMode, applicationInstance, Arrays.hashCode(nutsArgs), Arrays.hashCode(args));
    }

    @Override
    public String toString() {
        return NToStringBuilder.of(this).omitBlanks(true)
                .add("handleMode",handleMode)
                .addIfNonBlank("applicationInstance",applicationInstance)
                .addIfNonBlank("nutsArgs",nutsArgs)
                .addIfNonBlank("args", args)
                .build()
                ;
    }
}
