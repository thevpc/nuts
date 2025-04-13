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
 *
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
package net.thevpc.nuts.runtime.standalone.definition.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.NInstalledRepository;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.util.NFilter;
import net.thevpc.nuts.util.NFilterOp;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Package installation status.
 */
public class NInstallStatusDefinitionFilter2 extends AbstractDefinitionFilter {

    private final Mode mode;
    private final boolean value;

    public static enum Mode {
        INSTALLED,
        REQUIRED,
        DEPLOYED,
        NON_DEPLOYED,
        OBSOLETE,
        DEFAULT_VERSION,
        INSTALLED_OR_REQUIRED
    }

    public static NDefinitionFilter ofInstalled(boolean value) {
        return new NInstallStatusDefinitionFilter2(Mode.INSTALLED, value) ;
    }

    public static NDefinitionFilter ofInstalledOrRequired(boolean value) {
        return new NInstallStatusDefinitionFilter2(Mode.INSTALLED_OR_REQUIRED, value) ;
    }

    public static NDefinitionFilter ofRequired(boolean value) {
        return new NInstallStatusDefinitionFilter2(Mode.REQUIRED, value);
    }

    public static NDefinitionFilter ofObsolete(boolean value) {
        return new NInstallStatusDefinitionFilter2(Mode.OBSOLETE, value) ;
    }

    public static NDefinitionFilter ofDefaultVersion(boolean value) {
        return new NInstallStatusDefinitionFilter2(Mode.DEFAULT_VERSION, value) ;
    }

    public static NDefinitionFilter ofDeployed(boolean value) {
        return new NInstallStatusDefinitionFilter2(Mode.DEPLOYED, value) ;
    }

    public NInstallStatusDefinitionFilter2(Mode mode, boolean value) {
        super(NFilterOp.CUSTOM);
        this.mode = mode;
        this.value = value;
    }

    public Mode getMode() {
        return mode;
    }

    public boolean isValue() {
        return value;
    }

    @Override
    public boolean acceptDefinition(NDefinition definition) {
        NInstalledRepository installedRepository = NWorkspaceExt.of().getInstalledRepository();
        //will always load install information
        NInstallInformation n = installedRepository.getInstallInformation(definition.getId());
        NInstallStatus status = n.getInstallStatus();
        if (status == null) {
            return false;
        }
        switch (mode) {
            case INSTALLED:
                return status.isInstalled();
            case DEPLOYED:
                return status.isDeployed();
            case REQUIRED:
                return status.isRequired();
            case OBSOLETE:
                return status.isObsolete();
            case DEFAULT_VERSION:
                return status.isDefaultVersion();
            case INSTALLED_OR_REQUIRED:
                return status.isInstalledOrRequired();
            case NON_DEPLOYED:
                return status.isNonDeployed();
        }
        return false;
    }

    @Override
    public NDefinitionFilter simplify() {
        return this;
    }

    @Override
    public List<NFilter> getSubFilters() {
        return Collections.emptyList();
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NInstallStatusDefinitionFilter2 that = (NInstallStatusDefinitionFilter2) o;
        return value == that.value && Objects.equals(mode, that.mode)
                ;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!value) {
            sb.append("!");
        }
        sb.append(mode);
        return sb.toString();
    }
}
