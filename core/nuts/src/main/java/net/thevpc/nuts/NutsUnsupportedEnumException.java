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
 * Exception Thrown when for any reason, the enum value is not expected/supported.
 *
 * @author thevpc
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
                        "unexpected/unsupported enum " + enumValue + " of type " + enumValue.getClass().getName())
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
                        "unexpected/unsupported value " + stringValue + " of type " + enumValue.getClass().getName())
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
