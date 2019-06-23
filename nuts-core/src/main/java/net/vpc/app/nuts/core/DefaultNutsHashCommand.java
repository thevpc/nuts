/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsHashCommand;
import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.NutsUnsupportedArgumentException;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsHashCommand implements NutsHashCommand {

    private Object value;
    private String type;
    private String algorithm;
    private NutsWorkspace ws;

    public DefaultNutsHashCommand(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public String getAlgo() {
        return algorithm;
    }

    @Override
    public String getAlgorithm() {
        return algorithm;
    }

    @Override
    public NutsHashCommand algorithm(String algo) {
        return setAlgorithm(algo);
    }

    @Override
    public NutsHashCommand setAlgorithm(String algo) {
        if (CoreStringUtils.isBlank(algo)) {
            algo = null;
        }
        try {
            MessageDigest.getInstance(algo);
            this.algorithm = algo;
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalArgumentException(ex);
        }
        return this;
    }

    @Override
    public NutsHashCommand sha1() {
        return setAlgorithm("SHA1");
    }

    @Override
    public NutsHashCommand md5() {
        return setAlgorithm("MD5");
    }

    @Override
    public NutsHashCommand source(InputStream input) {
        this.value = input;
        this.type = "stream";
        return this;
    }

    @Override
    public NutsHashCommand source(File file) {
        this.value = file;
        this.type = "file";
        return this;
    }

    @Override
    public NutsHashCommand source(Path file) {
        this.value = file;
        this.type = "path";
        return this;
    }

    @Override
    public NutsHashCommand source(NutsDescriptor file) {
        this.value = file;
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
                ws.descriptor().compact().set((NutsDescriptor) value).print(new OutputStreamWriter(o));
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
