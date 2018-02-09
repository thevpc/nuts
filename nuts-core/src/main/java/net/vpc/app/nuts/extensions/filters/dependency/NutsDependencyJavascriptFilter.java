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
package net.vpc.app.nuts.extensions.filters.dependency;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.core.DefaultNutsDescriptor;
import net.vpc.app.nuts.extensions.core.NutsIdImpl;
import net.vpc.app.nuts.extensions.util.Simplifiable;

import java.util.*;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;
import net.vpc.app.nuts.extensions.util.JavascriptHelper;

/**
 * Created by vpc on 1/7/17.
 */
public class NutsDependencyJavascriptFilter implements NutsDependencyFilter, Simplifiable<NutsDependencyFilter>,JsNutsDependencyFilter {

    private static NutsId SAMPLE_NUTS_ID = new NutsIdImpl("sample", "sample", "sample", "sample", "sample");
    private static DefaultNutsDescriptor SAMPLE_NUTS_DESCRIPTOR = new DefaultNutsDescriptor(
            SAMPLE_NUTS_ID, "default",
            new NutsId[]{SAMPLE_NUTS_ID},
            "sample",
            true,
            "sample",
            new NutsExecutorDescriptor(
                    SAMPLE_NUTS_ID,
                    new String[]{"sample"},
                    null
            ),
            new NutsExecutorDescriptor(
                    SAMPLE_NUTS_ID,
                    new String[]{"sample"},
                    null
            ),
            "sample",
            "sample",
            new String[]{"sample"},
            new String[]{"sample"},
            new String[]{"sample"},
            new String[]{"sample"},
            null,
            null
    );

    private String code;
    private JavascriptHelper engineHelper;

    private static final WeakHashMap<String, NutsDependencyJavascriptFilter> cached = new WeakHashMap<>();

    public static NutsDependencyJavascriptFilter valueOf(String value) {
        if (CoreStringUtils.isEmpty(value)) {
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
        engineHelper = new JavascriptHelper(code, "var dependency=x; var id=x.toId(); var version=id.getVersion();", blacklist,null);
        this.code = code;
        //check if valid
//        accept(SAMPLE_DependencyNUTS_DESCRIPTOR);
    }

    public String getCode() {
        return code;
    }

    @Override
    public boolean accept(NutsDependency d) {
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
    
}
