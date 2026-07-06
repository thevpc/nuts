package net.thevpc.nuts.ext.compression;

import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.util.NScorableContext;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NStringUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;

import java.io.IOException;
import java.io.OutputStream;

public class NCompressTarXz extends NCompressTar {

    public NCompressTarXz() {
    }

    @Override
    protected TarArchiveOutputStream createTarOutputStream(OutputStream out) throws IOException {
        // Wrap the output stream in GZIPOutputStream before passing it to the TAR parser
        return super.createTarOutputStream(new XZCompressorOutputStream(out));
    }

    @Override
    protected NLog _LOG() {
        return NLog.of(NCompressTarXz.class);
    }

    @NScore
    public static int getScore(NScorableContext context) {
        String z = NStringUtils.strip(context.criteria(String.class)).toLowerCase();
        if (z.equals("tar.xz") || z.equals("txz")) {
            return NScorable.DEFAULT_SCORE;
        }
        return NScorable.UNSUPPORTED_SCORE;
    }
}