package net.vpc.app.nuts.core.util.mvn;

import java.util.ArrayList;
import java.util.List;

public class MavenMetadata {

    private String latest;
    private String release;
    private List<String> versions = new ArrayList<>();

    public List<String> getVersions() {
        return versions;
    }

    public String getLatest() {
        return latest;
    }

    public MavenMetadata setLatest(String latest) {
        this.latest = latest;
        return this;
    }

    public String getRelease() {
        return release;
    }

    public MavenMetadata setRelease(String release) {
        this.release = release;
        return this;
    }

    public MavenMetadata setVersions(List<String> versions) {
        this.versions = versions;
        return this;
    }
}
