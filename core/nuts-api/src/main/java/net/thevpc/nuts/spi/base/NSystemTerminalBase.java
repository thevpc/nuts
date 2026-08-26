/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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
 * <br> ====================================================================
 */
package net.thevpc.nuts.spi.base;

import net.thevpc.nuts.cmdline.NArgCompleteResolver;
import net.thevpc.nuts.cmdline.NCmdLineHistory;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminalFormatter;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.text.NTerminalCmd;
import net.thevpc.nuts.text.NTextStyles;
import net.thevpc.nuts.text.NMsg;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Objects;

/**
 * System Terminal defines all low level term interaction
 *
 * @author thevpc
 * @app.category SPI Base
 * @since 0.5.4
 */
public interface NSystemTerminalBase extends NComponent {
    /**
     * Read line.
     *
     * @param out out
     * @param message message
     * @return read line result
     */
    String readLine(NPrintStream out, NMsg message);

    /**
     * Read password.
     *
     * @param out out
     * @param message message
     * @return read password result
     */
    char[] readPassword(NPrintStream out, NMsg message);

    /**
     * In.
     *
     * @return in result
     */
    InputStream in();

    /**
     * Out.
     *
     * @return out result
     */
    NPrintStream out();

    /**
     * Err.
     *
     * @return err result
     */
    NPrintStream err();

    /**
     * Auto complete resolver.
     *
     * @return auto complete resolver result
     */
    default NArgCompleteResolver autoCompleteResolver() {
        return null;
    }

    /**
     * Checks if is auto complete supported.
     *
     * @return is auto complete supported result
     */
    default boolean isAutoCompleteSupported() {
        return false;
    }

    /**
     * Command auto complete resolver.
     *
     * @param autoCompleteResolver auto complete resolver
     * @return command auto complete resolver result
     */
    NSystemTerminalBase commandAutoCompleteResolver(NArgCompleteResolver autoCompleteResolver);

    /**
     * return History implementation
     *
     * @return History implementation
     */
    NCmdLineHistory commandHistory();

    /**
     * set History implementation
     *
     * @param history new history implementation
     * @return {@code this} instance
     */
    NSystemTerminalBase commandHistory(NCmdLineHistory history);

    /**
     * return command line language content type (or simple id) used for highlighting (syntax coloring).
     * when this returns blank, nuts uses 'system' which refers to the system shell highlighter
     *
     * @return command line language content type (or simple id) used for highlighting (syntax coloring)
     * @since 0.8.3
     */
    NTerminalFormatter commandHighlighter();

    /**
     * set command line language content type (or simple id) used for highlighting (syntax coloring).
     * when {@code commandContentType} is blank, nuts uses 'system' which refers to the system shell highlighter
     *
     * @param commandHighlighter commandContentType
     * @return {@code this} instance
     * @since 0.8.3
     */
    NSystemTerminalBase commandHighlighter(NTerminalFormatter commandHighlighter);

    /**
     * Run.
     *
     * @param command command
     * @param printStream print stream
     * @return run result
     */
    Object run(NTerminalCmd command, NPrintStream printStream);

    /**
     * Terminal cursor.
     *
     * @return terminal cursor result
     */
    Cursor terminalCursor();

    /**
     * Terminal size.
     *
     * @return terminal size result
     */
    Size terminalSize();

    /**
     * Reset line.
     *
     * @return reset line result
     */
    NSystemTerminalBase resetLine();

    /**
     * Clear screen.
     *
     * @return clear screen result
     */
    NSystemTerminalBase clearScreen();

    /**
     * Styles.
     *
     * @param styles styles
     * @param printStream print stream
     */
    void styles(NTextStyles styles, NPrintStream printStream);

    class Cursor implements Serializable {
        private int x;
        private int y;

        /**
         * Cursor.
         *
         * @return cursor result
         */
        private Cursor() {
            // for serialization purposes
        }

        /**
         * Cursor.
         *
         * @param x x
         * @param y y
         * @return cursor result
         */
        public Cursor(int x, int y) {
            this.x = x;
            this.y = y;
        }

        /**
         * X.
         *
         * @return x result
         */
        public int x() {
            return x;
        }

        /**
         * Y.
         *
         * @return y result
         */
        public int y() {
            return y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Cursor cursor = (Cursor) o;
            return x == cursor.x && y == cursor.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        @Override
        public String toString() {
            return "Cursor{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }
    }

    class Size implements Serializable {
        private int columns;
        private int rows;

        /**
         * Size.
         *
         * @return size result
         */
        private Size() {
            // for serialization purposes
        }

        /**
         * Size.
         *
         * @param columns columns
         * @param rows rows
         * @return size result
         */
        public Size(int columns, int rows) {
            this.columns = columns;
            this.rows = rows;
        }

        /**
         * Columns.
         *
         * @return columns result
         */
        public int columns() {
            return columns;
        }

        /**
         * Rows.
         *
         * @return rows result
         */
        public int rows() {
            return rows;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Cursor cursor = (Cursor) o;
            return columns == cursor.x && rows == cursor.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(columns, rows);
        }

        @Override
        public String toString() {
            return "Cursor{" +
                    "x=" + columns +
                    ", y=" + rows +
                    '}';
        }
    }

}
