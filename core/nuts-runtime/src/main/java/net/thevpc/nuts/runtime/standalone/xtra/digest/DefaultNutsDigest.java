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
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsFormatSPI;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.util.NutsStringUtils;
import net.thevpc.nuts.util.NutsUtils;

import java.io.*;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author thevpc
 */
public class DefaultNutsDigest implements NutsDigest {

    private final NutsWorkspace ws;
    private NutsInputSource source;
    private String algorithm;
    private NutsSession session;

    public DefaultNutsDigest(NutsSession session) {
        this.session = session;
        this.ws = session.getWorkspace();
    }

    @Override
    public NutsDigest setSource(NutsInputSource source) {
        this.source = source;
        return this;
    }

    public NutsInputSource getSource() {
        return source;
    }

    @Override
    public NutsDigest setSource(InputStream source) {
        if (source == null) {
            this.source = null;
        } else {
            this.source = NutsIO.of(session).createInputSource(source);
        }
        return this;
    }

    @Override
    public NutsDigest setSource(File source) {
        this.source = source == null ? null : NutsPath.of(source, getSession());
        return this;
    }

    @Override
    public NutsDigest setSource(Path source) {
        this.source = source == null ? null : NutsPath.of(source, getSession());
        return this;
    }

    @Override
    public NutsDigest setSource(NutsPath source) {
        this.source = source;
        return this;
    }

    @Override
    public NutsDigest setSource(byte[] source) {
        checkSession();
        this.source = source == null ? null : NutsIO.of(session).createInputSource(new ByteArrayInputStream(source));
        return null;
    }

    @Override
    public NutsDigest setSource(NutsDescriptor source) {
        checkSession();
        this.source = source == null ? null : new NutsDescriptorInputSource(source);
        ;
        return this;
    }

    @Override
    public String computeString() {
        return NutsStringUtils.toHexString(computeBytes());
    }

    @Override
    public byte[] computeBytes() {
        NutsUtils.requireNonNull(source, "source", getSession());
        try (InputStream is = new BufferedInputStream(source.getInputStream())) {
            return NutsDigestUtils.evalHash(is, getValidAlgo(), session);
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
    }

    @Override
    public NutsDigest writeHash(OutputStream out) {
        try {
            out.write(computeBytes());
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
        return this;
    }

    @Override
    public NutsDigest md5() {
        return setAlgorithm("MD5");
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsDigest setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(ws, session);
        return this;
    }

    @Override
    public NutsDigest sha1() {
        return setAlgorithm("SHA1");
    }

    @Override
    public NutsDigest sha256() {
        return setAlgorithm("SHA256");
    }

    @Override
    public NutsDigest algorithm(String algorithm) {
        return setAlgorithm(algorithm);
    }

    @Override
    public String getAlgorithm() {
        return algorithm;
    }

    @Override
    public NutsDigest setAlgorithm(String algorithm) {
        if (NutsBlankable.isBlank(algorithm)) {
            algorithm = null;
        }
        try {
            MessageDigest.getInstance(algorithm);
            this.algorithm = algorithm;
        } catch (NoSuchAlgorithmException ex) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.ofCstyle("unable to resolve algo: %s", algorithm), ex);
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
        NutsSessionUtils.checkSession(ws, session);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }

    private class NutsDescriptorInputSource implements NutsInputSource {
        private final NutsDescriptor source;

        public NutsDescriptorInputSource(NutsDescriptor source) {
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
        public NutsInputSourceMetadata getInputMetaData() {
            NutsId id = source.getId();
            NutsString str;
            if (id != null) {
                str = id.format(session);
            } else {
                str = NutsTexts.of(session).ofStyled("<empty-descriptor>", NutsTextStyle.path());
            }
            return new DefaultNutsInputSourceMetadata(NutsMessage.ofNtf(str), -1, null, null);
        }

        @Override
        public boolean isMultiRead() {
            return true;
        }

        @Override
        public NutsFormat formatter(NutsSession session) {
            return NutsFormat.of(DefaultNutsDigest.this.session, new NutsFormatSPI() {
                @Override
                public String getName() {
                    return "input-stream";
                }

                @Override
                public void print(NutsPrintStream out) {
                    NutsOptional<NutsMessage> m = getInputMetaData().getMessage();
                    if (m.isPresent()) {
                        out.print(m.get());
                    } else {
                        out.append(getClass().getSimpleName(), NutsTextStyle.path());
                    }
                }

                @Override
                public boolean configureFirst(NutsCommandLine commandLine) {
                    return false;
                }
            });
        }

        @Override
        public String toString() {
            NutsPlainPrintStream out = new NutsPlainPrintStream();
            NutsOptional<NutsMessage> m = getInputMetaData().getMessage();
            if (m.isPresent()) {
                out.print(m.get());
            } else {
                out.append(getClass().getSimpleName(), NutsTextStyle.path());
            }
            return out.toString();
        }

    }
}
