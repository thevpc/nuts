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
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.thevpc.nuts.runtime.io;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.thevpc.nuts.NutsDescriptor;
import net.thevpc.nuts.NutsIOHashAction;
import net.thevpc.nuts.NutsIllegalArgumentException;
import net.thevpc.nuts.NutsUnsupportedArgumentException;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.util.io.CoreIOUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsIOHashAction implements NutsIOHashAction {

    private Object value;
    private String type;
    private String algorithm;
    private NutsWorkspace ws;

    public DefaultNutsIOHashAction(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public String getAlgorithm() {
        return algorithm;
    }

    @Override
    public NutsIOHashAction algorithm(String algorithm) {
        return setAlgorithm(algorithm);
    }

    @Override
    public NutsIOHashAction setAlgorithm(String algorithm) {
        if (CoreStringUtils.isBlank(algorithm)) {
            algorithm = null;
        }
        try {
            MessageDigest.getInstance(algorithm);
            this.algorithm = algorithm;
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalArgumentException(ex);
        }
        return this;
    }

    @Override
    public NutsIOHashAction sha1() {
        return setAlgorithm("SHA1");
    }

    @Override
    public NutsIOHashAction md5() {
        return setAlgorithm("MD5");
    }

    @Override
    public NutsIOHashAction source(InputStream input) {
        this.value = input;
        this.type = "stream";
        return this;
    }

    @Override
    public NutsIOHashAction source(File file) {
        this.value = file;
        this.type = "file";
        return this;
    }

    @Override
    public NutsIOHashAction source(Path path) {
        this.value = path;
        this.type = "path";
        return this;
    }

    @Override
    public NutsIOHashAction source(NutsDescriptor descriptor) {
        this.value = descriptor;
        this.type = "desc";
        return this;
    }

    @Override
    public String computeString() {
        return CoreIOUtils.toHexString(computeBytes());
    }

    protected String getValidAlgo() {
        if (algorithm == null) {
            return "SHA1";
        }
        return algorithm;
    }

    @Override
    public NutsIOHashAction writeHash(OutputStream out) {
        if (type == null || value == null) {
            throw new NutsIllegalArgumentException(ws, "Missing Source");
        }
        switch (type) {
            case "stream": {
                try {
                    out.write(CoreIOUtils.evalHash(((InputStream) value), getValidAlgo()));
                    return this;
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
            case "file": {
                try (InputStream is = new BufferedInputStream(Files.newInputStream(((File) value).toPath()))) {
                    out.write(CoreIOUtils.evalHash(is, getValidAlgo()));
                    return this;
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
            case "path": {
                try (InputStream is = new BufferedInputStream(Files.newInputStream(((Path) value)))) {
                    out.write(CoreIOUtils.evalHash(is, getValidAlgo()));
                    return this;
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
            case "desc": {
                ByteArrayOutputStream o = new ByteArrayOutputStream();
                ws.descriptor().formatter((NutsDescriptor) value).compact().print(new OutputStreamWriter(o));
                try (InputStream is = new ByteArrayInputStream(o.toByteArray())) {
                    out.write(CoreIOUtils.evalHash(is, getValidAlgo()));
                    return this;
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
            default: {
                throw new NutsUnsupportedArgumentException(ws, "Unsupported type " + type);
            }
        }
    }

    @Override
    public byte[] computeBytes() {
        if (type == null || value == null) {
            throw new NutsIllegalArgumentException(ws, "Missing Source");
        }
        switch (type) {
            case "stream": {
                return CoreIOUtils.evalHash(((InputStream) value), getValidAlgo());
            }
            case "file": {
                try (InputStream is = new BufferedInputStream(Files.newInputStream(((File) value).toPath()))) {
                    return CoreIOUtils.evalHash(is, getValidAlgo());
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
            case "path": {
                try (InputStream is = new BufferedInputStream(Files.newInputStream(((Path) value)))) {
                    return CoreIOUtils.evalHash(is, getValidAlgo());
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
            case "desc": {
                ByteArrayOutputStream o = new ByteArrayOutputStream();
                ws.descriptor().formatter((NutsDescriptor) value).compact().print(new OutputStreamWriter(o));
                try (InputStream is = new ByteArrayInputStream(o.toByteArray())) {
                    return CoreIOUtils.evalHash(is, getValidAlgo());
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
            default: {
                throw new NutsUnsupportedArgumentException(ws, "Unsupported type " + type);
            }
        }
    }

}
