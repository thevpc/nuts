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
package net.vpc.app.nuts;

import net.vpc.app.nuts.util.DefaultNutsVersionFilter;
import net.vpc.app.nuts.util.StringUtils;
import net.vpc.app.nuts.util.VersionUtils;

/**
 * Created by vpc on 1/15/17.
 */
public class NutsVersion {

    private final String value;

    public String getValue() {
        return value;
    }

    public boolean isEmpty() {
        return StringUtils.isEmpty(value);
    }

    public NutsVersion(String value) {
        this.value = StringUtils.trim(value);
    }

    public int compareTo(String other) {
        return VersionUtils.compareVersions(value, other);
    }

    public int compareTo(NutsVersion other) {
        return compareTo(other == null ? null : other.value);
    }

    public boolean ge(String other) {
        return compareTo(other) >= 0;
    }

    public boolean gt(String other) {
        return compareTo(other) > 0;
    }

    public boolean le(String other) {
        return compareTo(other) <= 0;
    }

    public boolean lt(String other) {
        return compareTo(other) < 0;
    }

    public boolean eq(String other) {
        return compareTo(other) == 0;
    }

    public boolean ne(String other) {
        return compareTo(other) != 0;
    }

    public NutsVersionFilter toFilter() {
        return DefaultNutsVersionFilter.parse(value);
    }

    public NutsVersionInterval[] toIntervals() {
        DefaultNutsVersionFilter s = DefaultNutsVersionFilter.parse(value);
        return s.getIntervals();
    }

    public boolean isSingleValue() {
        NutsVersionInterval[] nutsVersionIntervals = toIntervals();
        if (nutsVersionIntervals.length == 0 || nutsVersionIntervals.length > 1) {
            return false;
        }
        return nutsVersionIntervals[0].isFixedValue();
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

        NutsVersion version = (NutsVersion) o;

        return value != null ? value.equals(version.value) : version.value == null;

    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
