/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2020 thevpc
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.thevpc.nuts;

/**
 * Exception thrown when extension could not be resolved.
 *
 * @since 0.5.4
 * @category Exception
 */
public class NutsExtensionNotFoundException extends NutsExtensionException {

    /**
     * missing type
     */
    private final Class missingType;

    /**
     * extension name
     */
    private final String extensionName;

    /**
     * Constructs a new NutsExtensionNotFoundException exception
     * @param workspace workspace
     * @param missingType missing type
     * @param extensionName extension name
     */
    public NutsExtensionNotFoundException(NutsWorkspace workspace, Class missingType, String extensionName) {
        super(workspace, null, "Extension " + extensionName + " could ot found. Type " + missingType.getName() + " could not be wired.", null);
        this.missingType = missingType;
        this.extensionName = extensionName;
    }

    /**
     * missing type
     * @return missing type
     */
    public Class getMissingType() {
        return missingType;
    }


    /**
     * extension name
     * @return extension name
     */
    public String getExtensionName() {
        return extensionName;
    }
}
