package net.thevpc.nuts.ext.compression;

import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.util.NFunction;

import java.io.InputStream;

public class DefaultNCompressItem1 {

    private final NInputSource inSource;
    private final NCompress c;

    public DefaultNCompressItem1(NInputSource value, NCompress c) {
        this.inSource = value;
        this.c = c;
    }

    public boolean isSourcePath() {
        return ((inSource instanceof NPath));
    }

    public boolean isSourceDirectory() {
        return isSourcePath() && ((NPath) inSource).isDirectory();
    }

    public DefaultNCompressItem1[] list() {
        if (isSourcePath()) {
            NPath p = (NPath) inSource;
            return p.stream().map(
                            NFunction.of(
                                    (NPath x) -> new DefaultNCompressItem1(x, c)
                            ).withDescription(NDescribables.ofDesc("NutsStreamOrPath::of"))
                    )
                    .toArray(DefaultNCompressItem1[]::new);
        }
        return new DefaultNCompressItem1[0];
    }

    public InputStream open() {
        if (c.options().contains(NPathOption.LOG)
                || c.options().contains(NPathOption.TRACE)
                || c.progressFactory() != null) {
            NInputStreamMonitor monitor = NInputStreamMonitor.of();
            monitor.origin(inSource);
            monitor.logProgress(c.options().contains(NPathOption.LOG));
            monitor.traceProgress(c.options().contains(NPathOption.TRACE));
            monitor.progressFactory(c.progressFactory());
            monitor.source(inSource);
            return monitor.create();
        }
        return inSource.inputStream();
    }

    public String getName() {
        return inSource.metaData().name().orElse(inSource.toString());
    }
}
