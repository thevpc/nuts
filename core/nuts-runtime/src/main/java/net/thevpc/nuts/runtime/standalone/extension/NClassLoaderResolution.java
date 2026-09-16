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
package net.thevpc.nuts.runtime.standalone.extension;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Single resolution abstraction for cross-leaf and cross-composite lookup
 * of classes and resources.
 *
 * @app.category Internal
 */
final class NClassLoaderResolution {

    private NClassLoaderResolution() {
    }

    /**
     * A single fallback tier in cross-leaf/cross-composite resolution.
     *
     * @param <T> resolved entity type
     */
    interface NResolutionTier<T> {
        /**
         * Tier name for diagnostic reporting.
         */
        String name();

        /**
         * Try resolving the requested entity in this tier.
         * Return null / empty list to fall through to the next tier.
         */
        T tryResolve(DefaultNLeafClassLoader requester, String key) throws Exception;
    }

    /**
     * Execution engine for fallback resolution chains.
     */
    static final class NResolutionChain {

        private NResolutionChain() {
        }

        /**
         * Single-result resolution (classes, single resource): first non-null wins.
         */
        static <T> T resolveFirst(DefaultNLeafClassLoader requester, String key, List<NResolutionTier<T>> tiers) throws Exception {
            for (NResolutionTier<T> tier : tiers) {
                T r = tier.tryResolve(requester, key);
                if (r != null) {
                    return r;
                }
            }
            return null;
        }

        /**
         * Multi-result resolution (resources): union across all tiers, deduplicated.
         */
        static <T> List<T> resolveAll(DefaultNLeafClassLoader requester, String key, List<NResolutionTier<List<T>>> tiers) throws Exception {
            List<T> all = new ArrayList<>();
            for (NResolutionTier<List<T>> tier : tiers) {
                List<T> r = tier.tryResolve(requester, key);
                if (r != null && !r.isEmpty()) {
                    all.addAll(r);
                }
            }
            return new ArrayList<>(new LinkedHashSet<>(all));
        }
    }
}
