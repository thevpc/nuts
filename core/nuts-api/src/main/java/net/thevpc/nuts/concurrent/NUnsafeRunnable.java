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

import java.util.function.Supplier;

/**
 * A runnable task that can throw checked exceptions and can provide a descriptive element.
 * <p>
 * This is similar to {@link Runnable}, but the {@link #run()} method is allowed to
 * throw any {@link Exception}. Additionally, it supports producing a structured description
 * via {@link NElement} and can be redescribed with custom metadata.
 *
 * @since 0.8.7
 */
public interface NUnsafeRunnable extends NElementRedescribable<NUnsafeRunnable> {
    /**
     * Returns the given {@link NUnsafeRunnable} instance or {@code null} if the input is null.
     *
     * @param o the unsafe runnable instance
     * @return the same instance, or {@code null}
     */
    static NUnsafeRunnable of(NUnsafeRunnable o) {
        if(o==null){
            return null;
        }
        return o;
    }

    /**
     * Creates a new {@link NUnsafeRunnable} with a custom {@link NElement} description.
     *
     * @param description a supplier of a description element
     * @return a new {@link NUnsafeRunnable} with the given description
     */
    @Override
    default NUnsafeRunnable redescribe(Supplier<NElement> description) {
        if (description == null) {
            return this;
        }
        return new NUnsafeRunnableWithDescription(this, description);
    }

    /**
     * Returns a {@link NElement} describing this runnable.
     * <p>
     * By default, this uses a late-to-string description for convenience.
     *
     * @return a descriptive {@link NElement}
     */
    @Override
    default NElement describe() {
        return NElementDescribables.ofLateToString(this).get();
    }

    /**
     * Executes the task.
     * <p>
     * This method may throw any checked exception.
     *
     * @throws Exception if the execution fails
     */
    void run() throws Exception;

}
