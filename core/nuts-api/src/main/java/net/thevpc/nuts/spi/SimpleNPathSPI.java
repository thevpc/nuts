package net.thevpc.nuts.spi;

import net.thevpc.nuts.io.NPath;

public abstract class SimpleNPathSPI implements NPathSPI{
    private String prefix;

    @Override
    public int getNameCount(NPath basePath) {
        return 0;
    }
}
