/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.util.fprint.renderer.ansi;

/**
 *
 * @author vpc
 */
public class BackgroundStyleApplier implements AnsiStyleStyleApplier {
    
    private String id;

    public BackgroundStyleApplier(String id) {
        this.id = id;
    }

    @Override
    public AnsiStyle apply(AnsiStyle old) {
        return old.setBackground(id);
    }
    
}
