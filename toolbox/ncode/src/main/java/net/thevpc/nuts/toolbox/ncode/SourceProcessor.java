/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ncode;

import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.nuts.NutsSession;

/**
 *
 * @author thevpc
 */
public interface SourceProcessor {

    /**
     *
     * @param source source
     * @param session session
     */
    Object process(Source source, NutsSession session);
}
