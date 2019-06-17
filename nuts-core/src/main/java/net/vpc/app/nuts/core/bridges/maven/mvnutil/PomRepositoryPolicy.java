package net.vpc.app.nuts.core.bridges.maven.mvnutil;

import java.util.Objects;

public class PomRepositoryPolicy {

    private boolean enabled = true;
    private String updatePolicy;
    private String checksumPolicy;

    public PomRepositoryPolicy() {
    }

    public PomRepositoryPolicy(boolean enabled, String updatePolicy, String checksumPolicy) {
        this.enabled = enabled;
        this.updatePolicy = updatePolicy;
        this.checksumPolicy = checksumPolicy;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public PomRepositoryPolicy setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public String getUpdatePolicy() {
        return updatePolicy;
    }

    public PomRepositoryPolicy setUpdatePolicy(String updatePolicy) {
        this.updatePolicy = updatePolicy;
        return this;
    }

    public String getChecksumPolicy() {
        return checksumPolicy;
    }

    public PomRepositoryPolicy setChecksumPolicy(String checksumPolicy) {
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
        PomRepositoryPolicy that = (PomRepositoryPolicy) o;
        return enabled == that.enabled
                && Objects.equals(updatePolicy, that.updatePolicy)
                && Objects.equals(checksumPolicy, that.checksumPolicy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, updatePolicy, checksumPolicy);
    }
}
