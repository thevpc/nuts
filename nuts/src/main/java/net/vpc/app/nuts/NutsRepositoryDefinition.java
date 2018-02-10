package net.vpc.app.nuts;

public class NutsRepositoryDefinition {

    private String id;
    private String location;
    private String type;
    private boolean proxied;

    public NutsRepositoryDefinition(String id, String location, String type, boolean proxied) {
        this.id = id;
        this.location = location;
        this.type = type;
        this.proxied = proxied;
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
