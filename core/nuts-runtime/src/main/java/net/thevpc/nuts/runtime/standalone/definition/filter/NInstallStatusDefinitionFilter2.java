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
import net.thevpc.nuts.runtime.standalone.definition.DefaultNInstallInfo;
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
public abstract class NInstallStatusDefinitionFilter2 extends AbstractDefinitionFilter {

    private final String name;
    private final boolean value;


    public static NDefinitionFilter ofInstalled(boolean value) {
        return new NInstallStatusDefinitionFilter2("installed", value) {
            @Override
            public boolean acceptDefinitionImpl(NDefinition definition, NInstallStatus status) {
                return status.isInstalled();
            }
        };
    }

    public static NDefinitionFilter ofRequired(boolean value) {
        return new NInstallStatusDefinitionFilter2("required", value) {
            @Override
            public boolean acceptDefinitionImpl(NDefinition definition, NInstallStatus status) {
                return status.isRequired();
            }
        };
    }

    public static NDefinitionFilter ofObsolete(boolean value) {
        return new NInstallStatusDefinitionFilter2("obsolete", value) {
            @Override
            public boolean acceptDefinitionImpl(NDefinition definition, NInstallStatus status) {
                return status.isObsolete();
            }
        };
    }

    public static NDefinitionFilter ofDefaultVersion(boolean value) {
        return new NInstallStatusDefinitionFilter2("defaultVersion", value) {
            @Override
            public boolean acceptDefinitionImpl(NDefinition definition, NInstallStatus status) {
                return status.isDefaultVersion();
            }
        };
    }

    public static NDefinitionFilter ofDeployed(boolean value) {
        return new NInstallStatusDefinitionFilter2("deployed", value) {
            @Override
            public boolean acceptDefinitionImpl(NDefinition definition, NInstallStatus status) {
                return status.isDeployed();
            }
        };
    }

    public NInstallStatusDefinitionFilter2(String name, boolean value) {
        super(NFilterOp.CUSTOM);
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public boolean isValue() {
        return value;
    }

    public abstract boolean acceptDefinitionImpl(NDefinition definition, NInstallStatus status);

    @Override
    public boolean acceptDefinition(NDefinition definition) {
        NInstalledRepository installedRepository = NWorkspaceExt.of().getInstalledRepository();
        //will always load install information
        NInstallInformation n = installedRepository.getInstallInformation(definition.getId());
        NInstallStatus status = n.getInstallStatus();
        if (status == null) {
            return false;
        }
        return acceptDefinitionImpl(definition, status);
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
        return Objects.hash(name, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NInstallStatusDefinitionFilter2 that = (NInstallStatusDefinitionFilter2) o;
        return value == that.value && Objects.equals(name, that.name)
                ;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!value) {
            sb.append("!");
        }
        sb.append(name);
        return sb.toString();
    }
}
