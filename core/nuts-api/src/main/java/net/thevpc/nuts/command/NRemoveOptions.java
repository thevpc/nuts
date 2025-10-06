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
package net.thevpc.nuts.command;

import java.util.Objects;

/**
 * @author thevpc
 * @app.category Commands
 * @since 0.5.4
 */
public class NRemoveOptions {

    private boolean erase = false;

    public boolean isErase() {
        return erase;
    }

    public NRemoveOptions setErase(boolean erase) {
        this.erase = erase;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(erase);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NRemoveOptions that = (NRemoveOptions) o;
        return erase == that.erase;
    }

    @Override
    public String toString() {
        return "NutsRemoveOptions{" +
                "erase=" + erase +
                '}';
    }
}
