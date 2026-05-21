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
package net.thevpc.nuts.spi;

import net.thevpc.nuts.core.NRepositorySpec;

import java.util.*;
import java.util.stream.Collectors;

public class NRepositorySelectorList {

    private final List<NRepositorySelector> selectors = new ArrayList<>();

    public NRepositorySelectorList() {
    }

    public NRepositorySelectorList(NRepositorySelector[] a) {
        for (NRepositorySelector repoDefString : a) {
            if (repoDefString != null) {
                selectors.add(repoDefString);
            }
        }
    }

    public List<NRepositorySelector> selectors() {
        return Collections.unmodifiableList(selectors);
    }

    public NRepositorySelectorList merge(NRepositorySelectorList other) {
        if (other == null || other.selectors.isEmpty()) {
            return this;
        }
        List<NRepositorySelector> result = new ArrayList<>();
        result.addAll(selectors);
        result.addAll(other.selectors);
        return new NRepositorySelectorList(result.toArray(new NRepositorySelector[0]));
    }

    public boolean acceptExisting(NRepositorySpec location) {
        boolean includeOthers = true;
        for (NRepositorySelector s : selectors) {
            if (s.matches(location.sourceLocation())) {
                switch (s.op()) {
                    case EXACT:
                    case INCLUDE:
                        return true;
                    case EXCLUDE:
                        return false;
                }
            }
            if (s.op() == NSelectorOp.EXACT) {
                includeOthers = false;
            }
        }
        return includeOthers;
    }

    public NRepositorySelector[] toArray() {
        return selectors.toArray(new NRepositorySelector[0]);
    }


    @Override
    public String toString() {
        return selectors.stream().map(x -> String.valueOf(x)).collect(Collectors.joining(","));
    }
}
