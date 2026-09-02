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
     * Creates a new instance of {@code NMainArgs} with the given arguments.
     *
     * @param args application arguments
     * @return a new {@code NMainArgs} instance
     */
    public static NMainArgs of(String[] args) {
        return new NMainArgs().args(args);
    }

    /**
     * Creates a new instance of {@code NMainArgs} with the given arguments in handled mode.
     *
     * @param args application arguments
     * @return a new {@code NMainArgs} instance
     */
    public static NMainArgs ofHandled(String[] args) {
        return new NMainArgs().args(args).handleMode(NApplicationHandleMode.HANDLE);
    }

    /**
     * Creates a new instance of {@code NMainArgs} with the given arguments in propagated mode.
     *
     * @param args application arguments
     * @return a new {@code NMainArgs} instance
     */
    public static NMainArgs ofPropagated(String[] args) {
        return new NMainArgs().args(args).handleMode(NApplicationHandleMode.PROPAGATE);
    }

    /**
     * Creates a new instance of {@code NMainArgs} with the given arguments in exit mode.
     *
     * @param args application arguments
     * @return a new {@code NMainArgs} instance
     */
    public static NMainArgs ofExit(String[] args) {
        return new NMainArgs().args(args).handleMode(NApplicationHandleMode.EXIT);
    }

    /**
     * Creates a new instance of {@code NMainArgs} with the given application handler and arguments.
     *
     * @param application application handler
     * @param args        application arguments
     * @return a new {@code NMainArgs} instance
     */
    public static NMainArgs of(NApplicationHandler application, String[] args) {
        return new NMainArgs().applicationInstance(application).nutsArgs(args);
    }

    /**
     * Creates a new instance of {@code NMainArgs} with the given application handler and arguments in handled mode.
     *
     * @param application application handler
     * @param args        application arguments
     * @return a new {@code NMainArgs} instance
     */
    public static NMainArgs ofHandled(NApplicationHandler application, String[] args) {
        return new NMainArgs().applicationInstance(application).args(args).handleMode(NApplicationHandleMode.HANDLE);
    }

    /**
     * Creates a new instance of {@code NMainArgs} with the given application handler and arguments in propagated mode.
     *
     * @param application application handler
     * @param args        application arguments
     * @return a new {@code NMainArgs} instance
     */
    public static NMainArgs ofPropagated(NApplicationHandler application, String[] args) {
        return new NMainArgs().applicationInstance(application).args(args).handleMode(NApplicationHandleMode.PROPAGATE);
    }

    /**
     * Creates a new instance of {@code NMainArgs} with the given application handler and arguments in exit mode.
     *
     * @param application application handler
     * @param args        application arguments
     * @return a new {@code NMainArgs} instance
     */
    public static NMainArgs ofExit(NApplicationHandler application, String[] args) {
        return new NMainArgs().args(args).applicationInstance(application).handleMode(NApplicationHandleMode.EXIT);
    }


    /**
     * Gets the application handle mode.
     *
     * @return current handle mode
     */
    public NApplicationHandleMode handleMode() {
        return handleMode;
    }

    /**
     * Sets the application handle mode.
     *
     * @param mode handle mode
     * @return {@code this} instance
     */
    public NMainArgs handleMode(NApplicationHandleMode mode) {
        this.handleMode = mode;
        return this;
    }

    /**
     * Gets the application handler instance.
     *
     * @return application handler instance
     */
    public NApplicationHandler applicationInstance() {
        return applicationInstance;
    }

    /**
     * Sets the application handler instance.
     *
     * @param applicationInstance application handler instance
     * @return {@code this} instance
     */
    public NMainArgs applicationInstance(NApplicationHandler applicationInstance) {
        this.applicationInstance = applicationInstance;
        return this;
    }

    /**
     * Gets the Nuts bootstrap arguments.
     *
     * @return Nuts bootstrap arguments
     */
    public String[] nutsArgs() {
        return nutsArgs;
    }

    /**
     * Sets the Nuts bootstrap arguments.
     *
     * @param nutsArgs Nuts bootstrap arguments
     * @return {@code this} instance
     */
    public NMainArgs nutsArgs(String[] nutsArgs) {
        this.nutsArgs = nutsArgs;
        return this;
    }

    /**
     * Parses and sets Nuts arguments from a single command-line string.
     *
     * @param nutsArgs command-line string
     * @return {@code this} instance
     */
    public NMainArgs nutsArgsLine(String nutsArgs) {
        this.nutsArgs = NBootCmdLine.parseDefault(nutsArgs);
        return this;
    }

    /**
     * Parses and sets Nuts arguments from a command-line string with additional arguments.
     *
     * @param nutsArgs  command-line string
     * @param extraArgs additional arguments
     * @return {@code this} instance
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
     * Gets the application arguments.
     *
     * @return application arguments
     */
    public String[] args() {
        return args;
    }

    /**
     * Sets the application arguments.
     *
     * @param args application arguments
     * @return {@code this} instance
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
