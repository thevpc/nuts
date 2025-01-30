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
package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.NId;
import net.thevpc.nuts.NIsolationLevel;
import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NStringUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.locks.Lock;

/**
 * Lock builder to create mainly File based Locks
 *
 * @author thevpc
 * @app.category Input Output
 * @since 0.5.8
 */
public interface NLockBuilder extends NComponent {
    static NLockBuilder of() {
        return NExtensions.of(NLockBuilder.class);
    }

    /**
     * lock source represents a user defined
     * object for which the lock will be created.
     *
     * @return lock source
     */
    Object getSource();

    NLockBuilder setResource(NPath source);

    /**
     * update source
     *
     * @param source source
     * @return {@code this} instance
     */
    NLockBuilder setSource(Object source);

    /**
     * lock resource represents the lock it self.
     * In most cases this will be the lock file.
     *
     * @return lock resource
     */
    Object getResource();

    /**
     * update resource
     *
     * @param source resource
     * @return {@code this} instance
     */
    NLockBuilder setResource(File source);

    /**
     * update resource
     *
     * @param source resource
     * @return {@code this} instance
     */
    NLockBuilder setResource(Path source);

    /**
     * update resource
     *
     * @param source resource
     * @return {@code this} instance
     */
    NLockBuilder setResource(Object source);


    /**
     * create lock object for the given source and resource
     *
     * @return new {@link Lock} instance
     */
    NLock build();


}
