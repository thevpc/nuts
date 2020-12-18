package net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.base;

public class NdiConfig {
    private String apiVersion;
    private String ndiVersion;

    public String getApiVersion() {
        return apiVersion;
    }

    public NdiConfig setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    public String getNdiVersion() {
        return ndiVersion;
    }

    public NdiConfig setNdiVersion(String ndiVersion) {
        this.ndiVersion = ndiVersion;
        return this;
    }
}
