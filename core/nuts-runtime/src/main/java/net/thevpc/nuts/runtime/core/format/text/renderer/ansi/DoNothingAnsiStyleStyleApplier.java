/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.core.format.text.renderer.ansi;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.core.format.text.RenderedRawStream;
import net.thevpc.nuts.runtime.core.format.text.renderer.AnsiStyleStyleApplierResolver;

/**
 *
 * @author thevpc
 */
public class DoNothingAnsiStyleStyleApplier implements AnsiStyleStyleApplier {

    public static final DoNothingAnsiStyleStyleApplier INSTANCE = new DoNothingAnsiStyleStyleApplier();

    @Override
    public AnsiStyle apply(AnsiStyle old, RenderedRawStream out, NutsSession session, AnsiStyleStyleApplierResolver applierResolver) {
        return old;
    }

}
