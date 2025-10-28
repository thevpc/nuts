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
 * A {@link Callable} extension that integrates with the Nuts element description system.
 * <p>
 * {@code NCallable} behaves like a standard {@link Callable} but adds
 * support for structured self-description through {@link NElementRedescribable}.
 * This allows callables to carry semantic metadata that can be serialized,
 * logged, or analyzed at runtime.
 * </p>
 *
 * <p>
 * This interface also provides exception-safety utilities for wrapping regular
 * {@link Callable} instances so that checked exceptions are rethrown as unchecked
 * using {@link NExceptions#ofUncheckedException(Throwable)}.
 * </p>
 *
 * <p>
 * Example:
 * <pre>{@code
 * NCallable<String> task = NCallable.of(() -> {
 *     Thread.sleep(100);
 *     return "Done";
 * });
 *
 * // Optionally attach a structured description
 * task = task.redescribe(() -> NElements.ofObject().set("task", "sleep-then-done").build());
 *
 * String result = task.call(); // No checked exception declaration needed
 * }</pre>
 * </p>
 *
 * @param <T> the result type returned by this callable
 * @see NElementRedescribable
 * @see NElement
 * @see NExceptions
 * @since 0.8.0
 */
public interface NCallable<T> extends NElementRedescribable<NCallable<T>>, Callable<T> {

    /**
     * Wraps a standard {@link Callable} into an {@code NCallable}.
     * <p>
     * If {@code other} is already an instance of {@code NCallable}, it is returned as-is.
     * Otherwise, this method creates a wrapper that delegates to {@code other.call()}.
     * </p>
     * <p>
     * Checked exceptions thrown by the delegate are automatically rethrown
     * as unchecked exceptions using {@link NExceptions#ofUncheckedException(Throwable)}.
     * </p>
     *
     * @param other the callable to wrap, may be {@code null}
     * @param <T>   the result type of the callable
     * @return a non-null {@code NCallable} wrapping the given callable
     */
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

    /**
     * Executes the computation and returns its result.
     * <p>
     * Unlike the standard {@link Callable#call()}, this method does not
     * declare checked exceptions; they are automatically rethrown as unchecked.
     * </p>
     *
     * @return the computed result
     * @throws RuntimeException if the computation throws any exception
     */
    T call();

    /**
     * Associates a new structured description with this callable.
     * <p>
     * The description is a lazily evaluated {@link NElement} that provides
     * additional metadata about the callable’s purpose or configuration.
     * </p>
     *
     * <p>
     * If {@code description} is {@code null}, the original callable is returned unchanged.
     * </p>
     *
     * @param description supplier of the {@link NElement} description
     * @return a new {@code NCallable} instance with the given description
     */
    default NCallable<T> redescribe(Supplier<NElement> description) {
        if (description == null) {
            return this;
        }
        return new NCallableWithDescription<>(this, description);
    }
}
