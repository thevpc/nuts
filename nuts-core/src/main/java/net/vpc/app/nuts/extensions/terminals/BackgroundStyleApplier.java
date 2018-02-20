/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.extensions.terminals;

/**
 *
 * @author vpc
 */
class BackgroundStyleApplier implements AnsiStyleStyleApplier {
    
    String id;

    public BackgroundStyleApplier(String id) {
        this.id = id;
    }

    @Override
    public AnsiStyle apply(AnsiStyle old) {
        return old.setBackground(id);
    }
    
}
