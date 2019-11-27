package net.vpc.app.nuts.runtime.bridges.maven.mvnutil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MavenMetadata {

    private String groupId;
    private String artifactId;
    private String latest;
    private String release;
    private List<String> versions = new ArrayList<>();
    private Date lastUpdated;

    public List<String> getVersions() {
        return versions;
    }

    public String getLatest() {
        return latest;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
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

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

}
