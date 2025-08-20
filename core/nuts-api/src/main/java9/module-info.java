/**
 * documentation
 */
module net.thevpc.nuts {
    requires java.base;
    requires java.logging;
    requires java.xml;
    requires java.desktop;
    requires net.thevpc.nuts.boot;

    exports net.thevpc.nuts;
    exports net.thevpc.nuts.cmdline;
    exports net.thevpc.nuts.concurrent;
    exports net.thevpc.nuts.elem;
    exports net.thevpc.nuts.expr;
    exports net.thevpc.nuts.ext;
    exports net.thevpc.nuts.format;
    exports net.thevpc.nuts.io;
    exports net.thevpc.nuts.log;
    exports net.thevpc.nuts.math;
    exports net.thevpc.nuts.reflect;
    exports net.thevpc.nuts.security;
    exports net.thevpc.nuts.spi;
    exports net.thevpc.nuts.text;
    exports net.thevpc.nuts.time;
    exports net.thevpc.nuts.util;
    exports net.thevpc.nuts.web;

    uses net.thevpc.nuts.boot.NBootWorkspaceFactory;
    uses net.thevpc.nuts.spi.NComponent;
}
