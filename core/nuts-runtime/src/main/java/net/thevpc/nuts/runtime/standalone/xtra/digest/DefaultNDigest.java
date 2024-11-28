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

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.format.NFormats;
import net.thevpc.nuts.format.NTreeVisitResult;
import net.thevpc.nuts.format.NTreeVisitor;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.io.util.AbstractMultiReadNInputSource;
import net.thevpc.nuts.io.NDigest;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.*;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author thevpc
 */
@NComponentScope(NScopeType.PROTOTYPE)
public class DefaultNDigest implements NDigest {

    private final NWorkspace workspace;
    private List<NInputSource> sources;
    private String algorithm;

    public DefaultNDigest(NWorkspace workspace) {
        this.workspace = workspace;
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
        this.sources = source == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(source));
        return this;
    }

    @Override
    public NDigest setSource(NInputSource source) {
        setSource0(source);
        return this;
    }

    public List<NInputSource> getSource() {
        return sources;
    }

    @Override
    public NDigest setSource(InputStream source) {
        this.setSource0((source == null ? null : NInputSource.of(source)));
        return this;
    }

    @Override
    public NDigest setSource(File source) {
        this.setSource0((source == null ? null : NPath.of(source)));
        return this;
    }

    @Override
    public NDigest setSource(Path source) {
        this.setSource0(source == null ? null : NPath.of(source));
        return this;
    }

    @Override
    public NDigest setSource(URL url) {
        this.setSource0(url == null ? null : NPath.of(url));
        return this;
    }

    @Override
    public NDigest setSource(NPath source) {
        this.setSource0(source);
        return this;
    }

    @Override
    public NDigest setSource(byte[] source) {
        this.setSource0(source == null ? null : NInputSource.of(source));
        return this;
    }

    @Override
    public NDigest setSource(NDescriptor source) {
        this.setSource0(source == null ? null : new NDescriptorInputSource(source, workspace));
        return this;
    }

    @Override
    public String computeString() {
        return NHex.toHexString(computeBytes());
    }

    @Override
    public byte[] computeBytes() {
        NAssert.requireTrue(!(sources == null || sources.isEmpty()), "source");
        String algo = getValidAlgo();
        MessageDigest md;
        try {
            md = MessageDigest.getInstance(algo);
            for (NInputSource source : sources) {
                incrementalUpdateFileDigestInputSource(source, md);
            }
        } catch (NoSuchAlgorithmException e) {
            throw new NIOException(new IOException(e));
        }
        return md.digest();
    }

    private void incrementalUpdateFileDigestPath(NPath file, MessageDigest md) {
        if (file.isDirectory()) {
            file.walkDfs(new NTreeVisitor<NPath>() {
                @Override
                public NTreeVisitResult visitFile(NPath file) {
                    incrementalUpdateFileDigestInputStream(new ByteArrayInputStream(file.toString().getBytes()), md);
                    try (InputStream is = file.getInputStream()) {
                        incrementalUpdateFileDigestInputStream(is, md);
                    } catch (IOException ex) {
                        throw new NIOException(ex);
                    }
                    return NTreeVisitResult.CONTINUE;
                }

                @Override
                public NTreeVisitResult preVisitDirectory(NPath dir) {
                    incrementalUpdateFileDigestInputStream(new ByteArrayInputStream(dir.toString().getBytes()), md);
                    return NTreeVisitResult.CONTINUE;
                }
            });
        } else if (file.isFile()) {
            try (InputStream is = file.getInputStream()) {
                incrementalUpdateFileDigestInputStream(is, md);
            } catch (IOException ex) {
                throw new NIOException(ex);
            }
        }
    }

    private void incrementalUpdateFileDigestInputSource(NInputSource source, MessageDigest md) {
        if (source instanceof NPath) {
            NPath file = (NPath) source;
            incrementalUpdateFileDigestPath(file, md);
            return;
        }
        try (InputStream is = source.getInputStream()) {
            incrementalUpdateFileDigestInputStream(is, md);
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    private void incrementalUpdateFileDigestInputStream(InputStream inputStream, MessageDigest md) {
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
        return setAlgorithm("MD5");
    }

    @Override
    public NDigest sha1() {
        return setAlgorithm("SHA1");
    }

    @Override
    public NDigest sha256() {
        return setAlgorithm("SHA256");
    }

    @Override
    public NDigest algorithm(String algorithm) {
        return setAlgorithm(algorithm);
    }

    @Override
    public String getAlgorithm() {
        return algorithm;
    }

    @Override
    public NDigest setAlgorithm(String algorithm) {
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

    protected String getValidAlgo() {
        if (algorithm == null) {
            return "SHA1";
        }
        return algorithm;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    public class NDescriptorInputSource extends AbstractMultiReadNInputSource {
        private final NDescriptor source;

        public NDescriptorInputSource(NDescriptor source, NWorkspace workspace) {
            super(workspace);
            this.source = source;
        }

        private byte[] getBytes() {
            return NDescriptorFormat.of(source)
                    .setNtf(false)
                    .format().filteredText().getBytes();
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(getBytes());
        }

        @Override
        public boolean isKnownContentLength() {
            return true;
        }

        @Override
        public long getContentLength() {
            return NDescriptorFormat.of(source)
                    .setNtf(false)
                    .format().filteredText().getBytes().length;
        }

        @Override
        public NContentMetadata getMetaData() {
            NId id = source.getId();
            NText str;
            if (id != null) {
                str = NFormats.of(id).get().format();
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
            NPlainPrintStream out = new NPlainPrintStream();
            NOptional<NMsg> m = getMetaData().getMessage();
            if (m.isPresent()) {
                out.print(m.get());
            } else {
                out.print(getClass().getSimpleName(), NTextStyle.path());
            }
            return out.toString();
        }

    }
}
