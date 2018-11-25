package net.vpc.app.nuts;

public class ExecutionEntry {
    private String name;
    private boolean defaultEntry;

    public ExecutionEntry(String name, boolean defaultEntry) {
        this.name = name;
        this.defaultEntry = defaultEntry;
    }

    public String getName() {
        return name;
    }

    public boolean isDefaultEntry() {
        return defaultEntry;
    }

    @Override
    public String toString() {
        return "ExecutionEntry{" +
                "name='" + name + '\'' +
                ", defaultEntry=" + defaultEntry +
                '}';
    }
}
