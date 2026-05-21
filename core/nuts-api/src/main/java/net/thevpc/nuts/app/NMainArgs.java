package net.thevpc.nuts.app;

import net.thevpc.nuts.boot.internal.cmdline.NBootCmdLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NMainArgs {
    private NApplicationHandleMode handleMode = NApplicationHandleMode.HANDLE;
    private NApplication applicationInstance;
    private String[] nutsArgs;
    private String[] args;

    public static NMainArgs of(String[] args) {
        return new NMainArgs().args(args);
    }

    public static NMainArgs ofHandled(String[] args) {
        return new NMainArgs().args(args).handleMode(NApplicationHandleMode.HANDLE);
    }

    public static NMainArgs ofPropagated(String[] args) {
        return new NMainArgs().args(args).handleMode(NApplicationHandleMode.PROPAGATE);
    }

    public static NMainArgs ofExit(String[] args) {
        return new NMainArgs().args(args).handleMode(NApplicationHandleMode.EXIT);
    }

    public static NMainArgs of(NApplication application, String[] args) {
        return new NMainArgs().applicationInstance(application).nutsArgs(args);
    }

    public static NMainArgs ofHandled(NApplication application, String[] args) {
        return new NMainArgs().applicationInstance(application).args(args).handleMode(NApplicationHandleMode.HANDLE);
    }

    public static NMainArgs ofPropagated(NApplication application, String[] args) {
        return new NMainArgs().applicationInstance(application).args(args).handleMode(NApplicationHandleMode.PROPAGATE);
    }

    public static NMainArgs ofExit(NApplication application, String[] args) {
        return new NMainArgs().args(args).applicationInstance(application).handleMode(NApplicationHandleMode.EXIT);
    }


    public NApplicationHandleMode handleMode() {
        return handleMode;
    }

    public NMainArgs handleMode(NApplicationHandleMode mode) {
        this.handleMode = mode;
        return this;
    }

    public NApplication applicationInstance() {
        return applicationInstance;
    }

    public NMainArgs applicationInstance(NApplication applicationInstance) {
        this.applicationInstance = applicationInstance;
        return this;
    }

    public String[] nutsArgs() {
        return nutsArgs;
    }

    public NMainArgs nutsArgs(String[] nutsArgs) {
        this.nutsArgs = nutsArgs;
        return this;
    }

    public NMainArgs nutsArgsLine(String nutsArgs) {
        this.nutsArgs = NBootCmdLine.parseDefault(nutsArgs);
        return this;
    }

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

    public String[] args() {
        return args;
    }

    public NMainArgs args(String[] args) {
        this.args = args;
        return this;
    }
}
