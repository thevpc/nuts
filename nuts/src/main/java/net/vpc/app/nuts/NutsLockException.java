/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2019 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

/**
 * Exception Thrown when a locked object is invoked.
 * @author vpc
 * @category Exception
 */
public class NutsLockException extends NutsException {
    /**
     * locked object
     */
    private Object lockedObject;

    /**
     * lock object
     */
    private Object lockObject;


    /**
     * Constructs a new ock exception.
     * @param workspace workspace
     * @param lockedObject locked Object
     * @param lockObject lock Object
     */
    public NutsLockException(NutsWorkspace workspace, Object lockedObject, Object lockObject) {
        this(workspace,null,lockedObject,lockObject);
    }

    /**
     * Constructs a new ock exception.
     * @param workspace workspace
     * @param message message or null
     * @param lockedObject locked Object
     * @param lockObject lock Object
     */
    public NutsLockException(NutsWorkspace workspace, String message, Object lockedObject, Object lockObject) {
        super(workspace,
                message == null ? ("Item Already Locked" + lockedObject)
                        : message
        );
        this.lockedObject=lockedObject;
        this.lockObject=lockObject;
    }

    /**
     * Constructs a new ock exception.
     * @param workspace workspace
     * @param message message or null
     * @param lockedObject locked Object
     * @param lockObject lock Object
     * @param cause cause
     */
    public NutsLockException(NutsWorkspace workspace, String message, Object lockedObject, Object lockObject,Throwable cause) {
        super(workspace,
                message == null ? ("Item Already Locked" + lockedObject)
                        : message,cause
        );
        this.lockedObject=lockedObject;
        this.lockObject=lockObject;
    }

    /**
     * return locked object
     * @return locked object
     */
    public Object getLockedObject() {
        return lockedObject;
    }

    /**
     * return lock object
     * @return lock object
     */
    public Object getLockObject() {
        return lockObject;
    }
}
