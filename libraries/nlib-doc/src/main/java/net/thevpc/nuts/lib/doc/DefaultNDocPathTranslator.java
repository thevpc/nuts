/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.doc;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author thevpc
 */
public class DefaultNDocPathTranslator implements NDocPathTranslator {

    private final Path source;
    private final Path target;

    public DefaultNDocPathTranslator(Path baseSource, Path baseTarget) {
        this.source = baseSource;
        this.target = baseTarget;
    }

    public Path getSource() {
        return source;
    }

    public Path getTarget() {
        return target;
    }

    @Override
    public String translatePath(String from) {
        Path path = Paths.get(from);
        if (path.startsWith(source)) {
            Path r = path.subpath(source.getNameCount(), path.getNameCount());
            Path t = target.resolve(r);
            return t.toString();
        }
        return null;
    }

}
