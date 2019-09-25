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
 * Copyright (C) 2016-2019 Taha BEN SALAH
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
package net.vpc.app.nuts.core.test.whitebox;

import net.vpc.app.nuts.NutsVersionFilter;
import net.vpc.app.nuts.runtime.filters.version.DefaultNutsVersionFilter;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author vpc
 */
public class Test09_VersionIntervals {

    @Test
    public void test1() {
        check("1.0", "[1.0]");
        check("[1.0]", "[1.0]");
        check("[1.0,[", "[1.0,[");
        check("[1.0[", "[1.0,1.0[");
        check("]1.0[", "]1.0,1.0[");
        check("[,1.0[", "],1.0[");
        check("[,[", "],[");
        check("[1,2[  ,  [1,3]", "[1,2[, [1,3]");
    }

    private void check(String a, String b) {
        NutsVersionFilter u = DefaultNutsVersionFilter.parse(a);
        String b2 = u.toString();
        Assert.assertEquals(b, b2);
        System.out.println(a + " ==> " + b);
    }
}
