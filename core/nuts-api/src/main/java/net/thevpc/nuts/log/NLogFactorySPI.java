package net.thevpc.nuts.log;

import net.thevpc.nuts.spi.NComponent;

public interface NLogFactorySPI extends NComponent {
    NLogSPI getLogSPI(String name);
}
