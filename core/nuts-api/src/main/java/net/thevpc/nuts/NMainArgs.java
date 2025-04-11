package net.thevpc.nuts;

public class NMainArgs {
    private NApplicationHandleMode handleMode = NApplicationHandleMode.HANDLE;
    private NApplication applicationInstance;
    private String[] nutsArgs;
    private String[] args;

    public static NMainArgs of(String[] args) {
        return new NMainArgs().setArgs(args);
    }

    public static NMainArgs ofHandled(String[] args) {
        return new NMainArgs().setArgs(args).setHandleMode(NApplicationHandleMode.HANDLE);
    }

    public static NMainArgs ofPropagated(String[] args) {
        return new NMainArgs().setArgs(args).setHandleMode(NApplicationHandleMode.PROPAGATE);
    }

    public static NMainArgs ofExit(String[] args) {
        return new NMainArgs().setArgs(args).setHandleMode(NApplicationHandleMode.EXIT);
    }

    public static NMainArgs of(NApplication application, String[] args) {
        return new NMainArgs().setApplicationInstance(application).setNutsArgs(args);
    }

    public static NMainArgs ofHandled(NApplication application, String[] args) {
        return new NMainArgs().setApplicationInstance(application).setArgs(args).setHandleMode(NApplicationHandleMode.HANDLE);
    }

    public static NMainArgs ofPropagated(NApplication application, String[] args) {
        return new NMainArgs().setApplicationInstance(application).setArgs(args).setHandleMode(NApplicationHandleMode.PROPAGATE);
    }

    public static NMainArgs ofExit(NApplication application, String[] args) {
        return new NMainArgs().setArgs(args).setApplicationInstance(application).setHandleMode(NApplicationHandleMode.EXIT);
    }


    public NApplicationHandleMode getHandleMode() {
        return handleMode;
    }

    public NMainArgs setHandleMode(NApplicationHandleMode mode) {
        this.handleMode = mode;
        return this;
    }

    public NApplication getApplicationInstance() {
        return applicationInstance;
    }

    public NMainArgs setApplicationInstance(NApplication applicationInstance) {
        this.applicationInstance = applicationInstance;
        return this;
    }

    public String[] getNutsArgs() {
        return nutsArgs;
    }

    public NMainArgs setNutsArgs(String[] nutsArgs) {
        this.nutsArgs = nutsArgs;
        return this;
    }

    public String[] getArgs() {
        return args;
    }

    public NMainArgs setArgs(String[] args) {
        this.args = args;
        return this;
    }
}
