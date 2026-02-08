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

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;

import java.nio.file.LinkOption;

/**
 * Equivalent to FileVisitOption
 *
 * @author thevpc
 * @app.category Base
 * @since 0.8.3
 */
public interface NPathOption {
    /**
     * follow links
     */
    NPathOption FOLLOW_LINKS = NPathStandardOption.FOLLOW_LINKS;
    NPathOption NOFOLLOW_LINKS = NPathStandardOption.NOFOLLOW_LINKS;
    NPathOption REPLACE_EXISTING = NPathStandardOption.REPLACE_EXISTING;
    /**
     * copy attributes to the new file.
     */
    NPathOption COPY_ATTRIBUTES = NPathStandardOption.COPY_ATTRIBUTES;
    NPathOption ATOMIC = NPathStandardOption.ATOMIC;
    NPathOption INTERRUPTIBLE = NPathStandardOption.INTERRUPTIBLE;
    /**
     * operations are implemented in two passes. copy/move will use a temporary file
     */
    NPathOption SAFE = NPathStandardOption.SAFE;

    /**
     * log the copy/move progress
     */
    NPathOption LOG = NPathStandardOption.LOG;
    /**
     * log the copy/move progress
     */
    NPathOption TRACE = NPathStandardOption.TRACE;
    NPathOption READ = NPathStandardOption.READ;

    /**
     * Open for write access.
     */
    NPathOption WRITE = NPathStandardOption.WRITE;

    /**
     * If the file is opened for {@link #WRITE} access then bytes will be written
     * to the end of the file rather than the beginning.
     *
     * <p> If the file is opened for write access by other programs, then it
     * is file system specific if writing to the end of the file is atomic.
     */
    NPathOption APPEND = NPathStandardOption.APPEND;

    /**
     * If the file already exists and It's opened for {@link #WRITE}
     * access, then its length is truncated to 0. This option is ignored
     * if the file is opened only for {@link #READ} access.
     */
    NPathOption TRUNCATE_EXISTING = NPathStandardOption.TRUNCATE_EXISTING;

    /**
     * Create a new file if it does not exist.
     * This option is ignored if the {@link #CREATE_NEW} option is also set.
     * The check for the existence of the file and the creation of the file
     * if it does not exist is atomic with respect to other file system
     * operations.
     */
    NPathOption CREATE = NPathStandardOption.CREATE;

    /**
     * Create a new file, failing if the file already exists.
     * The check for the existence of the file and the creation of the file
     * if it does not exist is atomic with respect to other file system
     * operations.
     */
    NPathOption CREATE_NEW = NPathStandardOption.CREATE_NEW;

    /**
     * Delete on close. When this option is present then the implementation
     * makes a <em>best effort</em> attempt to delete the file when closed
     * by the appropriate {@code close} method. If the {@code close} method is
     * not invoked then a <em>best effort</em> attempt is made to delete the
     * file when the Java virtual machine terminates (either normally, as
     * defined by the Java Language Specification, or where possible, abnormally).
     * This option is primarily intended for use with <em>work files</em> that
     * are used solely by a single instance of the Java virtual machine. This
     * option is not recommended for use when opening files that are open
     * concurrently by other entities. Many of the details as to when and how
     * the file is deleted are implementation specific and therefore not
     * specified. In particular, an implementation may be unable to guarantee
     * that it deletes the expected file when replaced by an attacker while the
     * file is open. Consequently, security sensitive applications should take
     * care when using this option.
     *
     * <p> For security reasons, this option may imply the {@link
     * LinkOption#NOFOLLOW_LINKS} option. In other words, if the option is present
     * when opening an existing file that is a symbolic link then it may fail
     * (by throwing {@link java.io.IOException}).
     */
    NPathOption DELETE_ON_CLOSE = NPathStandardOption.DELETE_ON_CLOSE;

    /**
     * Sparse file. When used with the {@link #CREATE_NEW} option then this
     * option provides a <em>hint</em> that the new file will be sparse. The
     * option is ignored when the file system does not support the creation of
     * sparse files.
     */
    NPathOption SPARSE = NPathStandardOption.SPARSE;

    /**
     * Requires that every update to the file's content or metadata be written
     * synchronously to the underlying storage device.
     *
     * @see <a href="package-summary.html#integrity">Synchronized I/O file integrity</a>
     */
    NPathOption SYNC = NPathStandardOption.SYNC;

    /**
     * Requires that every update to the file's content be written
     * synchronously to the underlying storage device.
     *
     * @see <a href="package-summary.html#integrity">Synchronized I/O file integrity</a>
     */
    NPathOption DSYNC = NPathStandardOption.DSYNC;
    NPathOption NOSHARE_READ = NPathStandardOption.NOSHARE_READ;
    NPathOption NOSHARE_WRITE = NPathStandardOption.NOSHARE_WRITE;
    NPathOption NOSHARE_DELETE = NPathStandardOption.NOSHARE_DELETE;

}
