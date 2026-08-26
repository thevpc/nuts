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
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.definition;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.command.NInstallInformation;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.util.NImmutable;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;

/**
 * Created by vpc on 1/6/17.
 */
@NImmutable
public class DefaultNDefinition implements NDefinition{

    private final NId id;
    private final NDescriptor descriptor;
    private final String repositoryUuid;
    private final String repositoryName;

    private final NPath content;
    private final NInstallInformation installInformation;
    private final NDependencies dependencies;
    private final NDescriptor effectiveDescriptor;
    private final NId apiId;
    private final NDependency dependency;
    private final Set<NDescriptorFlag> effectiveFlags;

    public DefaultNDefinition() {
        this.descriptor = null;
        this.id = null;
        this.repositoryUuid = null;
        this.repositoryName = null;
        this.content = null;
        this.installInformation = null;
        this.effectiveDescriptor = null;
        this.dependencies = null;
        this.apiId = null;
        this.dependency = null;
        this.effectiveFlags = Collections.emptySet();
    }

    public DefaultNDefinition(String repoUuid, String repoName, NId id, NDescriptor descriptor, NPath content, NInstallInformation install, NId apiId
            , NDescriptor effectiveDescriptor
            , NDependencies dependencies
            , NDependency dependency
            , Set<NDescriptorFlag> effectiveFlags
    ) {
        this.id = id;
        this.descriptor = descriptor;
        this.content = content;
        if (!id.isLongId()) {
            throw new NIllegalArgumentException(NMsg.ofC("id should not have query defined in descriptors : %s", id));
        }
        this.installInformation = install;
        this.repositoryUuid = repoUuid;
        this.repositoryName = repoName;
        this.apiId = apiId;
        this.effectiveDescriptor = effectiveDescriptor;
        this.dependencies = dependencies;
        this.dependency = dependency;
        this.effectiveFlags = effectiveFlags==null?null:Collections.unmodifiableSet(new HashSet<>(effectiveFlags));
    }

    public DefaultNDefinition(NDefinition other) {
        if (other != null) {
            this.descriptor = other.descriptor();
            this.id = other.id();
            this.repositoryUuid = other.repositoryUuid();
            this.repositoryName = other.repositoryName();

            this.content = other.content().orNull();
            this.installInformation = other.installInformation().orNull();
            this.effectiveDescriptor = other.effectiveDescriptor().orNull();
            this.dependencies = other.dependencies().orNull();
            this.apiId = other.apiId();
            this.dependency = other.dependency();
            this.effectiveFlags = other.effectiveFlags().orNull();
        } else {
            this.descriptor = null;
            this.id = null;
            this.repositoryUuid = null;
            this.repositoryName = null;
            this.content = null;
            this.installInformation = null;
            this.effectiveDescriptor = null;
            this.dependencies = null;
            this.apiId = null;
            this.dependency = null;
            this.effectiveFlags = null;
        }
    }

    public NDependency dependency() {
        return dependency;
    }

    @Override
    public String repositoryUuid() {
        return repositoryUuid;
    }

    @Override
    public String repositoryName() {
        return repositoryName;
    }

    @Override
    public NDefinitionBuilder builder() {
        return new DefaultNDefinitionBuilder(this);
    }

    @Override
    public NId id() {
        return id;
    }

    public boolean isTemporary() {
        return content != null && content.isUserTemporary();
    }

    public NDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public String toString() {
        return "Definition{"
                + " id=" + id
                + ", content=" + content
                + '}';
    }


    @Override
    public NOptional<NPath> content() {
        return NOptional.of(content, () -> NMsg.ofC("content not found for id %s", id()));
    }

    @Override
    public NOptional<NDescriptor> effectiveDescriptor() {
        return NOptional.of(effectiveDescriptor, () -> NMsg.ofC("unable to get effectiveDescriptor for id %s. You need to call search.setEffective(...) first.", id()));
    }

    @Override
    public NOptional<NInstallInformation> installInformation() {
        return NOptional.of(installInformation, () -> NMsg.ofC("unable to get install information for id %s.", id()));
    }

    @Override
    public NOptional<NDependencies> dependencies() {
        return NOptional.of(dependencies, () -> NMsg.ofC("unable to get dependencies for id %s. You need to call search.setDependencies(...) first.", id()));
    }

    @Override
    public NOptional<Set<NDescriptorFlag>> effectiveFlags() {
        return NOptional.of(effectiveFlags, () -> NMsg.ofC("unable to get effectiveFlags", id()));
    }

    @Override
    public int compareTo(NDefinition n2) {
        if (n2 == null) {
            return 1;
        }
        if (!(n2 instanceof DefaultNDefinition)) {
            return -1;
        }
        NId o1 = id();
        NId o2 = n2.id();
        if (o1 == null || o2 == null) {
            if (o1 == o2) {
                return 0;
            }
            if (o1 == null) {
                return -1;
            }
            return 1;
        }
        return o1.compareTo(o2);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DefaultNDefinition other = (DefaultNDefinition) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }


    @Override
    public NId apiId() {
        return apiId;
    }


}
