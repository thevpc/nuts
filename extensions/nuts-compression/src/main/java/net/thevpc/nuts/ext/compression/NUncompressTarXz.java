package net.thevpc.nuts.ext.compression;

import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.util.NScorableContext;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NStringUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;

import java.io.IOException;
import java.io.InputStream;

public class NUncompressTarXz extends NUncompressTar {

    public NUncompressTarXz() {
    }

    @Override
    protected TarArchiveInputStream createTarInputStream(InputStream in) throws IOException {
        return new TarArchiveInputStream(new XZCompressorInputStream(in));
    }

    @Override
    protected NLog _LOG() {
        return NLog.of(NUncompressTarXz.class);
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