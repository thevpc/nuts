package net.thevpc.nuts.runtime.standalone.util.jclass;

public class MainClassType {

    private final String name;
    private final boolean app;
    private final boolean main;

    public MainClassType(String name, boolean main, boolean app) {
        this.name = name;
        this.app = app;
        this.main = main;
    }

    public String getName() {
        return name;
    }

    public boolean isApp() {
        return app;
    }

    public boolean isMain() {
        return main;
    }

}
