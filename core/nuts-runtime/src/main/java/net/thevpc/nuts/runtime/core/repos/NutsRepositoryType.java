/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
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
package net.thevpc.nuts.runtime.core.repos;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author vpc
 */
public class NutsRepositoryType {

    private Set<String> repoProtocols = new LinkedHashSet<String>();

    public NutsRepositoryType(String... types) {
        if (types != null) {
            for (String t : types) {
                if (t != null) {
                    for (String i : t.split("[+]")) {
                        i = i.trim();
                        if (i.length() > 0) {
                            repoProtocols.add(i);
                        }
                    }
                }
            }
        }
    }

    public boolean contains(String desc) {
        return repoProtocols.contains(desc);
    }

    public boolean isEmpty() {
        return repoProtocols.isEmpty();
    }

    public Set<String> getProtocols() {
        return Collections.unmodifiableSet(repoProtocols);
    }

    public boolean isNuts() {
        return repoProtocols.contains("nuts");
    }

    public boolean isMaven() {
        return repoProtocols.contains("maven");
    }

    @Override
    public String toString() {
        return String.join("+", repoProtocols);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + Objects.hashCode(this.repoProtocols);
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
        final NutsRepositoryType other = (NutsRepositoryType) obj;
        if (!Objects.equals(this.repoProtocols, other.repoProtocols)) {
            return false;
        }
        return true;
    }

}
