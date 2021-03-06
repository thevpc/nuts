/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 *
 * <br>
 *
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
package net.thevpc.nuts.runtime.core.format.text;

import java.util.Arrays;
import java.util.Iterator;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.format.text.renderer.AnsiStyleStyleApplierResolver;
import net.thevpc.nuts.runtime.core.format.text.renderer.ansi.AnsiStyle;
import net.thevpc.nuts.runtime.core.format.text.renderer.ansi.AnsiStyleStyleApplier;

/**
 *
 * @author thevpc
 */
public class AnsiEscapeCommandList extends AnsiEscapeCommand implements Iterable<AnsiEscapeCommand>, AnsiStyleStyleApplier {

    private final AnsiEscapeCommand[] children;

    public AnsiEscapeCommandList(AnsiEscapeCommand... others) {
        if (others == null) {
            throw new NullPointerException();
        }
        this.children = others;
    }

    public AnsiEscapeCommand[] getChildren() {
        return Arrays.copyOf(children, children.length);
    }

    @Override
    public Iterator<AnsiEscapeCommand> iterator() {
        return Arrays.asList(children).iterator();
    }

    @Override
    public String toString() {
        return Arrays.toString(children);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Arrays.deepHashCode(this.children);
        return hash;
    }

    @Override
    public AnsiStyle apply(AnsiStyle old, RenderedRawStream out, NutsWorkspace ws, AnsiStyleStyleApplierResolver applierResolver) {
        for (AnsiEscapeCommand cmd : children) {
            old = applierResolver.resolveStyleApplyer(cmd).apply(old, out, ws, applierResolver);
        }
        return old;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AnsiEscapeCommandList other = (AnsiEscapeCommandList) obj;
        if (!Arrays.deepEquals(this.children, other.children)) {
            return false;
        }
        return true;
    }

}
