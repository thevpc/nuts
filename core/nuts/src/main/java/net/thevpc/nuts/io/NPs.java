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
package net.thevpc.nuts.io;

import net.thevpc.nuts.NExtensions;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NSessionProvider;
import net.thevpc.nuts.util.NStream;
import net.thevpc.nuts.spi.NComponent;

/**
 * I/O Action that help monitoring processes
 *
 * @author thevpc
 * @app.category Toolkit
 * @since 0.5.8
 */
public interface NPs extends NComponent, NSessionProvider {
    static NPs of(NSession session) {
        return NExtensions.of(session).createComponent(NPs.class).get();
    }

    /**
     * process type to consider. Supported 'java'
     *
     * @return process type to consider. Supported 'java'
     */
    String getType();

    /**
     * set process type to consider.
     * Supported 'java' or 'java#version'
     *
     * @param processType new type
     * @return return {@code this} instance
     */
    NPs setType(String processType);

    /**
     * set process type to consider.
     * Supported 'java' or 'java#version'
     *
     * @param processType new type
     * @return return {@code this} instance
     */
    NPs type(String processType);

    /**
     * list all processes of type {@link #getType()}
     *
     * @return list all processes of type {@link #getType()}
     */
    NStream<NPsInfo> getResultList();

    /**
     * update session
     *
     * @param session session
     * @return {@code this} instance
     */
    NPs setSession(NSession session);

    /**
     * return true if fail fast.
     * When fail fast flag is armed, the first
     * error that occurs will throw an {@link java.io.UncheckedIOException}
     *
     * @return true if fail fast
     */
    boolean isFailFast();

    /**
     * update fail fast flag
     *
     * @param failFast value
     * @return {@code this} instance
     */
    NPs setFailFast(boolean failFast);

    boolean isSupportedKillProcess();

    boolean killProcess(String processId);

    /**
     * update fail fast flag
     *
     * @param failFast value
     * @return {@code this} instance
     */
    NPs failFast(boolean failFast);

    /**
     * set fail fast flag
     *
     * @return {@code this} instance
     */
    NPs failFast();
}
