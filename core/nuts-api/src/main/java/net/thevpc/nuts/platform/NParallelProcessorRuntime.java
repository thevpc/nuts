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
package net.thevpc.nuts.platform;

import java.util.Objects;

/**
 * Availability of a parallel processing runtime on a machine.
 * <p>
 * The important part is that executing and building are reported separately,
 * because they are installed separately and lead to opposite decisions. A
 * machine with an NVIDIA driver but no CUDA toolkit can run a prebuilt cuda
 * binary yet cannot compile one, while a build container with the toolkit and no
 * device is exactly the reverse. Reducing both to a single "has cuda" boolean,
 * which is what {@link NParallelProcessorFamily#getCurrent()} does on its own,
 * loses the distinction a resolver needs to choose between shipping a prebuilt
 * artifact and shipping sources.
 *
 * @author thevpc
 * @app.category Base
 * @since 1.0.0
 */
public class NParallelProcessorRuntime {

    private final NParallelProcessorFamily family;
    private final boolean runtimeAvailable;
    private final boolean toolkitAvailable;
    private final String version;

    private NParallelProcessorRuntime(NParallelProcessorFamily family, boolean runtimeAvailable,
                                      boolean toolkitAvailable, String version) {
        this.family = family == null ? NParallelProcessorFamily.UNKNOWN : family;
        this.runtimeAvailable = runtimeAvailable;
        this.toolkitAvailable = toolkitAvailable;
        this.version = version;
    }

    /**
     * Creates a runtime availability description.
     *
     * @param family           the runtime family
     * @param runtimeAvailable true when code targeting this family can be executed
     * @param toolkitAvailable true when code targeting this family can be compiled
     * @param version          detected version, null when unknown
     * @return a new description
     */
    public static NParallelProcessorRuntime of(NParallelProcessorFamily family, boolean runtimeAvailable,
                                               boolean toolkitAvailable, String version) {
        return new NParallelProcessorRuntime(family, runtimeAvailable, toolkitAvailable, version);
    }

    /**
     * The runtime family this availability describes.
     *
     * @return the family, never null
     */
    public NParallelProcessorFamily getFamily() {
        return family;
    }

    /**
     * Returns true when code targeting this family can be executed, which needs
     * the driver and its runtime library, not the development toolkit.
     *
     * @return true when execution is possible
     */
    public boolean isRuntimeAvailable() {
        return runtimeAvailable;
    }

    /**
     * Returns true when code targeting this family can be compiled, which needs
     * the development toolkit such as the CUDA toolkit or the ROCm HIP compiler.
     *
     * @return true when compilation is possible
     */
    public boolean isToolkitAvailable() {
        return toolkitAvailable;
    }

    /**
     * Detected version, best effort. Vendors do not expose a version uniformly,
     * so this stays null on installations that publish none in a readable form.
     *
     * @return version, null when unknown
     */
    public String getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        return Objects.hash(family, runtimeAvailable, toolkitAvailable, version);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NParallelProcessorRuntime that = (NParallelProcessorRuntime) o;
        return runtimeAvailable == that.runtimeAvailable
                && toolkitAvailable == that.toolkitAvailable
                && family == that.family
                && Objects.equals(version, that.version);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(family.id());
        if (version != null) {
            sb.append(' ').append(version);
        }
        sb.append(" [");
        if (runtimeAvailable && toolkitAvailable) {
            sb.append("run,build");
        } else if (runtimeAvailable) {
            sb.append("run");
        } else if (toolkitAvailable) {
            sb.append("build");
        } else {
            sb.append("none");
        }
        sb.append(']');
        return sb.toString();
    }
}
