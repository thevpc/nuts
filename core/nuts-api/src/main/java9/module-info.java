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
    exports net.thevpc.nuts.app;
    exports net.thevpc.nuts.artifact;
    exports net.thevpc.nuts.cmdline;
    exports net.thevpc.nuts.collections;
    exports net.thevpc.nuts.command;
    exports net.thevpc.nuts.concurrent;
    exports net.thevpc.nuts.core;
    exports net.thevpc.nuts.elem;
    exports net.thevpc.nuts.expr;
    exports net.thevpc.nuts.ext;
    exports net.thevpc.nuts.io;
    exports net.thevpc.nuts.log;
    exports net.thevpc.nuts.math;
    exports net.thevpc.nuts.mon;
    exports net.thevpc.nuts.net;
    exports net.thevpc.nuts.pipeline;
    exports net.thevpc.nuts.platform;
    exports net.thevpc.nuts.reflect;
    exports net.thevpc.nuts.security;
    exports net.thevpc.nuts.spi;
    exports net.thevpc.nuts.spi.base;
    exports net.thevpc.nuts.text;
    exports net.thevpc.nuts.time;
    exports net.thevpc.nuts.util;

    // ARCH-01: runtime implements internal RPI contracts but they are not public API
    exports net.thevpc.nuts.internal to net.thevpc.nuts.runtime;
    exports net.thevpc.nuts.internal.artifact to net.thevpc.nuts.runtime;
    exports net.thevpc.nuts.internal.optional to net.thevpc.nuts.runtime;
    exports net.thevpc.nuts.internal.rpi to net.thevpc.nuts.runtime;
    exports net.thevpc.nuts.internal.util to net.thevpc.nuts.runtime;

    uses net.thevpc.nuts.boot.NBootWorkspaceFactory;
    uses net.thevpc.nuts.spi.NComponent;
}
