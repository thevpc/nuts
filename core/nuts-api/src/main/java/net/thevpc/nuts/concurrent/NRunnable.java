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
package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.internal.util.NRunnableFromJavaRunnable;
import net.thevpc.nuts.internal.util.NRunnableWithDescription;

import java.util.function.Supplier;

/**
 * A {@link Runnable} that can provide a description of itself as an {@link NElement}.
 * <p>
 * This interface extends the standard {@link Runnable} with a "describable" capability,
 * allowing it to be converted into an {@link NElement} for logging, monitoring, or inspection.
 *
 * @since 0.8.7
 */
public interface NRunnable extends NElementRedescribable<NRunnable>, Runnable {
    /**
     * Wraps a standard {@link Runnable} into an {@link NRunnable}.
     * <p>
     * If the given runnable is already an {@link NRunnable}, it is returned as-is.
     * Otherwise, it is wrapped into an {@link NRunnableFromJavaRunnable}.
     *
     * @param o the runnable to wrap
     * @return a non-null {@link NRunnable} wrapper, or {@code null} if input is {@code null}
     */
    static NRunnable of(Runnable o) {
        if (o == null) {
            return null;
        }
        if (o instanceof NRunnable) {
            return (NRunnable) o;
        }
        return new NRunnableFromJavaRunnable(o);
    }


    /**
     * Returns a new {@link NRunnable} with a custom description.
     * <p>
     * The provided {@link Supplier} of {@link NElement} will be used when the
     * runnable is described via {@link #describe()} or similar methods.
     *
     * @param description supplier providing the description element
     * @return a new {@link NRunnable} wrapping this instance with the custom description
     */
    @Override
    default NRunnable redescribe(Supplier<NElement> description) {
        if (description == null) {
            return this;
        }
        return new NRunnableWithDescription(this, description);
    }

    /**
     * Executes the runnable task.
     * <p>
     * This overrides {@link Runnable#run()}.
     */
    void run();
}
