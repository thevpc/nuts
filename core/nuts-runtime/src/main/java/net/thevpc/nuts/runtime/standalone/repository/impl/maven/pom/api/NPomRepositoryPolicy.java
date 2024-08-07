package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api;

import java.util.Objects;

public class NPomRepositoryPolicy {

    private boolean enabled = true;
    private String updatePolicy;
    private String checksumPolicy;

    public NPomRepositoryPolicy() {
    }

    public NPomRepositoryPolicy(boolean enabled, String updatePolicy, String checksumPolicy) {
        this.enabled = enabled;
        this.updatePolicy = updatePolicy;
        this.checksumPolicy = checksumPolicy;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public NPomRepositoryPolicy setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public String getUpdatePolicy() {
        return updatePolicy;
    }

    public NPomRepositoryPolicy setUpdatePolicy(String updatePolicy) {
        this.updatePolicy = updatePolicy;
        return this;
    }

    public String getChecksumPolicy() {
        return checksumPolicy;
    }

    public NPomRepositoryPolicy setChecksumPolicy(String checksumPolicy) {
        this.checksumPolicy = checksumPolicy;
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
        NPomRepositoryPolicy that = (NPomRepositoryPolicy) o;
        return enabled == that.enabled
                && Objects.equals(updatePolicy, that.updatePolicy)
                && Objects.equals(checksumPolicy, that.checksumPolicy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, updatePolicy, checksumPolicy);
    }
}
