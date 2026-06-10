package net.thevpc.nuts.ext.compression;

import net.thevpc.nuts.io.NCompress;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.util.NScorableContext;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NStringUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public class NCompressTarGz extends NCompressTar {

    public NCompressTarGz() {
    }

    @Override
    protected TarArchiveOutputStream createTarOutputStream(OutputStream out) throws IOException {
        // Wrap the output stream in GZIPOutputStream before passing it to the TAR parser
        return super.createTarOutputStream(new GZIPOutputStream(out));
    }

    @Override
    protected NLog _LOG() {
        return NLog.of(NCompressTarGz.class);
    }

    @NScore
    public static int getScore(NScorableContext context) {
        String z = NStringUtils.trim(context.criteria(String.class)).toLowerCase();
        if (z.equals("tar.gz") || z.equals("tgz")) {
            return NScorable.DEFAULT_SCORE;
        }
        return NScorable.UNSUPPORTED_SCORE;
    }
}