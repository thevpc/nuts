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
public class ForegroundStyleApplier implements AnsiStyleStyleApplier {

    String id;
    int intensity;

    public ForegroundStyleApplier(String id, int intensity) {
        this.id = id;
        this.intensity = intensity;
    }

    @Override
    public AnsiStyle apply(AnsiStyle old) {
        return old.setForeground(id).setIntensity(intensity);
    }

}
