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
package net.thevpc.nuts.text;

import net.thevpc.nuts.internal.rpi.NTextRPI;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NSetter;

/**
 * NTextTheme interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NTextTheme {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    @NGetter
    static NTextTheme of(){
        return NTextRPI.of().currentTheme();
    }

    /**
     * Sets the set.
     *
     * @param theme theme
     */
    @NSetter
    static void set(NTextTheme theme){
        NTextRPI.of().setTheme(theme);
    }

    /**
     * Sets the set.
     *
     * @param themeName theme name
     */
    @NSetter
    static void set(String themeName){
        NTextRPI.of().setTheme(themeName);
    }

    /**
     * Returns the get.
     *
     * @param name name
     * @return get result
     */
    static NOptional<NTextTheme> get(String name){
        return NTextRPI.of().getTheme(name);
    }

    /**
     * Name.
     *
     * @return name result
     */
    String name();

    /**
     * return styles in terms of color
     *
     * @param style
     * @param basicTrueStyles
     * @return
     */
    NTextStyles toBasicStyles(NTextStyles style, boolean basicTrueStyles);

    /**
     * Converts to basic styles.
     *
     * @param style style
     * @param basicTrueStyles basic true styles
     * @return to basic styles result
     */
    NTextStyles toBasicStyles(NTextStyle style, boolean basicTrueStyles);

}
