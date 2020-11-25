package net.thevpc.nuts;

import java.net.URL;

public class NutsIdURL {
    private String id;
    private URL url;
    private NutsIdURL[] dependencies;

    public NutsIdURL(String id, URL url, NutsIdURL... dependencies) {
        this.id = id;
        this.url = url;
        this.dependencies = dependencies;
    }

    public String getId() {
        return id;
    }

    public URL getURL() {
        return url;
    }

    public NutsIdURL[] getDependencies() {
        return dependencies;
    }
}
