/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.core.format.text.renderer.ansi;

import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.format.text.RenderedRawStream;

/**
 *
 * @author thevpc
 */
public class BackgroundStyleApplier implements AnsiStyleStyleApplier {

    private String id;

    public BackgroundStyleApplier(String id) {
        this.id = id;
    }

    @Override
    public AnsiStyle apply(AnsiStyle old, RenderedRawStream out, NutsWorkspace ws) {
        return old.setBackground(id);
    }

}
