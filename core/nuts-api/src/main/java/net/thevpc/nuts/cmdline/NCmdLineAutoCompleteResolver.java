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
 * <br> ====================================================================
 */
package net.thevpc.nuts.cmdline;

import java.util.List;
import java.util.Objects;

/**
 * Auto Complete Resolver
 *
 * @author thevpc
 * @app.category Command Line
 * @since 0.8.0
 */
public interface NCmdLineAutoCompleteResolver {

    /**
     * resolve possible candidates
     *
     * @param cmdLine command line
     * @param pos     cursor position where to complete
     * @return possible candidates
     */
    List<NArgCandidate> resolveCandidates(NCmdLine cmdLine, Pos pos);

    class Pos {
        private final int wordIndex;
        private final int inWordCursor;
        private final int inLineCursor;

        public Pos(int wordIndex, int inWordCursor, int inLineCursor) {
            this.wordIndex = wordIndex;
            this.inWordCursor = inWordCursor;
            this.inLineCursor = inLineCursor;
        }

        public int wordIndex() {
            return wordIndex;
        }

        public int inWordCursor() {
            return inWordCursor;
        }

        public int inLineCursor() {
            return inLineCursor;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Pos pos = (Pos) o;
            return wordIndex == pos.wordIndex && inWordCursor == pos.inWordCursor && inLineCursor == pos.inLineCursor;
        }

        @Override
        public int hashCode() {
            return Objects.hash(wordIndex, inWordCursor, inLineCursor);
        }

        @Override
        public String toString() {
            return "Pos{" +
                    "wordIndex=" + wordIndex +
                    ", inWordCursor=" + inWordCursor +
                    ", inLineCursor=" + inLineCursor +
                    '}';
        }
    }
}
