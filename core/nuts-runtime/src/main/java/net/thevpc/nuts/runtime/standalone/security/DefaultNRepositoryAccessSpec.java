package net.thevpc.nuts.runtime.standalone.security;

import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.security.NCredentialId;
import net.thevpc.nuts.security.NRepositoryAccess;
import net.thevpc.nuts.security.NRepositoryAccessSpec;
import net.thevpc.nuts.util.NBlankable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DefaultNRepositoryAccessSpec implements NRepositoryAccessSpec {
    private String userName;
    private String repository;
    private String remoteUserName;
    private NCredentialId remoteCredential;
    private String remoteAuthType;
    private List<String> permissions;

    public DefaultNRepositoryAccessSpec(String userName, String repository) {
        this.userName = userName;
        this.repository = repository;
    }

    public DefaultNRepositoryAccessSpec(String userName, String repository, String remoteUserName, NCredentialId remoteCredential, String remoteAuthType, List<String> permissions) {
        this.userName = userName;
        this.repository = repository;
        this.remoteUserName = remoteUserName;
        this.remoteCredential = remoteCredential;
        this.remoteAuthType = remoteAuthType;
        this.permissions = CoreNUtils.copyNonNullUnmodifiableList(permissions);
    }

    @Override
    public NRepositoryAccessSpec setRemoteUserName(String remoteUserName) {
        this.remoteUserName = remoteUserName;
        return this;
    }

    @Override
    public DefaultNRepositoryAccessSpec setRemoteCredential(NCredentialId remoteCredential) {
        this.remoteCredential = remoteCredential;
        return this;
    }

    @Override
    public DefaultNRepositoryAccessSpec setRemoteAuthType(String remoteAuthType) {
        this.remoteAuthType = remoteAuthType;
        return this;
    }

    @Override
    public DefaultNRepositoryAccessSpec setPermissions(List<String> permissions) {
        this.permissions = permissions;
        return this;
    }

    @Override
    public NRepositoryAccessSpec addPermissions(String... permissions) {
        if (permissions != null) {
            for (String p : permissions) {
                if (!NBlankable.isBlank(p)) {
                    if (this.permissions == null) {
                        this.permissions = new ArrayList<>();
                    }
                    this.permissions.add(p);
                }
            }
        }
        return this;
    }

    @Override
    public NRepositoryAccessSpec removePermissions(String... permissions) {
        if (permissions != null) {
            if (this.permissions != null) {
                for (String p : permissions) {
                    if (!NBlankable.isBlank(p)) {
                        this.permissions.remove(p);
                    }
                }
            }
        }
        return this;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public String getRepository() {
        return repository;
    }

    @Override
    public String getRemoteUserName() {
        return remoteUserName;
    }

    @Override
    public NCredentialId getRemoteCredential() {
        return remoteCredential;
    }

    @Override
    public String getRemoteAuthType() {
        return remoteAuthType;
    }

    @Override
    public List<String> getPermissions() {
        return permissions;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNRepositoryAccessSpec that = (DefaultNRepositoryAccessSpec) o;
        return Objects.equals(userName, that.userName) && Objects.equals(repository, that.repository) && Objects.equals(remoteUserName, that.remoteUserName) && Objects.equals(remoteCredential, that.remoteCredential) && Objects.equals(remoteAuthType, that.remoteAuthType) && Objects.equals(permissions, that.permissions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, repository, remoteUserName, remoteCredential, remoteAuthType, permissions);
    }

    @Override
    public String toString() {
        return "DefaultNRepositoryAccess{" +
                "userName='" + userName + '\'' +
                ", repositoryUuid='" + repository + '\'' +
                ", remoteUserName='" + remoteUserName + '\'' +
                ", remoteCredential=" + remoteCredential +
                ", remoteAuthType='" + remoteAuthType + '\'' +
                ", permissions=" + permissions +
                '}';
    }
}
