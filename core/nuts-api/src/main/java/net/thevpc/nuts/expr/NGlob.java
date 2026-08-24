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
 *
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
package net.thevpc.nuts.expr;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;

import java.util.regex.Pattern;

/**
 * NGlob interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NGlob extends NComponent {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NGlob of() {
        return NExtensions.of(NGlob.class);
    }

    /**
     * Separator.
     *
     * @return separator result
     */
    String separator();

    /**
     * Separator.
     *
     * @param c c
     * @return separator result
     */
    NGlob separator(String c);

    /**
     * Checks if is glob.
     *
     * @param pattern pattern
     * @return is glob result
     */
    boolean isGlob(String pattern);

    /**
     * Converts to pattern.
     *
     * @param pattern pattern
     * @return to pattern result
     */
    Pattern toPattern(String pattern);

    /**
     * Converts to pattern string.
     *
     * @param pattern pattern
     * @return to pattern string result
     */
    String toPatternString(String pattern);

    /**
     * Escape.
     *
     * @param s s
     * @return escape result
     */
    String escape(String s);
}
