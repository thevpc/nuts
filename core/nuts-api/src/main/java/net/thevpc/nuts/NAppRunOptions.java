package net.thevpc.nuts;

public class NAppRunOptions {
    private NApplicationHandleMode handleMode = NApplicationHandleMode.HANDLE;
    private NApplication applicationInstance;
    private String[] nutsArgs;
    private String[] args;

    public static NAppRunOptions of(String[] args) {
        return new NAppRunOptions().setArgs(args);
    }

    public static NAppRunOptions ofHandled(String[] args) {
        return new NAppRunOptions().setArgs(args).setHandleMode(NApplicationHandleMode.HANDLE);
    }

    public static NAppRunOptions ofPropagated(String[] args) {
        return new NAppRunOptions().setArgs(args).setHandleMode(NApplicationHandleMode.PROPAGATE);
    }
    public static NAppRunOptions ofExit(String[] args) {
        return new NAppRunOptions().setArgs(args).setHandleMode(NApplicationHandleMode.EXIT);
    }

    public static NAppRunOptions of(NApplication application, String[] args) {
        return new NAppRunOptions().setApplicationInstance(application).setNutsArgs(args);
    }

    public static NAppRunOptions ofHandled(NApplication application,String[] args) {
        return new NAppRunOptions().setApplicationInstance(application).setArgs(args).setHandleMode(NApplicationHandleMode.HANDLE);
    }

    public static NAppRunOptions ofPropagated(NApplication application,String[] args) {
        return new NAppRunOptions().setApplicationInstance(application).setArgs(args).setHandleMode(NApplicationHandleMode.PROPAGATE);
    }
    public static NAppRunOptions ofExit(NApplication application,String[] args) {
        return new NAppRunOptions().setArgs(args).setApplicationInstance(application).setHandleMode(NApplicationHandleMode.EXIT);
    }



    public NApplicationHandleMode getHandleMode() {
        return handleMode;
    }

    public NAppRunOptions setHandleMode(NApplicationHandleMode mode) {
        this.handleMode = mode;
        return this;
    }

    public NApplication getApplicationInstance() {
        return applicationInstance;
    }

    public NAppRunOptions setApplicationInstance(NApplication applicationInstance) {
        this.applicationInstance = applicationInstance;
        return this;
    }

    public String[] getNutsArgs() {
        return nutsArgs;
    }

    public NAppRunOptions setNutsArgs(String[] nutsArgs) {
        this.nutsArgs = nutsArgs;
        return this;
    }

    public String[] getArgs() {
        return args;
    }

    public NAppRunOptions setArgs(String[] args) {
        this.args = args;
        return this;
    }
}
