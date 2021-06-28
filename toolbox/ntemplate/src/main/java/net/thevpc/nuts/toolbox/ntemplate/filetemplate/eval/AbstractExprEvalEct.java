/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ntemplate.filetemplate.eval;

/**
 *
 * @author thevpc
 */
public abstract class AbstractExprEvalEct implements ExprEvalFct {
    private final String name;

    public AbstractExprEvalEct(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
    
}
