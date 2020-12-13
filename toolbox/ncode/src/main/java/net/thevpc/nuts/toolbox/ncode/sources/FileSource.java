/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ncode.sources;

import net.thevpc.nuts.toolbox.ncode.Source;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author thevpc
 */
public class FileSource implements Source {

    private File file;

    public FileSource(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public String getExternalPath() {
        return file.getPath();
    }

    @Override
    public String getInternalPath() {
        return file.getPath();
    }

    @Override
    public Iterable<Source> getChildren() {
        List<Source> found = new ArrayList<>();
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                found.add(SourceFactory.create(f));
            }
        }
        return found;
    }

    @Override
    public boolean isStream() {
        return !file.isDirectory();
    }

    @Override
    public boolean isFolder() {
        return file.isDirectory();
    }

    @Override
    public InputStream openStream() throws IOException {
        return new FileInputStream(getFile());
    }

}
