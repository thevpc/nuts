/**
 * JPMS descriptor for the nuts launcher.
 */
module net.thevpc.nuts.app {
    requires java.base;
    requires java.logging;
    requires java.xml;
    requires java.desktop;
    requires net.thevpc.nuts;
    requires net.thevpc.nuts.boot;
    requires net.thevpc.nuts.runtime;

    exports net.thevpc.nuts;
}
