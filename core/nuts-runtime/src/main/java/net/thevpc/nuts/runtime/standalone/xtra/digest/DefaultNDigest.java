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
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NFormatSPI;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NStringUtils;

import java.io.*;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author thevpc
 */
public class DefaultNDigest implements NDigest {

    private final NWorkspace ws;
    private NInputSource source;
    private String algorithm;
    private NSession session;

    public DefaultNDigest(NSession session) {
        this.session = session;
        this.ws = session.getWorkspace();
    }

    @Override
    public NDigest setSource(NInputSource source) {
        this.source = source;
        return this;
    }

    public NInputSource getSource() {
        return source;
    }

    @Override
    public NDigest setSource(InputStream source) {
        if (source == null) {
            this.source = null;
        } else {
            this.source = NIO.of(session).ofInputSource(source);
        }
        return this;
    }

    @Override
    public NDigest setSource(File source) {
        this.source = source == null ? null : NPath.of(source, getSession());
        return this;
    }

    @Override
    public NDigest setSource(Path source) {
        this.source = source == null ? null : NPath.of(source, getSession());
        return this;
    }

    @Override
    public NDigest setSource(NPath source) {
        this.source = source;
        return this;
    }

    @Override
    public NDigest setSource(byte[] source) {
        checkSession();
        this.source = source == null ? null : NIO.of(session).ofInputSource(new ByteArrayInputStream(source));
        return null;
    }

    @Override
    public NDigest setSource(NDescriptor source) {
        checkSession();
        this.source = source == null ? null : new NDescriptorInputSource(source);
        ;
        return this;
    }

    @Override
    public String computeString() {
        return NStringUtils.toHexString(computeBytes());
    }

    @Override
    public byte[] computeBytes() {
        NAssert.requireNonNull(source, "source", getSession());
        try (InputStream is = new BufferedInputStream(source.getInputStream())) {
            return NDigestUtils.evalHash(is, getValidAlgo(), session);
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
        return DEFAULT_SUPPORT;
    }

    private class NDescriptorInputSource implements NInputSource {
        private final NDescriptor source;

        public NDescriptorInputSource(NDescriptor source) {
            this.source = source;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(source.formatter(session)
                    .setNtf(false)
                    .format().filteredText().getBytes()
            );
        }

        @Override
        public NInputSourceMetadata getInputMetaData() {
            NId id = source.getId();
            NString str;
            if (id != null) {
                str = id.format(session);
            } else {
                str = NTexts.of(session).ofStyled("<empty-descriptor>", NTextStyle.path());
            }
            return new DefaultNInputSourceMetadata(NMsg.ofNtf(str), -1, null, null);
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
                    NOptional<NMsg> m = getInputMetaData().getMessage();
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
            NOptional<NMsg> m = getInputMetaData().getMessage();
            if (m.isPresent()) {
                out.print(m.get());
            } else {
                out.print(getClass().getSimpleName(), NTextStyle.path());
            }
            return out.toString();
        }

    }
}
