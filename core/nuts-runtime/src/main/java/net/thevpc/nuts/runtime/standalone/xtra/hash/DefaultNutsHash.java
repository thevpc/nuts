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
package net.thevpc.nuts.runtime.standalone.xtra.hash;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.io.NutsStreamOrPath;
import net.thevpc.nuts.runtime.standalone.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.io.*;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author thevpc
 */
public class DefaultNutsHash implements NutsHash {

    private NutsStreamOrPath source;
    private String algorithm;
    private final NutsWorkspace ws;
    private NutsSession session;

    public DefaultNutsHash(NutsSession session) {
        this.session = session;
        this.ws = session.getWorkspace();
    }

    @Override
    public NutsHash setSource(InputStream source) {
        this.source = source == null ? null : NutsStreamOrPath.of(source);
        return this;
    }

    @Override
    public NutsHash setSource(File source) {
        this.source = source == null ? null : NutsStreamOrPath.of(source, getSession());
        return this;
    }

    @Override
    public NutsHash setSource(Path source) {
        this.source = source == null ? null : NutsStreamOrPath.of(source, getSession());
        return this;
    }

    @Override
    public NutsHash setSource(NutsPath source) {
        this.source = source == null ? null : NutsStreamOrPath.of(source);
        return this;
    }

    @Override
    public NutsHash setSource(byte[] source) {
        this.source = source == null ? null : NutsStreamOrPath.of(new ByteArrayInputStream(source));
        return null;
    }

    @Override
    public NutsHash setSource(NutsDescriptor source) {
        this.source = source == null ? null : NutsStreamOrPath.ofSpecial(source, NutsStreamOrPath.Type.DESCRIPTOR);
        return this;
    }

    @Override
    public String computeString() {
        return NutsUtilStrings.toHexString(computeBytes());
    }

    @Override
    public byte[] computeBytes() {
        if (source == null) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("missing Source"));
        }
        checkSession();
        switch (source.getType()) {
            case INPUT_STREAM: {
                return CoreIOUtils.evalHash(source.getInputStream(), getValidAlgo(),session);
            }
            case PATH: {
                try (InputStream is = new BufferedInputStream(source.getInputStream())) {
                    return CoreIOUtils.evalHash(is, getValidAlgo(),session);
                } catch (IOException ex) {
                    throw new NutsIOException(session, ex);
                }
            }
            case DESCRIPTOR: {
                ByteArrayOutputStream o = new ByteArrayOutputStream();
                ((NutsDescriptor) source.getValue()).formatter().setSession(session)
                        .compact().setSession(session).print(new OutputStreamWriter(o));
                try (InputStream is = new ByteArrayInputStream(o.toByteArray())) {
                    return CoreIOUtils.evalHash(is, getValidAlgo(),session);
                } catch (IOException ex) {
                    throw new NutsIOException(session, ex);
                }
            }
            default: {
                throw new NutsUnsupportedArgumentException(getSession(), NutsMessage.cstyle("unsupported type %s", source.getType()));
            }
        }
    }

    @Override
    public NutsHash writeHash(OutputStream out) {
        try {
            out.write(computeBytes());
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
        return this;
    }

    @Override
    public NutsHash md5() {
        return setAlgorithm("MD5");
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsHash setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(ws, session);
        return this;
    }

    @Override
    public NutsHash sha1() {
        return setAlgorithm("SHA1");
    }

    @Override
    public NutsHash sha256() {
        return setAlgorithm("SHA256");
    }

    @Override
    public NutsHash algorithm(String algorithm) {
        return setAlgorithm(algorithm);
    }

    @Override
    public String getAlgorithm() {
        return algorithm;
    }

    @Override
    public NutsHash setAlgorithm(String algorithm) {
        if (NutsBlankable.isBlank(algorithm)) {
            algorithm = null;
        }
        try {
            MessageDigest.getInstance(algorithm);
            this.algorithm = algorithm;
        } catch (NoSuchAlgorithmException ex) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("unable to resolve algo: %s", algorithm), ex);
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
        NutsWorkspaceUtils.checkSession(ws, session);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
