/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.util;

import java.util.HashMap;
import java.util.Map;
import net.vpc.app.nuts.NutsIdFilter;
import net.vpc.app.nuts.core.filters.id.NutsIdFilterAnd;
import net.vpc.app.nuts.core.filters.id.NutsIdFilterOr;
import net.vpc.app.nuts.core.filters.id.NutsJavascriptIdFilter;
import net.vpc.app.nuts.core.filters.id.NutsSimpleIdFilter;

/**
 *
 * @author vpc
 */
public class NutsIdFilterVarBuilder {

    private static final String CURR = "$CURRENT";
    private Map<String, NutsIdFilter> vars = new HashMap<String, NutsIdFilter>();

    public NutsIdFilterVarBuilder js(String n, String js) {
        return store(n, new NutsJavascriptIdFilter(js));
    }

    public NutsIdFilterVarBuilder and(String n, String... a) {
        NutsIdFilter[] aa = new NutsIdFilter[a.length];
        for (int i = 0; i < aa.length; i++) {
            aa[i] = get(a[i]);
        }
        return store(n, new NutsIdFilterAnd(aa));
    }

    public NutsIdFilterVarBuilder or(String n, String... a) {
        NutsIdFilter[] aa = new NutsIdFilter[a.length];
        for (int i = 0; i < aa.length; i++) {
            aa[i] = get(a[i]);
        }
        return store(n, new NutsIdFilterOr(aa));
    }

//    public NutsIdFilterBuilder id(String id) {
//        return id(null,id);
//    }

    public NutsIdFilterVarBuilder id(String n, String id) {
        return store(n, new NutsSimpleIdFilter(CoreNutsUtils.parseNutsId(id)));
    }

    private NutsIdFilter get(String n) {
        if (n == null) {
            n = CURR;
        }
        NutsIdFilter k = vars.get(n);
        if (k == null) {
            if (!CURR.equals(n)) {
                throw new IllegalArgumentException("Not found " + n);
            }
        }
        return k;
    }

    private NutsIdFilterVarBuilder store(String n, NutsIdFilter filter) {
        if (n == null) {
            n = CURR;
        }
        vars.put(n, filter);
        return this;
    }

    public NutsIdFilter build() {
        return vars.get(CURR);
    }
}
