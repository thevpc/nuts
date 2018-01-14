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
package net.vpc.app.nuts.extensions.core;

import net.vpc.app.nuts.NutsVersion;
import net.vpc.app.nuts.NutsVersionFilter;
import net.vpc.app.nuts.NutsVersionInterval;
import net.vpc.app.nuts.extensions.util.CoreVersionUtils;
import net.vpc.app.nuts.extensions.util.DefaultNutsVersionFilter;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;

/**
 * Created by vpc on 1/15/17.
 */
public class NutsVersionImpl implements NutsVersion {

    private final String value;

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public boolean isEmpty() {
        return CoreStringUtils.isEmpty(value);
    }

    public NutsVersionImpl(String value) {
        this.value = CoreStringUtils.trim(value);
    }

    @Override
    public int compareTo(String other) {
        return CoreVersionUtils.compareVersions(value, other);
    }

    @Override
    public int compareTo(NutsVersion other) {
        return compareTo(other == null ? null : other.getValue());
    }

    @Override
    public boolean ge(String other) {
        return compareTo(other) >= 0;
    }

    @Override
    public boolean gt(String other) {
        return compareTo(other) > 0;
    }

    @Override
    public boolean le(String other) {
        return compareTo(other) <= 0;
    }

    @Override
    public boolean lt(String other) {
        return compareTo(other) < 0;
    }

    @Override
    public boolean eq(String other) {
        return compareTo(other) == 0;
    }

    @Override
    public boolean ne(String other) {
        return compareTo(other) != 0;
    }

    @Override
    public NutsVersionFilter toFilter() {
        return DefaultNutsVersionFilter.parse(value);
    }

    @Override
    public NutsVersionInterval[] toIntervals() {
        DefaultNutsVersionFilter s = DefaultNutsVersionFilter.parse(value);
        return s.getIntervals();
    }

    @Override
    public boolean isSingleValue() {
        NutsVersionInterval[] nutsVersionIntervals = toIntervals();
        return nutsVersionIntervals.length != 0 && nutsVersionIntervals.length <= 1 && nutsVersionIntervals[0].isFixedValue();
    }

    @Override
    public String toString() {
        return value == null ? "" : String.valueOf(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NutsVersionImpl version = (NutsVersionImpl) o;

        return value != null ? value.equals(version.value) : version.value == null;

    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
