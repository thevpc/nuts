package net.vpc.app.nuts;

public class NutsExecutionEntry {
    private String name;
    private boolean defaultEntry;
    private boolean app;

    public NutsExecutionEntry(String name, boolean defaultEntry, boolean app) {
        this.name = name;
        this.defaultEntry = defaultEntry;
        this.app = app;
    }

    public boolean isApp() {
        return app;
    }

    public String getName() {
        return name;
    }

    public boolean isDefaultEntry() {
        return defaultEntry;
    }

    @Override
    public String toString() {
        return "NutsExecutionEntry{" +
                "name='" + name + '\'' +
                ", app=" + app +
                ", defaultEntry=" + defaultEntry +
                '}';
    }
}
