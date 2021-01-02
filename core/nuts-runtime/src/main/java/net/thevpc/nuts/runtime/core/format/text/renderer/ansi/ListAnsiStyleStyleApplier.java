/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.core.format.text.renderer.ansi;

import net.thevpc.nuts.NutsWorkspace;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author thevpc
 */
public class ListAnsiStyleStyleApplier implements AnsiStyleStyleApplier {

    public List<AnsiStyleStyleApplier> suppliers = new ArrayList<AnsiStyleStyleApplier>();

    public ListAnsiStyleStyleApplier(List<AnsiStyleStyleApplier> suppliers) {
        this.suppliers = suppliers;
    }

    @Override
    public AnsiStyle apply(AnsiStyle old, NutsWorkspace ws) {
        for (AnsiStyleStyleApplier supplier : suppliers) {
            old = supplier.apply(old, ws);
        }
        return old;
    }

}
