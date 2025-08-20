/**
 * documentation
 */
module net.thevpc.nuts.runtime {
    requires java.base;
    requires java.logging;
    requires java.xml;
    requires java.desktop;
    requires net.thevpc.nuts;
    requires net.thevpc.nuts.boot;

    uses net.thevpc.nuts.spi.NComponent;
}
