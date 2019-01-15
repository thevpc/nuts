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
package net.vpc.app.nuts.core.filters.dependency;

import net.vpc.app.nuts.NutsDependency;
import net.vpc.app.nuts.NutsDependencyFilter;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.core.DefaultNutsId;
import net.vpc.app.nuts.core.util.JavascriptHelper;
import net.vpc.app.nuts.core.util.Simplifiable;
import net.vpc.common.strings.StringUtils;

import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Created by vpc on 1/7/17.
 */
public class NutsDependencyJavascriptFilter implements NutsDependencyFilter, Simplifiable<NutsDependencyFilter>, JsNutsDependencyFilter {

    private static NutsId SAMPLE_NUTS_ID = new DefaultNutsId("sample", "sample", "sample", "sample", "sample");

    private String code;
    private JavascriptHelper engineHelper;

    private static final WeakHashMap<String, NutsDependencyJavascriptFilter> cached = new WeakHashMap<>();

    public static NutsDependencyJavascriptFilter valueOf(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        synchronized (cached) {
            NutsDependencyJavascriptFilter old = cached.get(value);
            if (old == null) {
                old = new NutsDependencyJavascriptFilter(value);
                cached.put(value, old);
            }
            return old;
        }
    }

    public NutsDependencyJavascriptFilter(String code) {
        this(code, null);
    }

    public NutsDependencyJavascriptFilter(String code, Set<String> blacklist) {
        engineHelper = new JavascriptHelper(code, "var dependency=x; var id=x.getId(); var version=id.getVersion();", blacklist, null);
        this.code = code;
        //check if valid
//        accept(SAMPLE_DependencyNUTS_DESCRIPTOR);
    }

    public String getCode() {
        return code;
    }

    @Override
    public boolean accept(NutsId from, NutsDependency d) {
        return engineHelper.accept(d);
    }

    @Override
    public NutsDependencyFilter simplify() {
        return this;
    }

    @Override
    public String toJsNutsDependencyFilterExpr() {
//        return "util.matches(dependency,'" + CoreStringUtils.escapeCoteStrings(code) + "')";
        return getCode();
    }

    @Override
    public String toString() {
        return "NutsDependencyJavascriptFilter{" + code + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + Objects.hashCode(this.code);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NutsDependencyJavascriptFilter other = (NutsDependencyJavascriptFilter) obj;
        if (!Objects.equals(this.code, other.code)) {
            return false;
        }
        return true;
    }

    
}
