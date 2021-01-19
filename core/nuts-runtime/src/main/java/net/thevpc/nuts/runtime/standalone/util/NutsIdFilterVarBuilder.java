/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.util;

import java.util.HashMap;
import java.util.Map;
import net.thevpc.nuts.NutsIdFilter;
import net.thevpc.nuts.NutsWorkspace;

/**
 *
 * @author thevpc
 */
public class NutsIdFilterVarBuilder {

    private static final String CURR = "$CURRENT";
    private Map<String, NutsIdFilter> vars = new HashMap<String, NutsIdFilter>();
    private NutsWorkspace ws;

    public NutsIdFilterVarBuilder(NutsWorkspace ws) {
        this.ws = ws;
    }

    public NutsIdFilterVarBuilder js(String n, String js) {
        return store(n, ws.id().filter().byExpression(js));
    }

    public NutsIdFilterVarBuilder and(String n, String... a) {
        NutsIdFilter[] aa = new NutsIdFilter[a.length];
        for (int i = 0; i < aa.length; i++) {
            aa[i] = get(a[i]);
        }
        return store(n, ws.id().filter().all(aa));
    }

    public NutsIdFilterVarBuilder or(String n, String... a) {
        NutsIdFilter[] aa = new NutsIdFilter[a.length];
        for (int i = 0; i < aa.length; i++) {
            aa[i] = get(a[i]);
        }
        return store(n, ws.id().filter().any(aa));
    }

//    public NutsIdFilterBuilder id(String id) {
//        return id(null,id);
//    }
    public NutsIdFilterVarBuilder id(String n, String id) {
        return store(n, ws.id().filter().byName(id));
    }

    private NutsIdFilter get(String n) {
        if (n == null) {
            n = CURR;
        }
        NutsIdFilter k = vars.get(n);
        if (k == null) {
            if (!CURR.equals(n)) {
                throw new IllegalArgumentException("not found " + n);
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
