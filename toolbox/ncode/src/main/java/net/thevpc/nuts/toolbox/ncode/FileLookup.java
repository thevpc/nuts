/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ncode;

import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.NSession;

/**
 * @author thevpc
 */
public class FileLookup implements SourceProcessor {

    public FileLookup() {
    }

    @Override
    public Object process(Source source, NSession session) {
        return NPath.of(source.getExternalPath(),session);
    }

}
