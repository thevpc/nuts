/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.xtra.digest;

import net.thevpc.nuts.artifact.NDescriptor;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.io.util.AbstractMultiReadNInputSource;
import net.thevpc.nuts.io.NDigest;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.util.*;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
@NComponentScope(NScopeType.PROTOTYPE)
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNDigest implements NDigest {

    private List<NInputSource> sources;
    private String algorithm;

    public DefaultNDigest() {
    }

    public NDigest addSource(NInputSource source) {
        return addSource0(source);
    }

    private NDigest addSource0(NInputSource source) {
        if (source != null) {
            if (this.sources == null) {
                this.sources = new ArrayList<>();
            }
            this.sources.add(source);
        }
        return this;
    }

    private NDigest setSource0(NInputSource source) {
        this.sources = source == null ? new ArrayList<>() : new ArrayList<>(Collections.singletonList(source));
        return this;
    }

    @Override
    public NDigest source(NInputSource source) {
        addSource0(source);
        return this;
    }

    public List<NInputSource> source() {
        return sources;
    }

    @Override
    public NDigest source(InputStream source) {
        this.addSource0((source == null ? null : NInputSource.of(source)));
        return this;
    }

    @Override
    public NDigest source(File source) {
        this.addSource0((source == null ? null : NPath.of(source)));
        return this;
    }

    @Override
    public NDigest source(Path source) {
        this.addSource0(source == null ? null : NPath.of(source));
        return this;
    }

    @Override
    public NDigest source(URL url) {
        this.setSource0(url == null ? null : NPath.of(url));
        return this;
    }

    @Override
    public NDigest source(NPath source) {
        this.addSource0(source);
        return this;
    }

    @Override
    public NDigest source(byte[] source) {
        this.addSource0(source == null ? null : NInputSource.of(source));
        return this;
    }

    @Override
    public NDigest source(NDescriptor source) {
        this.addSource(source == null ? null : new NDescriptorInputSource(source));
        return this;
    }

    @Override
    public NDigest addSource(InputStream source) {
        this.addSource0((source == null ? null : NInputSource.of(source)));
        return this;
    }

    @Override
    public NDigest addSource(File source) {
        this.addSource0((source == null ? null : NPath.of(source)));
        return this;
    }

    @Override
    public NDigest addSource(Path source) {
        this.addSource0(source == null ? null : NPath.of(source));
        return this;
    }

    @Override
    public NDigest addSource(URL url) {
        this.addSource0(url == null ? null : NPath.of(url));
        return this;
    }

    @Override
    public NDigest addSource(NPath source) {
        this.addSource0(source);
        return this;
    }

    @Override
    public NDigest addSource(byte[] source) {
        this.addSource0(source == null ? null : NInputSource.of(source));
        return this;
    }

    @Override
    public NDigest addSource(NDescriptor source) {
        this.addSource0(source == null ? null : new NDescriptorInputSource(source));
        return this;
    }

    @Override
    public String computeString() {
        return NHex.fromBytes(computeBytes());
    }

    @Override
    public String computeManifestString() {
        BytesAndName u = computeBytesAndName();
        byte[] a = computeBytes();
        return NHex.fromBytes(a) + " " + (u.binary ? "*" : " ") + u.names.stream().sorted().collect(Collectors.joining(","));
    }

    private static class BytesAndName {
        byte[] bytes;
        List<String> names = new ArrayList<>();
        boolean binary;
    }

    private BytesAndName computeBytesAndName() {
        NAssert.requireNamedTrue(!(sources == null || sources.isEmpty()), "source");
        String algo = getValidAlgo();
        MessageDigest md;
        BytesAndName a = new BytesAndName();
        try {
            md = MessageDigest.getInstance(algo);
            for (NInputSource source : sources) {
                BytesAndName a0 = incrementalUpdateFileDigestInputSource(source, md);
                if (a0 != null) {
                    a.names.addAll(a0.names);
                    a.binary |= a0.binary;
                }
            }
        } catch (NoSuchAlgorithmException e) {
            throw new NIOException(new IOException(e));
        }
        a.bytes = md.digest();
        return a;
    }

    @Override
    public byte[] computeBytes() {
        return computeBytesAndName().bytes;
    }

    private BytesAndName incrementalUpdateFileDigestPath(NPath file, MessageDigest md) {
        if (file.isDirectory()) {
            file.walkDfs(new NTreeVisitor<NPath>() {
                @Override
                public NTreeVisitResult visitFile(NPath file) {
                    incrementalUpdateFileDigestInputStream(new ByteArrayInputStream(file.name().getBytes(StandardCharsets.UTF_8)), md, file.name());
                    try (InputStream is = file.inputStream()) {
                        incrementalUpdateFileDigestInputStream(is, md, file.name());
                    } catch (IOException ex) {
                        throw new NIOException(ex);
                    }
                    return NTreeVisitResult.CONTINUE;
                }

                @Override
                public NTreeVisitResult preVisitDirectory(NPath dir) {
                    incrementalUpdateFileDigestInputStream(new ByteArrayInputStream(dir.name().getBytes(StandardCharsets.UTF_8)), md, dir.name() + "/");
                    return NTreeVisitResult.CONTINUE;
                }
            }, NPathOption.SORTED);
            BytesAndName i = new BytesAndName();
            i.names.add(file.name() + "/");
            i.binary = true;
            return i;
        } else if (file.isFile()) {
            try (InputStream is = file.inputStream()) {
                return incrementalUpdateFileDigestInputStream(is, md, file.name());
            } catch (IOException ex) {
                throw new NIOException(ex);
            }
        }
        return null;
    }

    private BytesAndName incrementalUpdateFileDigestInputSource(NInputSource source, MessageDigest md) {
        if (source instanceof NPath) {
            NPath file = (NPath) source;
            return incrementalUpdateFileDigestPath(file, md);
        }
        try (InputStream is = source.inputStream()) {
            return incrementalUpdateFileDigestInputStream(is, md, "binary");
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    private BytesAndName incrementalUpdateFileDigestInputStream(InputStream inputStream, MessageDigest md, String name) {
        try (InputStream is = new BufferedInputStream(inputStream)) {
            byte[] buffer = new byte[8192];
            int len = 0;
            try {
                len = is.read(buffer);
                while (len != -1) {
                    md.update(buffer, 0, len);
                    len = is.read(buffer);
                }
            } catch (IOException e) {
                throw new NIOException(e);
            }
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
        BytesAndName u = new BytesAndName();
        u.names.add(name);
        u.binary = true;
        return u;
    }

    @Override
    public NDigest writeHash(OutputStream out) {
        try {
            out.write(computeBytes());
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
        return this;
    }

    @Override
    public NDigest md5() {
        return algorithm("MD5");
    }

    @Override
    public NDigest sha1() {
        return algorithm("SHA1");
    }

    @Override
    public NDigest sha256() {
        return algorithm("SHA256");
    }

    @Override
    public NDigest algorithm(String algorithm) {
        if (NBlankable.isBlank(algorithm)) {
            algorithm = null;
        }
        try {
            MessageDigest.getInstance(algorithm);
            this.algorithm = algorithm;
        } catch (NoSuchAlgorithmException ex) {
            throw new NIllegalArgumentException(NMsg.ofC("unable to resolve algo: %s", algorithm), ex);
        }
        return this;
    }

    @Override
    public String algorithm() {
        return algorithm;
    }


    protected String getValidAlgo() {
        if (algorithm == null) {
            return "SHA1";
        }
        return algorithm;
    }

    public class NDescriptorInputSource extends AbstractMultiReadNInputSource {
        private final NDescriptor source;

        public NDescriptorInputSource(NDescriptor source) {
            super();
            this.source = source;
        }

        private byte[] getBytes() {
            return NDescriptorWriter.of()
                    .ntf(false)
                    .format(source).filteredText().getBytes();
        }

        @Override
        public InputStream inputStream() {
            return new ByteArrayInputStream(getBytes());
        }

        @Override
        public boolean isKnownContentLength() {
            return true;
        }

        @Override
        public long contentLength() {
            return NDescriptorWriter.of()
                    .ntf(false)
                    .format(source).filteredText().getBytes().length;
        }

        @Override
        public NContentMetadata metaData() {
            NId id = source.id();
            NText str;
            if (id != null) {
                str = NObjectWriter.of(id).format(id);
            } else {
                str = NText.ofStyled("<empty-descriptor>", NTextStyle.path());
            }
            return new DefaultNContentMetadata(NMsg.ofNtf(str), null, null, null, null);
        }

        @Override
        public boolean isMultiRead() {
            return true;
        }

        @Override
        public String toString() {
            NMemoryPrintStream out = NPrintStream.ofMem(NTerminalMode.FILTERED);
            NOptional<NMsg> m = metaData().message();
            if (m.isPresent()) {
                out.print(m.get());
            } else {
                out.print(getClass().getSimpleName(), NTextStyle.path());
            }
            return out.toString();
        }

    }
}
