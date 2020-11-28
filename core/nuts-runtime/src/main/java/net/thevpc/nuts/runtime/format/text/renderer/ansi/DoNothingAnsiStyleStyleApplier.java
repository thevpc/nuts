/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.format.text.renderer.ansi;

/**
 *
 * @author vpc
 */
public class DoNothingAnsiStyleStyleApplier implements AnsiStyleStyleApplier {

    public static final DoNothingAnsiStyleStyleApplier INSTANCE = new DoNothingAnsiStyleStyleApplier();

    @Override
    public AnsiStyle apply(AnsiStyle old) {
        return old;
    }

}
