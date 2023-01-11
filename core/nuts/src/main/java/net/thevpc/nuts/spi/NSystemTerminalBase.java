/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.spi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCommandAutoCompleteResolver;
import net.thevpc.nuts.cmdline.NCommandHistory;
import net.thevpc.nuts.io.NOutputStream;
import net.thevpc.nuts.text.NTerminalCommand;
import net.thevpc.nuts.text.NTextStyles;

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
    String readLine(NOutputStream out, NMsg message, NSession session);

    char[] readPassword(NOutputStream out, NMsg message, NSession session);

    InputStream getIn();

    NOutputStream getOut();

    NOutputStream getErr();

    default NCommandAutoCompleteResolver getAutoCompleteResolver() {
        return null;
    }

    default boolean isAutoCompleteSupported() {
        return false;
    }

    NSystemTerminalBase setCommandAutoCompleteResolver(NCommandAutoCompleteResolver autoCompleteResolver);

    /**
     * return History implementation
     *
     * @return History implementation
     */
    NCommandHistory getCommandHistory();

    /**
     * set History implementation
     *
     * @param history new history implementation
     * @return {@code this} instance
     */
    NSystemTerminalBase setCommandHistory(NCommandHistory history);

    /**
     * return command line language content type (or simple id) used for highlighting (syntax coloring).
     * when this returns blank, nuts uses 'system' which refers to the system shell highlighter
     *
     * @return command line language content type (or simple id) used for highlighting (syntax coloring)
     * @since 0.8.3
     */
    String getCommandHighlighter();

    /**
     * set command line language content type (or simple id) used for highlighting (syntax coloring).
     * when {@code commandContentType} is blank, nuts uses 'system' which refers to the system shell highlighter
     *
     * @param commandHighlighter commandContentType
     * @return {@code this} instance
     * @since 0.8.3
     */
    NSystemTerminalBase setCommandHighlighter(String commandHighlighter);

    Object run(NTerminalCommand command, NSession session);

    Cursor getTerminalCursor(NSession session);

    Size getTerminalSize(NSession session);

    NSystemTerminalBase resetLine(NSession session) ;

    NSystemTerminalBase clearScreen(NSession session) ;

    void setStyles(NTextStyles styles, NSession session);

    class Cursor implements Serializable {
        private int x;
        private int y;

        private Cursor() {
            // for serialization purposes
        }
        public Cursor(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
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

        private Size() {
            // for serialization purposes
        }
        public Size(int columns, int rows) {
            this.columns = columns;
            this.rows = rows;
        }

        public int getColumns() {
            return columns;
        }

        public int getRows() {
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
