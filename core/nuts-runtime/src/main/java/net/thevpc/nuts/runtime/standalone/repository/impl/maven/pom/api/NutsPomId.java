package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api;

import java.util.Objects;

public class NutsPomId {

    private String groupId;
    private String artifactId;
    private String version;

    public NutsPomId(String groupId, String artifactId, String version) {
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

    public NutsPomId setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public NutsPomId setArtifactId(String artifactId) {
        this.artifactId = artifactId;
        return this;
    }

    public NutsPomId setVersion(String version) {
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
        NutsPomId pomId = (NutsPomId) o;
        return Objects.equals(groupId, pomId.groupId)
                && Objects.equals(artifactId, pomId.artifactId)
                && Objects.equals(version, pomId.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, artifactId, version);
    }


}
