/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.security;

import net.thevpc.nuts.core.NConfigItem;
import net.thevpc.nuts.internal.NReservedLangUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author thevpc
 * @app.category Config
 * @since 0.5.4
 */
public final class NRepositoryAccessConfig extends NConfigItem implements Cloneable {

    private static final long serialVersionUID = 2;
    private String userName;
    private String repository;
    private String remoteUserName;
    private String remoteCredential;
    private String remoteAuthType;
    private List<String> permissions;

    public NRepositoryAccessConfig() {
    }

    public NRepositoryAccessConfig(NRepositoryAccessConfig other) {
        this.userName = other.getUserName();
        this.repository = other.getRepository();
        this.remoteUserName = other.getRemoteUserName();
        this.remoteCredential = other.getRemoteCredential();
        this.remoteAuthType = other.getRemoteAuthType();
        setPermissions(other.getPermissions());
    }

    public NRepositoryAccessConfig(String userName, String repository, String remoteUserName, String remoteCredential, String remoteAuthType, List<String> permissions) {
        this.userName = userName;
        this.repository = repository;
        this.remoteUserName = remoteUserName;
        this.remoteCredential = remoteCredential;
        this.remoteAuthType = remoteAuthType;
        setPermissions(permissions);
    }

    public String getRemoteUserName() {
        return remoteUserName;
    }

    public NRepositoryAccessConfig setRemoteUserName(String remoteUserName) {
        this.remoteUserName = remoteUserName;
        return this;
    }

    public String getRemoteAuthType() {
        return remoteAuthType;
    }

    public NRepositoryAccessConfig setRemoteAuthType(String remoteAuthType) {
        this.remoteAuthType = remoteAuthType;
        return this;
    }

    public String getRemoteCredential() {
        return remoteCredential;
    }

    public NRepositoryAccessConfig setRemoteCredential(String remoteCredential) {
        this.remoteCredential = remoteCredential;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public NRepositoryAccessConfig setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public String getRepository() {
        return repository;
    }

    public NRepositoryAccessConfig setRepository(String repository) {
        this.repository = repository;
        return this;
    }

    public NRepositoryAccessConfig copy() {
        return clone();
    }

    @Override
    protected NRepositoryAccessConfig clone() {
        try {
            NRepositoryAccessConfig o = (NRepositoryAccessConfig) super.clone();
            if (o.permissions != null) {
                o.permissions = new ArrayList<>(o.permissions);
            }
            return o;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = NReservedLangUtils.nonNullList(permissions);
    }


    @Override
    public int hashCode() {
        int result = Objects.hash(userName,repository, remoteCredential, permissions);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NRepositoryAccessConfig that = (NRepositoryAccessConfig) o;
        return Objects.equals(userName, that.userName) &&
                Objects.equals(permissions, that.permissions) &&
                Objects.equals(remoteUserName, that.remoteUserName) &&
                Objects.equals(remoteAuthType, that.remoteAuthType) &&
                Objects.equals(remoteCredential, that.remoteCredential) &&
                Objects.equals(repository, that.repository)
                ;
    }

    @Override
    public String toString() {
        return "NutsUserConfig{" +
                ", user='" + userName + '\'' +
                ", repository='" + repository + '\'' +
                ", credentials='" + remoteCredential + '\'' +
                ", permissions=" + permissions +
                '}';
    }
}
