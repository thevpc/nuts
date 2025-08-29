package net.thevpc.nuts.runtime.standalone.log;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.log.NLogFactorySPI;
import net.thevpc.nuts.log.NLogSPI;
import net.thevpc.nuts.spi.NSupportLevelContext;

import java.util.Objects;

public class NLogFactorySPIJUL implements NLogFactorySPI {
    @Override
    public NLogSPI getLogSPI(String name) {
        return new NLogSPIJUL(name);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NLogFactorySPIJUL that = (NLogFactorySPIJUL) o;
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(1);
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

}
