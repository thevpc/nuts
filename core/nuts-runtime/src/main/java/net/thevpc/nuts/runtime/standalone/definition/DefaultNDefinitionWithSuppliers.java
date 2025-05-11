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

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NCallOnceSupplier;
import net.thevpc.nuts.util.NImmutable;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Created by vpc on 1/6/17.
 */
public class DefaultNDefinitionWithSuppliers implements NDefinition, NImmutable {

    final NCallOnceSupplier<NId> id;
    final NCallOnceSupplier<String> repositoryUuid;
    final NCallOnceSupplier<String> repositoryName;

    final NCallOnceSupplier<NId> apiId;
    final NCallOnceSupplier<NDependency> dependency;


    final NCallOnceSupplier<NDescriptor> descriptor;
    final NCallOnceSupplier<NPath> content;
    final NCallOnceSupplier<NInstallInformation> installInformation;
    final NCallOnceSupplier<NDependencies> dependencies;
    final NCallOnceSupplier<NDescriptor> effectiveDescriptor;
    final NCallOnceSupplier<Set<NDescriptorFlag>> effectiveFlags;



    public DefaultNDefinitionWithSuppliers(Supplier<String> repoUuid, Supplier<String> repoName, Supplier<NId> id, Supplier<NDescriptor> descriptor,
                                           Supplier<NPath> content,
                                           Supplier<NInstallInformation> install,
                                           Supplier<NId> apiId
            , Supplier<NDescriptor> effectiveDescriptor
            , Supplier<NDependencies> dependencies
            , Supplier<NDependency> dependency
            , Supplier<Set<NDescriptorFlag>> effectiveFlags
    ) {
        this.id = new NCallOnceSupplier<>(id);
        this.descriptor = new NCallOnceSupplier<>(descriptor);
        this.content = new NCallOnceSupplier<>(content);
        this.installInformation = new NCallOnceSupplier<>(install);
        this.repositoryUuid = new NCallOnceSupplier<>(repoUuid);
        this.repositoryName = new NCallOnceSupplier<>(repoName);
        this.apiId = new NCallOnceSupplier<>(apiId);
        this.effectiveDescriptor = new NCallOnceSupplier<>(effectiveDescriptor);
        this.dependencies = new NCallOnceSupplier<>(dependencies);
        this.dependency = new NCallOnceSupplier<>(dependency);
        this.effectiveFlags = new NCallOnceSupplier<>(effectiveFlags);
    }

    public NDependency getDependency() {
        return dependency.get();
    }

    @Override
    public String getRepositoryUuid() {
        return repositoryUuid.get();
    }

    @Override
    public String getRepositoryName() {
        return repositoryName.get();
    }

    @Override
    public NDefinitionBuilder builder() {
        return new DefaultNDefinitionBuilder(this);
    }

    @Override
    public NId getId() {
        return id.get();
    }

    public boolean isTemporary() {
        NPath c = content.get();
        return c != null && c.isUserTemporary();
    }

    public NDescriptor getDescriptor() {
        return descriptor.get();
    }

    @Override
    public String toString() {
        return "Definition{"
                + " id=" + id
                + ", content=" + content
                + '}';
    }


    @Override
    public NOptional<NPath> getContent() {
        return NOptional.of(content.get(), () -> NMsg.ofC("content not found for id %s", getId()));
    }

    @Override
    public NOptional<NDescriptor> getEffectiveDescriptor() {
        return NOptional.of(effectiveDescriptor.get(), () -> NMsg.ofC("unable to get effectiveDescriptor for id %s. You need to call search.setEffective(...) first.", getId()));
    }

    @Override
    public NOptional<NInstallInformation> getInstallInformation() {
        return NOptional.of(installInformation.get(), () -> NMsg.ofC("unable to get install information for id %s.", getId()));
    }

    @Override
    public NOptional<NDependencies> getDependencies() {
        return NOptional.of(dependencies.get(), () -> NMsg.ofC("unable to get dependencies for id %s. You need to call search.setDependencies(...) first.", getId()));
    }

    @Override
    public NOptional<Set<NDescriptorFlag>> getEffectiveFlags() {
        return NOptional.of(effectiveFlags.get(), () -> NMsg.ofC("unable to get effectiveFlags for id %s.", getId()));
    }

    @Override
    public int compareTo(NDefinition n2) {
        if (n2 == null) {
            return 1;
        }
        if (!(n2 instanceof DefaultNDefinitionWithSuppliers)) {
            return -1;
        }
        NId o1 = getId();
        NId o2 = n2.getId();
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
        final DefaultNDefinitionWithSuppliers other = (DefaultNDefinitionWithSuppliers) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }


    @Override
    public NId getApiId() {
        return apiId.get();
    }


}
