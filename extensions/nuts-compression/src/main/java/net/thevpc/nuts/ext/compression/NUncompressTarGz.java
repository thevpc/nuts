package net.thevpc.nuts.ext.compression;

import net.thevpc.nuts.io.NUncompress;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.util.NScorableContext;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NStringUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class NUncompressTarGz extends NUncompressTar {

    public NUncompressTarGz() {
    }

    @Override
    protected TarArchiveInputStream createTarInputStream(InputStream in) throws IOException {
        // Wrap the input stream in a GZIPInputStream before passing it to the Tar parser
        return new TarArchiveInputStream(new GZIPInputStream(in));
    }

    @Override
    protected NLog _LOG() {
        return NLog.of(NUncompressTarGz.class);
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