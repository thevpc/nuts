package net.thevpc.nuts.runtime.standalone.repository.impl.maven.util;

import java.util.Objects;

public class PomId {

    private String groupId;
    private String artifactId;
    private String version;

    public PomId(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return groupId + ":" + artifactId + "#" + version;
    }

    public PomId setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public PomId setArtifactId(String artifactId) {
        this.artifactId = artifactId;
        return this;
    }

    public PomId setVersion(String version) {
        this.version = version;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PomId pomId = (PomId) o;
        return Objects.equals(groupId, pomId.groupId)
                && Objects.equals(artifactId, pomId.artifactId)
                && Objects.equals(version, pomId.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, artifactId, version);
    }


}
