/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.runtime.util;

import java.util.Stack;
import net.vpc.app.nuts.NutsIdFilter;
import net.vpc.app.nuts.runtime.NutsPatternIdFilter;
import net.vpc.app.nuts.runtime.filters.id.NutsIdFilterAnd;
import net.vpc.app.nuts.runtime.filters.id.NutsIdFilterOr;
import net.vpc.app.nuts.runtime.filters.id.NutsJavascriptIdFilter;

/**
 *
 * @author vpc
 */
public class NutsIdFilterStackBuilder {

    private Stack<NutsIdFilter> vars = new Stack<>();

    public NutsIdFilterStackBuilder js(String n, String js) {
        return push(new NutsJavascriptIdFilter(js));
    }

    public NutsIdFilterStackBuilder and() {
        return and(2);
    }

    public NutsIdFilterStackBuilder and(int n) {
        return push(new NutsIdFilterAnd(pop(n)));
    }

    public NutsIdFilterStackBuilder or() {
        return or(2);
    }

    public NutsIdFilterStackBuilder or(int n) {
        return push(new NutsIdFilterOr(pop(n)));
    }

    public NutsIdFilterStackBuilder id(String id) {
        return push(new NutsPatternIdFilter(CoreNutsUtils.parseNutsId(id)));
    }

    private NutsIdFilter pop() {
        return vars.pop();
    }

    private NutsIdFilter[] pop(int n) {
        NutsIdFilter[] r = new NutsIdFilter[n];
        for (int i = 0; i < r.length; i++) {
            r[r.length - 1 - i] = vars.pop();
        }
        return r;
    }

    private NutsIdFilterStackBuilder push(NutsIdFilter filter) {
        vars.push(filter);
        return this;
    }

    public NutsIdFilter build() {
        return vars.pop();
    }
}
