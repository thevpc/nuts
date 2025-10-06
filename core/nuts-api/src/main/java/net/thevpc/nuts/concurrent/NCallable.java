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

import net.thevpc.nuts.util.NExceptions;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.internal.util.NCallableWithDescription;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Describable Runnable
 */
public interface NCallable<T> extends NElementRedescribable<NCallable<T>>, Callable<T> {
    static <T> NCallable<T> of(Callable<T> other) {
        if (other instanceof NCallable) {
            return (NCallable<T>) other;
        }
        return () -> {
            try {
                if (other != null) {
                    return other.call();
                }
                return null;
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw NExceptions.ofUncheckedException(e);
            }
        };
    }

    T call();

    default NCallable<T> redescribe(Supplier<NElement> description) {
        if (description == null) {
            return this;
        }
        return new NCallableWithDescription<>(this, description);
    }
}
