/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.nuts.toolbox.feenoo.sources;

import net.vpc.nuts.toolbox.feenoo.Source;
import net.vpc.nuts.toolbox.feenoo._Utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author vpc
 */
public class ZipEntrySource implements Source {

    private boolean stream;
    private boolean directory;
    private String name;
    private String prefixPath;
    private String internalPath;
    private ZipEntry entry;
    private ZipInputStream zip;
    private byte[] bytes;

    public ZipEntrySource(ZipEntry entry, ZipInputStream zip, String prefixPath) {
        this.stream = !entry.getName().endsWith("/");
        this.directory = !stream;
        this.entry = entry;
        this.name = new File(entry.getName()).getName();
        this.internalPath = entry.getName();
        this.prefixPath = prefixPath;
        this.zip = zip;
    }

    @Override
    public boolean isStream() {
        return stream;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getExternalPath() {
        return prefixPath + ":" + internalPath;
    }

    @Override
    public String getInternalPath() {
        return internalPath;
    }


    @Override
    public boolean isFolder() {
        return directory;
    }

    @Override
    public InputStream openStream() throws IOException {
        if (bytes == null) {
            bytes = _Utils.toByteArray(zip);
        }
        return new ByteArrayInputStream(bytes);
    }

    @Override
    public Iterable<Source> getChildren() {
        return Collections.EMPTY_LIST;
    }


}
