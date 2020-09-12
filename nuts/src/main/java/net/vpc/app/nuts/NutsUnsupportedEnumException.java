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
 * Exception Thrown when for any reason, the enum value is not expected/supported.
 *
 * @author vpc
 * @category Exception
 */
public class NutsUnsupportedEnumException extends NutsException {
    private Enum enumValue;

    /**
     * create new instance of NutsUnexpectedEnumException
     *
     * @param workspace workspace
     * @param enumValue enumeration instance (cannot be null)
     */
    public NutsUnsupportedEnumException(NutsWorkspace workspace, Enum enumValue) {
        this(workspace, null, enumValue);
    }

    /**
     * create new instance of NutsUnexpectedEnumException
     *
     * @param workspace workspace
     * @param enumValue enumeration instance (cannot be null)
     * @param message   message
     */
    public NutsUnsupportedEnumException(NutsWorkspace workspace, String message, Enum enumValue) {
        super(workspace,
                message != null ? message : (
                        "Unexpected/Unsupported enum " + enumValue + " of type " + enumValue.getClass().getName())
        );
        this.enumValue = enumValue;
    }

    /**
     * create new instance of NutsUnexpectedEnumException
     *
     * @param workspace   workspace
     * @param enumValue   enumeration instance (cannot be null)
     * @param stringValue invalid value
     * @param message message
     */
    public NutsUnsupportedEnumException(NutsWorkspace workspace, String message, String stringValue, Enum enumValue) {
        super(workspace,
                message == null ? (
                        "Unexpected/Unsupported value " + stringValue + " of type " + enumValue.getClass().getName())
                        : message
        );
    }

    /**
     * enum value
     *
     * @return enum value
     */
    public Enum getEnumValue() {
        return enumValue;
    }


}
