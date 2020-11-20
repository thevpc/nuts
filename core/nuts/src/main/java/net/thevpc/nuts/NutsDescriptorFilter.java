/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts;

/**
 * Descriptor filter
 *
 * @since 0.5.4
 * %category Descriptor
 */
public interface NutsDescriptorFilter extends NutsArtifactFilter {

    /**
     * return true if descriptor is accepted
     * @param descriptor descriptor
     * @param session session
     * @return true if descriptor is accepted
     */
    boolean acceptDescriptor(NutsDescriptor descriptor, NutsSession session);

    /**
     * default implementation of {@link NutsArtifactFilter}
     * @param sid search id
     * @param session session
     * @return true if accepted
     */
    @Override
    default boolean acceptSearchId(NutsSearchId sid, NutsSession session) {
        return acceptDescriptor(sid.getDescriptor(session), session);
    }

    default NutsDescriptorFilter or(NutsDescriptorFilter other) {
        return or((NutsFilter)other).to(NutsDescriptorFilter.class);
    }

    default NutsDescriptorFilter and(NutsDescriptorFilter other) {
        return and((NutsFilter)other).to(NutsDescriptorFilter.class);
    }

    default NutsDescriptorFilter neg() {
        return NutsArtifactFilter.super.neg().to(NutsDescriptorFilter.class);
    }
}