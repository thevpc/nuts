package net.vpc.app.nuts.core.bridges.maven.mvnutil;

import java.util.Arrays;
import java.util.Objects;

public class PomDependency {
    private String groupId;
    private String artifactId;
    private String version;
    private String scope;
    private String classifier;
    private String optional;
    private PomId[] exclusions;

    public PomDependency(String groupId, String artifactId, String version) {
        this(groupId, artifactId, null, version, null, null, new PomId[0]);
    }

    public PomDependency(String groupId, String artifactId, String classifier, String version, String scope, String optional, PomId[] exclusions) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.optional = optional;
        this.scope = scope;
        this.classifier = classifier;
        this.exclusions = exclusions;
    }

    public String getClassifier() {
        return classifier;
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

    public String getScope() {
        return scope;
    }

    public String getOptional() {
        return optional;
    }

    public PomId[] getExclusions() {
        return exclusions;
    }

    public PomDependency setClassifier(String classifier) {
        this.classifier = classifier;
        return this;
    }

    public PomDependency setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public PomDependency setArtifactId(String artifactId) {
        this.artifactId = artifactId;
        return this;
    }

    public PomDependency setVersion(String version) {
        this.version = version;
        return this;
    }

    public PomDependency setScope(String scope) {
        this.scope = scope;
        return this;
    }

    public PomDependency setOptional(String optional) {
        this.optional = optional;
        return this;
    }

    public PomDependency setExclusions(PomId[] exclusions) {
        this.exclusions = exclusions;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PomDependency that = (PomDependency) o;
        return Objects.equals(toUniformGroupId(groupId), toUniformGroupId(that.groupId)) &&
                Objects.equals(toUniformGroupId(artifactId), toUniformGroupId(that.artifactId)) &&
                Objects.equals(toUniformGroupId(version), toUniformGroupId(that.version)) &&
                Objects.equals(toUniformScope(scope), toUniformScope(that.scope)) &&
                Objects.equals(toUniformOptional(optional), toUniformOptional(that.optional)) &&
                Arrays.equals(exclusions, that.exclusions);
    }

    private static String toUniformGroupId(String s) {
        return s == null ? "" : s.trim();
    }

    private static String toUniformScope(String s) {
        s = s == null ? "" : s.trim().toLowerCase();
        if (s.isEmpty()) {
            return "compile";
        }
        return s;
    }

    private static String toUniformOptional(String s) {
        s = s == null ? "" : s.trim().toLowerCase();
        return s;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(groupId, artifactId, version, scope, optional);
        result = 31 * result + Arrays.hashCode(exclusions);
        return result;
    }

    @Override
    public String toString() {
        return "PomDependency{" +
                "groupId='" + groupId + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", version='" + version + '\'' +
                ", scope='" + scope + '\'' +
                ", optional='" + optional + '\'' +
                ", exclusions=" + Arrays.toString(exclusions) +
                '}';
    }
}
