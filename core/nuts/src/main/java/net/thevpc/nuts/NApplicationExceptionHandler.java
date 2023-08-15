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
package net.thevpc.nuts;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;

/**
 * This interface defines how exit application exceptions/throwables are handled
 */
public interface NApplicationExceptionHandler extends NComponent {
    /**
     * return a new instance of {@code NApplicationExceptionHandler}
     * @param session session
     * @return a new instance of {@code NApplicationExceptionHandler}
     */
    static NApplicationExceptionHandler of(NSession session) {
       return NExtensions.of(session).createComponent(NApplicationExceptionHandler.class).get();
    }

    /**
     * defines how exit application exceptions/throwables are handled
     *
     * @param args application arguments
     * @param throwable application exception
     * @param session session
     * @return return exit code
     */
    int processThrowable(String[] args, Throwable throwable);
}
