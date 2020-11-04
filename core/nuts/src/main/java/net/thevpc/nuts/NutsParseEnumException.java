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
 * Copyright (C) 2016-2020 thevpc
 * <br>
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
 * Exception Thrown when for any reason, the enum value is not expected/supported.
 * @author vpc
 * @category Exception
 */
public class NutsParseEnumException extends NutsException {
    private String invalidValue;
    private Class<? extends Enum> enumType;

    /**
     * create new instance of NutsUnexpectedEnumException
     * @param workspace workspace
     * @param enumType enumeration instance (cannot be null)
     * @param invalidValue invalid value
     */
    public NutsParseEnumException(NutsWorkspace workspace, String invalidValue, Class<? extends Enum> enumType) {
        this(workspace,null,invalidValue, enumType);
    }

    /**
     * create new instance of NutsUnexpectedEnumException
     * @param workspace workspace
     * @param enumType enumeration instance (cannot be null)
     * @param invalidValue invalid value
     * @param message message
     */
    public NutsParseEnumException(NutsWorkspace workspace, String message, String invalidValue, Class<? extends Enum> enumType) {
        super(workspace,
                message == null ? (
                        "Invalid value " + invalidValue + " of type " + enumType.getName())
                        : message
        );
        this.enumType = enumType;
        this.invalidValue= invalidValue;
    }

    /**
     * return invalid value
     * @return invalid value
     */
    public String getInvalidValue() {
        return invalidValue;
    }

    /**
     * enum type
     * @return enum type
     */
    public Class<? extends Enum> getEnumType() {
        return enumType;
    }
}
