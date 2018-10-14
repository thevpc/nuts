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
package net.vpc.app.nuts.extensions.filters.id;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.core.NutsIdImpl;
import net.vpc.app.nuts.extensions.util.Simplifiable;

import java.util.*;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;
import net.vpc.app.nuts.extensions.util.JavascriptHelper;

/**
 * Created by vpc on 1/7/17.
 */
public class NutsJavascriptIdFilter implements NutsIdFilter, Simplifiable<NutsIdFilter>, NutsJsAwareIdFilter {

    private static NutsId SAMPLE_NUTS_ID = new NutsIdImpl("sample", "sample", "sample", "sample", "sample");

    private String code;
    private JavascriptHelper engineHelper;

    private static final WeakHashMap<String, NutsJavascriptIdFilter> cached = new WeakHashMap<>();

    public static NutsJavascriptIdFilter valueOf(String value) {
        if (CoreStringUtils.isEmpty(value)) {
            return null;
        }
        synchronized (cached) {
            NutsJavascriptIdFilter old = cached.get(value);
            if (old == null) {
                old = new NutsJavascriptIdFilter(value);
                cached.put(value, old);
            }
            return old;
        }
    }

    public NutsJavascriptIdFilter(String code) {
        this(code, null);
    }

    public NutsJavascriptIdFilter(String code, Set<String> blacklist) {
        engineHelper = new JavascriptHelper(code, "var id=x.getId(); var version=id.getVersion();", blacklist, null);
        this.code = code;
        //check if valid
        accept(SAMPLE_NUTS_ID);
    }

    public String getCode() {
        return code;
    }

    public boolean accept(NutsId id) {
        return engineHelper.accept(id);
    }

    @Override
    public NutsIdFilter simplify() {
        return this;
    }

    @Override
    public String toJsNutsIdFilterExpr() {
//        return "util.matches(dependency,'" + CoreStringUtils.escapeCoteStrings(code) + "')";
        return getCode();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + Objects.hashCode(this.code);
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
        final NutsJavascriptIdFilter other = (NutsJavascriptIdFilter) obj;
        if (!Objects.equals(this.code, other.code)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "NutsIdJavascriptFilter{" + code + '}';
    }

}