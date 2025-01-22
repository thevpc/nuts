/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.doc.context;

import net.thevpc.nuts.io.NPath;


/**
 *
 * @author thevpc
 */
public class DefaultNDocPathTranslator implements NDocPathTranslator {

    private final NPath source;
    private final NPath target;

    public DefaultNDocPathTranslator(NPath baseSource, NPath baseTarget) {
        this.source = baseSource;
        this.target = baseTarget;
    }

    public NPath getSource() {
        return source;
    }

    public NPath getTarget() {
        return target;
    }

    @Override
    public String translatePath(String from) {
        NPath path = NPath.of(from);
        if (path.startsWith(source)) {
            NPath r = path.subpath(source.getNameCount(), path.getNameCount());
            NPath t = target.resolve(r);
            return t.toString();
        }
        return null;
    }

}
