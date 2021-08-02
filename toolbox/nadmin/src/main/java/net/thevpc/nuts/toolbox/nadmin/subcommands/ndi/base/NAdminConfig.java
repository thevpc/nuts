package net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.base;

public class NAdminConfig {
    private String apiVersion;
    private String ndiVersion;

    public String getApiVersion() {
        return apiVersion;
    }

    public NAdminConfig setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    public String getNdiVersion() {
        return ndiVersion;
    }

    public NAdminConfig setNdiVersion(String ndiVersion) {
        this.ndiVersion = ndiVersion;
        return this;
    }
}
