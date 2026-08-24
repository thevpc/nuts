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
 * Copyright [2020] [thevpc] Licensed under the GNU LESSER GENERAL PUBLIC
 * LICENSE Version 3 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * https://www.gnu.org/licenses/lgpl-3.0.en.html Unless required by applicable
 * law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.text;

import java.awt.Image;

/**
 *
 * @author vpc
 */
public interface NTextArtImageRenderer extends NTextArtTextRenderer {

    /**
     * Font name.
     *
     * @param fontName font name
     * @return font name result
     */
    NTextArtImageRenderer fontName(String fontName);

    /**
     * Font size.
     *
     * @param fontSize font size
     * @return font size result
     */
    NTextArtImageRenderer fontSize(int fontSize);

    /**
     * Font italic.
     *
     * @param italic italic
     * @return font italic result
     */
    NTextArtImageRenderer fontItalic(boolean italic);

    /**
     * Font bold.
     *
     * @param bold bold
     * @return font bold result
     */
    NTextArtImageRenderer fontBold(boolean bold);

    /**
     * Output size.
     *
     * @param columns columns
     * @param rows rows
     * @return output size result
     */
    NTextArtImageRenderer outputSize(int columns, int rows);

    /**
     * Output columns.
     *
     * @param columns columns
     * @return output columns result
     */
    NTextArtImageRenderer outputColumns(int columns);

    /**
     * Render.
     *
     * @param image image
     * @return render result
     */
    NText render(Image image);
}
