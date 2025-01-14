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

import net.thevpc.nuts.NException;
import net.thevpc.nuts.util.NMsg;

/**
 * Exception Thrown when a locked object is invoked.
 *
 * @author thevpc
 * @app.category Exceptions
 */
public class NLockException extends NException {
    /**
     * locked object
     */
    private final Object lockedObject;

    /**
     * lock object
     */
    private final Object lockObject;


    /**
     * Constructs a new ock exception.
     *
     * @param lockedObject locked Object
     * @param lockObject   lock Object
     */
    public NLockException(Object lockedObject, Object lockObject) {
        this(null, lockedObject, lockObject);
    }

    /**
     * Constructs a new ock exception.
     *
     * @param message      message or null
     * @param lockedObject locked Object
     * @param lockObject   lock Object
     */
    public NLockException(NMsg message, Object lockedObject, Object lockObject) {
        super(
                message == null ? NMsg.ofC("item already locked %s", lockedObject)
                        : message
        );
        this.lockedObject = lockedObject;
        this.lockObject = lockObject;
    }

    /**
     * Constructs a new ock exception.
     *
     * @param message      message or null
     * @param lockedObject locked Object
     * @param lockObject   lock Object
     * @param cause        cause
     */
    public NLockException(NMsg message, Object lockedObject, Object lockObject, Throwable cause) {
        super(
                message == null ? NMsg.ofC("item already locked %s", lockedObject)
                        : message, cause
        );
        this.lockedObject = lockedObject;
        this.lockObject = lockObject;
    }

    /**
     * return locked object
     *
     * @return locked object
     */
    public Object getLockedObject() {
        return lockedObject;
    }

    /**
     * return lock object
     *
     * @return lock object
     */
    public Object getLockObject() {
        return lockObject;
    }
}
