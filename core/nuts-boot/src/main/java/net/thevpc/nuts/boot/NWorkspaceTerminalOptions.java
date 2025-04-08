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
package net.thevpc.nuts.boot;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;

/**
 * Boot Terminal allows usage of custom stdin/out/err when calling nuts
 *
 * @author thevpc
 */
public final class NWorkspaceTerminalOptions {
    private final InputStream in;
    private final PrintStream out;
    private final PrintStream err;
    private final Set<String> flags;

    public NWorkspaceTerminalOptions(InputStream in, PrintStream out, PrintStream err, String... flags) {
        this.in = in;
        this.out = out;
        this.err = err;
        this.flags = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(flags)));
    }

    public Set<String> getFlags() {
        return flags;
    }

    public InputStream getIn() {
        return in;
    }

    public PrintStream getOut() {
        return out;
    }

    public PrintStream getErr() {
        return err;
    }

    @Override
    public int hashCode() {
        return Objects.hash(in, out, err);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NWorkspaceTerminalOptions that = (NWorkspaceTerminalOptions) o;
        return Objects.equals(in, that.in) && Objects.equals(out, that.out) && Objects.equals(err, that.err);
    }

    @Override
    public String toString() {
        return "NutsBootTerminal{" +
                "in=" + in +
                ", out=" + out +
                ", err=" + err +
                '}';
    }
}
