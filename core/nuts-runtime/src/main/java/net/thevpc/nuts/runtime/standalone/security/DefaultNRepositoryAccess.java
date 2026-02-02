package net.thevpc.nuts.runtime.standalone.security;

import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.security.NCredentialId;
import net.thevpc.nuts.security.NRepositoryAccess;
import net.thevpc.nuts.security.NRepositoryAccessSpec;

import java.util.List;
import java.util.Objects;

public class DefaultNRepositoryAccess implements NRepositoryAccess {
    private String userName;
    private String repositoryUuid;
    private String repositoryName;
    private String remoteUserName;
    private NCredentialId remoteCredential;
    private String remoteAuthType;
    private List<String> permissions;

    public DefaultNRepositoryAccess(String userName, String repositoryUuid, String repositoryName, String remoteUserName, NCredentialId remoteCredential, String remoteAuthType, List<String> permissions) {
        this.userName = userName;
        this.repositoryUuid = repositoryUuid;
        this.repositoryName = repositoryName;
        this.remoteUserName = remoteUserName;
        this.remoteCredential = remoteCredential;
        this.remoteAuthType = remoteAuthType;
        this.permissions = CoreNUtils.copyNonNullUnmodifiableList(permissions);
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public String getRepositoryUuid() {
        return repositoryUuid;
    }

    @Override
    public String getRepositoryName() {
        return repositoryName;
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
        DefaultNRepositoryAccess that = (DefaultNRepositoryAccess) o;
        return Objects.equals(userName, that.userName) && Objects.equals(repositoryUuid, that.repositoryUuid) && Objects.equals(repositoryName, that.repositoryName) && Objects.equals(remoteUserName, that.remoteUserName) && Objects.equals(remoteCredential, that.remoteCredential) && Objects.equals(remoteAuthType, that.remoteAuthType) && Objects.equals(permissions, that.permissions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, repositoryUuid, repositoryName, remoteUserName, remoteCredential, remoteAuthType, permissions);
    }

    @Override
    public String toString() {
        return "DefaultNRepositoryAccess{" +
                "userName='" + userName + '\'' +
                ", repositoryUuid='" + repositoryUuid + '\'' +
                ", repositoryName='" + repositoryName + '\'' +
                ", remoteUserName='" + remoteUserName + '\'' +
                ", remoteCredential=" + remoteCredential +
                ", remoteAuthType='" + remoteAuthType + '\'' +
                ", permissions=" + permissions +
                '}';
    }

    @Override
    public NRepositoryAccessSpec toSpec() {
        return new DefaultNRepositoryAccessSpec(userName, repositoryUuid, remoteUserName, remoteCredential, remoteAuthType, permissions);
    }
}
