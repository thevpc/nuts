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
package net.vpc.app.nuts.core.filters.descriptor;

import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsDescriptorFilter;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.JavascriptHelper;
import net.vpc.app.nuts.core.util.Simplifiable;

import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import net.vpc.app.nuts.core.util.CoreStringUtils;

/**
 * Created by vpc on 1/7/17.
 */
public class NutsDescriptorJavascriptFilter implements NutsDescriptorFilter, Simplifiable<NutsDescriptorFilter>, JsNutsDescriptorFilter {


    private String code;
    private JavascriptHelper engineHelper;

    private static final WeakHashMap<String, NutsDescriptorJavascriptFilter> cached = new WeakHashMap<>();

    public static NutsDescriptorJavascriptFilter valueOf(String value) {
        if (CoreStringUtils.isBlank(value)) {
            return null;
        }
        synchronized (cached) {
            NutsDescriptorJavascriptFilter old = cached.get(value);
            if (old == null) {
                old = new NutsDescriptorJavascriptFilter(value);
                cached.put(value, old);
            }
            return old;
        }
    }

    public NutsDescriptorJavascriptFilter(String code) {
        this(code, null);
    }

    public NutsDescriptorJavascriptFilter(String code, Set<String> blacklist) {
        engineHelper = new JavascriptHelper(code, "var descriptor=x; var id=x.getId(); var version=id.getVersion();", blacklist, null);
        this.code = code;
        //check if valid
        accept(CoreNutsUtils.SAMPLE_NUTS_DESCRIPTOR);
    }

    public String getCode() {
        return code;
    }

    public boolean accept(NutsDescriptor d) {
        return engineHelper.accept(d);
    }

    @Override
    public NutsDescriptorFilter simplify() {
        return this;
    }

    @Override
    public String toJsNutsDescriptorFilterExpr() {
        return getCode();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.code);
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
        final NutsDescriptorJavascriptFilter other = (NutsDescriptorJavascriptFilter) obj;
        if (!Objects.equals(this.code, other.code)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "NutsDescriptorJavascriptFilter{" + code + '}';
    }

}
