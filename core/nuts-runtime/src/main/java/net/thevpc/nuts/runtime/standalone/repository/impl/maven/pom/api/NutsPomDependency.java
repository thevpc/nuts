package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api;

import net.thevpc.nuts.NutsBlankable;

import java.util.Arrays;
import java.util.Objects;

public class NutsPomDependency {

    private String groupId;
    private String artifactId;
    private String version;
    private String scope;
    private String classifier;
    private String os;
    private String arch;
    private String optional;
    private String type;
    private NutsPomId[] exclusions;

    public NutsPomDependency(String groupId, String artifactId, String version) {
        this(groupId, artifactId, null, version, null, null, null, null, null, new NutsPomId[0]);
    }

    public NutsPomDependency(String groupId, String artifactId, String classifier, String version, String scope, String optional,
                             String os,
                             String arch, String type,
                             NutsPomId[] exclusions) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.optional = optional;
        this.scope = scope;
        this.os = os;
        this.arch = arch;
        this.type = type;
        this.classifier = classifier;
        this.exclusions = exclusions;
    }

    public String getType() {
        return type;
    }

    public String getClassifier() {
        return classifier;
    }

    public boolean equalsShortName(NutsPomDependency other) {
        return other != null
                && Objects.equals(groupId, other.getGroupId())
                && Objects.equals(artifactId, other.getArtifactId())
                ;
    }

    public boolean equalsLongName(NutsPomDependency other) {
        return other != null
                && Objects.equals(groupId, other.getGroupId())
                && Objects.equals(artifactId, other.getArtifactId())
                && Objects.equals(version, other.getVersion())
                ;
    }

    public String getShortName() {
        if(NutsBlankable.isBlank(groupId) && NutsBlankable.isBlank(artifactId)){
            return "";
        }
        StringBuilder sb=new StringBuilder();
        if(!NutsBlankable.isBlank(groupId)){
            sb.append(groupId);
            sb.append(":");
        }
        sb.append(artifactId);
        return sb.toString();
    }

    public String getLongName() {
        if(NutsBlankable.isBlank(groupId) && NutsBlankable.isBlank(artifactId)){
            return "";
        }
        StringBuilder sb=new StringBuilder();
        if(!NutsBlankable.isBlank(groupId)){
            sb.append(groupId);
            sb.append(":");
        }
        sb.append(artifactId);
        if(!NutsBlankable.isBlank(version)){
            sb.append("#");
            sb.append(version);
        }
        return sb.toString();
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

    public NutsPomId[] getExclusions() {
        return exclusions;
    }

    public NutsPomDependency setClassifier(String classifier) {
        this.classifier = classifier;
        return this;
    }

    public NutsPomDependency setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public NutsPomDependency setArtifactId(String artifactId) {
        this.artifactId = artifactId;
        return this;
    }

    public NutsPomDependency setVersion(String version) {
        this.version = version;
        return this;
    }

    public NutsPomDependency setScope(String scope) {
        this.scope = scope;
        return this;
    }

    public NutsPomDependency setOptional(String optional) {
        this.optional = optional;
        return this;
    }

    public NutsPomDependency setExclusions(NutsPomId[] exclusions) {
        this.exclusions = exclusions;
        return this;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getArch() {
        return arch;
    }

    public void setArch(String arch) {
        this.arch = arch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NutsPomDependency that = (NutsPomDependency) o;
        return Objects.equals(toUniformGroupId(groupId), toUniformGroupId(that.groupId))
                && Objects.equals(toUniformGroupId(artifactId), toUniformGroupId(that.artifactId))
                && Objects.equals(toUniformGroupId(version), toUniformGroupId(that.version))
                && Objects.equals(toUniformGroupId(os), toUniformGroupId(that.os))
                && Objects.equals(toUniformGroupId(arch), toUniformGroupId(that.arch))
                && Objects.equals(toUniformScope(scope), toUniformScope(that.scope))
                && Objects.equals(toUniformOptional(optional), toUniformOptional(that.optional))
                && Arrays.equals(exclusions, that.exclusions);
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
        int result = Objects.hash(groupId, artifactId, version, scope, optional, os, arch);
        result = 31 * result + Arrays.hashCode(exclusions);
        return result;
    }

    @Override
    public String toString() {
        return "PomDependency{"
                + "groupId='" + groupId + '\''
                + ", artifactId='" + artifactId + '\''
                + ", version='" + version + '\''
                + ", scope='" + scope + '\''
                + ", os='" + os + '\''
                + ", arch='" + arch + '\''
                + ", optional='" + optional + '\''
                + ", type='" + type + '\''
                + ", exclusions=" + Arrays.toString(exclusions)
                + '}';
    }
}
