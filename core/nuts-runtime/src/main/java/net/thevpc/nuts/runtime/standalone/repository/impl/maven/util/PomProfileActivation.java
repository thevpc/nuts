package net.thevpc.nuts.runtime.standalone.repository.impl.maven.util;

import java.util.Objects;

public class PomProfileActivation {
    private Boolean activeByDefault;
    private String jdk;
    private String property;
    private String file;
    private String osName;
    private String osArch;
    private String osFamily;
    private String osVersion;

    public PomProfileActivation() {
    }

    public Boolean getActiveByDefault() {
        return activeByDefault;
    }

    public PomProfileActivation setActiveByDefault(Boolean activeByDefault) {
        this.activeByDefault = activeByDefault;
        return this;
    }

    public String getJdk() {
        return jdk;
    }

    public PomProfileActivation setJdk(String jdk) {
        this.jdk = jdk;
        return this;
    }

    public String getProperty() {
        return property;
    }

    public PomProfileActivation setProperty(String property) {
        this.property = property;
        return this;
    }

    public String getOsName() {
        return osName;
    }

    public PomProfileActivation setOsName(String osName) {
        this.osName = osName;
        return this;
    }

    public String getOsArch() {
        return osArch;
    }

    public PomProfileActivation setOsArch(String osArch) {
        this.osArch = osArch;
        return this;
    }

    public String getOsFamily() {
        return osFamily;
    }

    public PomProfileActivation setOsFamily(String osFamily) {
        this.osFamily = osFamily;
        return this;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public PomProfileActivation setOsVersion(String osVersion) {
        this.osVersion = osVersion;
        return this;
    }

    public String getFile() {
        return file;
    }

    public PomProfileActivation setFile(String file) {
        this.file = file;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PomProfileActivation that = (PomProfileActivation) o;
        return Objects.equals(activeByDefault, that.activeByDefault) && Objects.equals(jdk, that.jdk) && Objects.equals(property, that.property) && Objects.equals(file, that.file) && Objects.equals(osName, that.osName) && Objects.equals(osArch, that.osArch) && Objects.equals(osFamily, that.osFamily) && Objects.equals(osVersion, that.osVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activeByDefault, jdk, property, file, osName, osArch, osFamily, osVersion);
    }

    @Override
    public String toString() {
        return "PomProfileActivation{" +
                "activeByDefault=" + activeByDefault +
                ", jdk='" + jdk + '\'' +
                ", property='" + property + '\'' +
                ", file='" + file + '\'' +
                ", osName='" + osName + '\'' +
                ", osArch='" + osArch + '\'' +
                ", osFamily='" + osFamily + '\'' +
                ", osVersion='" + osVersion + '\'' +
                '}';
    }
}
