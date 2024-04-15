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
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
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
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.format.NFormat;
import net.thevpc.nuts.format.NTreeVisitResult;
import net.thevpc.nuts.format.NTreeVisitor;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.io.util.AbstractMultiReadNInputSource;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.io.NDigest;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NFormatSPI;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NString;
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

    private final NWorkspace ws;
    private List<NInputSource> sources;
    private String algorithm;
    private NSession session;

    public DefaultNDigest(NSession session) {
        this.session = session;
        this.ws = session.getWorkspace();
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
        this.setSource0((source == null ? null : NInputSource.of(source,session)));
        return this;
    }

    @Override
    public NDigest setSource(File source) {
        this.setSource0((source == null ? null : NPath.of(source, getSession())));
        return this;
    }

    @Override
    public NDigest setSource(Path source) {
        this.setSource0(source == null ? null : NPath.of(source, getSession()));
        return this;
    }

    @Override
    public NDigest setSource(URL url) {
        this.setSource0(url == null ? null : NPath.of(url, getSession()));
        return this;
    }

    @Override
    public NDigest setSource(NPath source) {
        this.setSource0(source);
        return this;
    }

    @Override
    public NDigest setSource(byte[] source) {
        checkSession();
        this.setSource0(source == null ? null : NInputSource.of(source,session));
        return this;
    }

    @Override
    public NDigest setSource(NDescriptor source) {
        checkSession();
        this.setSource0(source == null ? null : new NDescriptorInputSource(source, session));
        return this;
    }

    @Override
    public String computeString() {
        return NStringUtils.toHexString(computeBytes());
    }

    @Override
    public byte[] computeBytes() {
        NAssert.requireTrue(!(sources == null || sources.isEmpty()), "source", getSession());
        String algo = getValidAlgo();
        MessageDigest md;
        try {
            md = MessageDigest.getInstance(algo);
            for (NInputSource source : sources) {
                incrementalUpdateFileDigestInputSource(source, md);
            }
        } catch (NoSuchAlgorithmException e) {
            throw new NIOException(session, new IOException(e));
        }
        return md.digest();
    }

    private void incrementalUpdateFileDigestPath(NPath file, MessageDigest md) {
        if (file.isDirectory()) {
            file.walkDfs(new NTreeVisitor<NPath>() {
                @Override
                public NTreeVisitResult visitFile(NPath file, NSession session) {
                    incrementalUpdateFileDigestInputStream(new ByteArrayInputStream(file.toString().getBytes()), md);
                    try (InputStream is = file.getInputStream()) {
                        incrementalUpdateFileDigestInputStream(is, md);
                    } catch (IOException ex) {
                        throw new NIOException(session, ex);
                    }
                    return NTreeVisitResult.CONTINUE;
                }

                @Override
                public NTreeVisitResult preVisitDirectory(NPath dir, NSession session) {
                    incrementalUpdateFileDigestInputStream(new ByteArrayInputStream(dir.toString().getBytes()), md);
                    return NTreeVisitResult.CONTINUE;
                }
            });
        } else if (file.isFile()) {
            try (InputStream is = file.getInputStream()) {
                incrementalUpdateFileDigestInputStream(is, md);
            } catch (IOException ex) {
                throw new NIOException(session, ex);
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
            throw new NIOException(session, ex);
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
                throw new NIOException(session, e);
            }
        } catch (IOException ex) {
            throw new NIOException(session, ex);
        }
    }

    @Override
    public NDigest writeHash(OutputStream out) {
        try {
            out.write(computeBytes());
        } catch (IOException ex) {
            throw new NIOException(session, ex);
        }
        return this;
    }

    @Override
    public NDigest md5() {
        return setAlgorithm("MD5");
    }

    @Override
    public NSession getSession() {
        return session;
    }

    @Override
    public NDigest setSession(NSession session) {
        this.session = NWorkspaceUtils.bindSession(ws, session);
        return this;
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
            throw new NIllegalArgumentException(getSession(), NMsg.ofC("unable to resolve algo: %s", algorithm), ex);
        }
        return this;
    }

    protected String getValidAlgo() {
        if (algorithm == null) {
            return "SHA1";
        }
        return algorithm;
    }

    protected void checkSession() {
        NSessionUtils.checkSession(ws, session);
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    private class NDescriptorInputSource extends AbstractMultiReadNInputSource {
        private final NDescriptor source;

        public NDescriptorInputSource(NDescriptor source, NSession session) {
            super(session);
            this.source = source;
        }

        private byte[] getBytes() {
            return source.formatter(session)
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
            return source.formatter(session)
                    .setNtf(false)
                    .format().filteredText().getBytes().length;
        }

        @Override
        public NContentMetadata getMetaData() {
            NId id = source.getId();
            NString str;
            if (id != null) {
                str = id.format(session);
            } else {
                str = NTexts.of(session).ofStyled("<empty-descriptor>", NTextStyle.path());
            }
            return new DefaultNContentMetadata(NMsg.ofNtf(str), null, null, null, null);
        }

        @Override
        public boolean isMultiRead() {
            return true;
        }

        @Override
        public NFormat formatter(NSession session) {
            return NFormat.of(DefaultNDigest.this.session, new NFormatSPI() {
                @Override
                public String getName() {
                    return "input-stream";
                }

                @Override
                public void print(NPrintStream out) {
                    NOptional<NMsg> m = getMetaData().getMessage();
                    if (m.isPresent()) {
                        out.print(m.get());
                    } else {
                        out.print(getClass().getSimpleName(), NTextStyle.path());
                    }
                }

                @Override
                public boolean configureFirst(NCmdLine cmdLine) {
                    return false;
                }
            });
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
