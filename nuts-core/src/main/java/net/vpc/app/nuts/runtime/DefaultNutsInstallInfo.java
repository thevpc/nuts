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
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.runtime;

import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsInstallInformation;
import net.vpc.app.nuts.NutsInstallStatus;

import java.nio.file.Path;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

/**
 * @author vpc
 * @since 0.5.5
 */
public class DefaultNutsInstallInfo implements NutsInstallInformation {

    private NutsId id;
    private EnumSet<NutsInstallStatus> installStatus;
    private boolean wasInstalled;
    private boolean wasRequired;
    private Instant lasModifiedDate;
    private Instant createdDate;
    private String installUser;
    private Path installFolder;
    private String sourceRepositoryName;
    private String sourceRepositoryUUID;
    private boolean justInstalled;
    private boolean justRequired;
    public DefaultNutsInstallInfo(NutsId id, Set<NutsInstallStatus> installStatus, Path installFolder, Instant createdDate, Instant lasModifiedDate, String installUser, String sourceRepositoryName, String sourceRepositoryUUID,boolean justInstalled,boolean justRequired) {
        this.id = id;
        this.installStatus = EnumSet.copyOf(installStatus);
        this.installFolder = installFolder;
        this.createdDate = createdDate;
        this.lasModifiedDate = lasModifiedDate;
        this.installUser = installUser;
        this.sourceRepositoryName = sourceRepositoryName;
        this.sourceRepositoryUUID = sourceRepositoryUUID;
        this.justInstalled=justInstalled;
        this.justRequired=justRequired;
    }

    public DefaultNutsInstallInfo(NutsInstallInformation other) {
        this.id = other.getId();
        this.installStatus = EnumSet.copyOf(other.getInstallStatus());
        this.installFolder = other.getInstallFolder();
        this.createdDate = other.getCreatedDate();
        this.lasModifiedDate = other.getLastModifiedDate();
        this.installUser = other.getInstallUser();
        this.sourceRepositoryName = other.getSourceRepositoryName();
        this.sourceRepositoryUUID = other.getSourceRepositoryUUID();
        this.justInstalled = other.isJustInstalled();
        this.justRequired = other.isJustRequired();
    }

    public static DefaultNutsInstallInfo notInstalled(NutsId id) {
        return new DefaultNutsInstallInfo(null,
                EnumSet.of(NutsInstallStatus.NOT_INSTALLED),
                null,
                null,
                null,
                null,
                null, null,false,false
        );
    }

    @Override
    public NutsId getId() {
        return id;
    }

    @Override
    public Instant getCreatedDate() {
        return createdDate;
    }

    @Override
    public Instant getLastModifiedDate() {
        return lasModifiedDate;
    }

    @Override
    public boolean isDefaultVersion() {
        return getInstallStatus().contains(NutsInstallStatus.DEFAULT_VERSION);
    }

    @Override
    public Path getInstallFolder() {
        return installFolder;
    }

    @Override
    public boolean isWasInstalled() {
        return wasInstalled;
    }

    public DefaultNutsInstallInfo setWasInstalled(boolean wasInstalled) {
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

    public Set<NutsInstallStatus> getInstallStatus() {
        return installStatus;
    }

    @Override
    public boolean isInstalledOrRequired() {
        return installStatus.contains(NutsInstallStatus.REQUIRED)
                || installStatus.contains(NutsInstallStatus.INSTALLED);
    }

    @Override
    public String getSourceRepositoryName() {
        return sourceRepositoryName;
    }

    @Override
    public String getSourceRepositoryUUID() {
        return sourceRepositoryUUID;
    }

    public DefaultNutsInstallInfo setSourceRepositoryUUID(String sourceRepositoryUUID) {
        this.sourceRepositoryUUID = sourceRepositoryUUID;
        return this;
    }

    public DefaultNutsInstallInfo setSourceRepositoryName(String sourceRepositoryName) {
        this.sourceRepositoryName = sourceRepositoryName;
        return this;
    }

    public DefaultNutsInstallInfo setInstallStatus(EnumSet<NutsInstallStatus> installStatus) {
        this.installStatus = installStatus;
        return this;
    }

    public DefaultNutsInstallInfo setInstallUser(String installUser) {
        this.installUser = installUser;
        return this;
    }

    public DefaultNutsInstallInfo setWasRequired(boolean wasRequired) {
        this.wasRequired = wasRequired;
        return this;
    }

    public DefaultNutsInstallInfo setInstallFolder(Path installFolder) {
        this.installFolder = installFolder;
        return this;
    }

    public DefaultNutsInstallInfo setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public DefaultNutsInstallInfo setId(NutsId id) {
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

    public DefaultNutsInstallInfo setLasModifiedDate(Instant lasModifiedDate) {
        this.lasModifiedDate = lasModifiedDate;
        return this;
    }

    public DefaultNutsInstallInfo setJustInstalled(boolean justInstalled) {
        this.justInstalled = justInstalled;
        return this;
    }

    public DefaultNutsInstallInfo setJustRequired(boolean justRequired) {
        this.justRequired = justRequired;
        return this;
    }
}
