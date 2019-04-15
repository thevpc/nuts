/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.util.fprint.parser;

/**
 *
 * @author vpc
 */
class TokResult {
    
    FDocNode node;
    int consumedCount;

    public TokResult(FDocNode toks, int consumedCount) {
        this.node = toks;
        this.consumedCount = consumedCount;
    }

    @Override
    public String toString() {
        return "TokResult{" + "toks=" + node + ", consumedCount=" + consumedCount + '}';
    }
    
}
