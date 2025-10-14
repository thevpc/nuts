/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]  
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License"); 
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.util;

/**
 * Defines the possible states of an {@link NOptional} instance.
 * Unlike Java's {@code Optional} (which is bi-state: absent or present),
 * {@code NOptional} is tri-state:
 * <ul>
 * <li>{@link #EMPTY}: The value is logically absent (e.g., not found, not set).</li>
 * <li>{@link #PRESENT}: A value is held by the optional (which may itself be {@code null}).</li>
 * <li>{@link #ERROR}: A technical or logical failure occurred during evaluation.</li>
 * </ul>
 *
 * @author vpc
 */
public enum NOptionalType implements NEnum{
    /**
     * Indicates that the optional holds no value. This state is typically
     * reached when a value is logically missing or when an operation like
     * {@code NOptional.of(null)} or {@code NOptional.filter()} results in absence.
     */
    EMPTY,
    /**
     * Indicates that the optional holds a valid value. The held value may be
     * non-null or explicitly {@code null} (when created via
     * {@code NOptional.ofNullable(null)}).
     */
    PRESENT,
    /**
     * Indicates that the optional is in a failure state, meaning an attempt to
     * retrieve the value resulted in an error (e.g., an exception during a lazy
     * evaluation or an explicit call to {@code NOptional.ofError()}).
     */
    ERROR;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    /**
     * default constructor
     */
    NOptionalType() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    /**
     * Parses the given string value into an {@code NOptional<NOptionalType>}.
     * The parsing is generally case-insensitive and respects standard {@code NEnum}
     * naming conventions (like lower-cased identifiers).
     *
     * @param value the string representation of the enum entry
     * @return an {@code NOptional} wrapping the parsed {@code NOptionalType}, or empty if parsing fails
     */
    public static NOptional<NOptionalType> parse(String value) {
        return NEnumUtils.parseEnum(value, NOptionalType.class);
    }

    /**
     * Returns the lower-cased identifier for the enum entry.
     * This ID is typically used in serialization or configuration files.
     *
     * @return lower cased identifier
     */
    public String id() {
        return id;
    }
}
