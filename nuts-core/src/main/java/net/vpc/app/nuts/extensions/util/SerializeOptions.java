/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.util;

public class SerializeOptions {

    public boolean pretty;
    public boolean ignoreNulls;
    public boolean ignoreEmptyStrings;
    public boolean ignoreEmptyMaps;
    public boolean ignoreEmptyCollections;
    public boolean ignoreEmptyArrays;

    public boolean isPretty() {
        return pretty;
    }

    public SerializeOptions setPretty(boolean pretty) {
        this.pretty = pretty;
        return this;
    }

    public boolean isIgnoreNulls() {
        return ignoreNulls;
    }

    public SerializeOptions setIgnoreNulls(boolean ignoreNulls) {
        this.ignoreNulls = ignoreNulls;
        return this;
    }

    public boolean isIgnoreEmptyStrings() {
        return ignoreEmptyStrings;
    }

    public SerializeOptions setIgnoreEmptyStrings(boolean ignoreEmptyStrings) {
        this.ignoreEmptyStrings = ignoreEmptyStrings;
        return this;
    }

    public boolean isIgnoreEmptyMaps() {
        return ignoreEmptyMaps;
    }

    public SerializeOptions setIgnoreEmptyMaps(boolean ignoreEmptyMaps) {
        this.ignoreEmptyMaps = ignoreEmptyMaps;
        return this;
    }

    public boolean isIgnoreEmptyCollections() {
        return ignoreEmptyCollections;
    }

    public SerializeOptions setIgnoreEmptyCollections(boolean ignoreEmptyCollections) {
        this.ignoreEmptyCollections = ignoreEmptyCollections;
        return this;
    }

    public boolean isIgnoreEmptyArrays() {
        return ignoreEmptyArrays;
    }

    public SerializeOptions setIgnoreEmptyArrays(boolean ignoreEmptyArrays) {
        this.ignoreEmptyArrays = ignoreEmptyArrays;
        return this;
    }
}
