package net.thevpc.nuts.spi;

public interface NUtilSPI extends NComponent{
    <T extends NScorable> NScorable.Query<T> ofScorableQuery();
}
