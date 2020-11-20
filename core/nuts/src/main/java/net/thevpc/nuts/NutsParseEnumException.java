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
 * @author vpc
 * %category Exception
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
