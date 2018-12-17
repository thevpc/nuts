package net.vpc.app.nuts;

public class NutsRepositoryDefinition {
    public static final int ORDER_USER_LOCAL=1000;
    public static final int ORDER_USER_REMOTE=2000;
    public static final int ORDER_SYSTEM_LOCAL=10000;
    public static final int ORDER_SYSTEM_REMOTE=12000;
    private String id;
    private String location;
    private String type;
    private boolean proxied;
    private int order;

    public NutsRepositoryDefinition(String id, String location, String type, boolean proxied, int order) {
        this.id = id;
        this.location = location;
        this.type = type;
        this.proxied = proxied;
        this.order = order;
    }

    public String getId() {
        return id;
    }

    public String getLocation() {
        return location;
    }

    public String getType() {
        return type;
    }

    public boolean isProxied() {
        return proxied;
    }
}
