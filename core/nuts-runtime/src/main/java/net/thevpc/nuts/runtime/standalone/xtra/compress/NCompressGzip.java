package net.thevpc.nuts.runtime.standalone.xtra.compress;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.spi.NCompressPackaging;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.time.NChronometer;
import net.thevpc.nuts.util.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.logging.Level;
import java.util.zip.GZIPOutputStream;

public class NCompressGzip implements NCompressPackaging {

    public NCompressGzip() {
    }

    @Override
    public void compressPackage(NCompress compress) {
        List<NInputSource> sources = compress.sources();
        NAssert.requireNamedNonBlank(sources, "source");

        // GZIP only supports a single file
        if (sources.size() > 1) {
            throw new NIllegalArgumentException(NMsg.ofC("gzip supports only a single source, but got %s", sources));
        }

        NInputSource source = sources.get(0);
        DefaultNCompressItem item = new DefaultNCompressItem(source, compress);
        if (item.isSourceDirectory()) {
            throw new NIllegalArgumentException(NMsg.ofC("gzip cannot compress directories"));
        }

        NOutputTarget target = compress.target();
        NAssert.requireNamedNonBlank(target, "target");
        NChronometer chronometer = NChronometer.of();
        _LOG().log(NMsg.ofC("compress %s to %s", sources, target).asFinest().withIntent(NMsgIntent.START).withLevel(Level.FINEST));

        try {
            OutputStream fW = null;
            if (target instanceof NPath) {
                Path tempPath = null;
                if (compress.isSafe()) {
                    tempPath = NPath.ofTempFile("gz").toPath().get();
                }
                ((NPath) target).mkParentDirs();

                fW = (tempPath == null) ? target.outputStream() : Files.newOutputStream(tempPath);
                try {
                    try (GZIPOutputStream gzos = new GZIPOutputStream(fW)) {
                        byte[] buf = new byte[1024];
                        int len;
                        try (InputStream in = item.open()) {
                            while ((len = in.read(buf)) > 0) {
                                gzos.write(buf, 0, len);
                            }
                        }
                    }
                } finally {
                    if (fW != null) fW.close();
                }

                if (tempPath != null) {
                    if (target instanceof NPath) {
                        Files.move(tempPath, ((NPath) target).toPath().get(), StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        try (InputStream ii = Files.newInputStream(tempPath); OutputStream jj = target.outputStream()) {
                            NIOUtils.copy(ii, jj);
                        }
                    }
                }
            } else {
                throw new NIllegalArgumentException(NMsg.ofC("unsupported target %s", target));
            }

            _LOG().log(NMsg.ofC("compressed %s to %s", sources, target).asFinest().withIntent(NMsgIntent.SUCCESS).withDurationMillis(chronometer.durationMs()));
        } catch (IOException ex) {
            _LOG().log(NMsg.ofC("error compressing %s to %s : %s", sources, target, ex).asConfig().withIntent(NMsgIntent.FAIL).withDurationMillis(chronometer.durationMs()));
            throw new NIOException(ex);
        } catch (RuntimeException ex) {
            _LOG().log(NMsg.ofC("error compressing %s to %s : %s", sources, target, ex).asConfig().withIntent(NMsgIntent.FAIL).withDurationMillis(chronometer.durationMs()));
            throw ex;
        }
    }

    protected NLog _LOG() {
        return NLog.of(NCompressGzip.class);
    }

    @NScore
    public static int getScore(NScorableContext context) {
        String z = NStringUtils.trim(context.criteria(String.class)).toLowerCase();
        if (z.equals("gzip") || z.equals("gz")) {
            return NScorable.DEFAULT_SCORE;
        }
        return NScorable.UNSUPPORTED_SCORE;
    }
}