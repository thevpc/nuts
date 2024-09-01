/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 *
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
package net.thevpc.nuts.runtime.standalone.definition;

import net.thevpc.nuts.NId;
import net.thevpc.nuts.NInstallInformation;
import net.thevpc.nuts.NInstallStatus;
import net.thevpc.nuts.io.NPath;

import java.time.Instant;
import java.util.Objects;

/**
 * @author thevpc
 * @since 0.5.5
 */
public class DefaultNInstallInfo implements NInstallInformation {

    private NId id;
    private NInstallStatus installStatus;
    private boolean wasInstalled;
    private boolean wasRequired;
    private Instant lasModifiedDate;
    private Instant createdDate;
    private String installUser;
    private NPath installFolder;
    private String sourceRepositoryName;
    private String sourceRepositoryUUID;
    private boolean justInstalled;
    private boolean justRequired;
    public DefaultNInstallInfo(NId id, NInstallStatus installStatus, NPath installFolder, Instant createdDate, Instant lasModifiedDate, String installUser, String sourceRepositoryName, String sourceRepositoryUUID, boolean justInstalled, boolean justRequired) {
        this.id = id;
        this.installStatus = installStatus;
        this.installFolder = installFolder;
        this.createdDate = createdDate;
        this.lasModifiedDate = lasModifiedDate;
        this.installUser = installUser;
        this.sourceRepositoryName = sourceRepositoryName;
        this.sourceRepositoryUUID = sourceRepositoryUUID;
        this.justInstalled=justInstalled;
        this.justRequired=justRequired;
    }

    public DefaultNInstallInfo(NInstallInformation other) {
        this.id = other.getId();
        this.installStatus = other.getInstallStatus();
        this.installFolder = other.getInstallFolder();
        this.createdDate = other.getCreatedInstant();
        this.lasModifiedDate = other.getLastModifiedInstant();
        this.installUser = other.getInstallUser();
        this.sourceRepositoryName = other.getSourceRepositoryName();
        this.sourceRepositoryUUID = other.getSourceRepositoryUUID();
        this.justInstalled = other.isJustInstalled();
        this.justRequired = other.isJustRequired();
    }

    public static DefaultNInstallInfo notInstalled(NId id) {
        return new DefaultNInstallInfo(null,
                NInstallStatus.NONE,
                null,
                null,
                null,
                null,
                null, null,false,false
        );
    }

    @Override
    public NId getId() {
        return id;
    }

    @Override
    public Instant getCreatedInstant() {
        return createdDate;
    }

    @Override
    public Instant getLastModifiedInstant() {
        return lasModifiedDate;
    }

    @Override
    public boolean isDefaultVersion() {
        return getInstallStatus().isDefaultVersion();
    }

    @Override
    public NPath getInstallFolder() {
        return installFolder;
    }

    @Override
    public boolean isWasInstalled() {
        return wasInstalled;
    }

    public DefaultNInstallInfo setWasInstalled(boolean wasInstalled) {
        this.wasInstalled = wasInstalled;
        return this;
    }

    @Override
    public boolean isWasRequired() {
        return wasRequired;
    }

    @Override
    public String getInstallUser() {
        return installUser;
    }

    public NInstallStatus getInstallStatus() {
        return installStatus;
    }

    @Override
    public boolean isInstalledOrRequired() {
        return installStatus.isRequired()
                || installStatus.isInstalled();
    }

    @Override
    public String getSourceRepositoryName() {
        return sourceRepositoryName;
    }

    @Override
    public String getSourceRepositoryUUID() {
        return sourceRepositoryUUID;
    }

    public DefaultNInstallInfo setSourceRepositoryUUID(String sourceRepositoryUUID) {
        this.sourceRepositoryUUID = sourceRepositoryUUID;
        return this;
    }

    public DefaultNInstallInfo setSourceRepositoryName(String sourceRepositoryName) {
        this.sourceRepositoryName = sourceRepositoryName;
        return this;
    }

    public DefaultNInstallInfo setInstallStatus(NInstallStatus installStatus) {
        this.installStatus = installStatus;
        return this;
    }

    public DefaultNInstallInfo setInstallUser(String installUser) {
        this.installUser = installUser;
        return this;
    }

    public DefaultNInstallInfo setWasRequired(boolean wasRequired) {
        this.wasRequired = wasRequired;
        return this;
    }

    public DefaultNInstallInfo setInstallFolder(NPath installFolder) {
        this.installFolder = installFolder;
        return this;
    }

    public DefaultNInstallInfo setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public DefaultNInstallInfo setId(NId id) {
        this.id = id;
        return this;
    }

    @Override
    public boolean isJustReInstalled() {
        return isWasInstalled() && isJustInstalled();
    }

    @Override
    public boolean isJustInstalled() {
        return justInstalled;
    }

    @Override
    public boolean isJustReRequired() {
        return isWasRequired() && isJustRequired();
    }

    @Override
    public boolean isJustRequired() {
        return justRequired;
    }

    public DefaultNInstallInfo setLasModifiedDate(Instant lasModifiedDate) {
        this.lasModifiedDate = lasModifiedDate;
        return this;
    }

    public DefaultNInstallInfo setJustInstalled(boolean justInstalled) {
        this.justInstalled = justInstalled;
        return this;
    }

    public DefaultNInstallInfo setJustRequired(boolean justRequired) {
        this.justRequired = justRequired;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNInstallInfo that = (DefaultNInstallInfo) o;
        return wasInstalled == that.wasInstalled && wasRequired == that.wasRequired && justInstalled == that.justInstalled && justRequired == that.justRequired && Objects.equals(id, that.id) && Objects.equals(installStatus, that.installStatus) && Objects.equals(lasModifiedDate, that.lasModifiedDate) && Objects.equals(createdDate, that.createdDate) && Objects.equals(installUser, that.installUser) && Objects.equals(installFolder, that.installFolder) && Objects.equals(sourceRepositoryName, that.sourceRepositoryName) && Objects.equals(sourceRepositoryUUID, that.sourceRepositoryUUID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, installStatus, wasInstalled, wasRequired, lasModifiedDate, createdDate, installUser, installFolder, sourceRepositoryName, sourceRepositoryUUID, justInstalled, justRequired);
    }
}
