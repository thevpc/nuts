/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.text.renderer.ansi;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.text.RenderedRawStream;
import net.thevpc.nuts.runtime.standalone.text.renderer.AnsiStyleStyleApplierResolver;

/**
 *
 * @author thevpc
 */
public interface AnsiStyleStyleApplier {

    AnsiStyle apply(AnsiStyle old, RenderedRawStream out, NutsSession session, AnsiStyleStyleApplierResolver applierResolver);

}
