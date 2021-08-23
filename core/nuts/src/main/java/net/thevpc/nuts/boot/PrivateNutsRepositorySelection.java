package net.thevpc.nuts.boot;

public class PrivateNutsRepositorySelection {

    private final String name;
    private final String url;

    public PrivateNutsRepositorySelection(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

}
