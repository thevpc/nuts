/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ntemplate.filetemplate.eval;

import net.thevpc.nuts.toolbox.ntemplate.filetemplate.FileTemplater;

/**
 *
 * @author thevpc
 */
public interface ExprEvalFct {

    String getName();

    Object evalFunction(ExprNode[] args, FileTemplater context);
}
