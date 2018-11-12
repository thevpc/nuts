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
class DoNothingAnsiStyleStyleApplier implements AnsiStyleStyleApplier {

    public static final DoNothingAnsiStyleStyleApplier INSTANCE = new DoNothingAnsiStyleStyleApplier();

    @Override
    public AnsiStyle apply(AnsiStyle old) {
        return old;
    }

}
