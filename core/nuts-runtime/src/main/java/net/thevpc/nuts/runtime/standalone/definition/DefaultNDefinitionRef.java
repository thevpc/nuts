/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.definition;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.io.cache.CachedSupplier;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Created by vpc on 1/6/17.
 */
public class DefaultNDefinitionRef implements NDefinition {

    private NId id;
    private CachedSupplier<NDescriptor> descriptor;
    private CachedSupplier<String> repositoryUuid;
    private CachedSupplier<String> repositoryName;

    private CachedSupplier<NPath> content;
    private CachedSupplier<NInstallInformation> installInformation;
    private CachedSupplier<NDependencies> dependencies;
    private CachedSupplier<NDescriptor> effectiveDescriptor;
    private CachedSupplier<NId> apiId = null;
    private transient NSession session;

    public DefaultNDefinitionRef() {
    }

    public DefaultNDefinitionRef(CachedSupplier<String> repositoryUuid, CachedSupplier<String> repositoryName, NId id, CachedSupplier<NDescriptor> descriptor, CachedSupplier<NPath> content,
                                 CachedSupplier<NInstallInformation> install,
                                 CachedSupplier<NId> apiId,
                                 CachedSupplier<NDependencies> dependencies,
                                 CachedSupplier<NDescriptor> effectiveDescriptor,
                                 NSession session) {
        this.descriptor = NAssert.requireNonNull(descriptor, "descriptor", session);
        this.content = NAssert.requireNonNull(content, "content", session);
        this.id = NAssert.requireNonNull(id, "id", session);
        if (!id.isLongId()) {
            throw new NIllegalArgumentException(session, NMsg.ofC("id should not have query defined in descriptors : %s", id));
        }
        this.installInformation = NAssert.requireNonNull(install, "installInformation", session);
        this.repositoryUuid = NAssert.requireNonNull(repositoryUuid, "repositoryUuid", session);
        this.repositoryName = NAssert.requireNonNull(repositoryName, "repositoryName", session);
        this.dependencies = NAssert.requireNonNull(dependencies, "dependencies", session);
        this.effectiveDescriptor = NAssert.requireNonNull(effectiveDescriptor, "effectiveDescriptor", session);
        this.apiId = NAssert.requireNonNull(apiId, "apiId", session);
        this.session = session;
    }

    @Override
    public String getRepositoryUuid() {
        return repositoryUuid.getValue();
    }

    @Override
    public String getRepositoryName() {
        return repositoryName.getValue();
    }

    @Override
    public NId getId() {
        return id;
    }

    public boolean isTemporary() {
        NPath c = content.getValue();
        return c != null && c.isUserTemporary();
    }

    public NDescriptor getDescriptor() {
        return descriptor.getValue();
    }

    @Override
    public String toString() {
        return "Definition{"
                + " id=" + id
                + ", content=" + content
                + '}';
    }

    public DefaultNDefinition copy() {
        return new DefaultNDefinition(this, session);
    }

    @Override
    public NOptional<NPath> getContent() {
        NPath c = content.getValue();
        return NOptional.of(c, s -> NMsg.ofC("content not found for id %s", getId()))
                .setSession(session);
    }

    @Override
    public NOptional<NDescriptor> getEffectiveDescriptor() {
        NDescriptor c = effectiveDescriptor.getValue();
        return NOptional.of(c, s -> NMsg.ofC("unable to get effectiveDescriptor for id %s. You need to call search.setEffective(...) first.", getId()))
                .setSession(session);
    }

    @Override
    public NOptional<NInstallInformation> getInstallInformation() {
        return NOptional.of(installInformation.getValue(), s -> NMsg.ofC("unable to get install information for id %s.", getId()))
                .setSession(session);
    }

    @Override
    public NOptional<NDependencies> getDependencies() {
        return NOptional.of(dependencies.getValue(), s -> NMsg.ofC("unable to get dependencies for id %s. You need to call search.setDependencies(...) first.", getId()))
                .setSession(session);
    }

    @Override
    public int compareTo(NDefinition n2) {
        if (n2 == null) {
            return 1;
        }
        if (!(n2 instanceof DefaultNDefinitionRef)) {
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
        final DefaultNDefinitionRef other = (DefaultNDefinitionRef) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public NId getApiId() {
        return apiId.getValue();
    }
}
