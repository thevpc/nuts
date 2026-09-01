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
import net.thevpc.nuts.io.NPath;
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
     * resolve theme by name or path.
     *
     * when a simple name, it is read from classpath:/META-INF/ntf-themes/*.ntf-theme.
     * Themes loaded by name are cached.
     *
     * when a path (contains separators), it is loaded using NPath
     * Themes loaded by path are not necessarily cached (do not rely on that).
     *
     * when no name or path is passed (null or blank), the default theme is loaded.
     *
     * the default theme can be configured using --theme in nuts bootstrapping. When none is configured,
     * the implementation will create one of its own. That one can even (and usually is) OS dependent.
     * @param name theme name or path
     * @return optional of a theme
     */
    @NGetter
    static NOptional<NTextTheme> of(String name){
        return NTextRPI.of().createThemeByName(name);
    }

    /**
     * resolve theme from path.
     * when null is passed an empty optional is returned.
     *
     * @return optional of the loaded theme
     */
    @NGetter
    static NOptional<NTextTheme> of(NPath path){
        return NTextRPI.of().createThemeByPath(path);
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
