/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.util;

import java.util.Stack;
import net.thevpc.nuts.NutsIdFilter;
import net.thevpc.nuts.NutsWorkspace;

/**
 *
 * @author thevpc
 */
public class NutsIdFilterStackBuilder {
    NutsWorkspace ws;

    public NutsIdFilterStackBuilder(NutsWorkspace ws) {
        this.ws = ws;
    }

    private Stack<NutsIdFilter> vars = new Stack<>();

    public NutsIdFilterStackBuilder js(String n, String js) {
        return push(ws.id().filter().byExpression(js));
    }

    public NutsIdFilterStackBuilder and() {
        return and(2);
    }

    public NutsIdFilterStackBuilder and(int n) {
        return push(ws.id().filter().all(pop(n)));
    }

    public NutsIdFilterStackBuilder or() {
        return or(2);
    }

    public NutsIdFilterStackBuilder or(int n) {
        return push(ws.id().filter().any(pop(n)));
    }

    public NutsIdFilterStackBuilder id(String id) {
        return push(
                ws.id().filter().byName(id)
        );
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
