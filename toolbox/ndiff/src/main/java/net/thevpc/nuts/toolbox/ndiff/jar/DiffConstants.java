package net.thevpc.nuts.toolbox.ndiff.jar;

import net.thevpc.nuts.toolbox.ndiff.jar.util.DiffUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.jar.Manifest;

public class DiffConstants {
    public static final DiffHash HASH_SHA1 = x -> DiffUtils.hashStream(x);
    public static final DiffHash HASH_MANIFEST_SHA1 = x -> {
        Manifest m = DiffUtils.prepareManifest(new Manifest(x));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        m.write(os);
        return DiffUtils.hashStream(new ByteArrayInputStream(os.toByteArray()));
    };

    public static final DiffHashFactory DEFAULTS_FACTORY = entryName -> HASH_SHA1;

    public static final DiffHashFactory JAR_DEFAULTS_FACTORY = entryName ->
            entryName.endsWith("/MANIFEST.MF")? HASH_MANIFEST_SHA1 : HASH_SHA1;
}
