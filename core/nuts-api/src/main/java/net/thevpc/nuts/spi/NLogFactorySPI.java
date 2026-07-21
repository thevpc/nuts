package net.thevpc.nuts.spi;

public interface NLogFactorySPI extends NComponent {
    NLogSPI getLogSPI(String name);
}
