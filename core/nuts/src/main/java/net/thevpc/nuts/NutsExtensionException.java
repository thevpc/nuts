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
 *
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

/**
 * Base exception for Extension related exceptions
 *
 * @since 0.5.4
 * %category Exception
 */
public abstract class NutsExtensionException extends NutsException {

    /**
     * id
     */
    private final String id;

    /**
     * Constructs a new runtime exception with the specified detail message and
     * cause.
     * <br>
     * Note that the detail message associated with
     * {@code cause} is <i>not</i> automatically incorporated in
     * this runtime exception's detail message.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method). if the message is null, a
     *         default one is provided
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     * @param workspace the workspace of this Nuts Exception
     * @param extensionId extension id
     */
    public NutsExtensionException(NutsWorkspace workspace, String extensionId, String message, Throwable cause) {
        super(workspace,
                PrivateNutsUtils.isBlank(message)
                        ? ("extension " + (extensionId == null ? "<null>" : extensionId) + " has encountered problem") : message, cause);
        this.id = extensionId;
    }

    /**
     * extension id
     * @return extension id
     */
    public String getId() {
        return id;
    }
}
