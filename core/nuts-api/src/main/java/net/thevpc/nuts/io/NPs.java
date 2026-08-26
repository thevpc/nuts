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
package net.thevpc.nuts.io;

import net.thevpc.nuts.platform.NConnectionStringAware;
import net.thevpc.nuts.platform.NExecutionEngineFamily;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.pipeline.NStream;
import net.thevpc.nuts.spi.NComponent;

/**
 * I/O Action that help monitoring processes
 *
 * @author thevpc
 * @app.category Toolkit
 * @since 0.5.8
 */
public interface NPs extends NComponent, NConnectionStringAware {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NPs of() {
        return NExtensions.of(NPs.class);
    }

    /**
     * process type to consider. Supported 'java'
     *
     * @return process type to consider. Supported 'java'
     */
    NExecutionEngineFamily platformFamily();

    /**
     * set process type to consider.
     * Supported 'java' or 'java#version'
     *
     * @param processType new type
     * @return return {@code this} instance
     */
    NPs platformFamily(NExecutionEngineFamily processType);

    /**
     * list all processes of type {@link #platformFamily()}
     *
     * @return list all processes of type {@link #platformFamily()}
     */
    NStream<NPsInfo> getResultList();

    /**
     * return true if fail fast.
     * When fail fast flag is armed, the first
     * error that occurs will throw an {@link java.io.UncheckedIOException}
     *
     * @return true if fail fast
     */
    boolean isFailFast();

    /**
     * Checks if is supported kill process.
     *
     * @return is supported kill process result
     */
    boolean isSupportedKillProcess();

    /**
     * Kill process.
     *
     * @param processId process id
     * @return kill process result
     */
    boolean killProcess(String processId);

    /**
     * update fail fast flag
     *
     * @param failFast value
     * @return {@code this} instance
     */
    NPs failFast(boolean failFast);


    /**
     * update host connection string. when host is not blank, this connection
     * string will be used to connect to a remote host for execution
     *
     * @param host host
     * @return {@code this} instance
     */
    @Override
    NPs connectionString(String host);

    /**
     * update host connection string. when host is not blank, this connection
     * string will be used to connect to a remote host for execution
     *
     * @param host host as connection string
     * @return {@code this} instance
     */
    @Override
    NPs connectionString(NConnectionString host);

    /**
     * update host connection string. when host is not blank, this connection
     * string will be used to connect to a remote host for execution
     *
     * @param host host as connection string
     * @return {@code this} instance
     */
    @Override
    NPs at(String host);

    /**
     * update host connection string. when host is not blank, this connection
     * string will be used to connect to a remote host for execution
     *
     * @param host host as connection string
     * @return {@code this} instance
     */
    @Override
    NPs at(NConnectionString host);

}
