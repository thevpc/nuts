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
package net.thevpc.nuts.spi;

import net.thevpc.nuts.NSupported;

/**
 * Top Level extension Point in Nuts.
 * Extension mechanism in nuts is based on a factory that selects the best
 * implementation for a given predefined interface (named Extension Point).
 * Such interfaces must extend this {@code NutsComponent} interface.
 * Implementations must implement these extension points by providing their
 * best support level (when method {@link #getSupportLevel(NSupportLevelContext)} is invoked).
 * Only implementations with positive support level are considered.
 * Implementations with higher support level are selected first.
 *
 * @app.category SPI Base
 * @since 0.5.4
 */
public interface NComponent {

    /**
     * evaluate support level (who much this instance should be considered convenient, acceptable)
     * for the given arguments (provided in context).
     *
     * @param context evaluation context
     * @return support level value
     */
    default int getSupportLevel(NSupportLevelContext context) {
        return NSupported.CUSTOM_SUPPORT;
    }
}
