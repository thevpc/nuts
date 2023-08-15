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
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.util;

import java.util.Objects;

public class NRef<T> {

    private T value;
    private boolean set;

    public static <T> NRef<T> of(T t) {
        return new NRef<>(t);
    }

    public static <T> NRef<T> of(T t, Class<T> type) {
        return new NRef<>(t);
    }

    public static <T> NRef<T> ofNull(Class<T> t) {
        return new NRef<>(null);
    }

    public static <T> NRef<T> ofNull() {
        return of(null);
    }

    public NRef() {
    }

    public NRef(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

    public T orElse(T other) {
        if (value == null) {
            return other;
        }
        return value;
    }

    public void setNonNull(T value) {
        if (value != null) {
            set(value);
        }
    }

    public void set(T value) {
        this.value = value;
        this.set = true;
    }

    public void unset() {
        this.value = null;
        this.set = false;
    }

    public boolean isNotNull() {
        return value != null;
    }

    public boolean isBlank() {
        return NBlankable.isBlank(value);
    }

    public boolean isEmpty() {
        return value == null || String.valueOf(value).isEmpty();
    }

    public boolean isNull() {
        return value == null;
    }

    public boolean isSet() {
        return set;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public boolean isValue(Object o) {
        return Objects.equals(value, o);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NRef<?> nRef = (NRef<?>) o;
        return set == nRef.set && Objects.equals(value, nRef.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, set);
    }
}
