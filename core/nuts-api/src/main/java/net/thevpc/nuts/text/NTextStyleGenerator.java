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

/**
 * NTextStyleGenerator interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NTextStyleGenerator {
    /**
     * Checks if hash.
     *
     * @param i i
     * @return hash result
     */
    NTextStyles hash(Object i);

    /**
     * Checks if hash.
     *
     * @param i i
     * @return hash result
     */
    NTextStyles hash(int i);

    /**
     * Random.
     *
     * @return random result
     */
    NTextStyles random();

    /**
     * Checks if is include plain.
     *
     * @return is include plain result
     */
    boolean isIncludePlain();

    /**
     * Include plain.
     *
     * @param includePlain include plain
     * @return include plain result
     */
    NTextStyleGenerator includePlain(boolean includePlain);

    /**
     * Checks if is include bold.
     *
     * @return is include bold result
     */
    boolean isIncludeBold();

    /**
     * Include bold.
     *
     * @param includeBold include bold
     * @return include bold result
     */
    NTextStyleGenerator includeBold(boolean includeBold);

    /**
     * Checks if is include blink.
     *
     * @return is include blink result
     */
    boolean isIncludeBlink();

    /**
     * Include blink.
     *
     * @param includeBlink include blink
     * @return include blink result
     */
    NTextStyleGenerator includeBlink(boolean includeBlink);

    /**
     * Checks if is include reversed.
     *
     * @return is include reversed result
     */
    boolean isIncludeReversed();

    /**
     * Include reversed.
     *
     * @param includeReversed include reversed
     * @return include reversed result
     */
    NTextStyleGenerator includeReversed(boolean includeReversed);

    /**
     * Checks if is include italic.
     *
     * @return is include italic result
     */
    boolean isIncludeItalic();

    /**
     * Include italic.
     *
     * @param includeItalic include italic
     * @return include italic result
     */
    NTextStyleGenerator includeItalic(boolean includeItalic);

    /**
     * Checks if is include underlined.
     *
     * @return is include underlined result
     */
    boolean isIncludeUnderlined();

    /**
     * Include underlined.
     *
     * @param includeUnderlined include underlined
     * @return include underlined result
     */
    NTextStyleGenerator includeUnderlined(boolean includeUnderlined);

    /**
     * Checks if is include striked.
     *
     * @return is include striked result
     */
    boolean isIncludeStriked();

    /**
     * Include striked.
     *
     * @param includeStriked include striked
     * @return include striked result
     */
    NTextStyleGenerator includeStriked(boolean includeStriked);

    /**
     * Checks if is include foreground.
     *
     * @return is include foreground result
     */
    boolean isIncludeForeground();

    /**
     * Include foreground.
     *
     * @param includeForeground include foreground
     * @return include foreground result
     */
    NTextStyleGenerator includeForeground(boolean includeForeground);

    /**
     * Checks if is include background.
     *
     * @return is include background result
     */
    boolean isIncludeBackground();

    /**
     * Include background.
     *
     * @param includeBackground include background
     * @return include background result
     */
    NTextStyleGenerator includeBackground(boolean includeBackground);

    /**
     * Checks if is use theme colors.
     *
     * @return is use theme colors result
     */
    boolean isUseThemeColors();

    /**
     * Checks if is use palette colors.
     *
     * @return is use palette colors result
     */
    boolean isUsePaletteColors();

    /**
     * Checks if is use true colors.
     *
     * @return is use true colors result
     */
    boolean isUseTrueColors();

    /**
     * Use theme colors.
     *
     * @return use theme colors result
     */
    NTextStyleGenerator useThemeColors();

    /**
     * Use palette colors.
     *
     * @return use palette colors result
     */
    NTextStyleGenerator usePaletteColors();

    /**
     * Use true colors.
     *
     * @return use true colors result
     */
    NTextStyleGenerator useTrueColors();
}
