/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ncode.processors;

import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.toolbox.ncode.SourceProcessor;
import net.thevpc.nuts.toolbox.ncode.Source;

/**
 * @author thevpc
 */
public class PathSourceFormatter implements SourceProcessor {

    public PathSourceFormatter() {
    }

    @Override
    public Object process(Source source, NutsSession session) {
        return session.io().path(source.getExternalPath());
    }

}
