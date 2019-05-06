/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
            this.algorithm=algo;
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
            throw new NutsIllegalArgumentException("Missing Source");
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
                ws.formatter().createDescriptorFormat().setPretty(false).print((NutsDescriptor) value, o);
                try (InputStream is = new ByteArrayInputStream(o.toByteArray())) {
                    return CoreIOUtils.evalHash(is, getValidAlgo());
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
            default: {
                throw new NutsUnsupportedArgumentException("Unsupported type " + type);
            }
        }
    }

}
